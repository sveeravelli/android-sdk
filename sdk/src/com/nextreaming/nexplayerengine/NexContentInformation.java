package com.nextreaming.nexplayerengine;

import android.util.Log;

/**
 * Provides information on content.  This is returned by \link NexPlayer.getContentInfo\endlink. See that
 * method for details.
 * 
 * @author NexStreaming Corporation
 *
 */
public final class NexContentInformation
{
	// --- Video Codecs ---
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_MPEG4V = 0x00000020;
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_H263 = 0x000000C0;
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_H264 = 0x000000C1;
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_WMV = 0x5F574D56;
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_RV = 0x000000DB;
	/** Possible video codec value for \link NexContentInformation.mVideoCodec mVideoCodec\endlink. */
	public static final int NEXOTI_DIVX = 0x000000F1;

	// --- Audio Codecs ---
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_AAC = 0x00000040;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_AAC_GENERIC = 0x00000041;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_AAC_PLUS = 0x00000041;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_MPEG2AAC = 0x00000067;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_MP3inMP4 = 0x0000006B;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_MP3 = 0x0000016B;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_BSAC = 0x00000016;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_WMA = 0x5F574D41;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_RA = 0x000000DA;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_AC3 = 0x00002000;
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_EC3 = 0x00002001;	
	/** Possible audio codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. */
	public static final int NEXOTI_DRA = 0x000000E0;

	// --- Speech Codecs ---
	/** Possible speech codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink.
	 * This is not supported in the current version; do not use it. 
	 * @deprecated Not supported in the current version; do not use. */
	public static final int NEXOTI_AMR = 0x000000D0;
	/** Possible speech codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. 
	 * This is not supported in the current version; do not use it.
	 * @deprecated Not supported in the current version; do not use.  */
	public static final int NEXOTI_EVRC = 0x000000D1;
	/** Possible speech codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. 
	 * This is not supported in the current version; do not use it.
	 * @deprecated Not supported in the current version; do not use.  */
	public static final int NEXOTI_QCELP = 0x000000D2;
	/** Possible speech codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink.
	 * This is not supported in the current version; do not use it. 
	 * @deprecated Not supported in the current version; do not use.  */
	public static final int NEXOTI_QCELP_ALT = 0x000000E1;
	/** Possible speech codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink.
	 * This is not supported in the current version; do not use it. 
	 * @deprecated Not supported in the current version; do not use.  */
	public static final int NEXOTI_SMV = 0x000000D3;
	/** Possible speech codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink.
	 * This is not supported in the current version; do not use it. 
	 * @deprecated Not supported in the current version; do not use.  */
	public static final int NEXOTI_AMRWB = 0x000000D4;
	/** Possible speech codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. 
	 * This is not supported in the current version; do not use it.
	 * @deprecated Not supported in the current version; do not use.  */
	public static final int NEXOTI_G711 = 0x000000DF;
	/** Possible speech codec value for \link NexContentInformation.mAudioCodec mAudioCodec\endlink. 
	 * This is not supported in the current version; do not use it. 
	 * @deprecated Not supported in the current version; do not use.  */
	public static final int NEXOTI_G723 = 0x000000DE;

	// --- Text Codecs ---
	/** Possible text codec for subtitles. This is not supported in the current version; do not use it.
	 * @deprecated Not supported in the current version; do not use.*/
	public static final int NEXOTI_TEXT_3GPP = 0x000000E0;
	/** Possible text codec for subtitles. This is not supported in the current version; do not use it.
	 * @deprecated Not supported in the current version; do not use.*/
	public static final int NEXOTI_TEXT_SKT = 0x000000E2;

	/** Type of media that has been opened.
	 * 
	 * <b>Possible Values:</b>
	 *    - <b>1</b> : Audio Only
	 *    - <b>2</b> : Video Only
	 *    - <b>3</b> : AV
	 *
	 */
	// XXX:  Should this use constants or an enum?
	public int mMediaType;
	
	/** Length of open media in milliseconds. */
	public int mMediaDuration;
	
	/** 
	 * Video codec used by the currently open media. This is one of:
	 *    - \link NexContentInformation#NEXOTI_MPEG4V NEXOTI_MPEG4V\endlink 
	 *    - \link NexContentInformation#NEXOTI_H263 NEXOTI_H263\endlink 
	 *    - \link NexContentInformation#NEXOTI_H264 NEXOTI_H264\endlink 
	 *    - \link NexContentInformation#NEXOTI_WMV NEXOTI_WMV\endlink 
	 *    - \link NexContentInformation#NEXOTI_RV NEXOTI_RV\endlink 
	 *    - \link NexContentInformation#NEXOTI_DIVX NEXOTI_DIVX\endlink 
	 * 
	 */
	public int mVideoCodec;
	
