package com.ooyala.android;

import java.net.URL;

import com.ooyala.android.OoyalaPlayer.State;

class OoyalaAdPlayer extends MoviePlayer {
  private OoyalaAdSpot _ad;

  public OoyalaAdPlayer() {
    super();
  }

  @Override
  public void init(OoyalaPlayer parent, Object ad) {
    if (!(ad instanceof OoyalaAdSpot)) {
      this._error = "Invalid Ad";
      this._state = State.ERROR;
      return;
    }
    _seekable = false;
    _ad = (OoyalaAdSpot) ad;

    if (_ad.getStream() == null) {
      // TODO async call to fetch playback info!
      initAfterFetch(parent);
      return;
    }
    initAfterFetch(parent);
  }

  private void initAfterFetch(OoyalaPlayer parent) {
    super.init(parent, _ad.getStream());

    // TODO[jigish] setup clickthrough

    NetUtils pinger = new NetUtils();
    for (URL url : _ad.getTrackingURLs()) {
      pinger.ping(url);
    }
  }

  public OoyalaAdSpot getAd() {
    return _ad;
  }
}
