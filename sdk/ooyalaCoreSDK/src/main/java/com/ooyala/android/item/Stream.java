package com.ooyala.android.item;

import android.os.Build;
import android.util.Base64;

import com.ooyala.android.StreamSelector;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

public class Stream implements JSONUpdatableItem {
  static final String KEY_VIDEO_BITRATE = "video_bitrate";
  static final String KEY_AUDIO_BITRATE = "audio_bitrate";
  static final String KEY_VIDEO_CODEC = "video_codec";
  static final String KEY_FRAMERATE = "framerate";
  static final String KEY_DELIVERY_TYPE = "delivery_type";
  static final String KEY_DATA = "data";
  static final String KEY_FORMAT = "format";
  static final String KEY_IS_LIVE_STREAM = "is_live_stream";
  static final String KEY_ASPECT_RATIO = "aspect_ratio";
  static final String KEY_PROFILE = "profile";
  static final String KEY_WIDEVINE_SERVER_PATH = "widevine_server_path";
  static final String KEY_HEIGHT = "height";
  static final String KEY_WIDTH = "width";
  static final String KEY_URL = "url";

  static final String PROFILE_BASELINE = "baseline";

  public static final String DELIVERY_TYPE_HLS = "hls";
  public static final String DELIVERY_TYPE_MP4 = "mp4";
  public static final String DELIVERY_TYPE_DASH = "dash";
  public static final String DELIVERY_TYPE_REMOTE_ASSET = "remote_asset";
  public static final String DELIVERY_TYPE_WV_MP4 = "wv_mp4";
  public static final String DELIVERY_TYPE_WV_WVM = "wv_wvm";
  public static final String DELIVERY_TYPE_WV_HLS = "wv_hls";
  public static final String DELIVERY_TYPE_AKAMAI_HD2_VOD_HLS = "akamai_hd2_vod_hls";
  public static final String DELIVERY_TYPE_SMOOTH = "smooth";

  public static final String STREAM_URL_FORMAT_TEXT = "text";
  public static final String STREAM_URL_FORMAT_B64 = "encoded";

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
  protected String _profile = null;
  protected String _widevineServerPath = null;  // A path from SAS when this is a widevine encrypted stream

  private static class DefaultStreamSelector implements StreamSelector {
    public DefaultStreamSelector() {}

    @Override
    public Stream bestStream(Set<Stream> streams, boolean isWifiEnabled) {
      if (streams == null || streams.size() == 0) { return null; }

      Stream bestBitrateStream = null;
      for (Stream stream : streams) {
        // for remote assets, just pick the first stream
        if (stream.getDeliveryType().equals(DELIVERY_TYPE_REMOTE_ASSET)
            || stream.getDeliveryType().equals(DELIVERY_TYPE_HLS)
            || stream.getDeliveryType().equals(DELIVERY_TYPE_DASH)) { return stream; }
        if (Stream.isDeliveryTypePlayable(stream)
            && Stream.isProfilePlayable(stream)
            && (bestBitrateStream == null
                || stream.betterThan(bestBitrateStream, isWifiEnabled))) {
          bestBitrateStream = stream;
        }
      }

      return bestBitrateStream;
    }
  }

  private static StreamSelector _selector = new DefaultStreamSelector();

  /**
   * This method will set the StreamSelector used to select the Stream to play.
   * @param selector an implemented StreamSelector
   */
  public static void setStreamSelector(StreamSelector selector) {
    _selector = selector;
  }

  /**
   * This method will reset the StreamSelector to the default
   */
  public static void resetStreamSelector() {
    _selector = new DefaultStreamSelector();
  }

  public Stream() {}

  public Stream(JSONObject data) {
    update(data);
  }

  /**
   * Create an 'unbundled' Stream that doesn't use the Ooyala CMS.
   * @param url source for the video stream e.g. "http://techslides.com/demos/sample-videos/small.mp4".
   * @param deliveryType the stream delivery type e.g. DELIVERY_TYPE_MP4.
   */
  public Stream( String url, String deliveryType ) {
    this._url = url;
    this._deliveryType = deliveryType;
    this._urlFormat = STREAM_URL_FORMAT_TEXT;
  }

