package com.ooyala.android;

public class OoyalaException extends Exception
{
  private static final long serialVersionUID = 1L;

  public enum OoyalaErrorCode {
    ERROR_AUTHORIZATION_INVALID, /**< Authorization Response invalid */
    ERROR_CONTENT_TREE_INVALID, /**< Content Tree Response invalid */
    ERROR_AUTHORIZATION_FAILED, /**< Authorization failed */
    ERROR_AUTHORIZATION_SIGNATURE_INVALID, /**< The signature of the Authorization Response is invalid */
    ERROR_CONTENT_TREE_NEXT_FAILED, /**< Content Tree Next failed */
    ERROR_INTERNAL_ANDROID /**< An Internal Android Error. Check the Throwable properties. */
  };

	private OoyalaErrorCode _code;

	public OoyalaException(OoyalaErrorCode code, String description, Throwable throwable)
	{
	  super(description, throwable);
	  _code = code;
	}

  public OoyalaException(OoyalaErrorCode code, String description)
  {
    super(description);
    _code = code;
  }

	public OoyalaException(OoyalaErrorCode code)
	{
	  super();
    _code = code;
	}

	public OoyalaException(OoyalaErrorCode code, Throwable throwable)
	{
    super(throwable);
    _code = code;
	}

	public OoyalaErrorCode getCode()
	{
	  return _code;
	}
}
