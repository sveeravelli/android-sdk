package com.nextreaming.nexplayerengine;

import java.io.File;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

/**
 * The primary interface to the NexPlayer&trade;&nbsp;engine.  
 * For details on usage, see the 
 * NexPlayer&trade;&nbsp;Engine \ref legal "package" documentation.
 * 
 * @author NexStreaming Corporation
 */
 //@version 5.12

public final class NexPlayer
{
    // NOTE: Unused constants have been disabled to suppress warnings; these can
    //       be re-enabled later.
	private static final int NEXPLAYER_VERSION_MAJOR 					= 5;
	private static final int NEXPLAYER_VERSION_MINOR					= 12;
	
    // Event Definitions
    private static final int NEXPLAYER_EVENT_NOP                        = 0; // interface test message
    private static final int NEXPLAYER_EVENT_COMMON_BASEID              = 0x00010000;
    private static final int NEXPLAYER_EVENT_ENDOFCONTENT               = ( NEXPLAYER_EVENT_COMMON_BASEID + 1 );
    private static final int NEXPLAYER_EVENT_STARTVIDEOTASK             = ( NEXPLAYER_EVENT_COMMON_BASEID + 2 );
    private static final int NEXPLAYER_EVENT_STARTAUDIOTASK             = ( NEXPLAYER_EVENT_COMMON_BASEID + 3 );
    private static final int NEXPLAYER_EVENT_TIME                       = ( NEXPLAYER_EVENT_COMMON_BASEID + 4 );
    private static final int NEXPLAYER_EVENT_ERROR                      = ( NEXPLAYER_EVENT_COMMON_BASEID + 5 );
    
    // private static final int NEXPLAYER_EVENT_RECORDEND                  = ( NEXPLAYER_EVENT_COMMON_BASEID + 6 );
    private static final int NEXPLAYER_EVENT_STATECHANGED               = ( NEXPLAYER_EVENT_COMMON_BASEID + 7 );
    private static final int NEXPLAYER_EVENT_SIGNALSTATUSCHANGED        = ( NEXPLAYER_EVENT_COMMON_BASEID + 8 );
    private static final int NEXPLAYER_EVENT_DEBUGINFO                  = ( NEXPLAYER_EVENT_COMMON_BASEID + 9 );
    private static final int NEXPLAYER_EVENT_ASYNC_CMD_COMPLETE         = ( NEXPLAYER_EVENT_COMMON_BASEID + 10 );
    private static final int NEXPLAYER_EVENT_RTSP_COMMAND_TIMEOUT       = ( NEXPLAYER_EVENT_COMMON_BASEID + 11 );
    private static final int NEXPLAYER_EVENT_PAUSE_SUPERVISION_TIMEOUT  = ( NEXPLAYER_EVENT_COMMON_BASEID + 12 );
    private static final int NEXPLAYER_EVENT_DATA_INACTIVITY_TIMEOUT    = ( NEXPLAYER_EVENT_COMMON_BASEID + 13 );
    
    // private static final int NEXPLAYER_EVENT_RECORDING_ERROR            = ( NEXPLAYER_EVENT_COMMON_BASEID + 14 );
    // private static final int NEXPLAYER_EVENT_RECORDING                  = ( NEXPLAYER_EVENT_COMMON_BASEID + 15 );
    // private static final int NEXPLAYER_EVENT_TIMESHIFT_ERROR            = ( NEXPLAYER_EVENT_COMMON_BASEID + 16 );
    // private static final int NEXPLAYER_EVENT_TIMESHIFT                  = ( NEXPLAYER_EVENT_COMMON_BASEID + 17 );
    //  private static final int NEXPLAYER_EVENT_REPEAT                     = ( NEXPLAYER_EVENT_COMMON_BASEID + 18 );
    //  private static final int NEXPLAYER_EVENT_DECODER_INIT_COMPLETE      = ( NEXPLAYER_EVENT_COMMON_BASEID + 19 );
    private static final int NEXPLAYER_EVENT_STATUS_REPORT              = ( NEXPLAYER_EVENT_COMMON_BASEID + 20 );
    
    //--- just for streaming events ----
    private static final int NEXPLAYER_EVENT_STREAMING_BASEID           = 0x00030000;
    private static final int NEXPLAYER_EVENT_BUFFERINGBEGIN             = ( NEXPLAYER_EVENT_STREAMING_BASEID + 1 );
    private static final int NEXPLAYER_EVENT_BUFFERINGEND               = ( NEXPLAYER_EVENT_STREAMING_BASEID + 2 );
    private static final int NEXPLAYER_EVENT_BUFFERING                  = ( NEXPLAYER_EVENT_STREAMING_BASEID + 3 );
    
    
    private static final int NEXPLAYER_EVENT_FASTPLAY_BASEID			= 0x00050000;
    private static final int NEXPLAYER_EVENT_FASTPLAY_START				= (NEXPLAYER_EVENT_FASTPLAY_BASEID + 1);
    private static final int NEXPLAYER_EVENT_FASTPLAY_UPDATETIME		= (NEXPLAYER_EVENT_FASTPLAY_BASEID + 2);
    private static final int NEXPLAYER_EVENT_FASTPLAY_END				= (NEXPLAYER_EVENT_FASTPLAY_BASEID + 3);
    
    //--- just for video / audio render events ---
    private static final int NEXPLAYER_EVENT_AUDIO_RENDER_BASEID        = 0x00060000;
    private static final int NEXPLAYER_EVENT_AUDIO_RENDER_CREATE        = ( NEXPLAYER_EVENT_AUDIO_RENDER_BASEID + 1 );
    private static final int NEXPLAYER_EVENT_AUDIO_RENDER_DELETE        = ( NEXPLAYER_EVENT_AUDIO_RENDER_BASEID + 2 );  
    //  private static final int NEXPLAYER_EVENT_AUDIO_RENDER_RENDER        = ( NEXPLAYER_EVENT_AUDIO_RENDER_BASEID + 3 );
    private static final int NEXPLAYER_EVENT_AUDIO_RENDER_PAUSE         = ( NEXPLAYER_EVENT_AUDIO_RENDER_BASEID + 4 );
    private static final int NEXPLAYER_EVENT_AUDIO_RENDER_RESUME        = ( NEXPLAYER_EVENT_AUDIO_RENDER_BASEID + 5 );
    
    private static final int NEXPLAYER_EVENT_VIDEO_RENDER_BASEID        = 0x00070000;
    private static final int NEXPLAYER_EVENT_VIDEO_RENDER_CREATE        = ( NEXPLAYER_EVENT_VIDEO_RENDER_BASEID + 1 );
    private static final int NEXPLAYER_EVENT_VIDEO_RENDER_DELETE        = ( NEXPLAYER_EVENT_VIDEO_RENDER_BASEID + 2 );  
    private static final int NEXPLAYER_EVENT_VIDEO_RENDER_RENDER        = ( NEXPLAYER_EVENT_VIDEO_RENDER_BASEID + 3 );
    private static final int NEXPLAYER_EVENT_VIDEO_RENDER_CAPTURE       = ( NEXPLAYER_EVENT_VIDEO_RENDER_BASEID + 4 );
    
    private static final int NEXPLAYER_EVENT_TEXT_RENDER_BASEID         = 0x00080000;
    private static final int NEXPLAYER_EVENT_TEXT_RENDER_INIT           = ( NEXPLAYER_EVENT_TEXT_RENDER_BASEID + 1 );
    private static final int NEXPLAYER_EVENT_TEXT_RENDER_RENDER         = ( NEXPLAYER_EVENT_TEXT_RENDER_BASEID + 2 );

    private static final int NEXPLAYER_EVENT_TIMEDMETA_RENDER_BASEID 	= 0x00090000;
    private static final int NEXPLAYER_EVENT_TIMEDMETA_RENDER_RENDER 	= ( NEXPLAYER_EVENT_TIMEDMETA_RENDER_BASEID + 1 );

    private static final int NEXDOWNLOADER_EVENT_ERROR                = 0x00100000;
    private static final int NEXDOWNLOADER_EVENT_ASYNC_CMD_BASEID     = 0x00200000;
    private static final int NEXDOWNLOADER_EVENT_COMMON	              	= 0x00300000;
	private static final int NEXDOWNLOADER_EVENT_COMMON_DOWNLOAD_BEGIN 	= 0x00320001;
	private static final int NEXDOWNLOADER_EVENT_COMMON_DOWNLOAD_PROGRESS = 0x00320002;
	private static final int NEXDOWNLOADER_EVENT_COMMON_DOWNLOAD_COMPLETE = 0x00320003;
	private static final int NEXDOWNLOADER_EVENT_COMMON_STATE_CHANGED		= 0x00320004;
	
    
    //--- Signal status ---
    /** Normal signal status; see 
     * {@link IListener#onSignalStatusChanged(NexPlayer, int, int) onSignalStatusChanged}
     * for details. */
    public static final int NEXPLAYER_SIGNAL_STATUS_NORMAL      = 0;
    /** Weak signal status; see 
     * {@link IListener#onSignalStatusChanged(NexPlayer, int, int) onSignalStatusChanged}
     * for details. */
    public static final int NEXPLAYER_SIGNAL_STATUS_WEAK        = 1;
    /** No signal (out of service area); see 
     * {@link IListener#onSignalStatusChanged(NexPlayer, int, int) onSignalStatusChanged}
     * for details. */
    public static final int NEXPLAYER_SIGNAL_STATUS_OUT         = 2;
    
    //--- Async command completion values ---
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_NONE             = 0x00000000;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_OPEN_LOCAL       = 0x00000001;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_OPEN_STREAMING   = 0x00000002;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_OPEN_TV          = 0x00000003; 
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_START_LOCAL      = 0x00000005;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_START_STREAMING  = 0x00000006;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_START_TV         = 0x00000007;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_STOP             = 0x00000008;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_PAUSE            = 0x00000009;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_RESUME           = 0x0000000A;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_SEEK             = 0x0000000B;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_FORWARD          = 0x0000000C;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_BACKWARD         = 0x0000000D;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. */
    public static final int NEXPLAYER_ASYNC_CMD_STEP_SEEK        = 0x0000000E;
    
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. 
     * @deprecated Experimental; may or may not be present in future versions.
     */
    public static final int NEXPLAYER_ASYNC_CMD_RECORD_START     = 0x00000011;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. 
     * @deprecated Experimental; may or may not be present in future versions.
     */
    public static final int NEXPLAYER_ASYNC_CMD_RECORD_STOP      = 0x00000012;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. 
     * @deprecated Experimental; may or may not be present in future versions.
     */
    public static final int NEXPLAYER_ASYNC_CMD_RECORD_PAUSE     = 0x00000013;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. 
     * @deprecated Experimental; may or may not be present in future versions.
     */
    public static final int NEXPLAYER_ASYNC_CMD_RECORD_RESUME    = 0x00000014;
    
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. 
     * @deprecated Experimental; may or may not be present in future versions.
     */
    public static final int NEXPLAYER_ASYNC_CMD_TIMESHIFT_CREATE    = 0x00000021;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. 
     * @deprecated Experimental; may or may not be present in future versions.
     */
    public static final int NEXPLAYER_ASYNC_CMD_TIMESHIFT_DESTROY   = 0x00000022;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. 
     * @deprecated Experimental; may or may not be present in future versions.
     */
    public static final int NEXPLAYER_ASYNC_CMD_TIMESHIFT_PAUSE     = 0x00000023;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. 
     * @deprecated Experimental; may or may not be present in future versions.
     */
    public static final int NEXPLAYER_ASYNC_CMD_TIMESHIFT_RESUME    = 0x00000024;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. 
     * @deprecated Experimental; may or may not be present in future versions.
     */
    public static final int NEXPLAYER_ASYNC_CMD_TIMESHIFT_FORWARD   = 0x00000025;
    /** Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}. 
     * @deprecated Experimental; may or may not be present in future versions.
     */
    public static final int NEXPLAYER_ASYNC_CMD_TIMESHIFT_BACKWARD  = 0x00000026; 
    
    /** Possible value for <code>command</code> parameter of 
     * {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}.
     * \since version 5.12  */
    public static final int NEXPLAYER_ASYNC_CMD_FASTPLAY_START = 0x00000027;
    /** Possible value for <code>command</code> parameter of 
     * {@link IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete}.
     * \since version 5.12  */
    public static final int NEXPLAYER_ASYNC_CMD_FASTPLAY_STOP = 0x00000028;

    /** Possible value for \c msg parameter of 
     * {@link IListener#onDownloaderAsyncCmdComplete(NexPlayer mp, int msg, int param1, int param2) onDownloaderAsyncCmdComplete}. */
    public static final int NEXDOWNLOADER_ASYNC_CMD_OPEN			= 0x00200001;
    /** Possible value for \c msg parameter of 
     * {@link IListener#onDownloaderAsyncCmdComplete(NexPlayer mp, int msg, int param1, int param2) onDownloaderAsyncCmdComplete}. */
    public static final int NEXDOWNLOADER_ASYNC_CMD_CLOSE			= 0x00200002;
    /** Possible value for \c msg parameter of 
     * {@link IListener#onDownloaderAsyncCmdComplete(NexPlayer mp, int msg, int param1, int param2) onDownloaderAsyncCmdComplete}. */
    public static final int NEXDOWNLOADER_ASYNC_CMD_START			= 0x00200003;
    /** Possible value for \c msg parameter of 
     * {@link IListener#onDownloaderAsyncCmdComplete(NexPlayer mp, int msg, int param1, int param2) onDownloaderAsyncCmdComplete}. */
    public static final int NEXDOWNLOADER_ASYNC_CMD_STOP			= 0x00200004;
    
    
    
    /** Treat \c path as a local media file; a possible value for the \c type parameter of \link NexPlayer.open \endlink. */
    public static final int NEXPLAYER_SOURCE_TYPE_LOCAL_NORMAL      = 0;
    /** Treat \c path as a URL to a streaming media source; a possible value for the \c type parameter of \link NexPlayer.open \endlink. */
    public static final int NEXPLAYER_SOURCE_TYPE_STREAMING         = 1;
    
    /** Use TCP as the transport; possible value for the \link NexPlayer.open \endlink method */
    public static final int NEXPLAYER_TRANSPORT_TYPE_TCP            = 0;
    /** Use UDP as the transport; possible value for the \link NexPlayer.open \endlink method */
    public static final int NEXPLAYER_TRANSPORT_TYPE_UDP            = 1;
    
    // --- Return values for getState() ---
    /** No state information available for NexPlayer&trade;&nbsp;(this
     * is the state after {@link NexPlayer#release() release} has
     * been called; a possible \c return value of \link NexPlayer#getState() NexPlayer.getState\endlink.
     */
    public static final int NEXPLAYER_STATE_NONE = 0;
    /** No media source is open (this is the state when
     * the NexPlayer&trade;&nbsp;instance is initially created, and
     * after {@link NexPlayer#close() close} has completed; 
     * a possible \c return value of \link NexPlayer#getState() getState\endlink.
     */
    public static final int NEXPLAYER_STATE_CLOSED = 1;
    /** A media source is open but is currently stopped (this is the state
     * after {@link NexPlayer#open open} or {@link NexPlayer#stop() stop} 
     * has completed; a possible \c return value of \link NexPlayer#getState() getState\endlink.
     */
    public static final int NEXPLAYER_STATE_STOP = 2;
    /** A media source is open and playing (this is the state
     * after {@link NexPlayer#start(int) start}  
     * has completed; a possible \c return value of \link NexPlayer#getState() getState\endlink.
     */
    public static final int NEXPLAYER_STATE_PLAY = 3;
    /** A media source is open but has been paused (this is the state
     * after {@link NexPlayer#pause() pause}  
     * has completed; a possible \c return value of \link NexPlayer#getState() getState\endlink.
     */
    public static final int NEXPLAYER_STATE_PAUSE = 4;
    
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_MEDIA_TYPE = 0;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_MEDIA_DURATION = 1;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_CODEC = 2;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_WIDTH = 3;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_HEIGHT = 4;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_FRAMERATE = 5;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_BITRATE = 6;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_AUDIO_CODEC = 7;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_AUDIO_SAMPLINGRATE = 8;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_AUDIO_NUMOFCHANNEL = 9;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_AUDIO_BITRATE = 10;  
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_MEDIA_ISSEEKABLE = 11;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_MEDIA_ISPAUSABLE = 12;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_RENDER_AVE_FPS = 13;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/
    public static final int CONTENT_INFO_INDEX_VIDEO_AVG_BITRATE = 23;
    /** A possible argument value for {@link NexPlayer#getContentInfoInt(int) getContentInfoInt}*/  
    public static final int CONTENT_INFO_INDEX_AUDIO_AVG_BITRATE = 25;
    
    private final static String TAG = "NEXPLAYER_JAVA";
    
    /** Used for Picture-in-Picture support; not supported by current API version.
     * 
     * This method is deprecated and not supported in the current API version.  Do not use it.
     * @deprecated Not supported in current API version; do not use.
     */
    public int                  mNativeNexPlayerClient = 0; // accessed by native methods
    
    private Surface mSurface; // accessed by native methods
    private SurfaceHolder mSurfaceHolder;
    
    @SuppressWarnings("unused")  // Used in native code

    private AudioTrack mAudioTrack;
    
    
    private static IListener    mListener;
    
    // Tracks whether the NexPlayer engine has been successfully initialized
    private boolean mNexPlayerInit = false;
    
    /**
     * \brief This enum defines the categories for errors.
     * 
     * <B>CAUTION:</B> This is experimental and is subject to change.
     * 
     * Each error code has an associated category.  The intent of this is
     * to group errors based on cause so that a friendlier message can be
     * displayed to the user.  The exact groupings may change in future
     * versions.
     * 
     * @author NexStreaming
     *
     */
    public enum NexErrorCategory {
        /** There is no error */
        NO_ERROR, 
        
        /** Something is wrong with what was passed to the API; indicates a 
         *  bug in the host application. */ 
        API,
        
        /** Something went wrong internally; this could be due to API 
         *  misuse, something wrong with the OS, or a bug. */
        INTERNAL,
        
        /** Some feature of the media is not supported. */
        NOT_SUPPORT,
        
        /** General errors. */
        GENERAL,
        
        /** Errors we can't control relating to the system (for example, 
         *  memory allocation errors). */
        SYSTEM,
        
        /** Something is wrong with the content itself, or it uses a 
         *  feature we don't recognize. */
        CONTENT_ERROR,
        
        /** There was an error communicating with the server or an error 
         *  in the protocol */
        PROTOCOL,
        
        /** A network error was detected. */
        NETWORK,
        
        /** An error code base value (these shouldn't be used, so this 
         *  should be treated as an internal error). */
        BASE,
        
        /** Authentication error; not authorized to view this content, 
         *  or a DRM error while determining authorization. */
        AUTH,
        
        /**
         * An error was generated by the Downloader module.
         */
        DOWNLOADER;
    }
    
    /**
     * \brief This enumerator defines the possible properties that can be set on a NexPlayer&trade;&nbsp;instance.
     * 
     * 
     * To set a property, call \link NexPlayer#setProperty(NexProperty, int) setProperty\endlink on
     * the NexPlayer&trade;&nbsp;instance.  To get the current value of a property, call
     *  \link NexPlayer#getProperty(NexProperty) getProperty\endlink.
     *  
     * <h2>Property Fine-Tuning Guidelines</h2>
     * The default values for the properties should be acceptable for most common cases.
     * However, in some cases, adjusting the properties will give better performance or
     * better behavior.
     * 
     * <h3>Fine-Tuning Buffering Time</h3>
     * When dealing with streaming content, adjusting the buffer size can give smoother
     * playback.  For RTSP streaming, the recommended buffering time is between 3 and 5
     * seconds; for HTTP Live Streaming, the recommended buffering time is 8 seconds.
     * 
     * There are two settings for buffering time:  the initial time (the first time data is 
     * buffered before playback starts) and the re-buffering time (if buffering is needed
     * later, after playback has started).  Both default to 5 seconds.  For example, to
     * set the buffering time to 8 seconds for HTTP Live Streaming:
     * 
     * \code
     * void setBufferingTime( NexPlayer hNexPlayer ) {
     *     hNexPlayer.setProperty( 
     *         NexProperty.INITIAL_BUFFERING_DURATION,
     *         8000);
     *     hNexPlayer.setProperty( 
     *         NexProperty.RE_BUFFERING_DURATION,
     *         8000);
     * }
     * \endcode
     *  
     * <h2>Numeric Property Identifiers</h2>
     * Properties can also be identified by numeric value.  This is how NexPlayer&trade;&nbsp;identifies
     * properties internally, but in general, it is better to use this \c enum and the methods
     * listed above instead.
     * 
     * If you must work with the numeric property identifiers directly,
     * you can retrieve them using the {@link com.nextreaming.nexplayerengine.NexProperty#getPropertyCode() getPropertyCode}
     * method of a member of this enum, and the methods {@link com.nextreaming.nexplayerengine.NexPlayer#getProperties(int) getProperties(int)} and 
     * {@link com.nextreaming.nexplayerengine.NexPlayer#setProperties(int, int) setProperties(int, int)} can be used to get or set a property based
     * on the numeric identifier.
     * 
     *
     */
    public enum NexProperty {
        