  boolean betterThan(Stream other, boolean isWifiEnabled) {

    // if the bitrates are the same, always choose the bitrate with higher resolution
    if (this.getCombinedBitrate() == other.getCombinedBitrate() && this.getHeight() > other.getHeight()) {
      return true;
    } else if (isWifiEnabled) {
      return this.getCombinedBitrate() > other.getCombinedBitrate();
    } else {
      // if wifi is off, choose the one closest to 400.
      return Math.abs(400 - this.getCombinedBitrate()) < Math.abs(400 - other.getCombinedBitrate());
    }
  }

  ReturnState update(JSONObject data) {
    if (data.isNull(KEY_DELIVERY_TYPE)) {
      System.out.println("ERROR: Fail to update stream with dictionary because no delivery_type exists!");
      return ReturnState.STATE_FAIL;
    }
    if (data.isNull(KEY_URL)) {
      System.out.println("ERROR: Fail to update stream with dictionary because no url element exists!");
      return ReturnState.STATE_FAIL;
    }

    JSONObject urlData = null;
    try {
      urlData = data.getJSONObject(KEY_URL);
    } catch (JSONException exception) {
      System.out.println("ERROR: Fail to update stream with dictionary because url element is invalid.");
      return ReturnState.STATE_FAIL;
    }

    if (urlData.isNull(KEY_DATA)) {
      System.out.println("ERROR: Fail to update stream with dictionary because no url.data exists!");
      return ReturnState.STATE_FAIL;
    }
    if (urlData.isNull(KEY_FORMAT)) {
      System.out.println("ERROR: Fail to update stream with dictionary because no url.format exists!");
      return ReturnState.STATE_FAIL;
    }

    try {
      if (!data.isNull(KEY_WIDEVINE_SERVER_PATH)) {
        _widevineServerPath =  data.getString(KEY_WIDEVINE_SERVER_PATH);
      }
      _deliveryType = data.getString(KEY_DELIVERY_TYPE);
      _url = urlData.getString(KEY_DATA);
      _urlFormat = urlData.getString(KEY_FORMAT);
      _videoBitrate = data.isNull(KEY_VIDEO_BITRATE) ? _videoBitrate : data
          .getInt(KEY_VIDEO_BITRATE);
      _audioBitrate = data.isNull(KEY_AUDIO_BITRATE) ? _audioBitrate : data
          .getInt(KEY_AUDIO_BITRATE);
      _videoCodec = data.isNull(KEY_VIDEO_CODEC) ? _videoCodec : data
          .getString(KEY_VIDEO_CODEC);
      _height = data.isNull(KEY_HEIGHT) ? _height : data.getInt(KEY_HEIGHT);
      _width = data.isNull(KEY_WIDTH) ? _width : data.getInt(KEY_WIDTH);
      _framerate = data.isNull(KEY_FRAMERATE) ? _framerate : data
          .getString(KEY_FRAMERATE);
      _aspectRatio = data.isNull(KEY_ASPECT_RATIO) ? _aspectRatio : data
          .getString(KEY_ASPECT_RATIO);
      _isLiveStream = data.isNull(KEY_IS_LIVE_STREAM) ? _isLiveStream : data
          .getBoolean(KEY_IS_LIVE_STREAM);
      _profile = data.isNull(KEY_PROFILE) ? _profile : data.getString(KEY_PROFILE);
    } catch (JSONException jsonException) {
      System.out.println("ERROR: Fail to update stream with dictionary because of invalid JSON: "
          + jsonException);
      return ReturnState.STATE_FAIL;
    }
    return ReturnState.STATE_MATCHED;
  }

  public String getDeliveryType() {
    return _deliveryType;
  }

  public void setDeliveryType(String deliveryType) {
    this._deliveryType = deliveryType;
  }

