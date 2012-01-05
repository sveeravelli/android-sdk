package com.ooyala.android.player;

import java.net.URL;

import com.ooyala.android.NetUtils;
import com.ooyala.android.OoyalaAdSpot;

public class OoyalaAdPlayer extends MoviePlayer {
  private OoyalaAdSpot _ad;

  public OoyalaAdPlayer(URL url) {
    super(url);
  }
  
  public OoyalaAdPlayer(OoyalaAdSpot ad) {
    this(ad.getStream().decodedURL());
    _ad = ad;
    
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