        /**
         * The number of milliseconds of media to buffer initially before 
         * beginning streaming playback (HLS, RTSP, etc.).
         * 
         * This is the initial amount of audio and video that NexPlayer&trade;&nbsp;buffers
         * when it begins playback.  If further buffering is required later in
         * the playback process, the value of the property 
         * {@link NexPlayer.NexProperty#RE_BUFFERING_DURATION RE_BUFFERING_DURATION} 
         * will be used instead.
         * 
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> 5000 (5 seconds) \n
         */
        INITIAL_BUFFERING_DURATION              (9),
        /**
         * The number of milliseconds of media to buffer if additional buffering
         * is required during streaming playback (HLS, RTSP, etc).
         * 
         * This is the amount of audio and video that NexPlayer&trade;&nbsp;buffers
         * when the buffer becomes empty during playback (requiring additional
         * buffering).  For the initial buffering, the value of the property 
         * {@link NexPlayer.NexProperty#INITIAL_BUFFERING_DURATION INITIAL_BUFFERING_DURATION} 
         * is used instead.
         * 
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> 5000 (5 seconds) \n
         */
        RE_BUFFERING_DURATION                   (10),
        /**
         * The number of milliseconds (as a negative number) that video is allowed 
         * to run ahead of audio before the system waits for audio to catch up.
         * 
         * For example, -20 means that if the current video time is more than 20msec 
         * ahead of the audio time, the current video frame will not be displayed until
         * the audio catches up to the same time stamp.  This is used to adjust video 
         * and audio synchronization.
         * 
         * <b>Type:</b> integer <i>(should be negative)</i> \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> -20 (20msec) \n
         */
        TIMESTAMP_DIFFERENCE_VDISP_WAIT         (13),
        /**
         * The number of milliseconds that video is allowed to run behind audio 
         * before the system begins skipping frames to maintain synchronization.
         * 
         * For example, 200 means that if the current video time is more than 200msec 
         * behind the audio time, the current video frame will be skipped.
         * This is used to adjust video and audio synchronization.
         * 
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> 200 (0.2 sec) \n
         */
        TIMESTAMP_DIFFERENCE_VDISP_SKIP         (14),
        /**
         * The amount of time to wait for a server response before
         * generating an error event.
         * 
         * If there is no response from the server for longer than
         * the amount of time specified here, an error event will be
         * generated and playback will stop.
         * 
         * Set this to zero to disable timeout (NexPlayer&trade;&nbsp;will wait
         * indefinitely for a response).
         * 
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> 60,000 (60 seconds) \n
         */
        DATA_INACTIVITY_TIMEOUT                 (19),
        /**
         * The amount of time to wait before timing out when establishing
         * a connection to the server.
         * 
         * If the connection to the server (the socket connection) cannot
         * be established within the specified time, an error event will
         * be generated and playback will not start.
         * 
         * Set this to zero to disable timeout (NexPlayer&trade;&nbsp;will wait
         * indefinitely for a connection).
         * 
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> 10,000 (10 seconds) \n
         */
        SOCKET_CONNECTION_TIMEOUT               (20),
        /**
         * The minimum possible port number for the RTP port that is created
         * when performing RTSP streaming over UDP. \n
         * 
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> 12000 \n
         */
        RTP_PORT_MIN                            (22),
        /**
         * The maximum possible port number for the RTP port that is created
         * when performing RTSP streaming over UDP. \n
         * 
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> 30000 \n
         */
        RTP_PORT_MAX                            (23),
        /**
         * Prevents the audio track from playing back when set to TRUE (1). \n
         * 
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         */
        NOTOPEN_PLAYAUDIO                       (27),
        /**
         * Prevents the video track from playing back when set to TRUE (1). \n
         * 
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         */
        NOTOPEN_PLAYVIDEO                       (28),
        /**
         * Prevents the text (subtitle) track from playing back when set to TRUE (1). \n
         * 
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         */
        NOTOPEN_PLAYTEXT                        (29),
        /**
         * The logging level for the NexPlayer&trade;&nbsp;protocol module.
         * 
         * This affects the type of messages that are logged by the
         * protocol module (it does not affect the logging level of
         * other NexPlayer&trade;&nbsp;components).
         * 
         * This value is made by or-ing together zero or more of the
         * following values:
         *   - <b>LOG_LEVEL_NONE (0x00000000)</b>  Don't log anything <i>(not currently supported)</i>
         *   - <b>LOG_LEVEL_DEBUG (0x00000001)</b>  Log start, stop and errors (default for the debug version)
         *   - <b>LOG_LEVEL_RTP (0x00000002)</b>  Generate log entries relating to RTP packets
         *   - <b>LOG_LEVEL_RTCP (0x00000004)</b>  Generate log entries relating to RTCP packets
         *   - <b>LOG_LEVEL_FRAME (0x00000008)</b>  Log information about the frame buffer
         *   - <b>LOG_LEVEL_ALL (0x0000FFFF)</b>  Log everything <i>(not currently supported)</i>
         * 
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> LOG_LEVEL_DEBUG \n
         */
        LOG_LEVEL                               (35),
        /**
         * Controls when video initialization happens.
         * 
         * This can be any of the following values:
         * 
         * <ul>
         * <li><b>AV_INIT_PARTIAL (0x00000000)</b><br />
         *  If there is an audio track, wait for audio initialization to complete
         *  before initializing video. 
         * <li><b>AV_INIT_ALL (0x00000001)</b><br />
         *  Begin video initialization as soon as there is any video data, without
         *  any relation to the audio track status.
         * </ul>
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> AV_INIT_PARTIAL \n
         */
        AV_INIT_OPTION                          (46),
        /**
         * If set to 1, allows media playback even if the audio codec is not supported.
         * 
         * The default behavior (if this is 0) is to return an error or generate an
         * error event if the audio codec is not supported.
         * 
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> 0 \n
         */
        PLAYABLE_FOR_NOT_SUPPORT_AUDIO_CODEC    (48),
        /**
         * If set to 1, allows media playback even if the video codec is not supported.
         * 
         * The default behavior (if this is 0) is to return an error or generate an
         * error event if the video codec is not supported.
         * 
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> 0 \n
         */
        PLAYABLE_FOR_NOT_SUPPORT_VIDEO_CODEC    (49),
        /**
	     * Smooths playback when skipping frames.
	     * 
	     * Smooths out playback in cases where frames would be skipped due to the device 
	     * performance, network issues or similar reasons.
	     * This is enabled by default.
	     * However, it is normally not necessary to change this property.
	     * 
	     * If it is 1, use Eyepleaser
	     * If it is 0, do not use eyepleaser
	     */ 
	USE_EYEPLEASER	(50),
        /**
         * Live HLS playback option.<p>
         * 
         * This must be one of the following values:
         *    - <b>LIVE_VIEW_RECENT (0x00000000)</b>
         * Start playback from the most recent part in the HLS live playlist.  Except in
         * special cases, this is the value that should be used, as this provides the lowest
         * latency between streaming and playback.
         *    - <b>LIVE_VIEW_FIRST (0x00000001)</b>
         * Unconditionally start HLS playback from the first entry in the HLS playlist.
         * 
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> LIVE_VIEW_RECENT \n
         */
        LIVE_VIEW_OPTION                    (53),
        
        /**
         * RTSP/HTTP User Agent value.
         * 
         * <b>Type:</b> String \n
         * <b>Default:</b> &ldquo; User-Agent: Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_1_2 like Mac OS X; ko-kr) 
         *                      AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7D11 Safari/528.16 &rdquo;
         */
        USERAGENT_STRING                    (58),
        
        /**
         * Controls what is displayed while the player is waiting for audio data.
         * 
         * If this is set to 1 (the default), the first video frame is displayed as soon
         * as it has been decoded, and the player waits in a "freeze-frame" state until
         * the audio starts, at which point both the audio and video play together.
         * 
         * If this is set to 0, the player will not display the first video frame until
         * the audio is ready to play.  Whatever was previously displayed will continue
         * to be visible (typically a black frame).
         * 
         * Once audio has started, the behavior for both settings is the same; this only
         * affects what is displayed while the player is waiting for audio data.
         * 
         * Under old versions of the SDK (prior to the addition of this property) the
         * default behavior was as though this property were set to zero.
         * 
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 1 \n
         */
        FIRST_DISPLAY_VIDEOFRAME                (60),
        
        /**
         * If set to true, unconditionally skips all B-frames without decoding them. \n
         * 
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         */
        SET_TO_SKIP_BFRAME                      (103),
        /**
         * Maximum amount of silence to insert to make up for lost audio frames.
         * 
         * Under normal operation, if audio frames are lost (if there is a time gap
         * in received audio frames), silence will be inserted automatically to make
         * up the gap.
         * 
         * However, if the number of audio frames lost represents a span of time
         * larger than the value set for this property, it is assumed that there is
         * a network problem or some other abnormal condition and silence is not
         * inserted.
         * 
         * This prevents, for example, a corruption in the time stamp in an audio
         * frame from causing the system to insert an exceptionally long period of
         * silence (which could possibly prevent further audio playback or cause
         * other unusual behavior).
         * 
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Default:</b> 5000 (5 seconds) \n
         */
        TOO_MUCH_LOSTFRAME_DURATION             (105),
        /**
         * If set to 1, enables local file playback support. \n
         * 
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 1 \n
         */
        SUPPORT_LOCAL                           (110),
        /**
         * If set to 1, enables RTSP streaming support. \n
         * 
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 1 \n
         */
        SUPPORT_RTSP                            (111),
        /**
         * If set to 1, enables progressive download support. \n
         * 
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 1 \n
         */
        SUPPORT_PD                              (112),
        /**
         * If set to 1, enables Microsoft Windows Media Streaming support. \n
         * 
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         */
        SUPPORT_WMS                             (113),
        /**
         * If set to 1, enables Real Media Streaming support. \n
         * 
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         */
        SUPPORT_RDT                             (114),
        /**
         * If set to 1, enables Apple HTTP Live Streaming (HLS) support. \n
         * 
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 1 \n
         */
        SUPPORT_APPLE_HTTP                      (115),
        /**
         * If set to 1, enables HLS Adaptive Bit Rate (ABR) support. \n
         * 
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 1 \n
         */
        SUPPORT_ABR                             (116),
        /**
         * When using HLS ABR, this is the maximum allowable bandwidth.  Any track
         * with a bandwidth greater than this value will not be played back.
         * 
         * This should be set to zero for no maximum.
         * 
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> bps (bits per second) \n
         * <b>Default:</b> 0 (no maximum) \n
         */
        MAX_BW                                  (117),
        /**
         * Limits the H.264 profile that can be selected from an HLS playlist.
         * 
         * Under normal operation, the track with the highest supported H.264 profile
         * is selected from an HLS playlist.  If this property is set, no track with
         * a profile higher than this value will be selected.
         * 
         * This should be set to zero for no limit.
         * 
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> 0 (use any profile) \n
         */
        MAX_H264_PROFILE                        (118),
        /**
         * If set to 1, lost audio frames are always ignored (silence is never inserted).
         * 
         * See 
         * {@link NexPlayer.NexProperty#TOO_MUCH_LOSTFRAME_DURATION TOO_MUCH_LOSTFRAME_DURATION}
         * for details about the insertion of silence for lost audio frames.
         * 
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         */
        IGNORE_AUDIO_LOST_FRAME                 (119),
        /**
         * This is used to force NexPlayer&trade;&nbsp;to begin buffering as soon as all
         * availale audio frames have been processed, without regard to the state
         * of the video buffer.
         * 
         * Under normal operation, when there are no audio frames left in the audio
         * buffer, NexPlayer&trade;&nbsp;switches to buffering mode and temporarily suspends
         * playback.
         * 
         * There is an exception if the video buffer is more than 60% full.  In this
         * case, NexPlayer&trade;&nbsp;will continue video playback even if there is no more
         * audio available.
         * 
         * Setting this property to \c true (1) bypasses this exception
         * and forces the system to go to buffering immediately if there are no audio
         * frames left to play.
         * 
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         */
        ALWAYS_BUFFERING                        (120),
        /**
         * When \c true (1), this property causes audio/video synchronization to be bypassed.
         * 
         * In this state, audio and video are played back independently as soon as data is
         * received.
         * 
         * This property can be enabled if audio and video synchronization are not important,
         * and if real-time behavior is needed between the server and the client.
         * 
         * In normal cases, this should <i>not</i> be used (it should be set to zero) because it will
         * cause video and audio to quickly lose synchronization for most normal media streams.
         * 
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         */
        IGNORE_AV_SYNC                          (121),
        
        /**
         * If set to 1, this enables MS Smooth Streaming support. \n
         * 
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 1 \n
         */
        SUPPORT_MS_SMOOTH_STREAMING                     (123),
        
        /**
         * Adjusts A/V synchronization by ofsetting video relative to audio.
         * 
         * Positive values cause the video to play faster than the audio, while
         * negative values cause the audio to play faster than the video.  Under normal
         * operation, this can be set to zero, but in some cases where the synchronization
         * is bad in the original content, this can be used to correct for the error.
         * 
         * While A/V synchronization is generally optimized internally by NexPlayer&trade;&nbsp;, there may
         * occasionally be devices which need to be offset in order to improve
         * overall A/V synchronization.  For examples of how to set AV_SET_OFFSET based on the 
         * device in use, please see the Sample Application code as well as the
         * introductory section \ref avSync "A/V Synchronization" section.
         * 
         * Appropriate values for any other problematic devices need to be determined experimentally 
         * by testing manually.
         * 
         * <b>Type:</b> integer \n 
         * <b>Unit:</b> msec (1/1000 sec) \n
         * <b>Range:</b> -2000 ~ +2000 \n
         * <b>Default:</b> 0 \n
         * 
         */
        AV_SYNC_OFFSET                          (124),          // JDKIM 2010/12/09
        
        /**
         * Limits the maximum width (in pixels) of the video tracks that can be
         * selected during streaming play.
         *
         * This is used to prevent NexPlayer&trade;&nbsp;from attempting to play
         * tracks that are encoded at too high a resolution for the device to
         * handle effectively.  NexPlayer&trade;&nbsp;will instead select a track
         * with a lower resolution.
         *
         * <b>Type:</b> integer \n 
         * <b>Unit:</b> pixels \n
         * <b>Default:</b> 720 \n
         */
        MAX_WIDTH                               (125),
        
        /**
         * Limits the maximum height (in pixels) of the video tracks that can be
         * selected during streaming play.
         *
         * This is used to prevent NexPlayer&trade;&nbsp;from attempting to play
         * tracks that are encoded at too high a resolution for the device to
         * handle effectively.  NexPlayer&trade;&nbsp;will instead select a track
         * with a lower resolution.
         *
         * <b>Type:</b> integer \n 
         * <b>Unit:</b> pixels \n
         * <b>Default:</b> 480 \n
         */
        MAX_HEIGHT                              (126),

        
        /**
         * This property sets the preferred bandwidth when switching tracks during streaming play.
         *
         * Under normal operation (when this property is zero), if the available
         * network bandwidth drops below the minimum needed to play the current 
         * track without buffering, the player will immediately switch to a lower 
         * bandwidth track, if one is available, to minimize any time spent buffering.
         *
         * If this property is set, the player will attempt to choose only tracks 
         * above the specified bandwidth, even if that causes some buffering.  
         * However, if the buffering becomes too severe or lasts for an extended 
         * time, the player may eventually switch to a lower-bandwidth track anyway.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> kbps (kilobits per second) \n
         * <b>Default:</b> 0 \n
         * 
         * \see {@link NexPlayer.NexProperty.PREFER_AV PREFER_AV}
         */
        PREFER_BANDWIDTH                        (129),
        
        /**
         * Controls whether NexPlayer&trade;&nbsp;prefers tracks with both
         * audio and video content.
         *   
         * Under normal operation (when this property is set to 0), if the available
         * network bandwidth drops below the minimum needed to play the current 
         * track without buffering, the player will immediately switch to a lower 
         * bandwidth track, if one is available, to minimize any time spent buffering.
         *
         * If this property is set to 1, the player will attempt to choose only tracks 
         * that include both audio and video content, even if that causes some buffering.  
         * However, if the buffering becomes too severe or lasts for an extended 
         * time, the player may eventually switch to an audio-only track anyway.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> 0 \n
         * <b>Values:</b> 
         *      - 0: Normal behavior (immediate switching)
         *      - 1: Prefer tracks with both audio and video.
         * 
         * @see {@link NexPlayer.NexProperty#PREFER_BANDWIDTH PREFER_BANDWIDTH}
         */
        PREFER_AV                               (130),
        
        /**
         * Allows NexPlayer&trade;&nbsp;to switch to a lower bandwidth track if the
         * resolution or bitrate of the current track is too high for the
         * device to play smoothly.
         * 
         * Under normal operation, NexPlayer&trade;&nbsp;switches tracks based solely on
         * current network conditions.  When this property is enabled, NexPlayer&trade;&nbsp;
         * will also switch to a lower bandwith track if too many frames are skipped
         * during playback.
         * 
         * This is useful for content that is targeted for a variety of
         * devices, some of which may not be powerful enough to handle the higher
         * quality streams.
         * 
         * The {@link NexProperty#TRACKDOWN_VIDEO_RATIO TRACKDOWN_VIDEO_RATIO} property
         * controls the threshold at which the track change will occur, if frames
         * are being skipped.
         * 
         * <b>Type:</b> boolean \n
         * <b>Default:</b> 0 \n
         * <b>Values:</b>
         *     - 0: Normal behavior (switch based on network conditions only)
         *     - 1: Switch based on network conditions and device performance.
         * 
         */
        ENABLE_TRACKDOWN                        (131),
        
        /**
         * This property controls the ratio of skipped frames that will be tolerated before
         * a track change is forced, if ENABLE_TRACKDOWN
         * is enabled.
         * 
         * The formula used to determine if a track switch is necessary is:
         * \code 100 * (RenderedFrames / DecodedFrames) < TRACKDOWN_VIDEO_RATIO \endcode
         * 
         * In other words, if this property is set to 70, and ENABLE_TRACKDOWN
         * is set to 1, NexPlayer&trade;&nbsp;will require that at least 70% of the decoded frames
         * be displayed.  If less than 70% can be displayed (greater than 30% skipped frames),
         * then the next lower bandwidth track will be selected.
         * 
         * A performance-based track switch <b>permanently</b> limits the maximum bandwidth of
         * tracks that are eligible for playback until the content is closed.  For this reason, setting this 
         * ratio higher than the default value of 70 is strongly discouraged.  
         * (This differs from the bandwidth-based algorithm, which continuously adapts to current
         * network bandwidth).
         * 
         * <b>Type:</b> integer \n
         * <b>Range:</b> 0 to 100 \n
         * <b>Default:</b> 70 \n
         */
        TRACKDOWN_VIDEO_RATIO                   (132),
        
        /**
         * Controls the algorithm used for bitrate switching when playing an HLS stream. \n
         *
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b> 0 \n
         * <b>Values:</b>
         *     - <b>0:</b> Use a more aggressive algorithm: up-switching happens sooner.
         *     - <b>1:</b> Use a more conservative algorithm: up-switching happens only if a
         *          significant amount of extra bandwidth is available beyond that
         *          required to support the given bitrate.  This is similar to
         *          the iPhone algorithm.
         *
         */
        HLS_RUNMODE                             (133),

        /**
         * Additional HTTP headers to use to supply credentials when a 401 response
         * is received from the server.
         *
         * The string should be in the form of zero or more HTTP headers (header
         * name and value), and each header (including the last) should be terminated
         * with a CRLF sequence, for example:
         * \code
         * "id: test1\r\npw: 12345\r\n"
         * \endcode
         * The particulars of the headers depend on the server and the authentication
         * method being used.
         *
         * <b>Type:</b> String 
         */
        HTTP_CREDENTIAL                             (134),

        /**
         * Controls whether the player honors cookies sent by the server. \n
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Default:</b>1 \n
         * <b>Values:</b>
         *    - <b>0:</b> Ignore HTTP cookie headers sent by the server.
         *    - <b>1:</b> Cookie headers received from a streaming server along
         *                with the initial manifest or playlist are included
         *                with further HTTP requests during the session.
         * 
         */
        SET_COOKIE                                  (165),

        /**
         * For internal use only.  This should be otherwise ignored.
         */

        SET_DURATION_OF_UPDATE_CONTENT_INFO     (501),

        /**
         * Sets the type of caption display for CEA 608 captions.  
         * 
         * Because CEA 608 closed captions include multiple text attributes and additional display modes,
         * this property allows these captions to be displayed more simply in Basic form (where the CEA 608 captions 
         * are essentially treated in the same way as other forms of subtitles) or allows
         * the player to fully support all attributes and display modes available with CEA 608 specifications.
         * 
         * Note that if CEA 608 closed captions are included in the content, the BASIC mode may not always 
         * easily allow the player to display the captions in such a way that they are easily read, especially 
         * with live content that may be include backspacing, or other individual character specific attributes.
         * To fully support CEA 608 closed captions to specification, set this property to 1.
         * 
         * <b>Type:</b> unsigned integer \n 
         * <b>Default:</b> 0 \n
         * <b>Values:</b>
         *     - 0 : BASIC:  Captions displayed one row at a time.  
         *     - 1 : FULL:  Each character in the Closed Captions added individually.  This type of 
         *       caption display supports all CEA 608 text attributes and display specifications.
         * */
        SET_CEA608_TYPE				 			(502),


        /**
         * Sets the SmoothStreaming \c LiveBackOff property when playing Smooth Streaming content. 
         * 
         * This property sets the duration of content (closest to live) that cannot yet be accessed or downloaded
         * by the player.  It is like setting how long to wait before asking for the latest fragment in a live 
         * presentation, and thus basically sets the played "live" point back from the actual
         * live content available on the server.
         * 
         * The end-to-end latency of the player (what is being played "live" in the player compared to what is
         * available live on the server) is at least the duration of \c LiveBackOff added to the duration
         * set for \c LivePlaybackOffset with SET_LIVEPLAYBACKOFFSET.
         * 
         * <b>Type:</b> unsigned int \n 
         * <b>Units:</b> milliseconds (ms) \n
         * <b>Default:</b> 6000 (ms) \n
         * 
         * \since version 5.9
         * 
         */
       SET_LIVEBACKOFF								(504),
       /**
        * Sets the SmoothStreaming \c LivePlaybackOffset property when playing Smooth Streaming content.
        * 
        * This property sets the duration away from the live position to start playback when joining a 
        * live presentation when the LiveView option is set to "Recent", but excludes the \c LiveBackOff 
        * duration (set by SET_LIVEBACKOFF).
        * 
        * As a result, live content will be played behind the actual live position by a duration
        * determined by BOTH \c LiveBackOff and the value for \c LivePlaybackOffset set here.
        * 
        * Setting this property enables faster startup because it allows a buffer to be built up
        * as fast as bandwidth will support (potentially faster than real time), which creates a buffer
        * against network jitter.  It does however also increase end-to-end latency, which means what 
        * is played "live" in the player is farther behind the actual live playing point of the
        * content.
        * 
        * <b>Type:</b> unsigned int \n  
        * <b>Units:</b> milliseconds (ms) \n
        * <b>Default:</b> 7000 (ms) \n
        * 
        * \since version 5.9
        */
       SET_LIVEPLAYBACKOFFSET							(505),
       /**
	    * Starts video together with or seperately from audio.
        *
	    * This property starts to play audio and video together when starting, if video timestamp
        * is slower than audio's timestamp.
        * 
	    * If it is 1, it forces the video and audio to start at the same time.
	    * If it is 0, it forces the video and audio to start separately.
        */
	START_WITH_AV	(506),
       /**
        *  Ignores abnormal segment timestamp.  
        * 
        * If it is 1 or  enable, ignore the  abnormal segment timestamp. 
        * If it is 0 or disable, don't ignore the abnormal segment timestamp.
	*/
	IGNORE_ABNORMAL_SEGMENT_TIMESTAMP(508),
	
        /**
         * Indicates whether speed control is available on this device.
         *
         * This is useful to determine whether to display the speed control
         * in the user interface.
         *
         * <b>Type:</b> unsigned integer <i>(read-only)</i> \n
         * <b>Values:</b>
         *    - <b>0:</b> Device does not support speed control.
         *    - <b>1:</b> Device supports speed control.
         * 
         */
        SPEED_CONTROL_AVAILABILITY                  (0x00050001),

        /**
         * Indicates whether the NexSound audio solution component, EarComfort, is available on 
         * this device.
         * 
         * <b>Type:</b> unsigned integer <i>(read-only)</i> \n
         * <b>Values:</b>
         *    - <b>0:</b> Device does not support EarComfort component.
         *    - <b>1:</b> Device supports EarComfort component. 
         */
        AS_EARCOMFORT_AVAILABILITY                  	(0x00050002),
        
        /**
         * Indicates whether the NexSound audio solution component, Reverb, is available on 
         * this device.
         * 
         * <b>Type:</b> unsigned integer <i>(read-only)</i> \n
         * <b>Values:</b>
         *    - <b>0:</b> Device does not support Reverb component.
         *    - <b>1:</b> Device supports Reverb component. 
         */
        AS_REVERB_AVAILABILITY                      	(0x00050003),
        
        /**
         * Indicates whether the NexSound audio solution component, Stereo Chorus, is available on 
         * this device.
         * 
         * <b>Type:</b> unsigned integer <i>(read-only)</i> \n
         * <b>Values:</b>
         *    - <b>0:</b> Device does not support Stereo Chorus component.
         *    - <b>1:</b> Device supports Stereo Chorus component. 
         */        
        AS_STEREO_CHORUS_AVAILABILITY               	(0x00050004),
        
        /**
         * Indicates whether the NexSound audio solution component, Music Enhancer, is available on 
         * this device.
         * 
         * <b>Type:</b> unsigned integer <i>(read-only)</i> \n
         * <b>Values:</b>
         *    - <b>0:</b> Device does not support Music Enhancer component.
         *    - <b>1:</b> Device supports Music Enhancer component. 
         */
        AS_MUSIC_ENHANCER_AVAILABILITY              	(0x00050005),
        
        /**
         * Indicates whether the NexSound audio solution component, Cinema Sound, is available on 
         * this device.
         * 
         * <b>Type:</b> unsigned integer <i>(read-only)</i> \n
         * <b>Values:</b>
         *    - <b>0:</b> Device does not support Cinema Sound component.
         *    - <b>1:</b> Device supports Cinema Sound component. 
         */
        AS_CINEMA_SOUND_AVAILABILITY                	(0x00050006),


        /**
         * Controls the maximum number of pages the player can allocate for
         * the remote file cache.
         *
         * The remote file cache stores data that has been read from disk or
         * received over the network (this includes local, streaming and
         * progressive content).
         *
         * In general, this value should not be changed, as an incorrect
         * setting can adversely affect performance, particularly when seeking.
         *
         * In order to play multiplexed content, at least one audio chunk and
         * one video chunk must fit inside a single RFC buffer page.  For certain formats
         * (PIFF, for example) at very high bitrates, the chunks may be too big
         * to fit in a single page, in which case the RFC buffer page size will need
         * to be increased.  If the system has limited memory resources, it may be
         * necessary to decrease the buffer count when increasing the page size.
         *
         * Increasing the page size can increase seek times, especially for data
         * received over the network (progressive download and streaming cases), so
         * this value should not be changed unless there are issues playing
         * specific content that cannot be solved in another way.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> number of buffers \n
         * <b>Default:</b> 20 \n
         */
        RFC_BUFFER_COUNT                            (0x00070001),
        
