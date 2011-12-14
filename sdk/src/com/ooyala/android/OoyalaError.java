package com.ooyala.android;

public class OoyalaError
{
  public enum OoyalaErrorCode {
    ERROR_AUTHORIZATION_INVALID, /**< Authorization Response invalid */
    ERROR_CONTENT_TREE_INVALID, /**< Content Tree Response invalid */
    ERROR_AUTHORIZATION_FAILED, /**< Authorization failed */
    ERROR_AUTHORIZATION_SIGNATURE_INVALID, /**< The signature of the Authorization Response is invalid */
    ERROR_CONTENT_TREE_NEXT_FAILED /**< Content Tree Next failed */
  };

	private OoyalaErrorCode _code;
	private String _description = null;
	private Throwable _throwable = null;

	public OoyalaError(OoyalaErrorCode code, String description, Throwable throwable)
	{
	  _code = code;
	  _description = description;
	  _throwable = throwable;
	}

  public OoyalaError(OoyalaErrorCode code, String description)
  {
    this(code, description, null);
  }

	public OoyalaError(OoyalaErrorCode code)
	{
		this(code, null, null);
	}

	public OoyalaError(OoyalaErrorCode code, Throwable throwable)
	{
	  this(code, throwable == null ? null : throwable.getMessage(), throwable);
	}

	public OoyalaErrorCode getCode()
	{
	  return _code;
	}

	public String getDescription()
	{
	  return _description;
	}

	public Throwable getThrowable()
	{
	  return _throwable;
	}

}
