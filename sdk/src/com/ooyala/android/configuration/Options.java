package com.ooyala.android.configuration;

public class Options implements ReadonlyOptionsInterface {

  public static class Builder {
    private FCCTVRatingConfiguration tvRatingConfiguration;
    private VisualOnConfiguration visualOnConfiguration;
    private boolean showCuePoints;
    private boolean showAdsControls;

    public Builder() {
      this.tvRatingConfiguration = FCCTVRatingConfiguration.s_getDefaultTVRatingConfiguration();
      this.visualOnConfiguration = VisualOnConfiguration.s_getDefaultVisualOnConfiguration();
      this.showCuePoints = true;
      this.showAdsControls = true;
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

    public Options build() {
      return new Options(tvRatingConfiguration, visualOnConfiguration,
          showCuePoints, showAdsControls);
    }
  }

  private final FCCTVRatingConfiguration tvRatingConfiguration;
  private final VisualOnConfiguration visualOnConfiguration;
  private final boolean showCuePoints;
  private final boolean showAdsControls;

  private Options(FCCTVRatingConfiguration tvRatingConfiguration, VisualOnConfiguration visualOnConfiguration, boolean showCuePoints, boolean showAdsControls) {
    this.tvRatingConfiguration = tvRatingConfiguration;
    this.visualOnConfiguration = visualOnConfiguration;
    this.showCuePoints = showCuePoints;
    this.showAdsControls = showAdsControls;
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
}