        /**
         * Controls the size of each page in the remote file cache.
         *
         * Use caution when adjusting this value.  Improper settings may
         * adversely affect performance, or may cause some content to
         * fail to play.
         *
         * \see RFC_BUFFER_COUNT for a detailed description.
         *
         * <b>Type:</b> unsigned integer \n
         * <b>Unit:</b> kilobytes (kB) \n
         * <b>Default:</b> 256 \n
         */
        RFC_BUFFER_PAGE_SIZE                        (0x00070002),

        /**
         * An RTSP/HTTP User Agent value associated with the Downloader module.  
         * This property should be set before the Downloader module is opened.
         * 
         * <b>Type:</b> String \n
         */
        DOWNLOADER_USERAGENT_STRING		(0x00090001),
        /**
         *  This property adds additional header fields to be sent along with the HTTP headers
         *  when sending streaming requests (HLS and Smooth Streaming) from the Downloader module.
         *  This property should be set before the Downloader module is opened by DownloaderOpen(). 
         * 
         *  
         *  <b>Type:</b> String \n
         */
        DOWNLOADER_HTTP_HEADER			(0x00090002),
        
        /**
         * For limited time versions of NexPlayer&trade;, this indicates the start date of the limited period of valid use.
         * 
         * Time locked versions of NexPlayer&trade;&nbsp;will only be valid and play content during the period
         * defined by the properties \c LOCK_START_DATE and \c LOCK_END_DATE, and will otherwise
         * return an error ( \c PLAYER_ERROR_TIME_LOCKED). 
         * 
         * This property cannot be set independently but can only be retrieved by calling 
         * \link NexPlayer#getStringProperty(NexProperty) getStringProperty\endlink.  This may be useful for informing users of the limited availability 
         * of the player they are using.
         * 
         * <b>Type:</b> String \n
         * <b>Default:</b> 0 \n
         * 
         * \since version 5.10
         */
        LOCK_START_DATE					(0x000A0001),			// JDKIM 2012/06/11
        
        /**
         * For limited time versions of NexPlayer&trade;, this indicates the end date of the limited period of valid use.
         * 
         * Time locked versions of NexPlayer&trade;&nbsp;will only be valid and play content during the period
         * defined by the properties \c LOCK_START_DATE and \c LOCK_END_DATE, and will otherwise
         * return an error ( \c PLAYER_ERROR_TIME_LOCKED). 
         * 
         * This property cannot be set independently but can only be retrieved by calling 
         * \link NexPlayer#getStringProperty(NexProperty) getStringProperty\endlink.  This may be useful for informing users of the limited availability 
         * of the player they are using.
         * 
         * <b>Type:</b> String \n
         * <b>Default:</b> 0 \n
         * 
         * \since version 5.10
         */
        LOCK_END_DATE					(0x000A0002);			// JDKIM 2012/06/11
        
        private int mIntCode;
        
        /** This is a possible setting for the LOG_LEVEL property; see that property for details. */
        public static final int LOG_LEVEL_NONE = 0x00000000;
        /** This is a possible setting for the LOG_LEVEL property; see that property for details. */
        public static final int LOG_LEVEL_DEBUG = 0x00000001;
        /** This is a possible setting for the LOG_LEVEL property; see that property for details. */
        public static final int LOG_LEVEL_RTP = 0x00000002;
        /** This is a possible setting for the LOG_LEVEL property; see that property for details. */
        public static final int LOG_LEVEL_RTCP = 0x00000004;
        /** This is a possible setting for the LOG_LEVEL property; see that property for details. */
        public static final int LOG_LEVEL_FRAME = 0x00000008;
        /** This is a possible setting for the LOG_LEVEL property; see that property for details. */
        public static final int LOG_LEVEL_ALL = 0x0000FFFF;
        
        /** This is a possible setting for the AV_INIT_OPTION property; see that property for details. */
        public static final int AV_INIT_PARTIAL = 0x00000000;
        /** This is a possible setting for the AV_INIT_OPTION property; see that property for details. */
        public static final int AV_INIT_ALL = 0x00000001;
        
        /** This is a possible setting for the LIVE_VIEW_OPTION property; see that property for details. */
        public static final int LIVE_VIEW_RECENT = 0x00000000;
        /** This is a possible setting for the LIVE_VIEW_OPTION property; see that property for details. */
        public static final int LIVE_VIEW_FIRST = 0x00000001;
        
        /**
         * Sets the NexProperty 
         */
        NexProperty( int intCode ) {
            mIntCode = intCode;
        }
        
        /** 
         * Gets the integer code for the NexPlayer&trade;&nbsp;property.
         * 
         * @return The integer code for the specified property.
         */
        public int getPropertyCode( ) {
            return mIntCode;
        }
    }
    
    /**
     * \brief Possible error codes that NexPlayer&trade;&nbsp;can return.  
     * 
     * This is a Java \c enum so
     * each error constant is an object, but you can convert to or from a numerical
     * code using instance and class methods.
     * 
     * To get the error constant for a given code, call {@link com.nextreaming.nexplayerengine.NexPlayer.NexErrorCode#fromIntegerValue(int) fromIntegerValue(int)}.
     * 
     * To get the error code given an error constant, call {@link com.nextreaming.nexplayerengine.NexPlayer.NexErrorCode#getIntegerCode() getIntegerCode()}.
     * 
     * Because this is a Java \c enum, it is very easy to include the name of the
     * error constant in an error message instead of just the number.  For example, the following
     * code logs the errors that are received from the NexPlayer&trade;&nbsp;engine:
     * 
     * \code
     * void onError( NexPlayer mp, 
     *               NexErrorCode errorCode ) 
     * {
     *     Log.d( "onError", 
     *            "Received the error: " 
     *               + errorCode.name() 
     *               + " (0x" 
     *               + Integer.toHexString(
     *                    errorCode.getIntegerCode()) 
     *               + ")." 
     *          );
     * }
     * \endcode
     * 
     * @author NexStreaming
     *
     */
    public enum NexErrorCode {
        NONE(                           0x00000000,NexErrorCategory.NO_ERROR,"No error"),
        HAS_NO_EFFECT(                  0x00000001,NexErrorCategory.API,"Method has no effect"),
        INVALID_PARAMETER(              0x00000002,NexErrorCategory.API,"Parameter is invalid"),
        INVALID_INFO(                   0x00000003,NexErrorCategory.API,"Information is invalid"),
        INVALID_STATE(                  0x00000004,NexErrorCategory.API,"State is invalid"),
        MEMORY_OPERATION(               0x00000005,NexErrorCategory.SYSTEM, "Memory call failure"),
        FILE_OPERATION(                 0x00000006,NexErrorCategory.SYSTEM, "File system error"),
        FILE_INVALID_SYNTAX(            0x00000007,NexErrorCategory.CONTENT_ERROR, "File contains invalid syntax"),
        NOT_SUPPORT_PVX_FILE(           0x00000008,NexErrorCategory.NOT_SUPPORT, "PVX file is not supported"),
        NOT_SUPPORT_AUDIO_CODEC(        0x00000009,NexErrorCategory.NOT_SUPPORT, "The audio codec is not supported"),
        NOT_SUPPORT_VIDEO_CODEC(        0x0000000A,NexErrorCategory.NOT_SUPPORT, "The video codec is not supported"),
        NOT_SUPPORT_VIDEO_RESOLUTION(   0x0000000B,NexErrorCategory.NOT_SUPPORT, "The video resolution is not supported"),
        NOT_SUPPORT_MEDIA(              0x0000000C,NexErrorCategory.NOT_SUPPORT, "The content format is not supported"),
        INVALID_CODEC(                  0x0000000D,NexErrorCategory.CONTENT_ERROR, "The codec is not supported or is invalid"),
        CODEC(                          0x0000000E,NexErrorCategory.GENERAL, "The codec reported an error"),
        PARTIAL_SUCCESS(                0x0000000F,NexErrorCategory.GENERAL, "The function is succeeded partially" ),
        ALREADY_CREATE_ASYNC_PROC(      0x00000010,NexErrorCategory.INTERNAL, "Async Proc is created already"),
        INVALID_ASYNC_CMD(              0x00000011,NexErrorCategory.INTERNAL, "Async command is invalid"),
        ASYNC_OTHERCMD_PRCESSING(       0x00000012,NexErrorCategory.GENERAL, "Async queue is full"),            // The async queue is full (too many commands issued before existing commands completed)
        RTCP_BYE_RECEIVED(              0x00000013,NexErrorCategory.PROTOCOL, "RTCP Bye message is received"),
        USER_TERMINATED(                0x00000014,NexErrorCategory.NO_ERROR, "User called termination"),
        SYSTEM_FAIL(                    0x00000015,NexErrorCategory.SYSTEM, "System call failure"),
        NODATA_IN_BUFFER(               0x00000016,NexErrorCategory.GENERAL, "No data in buffer"),
        UNKNOWN(                        0x00000017,NexErrorCategory.GENERAL,"Unkown Error"),
        NOT_SUPPORT_TO_SEEK(            0x00000018,NexErrorCategory.NOT_SUPPORT, "The media source does not support seeking."),
        NOT_SUPPORT_AV_CODEC(           0x00000019,NexErrorCategory.NOT_SUPPORT, "Neither the audio nor video codec is supported"),

        NOT_SUPPORT_DRM(                0x00000020,NexErrorCategory.NOT_SUPPORT, "Not Support DRM"),
        NOT_SUPPORT_WMDRM(              0x00000021,NexErrorCategory.NOT_SUPPORT, "Not Support WMDRM"),
        
        PROTOCOL_BASE(                  0x00010000, NexErrorCategory.PROTOCOL),
        PROTOCOL_INVALID_URL(           0x00010001, NexErrorCategory.PROTOCOL, "The url is invalid"),
        PROTOCOL_INVALID_RESPONSE(      0x00010002, NexErrorCategory.PROTOCOL, "The response is invalid"),
        PROTOCOL_CONTENTINFO_PARSING_FAIL( 0x00010003, NexErrorCategory.PROTOCOL, "The Content info is incorrect"),
        PROTOCOL_NO_PROTOCOL(           0x00010004, NexErrorCategory.PROTOCOL, "There is no available protocol"),
        PROTOCOL_NO_MEDIA(              0x00010005, NexErrorCategory.PROTOCOL, "There is no available media" ),
        PROTOCOL_NET_OPEN_FAIL(         0x00010006, NexErrorCategory.PROTOCOL, "Socket open failure"),
        PROTOCOL_NET_CONNECT_FAIL(      0x00010007, NexErrorCategory.NETWORK, "Socket connect failure."),
        PROTOCOL_NET_BIND_FAIL(         0x00010008, NexErrorCategory.NETWORK, "Socket bind failure"),
        PROTOCOL_NET_DNS_FAIL(          0x00010009, NexErrorCategory.NETWORK, "Socket DNS failure"),
        PROTOCOL_NET_CONNECTION_CLOSED( 0x0001000A, NexErrorCategory.NETWORK, "Socket connection closed"),
        PROTOCOL_NET_SEND_FAIL(         0x0001000B, NexErrorCategory.NETWORK, "Socket send failure"),
        PROTOCOL_NET_RECV_FAIL(         0x0001000C, NexErrorCategory.NETWORK, "Socket recv failure"),
        PROTOCOL_NET_REQUEST_TIMEOUT(   0x0001000D, NexErrorCategory.NETWORK, "Request timed out."),

        
        
        ERROR_HTTP_STATUS_CODE(         0x00020000, NexErrorCategory.NETWORK, "HTTP/RTSP Error"),
        // NexPlayer don't use this value.
        NETWORK_RELATED_PROBLEM(        0x0002FFFF,NexErrorCategory.NETWORK, "Network related problem" ), 
        
        ERROR_INTERNAL_BASE(            0x00030000, NexErrorCategory.BASE, "Base of Internal Error"),
        
        ERROR_EXTERNAL_BASE(            0x00040000, NexErrorCategory.BASE, "Base of External Error"),
        
        // errors related to Downloader module
        HTTPDOWNLOADER_ERROR_BASE(	0x00100000, NexErrorCategory.DOWNLOADER, "Base of Http downloader error"),

        HTTPDOWNLOADER_ERROR_FAIL(0x00100000 + 1, NexErrorCategory.DOWNLOADER, "Http downloader error"),
        HTTPDOWNLOADER_ERROR_UNINIT_ERROR(0x00100000 + 2, NexErrorCategory.DOWNLOADER, "Http downloader uninitialized"),
        HTTPDOWNLOADER_ERROR_INVALID_PARAMETER(0x00100000 + 3, NexErrorCategory.DOWNLOADER, "Http downloader - parameter is invalid "),
        HTTPDOWNLOADER_ERROR_MEMORY_FAIL(0x00100000 + 4, NexErrorCategory.DOWNLOADER, "Http downloader - memory call failure"),
        HTTPDOWNLOADER_ERROR_SYSTEM_FAIL(0x00100000 + 5, NexErrorCategory.DOWNLOADER, "Http downloader - system call failure"),
        HTTPDOWNLOADER_ERROR_WRITE_FAIL(0x00100000 + 6, NexErrorCategory.DOWNLOADER, "Http downloader - file writing failure"),        
        HTTPDOWNLOADER_ERROR_HAS_NO_EFFEECT(0x00100000 + 7, NexErrorCategory.DOWNLOADER, "Http downloader - method has no effect"),
        HTTPDOWNLOADER_ERROR_MAX_DOWNLOADING(0x00100000 + 8, NexErrorCategory.DOWNLOADER,"Http downloader - number of download can't over maximum"),
        HTTPDOWNLOADER_ERROR_EVENT_FULL(0x00100000 + 9, NexErrorCategory.DOWNLOADER, "Http downloader - event is full"),
        //HTTPDOWNLOADER_ERROR_INVALID_DOWNLOAD_HANDLE(0x00100000 + 10, NexErrorCategory.API),
        HTTPDOWNLOADER_ERROR_NETWORK(0x00120000 + 0, NexErrorCategory.DOWNLOADER, "Http downloader - can't connect network"),
        HTTPDOWNLOADER_ERROR_NETWORK_RECV_FAIL(0x00120000 + 1, NexErrorCategory.DOWNLOADER, "Http downloader - recv failure"),
        HTTPDOWNLOADER_ERROR_NETWORK_INVALID_RESPONSE(0x00120000 + 2, NexErrorCategory.DOWNLOADER, "http downloader - The response is invalid"),
        HTTPDOWNLOADER_ERROR_PARSE_URL(0x00120000 + 3, NexErrorCategory.DOWNLOADER, "Http downloader - url is incorrect"),
        HTTPDOWNLOADER_ERROR_ALREADY_DOWNLOADED(0x00130000, NexErrorCategory.DOWNLOADER, "Http downloader - file is already downloaded"),
        HTTPDOWNLOADER_ERROR_UNKNOWN(0x001FFFFF + 0, NexErrorCategory.DOWNLOADER, "Http downloader - unknown error"),
        
        HTTPDOWNLOADERENG_ERROR_INIT_FAIL(0x9000A002, NexErrorCategory.DOWNLOADER, "Http downloader fail to initialize"),
        HTTPDOWNLOADERENG_ERROR_FAIL(0x9000A003, NexErrorCategory.DOWNLOADER, "Http downloader engine error."),
        HTTPDOWNLOADERENG_ERROR_INVALID_PARAMETER(0x9000A005, NexErrorCategory.DOWNLOADER, "Http downloader - parameter is invalid"),
        HTTPDOWNLOADERENG_ERROR_INVALID_HANDLE(0x9000A006, NexErrorCategory.DOWNLOADER, "Http downloader - handle is invalid"),

        JNI_ERROR(0x70000001, NexErrorCategory.API ,"Base of JNI error"),
        JNI_ERROR_NOT_SUPPORT_SDK(0x70000001, NexErrorCategory.API, "JNI - SDK doesn't support " ),
        JNI_ERROR_INVALID_PARAMETER(0x70000002, NexErrorCategory.API, "JNI - Parameter is invalid"),
        JNI_ERROR_VERSION_MISMATCH(0x70000003, NexErrorCategory.API, "JNI - Version mismatch" ),
        JNI_ERROR_LOAD_CALBODY(0x70000004, NexErrorCategory.API, "JNI - calbody loading failure"),
        JNI_ERROR_LOAD_RALBODY(0x70000005, NexErrorCategory.API, "JNI - ralbody loading failure"),
        JNI_ERROR_CREATE_PLAYER(0x70000006, NexErrorCategory.API, "JNI- creating player failure" ),
        JNI_ERROR_INVALID_PLAYER(0x70000007, NexErrorCategory.API, "JNI - player is invalid" ),
        JNI_ERROR_CREATE_DOWNLOADER(0x70000008, NexErrorCategory.API, "JNI - creating http downloader failure" ),
        JNI_ERROR_RALBODY_FUNC(0x70000009, NexErrorCategory.API, "JNI - ralbody function error" ),
        JNI_ERROR_OBJECT_FAIL(0x7000000A, NexErrorCategory.API, "JNI - can't create object" ),
        JNI_ERROR_REGISTER_DRM(0x7000000B, NexErrorCategory.API, "JNI - To register DRM failed" ),
        
        PLAYER_ERROR_COMMAND_RESULT(0x80000004, NexErrorCategory.API, "Player Eng - Command Error"),
        PLAYER_ERROR_CREATE_RFC(0x8000000C, NexErrorCategory.API, "Player Eng - To create RFC failure"),
        PLAYER_ERROR_INVALID_SDK(0x8000000D, NexErrorCategory.API, "Player Eng - SDK is invalid"),
        PLAYER_ERROR_ONLY_USE_DRM(0x8000000E, NexErrorCategory.API, "Player Eng - Only support drm" ),
        PLAYER_ERROR_NOT_SUPPORT(0x8000000F, NexErrorCategory.API, "Player Eng - API is not supported"),   
        PLAYER_ERROR_INVALID_PARAMETER(0x80000060, NexErrorCategory.API, "Player Eng - Parameter is invalid"),
        PLAYER_ERROR_TIME_LOCKED(0x800000A0, NexErrorCategory.API, "SDK was expired");						// JDKIM 2012/06/11
        
        private int mCode;
        private String mDesc;
        private NexErrorCategory mCategory;
        
        
        NexErrorCode( int code, String desc ){
            mCode = code;
            mDesc = desc;
            mCategory = NexErrorCategory.GENERAL;
        }
        
        NexErrorCode( int code, NexErrorCategory category, String desc ){
            mCode = code;
            mDesc = desc;
            mCategory = category;
        }
        
        NexErrorCode( int code, NexErrorCategory category ){
            mCode = code;
            mDesc = "An error occurred (error 0x " + Integer.toHexString(mCode) + ": " + this.name() + ").";
            mCategory = category;
        }
        
        NexErrorCode( int code ){
            mCode = code;
            mDesc = "An error occurred (error 0x " + Integer.toHexString(mCode) + ": " + this.name() + ").";
            mCategory = NexErrorCategory.GENERAL;
        }
        
        /**
         * Gets the integer code associated with a given error.
         * 
         * @return An integer error code as provided by the NexPlayer&trade;&nbsp;engine.
         */
        public int getIntegerCode() {
            return mCode;
        }
        
        /**
         * Gets a description of the error suitable for display in an
         * error pop-up.
         * 
         * <B>CAUTION:</B> This is experimental and is subject to change.
         * The strings returned by this method may change in future versions,
         * may not cover all possible errors, and are not currently localized.
         * 
         * @return A string describing the error.
         */
        public String getDesc() {
            return mDesc;
        }
        
        /**
         * Returns the category of the error.
         * 
         * <B>CAUTION:</B> This is experimental and is subject to change.
         * 
         * Error categories are an experimental feature.  The idea is that the
         * application can provide a friendlier (and possibly more useful) message
         * based on the category of the error.  For example, if the category is
         * <i>NETWORK</i>, the application may suggest that the user check their
         * network connection. 
         * 
         * This is experimental, so the set of categories may change in future
         * versions of the API, or the feature may be removed entirely.  Use it
         * with caution.
         * 
         * @return The category to which the error belongs.
         */
        public NexErrorCategory getCategory() {
            return mCategory;
        }
        
        /**
         * Returns a NexErrorCode object for the specified error code.
         * 
         * @param code
         *          The integer code to convert into a NexErrorCode object.
         * @return
         *          The corresponding NexErrorCode object or \c null if
         *          an invalid code was passed.
         */
        public static NexErrorCode fromIntegerValue( int code ) {
            for( int i=0; i<NexErrorCode.values().length; i++ ) {
                if( NexErrorCode.values()[i].mCode == code )
                    return NexErrorCode.values()[i];
            }
            return NexErrorCode.values()[23]; // Unknown Error.
        }
    }
    
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_NONE                    = 0x0;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_AUDIO_GET_CODEC_FAILED  = 0x1;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_VIDEO_GET_CODEC_FAILED  = 0x2;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_AUDIO_INIT_FAILED       = 0x3;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_VIDEO_INIT_FAILED       = 0x4;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_TRACK_CHANGED           = 0x5;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_STREAM_CHANGED          = 0x6;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_DSI_CHANGED             = 0x7;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_OBJECT_CHANGED          = 0x8;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_CONTENT_INFO_UPDATED    = 0x9;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_AVMODE_CHANGED          = 0xa;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_HTTP_INVALID_RESPONSE   = 0xb;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_EXTERNAL_DOWNLOAD_CANCELED   = 0x20;
    /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. */
    public static final int NEXPLAYER_STATUS_REPORT_MAX                     = 0xFFFFFFFF;
    
    //public byte[] pucAudioPCM = new byte[8192];
    
    static
    {
        /*
         * Load the library. If it's already loaded, this does nothing.
         */
        System.loadLibrary("nexplayerengine");
        Log.d(TAG,"Loading nexplayerengine.");
    }   
    
    /** return value of getPlatformInfo() for checking Android Version  
     *  not supported platform.
     */
    private static final int NEX_SUPPORT_PLATFORM_NOTHING = 0x0;
    
    /** return value of getPlatformInfo() for checking Android Version
     * Cupcake  
     */
    private static final int NEX_SUPPORT_PLATFORM_CUPCAKE = 0x15;
    /** return value of getPlatformInfo() for checking Android Version  
     *  Donut
     */
    private static final int NEX_SUPPORT_PLATFORM_DONUT = 0x16;
    /** return value of getPlatformInfo() for checking Android Version
     *  Eclair  
     */
    private static final int NEX_SUPPORT_PLATFORM_ECLAIR = 0x21;
    /** return value of getPlatformInfo() for checking Android Version
     *  Froyo  
     */
    private static final int NEX_SUPPORT_PLATFORM_FROYO = 0x22;
    /** return value of getPlatformInfo() for checking Android Version
     *  Gingerbread  
     */
    private static final int NEX_SUPPORT_PLATFORM_GINGERBREAD = 0x30;
    /** return value of getPlatformInfo() for checking Android Version
     *  Honeycomb  
     */ 
    private static final int NEX_SUPPORT_PLATFORM_HONEYCOMB = 0x31;
    /**
     *  return value of getPlatformInfo() for checking Android Version
     *  Ice Cream Sandwich
     */    
    private static final int NEX_SUPPORT_PLATFORM_ICECREAM_SANDWICH = 0x40;
    /**
     *  return value of getPlatformInfo() for checking Android Version
     *  Jelly Bean
     */    
    private static final int NEX_SUPPORT_PLATFORM_JELLYBEAN = 0x41;

    
    /** Returns Android Version
     */
    private int getPlatformInfo()
    {
        int iPlatform = 0;
        
        String strVersion =android.os.Build.VERSION.RELEASE;
        
        
        Log.d(TAG, "PLATFORM INFO: " + strVersion);
        
        
        if(strVersion.startsWith("1.5"))
        {
            iPlatform = NEX_SUPPORT_PLATFORM_CUPCAKE;
        }
        else if( strVersion.startsWith("1.6"))
        {
            iPlatform = NEX_SUPPORT_PLATFORM_DONUT;
        }
        else if( strVersion.startsWith("2.1"))
        {
            iPlatform = NEX_SUPPORT_PLATFORM_ECLAIR;
        }
        else if( strVersion.startsWith("2.2"))
        {
            iPlatform = NEX_SUPPORT_PLATFORM_FROYO;
        }
        else if( strVersion.startsWith("2.3"))
        {
            iPlatform = NEX_SUPPORT_PLATFORM_GINGERBREAD;
        }
        else if( strVersion.startsWith("3."))
        {
            iPlatform = NEX_SUPPORT_PLATFORM_HONEYCOMB;
        }
        else if( strVersion.startsWith("4.0"))
        {
            iPlatform = NEX_SUPPORT_PLATFORM_ICECREAM_SANDWICH;
        }
        else if( strVersion.startsWith("4.1"))
        {
        	iPlatform = NEX_SUPPORT_PLATFORM_JELLYBEAN;
        }
        else
        {
            iPlatform = NEX_SUPPORT_PLATFORM_NOTHING;
        }
        
        return iPlatform;
    }
    
