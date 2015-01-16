package com.ooyala.android.configuration;


/**
 * VisualOnConfiguration is a bundle that holds application-defined properties that configure
 * Ooyala Player's use of VisualOn and SecurePlayer
 *
 */
public class VisualOnConfiguration {
  public boolean disableLibraryVersionChecks;
  private int upperBitrateThreshold;
  private int lowerBitrateThreshold;
  private int initialBitrate;
  private int initialBufferingTime;
  private int maxBufferingTime;
  private int playbackBufferingTime;
  
  
  // Check the default number for these fields
  private static boolean DEFAULT_DISABLE_LIBRARY_VERSION_CHECKS = false;
  private static int DEFAULT_UPPER_BITRATE_THRESHOLD;
  private static int DEFAULT_LOWER_BITRATE_THRESHOLD;
  private static int DEFAULT_INITIAL_BITRATE;
  private static int DEFAULT_INITIAL_BUFFERING_TIME;
  private static int DEFAULT_MAX_BUFFERING_TIME;
  private static int DEFAULT_PLAYBACK_BUFFERING_TIME;

  /**
   * Build the object of VisualOn configurations
   *
   */
  public static class Builder {
    private boolean disableLibraryVersionChecks;
    private int upperBitrateThreshold;
    private int lowerBitrateThreshold;
    private int initialBitrate;
    private int initialBufferingTime;
    private int maxBufferingTime;
    private int playbackBufferingTime;

    public Builder() {
      this.disableLibraryVersionChecks = false;
      this.upperBitrateThreshold = -1;
      this.lowerBitrateThreshold = -1;
      this.initialBitrate = -1;
      this.initialBufferingTime = -1;
      this.maxBufferingTime = -1;
      this.playbackBufferingTime = -1;
    }

    /**
     * Disables the version check when using the VisualOn or SecurePlayer libraries.
     * @param DisableLibraryVersionChecks true if you want to allow playback with unexpected VisualOn versions (default false)
     * @return the Builder object to continue building
     */
    public Builder setDisableLibraryVersionChecks( boolean disableLibraryVersionChecks ) {
      this.disableLibraryVersionChecks = disableLibraryVersionChecks;
      return this;
    }
    
    public Builder setUpperBitrateThreshold(int upperBitrateThreshold) {
      this.upperBitrateThreshold = upperBitrateThreshold;
      return this;
    }
    
    public Builder setLowerBitrateThreshold(int lowerBitrateThreshold) {
      this.lowerBitrateThreshold = lowerBitrateThreshold;
      return this;
    }
    
    public Builder setInitialBitrate(int initialBitrate) {
      this.initialBitrate = initialBitrate;
      return this;
    }
    
    public Builder setInitialBufferingTime(int initialBufferingTime) {
      this.initialBufferingTime = initialBufferingTime;
      return this;
    }
    
    public Builder setMaxBufferingTime(int maxBufferingTime) {
      this.maxBufferingTime = maxBufferingTime;
      return this;
    }
    
    public Builder setPlaybackBufferingTime(int playbackBufferingTime) {
      this.playbackBufferingTime = playbackBufferingTime;
      return this;
    }
    

    /**
     * Generates a fully initialized VisualOnConfiguration
     * @return a VisualOnConfiguration for providing in the Options
     */
    public VisualOnConfiguration build() {
      return new VisualOnConfiguration(this.disableLibraryVersionChecks, this.upperBitrateThreshold, this.lowerBitrateThreshold, this.initialBitrate,  this.maxBufferingTime, this.initialBufferingTime, this.playbackBufferingTime);
    }

  }

  /**
   * Provides the default VisualOn configuration
   * @return the default VisualOn configuration
   */
  public static final VisualOnConfiguration s_getDefaultVisualOnConfiguration() {
    return new VisualOnConfiguration( DEFAULT_DISABLE_LIBRARY_VERSION_CHECKS );
  }

  /**
   * Initialize a VisualOnConfiguration. Private in favor of the Builder class
   * @param disableLibraryVersionChecks true if you want to allow playback with unexpected VisualOn versions (default false)
   */
  private VisualOnConfiguration(boolean disableLibraryVersionChecks) {
    this.disableLibraryVersionChecks = disableLibraryVersionChecks;
  }
  
  /**
   * Initialize a VisualOnConfiguration. Private in favor of the Builder class
   * @param disableLibraryVersionChecks true if you want to allow playback with unexpected VisualOn versions (default false)
   */
  private VisualOnConfiguration(boolean disableLibraryVersionChecks, int upperBitrateThreshold, int lowerBitrateThreshold, int initialBitrate, int maxBufferingTime, int initialBufferingTime, int playbackBufferingTime) {
    this.disableLibraryVersionChecks = disableLibraryVersionChecks;
    this.upperBitrateThreshold = upperBitrateThreshold;
    this.lowerBitrateThreshold = lowerBitrateThreshold;
    this.initialBitrate = initialBitrate;
    this.maxBufferingTime = maxBufferingTime;
    this.initialBufferingTime = initialBufferingTime;
    this.playbackBufferingTime = playbackBufferingTime;
  }
  
  public int getUpperBitrateThreshold() {
    return this.upperBitrateThreshold;
  }
  
  public int getLowerBitrateThreshold() {
    return this.lowerBitrateThreshold;
  }
  
  public int getInitialBitrate() {
    return this.initialBitrate;
  }
  
  public int getMaxBufferingTime() {
    return this.maxBufferingTime;
  }
  
  public int getInitialBufferingTime() {
    return this.initialBufferingTime;
  }
  
  public int getPlaybackBufferingTime() {
    return this.playbackBufferingTime;
  }
}
