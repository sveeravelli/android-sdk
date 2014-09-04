package com.ooyala.android.configuration;

public class TVRatingConfiguration {

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
  
  public long durationSeconds;
  public Position position;
  public float scale;
  public float opacity;

  public static final TVRatingConfiguration s_getDefaultTVRatingConfiguration() {
    return new TVRatingConfiguration( DEFAULT_DURATION_SECONDS, DEFAULT_POSITION, DEFAULT_SCALE, DEFAULT_OPACITY );
  }

  public TVRatingConfiguration( long displayDurationSeconds, Position position, float percentScale, float opacity ) {
    this.durationSeconds = displayDurationSeconds;
    this.position = position;
    this.scale = percentScale;
    this.opacity = opacity;
  }

  public TVRatingConfiguration setDurationSeconds( long durationSeconds ) { this.durationSeconds = durationSeconds; return this; }
  public TVRatingConfiguration setPosition( Position position ) { this.position = position; return this; }
  public TVRatingConfiguration setScale( float scale ) { this.scale = scale; return this; }
  public TVRatingConfiguration setOpacity( float opacity ) { this.opacity = opacity; return this; }
}