    /** A special debugging value for the \c strModel parameter of \link NexPlayer.init\endlink.  For testing only, not for release code.  See that method description for details.*/
    public static final String NEX_DEVICE_USE_ONLY_ANDROID = "Android";
    /** A special debugging value for the \c strModel parameter of \link NexPlayer.init\endlink.  For testing only, not for release code.  See that method description for details.*/
    public static final String NEX_DEVICE_USE_JAVA = "JAVA";
    /** A special debugging value for the \c strModel parameter of \link NexPlayer.init\endlink.  For testing only, not for release code.  See that method description for details.*/
    public static final String NEX_DEVICE_USE_OPENGL = "OPENGL";
    /** A special debugging value for the \c strModel parameter of \link NexPlayer.init\endlink.  For testing only, not for release code.  See that method description for details.*/
    public static final String NEX_DEVICE_USE_ANDROID_3D = "Android 3D";
    
    private String getDeviceInfo()
    {
        return android.os.Build.MODEL;
    }
    
    /**
     * Sole constructor for NexPlayer&trade;.
     * 
     * After constructing a NexPlayer&trade;&nbsp;object, you <i>must</i> call
     * NexPlayer.init before you can call any other methods
     */
    public NexPlayer( )
    {
        mNexPlayerInit = false;
    }   
    
    /**
     * \brief  Determines if NexPlayer&trade;&nbsp;is currently initialized.
     * 
     * To initialize NexPlayer&trade;, NexPlayer.init must be called. If that
     * method returns \c true, then this method will also return
     * \c true if called on the same instance of NexPlayer&trade;.
     * 
     * In some cases, it is necessary to call NexPlayer&trade;&nbsp; functions from event handlers
     * in subclasses of \c Activity (such as \c onPause or \c onStop ).
     * In such event handlers, it is possible for them to be called before code that
     * initializes NexPlayer&trade;, or for them to be called after a failed initialization.  Therefore,
     * any calls to NexPlayer&trade;&nbsp;methods made from \c onPause or similar event handlers
     * must be protected as follows: 
     * \code
     * if( nexPlayer.isInitialized() ) 
     * {
     *     // Calls to other methods are safe here 
     * } 
     * \endcode
     * 
     * @return \c true if NexPlayer&trade;&nbsp;is currently initialized.
     */
    public boolean isInitialized() {
        return mNexPlayerInit;
    }
    
    /**
     * \brief  Initializes NexPlayer&trade;. This must be called before any
     * other methods.
     * 
     * @param context       The current context; from \c Activity subclasses, you can
     *                      just pass <code>this</code>.
     * @param strModel      Device model name.  NexPlayer&trade;&nbsp; includes multiple renderer
     *                      modules, and past versions of the player selected the module most suitable
     *                      to the device based on this value. The renderer is now set by the parameter
     *                      \c strRenderMode.
     *                      Under normal (production) use, you should pass the MODEL
     *                      as available via the Android API in \c android.os.Build.MODEL.  
     *                      For example:
     * \code
     * nexPlayer.init(this, android.os.Build.MODEL, NEX_DEVICE_USE_OPENGL, 0, 1);
     * \endcode
     *                      NexPlayer&trade;&nbsp;uses this to select the most appropriate renderer if no renderer is selected
     *                      (\c NULL is passed) with the parameter \c strRenderMode below.  For OS versions
     *                      up to Gingerbread, this is always the Android Renderer (although from Froyo, other renderers are
     *                      supported as well). For Honeycomb and Ice Cream Sandwich (ICS), this is always the OpenGL renderer.
     * \param strRenderMode The Renderer to use, as a string. Past versions of NexPlayer&trade;&nbsp;set the renderer based 
     *                       on the device and operating system in use, but now this string sets
     *                       the renderer, regardless of the device.  While most devices should work properly with OpenGL,
     *                       occasionally another rendering module may be beneficial, for example if a device supports 3D rendering,
     *                       if an application doesn't implement support for the OpenGL renderer, or
     *                       for devices running older versions of the Android OS.  In some other cases, such as the Kindle Fire running on 
     *                       Gingerbread, while the default renderer is Android, the OpenGL renderer is recommended because
     *                       of improved performance. Please note that for Honeycomb and Ice Cream Sandwich (ICS), 
     *                       any value other than \c NEX_DEVICE_USE_OPENGL will be ignored because the OpenGL Renderer
     *                       will always be used.  This will be one of:
     *                          - <b>{@link NexPlayer#NEX_DEVICE_USE_ONLY_ANDROID NEX_DEVICE_USE_ONLY_ANDROID}
     *                          ("Android")</b> Use only standard Android API bitmaps to display frames.  This
     *                          is usually slower, but is more portable.
     *                          - <b>{@link NexPlayer#NEX_DEVICE_USE_JAVA NEX_DEVICE_USE_JAVA}
     *                          ("JAVA")</b> Use the Java renderer.
     *                          - <b>{@link NexPlayer#NEX_DEVICE_USE_OPENGL NEX_DEVICE_USE_OPENGL}
     *                          ("OPENGL")</b> Use the OpenGL renderer.
     *                          - <b>{@link NexPlayer#NEX_DEVICE_USE_ANDROID_3D NEX_DEVICE_USE_ANDROID_3D}
     *                          ("Android 3D")</b>Use the 3D video renderer with standard Android API bitmaps.
     * @param logLevel      NexPlayer&trade;&nbsp;SDK logging level.  This affects the messages that the SDK writes to the
     *                      Android log.
     *                          - <b>-1</b> : Do not output any log messages.
     *                          - <b>0</b> : Output basic log messages only (recommended).
     *                          - <b>1~4</b> : Output detailed log messages; higher numbers result in more verbose
     *                                      log entries, but may cause performance issues in some cases and are
     *                                      not recommended for general release code.
     * @param colorDepth    Video output image color depth.
     *                          - <b>1</b> : RGBA_8888
     *                          - <b>4</b> : RGB_565
     * 
     * @return              \c true if initialization succeeded; \c false in the case of a 
     *                      failure (in the case of failure, check the log for details).
     */
    
    public boolean init( Context context, String strModel, String strRenderMode, int logLevel, int colorDepth) {
        Log.d(TAG, "Request to init player; current init status=" + mNexPlayerInit);
        
        int iCPUInfo = 0;
        int iPlatform = 0;
        int iStartIndex = 0;
        int iPackageNameLength = 0;
        String strDeviceModel = "";
        String strDeviceRenderMode = "";
        
        String strPackageName = context.getApplicationContext().getPackageName();
        
        File fileDir = context.getFilesDir();
        if( fileDir == null)
            throw new IllegalStateException("No files directory - cannot play video - relates to Android issue: 8886!");
        String strPath = fileDir.getAbsolutePath();
        
        String strLibPath = "";
        
        iPackageNameLength = strPackageName.length();
        iStartIndex = strPath.indexOf(strPackageName);
        
        iCPUInfo = NexSystemInfo.getCPUInfo();
        iPlatform = getPlatformInfo();
        
        if(strModel == null)
        {
            strDeviceModel = getDeviceInfo();
        }
        else
        {
            strDeviceModel = strModel;
        }
        
        if(strRenderMode == null)
        {
        	strDeviceRenderMode = getDeviceInfo();
        }
        else
        {
        	strDeviceRenderMode = strRenderMode;
        }
        
        if( iPlatform == NEX_SUPPORT_PLATFORM_CUPCAKE )
            iCPUInfo = NexSystemInfo.NEX_SUPPORT_CPU_ARMV5;
        
        strLibPath = strPath.substring(0, iStartIndex + iPackageNameLength) + "/";
        
        Log.d(TAG, "PackageName : " + strPackageName);
        Log.d(TAG, "Files Dir : " + strPath);
        Log.d(TAG, "LibPath :" + strLibPath);
        Log.d(TAG, "CPUINFO :" + iCPUInfo + " SDKINFO : " + iPlatform);
        Log.d(TAG, "Model : " + strDeviceModel);
        Log.d(TAG, "RenderMode : " + strDeviceRenderMode);
        Log.d(TAG, "Log Level : " + logLevel);
        
        if( !mNexPlayerInit ) {
        	
        	int nMajor = getVersion(0);
        	int nMinor = getVersion(1);
        	
        	if(nMajor != NEXPLAYER_VERSION_MAJOR || nMinor != NEXPLAYER_VERSION_MINOR)
        	{
        		Log.d( TAG, "NexPlayer Version Mismatch!" );
        		return mNexPlayerInit;
        	}
        	
            int nRet = _Constructor( new WeakReference<NexPlayer>( this ), 
                                    context.getApplicationContext().getPackageName(),
                                    strLibPath,
                                    iPlatform,
                                    iCPUInfo,
                                    strDeviceModel,
                                    strDeviceRenderMode,
                                    logLevel,
                                    colorDepth);
            if( nRet == 0 ) {
                mNexPlayerInit = true;
                Log.d( TAG, "Init success!" );
            } else {
                Log.d( TAG, "Init failure: " + nRet );
            }
        }
        return mNexPlayerInit;
    }
    
    /**
     * \brief  Registers a callback that will be invoked when new events occur.
     * 
     * The events dispatched to this callback interface serve three functions:
     *  - to provide new video and audio data to the application,
     *      for the application to present to the user.
     *  - to notify the application when a command has completed,
     *      so that the application can issue any follow-up
     *      commands.  For example, issuing {@link NexPlayer#start(int) start} 
     *      when {@link NexPlayer#open open} has completed.
     *  - to notify the application when there are state changes that the
     *      application may wish to reflect in the interface.
     * 
     * All applications <i>must</i> implement
     * this callback and provide certain minimal functionality. See the
     * \link NexPlayer.IListener IListener\endlink documentation for a list of 
     * events and information on implementing them. 
     * 
     * In an Android application, there are two common idioms for implementing
     * this.  The most typical is to have the \c Activity subclass 
     * implement the \c IListener interface.
     * 
     * The other approach is to define an anonymous class in-line:
     * \code
     * mNexPlayer.setListener(new NexPlayer.IListener() {
     *
     *     &#064;Override
     *     public void onVideoRenderRender(NexPlayer mp) {
     *         // ...event implementaton goes here...
     *     }
     *     
     *     // ...other methods defined by the interface go here...
     * });
     * \endcode
     * 
     * \param listener
     *            The object on which methods will be called when new events occur.
     *            This must implement the \c IListener interface.
     */
    public void setListener( IListener listener )
    {
        if( !mNexPlayerInit )
        {
            Log.d(TAG, "Attempt to call setListered() but player not initialized; call NexPlayer.init() first!");
        }
        mListener = listener;
    }
    
    /**
     * \brief This method begins opening the media at the specified path or URL.  This supports both
     *        local content and streaming content.  
     *
     * This is an asynchronous operation that
     * will run in the background (even for local content).
     * 
     * When this operation completes,
     * \link NexPlayer.IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete\endlink
     * is called with one of the following command constants (depending on the \c type
     * specified in the \c open call): 
     * - \link NexPlayer#NEXPLAYER_ASYNC_CMD_OPEN_LOCAL NEXPLAYER_ASYNC_CMD_OPEN_LOCAL\endlink
     * - \link NexPlayer#NEXPLAYER_ASYNC_CMD_OPEN_STREAMING NEXPLAYER_ASYNC_CMD_OPEN_STREAMING\endlink
     * 
     * Success or failure of the operation can be determined by checking the \c result
     * argument passed to \c onAsyncCmdComplete.  If the result is 0, the media was
     * successfully opened; if it is any other value, the operation failed.
     * 
     * Calls to \c open must be matched with calls to NexPlayer.close .
     * 
     * @param path
     *          The location of the content: a path (for local content) or URL (for remote content).
     * @param smiPath
     *          The path to a local subtitle file, or \c null for no subtitles.  For streaming content
     *          that already includes subtitles, this should be \c null (using both types of subtitles
     *          at the same time will cause undefined behavior).
     * @param externalPDPath
     *          When not c\ null, the external path used to play PD content downloaded by the Downloader module.  
     *          This is only available for PIFF content. 
     * @param type
     *          This determines how the path argument is interpreted.  This will be one of:
     *                - \link NexPlayer#NEXPLAYER_SOURCE_TYPE_LOCAL_NORMAL NEXPLAYER_SOURCE_TYPE_LOCAL_NORMAL\endlink 
     *                  to play local media (the path is a local filesystem path)
     *                - \link NexPlayer#NEXPLAYER_SOURCE_TYPE_STREAMING NEXPLAYER_SOURCE_TYPE_STREAMING\endlink
     *                  to play remote media sources (including RTSP streaming, 
     *                  progressive download and HTTP Live streaming).  The path is
     *                  interpreted as an URL. \n
     *                .
     *          Other \c NEXPLAYER_SOURCE_* values are not 
     *          supported in this version and should not be used.
     * @param transportType
     *          The network transport type to use on the connection.  This should be one of:
     *                - \link NexPlayer#NEXPLAYER_TRANSPORT_TYPE_TCP  NEXPLAYER_TRANSPORT_TYPE_TCP\endlink
     *                - \link NexPlayer#NEXPLAYER_TRANSPORT_TYPE_UDP  NEXPLAYER_TRANSPORT_TYPE_UDP\endlink
     *                .
     * @param bufferingTime
     *          The number of milliseconds of media to buffer before beginning
     *          playback.
     *                
     * @return The status of the operation: this is zero in the case of success, or
     *          a non-zero NexPlayer&trade;&nbsp;error code in the case of failure.
     *
     *          \note This only indicates the success or failure of <i>starting</i> the operation.
     *          Even if this reports success, the operation may still fail later,
     *          asynchronously, in which case the application is notified in
     *          \c onAsyncCmdComplete.
     */

    public native int open( String path, String smiPath, String externalPDPath, int type, int transportType, int bufferingTime );
    
    /**
     * \brief Starts playing media from the specified timestamp.
     * 
     * The media must have already been successfully opened with
     * \link NexPlayer#open open\endlink.  This only works
     * for media that is in the stopped state; to change the play
     * position of media that is currently playing or paused, call
     * \link NexPlayer#seek(int) seek\endlink instead.
     * 
     * When this operation completes,
     * \link NexPlayer.IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete\endlink
     * is called with one of the following command constants (depending on the \c type
     * specified in the \c open call): 
     *  - \link NexPlayer#NEXPLAYER_ASYNC_CMD_START_LOCAL  NEXPLAYER_ASYNC_CMD_START_LOCAL\endlink
     *  - \link NexPlayer#NEXPLAYER_ASYNC_CMD_START_STREAMING  NEXPLAYER_ASYNC_CMD_START_STREAMING\endlink
     * 
     * Success or failure of the operation can be determined by checking the \c result
     * argument passed to \c onAsyncCmdComplete.  If the result is 0, the media was
     * successfully opened; if it is any other value, the operation failed.
     * 
     * \param msec
     *          The offset (in milliseconds) from the beginning of the media
     *          at which to start playback.  This should be zero to start at the beginning.
     *              
     * \return  The status of the operation.  This is zero in the case of success, or
     *          a non-zero NexPlayer error code in the case of failure.
     *
     *          \note This only indicates the success or failure of <i>starting</i> the operation.
     *          Even if this reports success, the operation may still fail later,
     *          asynchronously, in which case the application is notified in
     *          \c onAsyncCmdComplete.
     */
    public native int start( int msec );
    
    /** 
     * \brief This function pauses the current playback. 
     *
     * \return  Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
     */
    public native int pause();
    
    /**
     * \brief This function resumes playback beginning at the point at which the player
     *        was last paused.
     *
     * \return  Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
     */
    public native int resume();
    
    /**
     * \brief This function seeks the playback position to a specific time.  
     * 
     * This doesn't work if NexPlayer&trade;&nbsp;is stopped or if the stream
     * doesn't support seeking, but does work if NexPlayer&trade;&nbsp;is playing or paused.
     * 
     * \param msec
     *          The offset in milliseconds from the beginning of the media.
     *
     * \return  Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
     */
    public native int seek( int msec );
    
    /**
     *  \brief  This function activates the \c fastPlay feature in HLS content.
     *  
     *  The \c fastPlay feature allows NexPlayer&trade;&nbsp;to play HLS content at a speed other than normal playback speed.
     *  Because this feature uses the I-frame track of HLS content, when \c fastPlay is activated, content is played more
     *  quickly than normal and there is no audio (similar to a fast forward feature).
     *  
     *  The player can also rewind quickly through HLS content using the \c fastPlay feature by setting the \c rate parameter to a 
     *  negative value.
     *  
     *  To change the speed or direction of the \c fastPlay feature, simply call the \link NexPlayer#fastPlaySetPlaybackRate fastPlaySetPlaybackRate\endlink
     *  method and change the \c rate parameter to the desired value.
     *  
     *  \param msec		The time in the content at which to start \c fastPlay (in msec).
     *  \param rate		The speed at which video will play in \c fastPlay mode.
     *  				This speed is indicated by any \c float value (but NOT zero), where
     *  				negative values rewind the video at faster than normal playback speed and positive 
     *  				values play the video faster than normal (like fast forward).
     *  				For example:  
     *  					- \c rate = 3.0 (\c fastPlay plays video at 3x normal speed)
     *  					- \c rate = - 2.0 ( \c fastPlay rewinds video at 2x normal speed)
     *  
     *  \return  Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.  
     *  
     *  \since version 5.12
     */
    public native int fastPlayStart(int msec, float rate);

    /**
     *  \brief  This function sets the video playback rate for the \c fastPlay feature. 
     *  
     *  HLS video content will be played at the speed set by the playback rate when the \c fastPlay feature
     *  is activated by calling \c fastPlayStart.  This rate can be set to any \c float value (excluding zero), 
     *  where positive values will play content back at a faster speed and negative values will rewind content
     *  at the set rate faster than normal playback speed.
     *  
     *  If \c rate is set to zero, this method will return an error.
     *  
     *  \param rate		The speed at which video will play in \c fastPlay mode.
     *  				This speed is indicated by any \c float value (but NOT zero), where
     *  				negative values rewind the video at faster than normal playback speed and positive 
     *  				values play the video faster than normal (like fast forward).
     *  				For example:  
     *  					- rate = 3.0 (\c fastPlay plays video at 3x normal speed)
     *  					- rate = - 2.0 ( \c fastPlay rewinds video at 2x normal speed)
     *  
     *  \return  Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
     *  
     *  \since version 5.12
     *  
     */
    public native int fastPlaySetPlaybackRate( float rate);
    
    
    /**
     * \brief  This function turns off the \c fastPlay feature in HLS content.
     * 
     * Once the \c fastPlay feature has been activated by calling \link NexPlayer#fastPlayStart fastPlayStart\endlink, this method must be
     * called in order to stop \c fastPlay.  
     * 
     * In order to reactivate the \c fastPlay feature after calling \link NexPlayer#fastPlayStop fastPlayStop\endlink,
     * simply call the \link NexPlayer#fastPlayStart fastPlayStart\endlink method again.  If fastPlayStop is called when \c fastPlay is not 
     * activated, an error will be returned.
     * 
     * \param bResume	This boolean value sets whether to resume playback after \c fastPlay or not.
     * 					If \c bResume = 1, video will automatically resume playback when \c fastPlay stops.
     * 					If \c bResume = 0, when \c fastPlay stops, the content in NexPlayer&trade;&nbsp;will
     * 					be paused.
     * 
     * \return Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
     * 
     *  \since version 5.12
     */
    public int fastPlayStop(boolean bResume)
    {
    	return fastPlayStop(bResume?1:0);
    }
    
    
    private native int fastPlayStop(int bResume);
    
    
    /** 
     * \brief This function stops the current playback. 
     * 
     * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
     */
    public native int stop();
    
    /**
     * \brief This function ends all the work on the content currently open and
     * closes content data.  The content must be stopped <i>before</i> calling 
     * this method.
     * 
     * The correct way to finish playing content is to either wait for the
     * end of content, or to call \c stop and wait for the stop
     * operation to complete, then call \c close. 
     * 
     * @return Zero for success, or a non-zero NexPlayer&trade;&nbsp;error code in the event of a failure.
     */
    public native int close();
    
    /**
     * \brief This function retrieves the current state of NexPlayer&trade;.
     * 
     * Calling methods such as \link NexPlayer#open open\endlink
     * and \link NexPlayer#start start\endlink does not immediately change the
     * state.  The state changes asynchronously, and the new state goes
     * into effect at the same time 
     * \link NexPlayer.IListener#onAsyncCmdComplete(NexPlayer, int, int, int, int) onAsyncCmdComplete\endlink
     * is called to notify the application.
     * 
     * \return  A constant indicating the current state.  This is one 
     *          of the following values:
     *            - \link NexPlayer#NEXPLAYER_STATE_CLOSED NEXPLAYER_STATE_CLOSED\endlink
     *            - \link NexPlayer#NEXPLAYER_STATE_NONE NEXPLAYER_STATE_NONE\endlink
     *            - \link NexPlayer#NEXPLAYER_STATE_PAUSE NEXPLAYER_STATE_PAUSE\endlink
     *            - \link NexPlayer#NEXPLAYER_STATE_PLAY NEXPLAYER_STATE_PLAY\endlink
     *            - \link NexPlayer#NEXPLAYER_STATE_STOP NEXPLAYER_STATE_STOP\endlink
     *            .
     * 
     */
    public native int getState();
    
    // for Recording
    /** Recording interface; not available in current version. Do not use.
     * @deprecated Not available in current version; do not use. */
    public native int recStart( String path, int maxsize );
    /** Recording interface; not available in current version. Do not use.
     * @deprecated Not available in current version; do not use. */
    public native int recPause();
    /** Recording interface; not available in current version. Do not use.
     * @deprecated Not available in current version; do not use. */
    public native int recResume();
    /** Recording interface; not available in current version. Do not use.
     * @deprecated Not available in current version; do not use. */
    public native int recStop();
    
    // for TimeShift
    /** Timeshift interface; not available in current version. Do not use.
     * @deprecated Not available in current version; do not use. */
    public native int timeStart( String AudioFile, String VideoFile, int maxtime, int maxfilesize );
    /** Timeshift interface; not available in current version. Do not use.
     * @deprecated Not available in current version; do not use. */
    public native int timeResume();
    /** Timeshift interface; not available in current version. Do not use.
     * @deprecated Not available in current version; do not use. */
    public native int timeStop();
    /** Timeshift interface; not available in current version. Do not use.
     * @deprecated Not available in current version; do not use. */
    public native int timePause();
    /** Timeshift interface; not available in current version. Do not use.
     * @deprecated Not available in current version; do not use. */
    public native int timeBackward( int skiptime );
    /** Timeshift interface; not available in current version. Do not use.
     * @deprecated Not available in current version; do not use. */
    public native int timeForward( int skiptime );
    
    /**
     * 
     * \brief Sets the input channel for CEA 608 closed captions.
     * 
     *  This method will be one of eight available channels.  Setting the input channel to zero will also disable or turn off CEA 608 closed
     *  captions if they are present.
     *  
     *  When viewing content including CEA 608 closed captions, it is important to choose whether to have the 
     *  application support them in BASIC or FULL mode with the property {@link NexProperty#SET_CEA608_TYPE SET_CEA608_TYPE}.  
     *  In order to meet CEA 608 closed caption specifications completely, the closed captions should be displayed
     *  in the FULL mode.  If BASIC mode is selected, the closed captions will be treated similar to other subtitles
     *  and may not always be accurately displayed (or may be difficult for a user to read) depending on the display attributes 
     *  used in the closed captions.
     *  
     *  When supporting CEA 608 closed captions in BASIC mode, setting the input channel to any number between 1 and 4 merely
     *  enables and displays the captions. On the otherhand, setting the input channel to any number between 5 to 8 will enable the textmode.
     *  
     *  Since CEA 608 closed captions may include different information on the available input channels, the desired input
     *  channel may be selected in FULL mode by choosing the relevant channel number (1, 2, 3, 4, 5, 6, 7, 8).
     *  
     *  \param nChannel  This will be an integer from 0 to 8.  If it is zero, the closed captions will be disabled.
     *                   When using CEA 608 captions in BASIC mode, 1 to 4 simply enable the captions.
     *                   When using CEA 608 captions in FULL mode, 1 to 4 chooses the input channel to be displayed
     *                   excluding the textmode.
     *                   When using CEA 608 captions in FULL mode, 5 to 8 is primary used for enabling the textmode.
     *  
     *  \returns Zero if successful or a non-zero error code.
     */
    public native int setCEA608CaptionChannel( int nChannel ); // 0 = OFF, 1...4 = channel number
    
