package com.nextreaming.nexplayerengine;

import android.util.Log;

public class NexID3TagText {

	public static final int ENCODING_TYPE_ISO8859_1 	= 0x0;
	public static final int ENCODING_TYPE_UTF16 		= 0x1;
	public static final int	ENCODING_TYPE_UTF16_BE 	= 0x2;
	public static final int	ENCODING_TYPE_UTF8		= 0x3;
	public static final int	ENCODING_TYPE_ASCII		= 0x10;
	public static final int	ENCODING_TYPE_UNICODE	= 0x20;
	public static final int	ENCODING_TYPE_UNKNOWN	= 0xFFFFFFFF;
	
	private int			mEncodingType	= ENCODING_TYPE_ISO8859_1;
	private byte[]		mTextData		= null;
	
	private NexID3TagText(int encodingType, byte[] text)
	{
		if(text == null)
		{
			Log.d("ID3TagText", "ID3TagText text is null!!");
		}

		switch(encodingType)
		{
		case ENCODING_TYPE_ISO8859_1:
		case ENCODING_TYPE_UTF16:
		case ENCODING_TYPE_UTF16_BE:
		case ENCODING_TYPE_UTF8:
		case ENCODING_TYPE_ASCII:
		case ENCODING_TYPE_UNICODE:
			mEncodingType = encodingType;
			break;
		default:
			mEncodingType = ENCODING_TYPE_UNKNOWN;
		}
		mTextData = text;
	}
	
	public int getEncodingType()
	{
		return mEncodingType;
	}
	
	public byte[] getTextData()
	{
		return mTextData;
	}
}
