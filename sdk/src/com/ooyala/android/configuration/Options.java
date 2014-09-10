package com.ooyala.android.configuration;

public class Options implements ReadonlyOptionsInterface {

  public static class Builder {
    private FCCTVRatingConfiguration tvRatingConfiguration;
    
    public Builder() {
      this.tvRatingConfiguration = FCCTVRatingConfiguration.s_getDefaultTVRatingConfiguration(); 
    }

    public Builder setTVRatingConfiguration( FCCTVRatingConfiguration tvRatingConfiguration ) {
      this.tvRatingConfiguration = tvRatingConfiguration;
      return this;
    }

    public Options build() {
      return new Options( tvRatingConfiguration );
    }
  }

  private final FCCTVRatingConfiguration tvRatingConfiguration;

  private Options( FCCTVRatingConfiguration tvRatingConfiguration ) {
    this.tvRatingConfiguration = tvRatingConfiguration;
  }

  public FCCTVRatingConfiguration getTVRatingConfiguration() {
    return tvRatingConfiguration;
  }
}