    /**
     * This function retrieves the information on the currently
     * open content.
     * 
     * @param info
     *            Content information class object.
     */
    private native int getInfo( Object info );
    
    
    /**
     * \brief This function returns the name of a CSS class used in the current SMI subtitles file.
     * 
     * Each SMI subtitle file uses CSS classes to differentiate between the available
     * subtitle tracks within the file.
     * 
     * The total number of available subtitle tracks
     * is passed as an argument to \link NexPlayer.IListener.onTextRenderInit onTextRenderInit\endlink, and the CSS class name of an
     * individual track can be determined by passing the index (zero based) of the track
     * to this function.
     * 
     * Note that CSS class names are the internal names used in the SMI file to
     * identify a track and they should not be displayed directly to the user.
     * 
     * The results of calling this function with an out-of-range value are undefined;
     * it should be called only with values between \c 0 and \c n-1 where \c n is the number 
     * of tracks passed to \c onTextRenderInit.
     * 
     * For example, to list the CSS classes of all subtitle tracks in a file, include:
     * 
     * \code
     * public void onTextRenderInit(NexPlayer mp, int trackCount) {
     *     for(int i=0; i&lt;trackCount; i++) {
     *         Log.d(LOG_TAG, "ClassName[" + i + "] = " + mp.getSMIClassInfo(i));
     *     }
     * }    
     * \endcode
     * 
     * \note This is no longer supported; use \link NexContentInformation#mCaptionLanguages mCaptionLanguages\endlink instead.
     * 
     * @deprecated This is no longer supported; use \link NexContentInformation#mCaptionLanguages mCaptionLanguages\endlink instead.
     * 
     * \param nIndex
     *            The 0-based index of the track.
     *            
     * \return    The name of the CSS class for the specified index; undefined if the
     *              index is out of range.
     */
    public native String getSMIClassInfo( int nIndex ); 
    
    /**
     * \brief This function retrieves information on the content that is currently open.
     * 
     * \note <i>The \link NexPlayer#getContentInfoInt(int) getContentInfoInt\endlink function
     *       also returns information on the current content.  In some cases, the same information
     *       is available through both functions.  However, some items are available only through
     *       one of the functions.</i>
     * 
     * <b>PERFORMANCE NOTE:</b> This allocates a new instance of \c NexContentInformation
     * every time it is called, which may place a burden on the garbage collector in some cases.
     * If you need to access multiple fields, save the returned object in a variable. For cases
     * that are particularly sensitive to performance, selected content information is available
     * through \link NexPlayer#getContentInfoInt(int) getContentInfoInt\endlink, which doesn't allocate
     * any objects.
     * 
     * \return A \link NexContentInformation\endlink object containing information on the currently open content.
     *
     * \see \link NexPlayer#getContentInfoInt(int) getContentInfoInt\endlink
     */
    public NexContentInformation getContentInfo()
    {
        NexContentInformation info = new NexContentInformation();
        
        getInfo( info );
        
        return info;
    }
    
    /**
     * \brief Retrieves the specified content information item.  In most cases, this is equivalent
     *        to calling \link NexPlayer#getContentInfo() getContentInfo\endlink and accessing an individual
     *        field in the return value.  
     * 
     * However, there are a few items that are only available
     * through this method, and for items available through both methods, this one may
     * be more efficient in certain cases. See \c getContentInfo for more information.
     * 
     *   Certain fields (such as the list of tracks) are only
     * available through the full structure, and
     * certain fields (such as frames displayed per second) are only available
     * here.
     * 
     * <b>Content Info Indexes:</b> The following integer constants 
     * identify different content information items that are available; they
     * are passed in the \c info_index argument to specify which
     * content information item the caller is interested in.
     * 
     * <b>Also available in \c getContentInfo:</b>
     *  - <b>CONTENT_INFO_INDEX_MEDIA_TYPE (0)</b> Same as the \c mMediaType member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_MEDIA_DURATION (1)</b> Same as the \c mMediaDuration member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_VIDEO_CODEC (2)</b> Same as the \c mVideoCodec member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_VIDEO_WIDTH (3)</b> Same as the \c mVideoWidth member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_VIDEO_HEIGHT (4)</b> Same as the \c mVideoHeight member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_VIDEO_FRAMERATE (5)</b> Same as the \c mVideoFrameRate member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_VIDEO_BITRATE (6)</b> Same as the \c mVideoBitRate member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_AUDIO_CODEC (7)</b> Same as the \c mAudioCodec member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_AUDIO_SAMPLINGRATE (8)</b> Same as the \c mAudioSamplingRate member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_AUDIO_NUMOFCHANNEL (9)</b> Same as the \c mAudioNumOfChannel member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_AUDIO_BITRATE (10)</b> Same as the \c mAudioBitRate member of \c NexContentInfo  
     *  - <b>CONTENT_INFO_INDEX_MEDIA_ISSEEKABLE (11)</b> Same as the \c mIsSeekable member of \c NexContentInfo
     *  - <b>CONTENT_INFO_INDEX_MEDIA_ISPAUSABLE (12)</b> Same as the \c mIsPausable member of \c NexContentInfo
     * 
     * <b>Video Performance Information (Available only via \c getContentInfoInt):</b>
     * The NexPlayer&trade;&nbsp;engine reads frames from the content, then decodes and displays
     * each frame.  If the device is not powerful enough for the resolution or bitrate being played, the decoding or
     * display of some frames may be skipped in order to maintain synchronization with the audio track.  
     * 
     * The values of the parameters in this section provide information about the number of frames actually being displayed.
     * Per-second averages are calculated every two seconds (although this interval may change in future releases).
     * Frame counts reset at the same interval, so the ratio is generally more meaningful than the
     * actual numbers (since the interval may change).  Running totals are also provided, and are updated
     * at the same interval. 
     * 
     * If you wish to perform your own calculations or average over other intervals, you can
     * periodically sample the running totals.  Running totals are reset when new content is opened.
     *  - <b>CONTENT_INFO_INDEX_VIDEO_RENDER_AVE_FPS (13)</b> Average number of video frames per second decoded.
     * 
     * @param info_index    The integer index of the content information item to return.
     *                      This is one of the \c CONTENT_INFO_INDEX_* constants described
     *          above.
     *          
     * @return The integer value of the requested content information item.
     * 
     * @see \link NexPlayer#getContentInfo() getContentInfo\endlink
     * @see \link NexContentInformation \endlink
     */
    public native int getContentInfoInt( int info_index );
    
    /**
     * Sets the value of an individual NexPlayer&trade;&nbsp;property.
     * 
     * Properties control the behavior of NexPlayer&trade;&nbsp;and the features 
     * that are enabled.
     * 
     * This sets integer properties; use the \link NexPlayer#setProperty(NexProperty, String) setProperty(NexProperty, String)\endlink
     * version of this method for string properties.  If any properties need to be set before the player is opened, it is 
     * possible to call this method before calling \link NexPlayer#open open\endlink.
     * 
     * \see {@link NexProperty} for details.
     * 
     * \param property  The property to set.
     * \param value     The new value for the property.
     * 
     * \return          Zero if the property was succesfully set; non-zero if there was an error.
     */
    public int setProperty (NexProperty property, int value) {
        return setProperties( property.getPropertyCode(), value );
    }
    
    /**
     * \brief  Sets the value of an individual NexPlayer&trade;&nbsp;property.
     * 
     * Properties control the behavior of NexPlayer&trade;&nbsp;and the features 
     * that are enabled.
     * 
     * This sets string properties; use the \link NexPlayer#setProperty(NexProperty, int) setProperty(NexProperty, int)\endlink
     * version of this method for integer properties.  If any properties need to be set before the player is opened, it is 
     * possible to call this method before calling \link NexPlayer#open open\endlink.
     * 
     * \see {@link NexProperty} for details.
     * 
     * \param property  The property to set.
     * \param value     The new string value for the property.
     * 
     * \return          Zero if the property was succesfully set; non-zero if there was an error.
     */
    public int setProperty (NexProperty property, String value) {
        return setProperties( property.getPropertyCode(), value );
    }   
    
    /**
     * Gets the value of an individual NexPlayer&trade;&nbsp;integer property.
     * 
     * Properties control the behavior of NexPlayer&trade;&nbsp;and the features 
     * that are enabled.
     * 
     * This gets integer properties; for string properties, use
     * \link NexPlayer#getStringProperty(NexProperty) getStringProperty\endlink
     * instead.
     * 
     * \see {@link NexProperty} for details.
     * 
     * \param property  The property to get.
     * 
     * \return          The value of the property.
     */
    public int getProperty (NexProperty property) {
        return getProperties( property.getPropertyCode() );
    }
    
    /**
     * Gets the string value of an individual NexPlayer&trade;&nbsp;property.
     * 
     * Properties control the behavior of NexPlayer&trade;&nbsp;and the features 
     * that are enabled.
     * 
     * This gets string properties; for integer properties, use
     * \link NexPlayer#getProperty(NexProperty) getProperty\endlink
     * instead.
     * 
     * \see {@link NexProperty} for details.
     * 
     * \param property  The property to get.
     * 
     * \return          The string value of the property.
     */
    public String getStringProperty (NexProperty property) {
        return getStringProperties( property.getPropertyCode() );
    }   
    
    /**
     * Sets the value of an individual NexPlayer&trade;&nbsp;integer property based on the
     * numerical ID of the property.
     * 
     * Normally, \link NexPlayer#setProperty(NexProperty, int) setProperty\endlink should
     * be used instead of this method.  Use this method <i>only</i> if you have a numeric 
     * property code.
     * 
     * For a full list of properties, see the \link NexPlayer.NexProperty NexProperty\endlink
     * enum.  To get the numeric code for a property, call the \c getPropertyCode
     * method on the enum member.
     * 
     * For example:
     * \code
     * setProperties(
     *         NexProperty.SUPPORT_RTSP.getPropertyCode(),
     *         1  // enable RTSP support
     *         );
     * \endcode
     * 
     * \param property  The numeric property code identifying the property to set.
     * \param value     The new value for the property.
     * 
     * \return          Zero if the property was set successfully; non-zero 
     *                  if there was an error.
     */
    public native int setProperties( int property, int value );
    
    /**
     * Sets the value of an individual NexPlayer&trade;&nbsp;string property based on the
     * numerical ID of the property.
     * 
     * This is a string version of \link NexPlayer#setProperties(int, int) setProperties(int, int)\endlink.
     * 
     * Normally, \link NexPlayer#setProperty setProperty\endlink should
     * be used instead of this method.  Use this method <i>only</i> if you have a numeric 
     * property code.
     * 
     * \param property  The numeric property code identifying the property to set.
     * \param value     The new string value for the property.
     * 
     * \return         Zero if the property was set successfully; non-zero 
     *                  if there was an error.
     */
    public native int setProperties( int property, String value );  
    
    /**
     * Gets the value of an individual NexPlayer&trade;&nbsp;property based on the
     * numerical ID of the property.
     * 
     * Normally, \link NexPlayer#getProperty(NexProperty) getProperty\endlink should
     * be used instead of this method.  Use this method <i>only</i> if you have a numeric 
     * property code.
     * 
     * For a full list of properties, see the \link NexPlayer.NexProperty NexProperty\endlink
     * enum.  To get the numeric code for a property, call the \c getPropertyCode
     * method on the enum member.
     * 
     * For example:
     * \code
     * int supportRTSP = 
     *     getProperties(
     *         NexProperty.SUPPORT_RTSP.getPropertyCode() 
     *         );
     * \endcode
     * 
     * \param property  The numeric property code identifying the property to get.
     * 
     * \return          The value of the property.
     */
    public native int getProperties( int property );
    
    /**
     * Gets the string value of an individual NexPlayer&trade;&nbsp;property based on the
     * numerical ID of the property.
     * 
     * Normally, \link NexPlayer#getStringProperty(NexProperty) getStringProperty\endlink should
     * be used instead of this method.  Use this method <i>only</i> if you have a numeric 
     * property code.
     * 
     * For a full list of properties, see the \link NexPlayer.NexProperty NexProperty\endlink
     * enum.  To get the numeric code for a property, call the \c getPropertyCode
     * method on the enum member.
     * 
     * For example:
     * \code
     * String userAgent = 
     *     getProperties(
     *         NexProperty.USERAGENT_STRING.getPropertyCode() 
     *         );
     * \endcode
     * 
     * \param property  The numeric property code identifying the property to get.
     *
     * \return          The string value of the property.
     * 
     */
    public native String getStringProperties( int property );   
    
    /**
     * \brief   This function adds an RTSP header to be included with all future
     *          RTSP requests.
     * 
     * RTSP headers have the same format as HTTP headers,
     * but the set of field names is different.
     * 
     * There are several request types that are part of the RTSP protocol,
     * and when a header is added, you must specify with which request types
     * it will be included.  This is done by performing a bitwise \c OR on one
     * or more of the following values, and specifying the result in the 
     * \c methods parameter:
     *  - <b>RTSP_METHOD_DESCRIBE</b>
     *  - <b>RTSP_METHOD_SETUP</b>
     *  - <b>RTSP_METHOD_OPTIONS</b>
     *  - <b>RTSP_METHOD_PLAY</b>
     *  - <b>RTSP_METHOD_PAUSE</b>
     *  - <b>RTSP_METHOD_GETPARAMETER</b>
     *  - <b>RTSP_METHOD_TEARDOWN</b>
     *  - <b>RTSP_METHOD_ALL</b>
     *  
     * For example, to set a different user agent for the SETUP and PLAY requests:
     * 
     * \code
     * addRTSPHeaderFields( 
     *     RTSP_METHOD_SETUP | RTSP_METHOD_PLAY,
     *     "User-Agent: Nextreaming Android Player");
     * \endcode
     * 
     * \param methods   The set of request methods to which this will 
     *                  apply (RTSP_METHOD_* constants OR-ed together).
     * \param str       The actual header to add (including header name and value).
     * 
     * \return          Zero if successful, non-zero if there was an error.
     */
    public native int addRTSPHeaderFields( int methods, String str );
    
    /**
     * \brief   This function adds additional header fields to be sent along with the HTTP headers
     *          when sending streaming requests (HLS and Smooth Streaming).
     * 
     * The string should contain a single valid HTTP header, and should include the
     * header name, value, and delimiter.
     * 
     * For example: \code addHTTPHeaderFields("Cooki: Cooki test value."); \endcode
     * 
     * To add multiple header fields, simply call this function multiple times.
     * 
     * \param str   The header (including delimeter) to add to future HTTP requests.
     * 
     * \return      Zero if successful, non-zero if there was an error.
     */
    public native int addHTTPHeaderFields( String str);
    
    /**
     * This function controls the playback speed of content by the given percent.
     * This doesn't work if it is called when NexPlayer&trade;&nbsp;is stopped.
     * 
     * \param iPlaySeed
     *            This integer represents the percentage by which to change the playback speed.
     *            It must be in the range of -50 to 100. 
     */
    public native int playspeedcontrol( int iPlaySeed);
	

    /** One of the NexSound audio modes to be set by the \c uiAudioMode parameter in audioSetParam().    */
    public static final int NEX_AS_EARCOMFORT   			= 0x00000001;
    /** One of the NexSound audio modes to be set by the \c uiAudioMode parameter in audioSetParam().    */
    public static final int NEX_AS_REVERB					= 0x00000002;
    /** One of the NexSound audio modes to be set by the \c uiAudioMode parameter in audioSetParam().    */
    public static final int NEX_AS_STEREO_CHORUS 			= 0x00000003;
    /** One of the NexSound audio modes to be set by the \c uiAudioMode parameter in audioSetParam().    */
    public static final int NEX_AS_MUSIC_ENHANCER 			= 0x00000004;
    /** One of the NexSound audio modes to be set by the \c uiAudioMode parameter in audioSetParam().    */
    public static final int NEX_AS_CINEMA_SOUND 	= 0x00000006;


    /** 
     * \brief  This audio effect interface enhances sound on NexPlayer&trade;&nbsp;but is only available in some product categories.
     * 
     * The availability of each NexSound audio component can be checked by calling 
     * \link NexPlayer#getProperty(NexProperty) getProperty\endlink on the property related to the component 
     * to be checked, namely one of: 
     *              - <b>AS_EARCOMFORT_AVAILABILITY (0x00050002) </b>
     *              - <b>AS_REVERB_AVAILABILITY (0x00050003) </b>
     *              - <b>AS_STEREO_CHORUS_AVAILABILITY (0x00050004)</b>
     *              - <b>AS_MUSIC_ENHANCER_AVAILABILITY (0x00050005)</b>
     *              - <b>AS_CINEMA_SOUND_AVAILABILITY (0x00050006)</b>
     * 
     * \param uiAudioMode       The NexSound mode to set.  This is an integer and will be one of:
     *                            - <b> NEX_AS_EARCOMFORT = 0x00000001 </b>:  EarComfort mode moves the sound image to
     *                                  a position outside of the listener's head, simulating the more comfortable feeling
     *                                  of listening to speakers but through earphones. \n 
     *                                  <b>Default Values</b>:
     *                                     - \c uiEffectStrength = 2
     *                                     - \c uiBassStrength = 3
     *                            - <b> NEX_AS_REVERB = 0x00000002 </b>: Reverb mode adds reverb to audio. \n
     *                                  <b>Default Values</b>:
     *                                     - \c uiEffectStrength = 3
     *                                     - \c uiBassStrength = 3
     *                            - <b> NEX_AS_STEREO_CHORUS = 0x00000003 </b>:  Stereo Chorus mode. \n
     *                                  <b>Default Values</b>:
     *                                     - \c uiEffectStrength = 5
     *                                     - \c uiBassStrength = 3
     *                            - <b> NEX_AS_MUSIC_ENHANCER = 0x00000004 </b>: Music Enhancer mode.\n
     *                                  <b>Default Values</b>:
     *                                     - \c uiEffectStrength = 6
     *                                     - \c uiBassStrength = 5
     *                            - <b> NEX_AS_CINEMA_SOUND = 0x00000006</b>:  Virtual surround sound effect on ordinary
     *                                  earphones.  This mode can only be turned ON and OFF and parameters \c uiEffectStrength and 
     *                                  \c uiBassStrength do not apply.
     * \param uiEffectStrength  This sets the strength of the audio mode selected.  It is an integer between 0 and 6. 
     * \param uiBassStrength    This sets the bass strength of the audio mode selected.  It is an integer between 0 and 6.
     * 
     * \returns  Zero if successful, or a non-zero error code, including:
     *              - <b>NOT_SUPPORT (0x8000000FL)</b>: The requested NexSound audio mode is not supported in this version.  
     */
    public native int audioSetParam( int uiAudioMode, int uiEffectStrength, int uiBassStrength);

    
    /**
     * \brief  Turns the Auto Volume feature \c on or \c off, but this feature is only available in some product categories.
     * 
     * When Auto Volume is turned \c on, NexPlayer&trade;&nbsp;automatically adjusts the volume level of different content
     * so that it is played at a consistent and optimal volume level, allowing the user to play different content without having to constantly adjust
     * the volume when new content starts.
     * 
     * By default, Auto Volume is turned \c off (identical to the behavior of the player in product categories that do 
     * not support this feature).
     * 
     * \param uiOnOff  This turns the Auto Volume feature \c on and \off.  By default, this feature is \c off = 0. \n
     *                 <b>Possible Values:</b>  
     *                      - \c on = 1
     *                      - \c off = 0
     * 
     * \returns  The new value of Auto Volume.  If Auto Volume was turned \c off, this will be 0. \n
     *           If Auto Volume was turned \c on, it will return 1. \n
     *           If Auto Volume is not supported in this version of the NexPlayer&trade;, this will return
     *           the error, <b>NOT_SUPPORT (0x8000000FL)</b>.
     *           
     * \since version 5.10
     */
    
    public native int setAutoVolume(int uiOnOff);
    

    /** Possible \c return value for NexPlayer.GetRenderMode */
    public static final int NEX_USE_RENDER_AND      = 0x00000002;
    /** Possible \c return value for NexPlayer.GetRenderMode */
    public static final int NEX_USE_RENDER_JAVA     = 0x00000010;
    /** Possible \c return value for NexPlayer.GetRenderMode */
    public static final int NEX_USE_RENDER_OPENGL   = 0x00000020;
    
    /**
     * \brief Sets a bitmap to be used to receive rendered frames for display, when
     * using the Java-based renderer.
     * 
     * For more information, see the <i>\ref javarenderer</i> section of
     * the NexPlayer&trade;&nbsp;Engine documentation.
     * 
     * \param mFrameBitmap  The bitmap to receive rendered frames (when using Java-based renderer).
     * 
     * \return    Always 0, but may change in future versions.  The return value should be ignored.
     */
    public native int SetBitmap(Object mFrameBitmap);
    
    /**
     * \brief Informs NexPlayer&trade;&nbsp;of the current size of
     *        the GLSurfaceView subclass instance.
     *
     * This should be called whenever the size of the GLSurfaceView subclass
     * instance changes, as well as when the instance is initially created.  This
     * is because internally, OpenGL APIs use a different coordinate system, and
     * NexPlayer&trade;&nbsp;must know the pixel dimensions in order to map the OpenGL
     * coordinate system to per-pixel coordinates.
     *
     * \param width     Width of GLSurfaceView subclass instance, in pixels.
     * \param height    Height of GLSurfaceView subclass instance, in pixels.
     *
     * \returns Always 0, but may change in future versions.  The return value should be ignored.
     */
    public native int GLInit(int width, int height);
    
    /**
     * \brief Draws in the current OpenGL context.
     *
     * \deprecated  This method supports legacy code but should not be called by new code. 
     *              Instead use the GLRenderer class.
     * 
     * This remains public to support legacy code that implemented a GLSurfaceView
     *              subclass directly. However, new code should not call this method.  Instead,
     * simply use the GLRenderer class provided with the NexPlayer&trade;&nbsp;SDK. That
     *              class automatically calls GLDraw when needed.
     *
     * \warning This <em>must</em> be called from the OpenGL renderer thread
     *          (the thread where \c GLSurfaceView.Renderer.onDrawFrame is called).
     *          Calling this from anywhere else will result in undefined behavior
     *          and possibly cause the application to crash.
     *
     * \param mode     The type of drawing operation to perform.
     *                  - <b>0:</b> Draw the most recent video frame.
     *                  - <b>1:</b> Erase the surface to black.
     * \returns Always zero, but may change in future versions.  The return value should be ignored.
     */
    public native int GLDraw(int mode);
    
    /**
     * \brief Returns the type of renderer in use by the NexPlayer&trade;&nbsp;engine.
     *
     * You must check the render mode using this method and adjust
     * the application behavior appropriately. For details see  
     * \ref javarenderer or \ref glrenderer.
     * 
     * When using the Java renderer (\c NEX_USE_RENDERER_JAVA ), the application
     * must NOT call \c setOutputPos or \c setDisplay.  Doing so may
     * cause the application to crash if running under Honeycomb.
     *
     * When using the OpenGL renderer (\c NEX_USE_RENDERER_OPENGL ), the
     * application must NOT call \c setDisplay.
     *
     * @return Render mode; one of:
     * - <b> \link NexPlayer::NEX_USE_RENDER_AND NEX_USE_RENDER_AND\endlink</b>
     *          Using only standard Android API bitmaps 
     *          to display frames.
     * - <b> \link NexPlayer::NEX_USE_RENDER_JAVA NEX_USE_RENDER_JAVA\endlink</b>
     *          Don't render to the display.  Instead, each 
     *          frame is decoded and converted to the appropriate 
     *          color space, and then sent to the applicaton to display.
     * - <b> \link NexPlayer::NEX_USE_RENDER_OPENGL NEX_USE_RENDER_OPENGL\endlink</b>
     *          Using OpenGL ES 2.0 to display frames.
     */
    public native int GetRenderMode();
    
    /**
     * \brief Specifies the path to the renderer configuration file.
     *
     * The renderer configuration file defines which combinations of
     * codec and device should make use of which available renderer.  The configuration
     * file is provided with the SDK, but it is the responsibility of the app
     * developer to include the file with the application, and specify the path
     * using this method.
     *
     * The path must be specified before opening any content, otherwise the
     * renderer configuration file will not be used, and the player will choose the renderer
     * based on the version of Android OS alone without regard to the device model.
     *
     * \param strConfPath   The path to the configuration file.
     *
     * \returns Always zero, but may change in future versions.  The return value should be ignored.
     */
    public native int SetConfigFilePath(String strConfPath);
    
