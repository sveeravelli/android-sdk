package com.ooyala.android;

import android.widget.FrameLayout;

public abstract class AdMoviePlayer extends MoviePlayer {

  public void init(OoyalaPlayer parent, AdSpot ad) { }

  public abstract AdSpot getAd();

  public void processClickThrough() { }

  public void updateLearnMoreButton(FrameLayout layout, int topMargin) { }

}
