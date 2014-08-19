package com.ooyala.android.configuration;

public class Options implements ReadonlyOptionsInterface {

  public static class Builder {
    private TVRatingsConfiguration tvRatingsConfiguration;
    
    public Builder() {
      this.tvRatingsConfiguration = TVRatingsConfiguration.s_getDefaultTVRatingsConfiguration(); 
    }

    public Builder setTVRatingsConfiguration( TVRatingsConfiguration tvRatingsConfiguration ) {
      this.tvRatingsConfiguration = tvRatingsConfiguration;
      return this;
    }

    public Options build() {
      return new Options( tvRatingsConfiguration );
    }
  }

  private final TVRatingsConfiguration tvRatingsConfiguration;

  private Options( TVRatingsConfiguration tvRatingsConfiguration ) {
    this.tvRatingsConfiguration = tvRatingsConfiguration;
  }

  public TVRatingsConfiguration getTVRatingsConfiguration() {
    return tvRatingsConfiguration;
  }
}
