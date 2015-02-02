package com.ooyala.android.configuration;

public class Options implements ReadonlyOptionsInterface {

  public static class Builder {
    private FCCTVRatingConfiguration tvRatingConfiguration;
    private VisualOnConfiguration visualOnConfiguration;
    private boolean showCuePoints;
    private boolean showAdsControls;
    private boolean showLiveControls;
    private boolean preloadContent;
    private boolean showPromoImage;
    private int connectionTimeoutInMillisecond;
    private int readTimeoutInMillisecond;

    public Builder() {
      this.tvRatingConfiguration = FCCTVRatingConfiguration.s_getDefaultTVRatingConfiguration();
      this.visualOnConfiguration = VisualOnConfiguration.s_getDefaultVisualOnConfiguration();
      this.showCuePoints = true;
      this.showAdsControls = true;
      this.preloadContent = true;
      this.showPromoImage = false;
      this.connectionTimeoutInMillisecond = 0;
      this.readTimeoutInMillisecond = 0;
      this.showLiveControls = true;
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
     * Set the connection timeout value used by networking operations
     * @param connectionTimeoutInMillisecond the connection timeout in millisecond
     * default value is 0, which means never time out
     */
    public Builder setConnectionTimeout(int connectionTimeoutInMillisecond) {
      this.connectionTimeoutInMillisecond = connectionTimeoutInMillisecond;
      return this;
    }

    /**
     * Set the stream read timeout value used by networking operations
     * @param readTimeoutInMillisecond the read timeout in millisecond
     * default value is 0, which means never time out
     */
    public Builder setReadTimeout(int readTimeoutInMillisecond) {
      this.readTimeoutInMillisecond = readTimeoutInMillisecond;
      return this;
    }

    public Options build() {
      return new Options(tvRatingConfiguration, visualOnConfiguration,
          showCuePoints, showAdsControls, preloadContent, showPromoImage, showLiveControls,
              connectionTimeoutInMillisecond, readTimeoutInMillisecond);
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

  private Options(FCCTVRatingConfiguration tvRatingConfiguration,
      VisualOnConfiguration visualOnConfiguration, boolean showCuePoints,
      boolean showAdsControls, boolean preloadContent, boolean showPromoImage, boolean showLiveControls,
      int connectionTimeoutInMillisecond, int readTimeoutInMillisecond) {

    this.tvRatingConfiguration = tvRatingConfiguration;
    this.visualOnConfiguration = visualOnConfiguration;
    this.showCuePoints = showCuePoints;
    this.showAdsControls = showAdsControls;
    this.preloadContent = preloadContent;
    this.showPromoImage = showPromoImage;
    this.showLiveControls = showLiveControls;
    this.connectionTimeoutInMillisecond = connectionTimeoutInMillisecond;
    this.readTimeoutInMillisecond = readTimeoutInMillisecond;
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
}
