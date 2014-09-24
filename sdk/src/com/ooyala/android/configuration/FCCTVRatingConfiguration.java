package com.ooyala.android.configuration;

public class FCCTVRatingConfiguration {

  public enum Position {
    TopLeft,
    TopRight,
    BottomLeft,
    BottomRight;
  }

  public static final long DURATION_NONE = 0;
  public static final long DURATION_FOR_EVER = Long.MAX_VALUE;
  public static final long DEFAULT_DURATION_SECONDS = 10;//TIMER_NEVER;
  public static final Position DEFAULT_POSITION = Position.TopLeft;
  public static final float DEFAULT_SCALE = 0.2f;
  public static final float DEFAULT_OPACITY = 0.9f;
  
  public static class Builder {
    public long durationSeconds;
    public Position position;
    public float scale;
    public float opacity;
    
    public Builder() {
      this.durationSeconds = DURATION_NONE;
      this.position = DEFAULT_POSITION;
      this.scale = DEFAULT_SCALE;
      this.opacity = DEFAULT_OPACITY;
    }
    
    public FCCTVRatingConfiguration build() {
      return new FCCTVRatingConfiguration( durationSeconds, position, scale, opacity );
    }

    public Builder setDurationSeconds( long durationSeconds ) {
      this.durationSeconds = durationSeconds; return this;
    }
    public Builder setPosition( Position position ) {
      this.position = position; return this;
    }
    public Builder setScale( float scale ) {
      this.scale = scale; return this;
    }
    public Builder setOpacity( float opacity ) {
      this.opacity = opacity; return this;
    }
  }
  
  public long durationSeconds;
  public Position position;
  public float scale;
  public float opacity;

  public static final FCCTVRatingConfiguration s_getDefaultTVRatingConfiguration() {
    return new FCCTVRatingConfiguration( DEFAULT_DURATION_SECONDS, DEFAULT_POSITION, DEFAULT_SCALE, DEFAULT_OPACITY );
  }

  public FCCTVRatingConfiguration( long durationSeconds, Position position, float scale, float opacity ) {
    this.durationSeconds = durationSeconds;
    this.position = position;
    this.scale = scale;
    this.opacity = opacity;
  }
}