    /**
     * \brief  This method allows NexPlayer&trade;&nbsp;to adjust the contrast and brightness of the 
     *         displayed content.
     *         
     *  These values can be adjusted either from within the code itself or can be set by the user interface.
     *  If setting the values within code, it is important to stay <b>within</b> the given range of each parameter.  
     *  Values outside of these ranges will be ignored and the existing value will be retained, but unexpected results
     *  could also be potentially produced.
     *  
     *  These settings can be continuously adjusted by calling the method multiple times.
     * 
     *  \param  Contrast    This adjusts the contrast of the display.  It is an integer from 0 to 255.
     *                      By default, this value is set to 128.
     *  \param  Brightness  This adjusts the brightness of the display.  It is an integer from -127 to +128.
     *                      By default, this value is set to 0.
     *  
     *  \returns  Always zero, but may change in future versions.  The return value should be ignored.
     * 
     */
    
    public native int SetContrastBrightness(int Contrast, int Brightness);
    
    /** Possible value for arguments to {@link NexPlayer.setMediaStream()}.*/
    public static final int MEDIA_STREAM_DEFAULT_ID     = 0xFFFFFFFF;
    
    /** Possible value for {@link NexStreamInformation.mType}; see there for details.*/
    public static final int MEDIA_STREAM_TYPE_AUDIO     = 0x00;
    /** Possible value for {@link NexStreamInformation.mType}; see there for details.*/
    public static final int MEDIA_STREAM_TYPE_VIDEO     = 0x01;
    /** Possible value for {@link NexStreamInformation.mType}; see there for details.*/
    public static final int MEDIA_STREAM_TYPE_TEXT      = 0x02;
    
    /**
     * \brief    For media with multiple streams, this method selects the streams that will be presented
     *           to the user.
     * 
     * The full list of available streams (if any) can be found in
     * the \link NexContentInformation#mArrStreamInformation mArrStreamInformation\endlink
     * array in NexContentInformation.  Each stream is either an audio stream or a video stream, and one of each may be
     * selected for presentation to the user.  Please see \ref multiAV "Multi-Audio and Multi-Video Stream Playback" for more explanation.
     * 
     * Streams like in Smooth Streaming may in turn have associated custom attributes.  Custom attributes
     * limit playback to a subset of tracks within the stream.  Custom attributes are
     * key/value pairs.  Each possible pairing (from all the tracks in a stream) is
     * listed in \link NexStreamInformation#mArrCustomAttribInformation mArrCustomAttribInformation\endlink
     * along with an associated integer ID.  Specifying that particular integer ID causes 
     * only tracks with that particular key/value pairing to beused.  Only one ID may be
     * specified at any given time.
     * 
     * \param iAudioStreamId        
     *             The ID of the stream to use for audio.  
     *             If this is <b>MEDIA_STREAM_DEFAULT_ID</b>, the initial
     *             audio stream played will continue to be used.
     * 
     * \param iTextStreamId         
     *             The ID of the stream to use for text (subtitles, captions, and so on).  
     *             If this is <b>MEDIA_STREAM_DEFAULT_ID</b>, the initial
     *             text stream played will continue to be used. 
     * 
     * \param iVideoStreamId        
     *             The ID of the stream to use for video.  
     *             If this is <b>MEDIA_STREAM_DEFAULT_ID</b>, the initial
     *             video stream played will continue to be used.
     * \param iVideoCustomAttrId    
     *             The ID of the custom attribute to use.  If this
     *             is <b>MEDIA_STREAM_DEFAULT_ID</b>, 
     *             the default custom attribute will be used.  If no
     *             custom attributes are associated with a stream, this will be undefined.
     *             
     * \return   Zero if successful, non-zero if there was an error.
     * 
     */
    public native int setMediaStream(int iAudioStreamId, int iTextStreamId, int iVideoStreamId, int iVideoCustomAttrId);
    
    /**
     *  \brief  This sets the maximum bandwidth for streaming playback.  
     * 
     * This applies in
     * cases where there are multiple tracks at different bandwidths (such as
     * in the case of HLS or Smooth Streaming).  The player will not consider
     * any track over the maximum bandwidth when determining whether a track 
     * change is appropriate, even if it detects more bandwidth available.
     * 
     * \param iBandWidth    Maximum bandwidth in kbps (kilobits per second).
     * 
     * \return              Zero if successful, non-zero if there was an error.
     */
    public native int changeMaxBandWidth(int iBandWidth);
    
    /** This is a possible value for the \c methods parameter of {@link NexPlayer#addRTSPHeaderFields(int, String) addRTSPHeaderFields}.  See that method for details. */
    public static int RTSP_METHOD_DESCRIBE      = 0x00000001;
    /** This is a possible value for the \c methods parameter of {@link NexPlayer#addRTSPHeaderFields(int, String) addRTSPHeaderFields}.  See that method for details. */
    public static int RTSP_METHOD_SETUP         = 0x00000002;
    /** This is a possible value for the \c methods parameter of {@link NexPlayer#addRTSPHeaderFields(int, String) addRTSPHeaderFields}.  See that method for details. */
    public static int RTSP_METHOD_OPTIONS       = 0x00000004;
    /** This is a possible value for the \c methods parameter of {@link NexPlayer#addRTSPHeaderFields(int, String) addRTSPHeaderFields}.  See that method for details. */
    public static int RTSP_METHOD_PLAY          = 0x00000008;
    /** This is a possible value for the \c methods parameter of {@link NexPlayer#addRTSPHeaderFields(int, String) addRTSPHeaderFields}.  See that method for details. */
    public static int RTSP_METHOD_PAUSE         = 0x00000010;
    /** This is a possible value for the \c methods parameter of {@link NexPlayer#addRTSPHeaderFields(int, String) addRTSPHeaderFields}.  See that method for details. */
    public static int RTSP_METHOD_GETPARAMETER  = 0x00000020;
    /** This is a possible value for the \c methods parameter of {@link NexPlayer#addRTSPHeaderFields(int, String) addRTSPHeaderFields}.  See that method for details. */
    public static int RTSP_METHOD_TEARDOWN      = 0x00000040;
    /** This is a possible value for the \c methods parameter of {@link NexPlayer#addRTSPHeaderFields(int, String) addRTSPHeaderFields}.  See that method for details. */
    public static int RTSP_METHOD_ALL           = ( RTSP_METHOD_DESCRIBE 
                                                   | RTSP_METHOD_SETUP
                                                   | RTSP_METHOD_OPTIONS 
                                                   | RTSP_METHOD_PLAY
                                                   | RTSP_METHOD_PAUSE 
                                                   | RTSP_METHOD_GETPARAMETER
                                                   | RTSP_METHOD_TEARDOWN );
    
    /**
     * \brief  This method determines the amount of currently buffered data.
     * 
     * It returns the amount of data that has been buffered ahead of the current playing position.  This is useful
     * to know in cases when it is possible to seek in (for example) a progressive
     * download without needing to buffer.
     * 
     * \return The number of milliseconds (1/1000 sec) of media that has been buffered ahead.
     * 
     */
    public native int getBufferStatus();
    
    private native int prepareSurface(int surfacetype);
    
    
    /** This is a possible value for the \c iFlag parameter of {@link NexPlayer#setRenderOption(int) setRenderOption}.  See that method for details. */
    public static final int RENDER_MODE_VIDEO_NONE  =           0x00000000;
    /** This is a possible value for the \c iFlag parameter of {@link NexPlayer#setRenderOption(int) setRenderOption}.  See that method for details. */
    public static final int RENDER_MODE_VIDEO_FILTERBITMAP =    0x00000001;
    /** This is a possible value for the \c iFlag parameter of {@link NexPlayer#setRenderOption(int) setRenderOption}.  See that method for details. */
    public static final int  RENDER_MODE_VIDEO_DITHERING =      0x00000002;
    /** This is a possible value for the \c iFlag parameter of {@link NexPlayer#setRenderOption(int) setRenderOption}.  See that method for details. */
    public static final int  RENDER_MODE_VIDEO_ANTIALIAS =      0x00000004;
    /** This is a possible value for the \c iFlag parameter of {@link NexPlayer#setRenderOption(int) setRenderOption}.  See that method for details. */
    public static final int  RENDER_MODE_VIDEO_ALLFLAG =        0xFFFFFFFF;
    
    /**
     * \brief  This function configures the paint flags used with the Android bitmap rendering module.
     * 
     * There are multiple rendering modules that can be used for displaying video and
     * NexPlayer&trade;&nbsp;automatically selects the best one for the given content and device.
     * 
     * \see NexPlayer.init for more details on possible rendering modules.
     * 
     * In the case where the rendering
     * module uses bitmaps provided through the Android API, the rendering options specified here are
     * used to set up the flags on the \c android.os.Paint object that is used to display the bitmap.
     * 
     * For all other rendering modules, the values set here are ignored.
     * 
     * \param iFlag
     *            This is an integer representing options for video rendering. This can be zero or more of the following values, combined
     *            together using a bitwise \c OR.  Each value corresponds to an Android API flag available
     *            on a Paint object.
     *             - <b>{@link NexPlayer#RENDER_MODE_VIDEO_NONE RENDER_MODE_VIDEO_NONE} (0x00000000)</b>
     *                      No special paint flags are set.
     *             - <b>{@link NexPlayer#RENDER_MODE_VIDEO_FILTERBITMAP RENDER_MODE_VIDEO_FILTERBITMAP} (0x00000001)</b>
     *                      Corresponds to \c android.os.Paint.FILTER_BITMAP_FLAG.
     *             - <b>{@link NexPlayer#RENDER_MODE_VIDEO_ANTIALIAS RENDER_MODE_VIDEO_ANTIALIAS} (0x00000002)</b>
     *                      Corresponds to \c android.os.Paint.ANTI_ALIAS_FLAG.
     *             - <b>{@link NexPlayer#RENDER_MODE_VIDEO_DITHERING RENDER_MODE_VIDEO_DITHERING} (0x00000004)</b>
     *                      Corresponds to \c android.os.Paint.DITHER_FLAG.
     *             - <b>{@link NexPlayer#RENDER_MODE_VIDEO_ALLFLAG RENDER_MODE_VIDEO_ALLFLAG} (0xFFFFFFFF)</b>
     *                      Enables all options.
     *  
     * \return Always zero, but may change in future versions; the return value should be ignored.
     */
    public native int setRenderOption(int iFlag);
    
    /**
     * \brief This method sets the output video's position and size.
     * 
     * The position is relative to and within the surface specified in \link NexPlayer#setDisplay(SurfaceHolder) setDisplay\endlink
     * or relative to and within the application's OpenGL surface, if the OpenGL renderer is being used.
     * 
     * X and Y are the distances from the upper-left corner.  All units are in pixels and are resolution-dependent.
     * 
     * If the video is larger than the surface, or part of it is outside the surface, it will be cropped
     * appropriately.  Negative values are therefore acceptable for iX and iY.
     * 
     * \param iX
     *            The video display's horizontal (X) position.
     * \param iY
     *            The video display's vertical (Y) position.
     * \param iWidth
     *            The width of the video display.
     * \param iHeight
     *            The height of the video display.
     *                        
     * \return Always zero, but may change in future versions; the return value should be ignored.
     * 
     */
    public native int setOutputPos(int iX, int iY, int iWidth, int iHeight);
    
    /** 
     * \brief  This method turns video rendering on or off.
     * 
     * If video rendering is turned off, any existing frame will
     * remain on the display.  If you wish to clear it, you may
     * directly draw to the surface and fill it with black pixels
     * after turning off video rendering.
     * 
     * \param bOn  \c true to render video, \c false to turn off video rendering.
     * 
     */
    public void videoOnOff(boolean bOn) {
        videoOnOff(bOn?1:0,0);
    }
    
    /**
     * \brief This method turns video rendering on or off.
     * 
     * \warning This method is deprecated.  Use of \link NexPlayer#videoOnOff(boolean) videoOnOff(boolean)\endlink
     *          is recommended over of this function.
     * \deprecated Use videoOnOff(boolean) instead of this method.
     * 
     * \param bOn
     *            1 to turn on video rendering, 0 to turn off video rendering. Other values
     *            are reserved and should not be used.
     * \param bErase
     *            This parameter is reserved; it must be zero.
     *            
     * @return Always zero, but may change in future versions; the return value should be ignored.
     */
    public native int videoOnOff(int bOn, int bErase);
    
    private native int prepareAudio(int audiotype);

    /**
     * \brief  This sets the player output volume.
     *
     * This affects the output of the player before it is mixed with other sounds.  
     * Normally, this should be left at the default setting of 1.0, and the volume 
     * should be adjusted via the device master volume setting (adjustable by the 
     * the user via the hardware volume buttons on the device).  However, if the
     * application contains multiple audio sources (or if there is other audio being
     * played on the device), this property can be used to reduce the NexPlayer&trade;&nbsp;
     * volume in relation to other sounds.
     *
     * The valid range for this property is 0.0 ~ 1.0.  A value of 0.0 will silence
     * the output of the player, and a value of 1.0 (the default) plays the audio at
     * the original level, affected only by the device master volume setting (controlled
     * by the hardware buttons).
     *
     * It is not recommended to use this setting for volume controlled by the user; that
     * is best handled by adjusting the device master volume.
     * 
     * \param fGain  This is a \c float between 0.0 and 1.0.  It is set to 1.0 by default.
     * 
     */
    public native int setVolume(float fGain);
    
    /**
     * \brief Selects the caption (subtitle) track that will be used. 
     * 
     * Subtitles for the selected track will be passed to 
     * \link IListener#onTextRenderRender onTextRenderRender\endlink
     * for display.
     *
     * This is used for file-based captions only.  For streaming media with included
     * captions, \c setMediaStream() should be used instead, and local captions should
     * be turned off since running both types of captions at the same time has undefined
     * results.
     * 
     * \param indexOfCaptionLanguage  
     *          An index into the \link NexContentInformation#mCaptionLanguages mCaptionLanguages\endlink
     *          array specifying which language to use.  If there are <b> \c n </b> entries 
     *          in the caption array, then you may pass \c 0...n-1 to 
     *          specify a single language, \c n to specify all languages,
     *          and \c n+1 to turn off captions. 
     * 
     * \return
     *          Zero if successful, non-zero if there was an error.
     */
    public native int setCaptionLanguage(int indexOfCaptionLanguage);
    
    
    /**
     * \brief This function begins capturing video frames.  
     *
     * This may be used to capture a single frame immediately, or to capture a series of frames at
     * regular intervals. In either case, the captured frames are actually sent to the
     * \link IListener#onVideoRenderCapture(NexPlayer, int, int, int, Object) onVideoRenderCapture\endlink
     * handler, so your application must implement it to receive the frames.
     * 
     * When this function is called, the current frame will immediately be
     * sent to \c onVideoRenderCapture, and then any scheduled frames
     * will be sent after the specified interval has elapsed.
     * 
     * @param iCount
     *              The number of frames to capture; this should be at least 1 or the
     *              method has no effect.
     * 
     * @param iInterval
     *              If \c iCount is greater than 1, this is the number of milliseconds to
     *              wait between captures.  For example, if \c iCount is 3 and \c iInterval
     *              is 100, then one frame will be captured immediately, another after 1/10sec, and
     *              a third after a further 1/10sec.
     * @return          
     *              Zero if successful, non-zero if there was an error.
     */
    public native int captureVideo( int iCount, int iInterval );
    
    /**
     * \brief   Gets NexPlayer&trade;&nbsp;SDK version information.
     * 
     * The return value is an integer; the meaning is based on the 
     * \c mode argument passed.
     * 
     * Generally, the components of the version are assembled as follows:
     * 
     * \code
     * String versionString = nexPlayer.getVersion(0) + "." +
     *                        nexPlayer.getVersion(1) + "." +
     *                        nexPlayer.getVersion(2) + "." +
     *                        nexPlayer.getVersion(3);
     * \endcode
     * 
     * \param mode
     *              Version information to return.  This must be one of the following values (other
     *              values are reserved and should not be used):
     *                  - 0 : Major version
     *                  - 1 : Minor version
     *                  - 2 : Patch version
     *                  - 3 : Build version
     * 
     * \return   Requested version information (see \c mode above).
     */
    public native int   getVersion(int mode);   
    
    
    /**
     * \brief This function creates the NexPlayer&trade;&nbsp;engine.
     * 
     * @param nexplayer_this
     *            The CNexPlayer instance pointer.
     * @param strPackageName
     *            The application package name. (ex. com.nextreaming.nexplayersample)
     * @param strLibPath
     *            The library path for NexPlayer&trade;. 
     * @param sdkInfo
     *            The Android SDK version.  
     *              - 0x15 : SDK version 1.5 CUPCAKE
     *              - 0x16 : SDK version 1.6 DONUT
     *              - 0x21 : SDK version 2.1 ECLAIR
     *              - 0x22 : SDK version 2.2 FROYO
     * @param cpuInfo
     *            The cpuVersion.
     *               - 4 : armv4
     *               - 5 : armv5
     *               - 6 : armv6
     *               - 7 : armv7
     * @param strDeviceModel
     *            The device model name.
     * \param strDeviceRenderMode
     *            The rendering mode.  This will be one of:
     *                          - NEX_DEVICE_USE_ONLY_ANDROID
     *                          - NEX_DEVICE_USE_JAVA
     *                          - NEX_DEVICE_USE_OPENGL
     *                          - NEX_DEVICE_USE_ANDROID_3D 
     *            
     * @param logLevel
     *            NexPlayer&trade;&nbsp;SDK log display level.
     * @param pixelFormat
     *            The pixel format to use when using the Java renderer. 
     *            For more information, see the <i>Java Renderer</i> section of
     *            \link com.nextreaming.nexplayerengine nexplayerengine\endlink.
     *              - <b>1:</b> RGBA 8888
     *              - <b>4:</b> RGB 565
     *  
     */
    
    private native final int    _Constructor( Object nexplayer_this, String strPackageName, String strLibPath, int sdkInfo, int cpuInfo, String strDeviceModel, String strDeviceRenderMode, int logLevel, int pixelFormat );    
    
    /**
     * This function releases the NexPlayer&trade;&nbsp;engine.
     */
    private native void         _Release();
    
    @SuppressWarnings("unused")  // This called from the native code, so it is actually used
    private static void callbackFromNative( Object nexplayer_ref, int what, int arg1,
                                           int arg2, int arg3,int arg4, Object obj )
    {
        @SuppressWarnings("unchecked") // The type cast to WeakReference<NexPlayer> is always safe because 
        // this function is only called by known native code that always
        // passes an object of this type.
        NexPlayer nexplayer = (NexPlayer)( (WeakReference<NexPlayer>)nexplayer_ref ).get();
        if ( nexplayer == null )
        {
            // Log.w(TAG, "callbackFromNative returns null");
            return;
        }
        
        switch ( what )
        {
            case NEXPLAYER_EVENT_NOP:
                
                return;
            case NEXPLAYER_EVENT_ENDOFCONTENT:
                mListener.onEndOfContent( nexplayer );
                break;
            case NEXPLAYER_EVENT_STARTVIDEOTASK:
                mListener.onStartVideoTask( nexplayer );
                break;
            case NEXPLAYER_EVENT_STARTAUDIOTASK:
                mListener.onStartAudioTask( nexplayer );
                break;
            case NEXPLAYER_EVENT_TIME:
                mListener.onTime( nexplayer, arg1);
                break;
            case NEXPLAYER_EVENT_ERROR:
                mListener.onError( nexplayer, NexErrorCode.fromIntegerValue(arg1));
                break;
            case NEXPLAYER_EVENT_STATECHANGED:
                mListener.onStateChanged( nexplayer, arg1, arg2 );
                break;
            case NEXPLAYER_EVENT_SIGNALSTATUSCHANGED:
                mListener.onSignalStatusChanged( nexplayer, arg1, arg2 );
                break;
            case NEXPLAYER_EVENT_ASYNC_CMD_COMPLETE:
                Log.w(TAG, "NEXPLAYER_EVENT_ASYNC_CMD_COMPLETE :" + arg1 +", " + arg2 + ", " + arg3);
                mListener.onAsyncCmdComplete( nexplayer, arg1, arg2, arg3, arg4 );
                break;
            case NEXPLAYER_EVENT_RTSP_COMMAND_TIMEOUT:
                Log.w(TAG, "NEXPLAYER_EVENT_RTSP_COMMAND_TIMEOUT" );
                mListener.onRTSPCommandTimeOut( nexplayer );
                break;
            case NEXPLAYER_EVENT_PAUSE_SUPERVISION_TIMEOUT:
                Log.w(TAG, "NEXPLAYER_EVENT_PAUSE_SUPERVISION_TIMEOUT" );
                mListener.onPauseSupervisionTimeOut( nexplayer );
                break;
            case NEXPLAYER_EVENT_DATA_INACTIVITY_TIMEOUT:
                Log.w(TAG, "NEXPLAYER_EVENT_DATA_INACTIVITY_TIMEOUT" );
                mListener.onDataInactivityTimeOut( nexplayer );
                break;  
            case NEXPLAYER_EVENT_BUFFERINGBEGIN:
                mListener.onBufferingBegin( nexplayer );
                break;
            case NEXPLAYER_EVENT_BUFFERINGEND:
                mListener.onBufferingEnd( nexplayer );
                break;
            case NEXPLAYER_EVENT_BUFFERING:
                mListener.onBuffering( nexplayer, arg1 );
                break;
            case NEXPLAYER_EVENT_AUDIO_RENDER_CREATE:
                Log.v( TAG, "[CB] Audio render create !! " + arg1 );
                nexplayer.setAudioTrack(arg1, arg2);
                mListener.onAudioRenderCreate( nexplayer, arg1, arg2);
                break;
            case NEXPLAYER_EVENT_AUDIO_RENDER_DELETE:
                Log.v( TAG, "[CB] Audio render delete !! " );
                nexplayer.releaseAudioTrack();
                mListener.onAudioRenderDelete( nexplayer );
                Log.v( TAG, "[CB] Audio render delete Done!! ");
                break;
            case NEXPLAYER_EVENT_VIDEO_RENDER_CREATE:
                Log.v( TAG, "[CB] Video render create !! " + arg1 );
                mListener.onVideoRenderCreate( nexplayer, arg1, arg2, obj );
                break;
            case NEXPLAYER_EVENT_VIDEO_RENDER_DELETE:
                Log.v( TAG, "[CB] Video render delete !! " );
                mListener.onVideoRenderDelete( nexplayer);
                Log.v( TAG, "[CB] Video render delete Done!! " + arg1 );
                break;
            case NEXPLAYER_EVENT_VIDEO_RENDER_CAPTURE:
                Log.v( TAG, "[CB] Video render capture !! " );
                mListener.onVideoRenderCapture(nexplayer, arg1, arg2,arg3, obj );
                break;
            case NEXPLAYER_EVENT_VIDEO_RENDER_RENDER:
                //Log.v( TAG, "[CB] Video render render !! " + arg1 );//msg.arg1 );
                //this function is only used when java renderer working
                mListener.onVideoRenderRender( nexplayer );
                break;
            case NEXPLAYER_EVENT_TEXT_RENDER_INIT:
                Log.v( TAG, "[CB] Text render init !! " + arg1);//msg.arg1 );
                mListener.onTextRenderInit( nexplayer, arg1 );
                break;          
            case NEXPLAYER_EVENT_TEXT_RENDER_RENDER:
                Log.v( TAG, "[CB] Text render render !! " + " index : " + arg2);//msg.arg1 );
                mListener.onTextRenderRender( nexplayer, arg2, (NexClosedCaption)obj );
                break;      
            case NEXPLAYER_EVENT_TIMEDMETA_RENDER_RENDER:
                mListener.onTimedMetaRenderRender( nexplayer,(NexID3TagInformation)obj );
            	break;
            case NEXPLAYER_EVENT_STATUS_REPORT:     
                Log.v( TAG, "[CB] Status Report !! msg :" + arg1 + "   param1 : " + arg2);
                mListener.onStatusReport( nexplayer, arg1, arg2);
                /*case NEXPLAYER_EVENT_DEBUGINFO:       
                 Log.v( TAG, "[CB] Debug Info !! msg :" + arg1 + "   param1 : " + arg2);
                 mListener.onDebugInfo( nexplayer, arg1, (String)obj);*/
            default:
                //Log.e( TAG, "Unknown message type " + msg.what );
                return;
        }
    }
    @SuppressWarnings("unused") // Actually used (called from native code)
    private static int callbackFromNativeRet( Object nexplayer_ref, int what, int arg1,
                                             int arg2, Object obj )
    {
        int nRet = 0;
        @SuppressWarnings("unchecked") // Because the object handle is from known native code, the type is guaranteed
        NexPlayer nexplayer = (NexPlayer)( (WeakReference<NexPlayer>)nexplayer_ref ).get();
        if ( nexplayer == null )
        {
            //Log.e( TAG, "NexPlayer is NULL!!" );
            return -1;
        }
        
        
        switch ( what )
        {
                
            default:
                //Log.e( TAG, "Unknown message type " + msg.what );
                break;
        }
        
        return 0;
    }