	// XXX:  What are width and height for audio-only 
	//       sources?  Are they zero?  Undefined?
	/** Width of the video, in pixels */
	public int mVideoWidth;
	/** Height of the video, in pixels */
	public int mVideoHeight;
	
	/** 
	 * \brief Frame rate of the video, in frames per second.  This is the frame rate specified in the content.
	 * 
	 * If the device isn't powerful enough to decode and
	 * display the video stream in real-time, the actual number
	 * of displayed frames may be lower than this value.
	 */
	public int mVideoFrameRate;
	
	/** Bit rate of the video, in bits per second */
	public int mVideoBitRate;
	
	/** 
	 * Audio codec used by the currently open media. 
	 * 
	 * This can be one of the following audio codec constants:
	 *    - \link NexContentInformation#NEXOTI_AAC NEXOTI_AAC\endlink
	 *    - \link NexContentInformation#NEXOTI_AAC_GENERIC NEXOTI_AAC_GENERIC\endlink
	 *    - \link NexContentInformation#NEXOTI_AAC_PLUS NEXOTI_AAC_PLUS\endlink
	 *    - \link NexContentInformation#NEXOTI_MPEG2AAC NEXOTI_MPEG2AAC\endlink
	 *    - \link NexContentInformation#NEXOTI_MP3inMP4 NEXOTI_MP3inMP4\endlink
	 *    - \link NexContentInformation#NEXOTI_MP3 NEXOTI_MP3\endlink
	 *    - \link NexContentInformation#NEXOTI_BSAC NEXOTI_BSAC\endlink
	 *    - \link NexContentInformation#NEXOTI_WMA NEXOTI_WMA\endlink
	 *    - \link NexContentInformation#NEXOTI_RA NEXOTI_RA\endlink
	 *    - \link NexContentInformation#NEXOTI_AC3 NEXOTI_AC3\endlink
 	 *    - \link NexContentInformation#NEXOTI_EC3 NEXOTI_EC3\endlink
	 *    - \link NexContentInformation#NEXOTI_DRA NEXOTI_DRA\endlink
	 * 
	 * Or (in future versions) one of the following speech codec constants:
	 *    - \link NexContentInformation#NEXOTI_AMR NEXOTI_AMR\endlink
	 *    - \link NexContentInformation#NEXOTI_EVRC NEXOTI_EVRC\endlink
	 *    - \link NexContentInformation#NEXOTI_QCELP NEXOTI_QCELP\endlink
	 *    - \link NexContentInformation#NEXOTI_QCELP_ALT NEXOTI_QCELP_ALT\endlink
	 *    - \link NexContentInformation#NEXOTI_SMV NEXOTI_SMV\endlink
	 *    - \link NexContentInformation#NEXOTI_AMRWB NEXOTI_AMRWB\endlink
	 *    - \link NexContentInformation#NEXOTI_G711 NEXOTI_G711\endlink
	 *    - \link NEXOTI_G723\endlink
	 * 
	 */
	public int mAudioCodec;
	
	/** Audio sampling rate in samples per second. */
	public int mAudioSamplingRate;
	/** Number of audio channels. */
	public int mAudioNumOfChannel;
	/** Audio bit rate, in bits per second. */
	public int mAudioBitRate;
	
	// XXX: Should the type of mIsSeekable and mIsPausable be changed to Boolean?
	/** If the media supports seeking, this is 1; otherwise it is 0. */
	public int mIsSeekable;
	/** If the media supports pausing, this is 1; otherwise it is 0. */
	public int mIsPausable;
	
	//**
	// * \brief ID of currently playing track, for content that has multiple tracks.
	// * 
	// * The set of values is dependent on the format.  For example, in HLS streaming,
	// * this is the ID associated with the track in the HLS play list.<p>
	// * 
	// * Note that this is <b>not</b> a channel or stream number; the track refers to
	// * a track in the sense that HLS uses it: the same content in an alternate format
	// * or with an alternate resolution or bit rate. <p>
	// * 
	// * Although the values used here are arbitrary and format-dependent, then can be
	//* used to look up the current track in {@link NexContentInformation#mArrTrackInformation}
	// * by searching for an entry where the entry's <code>mTrackID</code> is equal to 
	// * <code>mCurrTrackID</code>.<p>
	// */
	//public int mCurrTrackID;
	
	/* The number of tracks in content.<p>
	 *
	 * Note that this does <b>not</b> refer to channels or streams; these are tracks in
	 * the sense that HLS uses the term: the same content in alternate formats
	 * or with alternate resolutions or bit rates. <p>
	 */
	//public int mTrackNum;
	
	/**
	 * The picture associated with the current content, for formats such
	 * as MP3 and AAC that can have an optional associated still image.
	 * 
	 * This is generally used in place of video for content that does not
	 * have video.  The exact use of the still image is up to the content
	 * producer. In the case of an MP3 or AAC audio file, it is usually
	 * the album cover artwork.  In the case of HTTP Live Streaming,
	 * audio-only tracks often have a still image to be shown in place of
	 * the video.
	 */
	public NexID3TagInformation mID3Tag;
	
