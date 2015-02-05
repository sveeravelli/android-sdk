package com.ooyala.android;

public class OoyalaException extends Exception {
  private static final long serialVersionUID = 1L;

  public enum OoyalaErrorCode {
    /** Authorization Response invalid */
    ERROR_AUTHORIZATION_INVALID,
    /** Content Tree Response invalid */
    ERROR_CONTENT_TREE_INVALID,
    /** Authorization failed */
    ERROR_AUTHORIZATION_FAILED,
    /** The signature of the Authorization Response is invalid */
    ERROR_AUTHORIZATION_SIGNATURE_INVALID,
    /** Content Tree Next failed */
    ERROR_CONTENT_TREE_NEXT_FAILED,
    /** An Internal Android Error. Check the Throwable properties. */
    ERROR_INTERNAL_ANDROID,
    /** Playback failed */
    ERROR_PLAYBACK_FAILED,
    /** Authorization Heartbeat failed.  Check properties. */
    ERROR_AUTHORIZATION_HEARTBEAT_FAILED,
    /** Metadata fetch failed*/
    ERROR_METADATA_FETCH_FAILED,

    /* Errors from DRM Stream Players */
    /** DRM Personalization/Device Identification failed */
    ERROR_DRM_PERSONALIZATION_FAILED,
    /** DRM File download failed */
    ERROR_DRM_FILE_DOWNLOAD_FAILED,
    /** Device check found invalid auth token */
    ERROR_DEVICE_INVALID_AUTH_TOKEN,
    /** Device binding failed */
    ERROR_DEVICE_BINDING_FAILED,
    /** Device id is too long */
    ERROR_DEVICE_ID_TOO_LONG,
    /** Device limit has been reached */
    ERROR_DEVICE_LIMIT_REACHED,
    /** DRM Rights Acquisition server error */
    ERROR_DRM_RIGHTS_SERVER_ERROR,
    /** General non-Ooyala related DRM failure. stack trace of resulting failure is attached */
    ERROR_DRM_GENERAL_FAILURE,

    /* Advertising Id Errors */
    /** Failed to obtain Advertising Id. */
    ERROR_ADVERTISING_ID_FAILURE,
  };

  private OoyalaErrorCode _code;

  public OoyalaException(OoyalaErrorCode code, String description, Throwable throwable) {
    super(description, throwable);
    _code = code;
  }

  public OoyalaException(OoyalaErrorCode code, String description) {
    super(description);
    _code = code;
  }

  public OoyalaException(OoyalaErrorCode code) {
    super();
    _code = code;
  }

  public OoyalaException(OoyalaErrorCode code, Throwable throwable) {
    super(throwable);
    _code = code;
  }

  public OoyalaErrorCode getCode() {
    return _code;
  }
}
