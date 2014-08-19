package com.ooyala.android.configuration;

public class TVRatingsConfiguration {

  public static final long TIMER_NEVER = 0;
  public static final long TIMER_ALWAYS = Long.MAX_VALUE;
  public enum Position {
    TopLeft,
    TopRight,
    BottomLeft,
    BottomRight;
  }

  public long durationSeconds;
  public Position position;
  public double percentScale;
  public double opacity;

  public static final TVRatingsConfiguration getDefaultTVRatingsConfiguration() {
    return new TVRatingsConfiguration( TIMER_NEVER, Position.TopLeft, 0.2, 0.9 );
  }

  public TVRatingsConfiguration( long timerSeconds, Position position, double percentScale, double opacity ) {
    this.durationSeconds = timerSeconds;
    this.position = position;
    this.percentScale = percentScale;
    this.opacity = opacity;
  }

  public TVRatingsConfiguration setTimerSeconds( long timerSeconds ) { this.durationSeconds = timerSeconds; return this; }
  public TVRatingsConfiguration setPosition( Position position ) { this.position = position; return this; }
  public TVRatingsConfiguration setPercentScale( double percentScale ) { this.percentScale = percentScale; return this; }
  public TVRatingsConfiguration setOpacity( double opacity ) { this.opacity = opacity; return this; }
}
