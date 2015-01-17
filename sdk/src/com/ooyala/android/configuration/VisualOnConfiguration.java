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
    
    /**
     * Set the upper bit rate threshold
     * @param upperBitrateThreshold 
     * @return the Builder object to continue building
     */
    public Builder setUpperBitrateThreshold(int upperBitrateThreshold) {
      this.upperBitrateThreshold = upperBitrateThreshold;
      return this;
    }
    
    /**
     * Set the lower bit rate threshold
     * @param lowerBitrateThreshold
     * @return the Builder object to continue building
     */
    public Builder setLowerBitrateThreshold(int lowerBitrateThreshold) {
      this.lowerBitrateThreshold = lowerBitrateThreshold;
      return this;
    }
    
    /**
     * Set initial bit rate
     * @param initialBitrate
     * @return the Builder object to continue building
     */
    public Builder setInitialBitrate(int initialBitrate) {
      this.initialBitrate = initialBitrate;
      return this;
    }
    
    /**
     * Set initial buffering time
     * @param initialBufferingTime
     * @return the Builder object to continue building
     */
    public Builder setInitialBufferingTime(int initialBufferingTime) {
      this.initialBufferingTime = initialBufferingTime;
      return this;
    }
    
    /**
     * Set max buffering time
     * @param maxBufferingTime
     * @return the Builder object to continue building
     * 
     */
    public Builder setMaxBufferingTime(int maxBufferingTime) {
      this.maxBufferingTime = maxBufferingTime;
      return this;
    }
    
    /**
     * Set the buffering time when re-buffering is needed during playback
     * @param playbackBufferingTime
     * @return the Builder object to continue building
     */
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
  
  /**
   * 
   * @return upper bit rate threshold
   */
  public int getUpperBitrateThreshold() {
    return this.upperBitrateThreshold;
  }
  
  /**
   * 
   * @return lower bit rate threshold
   */
  public int getLowerBitrateThreshold() {
    return this.lowerBitrateThreshold;
  }
  
  /**
   * 
   * @return initial bit rate
   */
  public int getInitialBitrate() {
    return this.initialBitrate;
  }
  
  /**
   * 
   * @return max buffering time
   */
  public int getMaxBufferingTime() {
    return this.maxBufferingTime;
  }
  
  /**
   * 
   * @return initial buffering time
   */
  public int getInitialBufferingTime() {
    return this.initialBufferingTime;
  }
  
  /**
   * 
   * @return the buffering time when re-buffering is needed during playback
   */
  public int getPlaybackBufferingTime() {
    return this.playbackBufferingTime;
  }
}
