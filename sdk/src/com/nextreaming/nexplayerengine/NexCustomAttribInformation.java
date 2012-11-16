package com.nextreaming.nexplayerengine;

/**
 * This class provides information about an individual content track, for formats that
 * use multiple tracks (such as HLS).  See {@link NexContentInformation} for details.
 * 
 * @author NexStreaming Corporation
 */
public final class NexCustomAttribInformation
{
	/**
	 * This is a unique arbitrary integer ID for this custom attribute. It is used
	 * when calling \link NexPlayer#setMediaStream(int, int, int, int) setMediaStream\endlink; see
	 * there for further details.
	 */
	public int 		mID;
	/**
	 * The name of the custom attribute.
	 */
	public NexID3TagText mName;
	/**
	 * The value of the custom attribute.
	 */
	public NexID3TagText mValue;
	
	/** The name of the attribute */
	//public String	mName;
	
	/** The value of the attribute */
	//public String	mValue;
	
	private NexCustomAttribInformation( int iID, NexID3TagText name, NexID3TagText value)
	{
		mID = iID;
		mName = name;
		mValue = value;
	}
}
