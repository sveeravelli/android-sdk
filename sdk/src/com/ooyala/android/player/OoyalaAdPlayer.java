package com.ooyala.android.player;

import java.net.URL;

import com.ooyala.android.NetUtils;
import com.ooyala.android.OoyalaAdSpot;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;

public class OoyalaAdPlayer extends MoviePlayer {
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
    _ad = (OoyalaAdSpot)ad;

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