  public String getVideoCodec() {
    return _videoCodec;
  }

  public void setVideoCodec(String videoCodec) {
    this._videoCodec = videoCodec;
  }

  public String getUrlFormat() {
    return _urlFormat;
  }

  public void setUrlFormat(String urlFormat) {
    this._urlFormat = urlFormat;
  }

  public String getFramerate() {
    return _framerate;
  }

  public void setFramerate(String framerate) {
    this._framerate = framerate;
  }

  public int getVideoBitrate() {
    return _videoBitrate;
  }

  public void setVideoBitrate(int videoBitrate) {
    this._videoBitrate = videoBitrate;
  }

  public int getAudioBitrate() {
    return _audioBitrate;
  }

  public void setAudioBitrate(int audioBitrate) {
    this._audioBitrate = audioBitrate;
  }

  public int getHeight() {
    return _height;
  }

  public void setHeight(int height) {
    this._height = height;
  }

  public int getWidth() {
    return _width;
  }

  public void setWidth(int width) {
    this._width = width;
  }

  public String getUrl() {
    return _url;
  }

  public void setUrl(String url) {
    this._url = url;
  }

  public String getAspectRatio() {
    return _aspectRatio;
  }

  public void setAspectRatio(String aspectRatio) {
    this._aspectRatio = aspectRatio;
  }

  public boolean isLiveStream() {
    return _isLiveStream;
  }

  public void setLiveStream(boolean isLiveStream) {
    this._isLiveStream = isLiveStream;
  }

  public String getProfile() {
    return _profile;
  }

  public void setProfile(String profile) {
    this._profile = profile;
  }

  public int getCombinedBitrate() {
    return (_videoBitrate + _audioBitrate);
  }

  public String getWidevineServerPath() {
    return _widevineServerPath;
  }

  public URL decodedURL() {
    try {
      if (_urlFormat.equals(STREAM_URL_FORMAT_B64)) { return new URL(new String(Base64.decode(_url,
          Base64.DEFAULT))); }
      return new URL(_url); // Otherwise assume plain text
    } catch (MalformedURLException exception) {
      System.out.println("Malformed URL: " + _url);
      return null;
    }
  }

  public static boolean isDeliveryTypePlayable(Stream stream) {
    String type = stream.getDeliveryType();
    /**
     * NOTE(jigish) Android 3.0+ supports HLS, but we support it only on 4.0+ to simplify secure HLS
     * implementation
     */


    boolean isHLS = type.equals(DELIVERY_TYPE_HLS) || type.equals (DELIVERY_TYPE_AKAMAI_HD2_VOD_HLS);
    boolean isWidevine = type.equals(DELIVERY_TYPE_WV_WVM) || type.equals(DELIVERY_TYPE_WV_HLS);
    boolean isSmooth = type.equals(DELIVERY_TYPE_SMOOTH);
    return type.equals(DELIVERY_TYPE_MP4) ||
           type.equals(DELIVERY_TYPE_REMOTE_ASSET) ||
           type.equals(DELIVERY_TYPE_WV_MP4) ||
           isSmooth ||
           (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && (isHLS || isWidevine ));
  }

  public static boolean isProfilePlayable(Stream stream) {
    if (!DELIVERY_TYPE_MP4.equals(stream.getDeliveryType())) { return true; }
    return stream.getProfile() == null || PROFILE_BASELINE.equals(stream.getProfile());
  }

  public static Stream bestStream(Set<Stream> streams, boolean isWifiEnabled) {
    return _selector.bestStream(streams, isWifiEnabled);
  }

  public static boolean streamSetContainsDeliveryType(Set<Stream> streams, String deliveryType) {
    return getStreamWithDeliveryType(streams, deliveryType) != null;
  }

  public static Stream getStreamWithDeliveryType(Set<Stream> streams, String deliveryType) {
    for (Stream stream:streams) {
      if (stream.getDeliveryType().equals(deliveryType)) { return stream; }
    }
    return null;
  }

}
