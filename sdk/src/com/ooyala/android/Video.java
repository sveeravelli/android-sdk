package com.ooyala.android;

import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.*;

import com.ooyala.android.Constants.ReturnState;

/**
 * Stores the info and metatdata for the specified movie.
 *
 */
public class Video extends ContentItem
{
  protected Set<Ad> _ads = new HashSet<Ad>();
  protected Set<Stream> _streams = new HashSet<Stream>();
  protected Channel _parent = null;
  protected int _duration = 0;
  protected boolean _isLive = false;

  public Video()
  {
  }

  public Video(JSONObject data, String embedCode, PlayerAPIClient api)
  {
    this(data, embedCode, null, api);
  }

  public Video(JSONObject data, String embedCode, Channel parent, PlayerAPIClient api)
  {
    _embedCode = embedCode;
    _api = api;
    _parent = parent;
    update(data);
  }

  public ReturnState update(JSONObject data)
  {
    switch (super.update(data))
    {
      case STATE_FAIL:
        return ReturnState.STATE_FAIL;
      case STATE_UNMATCHED:
        return ReturnState.STATE_UNMATCHED;
      default:
        break;
    }

    try
    {
      JSONObject myData = data.getJSONObject(_embedCode);
      if (!myData.isNull(Constants.KEY_DURATION))
      {
        _duration = myData.getInt(Constants.KEY_DURATION);
      }
      if (!myData.isNull(Constants.KEY_CONTENT_TYPE))
      {
        _isLive = myData.getString(Constants.KEY_CONTENT_TYPE).equals(Constants.CONTENT_TYPE_LIVE_STREAM);
      }
      if (!myData.isNull(Constants.KEY_AUTHORIZED) && myData.getBoolean(Constants.KEY_AUTHORIZED) &&
          !myData.isNull(Constants.KEY_STREAMS))
      {
        JSONArray streams = myData.getJSONArray(Constants.KEY_STREAMS);
        if (streams.length() > 0)
        {
          _streams.clear();
          for (int i = 0; i < streams.length(); i++)
          {
            Stream stream = new Stream(streams.getJSONObject(i));
            if (stream != null)
            {
              _streams.add(stream);
            }
            else
            {
              System.out.println("Unable to create stream.");
            }
          }
        }
        return ReturnState.STATE_MATCHED;
      }

      if (!myData.isNull(Constants.KEY_ADS))
      {
        JSONArray ads = myData.getJSONArray(Constants.KEY_ADS);
        if (ads.length() > 0)
        {
          _ads.clear();
          for (int i = 0; i < ads.length(); i++)
          {
            Ad ad = new Ad(ads.getJSONObject(i));
            if (ad != null)
            {
              _ads.add(ad);
            }
            else
            {
              System.out.println("Unable to create ad.");
            }
          }
        }
      }
    }
    catch (JSONException exception)
    {
      System.out.println("JSONException: " + exception);
      return ReturnState.STATE_FAIL;
    }

    return ReturnState.STATE_MATCHED;
  }

  public Set<Ad> getAds()
  {
    return _ads;
  }

  public Channel getParent()
  {
    return _parent;
  }

  public int getDuration()
  {
    return _duration;
  }

  public boolean isLive()
  {
    return _isLive;
  }

  public Video firstVideo()
  {
    return this;
  }

  public Video nextVideo()
  {
    return _parent == null ? _parent.nextVideo(this) : null;
  }

  public Video previousVideo()
  {
    return _parent == null ? _parent.previousVideo(this) : null;
  }

  public Stream getStream()
  {
    return Stream.bestStream(_streams);
  }

  public ReturnState fetchAdsPlaybackInfo(PlayerAPIClient api)
  {
    if (!hasAds()) { return ReturnState.STATE_UNMATCHED; }
    for (Ad ad : ads)
    {
      if (ad.fetchPlaybackInfo(api) != ReturnState.STATE_MATCHED)
      {
        return ReturnState.STATE_FAIL;
      }
    }
    return ReturnState.STATE_MATCHED;
  }

  /**
   * Returns whether this movie has ads
   * @return isAd
   */
  public boolean hasAds()
  {
    return (_ads != null && _ads.size() > 0);
  }
}