    @SuppressWarnings("unused") // Actually used (called from native code)
    private static void callbackFromNative2( Object nexplayer_ref, int what, int arg1,
                                           int arg2, int arg3, long arg4, long arg5, Object Obj)
    {


        @SuppressWarnings("unchecked") // The type cast to WeakReference<NexPlayer> is always safe because 
        // this function is only called by known native code that always
        // passes an object of this type.
        NexPlayer nexplayer = (NexPlayer)( (WeakReference<NexPlayer>)nexplayer_ref ).get();
        if ( nexplayer == null )
        {
            // Log.w(TAG, "callbackFromNative returns null");
            return;
        }

        Log.d(TAG, "callbackFromNative2  [what : " + what + "] "
        								+"[arg1 : " + arg1 + "] "
        								+"[arg2 : " + arg2 + "] "
        								+"[arg3 : " + arg3 + "] "
        								+"[arg4 : " + arg4 + "] "
        								+"[arg5 : " + arg5 + "] ");
        
        switch ( what )
        {
        case NEXDOWNLOADER_EVENT_ERROR:
        	mListener.onDownloaderError(nexplayer, arg1, arg2);
        	break;
        case NEXDOWNLOADER_EVENT_ASYNC_CMD_BASEID:
        	mListener.onDownloaderAsyncCmdComplete(nexplayer, arg1, arg2, arg3);
        	break;
        case NEXDOWNLOADER_EVENT_COMMON_DOWNLOAD_BEGIN:
        	mListener.onDownloaderEventBegin(nexplayer, arg2, arg3);
        	break;
        case NEXDOWNLOADER_EVENT_COMMON_DOWNLOAD_PROGRESS:
            mListener.onDownloaderEventProgress(nexplayer, arg2, arg3, arg4, arg5);
        	break;
        case NEXDOWNLOADER_EVENT_COMMON_DOWNLOAD_COMPLETE:
            mListener.onDownloaderEventComplete(nexplayer, arg1);
        	break;
        case NEXDOWNLOADER_EVENT_COMMON_STATE_CHANGED:
        	mListener.onDownloaderEventState(nexplayer, arg1, arg2);
        	break;
       	default :
       		break;
        }
    }
    
    /**
     * \brief This function sets the Android AudioTrack object that NexPlayer&trade;&nbsp;will output rendered audio to.
     * 
     * This is normally called from {@link NexPlayer.IListener#onAudioRenderCreate(NexPlayer, int, int) onAudioRenderCreate}
     * after creating the {@link android.media.AudioTrack} object.<p>
     * 
     * The audio track object must have the format <code>ENCODING_PCM_16BIT</code> and the mode 
     * <code>MODE_STREAM</code>.  The sampling rate must match that passed on <code>onAudioRenderCreate</code>
     * and the channel configutation must match the number of channels passed to <code>onAudioRenderCreate</code>.<p>
     * 
     * If you swap out the audio track for another one, you must call the <code>stop</code>, <code>flush</code>,
     * and <code>release</code> methods on the old audio track, in that order.  You must also call these methods
     * when NexPlayer is done with the audio track and calls <code>onAudioRenderDelete</code>.<p>
     * 
     * @param audioTrack The {@link android.media.AudioTrack AudioTrack} object to use for audio output.
     * @return Always zero, but may change in future versions; the return value should be ignored.
     */
    private int setAudioTrack(int samplingRate, int channelNum)
    {
        Log.d(TAG, "setAudioTrack");

        int nChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        if (channelNum != 1)
            nChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_STEREO;

        int nMinBufferSize = AudioTrack.getMinBufferSize(samplingRate,
                nChannelConfig, AudioFormat.ENCODING_PCM_16BIT);

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, samplingRate,
                nChannelConfig, AudioFormat.ENCODING_PCM_16BIT, nMinBufferSize * 4,
                AudioTrack.MODE_STREAM);

