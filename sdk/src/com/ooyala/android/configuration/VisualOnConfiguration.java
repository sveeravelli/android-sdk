package com.ooyala.android.configuration;


/**
 * VisualOnConfiguration is a bundle that holds application-defined properties that configure
 * Ooyala Player's use of VisualOn and SecurePlayer
 *
 */
public class VisualOnConfiguration {
  public boolean disableLibraryVersionChecks;
  private static boolean DEFAULT_DISABLE_LIBRARY_VERSION_CHECKS = false;

  /**
   * Build the object of VisualOn configurations
   *
   */
  public static class Builder {
    private boolean disableLibraryVersionChecks = false;

    public Builder() {
    }

    /**
     * Generates a fully initialized VisualOnConfiguration
     * @return a VisualOnConfiguration for providing in the Options
     */
    public VisualOnConfiguration build() {
      return new VisualOnConfiguration( disableLibraryVersionChecks );
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
  private VisualOnConfiguration( boolean disableLibraryVersionChecks ) {
    this.disableLibraryVersionChecks = disableLibraryVersionChecks;
  }
}
