package com.ooyala.android.configuration;

public class TVRatingsConfiguration {

  public enum Position {
    TopLeft,
    TopRight,
    BottomLeft,
    BottomRight;
  }

  public static final long TIMER_NEVER = 0;
  public static final long TIMER_ALWAYS = Long.MAX_VALUE;
  public static final long DEFAULT_TIMER = TIMER_ALWAYS;//TIMER_NEVER;
  public static final Position DEFAULT_POSITION = Position.TopLeft;
  public static final float DEFAULT_SCALE = 0.2f;
  public static final float DEFAULT_OPACITY = 0.9f;
  
  public long durationSeconds;
  public Position position;
  public float scale;
  public float opacity;

  public static final TVRatingsConfiguration s_getDefaultTVRatingsConfiguration() {
    return new TVRatingsConfiguration( DEFAULT_TIMER, DEFAULT_POSITION, DEFAULT_SCALE, DEFAULT_OPACITY );
  }

  public TVRatingsConfiguration( long timerSeconds, Position position, float percentScale, float opacity ) {
    this.durationSeconds = timerSeconds;
    this.position = position;
    this.scale = percentScale;
    this.opacity = opacity;
  }

  public TVRatingsConfiguration setTimerSeconds( long timerSeconds ) { this.durationSeconds = timerSeconds; return this; }
  public TVRatingsConfiguration setPosition( Position position ) { this.position = position; return this; }
  public TVRatingsConfiguration setPercentScale( float percentScale ) { this.scale = percentScale; return this; }
  public TVRatingsConfiguration setOpacity( float opacity ) { this.opacity = opacity; return this; }
}
