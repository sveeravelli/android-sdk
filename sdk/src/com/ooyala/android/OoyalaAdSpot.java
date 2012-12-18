package com.ooyala.android;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.*;

import android.os.AsyncTask;

import com.ooyala.android.Constants.ReturnState;

/**
 * Stores the info and metadata for the specified content item.
 * 
 */
public class OoyalaAdSpot extends AdSpot implements AuthorizableItemInternal, PlayableItem {
  protected Set<Stream> _streams = new HashSet<Stream>();
  protected String _embedCode = null;
  protected boolean _authorized = false;
  protected int _authCode = AuthCode.NOT_REQUESTED;

  /**
   * Initialize an OoyalaAdSpot using the specified data
   * @param time the time at which the VASTAdSpot should play
   * @param clickURL the clickthrough URL
   * @param trackingURLs the tracking URLs that should be pinged when this ad plays
   * @param embedCode the embed code associated with this OoyalaAdSpot
   */
  public OoyalaAdSpot(int time, URL clickURL, List<URL> trackingURLs, String embedCode) {
    super(time, clickURL, trackingURLs);
    _embedCode = embedCode;
  }

  OoyalaAdSpot(JSONObject data, PlayerAPIClient api) {
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
        if (!myData.isNull(Constants.KEY_AUTHORIZED)) {
          _authorized = myData.getBoolean(Constants.KEY_AUTHORIZED);
          if (!myData.isNull(Constants.KEY_CODE)) {
            int theAuthCode = myData.getInt(Constants.KEY_CODE);
            if (theAuthCode < AuthCode.MIN_AUTH_CODE || theAuthCode > AuthCode.MAX_AUTH_CODE) {
              _authCode = AuthCode.UNKNOWN;
            } else {
              _authCode = theAuthCode;
            }
          }
          if (_authorized && !myData.isNull(Constants.KEY_STREAMS)) {
            JSONArray streams = myData.getJSONArray(Constants.KEY_STREAMS);
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
      if (data.isNull(Constants.KEY_AD_EMBED_CODE)) {
        System.out
            .println("ERROR: Fail to update OoyalaAdSpot with dictionary because no ad embed code exists!");
        return ReturnState.STATE_FAIL;
      }
      _embedCode = data.getString(Constants.KEY_AD_EMBED_CODE);
      return ReturnState.STATE_MATCHED;
    } catch (JSONException exception) {
      System.out.println("JSONException: " + exception);
      return ReturnState.STATE_FAIL;
    }
  }

  public boolean fetchPlaybackInfo() {
    if (_authCode != AuthCode.NOT_REQUESTED) { return true; }
    try {
      return _api.authorize(this);
    } catch (OoyalaException e) {
      System.out.println("Unable to fetch playback info: " + e.getMessage());
      return false;
    }
  }

  private class FetchPlaybackInfoTask extends AsyncTask<Void, Integer, Boolean> {
    protected FetchPlaybackInfoCallback _callback = null;

    public FetchPlaybackInfoTask(FetchPlaybackInfoCallback callback) {
      super();
      _callback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
      return fetchPlaybackInfo();
    }

    @Override
    protected void onPostExecute(Boolean result) {
      _callback.callback(result.booleanValue());
    }
  }

  public Object fetchPlaybackInfo(FetchPlaybackInfoCallback callback) {
    FetchPlaybackInfoTask task = new FetchPlaybackInfoTask(callback);
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
  public List<String> embedCodesToAuthorize() {
    List<String> embedCodes = new ArrayList<String>();
    embedCodes.add(_embedCode);
    return embedCodes;
  }

  public boolean isAuthorized() {
    return _authorized;
  }

  public int getAuthCode() {
    return _authCode;
  }

  @Override
  public Set<Stream> getStreams() {
    return _streams;
  }
}
