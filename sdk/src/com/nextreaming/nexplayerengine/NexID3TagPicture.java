package com.nextreaming.nexplayerengine;

import android.util.Log;

public class NexID3TagPicture {

	private byte[] mPictureData;
	private NexID3TagText mMimeType;
	
	private NexID3TagPicture(byte[] pictureData, NexID3TagText mimeType)
	{
		mPictureData = pictureData;
		mMimeType = mimeType;		
	}
	
	public byte[] getPictureData()
	{
		return mPictureData;
	}
	
	public NexID3TagText getMimeType()
	{
		return mMimeType;
	}
}
