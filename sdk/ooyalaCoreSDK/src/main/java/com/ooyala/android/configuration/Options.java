package com.ooyala.android.configuration;

public class Options implements ReadonlyOptionsInterface {

  /**
   * Supports a fluid syntax for configuration.
   */
  public static class Builder {

    /**
     * FCCTVRatingConfiguration;
     * Default: default FCCTVRatingConfiguration object
     */
    private FCCTVRatingConfiguration tvRatingConfiguration;

    /**
     * VisualOnConfiguration
     * Default: default VisualOnConfiguration object
     */
    private VisualOnConfiguration visualOnConfiguration;

    /**
     *If set to "true",  show ad controls during ads playback.
     * Default: true
     */
    private boolean showAdsControls;

    /**
     * If set to "true", show cuepoint markers for ads.
     * Default: true
     */
    private boolean showCuePoints;

    /**
     * If set to "true", show live controls for live content playback (live stream only).
     * Default: true
     */
    private boolean showLiveControls;

    /**
     * If set to "true", load the content when the required information and authorization is available.
     * If set to "false", load the content after pre-roll (if pre-roll is available).
     *  Default: true
     */
    private boolean preloadContent;

    /**
     * If set to "true", show a promo image if one is available. 
     *  Default: false
     */
    private boolean showPromoImage;

    /**
     * Network connection timeout value used by networking operations.
     *  Default: 0
     */
    private int connectionTimeoutInMillisecond;

    /**
     * Read timeout value used by networking operations.
     * Default: 0
     */
    private int readTimeoutInMillisecond;

    /**
     * True to prevent screen sharing, false to allow.
     */
    private boolean preventVideoViewSharing;

    /**
     * True to use exoplayer
     */
    private boolean useExoPlayer;

    /**
     * Defaults to the following values:
     * tvRatingConfiguration = FCCTVRatingConfiguration.s_getDefaultTVRatingConfiguration();
     * visualOnConfiguration = VisualOnConfiguration.s_getDefaultVisualOnConfiguration();
     * showCuePoints = true;
     * showAdsControls = true;
     * showLiveControls = true;
     * preloadContent = true;
     * showPromoImage = false;
     * connectionTimeoutInMillisecond = 0;
     * readTimeoutInMillisecond = 0;
     * preventVideoViewSharing = false;
     */
    public Builder() {
      this.tvRatingConfiguration = FCCTVRatingConfiguration.s_getDefaultTVRatingConfiguration();
      this.visualOnConfiguration = VisualOnConfiguration.s_getDefaultVisualOnConfiguration();
      this.showCuePoints = true;
      this.showAdsControls = true;
      this.showLiveControls = true;
      this.preloadContent = true;
      this.showPromoImage = false;
      this.connectionTimeoutInMillisecond = 0;
      this.readTimeoutInMillisecond = 0;
      this.preventVideoViewSharing = false;
      this.useExoPlayer = false;
    }

    public Builder setTVRatingConfiguration( FCCTVRatingConfiguration tvRatingConfiguration ) {
      this.tvRatingConfiguration = tvRatingConfiguration;
      return this;
    }

    public Builder setVisualOnConfiguration( VisualOnConfiguration visualOnConfiguration) {
      this.visualOnConfiguration = visualOnConfiguration;
      return this;
    }

    public Builder setShowCuePoints(boolean showCuePoints) {
      this.showCuePoints = showCuePoints;
      return this;
    }

    public Builder setShowAdsControls(boolean showAdsControls) {
      this.showAdsControls = showAdsControls;
      return this;
    }

    public Builder setPreloadContent(boolean preloadContent) {
      this.preloadContent = preloadContent;
      return this;
    }

    public Builder setShowPromoImage(boolean showPromoImage) {
      this.showPromoImage = showPromoImage;
      return this;
    }

    public Builder setShowLiveControls(boolean showLiveControls) {
      this.showLiveControls = showLiveControls;
      return this;
    }

    /**
     * Set the connection timeout value used by networking operations.
     * @param connectionTimeoutInMillisecond - The connection timeout in milliseconds.
     * The default value is 0 (never times out).
     */
    public Builder setConnectionTimeout(int connectionTimeoutInMillisecond) {
      this.connectionTimeoutInMillisecond = connectionTimeoutInMillisecond;
      return this;
    }

