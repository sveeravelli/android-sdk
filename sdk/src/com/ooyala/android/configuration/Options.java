package com.ooyala.android.configuration;

public class Options implements ReadonlyOptionsInterface {

  public static class Builder {
    private TVRatingConfiguration tvRatingConfiguration;
    
    public Builder() {
      this.tvRatingConfiguration = TVRatingConfiguration.s_getDefaultTVRatingConfiguration(); 
    }

    public Builder setTVRatingConfiguration( TVRatingConfiguration tvRatingConfiguration ) {
      this.tvRatingConfiguration = tvRatingConfiguration;
      return this;
    }

    public Options build() {
      return new Options( tvRatingConfiguration );
    }
  }

  private final TVRatingConfiguration tvRatingConfiguration;

  private Options( TVRatingConfiguration tvRatingConfiguration ) {
    this.tvRatingConfiguration = tvRatingConfiguration;
  }

  public TVRatingConfiguration getTVRatingConfiguration() {
    return tvRatingConfiguration;
  }
}
