package com.ooyala.android.item;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ooyala.android.DebugMode;
import com.ooyala.android.OoyalaAPIClient;
import com.ooyala.android.OoyalaAdSpot;
import com.ooyala.android.ads.vast.VASTAdSpot;

public abstract class AdSpot implements JSONUpdatableItem {
  protected static final String KEY_TYPE = "type";  //AdSpot
  protected static final String KEY_TIME = "time";  //AdSpot
  protected static final String KEY_CLICK_URL = "click_url";
  protected static final String KEY_TRACKING_URL = "tracking_url";

  protected static final String AD_TYPE_OOYALA = "ooyala";
  protected static final String AD_TYPE_VAST = "vast";

  static final int DEFAULT_AD_TIME_SECONDS = 0;

  public static final boolean SINGLE_USE = false;
  public static final boolean REUSABLE = true;

  protected int _time = -1;
  protected URL _clickURL = null;
  protected List<URL> _trackingURLs = null;
  protected final boolean _isReusable;

  protected AdSpot() {
    _isReusable = REUSABLE;
  }

  protected AdSpot( boolean isOneTimeUse ) {
    _isReusable = isOneTimeUse;
  }

  protected AdSpot(int time, URL clickURL, List<URL> trackingURLs) {
    _time = time;
    _clickURL = clickURL;
    _trackingURLs = trackingURLs;
    _isReusable = REUSABLE;
  }

  protected AdSpot(JSONObject data) {
    _isReusable = REUSABLE;
    update(data);
  }

  protected ReturnState update(JSONObject data) {
    if (data == null) { return ReturnState.STATE_FAIL; }

    try {
      if (!data.isNull(KEY_TIME)) {
        _time = data.getInt(KEY_TIME);
      } else if (_time < 0) {
        _time = DEFAULT_AD_TIME_SECONDS;
      }

      if (!data.isNull(KEY_CLICK_URL)) {
        try {
          _clickURL = new URL(data.getString(KEY_CLICK_URL));
        } catch (MalformedURLException exception) {
          DebugMode.logD(this.getClass().getName(),
              "Malformed Ad Click URL: " + data.getString(KEY_CLICK_URL));
          _clickURL = null;
        }
      }

      if (!data.isNull(KEY_TRACKING_URL)) {
        JSONArray pixels = data.getJSONArray(KEY_TRACKING_URL);
        _trackingURLs = new ArrayList<URL>(pixels.length());
        for (int i = 0; i < pixels.length(); i++) {
          try {
            _trackingURLs.add(new URL(pixels.getString(i)));
          } catch (MalformedURLException exception) {
            DebugMode.logD(this.getClass().getName(),
                "Malformed Ad Tracking URL: " + data.getString(KEY_TRACKING_URL));
          }
        }
      }
    } catch (JSONException exception) {
      DebugMode.logD(this.getClass().getName(), "JSONException: " + exception);
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

  public static AdSpot create(JSONObject data, OoyalaAPIClient api) {
    if (data == null || data.isNull(KEY_TYPE)) { return null; }
    String type = null;
    try {
      type = data.getString(KEY_TYPE);
    } catch (JSONException exception) {
      DebugMode.logD(AdSpot.class.getName(), "Ad create failed due to JSONException: " + exception);
      return null;
    }

    if (type == null) {
      return null;
    } else if (type.equals(AD_TYPE_OOYALA)) {
      return new OoyalaAdSpot(data, api);
    } else if (type.equals(AD_TYPE_VAST)) {
      return new VASTAdSpot(data);
    } else {
      DebugMode.logD(AdSpot.class.getName(), "Unknown ad type: " + type);
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

  /**
   * @return false if this ad should not survive being interrupted. true (the default value)
   * means the ad will get put back into the collection of potential ads when interrupted.
   */
  public boolean isReusable() {
    return _isReusable;
  }
}
