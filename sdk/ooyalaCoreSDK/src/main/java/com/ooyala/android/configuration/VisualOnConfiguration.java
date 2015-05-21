package com.ooyala.android.configuration;

/**
 * VisualOnConfiguration is a bundle that holds application-defined properties that configure
 * Ooyala Player's use of VisualOn and SecurePlayer
 *
 */
public class VisualOnConfiguration {

  public static String PRODUCTION_PERSONALIZATION_SERVER_URL = "http://perso.purpledrm.com/PersoServer/Personalization";

  private boolean disableLibraryVersionChecks;
  private String personalizationServerUrl;
  private int upperBitrateThreshold;
  private int lowerBitrateThreshold;
  private int initialBitrate;
  private int initialBufferingTime;
  private int maxBufferingTime;
  private int playbackBufferingTime;

  /**
   * Build the object of VisualOn configurations
   *
   */
  public static class Builder {
    private boolean disableLibraryVersionChecks;
    private String personalizationServerUrl;
    private int upperBitrateThreshold;
    private int lowerBitrateThreshold;
    private int initialBitrate;
    private int initialBufferingTime;
    private int maxBufferingTime;
    private int playbackBufferingTime;

    public Builder() {
      this.disableLibraryVersionChecks = false;
      this.personalizationServerUrl = "http://persopp.purpledrm.com/PersoServer/Personalization";
      this.upperBitrateThreshold = -1;
      this.lowerBitrateThreshold = -1;
      this.initialBitrate = -1;
      this.initialBufferingTime = -1;
      this.maxBufferingTime = -1;
      this.playbackBufferingTime = -1;
    }

    /**
     * Disables the version check when using the VisualOn or SecurePlayer libraries.
     * @param disableLibraryVersionChecks true if you want to allow playback with unexpected VisualOn versions (default false)
     * @return the Builder object to continue building
     */
    public Builder setDisableLibraryVersionChecks( boolean disableLibraryVersionChecks ) {
      this.disableLibraryVersionChecks = disableLibraryVersionChecks;
      return this;
    }

    /**
     * Sets the personalization server URL used for personalization.
     *
     * This targets Viaccess-Orca's Pre-Production Personalization server, and must be modified
     * to target the production server when the application is intended to be deployed.  You can use
     * PRODUCTION_PERSONALIZATION_SERVER_URL when you are certified to use production personalization
     * @param personalizationServerUrl the url to be used for personalization
     * @return the Builder object to continue building
     */
    public Builder setPersonalizationServerUrl( String personalizationServerUrl ) {
      this.personalizationServerUrl = personalizationServerUrl;
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
     * Set the amount of video that is buffered when video is loaded or seeked.
     * @param initialBufferingTimeInMs the number of milliseconds to buffer before video starts to play
     * @return the Builder object to continue building
     */
    public Builder setInitialBufferingTime(int initialBufferingTimeInMs) {
      this.initialBufferingTime = initialBufferingTimeInMs;
      return this;
    }
    
    /**
     * Set the maximum amount of video that can be buffered during video playback
     * @param maxBufferingTimeInMs the maximum number of milliseconds the video can buffer
     * @return the Builder object to continue building
     * 
     */
    public Builder setMaxBufferingTime(int maxBufferingTimeInMs) {
      this.maxBufferingTime = maxBufferingTimeInMs;
      return this;
    }
    
    /**
     * Set the amount of video that is buffered when re-buffering is needed during playback
     * @param playbackBufferingTimeInMs the number of milliseconds to buffer before video starts to play
     * @return the Builder object to continue building
     */
    public Builder setPlaybackBufferingTime(int playbackBufferingTimeInMs) {
      this.playbackBufferingTime = playbackBufferingTimeInMs;
      return this;
    }

    /**
     * Generates a fully initialized VisualOnConfiguration
     * @return a VisualOnConfiguration for providing in the Options
     */
    public VisualOnConfiguration build() {
      return new VisualOnConfiguration(this.disableLibraryVersionChecks, this.personalizationServerUrl, this.upperBitrateThreshold, this.lowerBitrateThreshold, this.initialBitrate,  this.maxBufferingTime, this.initialBufferingTime, this.playbackBufferingTime);
    }
  }

  /**
   * Provides the default VisualOn configuration
   * @return the default VisualOn configuration
   */
  public static final VisualOnConfiguration s_getDefaultVisualOnConfiguration() {
    VisualOnConfiguration.Builder visualOnBuilder = new VisualOnConfiguration.Builder();
    return  visualOnBuilder.build();
  }

  /**
   * Initialize a VisualOnConfiguration. Private in favor of the Builder class
   * @param disableLibraryVersionChecks true if you want to allow playback with unexpected VisualOn versions (default false)
   */
  private VisualOnConfiguration(boolean disableLibraryVersionChecks, String personalizationServerUrl, int upperBitrateThreshold, int lowerBitrateThreshold, int initialBitrate, int maxBufferingTime, int initialBufferingTime, int playbackBufferingTime) {
    this.disableLibraryVersionChecks = disableLibraryVersionChecks;
    this.personalizationServerUrl = personalizationServerUrl;
    this.upperBitrateThreshold = upperBitrateThreshold;
    this.lowerBitrateThreshold = lowerBitrateThreshold;
    this.initialBitrate = initialBitrate;
    this.maxBufferingTime = maxBufferingTime;
    this.initialBufferingTime = initialBufferingTime;
    this.playbackBufferingTime = playbackBufferingTime;
  }
  /**
   *
   * @return disableLibraryVersionChecks true if you want to allow playback with unexpected VisualOn versions (default false)
   */
  public boolean getDisableLibraryVersionChecks() {
    return this.disableLibraryVersionChecks;
  }

  /**
   *
   * @return personalizationServerUrl the url to be used for personalization
   */
  public String getPersonalizationServerUrl() {
    return this.personalizationServerUrl;
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