	/**
	 * ID of currently selected video stream, for content types with
	 * multiple streams. This matches the ID member of an entry in the
	 * NexContentInformation.mArrStreamInformation() array.
	 */
	public int mCurrVideoStreamID;
	
	/**
	 * ID of currently selected audio stream, for content types with
	 * multiple streams. This matches the ID member of an entry in the
	 * NexContentInformation.mArrStreamInformation() array.
	 */
	public int mCurrAudioStreamID;

	/**
	 * ID of currently selected text stream, for content types with
	 * multiple streams. This matches the ID member of an entry in the
	 * NexContentInformation.mArrStreamInformation() array.
	 */
	public int mCurrTextStreamID;
	
	/**
	 * The number of streams (audio and video) available for the current
	 * content.  This is the same as the length of the 
	 * NexContentInformation.mArrStreamInformation() array.
	 * For formats that don't support multiple streams, this may be zero, or it
	 * may describe a single default stream.
	 */
	public int mStreamNum;
	
	/*public void clearContentInformation()
	{
		mMediaType = 0;
		mMediaDuration = 0;
		mVideoCodec = 0;
		mVideoWidth = 0;
		mVideoHeight = 0;
		mVideoFrameRate = 0;
		mVideoBitRate = 0;
		mAudioCodec = 0;
		mAudioSamplingRate = 0;
		mAudioNumOfChannel = 0;
		mAudioBitRate = 0;
		mIsSeekable = 0;
		mIsPausable = 0;
		
		mCurrVideoStreamID = 0xFFFFFFFF;
		mCurrAudioStreamID = 0xFFFFFFFF;
		mStreamNum = 0;
	}*/
	
	/*
	 * An array describing the tracks that are available for the current content,
	 * for formats such as HLS that support multiple tracks.  These are tracks in
	 * the HLS sense, not channels or streams.  See {@link NexTrackInformation} for
	 * details.
	 */
	//public NexTrackInformation[] mArrTrackInformation;
	
	/*
	 * Sets the value of the <code>mArrTrackInformation</code> member variable.<p>
	 * 
	 * @param trackInformation new value for mArrTrackInformation
	 */
	/*
	public void copyTrackInformation(NexTrackInformation[] trackInformation)
	{
		Log.d("CONTENT INFO", "copyTrackInformation()");
		mArrTrackInformation = trackInformation;
	}
	*/
	
	/**
	 * \brief This is an array describing the streams that are available for the current content.
	 * 
	 * Streams have semantically different content (for example, presented in
	 * different languages or from different camera angles) and are
	 * generally intended to be selected via the user interface.
	 * 
	 * For each stream, there can be multiple tracks.  Generally, tracks contain
	 * content that is equivalent but presented with different trade-offs between
	 * quality and bandwidth.  While streams are selected by the user, tracks are
	 * usually selected automatically based on available bandwidth and system
	 * capabilities, in order to provide the best experience to the user.
	 * 
	 * For formats that do not support multiple streams or multiple tracks, these
	 * arrays may be empty or may contain only a single element.
	 * 
	 */
	public NexStreamInformation[] mArrStreamInformation;
	
	@SuppressWarnings("unused")		// Called from native code
	private void copyStreamInformation(NexStreamInformation[] streamInformation)
	{
		mArrStreamInformation = streamInformation;
	}

	/**
	 * A list of the caption languages available for the current content (for
	 * example, from a subtitle file).  The format of this depends on the
	 * caption file.  This may be the language name spelled out, or it may be
	 * a language identifier such as EN for English or KR for Korean.
	 * 
	 * For SMI files, this is the class name of a given subtitle track, as specified
	 * in the SMI file.
	 * 
	 * There is currently no way to access the additional data associated with a
	 * subtitle track, but it is possible to guess the language (and therefore the encoding)
	 * indirectly from the track's class name.
	 * 
	 * Although the class name is arbitrary,
	 * many files follow the convention "LLCCTT" where LL is the language (EN for English,
	 * KR for Korean and so on), CC is the country (and may be omitted) and TT is the type
	 * (for example "CC" for closed captions).
	 * 
	 * For example, "ENUSCC" would be EN(English), US(United States), CC(Closed Captions).
	 * "KRCC" would be KR(Korean), CC(Closed Captions).
	 * 
	 * Currently, the safest way is to check only the first two letters of the class name
	 * to find the language, and assume the most common encoding for that language.
	 * 
	 */
	public String[] mCaptionLanguages;
	
	@SuppressWarnings("unused")		// Called from native code
	private void copyCaptionLanguages(String[] captionLanguages)
	{
		mCaptionLanguages = captionLanguages;
	}
}
