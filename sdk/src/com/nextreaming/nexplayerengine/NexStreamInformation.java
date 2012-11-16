package com.nextreaming.nexplayerengine;

import android.util.Log;

/**
 * \brief This class provides information on a content stream.  
 * 
 * Content streams are listed in the
 *  {@link NexContentInformation#mArrStreamInformation mArrStreamInformation}
 *  member of {@link NexContentInformation}. See there for details.
 *  
 * @author NexStreaming Corporation
 *
 */
public final class NexStreamInformation
{
	/** 
	 * This is a unique integer used to identify the current stream in API calls.
	 * For example, this is used when calling 
	 * \link NexPlayer#setMediaStream(int, int, int, int) setMediaStream\endlink.
	 * 
	 */
	public int 		mID;
	
	/** 
	 * This indicates the stream type (audio or video).
	 * This is one of:
	 *   - <b>(0x00)</b> {@link NexPlayer#MEDIA_STREAM_TYPE_AUDIO MEDIA_STREAM_TYPE_AUDIO}
	 *   - <b>(0x01)</b> {@link NexPlayer#MEDIA_STREAM_TYPE_VIDEO MEDIA_STREAM_TYPE_VIDEO}
	 *   - <b>(0x02)</b> {@link NexPlayer#MEDIA_STREAM_TYPE_TEXT MEDIA_STREAM_TYPE_TEXT}
	 *
	 */
	public int 		mType;
	
	/**
	 * The name of the media stream, for streaming formats that
	 * have named streams.  This is an arbitrary value set by the
	 * author, and is generally intended for user display (to allow
	 * the user to select among different alternative streams).
	 * 
	 */
	public NexID3TagText mName;
	//public String	mName;
	
	/**
	 * \brief  This is the language of the media stream, for streaming formats that
	 *         include language data.  
	 * 
	 * This is an arbitrary value set by the
	 * author, and is intended for user display (to allow users to
	 * select among different alternative streams).  Applications
	 * should NOT rely on this being any particular format; it is
	 * most likely to be the display name of the language, but may
	 * be any string.
	 */
	public NexID3TagText mLanguage;
	//public String	mLanguage;
	
	/**
	 * This indicates the number of custom attributes associated with this stream.
	 */
	public int 		mAttrCount;
	
	/**
	 * This is the number of tracks associated with this stream.  This is the
	 * same as the length of \c mArrTrackInformation, and may be zero.
	 */
	public int 		mTrackCount;

	/**
	 * \brief This is the ID of the track within the stream that is currently
	 * playing, or -1 if no track in this stream is currently playing.
	 *        
	 * This ID matches a value in <code>mArrTrackInformation[].mTrackID</code>.
	 * If the \c mArrTrackInformation array is empty, this value
	 * is undefined.
	 * 
	 */
	public int		mCurrTrackID;
	
	/**
	 * The ID of the custom attribute within this stream that is currently
	 * active, or -1 if no custom attribute in this stream is currently active.
	 * This ID matches a value in \c NexCustomAttribInformation[].mID.
	 * If the \c NexCustomAttribInformation array is empty, this value
	 * is undefined.
	 */
	public int 		mCurrCustomAttrID;
	
	/**
	 * For HLS content, this indicates whether the track is a normal audio video track
	 * or a track of only I-frames.  For an I-frame-only track, this value will be 1, 
	 * and for other kinds of tracks, it will be 0.  Since other kinds of content do not include
	 * any I-frame-only tracks, this value will always be 0 for content other than HLS.
	 * 
	 *  \since version 5.12
	 */
	public int		mIsIframeTrack;

	
	/**
	 * For formats such as HLS that support multiple tracks for
	 * a given stream, this is an array containing information on
	 * each track associated with the stream.  This may be an
	 * empty array for formats that don't have track information.
	 */
	public NexTrackInformation[] mArrTrackInformation;
	
	/**
	 * This is an array of the custom attributes associated with the current
	 * stream, for formats such as Smooth Streaming that support
	 * custom attributes.
	 */
	public NexCustomAttribInformation[] mArrCustomAttribInformation;
	
	/**
	 * This is the sole constructor for NexStreamInformation. The parameters match
	 * the members of the class one-to-one.  Generally, it is not
	 * necessary to call the constructor; rather, objects of this class
	 * are created by NexPlayer&trade;&nbsp;internally and made available through
	 * \link NexContentInformation#mArrStreamInformation mArrStreamInformation\endlink.
	 * 
	 * \param iID			Initial value for \c mID member.
	 * \param iType			Initial value for \c mType member.
	 * \param currCustomAttrId Initial value for \c mCurrCustomAttrId member.
	 * \param currTrackId 	Initial value for \c mCurrTrackId member.
	 * \param isIFrameTrack	Initial value for \c mIsIframeTrack member.
	 * \param name			Initial value for \c mName member.
	 * \param language		Initial value for \c mLanguage member.
	 * 
	 */
	public NexStreamInformation( int iID, int iType, int currCustomAttrId, int currTrackId, int isIFrameTrack, NexID3TagText name, NexID3TagText language)
	{
		mID = iID;
		mType = iType;
		mName = name;
		mLanguage = language;
		mCurrCustomAttrID = currCustomAttrId;
		mCurrTrackID = currTrackId;
		mIsIframeTrack = isIFrameTrack;
	}
	
	/*public void clearStreamInformation()
	{
		mID		= 0;
		mType	= 0;
		mName	= "";
		mLanguage = "";
		
		mAttrCount = 0;
		mTrackCount = 0;
	}*/
	
	@SuppressWarnings("unused")		// Called from native code
	private void copyCustomAttribInformation(NexCustomAttribInformation[] customAttribInformation)
	{
		mArrCustomAttribInformation = customAttribInformation;
		mAttrCount = mArrCustomAttribInformation.length;
	}
	
	@SuppressWarnings("unused")		// Called from native code
	private void copyTrackInformation(NexTrackInformation[] trackInformation)
	{
		mArrTrackInformation = trackInformation;
		mTrackCount = mArrTrackInformation.length;
	}
}
