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

    public Builder() {
      this.tvRatingConfiguration = FCCTVRatingConfiguration.s_getDefaultTVRatingConfiguration();
      this.visualOnConfiguration = VisualOnConfiguration.s_getDefaultVisualOnConfiguration();
      this.showCuePoints = true;
      this.showAdsControls = true;
      this.preloadContent = true;
      this.showPromoImage = false;
      this.showLiveControls = false;
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

    public Options build() {
      return new Options(tvRatingConfiguration, visualOnConfiguration,
          showCuePoints, showAdsControls, preloadContent, showPromoImage, showLiveControls);
    }
  }

  private final FCCTVRatingConfiguration tvRatingConfiguration;
  private final VisualOnConfiguration visualOnConfiguration;
  private final boolean showCuePoints;
  private final boolean showAdsControls;
  private final boolean preloadContent;
  private final boolean showPromoImage;
  private final boolean showLiveControls;

  private Options(FCCTVRatingConfiguration tvRatingConfiguration,
      VisualOnConfiguration visualOnConfiguration, boolean showCuePoints,
      boolean showAdsControls, boolean preloadContent, boolean showPromoImage, boolean showLiveControls) {
    this.tvRatingConfiguration = tvRatingConfiguration;
    this.visualOnConfiguration = visualOnConfiguration;
    this.showCuePoints = showCuePoints;
    this.showAdsControls = showAdsControls;
    this.preloadContent = preloadContent;
    this.showPromoImage = showPromoImage;
    this.showLiveControls = showLiveControls;
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
  
}
