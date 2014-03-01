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
    /** DRM-related requests failed */
    ERROR_DRM_FAILED,

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
