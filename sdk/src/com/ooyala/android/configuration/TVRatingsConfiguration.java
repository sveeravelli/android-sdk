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

  public final long timerSeconds;
  public final Position position;
  public final double percentScale;
  public final double opacity;

  public static final TVRatingsConfiguration getDefaultTVRatingsConfiguration() {
    return new TVRatingsConfiguration(TIMER_NEVER, Position.TopLeft, 0.2, 0.9);
  }

  public TVRatingsConfiguration(long timerSeconds, Position position, double percentScale, double opacity) {
    this.timerSeconds = timerSeconds;
    this.position = position;
    this.percentScale = percentScale;
    this.opacity = opacity;
  }

}
