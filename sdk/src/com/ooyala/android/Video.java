package com.ooyala.android;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.*;

import android.os.AsyncTask;

import com.ooyala.android.Constants.ReturnState;

/**
 * Stores the info and metatdata for the specified movie.
 */
public class Video extends ContentItem implements PlayableItem {
  protected List<AdSpot> _ads = new ArrayList<AdSpot>();
  protected Set<Stream> _streams = new HashSet<Stream>();
  protected Channel _parent = null;
  protected int _duration = 0;
  protected boolean _live = false;
  protected ClosedCaptions _closedCaptions = null;

  Video() {}

  Video(JSONObject data, String embedCode, PlayerAPIClient api) {
    this(data, embedCode, null, api);
  }

  Video(JSONObject data, String embedCode, Channel parent, PlayerAPIClient api) {
    _embedCode = embedCode;
    _api = api;
    _parent = parent;
    update(data);
  }

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
      JSONObject myData = data.getJSONObject(_embedCode);
      if (!myData.isNull(Constants.KEY_DURATION)) {
        _duration = myData.getInt(Constants.KEY_DURATION);
      }
      if (!myData.isNull(Constants.KEY_CONTENT_TYPE)) {
        _live = myData.getString(Constants.KEY_CONTENT_TYPE).equals(Constants.CONTENT_TYPE_LIVE_STREAM);
      }
      if (!myData.isNull(Constants.KEY_AUTHORIZED) && myData.getBoolean(Constants.KEY_AUTHORIZED)
          && !myData.isNull(Constants.KEY_STREAMS)) {
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
        return ReturnState.STATE_MATCHED;
      }

      if (!myData.isNull(Constants.KEY_ADS)) {
        JSONArray ads = myData.getJSONArray(Constants.KEY_ADS);
        if (ads.length() > 0) {
          _ads.clear();
          for (int i = 0; i < ads.length(); i++) {
            AdSpot ad = AdSpot.create(ads.getJSONObject(i), _api);
            if (ad != null) {
              _ads.add(ad);
            } else {
              System.out.println("Unable to create ad.");
            }
          }
        }
      }

      if (!myData.isNull(Constants.KEY_CLOSED_CAPTIONS)) {
        _closedCaptions = null;
        JSONArray array = myData.getJSONArray(Constants.KEY_CLOSED_CAPTIONS);
        if (array.length() > 0) {
          /*
           * NOTE [jigish]: here we only select the first closed caption returned. according to rui it is
           * guaranteed by the ingestion API that only one closed caption file will exist per movie. we are
           * not doing this restriction server side in the content tree api because the DB does not have this
           * restriction in case we want to support having multiple closed caption files per movie. if that
           * ever happens, we will have to change this to support multiple closed captions.
           */
          JSONObject o = (JSONObject) array.get(0);
          _closedCaptions = new ClosedCaptions(o);
        }
      }
    } catch (JSONException exception) {
      System.out.println("JSONException: " + exception);
      return ReturnState.STATE_FAIL;
    }

    return ReturnState.STATE_MATCHED;
  }

  public List<AdSpot> getAds() {
    return _ads;
  }

  /**
   * Insert an AdSpot to play during this video
   * @param ad the AdSpot to play during this video
   */
  public void insertAd(AdSpot ad) {
    ad.setAPI(_api);
    for (int i = 0; i < _ads.size(); i++) {
      if (ad.getTime() < _ads.get(i).getTime()) {
        _ads.add(i, ad);
        break;
      }
    }
  }

  public Channel getParent() {
    return _parent;
  }

  public int getDuration() {
    return _duration;
  }

  public Video firstVideo() {
    return this;
  }

  public Video nextVideo() {
    return _parent == null ? null : _parent.nextVideo(this);
  }

  public Video previousVideo() {
    return _parent == null ? null : _parent.previousVideo(this);
  }

  public Stream getStream() {
    return Stream.bestStream(_streams);
  }

  public boolean fetchPlaybackInfo() {
    if (hasAds()) {
      for (AdSpot ad : _ads) {
        if (!ad.fetchPlaybackInfo()) { return false; }
      }
    }

    if (_closedCaptions != null) if (!_closedCaptions.fetchClosedCaptionsInfo()) return false;

    return true;
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

  /**
   * Returns whether this movie has ads
   * @return isAd
   */
  public boolean hasAds() {
    return (_ads != null && _ads.size() > 0);
  }

  public boolean isLive() {
    return _live;
  }

  public ClosedCaptions getClosedCaptions() {
    return _closedCaptions;
  }

  public void setClosedCaptions(ClosedCaptions closedCaptions) {
    this._closedCaptions = closedCaptions;
  }

  public boolean hasClosedCaptions() {
    return _closedCaptions != null && _closedCaptions.getLanguages().size() > 0;
  }

  @Override
  public Video videoFromEmbedCode(String embedCode, Video currentItem) {
    if (_embedCode.equals(embedCode)) { return this; }
    return null;
  }
}
