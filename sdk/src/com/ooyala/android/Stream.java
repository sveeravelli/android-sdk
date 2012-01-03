package com.ooyala.android;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.json.*;

import android.util.Base64;

import com.ooyala.android.Constants.ReturnState;

public class Stream
{
  protected String _deliveryType = null;
  protected String _videoCodec = null;
  protected String _urlFormat = null;
  protected String _framerate = null;
  protected int _videoBitrate = -1;
  protected int _audioBitrate = -1;
  protected int _height = -1;
  protected int _width = -1;
  protected String _url = null;
  protected String _aspectRatio = null;
  protected boolean _isLiveStream = false;

  public Stream()
  {
  }

  public Stream(JSONObject data)
  {
    update(data);
  }

  public ReturnState update(JSONObject data)
  {
    if (data.isNull(Constants.KEY_DELIVERY_TYPE))
    {
      System.out.println("ERROR: Fail to update stream with dictionary because no delivery_type exists!");
      return ReturnState.STATE_FAIL;
    }
    if (data.isNull(Constants.KEY_URL))
    {
      System.out.println("ERROR: Fail to update stream with dictionary because no url element exists!");
      return ReturnState.STATE_FAIL;
    }

    JSONObject urlData = null;
    try
    {
      urlData = data.getJSONObject(Constants.KEY_URL);
    }
    catch (JSONException exception)
    {
      System.out.println("ERROR: Fail to update stream with dictionary because url element is invalid.");
      return ReturnState.STATE_FAIL;
    }

    if (urlData.isNull(Constants.KEY_DATA))
    {
      System.out.println("ERROR: Fail to update stream with dictionary because no url.data exists!");
      return ReturnState.STATE_FAIL;
    }
    if (urlData.isNull(Constants.KEY_FORMAT))
    {
      System.out.println("ERROR: Fail to update stream with dictionary because no url.format exists!");
      return ReturnState.STATE_FAIL;
    }

    try
    {
      _deliveryType = data.getString(Constants.KEY_DELIVERY_TYPE);
      _url = urlData.getString(Constants.KEY_DATA);
      _urlFormat = urlData.getString(Constants.KEY_FORMAT);
      _videoBitrate = data.isNull(Constants.KEY_VIDEO_BITRATE) ? _videoBitrate : data.getInt(Constants.KEY_VIDEO_BITRATE);
      _audioBitrate = data.isNull(Constants.KEY_AUDIO_BITRATE) ? _audioBitrate : data.getInt(Constants.KEY_AUDIO_BITRATE);
      _videoCodec = data.isNull(Constants.KEY_VIDEO_CODEC) ? _videoCodec : data.getString(Constants.KEY_VIDEO_CODEC);
      _height = data.isNull(Constants.KEY_HEIGHT) ? _height : data.getInt(Constants.KEY_HEIGHT);
      _width = data.isNull(Constants.KEY_WIDTH) ? _width : data.getInt(Constants.KEY_WIDTH);
      _framerate = data.isNull(Constants.KEY_FRAMERATE) ? _framerate : data.getString(Constants.KEY_FRAMERATE);
      _aspectRatio = data.isNull(Constants.KEY_ASPECT_RATIO) ? _aspectRatio : data.getString(Constants.KEY_ASPECT_RATIO);
      _isLiveStream = data.isNull(Constants.KEY_IS_LIVE_STREAM) ? _isLiveStream : data.getBoolean(Constants.KEY_IS_LIVE_STREAM);
    }
    catch (JSONException jsonException)
    {
      System.out.println("ERROR: Fail to update stream with dictionary because of invalid JSON: " + jsonException);
      return ReturnState.STATE_FAIL;
    }
    return ReturnState.STATE_MATCHED;
  }

  public String getDeliveryType()
  {
    return _deliveryType;
  }

  public void setDeliveryType(String deliveryType)
  {
    this._deliveryType = deliveryType;
  }

  public String getVideoCodec()
  {
    return _videoCodec;
  }

  public void setVideoCodec(String videoCodec)
  {
    this._videoCodec = videoCodec;
  }

  public String getUrlFormat()
  {
    return _urlFormat;
  }

  public void setUrlFormat(String urlFormat)
  {
    this._urlFormat = urlFormat;
  }

  public String getFramerate()
  {
    return _framerate;
  }

  public void setFramerate(String framerate)
  {
    this._framerate = framerate;
  }

  public int getVideoBitrate()
  {
    return _videoBitrate;
  }

  public void setVideoBitrate(int videoBitrate)
  {
    this._videoBitrate = videoBitrate;
  }

  public int getAudioBitrate()
  {
    return _audioBitrate;
  }

  public void setAudioBitrate(int audioBitrate)
  {
    this._audioBitrate = audioBitrate;
  }

  public int getHeight()
  {
    return _height;
  }

  public void setHeight(int height)
  {
    this._height = height;
  }

  public int getWidth()
  {
    return _width;
  }

  public void setWidth(int width)
  {
    this._width = width;
  }

  public String getUrl()
  {
    return _url;
  }

  public void setUrl(String url)
  {
    this._url = url;
  }

  public String getAspectRatio()
  {
    return _aspectRatio;
  }

  public void setAspectRatio(String aspectRatio)
  {
    this._aspectRatio = aspectRatio;
  }

  public boolean isLiveStream()
  {
    return _isLiveStream;
  }

  public void setLiveStream(boolean isLiveStream)
  {
    this._isLiveStream = isLiveStream;
  }

  public int getCombinedBitrate()
  {
    return (_videoBitrate + _audioBitrate);
  }

  public URL decodedURL()
  {
    try
    {
      if (_urlFormat.equals(Constants.STREAM_URL_FORMAT_B64))
      {
        return new URL(new String(Base64.decode(_url, Base64.DEFAULT)));
      }
      return new URL(_url); // Otherwise assume plain text
    }
    catch (MalformedURLException exception)
    {
      System.out.println("Malformed URL: " + _url);
      return null;
    }
  }

  public static boolean isDeliveryTypePlayable(Stream stream)
  {
    String type = stream.getDeliveryType();
    return type.equals(Constants.DELIVERY_TYPE_MP4) || type.equals(Constants.DELIVERY_TYPE_REMOTE_ASSET);
  }

  public static Stream bestStream(Set<Stream> streams)
  {
    if (streams == null || streams.size() == 0) { return null; }

    Stream lowestBitrateStream = null;
    for (Stream stream : streams)
    {
      // for remote assets, just pick the first stream
      if (stream.getDeliveryType().equals(Constants.DELIVERY_TYPE_REMOTE_ASSET))
      {
        return stream;
      }
      if (Stream.isDeliveryTypePlayable(stream) &&
          (lowestBitrateStream == null ||
           stream.getCombinedBitrate() < lowestBitrateStream.getCombinedBitrate() ||
           (stream.getCombinedBitrate() == lowestBitrateStream.getCombinedBitrate() &&
            stream.getHeight() < lowestBitrateStream.getHeight())))
      {
        lowestBitrateStream = stream;
      }
    }

    return lowestBitrateStream;
  }

}
