package com.ooyala.android;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.*;

import android.util.Log;

import com.ooyala.android.Constants.ReturnState;

public abstract class AdSpot {
  protected int _time = -1;
  protected URL _clickURL = null;
  protected List<URL> _trackingURLs = null;
  protected PlayerAPIClient _api;

  AdSpot() {}

  AdSpot(int time, URL clickURL, List<URL> trackingURLs) {
    _time = time;
    _clickURL = clickURL;
    _trackingURLs = trackingURLs;
  }

  AdSpot(JSONObject data, PlayerAPIClient api) {
    _api = api;
    update(data);
  }

  ReturnState update(JSONObject data) {
    if (data == null) { return ReturnState.STATE_FAIL; }

    try {
      if (!data.isNull(Constants.KEY_TIME)) {
        _time = data.getInt(Constants.KEY_TIME);
      } else if (_time < 0) {
        _time = Constants.DEFAULT_AD_TIME_SECONDS;
      }

      if (!data.isNull(Constants.KEY_CLICK_URL)) {
        try {
          _clickURL = new URL(data.getString(Constants.KEY_CLICK_URL));
        } catch (MalformedURLException exception) {
          Log.d(this.getClass().getName(),
              "Malformed Ad Click URL: " + data.getString(Constants.KEY_CLICK_URL));
          _clickURL = null;
        }
      }

      if (!data.isNull(Constants.KEY_TRACKING_URL)) {
        JSONArray pixels = data.getJSONArray(Constants.KEY_TRACKING_URL);
        _trackingURLs = new ArrayList<URL>(pixels.length());
        for (int i = 0; i < pixels.length(); i++) {
          try {
            _trackingURLs.add(new URL(pixels.getString(i)));
          } catch (MalformedURLException exception) {
            Log.d(this.getClass().getName(),
                "Malformed Ad Tracking URL: " + data.getString(Constants.KEY_TRACKING_URL));
          }
        }
      }
    } catch (JSONException exception) {
      Log.d(this.getClass().getName(), "JSONException: " + exception);
      return ReturnState.STATE_FAIL;
    }
    return ReturnState.STATE_MATCHED;
  }

  /**
   * This method will fetch all the information required to play this AdSpot. This method should automatically
   * get called during playback. There is no reason to call it unless you want to prefetch the data.
   * @return true if success, false if failure.
   */
  public abstract boolean fetchPlaybackInfo();

  static AdSpot create(JSONObject data, PlayerAPIClient api) {
    if (data == null || data.isNull(Constants.KEY_TYPE)) { return null; }
    String type = null;
    try {
      type = (String) data.getString(Constants.KEY_TYPE);
    } catch (JSONException exception) {
      Log.d(AdSpot.class.getName(), "Ad create failed due to JSONException: " + exception);
      return null;
    }

    if (type == null) {
      return null;
    } else if (type.equals(Constants.AD_TYPE_OOYALA)) {
      return new OoyalaAdSpot(data, api);
    } else if (type.equals(Constants.AD_TYPE_VAST)) {
      return new VASTAdSpot(data, api);
    } else {
      Log.d(AdSpot.class.getName(), "Unknown ad type: " + type);
      return null;
    }
  }

  /**
   * Fetch the time at which this AdSpot should play.
   * @return The time at which this AdSpot should play in milliseconds.
   */
  public int getTime() {
    return _time;
  }

  /**
   * Fetch the URL to ping when this AdSpot is clicked.
   * @return the click URL
   */
  public URL getClickURL() {
    return _clickURL;
  }

  /**
   * Fetch the list of tracking URLs to ping when this AdSpot is played.
   * @return the list of tracking URLs
   */
  public List<URL> getTrackingURLs() {
    return _trackingURLs;
  }

  void setAPI(PlayerAPIClient api) {
    this._api = api;
  }
}
