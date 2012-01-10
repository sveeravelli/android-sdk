package com.ooyala.android.player;

import java.net.URL;

import android.content.Context;

import com.ooyala.android.NetUtils;
import com.ooyala.android.OoyalaAdSpot;
import com.ooyala.android.OoyalaPlayer.OoyalaPlayerState;

public class OoyalaAdPlayer extends MoviePlayer {
  private OoyalaAdSpot _ad;

  public OoyalaAdPlayer() {
    super();
  }

  public OoyalaAdPlayer(Context c, OoyalaAdSpot ad) {
    init(c, ad);
  }

  public void init(Context c, Object ad) {
    if (!(ad instanceof OoyalaAdSpot)) {
      this._error = "Invalid Ad";
      this._state = OoyalaPlayerState.OoyalaPlayerStateError;
      return;
    }
    _ad = (OoyalaAdSpot)ad;
    super.init(c, _ad.getStream().decodedURL());

    // TODO[jigish] setup clickthrough

    NetUtils pinger = new NetUtils();
    for (URL url : _ad.getTrackingURLs()) {
      pinger.ping(url);
    }
  }

  @Override
  public boolean seekable() {
    return false;
  }

  public OoyalaAdSpot getAd() {
    return _ad;
  }

  public void setAd(OoyalaAdSpot ad) {
    this._ad = ad;
  }

}
