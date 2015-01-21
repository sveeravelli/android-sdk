package com.ooyala.android.configuration;

public interface ReadonlyOptionsInterface {

  FCCTVRatingConfiguration getTVRatingConfiguration();

  VisualOnConfiguration getVisualOnConfiguration();

  boolean getShowAdsControls();

  boolean getShowCuePoints();

  boolean getPreloadContent();

  boolean getShowPromoImage();

  boolean getShowLiveControls();
}
