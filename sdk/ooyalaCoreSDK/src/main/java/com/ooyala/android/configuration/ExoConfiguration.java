package com.ooyala.android.configuration;

import com.google.android.exoplayer.DefaultLoadControl;
import com.ooyala.android.util.DebugMode;

/**
 * Created by zchen on 2/17/16.
 */
public class ExoConfiguration {
  private static final String TAG = ExoConfiguration.class.getSimpleName();
  private final long upperBitrateThreshold;
  private final long lowerBitrateThreshold;
  private final long lowWatermarkMs;
  private final long highWatermarkMs;
  private final float lowBufferLoad;
  private final float highBufferLoad;

  /**
   * Build the object of VisualOn configurations
   *
   */
  public static class Builder {
    private long upperBitrateThreshold;
    private long lowerBitrateThreshold;
    private long lowWatermarkMs;
    private long highWatermarkMs;
    private float lowBufferLoad;
    private float highBufferLoad;

    public Builder() {
      this.upperBitrateThreshold = Integer.MAX_VALUE;
      this.lowerBitrateThreshold = 0;
      this.lowWatermarkMs = DefaultLoadControl.DEFAULT_LOW_WATERMARK_MS;
      this.highWatermarkMs = DefaultLoadControl.DEFAULT_HIGH_WATERMARK_MS;
      this.lowBufferLoad = DefaultLoadControl.DEFAULT_LOW_BUFFER_LOAD;
      this.highBufferLoad = DefaultLoadControl.DEFAULT_HIGH_BUFFER_LOAD;
    }

    /**
     * Set the upper bit rate threshold
     *
     * @param upperBitrateThreshold
     * @return the Builder object to continue building
     */
    public Builder setUpperBitrateThreshold(long upperBitrateThreshold) {
      this.upperBitrateThreshold = upperBitrateThreshold;
      return this;
    }

    /**
     * Set the lower bit rate threshold
     *
     * @param lowerBitrateThreshold
     * @return the Builder object to continue building
     */
    public Builder setLowerBitrateThreshold(long lowerBitrateThreshold) {
      this.lowerBitrateThreshold = lowerBitrateThreshold;
      return this;
    }

    /**
     * Set high watermark in millisecond
     * @param highWatermarkMs The minimum duration of media that can be buffered for the control to
     *     transition from filling to draining.
     *
     * @return the Builder object to continue building
     */
    public Builder setHighWatermarkMs(long highWatermarkMs) {
      this.highWatermarkMs = highWatermarkMs;
      return this;
    }

    /**
     * Set high watermark in millisecond, if buffer below this watermark buffering will start
     *
     * @param lowWatermarkMs The minimum duration of media that can be buffered for the control to
     *     be in the draining state. If less media is buffered, then the control will transition to
     *     the filling state.
     * @return the Builder object to continue building
     */
    public Builder setLowWatermarkMs(long lowWatermarkMs) {
      this.lowWatermarkMs = lowWatermarkMs;
      return this;
    }

    /**
     * Set the maximum amount of video that can be buffered during video playback
     *
     * @param highBufferLoad The minimum fraction of the buffer that must be utilized for the control
     *     to transition from the loading state to the draining state. should be between 0 and 1
     * @return the Builder object to continue building
     */
    public Builder setHighBufferLoad(float highBufferLoad) {
      this.highBufferLoad = highBufferLoad;
      return this;
    }

    /**
     * Set the amount of video that is buffered when re-buffering is needed during playback
     *
     * @param lowBufferLoad The minimum fraction of the buffer that must be utilized for the control
     *     to be in the draining state. If the utilization is lower, then the control will transition
     *     to the filling state. should be between 0 and 1
     * @return the Builder object to continue building
     */
    public Builder setLowBufferLoad(float lowBufferLoad) {
      this.lowBufferLoad = lowBufferLoad;
      return this;
    }

    public ExoConfiguration build() {
      if (validate()) {
        return new ExoConfiguration(upperBitrateThreshold, lowerBitrateThreshold, lowWatermarkMs, highWatermarkMs, lowBufferLoad, highBufferLoad);
      } else {
        return null;
      }
    }

    private boolean validate() {
      if ((lowerBitrateThreshold > upperBitrateThreshold) ||
          (lowWatermarkMs > highWatermarkMs) ||
          (lowBufferLoad > highBufferLoad) ||
          (lowBufferLoad < 0) ||
          (highBufferLoad > 1)){
        DebugMode.logE(TAG,
            "Invalid parameters: upperBitrate " + upperBitrateThreshold + " lowerBitrate " + lowerBitrateThreshold + " highWatermark "
                + highWatermarkMs + " lowWatermark " + lowWatermarkMs + "highLoad" + highBufferLoad + "lowLoad" + lowBufferLoad);
        return false;
      }
      return true;
    }
  }

  /**
   * Provides the default ExoConfiguration
   * @return the default configuration
   */
  public static ExoConfiguration getDefaultExoConfiguration() {
    return new Builder().build();
  }

  private ExoConfiguration(
      long upperBitrateThreshold,
      long lowerBitrateThreshold,
      long lowWatermarkMs,
      long highWatermarkMs,
      float lowBufferLoad,
      float highBufferLoad) {
    this.upperBitrateThreshold = upperBitrateThreshold;
    this.lowerBitrateThreshold = lowerBitrateThreshold;
    this.highWatermarkMs = highWatermarkMs;
    this.lowWatermarkMs = lowWatermarkMs;
    this.highBufferLoad = highBufferLoad;
    this.lowBufferLoad = lowBufferLoad;
  }

  /**
   *
   * @return upper bit rate threshold
   */
  public long getUpperBitrateThreshold() {
    return upperBitrateThreshold;
  }

  /**
   *
   * @return lower bit rate threshold
   */
  public long getLowerBitrateThreshold() {
    return lowerBitrateThreshold;
  }

  /**
   *
   * @return high watermark in milliseconds
   */
  public long getHighWatermarkMs() {
    return highWatermarkMs;
  }

  /**
   *
   * @return low watermark in milliseconds
   */
  public long getLowWatermarkMs() {
    return lowWatermarkMs;
  }

  /**
   *
   * @return high buffer load
   */
  public float getHighBufferLoad() {
    return highBufferLoad;
  }

  /**
   *
   * @return low buffer load
   */
  public float getLowBufferLoad() {
    return lowBufferLoad;
  }
}