    /**
     * Set the stream read timeout value used by networking operations.
     * @param readTimeoutInMillisecond -  The read timeout in milliseconds.
     *The default value is 0, (never times out).
     */
    public Builder setReadTimeout(int readTimeoutInMillisecond) {
      this.readTimeoutInMillisecond = readTimeoutInMillisecond;
      return this;
    }

    public Builder setPreventVideoViewSharing( boolean preventVideoViewSharing ) {
      this.preventVideoViewSharing = preventVideoViewSharing;
      return this;
    }

    public Builder setUseExoPlayer(boolean useExoPlayer) {
      this.useExoPlayer = useExoPlayer;
      return this;
    }

    public Options build() {
      return new Options(
          tvRatingConfiguration,
          visualOnConfiguration,
          showCuePoints,
          showAdsControls,
          preloadContent,
          showPromoImage,
          showLiveControls,
          connectionTimeoutInMillisecond,
          readTimeoutInMillisecond,
          preventVideoViewSharing,
          useExoPlayer);
    }
  }

  private final FCCTVRatingConfiguration tvRatingConfiguration;
  private final VisualOnConfiguration visualOnConfiguration;
  private final boolean showCuePoints;
  private final boolean showAdsControls;
  private final boolean preloadContent;
  private final boolean showPromoImage;
  private final int connectionTimeoutInMillisecond;
  private final int readTimeoutInMillisecond;
  private final boolean showLiveControls;
  private final boolean preventVideoViewSharing;
  private final boolean useExoPlayer;

  /**
   * Initialize an Options object with given parameters:
   * @param tvRatingConfiguration - Configure to use TV Ratings.
   * @param visualOnConfiguration - Configure to use VisualOn ads.
   * @param showCuePoints - Configure to show cue p oint markers.
   * @param showAdsControls - Configure to show ad controls in the player.
   * @param preloadContent - Configure to preload content before playback is initiated.
   * @param showPromoImage - Configure to show a promo image if one is avaiable.
   * @param showLiveControls - Configure to show live controls
   * @param connectionTimeoutInMillisecond - Configure to set a connection timeout value.
   * @param readTimeoutInMillisecond - Configure to set a read time out value.
   * @param preventVideoViewSharing - Configure to prevent/allow video sharing.
   * @return the initialized Options - Return the configured options.
   */
  private Options(FCCTVRatingConfiguration tvRatingConfiguration,
                  VisualOnConfiguration visualOnConfiguration,
                  boolean showCuePoints,
                  boolean showAdsControls,
                  boolean preloadContent,
                  boolean showPromoImage,
                  boolean showLiveControls,
                  int connectionTimeoutInMillisecond,
                  int readTimeoutInMillisecond,
                  boolean preventVideoViewSharing,
                  boolean useExoPlayer) {

    this.tvRatingConfiguration = tvRatingConfiguration;
    this.visualOnConfiguration = visualOnConfiguration;
    this.showCuePoints = showCuePoints;
    this.showAdsControls = showAdsControls;
    this.preloadContent = preloadContent;
    this.showPromoImage = showPromoImage;
    this.showLiveControls = showLiveControls;
    this.connectionTimeoutInMillisecond = connectionTimeoutInMillisecond;
    this.readTimeoutInMillisecond = readTimeoutInMillisecond;
    this.preventVideoViewSharing = preventVideoViewSharing;
    this.useExoPlayer = useExoPlayer;
  }

  @Override
  public FCCTVRatingConfiguration getTVRatingConfiguration() {
    return tvRatingConfiguration;
  }

  @Override
  public VisualOnConfiguration getVisualOnConfiguration() {
    return visualOnConfiguration;
  }

  @Override
  public boolean getShowAdsControls() {
    return showAdsControls;
  }

  @Override
  public boolean getShowCuePoints() {
    return showCuePoints;
  }

  @Override
  public boolean getPreloadContent() {
    return preloadContent;
  }

  @Override
  public boolean getShowPromoImage() {
    return showPromoImage;
  }

  @Override
  public boolean getShowLiveControls() {
    return showLiveControls;
  }

  @Override
  public int getConnectionTimeoutInMillisecond() {
    return connectionTimeoutInMillisecond;
  }

  @Override
  public int getReadTimeoutInMillisecond() {
    return readTimeoutInMillisecond;
  }

  @Override
  public boolean getPreventVideoViewSharing() {
    return preventVideoViewSharing;
  }

  @Override
  public boolean getUseExoPlayer() {
    return useExoPlayer;
  }
}
