package com.nextreaming.nexplayerengine;

/**
 * Stores and provides information about an individual content track, for formats that
 * use multiple tracks (such as HLS).  See {@link NexContentInformation} for details.
 * 
 * @author NexStreaming Corporation
 */
public final class NexTrackInformation
{
	/** 
	 * The ID of the track.  This is an arbitrary value, not an index, but can be matched
	 * to the currently playing track as indicated by \link NexStreamInformation#mCurrTrackID mCurrTrackID\endlink. 
	 */
	public int mTrackID;

	/** 
	 * The Custom Attribute ID of the track.  In some cases, a stream may have multiple
	 * equivalent tracks.  Setting a custom attribute ID in 
	 * \link NexPlayer#setMediaStream() setMediaStream\endlink causes only tracks
	 * with a matching custom attribute ID to be selected.  A custom attribute ID
	 * represents a particular key/value attribute pair.  The full list of available pairs
	 * and their associated ID values can be found in 
	 * {@link NexStreamInformation#mArrCustomAttribInformation mArrCustomAttribInformation}.
	 * 
	 * Please keep in mind that this is an arbitrary value, not an index into the custom
	 * attribute array. 
	 */
	public int mCustomAttribID;
	
	/**
	 *  Bandwidth of the track in bytes per second (Bps). 
	 */
	public int mBandWidth;
	
	/** 
	 * This indicates the type of track:
	 *  - <b>1</b> : Audio Only
	 *  - <b>2</b> : Video Only
	 *  - <b>3</b> : AV
	 * 
	 */
	public int mType;
	
	/**
	 * Indicates if this track is valid (that is, if the codecs, bit rates, and so on are
	 * supported by NexPlayer&trade;).
	 *  - <b>0</b> : Unsupported or invalid track
	 *  - <b>1</b> : Valid and supported track 
	 */
	public int mValid;

    /** Possible value for NexTrackInformation.mReason */ 
	public final static int REASON_TRACK_NOT_SUPPORT_VIDEO_CODEC		= 0x0000001;
    /** Possible value for NexTrackInformation.mReason */ 
	public final static int REASON_TRACK_NOT_SUPPORT_AUDIO_CODEC		= 0x0000002;
    /** Possible value for NexTrackInformation.mReason */ 
	public final static int REASON_TRACK_NOT_SUPPORT_VIDEO_RESOLUTION	= 0x0000003;
    /** Possible value for NexTrackInformation.mReason */ 
	public final static int REASON_TRACK_NOT_SUPPORT_VIDEO_RENDER		= 0x0000004;
	
    /**
     * For invalid tracks, this variable indicates the reason they are not currently valid.
     *
     * This may be any of the following values:
     * - <b>\link NexTrackInformation::REASON_TRACK_NOT_SUPPORT_VIDEO_CODEC REASON_TRACK_NOT_SUPPORT_VIDEO_CODEC\endlink</b>
     *          if the player doesn't support the video codec used for this content.
     * - <b>\link NexTrackInformation::REASON_TRACK_NOT_SUPPORT_AUDIO_CODEC REASON_TRACK_NOT_SUPPORT_AUDIO_CODEC\endlink</b>
     *          if the player doesn't support the audio video codec used for this content.
     * - <b>\link NexTrackInformation::REASON_TRACK_NOT_SUPPORT_VIDEO_RESOLUTION REASON_TRACK_NOT_SUPPORT_VIDEO_RESOLUTION\endlink</b>
     *          if the track is locked out because the video resolution is too high to play, as determined by the settings of the
     *          MAX_HEIGHT and MAX_WIDTH properties.
     * - <b>\link NexTrackInformation::REASON_TRACK_NOT_SUPPORT_VIDEO_RENDER REASON_TRACK_NOT_SUPPORT_VIDEO_RENDER\endlink</b>
     *          if the track was locked out because the video renderer wasn't capable of playing it smoothly (the resolution and/or bit rate too high).
     */
	public int mReason;
	
	/**
	 * \brief The sole initializer for this class.
	 * 
	 * The arguments match the names of the relevant member variables, and
	 * are simply assigned on a 1-to-1 basis.
	 * 
	 * @param iTrackID           Initializes mTrackID.
	 * @param iCustomAttribID    Initializes mCustomAttribID.
	 * @param iBandWidth         Initializes mBandWidth.
	 * @param iType              Initializes mType.
	 * @param iValid             Initializes mValid.
	 * \param iReason            Initializes mReason.
	 * 
	 */
	public NexTrackInformation( int iTrackID, int iCustomAttribID, int iBandWidth, int iType, int iValid, int iReason)
	{
		mTrackID = iTrackID;
		mCustomAttribID = iCustomAttribID;
		mBandWidth = iBandWidth;
		mType = iType;
		mValid = iValid;
		mReason = iReason;
	}
}
