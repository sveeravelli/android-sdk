package com.ooyala.android.configuration;

public interface ReadonlyOptionsInterface {

  /**
   * @see FCCTVRatingConfiguration
   */
  FCCTVRatingConfiguration getTVRatingConfiguration();

  /**
   * @see VisualOnConfiguration
   */
  VisualOnConfiguration getVisualOnConfiguration();

  /**
   * @see ExoConfiguration
   */
  ExoConfiguration getExoConfiguration();

  /**
   * If "true",  show ad controls during ads playback.
   */
  boolean getShowAdsControls();

  /**
   * If "true", show cuepoint markers for ads.
   */
  boolean getShowCuePoints();

  /**
   * If "true", show live controls for live content playback (live stream only).
   */
  boolean getShowLiveControls();

  /**
   * If "true", load the content when the rquired information and authorization is available.
   * If "false", load the content after pre-roll (if pre-roll is available).
   */
  boolean getPreloadContent();

  /**
   * If "true", show a promo image if one is available.
   */
  boolean getShowPromoImage();

  /**
   * Network connection timeout value used by networking operations.
   */
  int getConnectionTimeoutInMillisecond();

  /**
   * Read timeout value used by networking operations.
   */
  int getReadTimeoutInMillisecond();

  /**
   * True is prevent video view sharing, false is allow.
   */
  boolean getPreventVideoViewSharing();

  /**
   * True if use exoplayer, false otherwise.
   */
  boolean getUseExoPlayer();

  /**
   * Is set True to show Learn More button for Ads.
   */
  boolean getShowNativeLearnMoreButton();

}