        prepareAudio(0);
        return 0;
    }
    
    private int releaseAudioTrack()
    {
        if( mAudioTrack != null)
        {
            mAudioTrack.release();
            mAudioTrack = null;
        }

        return 0;
    }
    /**
     * \brief This function sets the surface on which video will be displayed.
     *
     * \warning <i>This is <b>NOT</b> supported with the Java or OpenGL renderers, and should <b>not</b> be called
     *          if one of those renderers is in use.</i>
     * 
     * This function actually takes the \c android.view.SurfaceHolder associated
     * with the surface on which the video will be displayed.
     * 
     * This function should be called from 
     * \link NexPlayer.IListener#onVideoRenderCreate(NexPlayer, int, int, Object) onVideoRenderCreate\endlink
     * after the surface has been created.  In addition, if the surface object changes (for example, if the 
     * <code>SurfaceHolder</code>'s \c surfaceCreated callback is after the initial setup), this
     * function should be called again to provide the new surface.
     * 
     * The surface should match the pixel format of the screen, if possible, or should
     * bet set to \c PixelFormat.RGB_565.
     * 
     * In general, the surface should be created as follows:
     * \code
     * Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
     * int displayPixelFormat = display.getPixelFormat();
     * 
     * SurfaceView surfaceView = new SurfaceView(this);
     * SurfaceHolder surfaceHolder = mVideoSurfaceView.getHolder();
     * 
     * if( displayPixelFormat == PixelFormat.RGBA_8888 ||
     *     displayPixelFormat == PixelFormat.RGBX_8888 ||
     *     displayPixelFormat == PixelFormat.RGB_888 ||
     *     displayPixelFormat == 5 )
     * {
     *     surfaceHolder.setFormat(PixelFormat.RGBA_8888);
     * }
     * else
     * {
     *     surfaceHolder.setFormat(PixelFormat.RGB_565);
     * }
     * 
     * surfaceHolder.addCallback(new SurfaceHolder.Callback() {
     *     &#x0040;Override
     *     public void surfaceDestroyed(SurfaceHolder holder) {
     *         mSurfaceExists = false;
     *     }
     *     &#x0040;Override
     *     public void surfaceCreated(SurfaceHolder holder) {
     *         mSurfaceExists = true;
     *         if( mPlaybackStarted ) {
     *             mNexPlayer.setDisplay(holder);
     *         }
     *     }
     *     &#x0040;Override
     *     public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
     *         // do nothing
     *     }
     * });
     * \endcode
     * 
     * In \c onVideoRenderCreate, the code should ensure that the surface has already been created
     * before passing the surface holder to \c setDisplay.  Because \c onViewRenderCreate
     * can run asynchronously, it may need to wait until the surface is created by sleeping and polling.
     * 
     * For example, if using the example code above, \c onVideoRenderCreate would wait until
     * \c mSurfaceExists becomes \c true, using something like:
     * 
     * \code
     * while(!mSurfaceExists)
     *     Thread.sleep(10);
     * nexPlayer.setDisplay(surfaceHolder);
     * \endcode
     * 
     * \param sh    The \c android.view.SurfaceHolder holding the surface on which to display video.
     *
     */
    public void setDisplay( SurfaceHolder sh ) {
        setDisplay(sh,0);
    }
    
    /**
     * \brief  This method sets the surface on which video will be displayed.
     * 
     * This is the same as {@link NexPlayer#setDisplay(SurfaceHolder) setDisplay(SurfaceHolder)}, except that
     * it takes an additional surface number parameter.  Currently, only one surface
     * at a time is supported, so this additional parameter must always be zero.
     * 
     * In general, it's better to use {@link NexPlayer#setDisplay(SurfaceHolder) setDisplay(SurfaceHolder)}.
     * 
     * \param sh  The \c android.view.SurfaceHolder holding the surface on which to display video.
     * \param surfaceNumber 
     *            This integer sets the number of additional surfaces (currently must be zero).
     * 
     * \return    Zero if successful, non-zero if there was an error.
     * 
     */
    public int setDisplay( SurfaceHolder sh, int surfaceNumber )
    {
        
        if ( surfaceNumber == 0 )
        {
            mSurfaceHolder = sh;
            if ( mSurfaceHolder == null )
                mSurface = null;
            else
                mSurface = sh.getSurface();
            Log.w( TAG, "setDisplay : " + mSurfaceHolder + "," + mSurface );
        }
        updateSurfaceScreenOn( surfaceNumber );
        return prepareSurface( surfaceNumber );
    }
    
    
    private void updateSurfaceScreenOn( int surfacetype )
    {
        
        if ( surfacetype == 0 )
        {
            Log.w(TAG, "updateSurfaceScreenOn surface type is 0");
            if ( mSurfaceHolder != null )
            {
                mSurfaceHolder.setKeepScreenOn( true );
            }
            
        }
    }
    
    /**
     * \brief This method releases resources used by the NexPlayer&trade;&nbsp;instance.
     * 
     * This should be called when the instance is no longer needed.  After
     * calling this method, the instance can no longer be used, and methods
     * on it should not be called, except for {@link NexPlayer#getState() getState} which
     * will return {@link NexPlayer#NEXPLAYER_STATE_NONE NEXPLAYER_STATE_NONE}.
     * 
     */
    
    public void release()
    {
        updateSurfaceScreenOn( 0 );
        releaseAudioTrack();
        _Release();
    }
    
    
    /**
     * releases native resources
     */
    @Override
    protected void finalize()
    {
        _Release();
    }
    
    /**
     * \brief  The application must implement this interface in order to receive
     *         events from NexPlayer&trade;.
     * 
     * <b>CAUTION:</b> These callbacks may occur in any thread, not
     * necessarily the main application thread. In some cases, it may not
     * be safe to call UI-related functions from within \c IListener
     * callbacks.  The safest way to update the UI is to use \c android.os.Handler
     * to post an event back to the main application thread.
     * 
     * NexPlayer&trade;&nbsp;will call the methods provided in this interface
     * automatically during playback to notify the application when various
     * events have occurred.
     * 
     * In most cases, the handling of these events is optional; NexPlayer&trade;&nbsp;
     * will continue playback normally without the application doing anything
     * special.  There are a few exceptions to this which are listed below.
     * 
     * There are two categories of notifications.  First of all, for any asynchronous command
     * issued to NexPlayer&trade;&nbsp;(via the appropriate method call), a callback
     * will occur when that command has completed to notify the application of
     * the success or failure of the operation.
     * 
     * The other category of notifications are notifications that occur during
     * playback to notify the application of changes in the state of NexPlayer&trade;.
     * For example, if NexPlayer&trade;&nbsp;begins buffering data during streaming
     * playback, an event occurs to allow the application to display an appropriate
     * message, if necessary.
     * 
     * For asynchronous commands, the application will generally want to take
     * action in the following cases (some applications may need to handle
     * these differently depending on their requirements; these are correct
     * for most cases):
     * 
     *   - When any command fails, display an error to the user.
     *   - When an \c open command succeeds, issue a \c start command to
     *      begin actual playback.
     *   - When a \c stop command succeeds, issue a \c close command to
     *      close the file.
     * 
     * 
     * This is because commands such as \c open and \c stop take some
     * time to execute, and follow-up commands such as \c start and \c close
     * cannot be called immediately, but must wait until the first command has
     * completed.
     * 
     * See each individual \c IListener method for a recommendation
     * on how to implement that method in your application.
     * 
     */ 
    public interface IListener
        {
            /**
             * \brief This method indicates when playback has completed successfully up to the end of the content.
             * 
             * This event occurs when the player reaches the end of the file or stream.
             * In most cases, applications should respond to this by calling \link NexPlayer.stop\endlink
             * and then updating the user interface. 
             * 
             * \param mp The NexPlayer&trade;&nbsp;object generating the event.
             * 
             */
            void onEndOfContent( NexPlayer mp );
            
            /**
             * \brief The NexPlayer&trade;&nbsp;video task has started.
             * 
             * \deprecated This method is only included for compatibility with older code and 
             *             should not be used.
             * 
             * This is provided for compatibility with older code, and new
             * applications may safely ignore this event.
             * 
             * \param mp The NexPlayer&trade;&nbsp;object generating the event.
             *
             */
            void onStartVideoTask( NexPlayer mp );
            
            /**
             * \brief The NexPlayer&trade;&nbsp;audio task has started.
             * 
             * \deprecated This method is only included for compatibility with older code and 
             *             should not be used.
             * 
             * This is provided for compatibility with older code, and new
             * applications may safely ignore this event.
             * 
             * @param mp The NexPlayer&trade;&nbsp;object generating the event.
             */
            void onStartAudioTask( NexPlayer mp);
            
            /**
             * \brief This method indicates that playback has progressed to the specified position.
             * 
             * This event occurs once per second. If the application is
             * displaying the current play position, it should update it
             * to reflect this new value.
             * 
             * Applications that wish to update the play time more often
             * that once per second or with a greater accuracy may ignore
             * this event and create their own timer, in which case they
             * can use the current play time reported in {@link NexContentInformation}.
             * 
             * @param mp The NexPlayer&trade;&nbsp;object generating the event.
             * @param sec
             *            Current play position in seconds.
             */
            void onTime( NexPlayer mp, int sec );
            
            /**
             * \brief    An error has occurred during playback.
             * 
             * @param mp The NexPlayer&trade;&nbsp;object generating the event.
             * @param errorcode The error code for the generated error.
             */
            void onError( NexPlayer mp, NexErrorCode errorcode );
            
            /**
             * \brief NexPlayer&trade;'s signal status has been changed.
             * 
             * @param mp The NexPlayer&trade;&nbsp;object generating the event.
             * @param pre
             *            The previous signal status.
             * @param now
             *            The current signal status.
             */
            void onSignalStatusChanged( NexPlayer mp, int pre, int now );
            
            /**
             * \brief NexPlayer&trade;'s state has been changed.
             * 
             * @param mp 
             *            The NexPlayer&trade;&nbsp;object generating the event.
             * @param pre
             *            The previous play status.
             * @param now
             *            The current play status.
             */
            void onStateChanged( NexPlayer mp, int pre, int now );
            
            /**
             * \brief This indicates when there has been a NexPlayer&trade;&nbsp;recording error.
             * 
             * @param mp 
             *            The NexPlayer&trade;&nbsp;object generating the event.
             * \param err
             *            An error while recording.
             * @deprecated Not available in current version; do not use.
             */
            void onRecordingErr( NexPlayer mp, int err );
            
            /**
             * \brief This indicates when NexPlayer&trade;&nbsp; recording has ended.
             * 
             * @param mp 
             *            The NexPlayer&trade;&nbsp;object generating the event.
             * \param success 
             * @deprecated Not available in current version; do not use.            
             */
            void onRecordingEnd( NexPlayer mp, int success );
            
            /**
             * \brief This reports NexPlayer&trade;'s recording status.
             *  
             * @param mp 
             *            The NexPlayer&trade;&nbsp;object generating the event.
             * \param recDuration
             *            An integer indicating the duration of the recording so far.
             * \param recSize
             *            An integer indicating the size of the recording so far.
             * @deprecated Not available in current version; do not use.  
             */
            void onRecording( NexPlayer mp, int recDuration, int recSize );
            
            /**
             * \brief This is a deprecated method that formerly reported any NexPlayer&trade;&nbsp;Time shift error.
             * @deprecated Not available in current version; do not use.  
             */
            void onTimeshiftErr( NexPlayer mp, int err );
            
            /**
             * \brief This is a deprecated method that formerly reported NexPlayer&trade;'s Time shift status.
             * 
             * @param mp 
             *            The NexPlayer&trade;&nbsp;object generating the event.
             * \param currTime
             *            The current time.
             * \param TotalTime
             *            The total time.
             * @deprecated Not available in current version; do not use.  
             */
            void onTimeshift( NexPlayer mp, int currTime, int TotalTime );
            
            /**
             * \brief   When an asynchronous method of NexPlayer&trade;&nbsp; has completed
             *          successfully or failed, this event occurs.
             * 
             * @param mp 
             *            The NexPlayer&trade;&nbsp;object generating the event.
             * 
             * @param command   The command which completed.  This may be any
             *                  of the following values:
             *                  <ul>
             *                    <li><code>NEXPLAYER_ASYNC_CMD_NONE</code> (0x00000000)</li>
             *                    <li><code>NEXPLAYER_ASYNC_CMD_OPEN_LOCAL</code> (0x00000001)</li>
             *                    <li><code>NEXPLAYER_ASYNC_CMD_OPEN_STREAMING</code> (0x00000002)</li>
             *                    <li><code>NEXPLAYER_ASYNC_CMD_OPEN_TV</code> (0x00000003)</li> 
             *                    <li><code>NEXPLAYER_ASYNC_CMD_START_LOCAL</code> (0x00000005)</li>
             *                    <li><code>NEXPLAYER_ASYNC_CMD_START_STREAMING</code> (0x00000006)</li>
             *                    <li><code>NEXPLAYER_ASYNC_CMD_START_TV</code> (0x00000007)</li>
             *                    <li><code>NEXPLAYER_ASYNC_CMD_STOP</code> (0x00000008)</li>
             *                    <li><code>NEXPLAYER_ASYNC_CMD_PAUSE</code> (0x00000009)</li>
             *                    <li><code>NEXPLAYER_ASYNC_CMD_RESUME</code> (0x0000000A)</li>
             *                    <li><code>NEXPLAYER_ASYNC_CMD_SEEK</code> (0x0000000B)</li>
             *                    <li><code>NEXPLAYER_ASYNC_CMD_FORWARD</code> (0x0000000C)</li>
             *                    <li><code>NEXPLAYER_ASYNC_CMD_BACKWARD</code> (0x0000000D)</li>
             *                    <li><code>NEXPLAYER_ASYNC_CMD_STEP_SEEK</code> (0x0000000E)</li>
             *                    <li><code>NEXPLAYER_ASYNC_CMD_FASTPLAY_START</code> (0x00000027)</li>
             *                    <li><code>NEXPLAYER_ASYNC_CMD_FASTPLAY_STOP</code> (0x00000028)</li>
             *                  </ul>
             * @param result    Zero if the command was successful, otherwise
             *                   an error code.
             * @param param1    A value specific to the command that has completed.  The following
             *                  commands use this value (for all other commands, the value is 
             *                  undefined and reserved for future use, and must be ignored):
             *                  <ul>
             *                    <li><b>NEXPLAYER_ASYNC_CMD_SEEK, NEXPLAYER_ASYNC_CMD_FORWARD, NEXPLAYER_ASYNC_CMD_BACKWARD:</b><br />
             *                      The actual position at which the seek, forward or backward command completed.  Depending on the
             *                      media format, this may be different than the position that was requested for the seek operation.
             *                  </ul>
             * @param param2    A value specific to the command that has completed.  Currently
             *                  there are no commands that pass this parameter, and it is
             *                  reserved for future use.  Applications should ignore this value.
             */
            void onAsyncCmdComplete( NexPlayer mp, int command, int result, int param1, int param2 );
            
            /**
             * Reports RTSP command Timeout.
             * 
             * \param mp  The NexPlayer&trade;&nbsp;object to which this event applies.
             */
            void onRTSPCommandTimeOut( NexPlayer mp );
            
            /**
             * Reports Pause Supervision Timeout.
             * 
             * \param mp  The NexPlayer&trade;&nbsp;object to which this event applies.
             */
            void onPauseSupervisionTimeOut( NexPlayer mp );
            
            /**
             * Reports Data Inactivity Timeout.
             * 
             * \param mp  The NexPlayer&trade;&nbsp;object to which this event applies.
             */
            void onDataInactivityTimeOut( NexPlayer mp );
            
            /**
             * \brief Reports the start of buffering.
             * 
             * \param mp  The NexPlayer&trade;&nbsp;object to which this event applies.
             */
            void onBufferingBegin( NexPlayer mp );
            
            /**
             * \brief This reports the end of buffering.
             * 
             * \param mp  The NexPlayer&trade;&nbsp;object to which this event applies.
             */
            void onBufferingEnd( NexPlayer mp );
            
            /**
             * \brief This reports the current buffering status.
             * 
             * \param mp  The NexPlayer&trade;&nbsp;object to which this event applies.
             * \param progress_in_percent
             *            The current buffering percentage.
             */
            void onBuffering( NexPlayer mp, int progress_in_percent );
            
            /**
             * \brief Notification that the audio rendering thread has been created.
             * 
             * Under previous versions of the SDK, it was necessary to create and
             * manage the audio renderer.  However, under the current version this
             * is done automatically, and the \c onAudioRenderCreate method should
             * be empty or contain only diagnostic code.
             * 
             * @param mp
             *            The NexPlayer&trade;&nbsp;object to which this event applies.
             * @param samplingRate
             *            The sample rate (in Hz) of the content to be played back.
             * @param channelNum
             *            The number of channels in the content (1=mono, 2=stereo).
             */
            void onAudioRenderCreate( NexPlayer mp, int samplingRate, int channelNum );
            
            /**
             * \brief Notification that the audio rendering thread has been destroyed.
             * 
             * Under previous versions of the SDK, it was necessary to destroy
             * the audio renderer.  However, under the current version this
             * is done automatically, and the \c onAudioRenderDelete method should
             * be empty or contain only diagnostic code.
             * 
             * @param mp
             *           The NexPlayer&trade;&nbsp;object to which this event applies.
             */
            void onAudioRenderDelete( NexPlayer mp );
            
            /**
             * \brief  Called when NexPlayer&trade;&nbsp;needs the application to create a surface on which
             *         to render the video.
             * 
             * The application must respond to this by calling 
             * {@link NexPlayer#setDisplay(SurfaceHolder) setDisplay}.
             * 
             * Generally speaking, the application will actually create the surface earlier,
             * during GUI layout, and will simply use the existing handle in response to this
             * call.  There are, however, some threading considerations.  See 
             * {@link NexPlayer#setDisplay(SurfaceHolder) setDisplay} for details.
             * 
             * @param mp
             *            The NexPlayer&trade;&nbsp;object to which this event applies.
             * @param width
             *            The width of the source video.
             * @param height
             *            The height of the source video.
             * @param rgbBuffer
             *            Direct RGB Buffer(RGB565 format).
             *            This RGB buffer is shared with NexPlayer&trade;&nbsp;Engine native code.
             */
            void onVideoRenderCreate( NexPlayer mp, int width, int height, Object rgbBuffer );
            
            /**
             * \brief This method is called when NexPlayer&trade;&nbsp;no longer needs the render surface.
             * 
             * If a surface was created in \c onVideoRenderCreate, this is the
             * place to destroy it.  However, if (as in most cases) an existing surface
             * was used, then this function need not take any special action, other than
             * updating whatever state the application needs to track.
             * 
             * @param mp
             *            The NexPlayer&trade;&nbsp;object to which this event applies.
             */
            void onVideoRenderDelete( NexPlayer mp);
            
            /**
             * \brief  This request to display Video frame data at JAVA application
             * 
             * \param mp  The NexPlayer&trade;&nbsp;object to which this event applies.
             */
            void onVideoRenderRender( NexPlayer mp );
            
            /**
             * \brief   Called when a frame of video has been captured.
             * 
             * After calling {@link NexPlayer#captureVideo(int, int) captureVideo} to
             * set up video capture, this function will be called whenever a frame is
             * captured, and can process the captured frame as necessary.
             * 
             * \code
             Bitmap bitmap = Bitmap.createBitmap(width, height, pixelbyte==2?Config.RGB_565:Config.ARGB_8888 );
             ByteBuffer RGBBuffer = (ByteBuffer)rgbBuffer;
             RGBBuffer.asIntBuffer();
             bitmap.copyPixelsFromBuffer(RGBBuffer);
             * \endcode
             * 
             * @param mp
             *            The NexPlayer&trade;&nbsp;object to which this event applies.
             * @param width
             *            The width of the captured frame.
             * @param height
             *            The height of the captured frame.
             * @param pixelbyte
             *            The number of bytes per pixel (2 for RGB565; 4 for RGBA).
             * @param bitmap
             *            The object where the captured video frame data is stored. 
             * 
             */
            void onVideoRenderCapture(NexPlayer mp, int width, int height, int pixelbyte, Object bitmap );
            
            /**
             * \brief   Called when initially beginning playback of media content with 
             *          associated subtitles.
             * 
             * @param mp
             *          The NexPlayer&trade;&nbsp;object to which this event applies.
             * 
             * @param numTracks
             *          The number of subtitle tracks available for this media.  Note
             *          that this may be 0 if there are no subtitles, or this function
             *          may not be called at all.
             */
            void onTextRenderInit( NexPlayer mp, int numTracks );
            
            /**
             * \brief This function is called when new subtitle data is ready for display.
             * 
             * This is called whenever playback reaches a point in time where subtitles on any
             * track need to be displayed or cleared.
             * 
             * The text to display is provided in a \c NexClosedCaption object as a byte array; 
             * it is the responsibility of the application to convert this to text with the appropriate 
             * encoding.  Where possible, the encoding information will be provided in the 
             * NexClosedCaption.mEncodingType, but many subtitle
             * file formats do not explicity specify an encoding, so it may be necessary for
             * the application to guess the encoding or allow the user to select it.
             * 
             * \par <i>HISTORIAL NOTE 1:</i> 
             *      In previous API versions, it was the responsibility of the
             * application to handle the case where there were multiple tracks in a file by
             *      filtering based on \c trackIndex.  This is no longer necessary (or even
             * possible) as that functionality has been replaced by 
             *      {@link NexPlayer#setCaptionLanguage(int) setCaptionLanguage} and \c trackIndex
             *      is no longer used and is always zero.
             *
             * \par <i>HISTORIAL NOTE 2:</i> 
             *      In previous API versions, the third argument of this method
             *      was a Java byte array, and encoding information was not specified.
             * 
             * @param mp
             *          The NexPlayer&trade;&nbsp;object to which this event applies.
             * 
             * @param trackIndex
             *          This is always zero and should always be ignored.
             * 
             * @param textInfo
             *          The text to be displayed (cast this to a \c NexClosedCaption object).
             */
            void onTextRenderRender( NexPlayer mp, int trackIndex, NexClosedCaption textInfo );
            

            /**
             * \brief This method is called when new timed metadata is ready for display in HLS.
             * 
             * Timed metadata includes additional information about the playing content that may be
             * displayed to the user and this information may change at different times throughout the 
             * content.  Each time new metadata is available for display, this method is called.
             * 
             * \see  NexID3TagInformation for more details on the available metadata information.
             * 
             * \param mp         The NexPlayer&trade;&nbsp;object to which this event applies.
             * \param TimedMeta  An NexID3TagInformation object that contains the timed metadata
             *                   associated with the content to be displayed.
             * 
             */
            void onTimedMetaRenderRender( NexPlayer mp, NexID3TagInformation TimedMeta );
            
            //* Possible value for <code>command</code> parameter of {@link IListener#onAsyncCmdComplete(NexPlayer, int, int) onAsyncCmdComplete}. */
            // public static final int NEXPLAYER_ASYNC_CMD_NONE             = 0x00000000;
            
            /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. 
             * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_NONE} instead. */
            public static final int eNEXPLAYER_STATUS_NONE              = 0x00000000;
            /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. 
             * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_AUDIO_GET_CODEC_FAILED} instead. */
            public static final int eNEXPLAYER_AUDIO_GET_CODEC_FAILED   = 0x00000001;
            /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. 
             * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_VIDEO_GET_CODEC_FAILED} instead. */
            public static final int eNEXPLAYER_VIDEO_GET_CODEC_FAILED   = 0x00000002;
            /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. 
             * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_AUDIO_INIT_FAILED} instead. */
            public static final int eNEXPLAYER_AUDIO_INIT_FAILED        = 0x00000003;
            /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. 
             * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_VIDEO_INIT_FAILED} instead. */
            public static final int eNEXPLAYER_VIDEO_INIT_FAILED        = 0x00000004;
            /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. 
             * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_TRACK_CHANGED} instead. */
            public static final int eNEXPLAYER_TRACK_CHANGED            = 0x00000005;
            /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. 
             * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_STREAM_CHANGED} instead. */
            public static final int eNEXPLAYER_STREAM_CHANGED           = 0x00000006;
            /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. 
             * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_DSI_CHANGED} instead. */
            public static final int eNEXPLAYER_DSI_CHANGED              = 0x00000007;
            /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. 
             * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_OBJECT_CHANGED} instead. */
            public static final int eNEXPLAYER_OBJECT_CHANGED           = 0x00000008;
            /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. 
             * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_CONTENT_INFO_UPDATED} instead. */
            public static final int eNEXPLAYER_CONTENT_INFO_UPDATED     = 0x00000009;
            /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onStatusReport(NexPlayer, int, int) onStatusReport}. 
             * This value is deprecated and has been renamed.  Use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_MAX NEXPLAYER_STATUS_REPORT_MAX} instead. 
             * @deprecated Renamed; use {@link NexPlayer#NEXPLAYER_STATUS_REPORT_MAX} instead. */
            public static final int NEXPLAYER_STATUS_MAX                = 0xFFFFFFFF;
            
            
            /**
             * \brief This function is called when there is a change in the available content information.
             * 
             * This can happen, for example, if the track changes during HLS playback,
             * resulting in changes to the bitrate, resolution, or even the codec
             * in use.
             * 
             * The \c msg parameter contains information about the condition
             * that has changed.
             * 
             * Because multiple calls to this function can be issued for the same event,
             * unknown values for \c msg should generally be ignored.  To handle
             * general status changes that affect content information without processing
             * duplicate messages, the best approach is just to handle 
             * \link NexPlayer.IListener.eNEXPLAYER_CONTENT_INFO_UPDATED eNEXPLAYER_CONTENT_INFO_UPDATED\endlink.
             * 
             * To determine the new content information when this event occurs, call 
             * \link NexPlayer#getContentInfo() getContentInfo\endlink or 
             * \link NexPlayer#getContentInfoInt(int) getContentInfoInt\endlink.
             * 
             * @param mp
             *        The NexPlayer&trade;&nbsp;object to which this event applies.
             * 
             * @param msg 
             *        The type of notification.  This is one of the following values:
             *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_NONE NEXPLAYER_STATUS_REPORT_NONE} (0x00000000) </b>
             *              No status change (this value is not normally passed to \c onStatusReport, and
             *              should generally be ignored).
             *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_AUDIO_GET_CODEC_FAILED NEXPLAYER_STATUS_REPORT_AUDIO_GET_CODEC_FAILED} (0x00000001) </b>
             *              Failed to determine the audio codec.  This notification can happen at the beginning of
             *              playback, or during playback if there is an audio codec change.  This can happen because of a
             *              switch to a new codec that NexPlayer&trade;&nbsp;does not support, or due to an error in the format 
             *              of the content or corrupted data in the content.
             *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_VIDEO_GET_CODEC_FAILED NEXPLAYER_STATUS_REPORT_VIDEO_GET_CODEC_FAILED} (0x00000002) </b>
             *              Failed to determine the video codec.  This notification can happen at the beginning of
             *              playback, or during playback if there is a video codec change.  This can happen because of a
             *              switch to a new codec that NexPlayer&trade;&nbsp;does not support, or due to an error in the format 
             *              of the content or corrupted data in the content.
             *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_AUDIO_INIT_FAILED NEXPLAYER_STATUS_REPORT_AUDIO_INIT_FAILED} (0x00000003) </b>
             *              The audio codec failed to initialize.  This can happen for several reasons.  The container may
             *              indicate the wrong audio codec, or the audio stream may be incorrect or corrupted, or the audio
             *              stream may use a codec version or features that NexPlayer&trade;&nbsp;doesn't support.
             *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_VIDEO_INIT_FAILED NEXPLAYER_STATUS_REPORT_VIDEO_INIT_FAILED} (0x00000004) </b>
             *              The video codec failed to initialize.  This can happen for several reasons.  The container may
             *              indicate the wrong video codec, or the video stream may be incorrect or corrupted, or the video
             *              stream may use a codec version or features that NexPlayer&trade;&nbsp;doesn't support.
             *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_TRACK_CHANGED NEXPLAYER_STATUS_REPORT_TRACK_CHANGED} (0x00000005) </b>
             *              The track has changed. This happens for protocols such as HLS that provide the content
             *              in multiple formats or at multiple resolutions or bitrates.  The ID of the new track can
             *              be found in \link NexStreamInformation#mCurrTrackID mCurrTrackID\endlink, and also in \c param1.
             *              <i>When this event occurs, NexPlayer&trade;&nbsp;also generates a eNEXPLAYER_CONTENT_INFO_UPDATED event.</i>
             *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_STREAM_CHANGED NEXPLAYER_STATUS_REPORT_STREAM_CHANGED} (0x00000006) </b>
             *              The stream being played back has changed (between the states Audio-Only, Video-Only and Audio+Video).
             *              The new stream type is in \link NexContentInformation#mMediaType mMediaType\endlink, and also in \c param1.
             *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_DSI_CHANGED NEXPLAYER_STATUS_REPORT_DSI_CHANGED} (0x00000007) </b>
             *              An attribute relating to the video or audio format (such as the resolution, bitrate, etc.) has changed. This is
             *              considered Decoder Specific Information (DSI).
             *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_OBJECT_CHANGED NEXPLAYER_STATUS_REPORT_OBJECT_CHANGED} (0x00000008) </b>
             *              One of the codec objects in use has changed (that is, the audio or video codec in use
             *              has changed). 
             *              See \link NexContentInformation#mAudioCodec mAudioCodec\endlink and
             *              \link NexContentInformation#mVideoCodec mVideoCodec\endlink to get the ID of the new codec.
             *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_CONTENT_INFO_UPDATED NEXPLAYER_STATUS_REPORT_CONTENT_INFO_UPDATED} (0x00000009) </b>
             *              The content information has changed.  When onStatusReport is called with any other non-Failure
             *              value for \c msg, it will also be called with this one as well.  This is a good
             *              place to monitor for any non-specific change to the content information.
             *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_AVMODE_CHANGED NEXPLAYER_STATUS_REPORT_AVMODE_CHANGED} (0x0000000A) </b>
             *              The stream being played back has changed and the new stream
             *              has a different media type.  This event happens whenever the state changes between 
             *              video-only, audio-only and audio-video. \c param1 contains the new media type: 1 for audio, 2 for video, 3 for both.
             *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_HTTP_INVALID_RESPONSE NEXPLAYER_STATUS_REPORT_HTTP_INVALID_RESPONSE} (0x0000000B) </b>
             *              An HTTP error response was received from the server.  \c param1 contains the error code (this is
             *              a normal HTTP response code, such as 404, 500, etc.)
             *            - <b>{@link NexPlayer#NEXPLAYER_STATUS_REPORT_MAX NEXPLAYER_STATUS_REPORT_MAX} (0xFFFFFFFF) </b>
             *              This value is reserved; do not use it.
             *            .
             * @param param1
             *          Additional information.  The meaning of this depends on the value of \c msg.  If the description
             *          above doesn't refer to \c param1, then this parameter is undefined for that value of
             *          \c msg and should not be used.
             */
            void onStatusReport( NexPlayer mp, int msg, int param1);
            

            /**
             *  \brief  This function is called when an error is generated by the Downloader module.
             *  
             * @param mp       The NexPlayer&trade;&nbsp;object to which this event applies.
             * @param msg      An integer indicating the error generated by the Downloader module.
             * @param param1   This parameter is currently undefined but reserved for future use and should not be used.
             * 
             */
            void onDownloaderError(NexPlayer mp, int msg, int param1);
            /**
             *  \brief  This function is called when an asynchronous command in the Downloader module is complete.
             *  
             *  This method will be called whenever DownloaderOpen(), DownloaderClose(), DownloaderStart() or DownloaderStop()
             *  finish asynchronously.
             *  
             * @param mp      The NexPlayer&trade;&nbsp;object to which this event applies.
             * @param msg     The asynchronous command completed.  This will be one of:
             *                   - <b>NEXDOWNLOADER_ASYNC_CMD_OPEN = 0x00200001</b>
             *                   - <b>NEXDOWNLOADER_ASYNC_CMD_CLOSE = 0x00200002</b>
             *                   - <b>NEXDOWNLOADER_ASYNC_CMD_START = 0x00200003</b>
             *                   - <b>NEXDOWNLOADER_ASYNC_CMD_STOP = 0x00200004</b>
             * @param param1  This integer indicates the result of the command.  It will be 0 in the event of
             *                success, or will be an error code in the event of failure.
             * @param param2  Additional information, if available, concerning the result reported in \c param1.  For example
             *                if the error is invalid response, \c param2 gives the HTTP status code.
             * 
             */
            void onDownloaderAsyncCmdComplete(NexPlayer mp, int msg, int param1, int param2);

			/**
			 * \brief  This method reports when a Downloader event has started.
			 * 
			 *  \param mp      The NexPlayer&trade;&nbsp;object to which this event applies.
			 *  \param param1  This parameter is currently undefined and is not used but is reserved for future use.
			 *  \param param2  The total size of the content file to be downloaded.
			 *  
			 */
            void onDownloaderEventBegin(NexPlayer mp, int param1, int param2);

            /**
             *  \brief  This function is called to pass the downloading progress of a Downloader event. 
             * 
             * @param mp      The NexPlayer&trade;&nbsp;object to which this event applies.
             * @param param1  This parameter is currently undefined and is not used but is reserved for future use.
             * @param param2  The time remaining until the downloading file has saved completely, in msec.
             * @param param3  The size of the portion of the downloading file already received. in bytes (B).
             * @param param4  The total size of the content file being downloaded, in bytes (B).
             * 
             */
            void onDownloaderEventProgress(NexPlayer mp, int param1, int param2, long param3, long param4);
             /**
             * \brief  This function is called when a Downloader event has completed.
             * 
             * @param mp      The NexPlayer&trade;&nbsp;object to which this event applies.
             * @param param1  This parameter is currently undefined and is not used but is reserved for future use.
             * 
             */
            void onDownloaderEventComplete(NexPlayer mp, int param1);
            /**
             * \brief  This method reports the current state of a Downloader event. 
             * 
             * @param mp      The NexPlayer&trade;&nbsp;object to which this event applies.
             * @param param1  This parameter is currently undefined and is not used but is reserved for future use.
             * @param param2  This is an integer that indicates the current state of the Downloader event.  
             *                This will be one of:
             *                  - <b>NEXDOWNLOADER_STATE_NONE = 0</b>
             *                  - <b>NEXDOWNLOADER_STATE_CLOSED = 2</b>
             *                  - <b>NEXDOWNLOADER_STATE_STOP = 3</b>
             *                  - <b>NEXDOWNLOADER_STATE_DOWNLOAD = 4</b>
             */
            void onDownloaderEventState(NexPlayer mp, int param1, int param2);
            
            
            /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onDebugInfo(NexPlayer, int, String) onDebugInfo}. */
            //public static final int NEXPLAYER_DEBUGINFO_RTSP            = 0x00;
            /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onDebugInfo(NexPlayer, int, String) onDebugInfo}. */
            //public static final int NEXPLAYER_DEBUGINFO_RTCP_RR_SEND    = 0x01;
            /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onDebugInfo(NexPlayer, int, String) onDebugInfo}. */
            //public static final int NEXPLAYER_DEBUGINFO_RTCP_BYE_RECV   = 0x02;
            /** Possible value for <code>msg</code> parameter of {@link NexPlayer.IListener#onDebugInfo(NexPlayer, int, String) onDebugInfo}. */
            //public static final int NEXPLAYER_DEBUGINFO_CONTENT_INFO    = 0x03;
            
            
            /**
             * Provides debugging and diagnostic information during playback.  The information provided
             * here is for debugging purposes only; the contents of the strings provided may change in future
             * versions, so do not attempt to parse them or make programmating decisions based on the contents.
             * Also, do not make assumptions about line length or number of lines.
             * 
             * <b>Superceded:</b> The relevant information provided in the freeform text strings
             *                    that used to be passed to this method is now available directly
             *                    in NexContentInformation.<p>
             * 
             * @param mp
             *          The NexPlayer&trade;&nbsp;object to which this event applies.
             * 
             * @param msg
             *          Identifies the type of debugging information being provided.  This is one of the following values:
             * <ul>
             * <li><b>{@link NexPlayer.IListener#NEXPLAYER_DEBUGINFO_RTSP NEXPLAYER_DEBUGINFO_RTSP} (0x00)</b>
             *      Debugging information related to the RTSP connection status.</li>
             * <li><b>{@link NexPlayer.IListener#NEXPLAYER_DEBUGINFO_RTCP_RR_SEND NEXPLAYER_DEBUGINFO_RTCP_RR_SEND} (0x01)</b>
             *      Debugging information associated with the transmission of an RTCP RR (Receiver Report) packet.</li>
             * <li><b>{@link NexPlayer.IListener#NEXPLAYER_DEBUGINFO_RTCP_BYE_RECV NEXPLAYER_DEBUGINFO_RTCP_BYE_RECV} (0x02)</b>
             *      This occurs when an RTCP BYE packet is received.</li>
             * <li><b>{@link NexPlayer.IListener#NEXPLAYER_DEBUGINFO_CONTENT_INFO NEXPLAYER_DEBUGINFO_CONTENT_INFO} (0x03)</b>
             *      General information about the content that is currently playing.  This is intended to be shown
             *      in a "heads-up" style overlay or suplementary display, and replaces information provided in
             *      any previous <code>NEXPLAYER_DEBUGINFO_CONTENT_INFO</code> calls.</li>
             * </ul>
             * @param strDbg
             *      A string containing the debugging information associated with the event.  This may contain
             *      multiple lines of text.
             */
            //void onDebugInfo( NexPlayer mp, int msg, String strDbg);
        }


    /**
     * \brief  This method sets the size of the file being downloaded in the Downloader module.
     * 
     * 
     * \param  ReceivedSize  The size of portion of the file received so far, in bytes (B).
     * \param  TotalSize     The total size of the file being downloaded, in bytes (B).
     * 
     * \returns  Zero if successful, another value in the case of failure.
     */
    public native int SetExternalPDFileDownloadSize(long ReceivedSize, long TotalSize);
	
	//  APIs of Downloader must be called after init().
    /**
     * This is a possible value for the parameter \c eType in the method DownloaderOpen(). */
   	public static final int NEXDOWNLOADER_OPEN_TYPE_CREATE = 0;
    /** This is a possible value for the parameter \c eType in the method DownloaderOpen(). */
	public static final int NEXDOWNLOADER_OPEN_TYPE_APPEND = 1;
	
	
	/** This is a possible value for the parameter \c param2 of {@link IListener#onDownloaderEventState(NexPlayer mp, int param1, int param2) onDownloaderEventState}.  */
    public static final int NEXDOWNLOADER_STATE_NONE = 0;	 
    /** This is a possible value for the parameter \c param2 of {@link IListener#onDownloaderEventState(NexPlayer mp, int param1, int param2) onDownloaderEventState}.  */
	public static final int NEXDOWNLOADER_STATE_CLOSED = 2; 	
	/** This is a possible value for the parameter \c param2 of {@link IListener#onDownloaderEventState(NexPlayer mp, int param1, int param2) onDownloaderEventState}.  */
	public static final int NEXDOWNLOADER_STATE_STOP = 3; 	 
	/** This is a possible value for the parameter \c param2 of {@link IListener#onDownloaderEventState(NexPlayer mp, int param1, int param2) onDownloaderEventState}.  */
	public static final int NEXDOWNLOADER_STATE_DOWNLOAD = 4;
	
	
	/**
	 * \brief  This method is called to open a new event in the Downloader module.
	 * 
	 * The Downloader module allows users to download and save streaming PIFF progressive download (PD) content so that 
	 * if can be viewed at a later time.  It must be opened before opening the content to be downloaded and calling \c NexPlayer.open().
	 * 
	 * Note that this method cannot be called until after initializing NexPlayer&trade;&nbsp;by calling init().
	 * 
	 * @param strUrl        This is a string passing the URL to the content to be downloaded.
	 * @param strStorePath  This is a string indicating the path to where the downloaded file is saved.
	 * @param proxyPath     This is a string indicating the path to the proxy server.
	 * @param proxyPort     This is an integer indicating the port to use on the proxy server.
	 * @param eType         This is an integer indicating the type of event being opened.  It will be one of:
	 *                        - <b>NEXDOWNLOADER_OPEN_TYPE_CREATE = 0 </b>:  This creates a new Downloader event.
	 *                        - <b>NEXDOWNLOADER_OPEN_TYPE_APPEND = 1 </b>:  This appends newly downloaded information to 
	 *                          an existing file already begun.  Note that not every server will support APPEND 
	 *                          events so this should only be used conditional on the content server.
	 * 
     * \return  Zero if successful, or an error code in the event of failure.
	 */
	public native int DownloaderOpen(String strUrl, String strStorePath, String proxyPath, int proxyPort, int eType );
	
    /**
     * \brief  This method is called to close a Downloader event.
     * 
     * Note that this method cannot be called until after initializing NexPlayer&trade;&nbsp;by calling init().  Anytime DownloaderOpen()
     * is called, this method must also be called properly close the Downloader module.  If a call is also made to 
     * DownloaderStart(), then DownloaderStop() must be called BEFORE calling DownloaderClose() to properly end the event.  
     * 
     * \return  Zero if successful, or an error code in the event of failure.
     */
	public native int DownloaderClose( );
	/**
	 * \brief  This method is called to start downloading and saving content in a Downloader event.
	 * 
	 * Note that this method cannot be called until after both initializing NexPlayer&trade;&nbsp;by calling init() and
	 * opening an event in the Downloader module with DownloaderOpen().
     * 
     * \return  Zero if successful, or an error code in the event of failure.
	 */
	public native int DownloaderStart( );
	
	/**
	 * \brief  This method is called to stop downloading and saving content in a Downloader event.
	 * 
	 * Note that this method cannot be called until after initializing NexPlayer&trade;&nbsp;by calling init().  Anytime a 
	 * call is made to DownloaderStart(), it must be matched by a call to this method to properly stop and finish 
	 * a Downloader event.
	 * 
     * \return  Zero if successful, or an error code in the event of failure. 
	 */
	public native int DownloaderStop( );
	
	/**
	 * \brief  This method is called to get information about a Downloader event.
	 * 
	 * Note that this method cannot be called until after initializing NexPlayer&trade;&nbsp;by calling init().
	 * 
	 * \param  info  The Downloader object to get information about.
	 * 
	 * 
	 */
	public native int DownloaderGetInfo( Object info);
 
	private native int getSeekableRange(long[] info);
 
	private static long[] arrLongInfo = new long[2];
	
	/**
	 * \brief  This method returns the range of the current content that is seekable.
	 * 
	 * This method is used to allow NexPlayer&trade;&nbsp;to support timeshifting playback within HLS Live and 
	 * Smooth Streaming content.  Based on the amount of content available from the server at a particular time,
	 * it determines the seekable range within the playing content which also indicates the range where playback may
	 * be timeshifted.  This range will be constantly shifting as the live streaming content available from the server
	 * changes in real time, so this method will need to be repeatedly called to ensure accurate shifting of playback.
	 *   
	 * For local content this method will always return the same two values, and the second value indicating the end 
	 * of the seekable range will continuously change in progressive download (PD) content, but this method is 
	 * most relevant when playing live streaming content, as with HLS and Smooth Streaming.
	 * 
	 * For more information about how this method may be used to timeshift playback in live content, please
	 * also refer to the introductory section on \ref timeshift "time shift support".
	 * 
	 * \returns  An array of two \c longs, the first \c long being the timestamp indicating the start of the seekable range
	 *           and the second being the timestamp indicating the end of the seekable range.
	 */
	
	public long[] getSeekableRangeInfo()
	{
	    //long[] info = new long[2];
	    int nRet = 0;
	    nRet = getSeekableRange( arrLongInfo );
	    Log.w(TAG, "getSeekableRange. return:"+nRet);
	    if(nRet != 0)
	    {
	    	return null;
	    }
	    
	    return arrLongInfo;
	}
	
	/**
	 * \brief  This method returns the name of the NexPlayer&trade;&nbsp;SDK in use.
	 * 
	 * It can be used for confirmation and for debugging purposes but should generally
	 * be ignored.
	 * 
	 * \returns The name of the NexPlayer&trade;&nbsp;SDK as a string.
	 */
    public native String getSDKName( );
}
