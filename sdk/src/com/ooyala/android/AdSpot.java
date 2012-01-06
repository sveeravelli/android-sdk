package com.ooyala.android;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.*;

import com.ooyala.android.Constants.ReturnState;

public abstract class AdSpot
{
  protected int _time = -1;
  protected URL _clickURL = null;
  protected List<URL> _trackingURLs = null;
  protected PlayerAPIClient _api;

  public AdSpot()
  {
  }

  public AdSpot(JSONObject data, PlayerAPIClient api)
  {
    _api = api;
    update(data);
  }

  public ReturnState update(JSONObject data)
  {
    if (data == null) { return ReturnState.STATE_FAIL; }

    try
    {
      if (!data.isNull(Constants.KEY_TIME))
      {
        _time = data.getInt(Constants.KEY_TIME);
      }
      else if (_time < 0)
      {
        _time = Constants.DEFAULT_AD_TIME_SECONDS;
      }

      if (!data.isNull(Constants.KEY_CLICK_URL))
      {
        try
        {
          _clickURL = new URL(data.getString(Constants.KEY_CLICK_URL));
        }
        catch (MalformedURLException exception)
        {
          System.out.println("Malformed Ad URL: " + data.getString(Constants.KEY_CLICK_URL));
          _clickURL = null;
        }
      }

      if (!data.isNull(Constants.KEY_TRACKING_URL))
      {
        JSONArray pixels = data.getJSONArray(Constants.KEY_TRACKING_URL);
        _trackingURLs = new ArrayList<URL>(pixels.length());
        for (int i = 0; i < pixels.length(); i++)
        {
          try
          {
            _trackingURLs.add(new URL(pixels.getString(i)));
          }
          catch (MalformedURLException exception)
          {
            System.out.println("Malformed Ad Tracking URL: " + data.getString(Constants.KEY_TRACKING_URL));
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

  public abstract boolean fetchPlaybackInfo();

  public static AdSpot create(JSONObject data, PlayerAPIClient api)
  {
    if (data == null || data.isNull(Constants.KEY_TYPE)) { return null; }
    String type = null;
    try
    {
      type = (String)data.getString(Constants.KEY_TYPE);
    }
    catch (JSONException exception)
    {
      System.out.println("Ad create failed due to JSONException: " + exception);
      return null;
    }

    if (type == null)
    {
      return null;
    }
    else if (type.equals(Constants.AD_TYPE_OOYALA))
    {
      return new OoyalaAdSpot(data, api);
    }
    else if (type.equals(Constants.AD_TYPE_VAST))
    {
      return new VASTAdSpot(data, api);
    }
    else
    {
      System.out.println("Unknown ad type: " + type);
      return null;
    }
  }

  public int getTime() {
    return _time;
  }

  public URL getClickURL() {
    return _clickURL;
  }

  public List<URL> getTrackingURLs() {
    return _trackingURLs;
  }
}
