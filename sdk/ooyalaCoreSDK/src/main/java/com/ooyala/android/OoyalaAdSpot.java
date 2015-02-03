package com.ooyala.android;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.ooyala.android.apis.FetchPlaybackInfoCallback;
import com.ooyala.android.item.AuthorizableItem;
import com.ooyala.android.item.OoyalaManagedAdSpot;
import com.ooyala.android.item.PlayableItem;
import com.ooyala.android.item.Stream;
import com.ooyala.android.player.StreamPlayer;
import com.ooyala.android.util.DebugMode;

/**
 * Stores the info and metadata for an Ooyala Managed Adspot.
 *
 */
public class OoyalaAdSpot extends OoyalaManagedAdSpot implements AuthorizableItem, PlayableItem {
  private static final String TAG = OoyalaAdSpot.class.getSimpleName();
  static final String KEY_AUTHORIZED = "authorized";
  static final String KEY_CODE = "code";
  static final String KEY_STREAMS = "streams";  //OoyalaAdSpot, Video
  static final String KEY_AD_EMBED_CODE = "ad_embed_code"; //OoyalaAdSpot

  protected Set<Stream> _streams = new HashSet<Stream>();
  protected String _embedCode = null;
  protected boolean _authorized = false;
  protected OoyalaAPIClient _api;
  protected int _authCode = AuthCode.NOT_REQUESTED;

  /**
   * Initialize an OoyalaAdSpot using the specified data
   * @param time the time at which the VASTAdSpot should play
   * @param clickURL the clickthrough URL
   * @param trackingURLs the tracking URLs that should be pinged when this ad plays
   * @param embedCode the embed code associated with this OoyalaAdSpot
   */
  public OoyalaAdSpot(int time, URL clickURL, List<URL> trackingURLs, String embedCode, OoyalaAPIClient api) {
    super(time, clickURL, trackingURLs);
    _embedCode = embedCode;
    _api = api;
  }

  /**
   * Initialize the Ooyala Ad Spot
   * @param data the metadata needed to update the Ooyala Ad
   * @param api the API to authorize the ad spot at a later time
   */
  public OoyalaAdSpot(JSONObject data, OoyalaAPIClient api) {
    _api = api;
    update(data);
  }

  /**
   * Get the embedCode for this content item.
   * @return embedCode of this content item
   */
  public String getEmbedCode() {
    return _embedCode;
  }

  /**
   * For internal use only. Update the AuthorizableItem using the specified data (subclasses should override
   * and call this)
   * @param data the data to use to update this AuthorizableItem
   * @return a ReturnState based on if the data matched or not (or parsing failed)
   */
  @Override
  public ReturnState update(JSONObject data) {
    switch (super.update(data)) {
      case STATE_FAIL:
        return ReturnState.STATE_FAIL;
      case STATE_UNMATCHED:
        return ReturnState.STATE_UNMATCHED;
      default:
        break;
    }

    try {
      if (_embedCode != null && !data.isNull(_embedCode)) {
        JSONObject myData = data.getJSONObject(_embedCode);
        if (!myData.isNull(KEY_AUTHORIZED)) {
          _authorized = myData.getBoolean(KEY_AUTHORIZED);
          if (!myData.isNull(KEY_CODE)) {
            int theAuthCode = myData.getInt(KEY_CODE);
            _authCode = theAuthCode;
          }
          if (_authorized && !myData.isNull(KEY_STREAMS)) {
            JSONArray streams = myData.getJSONArray(KEY_STREAMS);
            if (streams.length() > 0) {
              _streams.clear();
              for (int i = 0; i < streams.length(); i++) {
                Stream stream = new Stream(streams.getJSONObject(i));
                if (stream != null) {
                  _streams.add(stream);
                }
              }
            }
          }
        }
        return ReturnState.STATE_MATCHED;
      }
      if (data.isNull(KEY_AD_EMBED_CODE)) {
        System.out
            .println("ERROR: Fail to update OoyalaAdSpot with dictionary because no ad embed code exists!");
        return ReturnState.STATE_FAIL;
      }
      _embedCode = data.getString(KEY_AD_EMBED_CODE);
      return ReturnState.STATE_MATCHED;
    } catch (JSONException exception) {
      System.out.println("JSONException: " + exception);
      return ReturnState.STATE_FAIL;
    }
  }

  @Override
  public boolean fetchPlaybackInfo() {
   return fetchPlaybackInfo(StreamPlayer.defaultPlayerInfo);
  }

  public boolean fetchPlaybackInfo(PlayerInfo info) {
    if (_authCode != AuthCode.NOT_REQUESTED) { return true; }
    try {
      return _api.authorize(this, info);
    } catch (OoyalaException e) {
      DebugMode.logE(TAG, "Unable to fetch playback info: " + e.getMessage());
      return false;
    }
  }

  private class FetchPlaybackInfoTask extends AsyncTask<Void, Integer, Boolean> {
    protected FetchPlaybackInfoCallback _callback = null;
    protected PlayerInfo _info = null;

    public FetchPlaybackInfoTask(PlayerInfo info, FetchPlaybackInfoCallback callback) {
      super();
      _callback = callback;
      _info = info;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
      return fetchPlaybackInfo(_info);
    }

    @Override
    protected void onPostExecute(Boolean result) {
      _callback.callback(result.booleanValue());
    }
  }

  public Object fetchPlaybackInfo(PlayerInfo info, FetchPlaybackInfoCallback callback) {
    FetchPlaybackInfoTask task = new FetchPlaybackInfoTask(info, callback);
    task.execute();
    return task;
  }

  public Stream getStream() {
    return Stream.bestStream(_streams);
  }

  /**
   * For internal use only. The embed codes to authorize for the AuthorizableItem
   * @return the embed codes to authorize as a List
   */
  @Override
  public List<String> embedCodesToAuthorize() {
    List<String> embedCodes = new ArrayList<String>();
    embedCodes.add(_embedCode);
    return embedCodes;
  }

  @Override
  public boolean isAuthorized() {
    return _authorized;
  }

  @Override
  public int getAuthCode() {
    return _authCode;
  }

  @Override
  public boolean isHeartbeatRequired() {
    return false;
  }
  @Override
  public Set<Stream> getStreams() {
    return _streams;
  }
}
