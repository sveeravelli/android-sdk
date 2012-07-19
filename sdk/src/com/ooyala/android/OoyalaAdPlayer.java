package com.ooyala.android;

import java.net.URL;

import com.ooyala.android.OoyalaPlayer.State;

class OoyalaAdPlayer extends MoviePlayer {
  private OoyalaAdSpot _ad;
  private Object _fetchTask;

  public OoyalaAdPlayer() {
    super();
  }

  @Override
  public void init(final OoyalaPlayer parent, Object ad) {
    if (!(ad instanceof OoyalaAdSpot)) {
      this._error = "Invalid Ad";
      this._state = State.ERROR;
      return;
    }
    _seekable = false;
    _ad = (OoyalaAdSpot) ad;
    if (_ad.getStream() == null) {
      if (_fetchTask != null) {
        this._parent.getPlayerAPIClient().cancel(_fetchTask);
      }
      _fetchTask = _ad.fetchPlaybackInfo(new FetchPlaybackInfoCallback() {

        @Override
        public void callback(boolean result) {
          if (!result) {
            _error = "Error fetching VAST XML";
            setState(State.ERROR);
            return;
          }
          initAfterFetch(parent);
        }

      });
      return;
    }
    initAfterFetch(parent);
  }

  private void initAfterFetch(OoyalaPlayer parent) {
    super.init(parent, _ad.getStream().decodedURL().toString());

    // TODO[jigish] setup clickthrough

    if (_ad.getTrackingURLs() != null) {
      for (URL url : _ad.getTrackingURLs()) {
        NetUtils.ping(url);
      }
    }
  }

  public OoyalaAdSpot getAd() {
    return _ad;
  }

  @Override
  public void destroy() {
    if (_fetchTask != null) this._parent.getPlayerAPIClient().cancel(_fetchTask);
    super.destroy();
  }
}
