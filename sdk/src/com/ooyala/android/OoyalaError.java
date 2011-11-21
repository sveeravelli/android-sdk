package com.ooyala.android;

public class OoyalaError
{
	private int _code;
	private String _description;

	public OoyalaError(int code, String description)
	{
	  _code = code;
	  _description = description;
	}

	public OoyalaError(int code)
	{
		this(code, null);
	}

	public int getCode()
	{
	  return _code;
	}

	public String getDescription()
	{
	  return _description;
	}
}
