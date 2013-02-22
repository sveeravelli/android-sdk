package com.ooyala.android;

public abstract class AdMoviePlayer extends MoviePlayer {

  public void init(OoyalaPlayer parent, AdSpot ad) { }

  public abstract AdSpot getAd();

}
