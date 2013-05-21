/************************************************************************
VisualOn Proprietary
Copyright (c) 2013, VisualOn Incorporated. All Rights Reserved

VisualOn, Inc., 4675 Stevens Creek Blvd, Santa Clara, CA 95051, USA

All data and information contained in or disclosed by this document are
confidential and proprietary information of VisualOn, and all rights
therein are expressly reserved. By accepting this material, the
recipient agrees that this material and the information contained
therein are held in confidence and in trust. The material may only be
used and/or disclosed as authorized in a license agreement controlling
such use and disclosure.
************************************************************************/

package com.visualon.osmpSamplePlayer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.visualon.OSMPBasePlayer.*;
import com.visualon.OSMPBasePlayer.voOSBasePlayer.onEventListener;
import com.visualon.OSMPBasePlayer.voOSBasePlayer.onRequestListener;
import com.visualon.OSMPUtils.voLog;
import com.visualon.OSMPUtils.voOSDVRInfo;
import com.visualon.OSMPUtils.voOSType;
import com.visualon.osmpSamplePlayer.AssetOp.AssetInfo;
import com.visualon.osmpSamplePlayer.AssetOp.AssetType;

/* Activity implement SurfaceHolder.Callback */
public class Player extends Activity
{
	private static final String	 TAG = "@@@OSMP+Player";		// Tag for voLog messages

	/* Messages for managing the user interface */
	private static final int     MSG_SHOW_CONTROLLER  = 1;
	private static final int     MSG_HIDE_CONTROLLER  = 2;
	private static final int     MSG_UPDATE_UI        = 3;
	private static final int     MSG_PLAYCOMPLETE     = 5;

	/* SurfaceView must be passed to SDK */
	private SurfaceView			 m_svMain             = null;
	private SurfaceHolder		 m_shMain             = null;

	/* Media controls and User interface */
	private ImageButton			 m_ibPlayPause		  = null;   // Play/Pause button
	private SeekBar				 m_sbMain			  = null;   // Seekbar

	private TextView			 m_tvCurrentTime	  = null;	// Current position
	private TextView			 m_tvTotalTime        = null;   // Total duration

	private ProgressBar			 m_pbLoadingProgress  = null;	// Wait icon for buffered or stopped video
	private Date				 m_dateUIDisplayStartTime=null; // Last update of media controls

	private	Timer 				 m_timerMain          = null;	// Timer for display of media controls
	private TimerTask 			 m_ttMain             = null;	// Timer for display of media controls

	private voOSBasePlayer		 m_osbasePlayer       = null;	// SDK player
	private AssetOp				 m_assetOpMain		  = null;	// Video, Audio and Subtitle asset operation

	/* Flags */
	private boolean				 m_bTrackProgressing  = false;	// Seekbar flag

	private boolean				 m_bPaused            = false;	// Pause flag
	private boolean				 m_bStoped            = false;	// Stop flag

	private String				 m_strVideoPath		  = "";		// URL or file path to media source
	private String               m_strSubtitlePath    = "";

	private int					 m_nVideoWidth        = 0;      // Video width
	private int					 m_nVideoHeight       = 0;		// video height
	private int					 m_nDuration          = 0;		// Total duration
	private int					 m_nPos               = 0;		// Current position

	/* User interface for main view (media source input/selection) */
	private EditText 			 m_edtInputURL		  = null;	// User input URL or file path
	private AlertDialog 		 m_adlgMain           = null;  	// Dialog for media source
	private Spinner 			 m_spSelectURL		  = null;   // Media source list selector
	private ArrayList<String> 	 m_lstSelectURL       = null;	// Media source list from url.txt
	private Spinner              m_spSelectSubtitle   = null;   // External subtitle list selector
    private ArrayList<String>    m_lstSelectSubtitle  = null;   // External subtitle String list
    private boolean              m_bSpinnerClickAutoTriggered_SelectUrl = false;
    private boolean              m_bSpinnerClickAutoTriggered_SelectSubtitle = false;

	private CheckBox             m_chbOpenMAXAL       = null;
	private CheckBox             m_chbAsync           = null;

	private boolean              m_bEnableVideo       = false;   // BpsQuality / Video
	private boolean              m_bEnableAudio       = false;   // AudioTrack
	private boolean              m_bEnableSubtitle    = true;   // External/DVB Subtitle or ClosedCaption 608 708
	private boolean              m_bShowSubtitleSelector = true; // If show External Subtitle selector/listbox on startup window/dialog

	private boolean              m_bEnableChannel     = false;  // Fast Channel/url switching in the left of player UI.
	private boolean              m_bChannelSwitchPerf = false;  // for QA test purpuse

	private boolean              m_bShowOpenMAXAL     = false;
	private boolean              m_bShowAsyncCheckBox = false;

	private RelativeLayout       m_rlTop              = null;
	private RelativeLayout       m_rlBottom           = null;
	private RelativeLayout       m_rlRight            = null;
	private ScrollView           m_svRight            = null;
    private LinearLayout         m_llRight            = null;
    private LinearLayout         m_llVideoPopupMenu   = null;
    private LinearLayout         m_llAudioPopupMenu   = null;
    private LinearLayout         m_llSubtitlePopupMenu= null;
    private HorizontalScrollView m_hsvVideoPopupMenu  = null;
    private HorizontalScrollView m_hsvAudioPopupMenu  = null;
    private HorizontalScrollView m_hsvSubtitlePopupMenu = null;

    private ImageButton          m_ibBpsQuality       = null;
    private RelativeLayout       m_rlBpsQuality       = null;
    private ImageButton          m_ibAudioTrack       = null;
    private RelativeLayout       m_rlAudioTrack       = null;
    private ImageButton          m_ibSubtitle         = null;
    private RelativeLayout       m_rlSubtitle         = null;

    private RelativeLayout       m_rlChannel          = null;
    private ListView             m_lvChannel          = null;

    private AutoSwitchVideo      m_autoSwitchVideo    = null;

    private SurfaceHolder.Callback m_cbSurfaceHolder = new SurfaceHolder.Callback() {
        /* Notify SDK on Surface Change */
        public void surfaceChanged (SurfaceHolder surfaceholder, int format, int w, int h) {
            voLog.i(TAG, "Surface Changed");
            if (m_osbasePlayer == null)
                return;

            m_osbasePlayer.SetParam(voOSType.VOOSMP_PID_SURFACE_CHANGED, 1);
        }

        /* Notify SDK on Surface Creation */
        public void surfaceCreated(SurfaceHolder surfaceholder) {
            voLog.i(TAG, "Surface Created");
            if (m_osbasePlayer != null) {
                // For handling the situation such as phone calling is coming.

                // If SDK player already exists, show media controls
                m_osbasePlayer.SetParam(voOSType.VOOSMP_PID_VIEW_ACTIVE, m_svMain);
                voLog.i(TAG, "SetParam(VOOSMP_PID_VIEW_ACTIVE(51 0x33), m_svMain)");
                showMediaController();
                return;
            }

            if ((m_strVideoPath == null) || (m_strVideoPath.trim().length() <=0))
                return;

            // Enter from the other APP such as a browser or file explorer
            initPlayer(m_strVideoPath);
            playVideo(m_strVideoPath);

        }

        public void surfaceDestroyed(SurfaceHolder surfaceholder) {
            voLog.i(TAG, "Surface Destroyed");

            if (m_osbasePlayer == null)
                return;

            m_osbasePlayer.SetView(null);
        }
    };

    private onRequestListener    m_listenerRequest = new onRequestListener() {
        /* Manage SDK requests */
        public int onRequest(int arg0, int arg1, int arg2, Object arg3) {
            voLog.v(TAG, "OnEvent onRequest nID is %s, nParam1 is %s, nParam2 is %s",
                    Integer.toHexString(arg0),Integer.toHexString(arg1),Integer.toHexString(arg2));
            return 0;
        }
    };

    private onEventListener      m_listenerEvent = new onEventListener() {
        /* SDK event handling */
        public int onEvent(int nID, int nParam1, int nParam2, Object obj) {

            voLog.v(TAG, "OnEvent nID is %s, nParam1 is %s, nParam2 is %s",
                    Integer.toHexString(nID),Integer.toHexString(nParam1),Integer.toHexString(nParam2));

            switch (nID) {

            case voOSType.VOOSMP_SRC_CB_Adaptive_Streaming_Error : {
                if (nParam1 == 4) {//VO_SOURCE2_ADAPTIVESTREAMING_ERROREVENT_STREAMING_DOWNLOADFAIL
                    voLog.v(TAG, "VOOSMP_SRC_CB_Adaptive_Streaming_Error, nParam1 is VO_SOURCE2_ADAPTIVESTREAMING_ERROREVENT_STREAMING_DOWNLOADFAIL");
                }
                break;
            }
            case voOSType.VOOSMP_SRC_CB_Adaptive_Stream_Warning : {
                if (nParam1 == voOSType.VOOSMP_SRC_ADAPTIVE_STREAMING_WARNING_EVENT_CHUNK_DOWNLOADERROR) {
                    voLog.v(TAG, "VOOSMP_SRC_CB_Adaptive_Stream_Warning, nParam1 is VOOSMP_SRC_ADAPTIVE_STREAMING_WARNING_EVENT_CHUNK_DOWNLOADERROR");
                } else{
                    voLog.v(TAG, "VOOSMP_SRC_CB_Adaptive_Stream_Warning, nParam1 is "+nParam1);
                }
                break;
            }
            case voOSType.VOOSMP_SRC_CB_Adaptive_Streaming_Info : {

                switch(nParam1) {
                    case voOSType.VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_BITRATE_CHANGE : {
                        voLog.v(TAG, "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_BITRATE_CHANGE, param2 is %d . ", nParam2);
                        break;
                    }
                    case voOSType.VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE : {
                        voLog.v(TAG, "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, param2 is %d . ", nParam2);

                        switch(nParam2) {
                            case voOSType.VOOSMP_AVAILABLE_PUREAUDIO : {
                                voLog.v(TAG, "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_PUREAUDIO");
                                break;
                            }
                            case voOSType.VOOSMP_AVAILABLE_PUREVIDEO : {
                                voLog.v(TAG, "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_PUREVIDEO");
                                break;
                            }
                            case voOSType.VOOSMP_AVAILABLE_AUDIOVIDEO: {
                                voLog.v(TAG, "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_AUDIOVIDEO");
                                break;
                            }
                        }
                        break;
                    }
                }
                break;
            }
            case voOSType.VOOSMP_CB_Error : {
                // Display error dialog and stop player
                onError(m_osbasePlayer, 1, 0);
                break;
            }
            case voOSType.VOOSMP_CB_PlayComplete : {
                handler.sendEmptyMessage(MSG_PLAYCOMPLETE);
                break;
            }
            case voOSType.VOOSMP_CB_SeekComplete : {      // Seek (SetPos) complete
                TimeCal.printTime("Receive Engine Seek Complete <---");
                break;
            }
            case voOSType.VOOSMP_SRC_CB_Seek_Complete : { // Seek (SetPos) complete
                TimeCal.printTime("Receive Source Seek Complete <---");
                break;
            }
            case voOSType.VOOSMP_CB_VideoRenderStart : {
                TimeCal.printTime("Receive VideoRenderStart <---");
                break;
            }
            case voOSType.VOOSMP_CB_BufferStatus : {      // Updated buffer status
                break;
            }
            case voOSType.VOOSMP_CB_VideoSizeChanged : {  // Video size changed

                m_nVideoWidth = nParam1;
                m_nVideoHeight = nParam2;

                // Retrieve new display metrics
                DisplayMetrics dm  = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);

                if (getResources().getConfiguration().orientation
                        == Configuration.ORIENTATION_PORTRAIT) {

                    // If portrait orientation, scale height as a ratio of the new aspect ratio
                    ViewGroup.LayoutParams lp = m_svMain.getLayoutParams();
                    lp.width = dm.widthPixels;
                    lp.height = dm.widthPixels * m_nVideoHeight / m_nVideoWidth;
                    m_svMain.setLayoutParams(lp);
                }
                break;
            }
            case voOSType.VOOSMP_CB_VideoStopBuff : {     // Video buffering stopped
                m_pbLoadingProgress.setVisibility(View.GONE);       // Hide wait icon
                break;
            }
            case voOSType.VOOSMP_CB_VideoStartBuff : {    // Video buffering started
                m_pbLoadingProgress.setVisibility(View.VISIBLE);                // Show wait icon
                break;
            }
            case voOSType.VOOSMP_CB_AudioStopBuff : {
                m_pbLoadingProgress.setVisibility(View.INVISIBLE);
                break;
            }
            case voOSType.VOOSMP_CB_AudioStartBuff : {
                m_pbLoadingProgress.setVisibility(View.VISIBLE);
                break;
            }
            case voOSType.VOOSMP_CB_ClosedCaptionData : {  // CC data
                break;
            }
            case voOSType.VOOSMP_SRC_CB_Connection_Fail :
            case voOSType.VOOSMP_SRC_CB_Download_Fail :
            case voOSType.VOOSMP_SRC_CB_DRM_Fail :
            case voOSType.VOOSMP_SRC_CB_Playlist_Parse_Err :
            case voOSType.VOOSMP_SRC_CB_Connection_Rejected :
            case voOSType.VOOSMP_SRC_CB_DRM_Not_Secure :
            case voOSType.VOOSMP_SRC_CB_DRM_AV_Out_Fail : {   // Errors

                // Display error dialog and stop player
                onError(m_osbasePlayer, nID, 0);
                break;

            }
            case voOSType.VOOSMP_SRC_CB_BA_Happened : {        // Unimplemented

                voLog.v(TAG, "OnEvent VOOSMP_SRC_CB_BA_Happened, param is %d . ", nParam1);
                break;
            }
            case voOSType.VOOSMP_SRC_CB_Download_Fail_Waiting_Recover : {
                voLog.v(TAG, "OnEvent VOOSMP_SRC_CB_Download_Fail_Waiting_Recover, param is %d . ", nParam1);
                break;
            }
            case voOSType.VOOSMP_SRC_CB_Download_Fail_Recover_Success : {
                voLog.v(TAG, "OnEvent VOOSMP_SRC_CB_Download_Fail_Recover_Success, param is %d . ", nParam1);
                break;
            }
            case voOSType.VOOSMP_SRC_CB_Open_Finished : {

                TimeCal.printTime("Async Open Finished <---");

                if (nParam1 == voOSType.VOOSMP_ERR_None) {
                    voLog.v(TAG, "MediaPlayer is opened async.");

                    int nRet;

                    /* Run (play) media pipeline */
                    TimeCal.printTime("Run --->");
                    nRet = m_osbasePlayer.Run();
                    TimeCal.printTime("Run <---");
                    if (nRet == voOSType.VOOSMP_ERR_None) {
                        voLog.v(TAG, "MediaPlayer run async");
                    } else {
                        onError(m_osbasePlayer, nRet, 0);
                    }

                    m_nPos = m_osbasePlayer.GetPos();
                    m_nDuration = m_osbasePlayer.GetDuration();

                    updatePosDur();

                    m_bStoped = false;
                    m_ibPlayPause.setImageResource(R.drawable.pause_button);

                    m_autoSwitchVideo.startAutoSwitchVideo();

                } else {
                    onError(m_osbasePlayer, nParam1, 0);
                }

                break;
            }
            case voOSType.VOOSMP_SRC_CB_Program_Changed : {
                voLog.v(TAG, "OnEvent nID = %s, VOOSMP_SRC_CB_Program_Changed", Integer.toHexString(nID));
                break;
            }
            case voOSType.VOOSMP_SRC_CB_Program_Reset : {
                voLog.v(TAG, "OnEvent nID = %s, VOOSMP_SRC_CB_Program_Reset", Integer.toHexString(nID));
                break;
            }

            }

            return 0;
        }
    };

    private OnSeekBarChangeListener m_listenerSeekBar = new OnSeekBarChangeListener() {
        /* Flag when Seekbar is being dragged */
        public void onStartTrackingTouch(SeekBar arg0) {
            m_bTrackProgressing = true;
        }

        /* Seek to new position when Seekbar drag is complete */
        public void onStopTrackingTouch(SeekBar arg0) {
            // Calculate new position as percentage of total duration
            int nCurrent = arg0.getProgress();
            int nMax = arg0.getMax();

            m_bTrackProgressing = false;            // Disable Seekbar tracking flag

            int lNewPosition;
            if (m_nDuration > 0) {
                lNewPosition = nCurrent * m_nDuration / nMax;
            }
            else {
        		voOSDVRInfo info = (voOSDVRInfo) m_osbasePlayer.GetParam(voOSType.VOOSMP_SRC_PID_DVRINFO);
        		if (info == null)
        			lNewPosition = 0;
        		else
        		{
	                int nMinPos = (int) info.getStartTime();
	                int nMaxPos = (int) info.getLiveTime();
	                int nDuration = nMaxPos - nMinPos;

	                lNewPosition = nCurrent * nDuration / nMax;

	                lNewPosition = lNewPosition + nMinPos;
        		}
            }

            if (m_osbasePlayer != null) {
                voLog.v(TAG,"Seek To " + lNewPosition);
                TimeCal.printTime("SetPos --->");
                m_osbasePlayer.SetPos(lNewPosition);  // Set new position
                TimeCal.printTime("SetPos <---");
            }

        }

        public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {}
    };

    // System
    private Handler handler = new Handler() {
		/* Handler to manage user interface during playback */
		public void handleMessage(Message msg) {

			if (m_osbasePlayer == null)
				return;

			if (msg.what == MSG_SHOW_CONTROLLER) {
				/* Show media controls */
				showMediaControllerImpl();
			} else if (msg.what == MSG_HIDE_CONTROLLER) {
				/* Hide media controls */
				hideControllerImpl();
			} else if (msg.what == MSG_UPDATE_UI) {
				/* Update UI */
			    doUpdateUI();
			} else if (msg.what == MSG_PLAYCOMPLETE) {
			    /* Playback in complete, stop player */
		      stopAndFinish();
			}
		}
	};

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		voLog.v(TAG, "Player onCreate");

		/*Screen always on*/
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,  WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.player);

		initUI();

		Uri uri = getIntent().getData();
		if (uri != null) {
			// If media source was passed as URI data, use it
			m_strVideoPath = uri.getPath();

			int i = m_strVideoPath.indexOf("/mnt/");
			if (i != -1) {
				//local file
				m_strVideoPath = m_strVideoPath.subSequence(i, m_strVideoPath.length()).toString();
			} else {
				//stream
				m_strVideoPath = uri.toString();
			}

			voLog.v(TAG, "Source URI: " + m_strVideoPath);
		} else {
			// Else bring up main view to input/select media source
			SourceWindow();
		}

		// Find View and UI objects
		m_svMain = (SurfaceView) findViewById(R.id.svMain);
		m_shMain = m_svMain.getHolder();
		m_shMain.addCallback(m_cbSurfaceHolder);
		m_shMain.setFormat(PixelFormat.RGBA_8888);
		m_shMain.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);

		m_ibPlayPause     = (ImageButton)findViewById(R.id.ibPlayPause);
		m_sbMain          = (SeekBar)findViewById(R.id.sbMain);
		m_tvCurrentTime   = (TextView)findViewById(R.id.tvCurrentTime);
		m_tvTotalTime     = (TextView)findViewById(R.id.tvTotalTime);
		m_pbLoadingProgress = (ProgressBar) findViewById(R.id.pbBuffer);

		m_sbMain.setOnSeekBarChangeListener(m_listenerSeekBar);

		voLog.v(TAG, "Video source is " + m_strVideoPath);

		m_rlTop.setVisibility(View.INVISIBLE);
		m_rlBottom.setVisibility(View.INVISIBLE);
		m_pbLoadingProgress.setVisibility(View.GONE);

		// Activate listener for Play/Pause button
		m_ibPlayPause.setOnClickListener(new ImageButton.OnClickListener() {
		    public void onClick(View view) {
		        playerPauseRestart();
			}
		});
		// Copy license file,
		CommonFunc.copyfile(this, "cap.xml", "cap.xml");

	}

	@Override
    protected void onDestroy() {
        // Finish GLView rendering if 3D animation is enabled.  Do it before
        // calling parent class onDestroy function.
        super.onDestroy();
        voLog.v(TAG, "Player onDestroy Completed!");
    }

    /* Pause/Stop playback on activity pause */
    protected void onPause() {
        super.onPause();
        voLog.v(TAG, "Player onPause");

        if (m_osbasePlayer!= null) {
            if(m_osbasePlayer.GetDuration()<=0) {
                // If for live streaming, we stop playbacking directly.
                stopAndFinish();
            } else {
                // Else pause playback and show media controls
                m_osbasePlayer.Pause();
                m_ibPlayPause.setImageResource(R.drawable.play_button);
                m_bPaused = true;
                voLog.v(TAG, "Player pause");

                showMediaController();
            }

        }
    }

    /* (non-Javadoc)
    * @see android.app.Activity#onRestart()
    */
    @Override
    protected void onRestart() {
        voLog.v(TAG, "Player onRestart");

        super.onRestart();
        if(m_osbasePlayer == null)
            return;
    }

    /* Show media controls on activity touch event */
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            resetLayoutRightPopupMenu();

            if (m_rlBottom.getVisibility() != View.VISIBLE) {
                showMediaControllerImpl();
            } else {
                hideControllerImpl();
            }
      }

        return super.onTouchEvent(event);
    }

    /* Stop player and exit on Back key */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        voLog.v(TAG, "Key click is " + keyCode);

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            voLog.v(TAG, "Key click is Back key");
            stopAndFinish();

            return super.onKeyDown(keyCode, event);
        }

        return super.onKeyDown(keyCode, event);
    }

    /* Notify SDK of configuration change */
    public void onConfigurationChanged(Configuration newConfig) {

        if (m_osbasePlayer == null ||  m_nVideoHeight == 0 || m_nVideoWidth == 0) {
            super.onConfigurationChanged(newConfig);
            return;
        }

        // Retrieve new display metrics
        DisplayMetrics dm  = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        ViewGroup.LayoutParams lp = m_svMain.getLayoutParams();
        lp.width = dm.widthPixels;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // If landscape orientation, use display metrics height
            lp.height = dm.heightPixels;
            m_osbasePlayer.SetDisplaySize(dm.widthPixels, dm.heightPixels);
            m_osbasePlayer.updateVideoAspectRatio(m_nVideoWidth, m_nVideoHeight);

        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // If portrait orientation, scale height as a ratio of the original aspect ratio
            lp.height = dm.widthPixels * m_nVideoHeight / m_nVideoWidth;
        }
        // Pass new width/height to View
        m_svMain.setLayoutParams(lp);

        super.onConfigurationChanged(newConfig);
    }

    // UI
    private void doUpdateUI() {
        // If the controls are not visible do not update
        if (m_rlBottom.getVisibility() != View.VISIBLE)
            return;

        /* If the player is stopped do not update */
        if (m_bStoped)
            return;

        /* If the user is dragging the progress bar do not update */
        if (m_bTrackProgressing)
            return;

        Date timeNow = new Date(System.currentTimeMillis());
        long timePeriod = (timeNow.getTime() - m_dateUIDisplayStartTime.getTime()) / 1000;
        if (timePeriod >= 7 && m_bPaused == false) {

            /* If the media is being played back, hide media controls after 7 seconds if unused */
            hideController();
            return;
        }

        /* update the Seekbar and Time display with current position */

        if (m_nDuration > 0) {
        	m_nPos = m_osbasePlayer.GetPos();
            m_sbMain.setProgress(m_nPos * 100 / m_nDuration);
            String str = DateUtils.formatElapsedTime(m_nPos / 1000);
            m_tvCurrentTime.setText(str);
        }
    }

    /* Show media controller. Implemented by showMediaControllerImpl(), called by handler */
    private void showMediaController() {
        if(m_osbasePlayer == null)
            return;

        if(m_osbasePlayer.GetDuration() <=0) {
            voLog.v(TAG, "live module, Don't show control bar!");
            return;
        }

        handler.sendEmptyMessage(MSG_SHOW_CONTROLLER);
    }

    /* Show media controller implementation */
    private void showMediaControllerImpl() {
        voLog.v(TAG, "Touch screen, layout status is " +m_rlBottom.getVisibility());
        m_dateUIDisplayStartTime = new Date(System.currentTimeMillis());        // Get current system time

        if (m_rlBottom.getVisibility() != View.VISIBLE) {
            voLog.v(TAG, "mIsStop is " + m_bStoped);

            // Schedule next UI update in 200 milliseconds
            if(m_ttMain != null)
                m_ttMain = null;

            m_ttMain= new TimerTask() {
                public void run() {
                    handler.sendEmptyMessage(MSG_UPDATE_UI);
                }
            };

            if(m_timerMain == null) {
                m_timerMain = new Timer();
            }
            m_timerMain.schedule(m_ttMain, 0, 200);

            // Show controls
            m_rlBottom.setVisibility(View.VISIBLE);
            m_rlTop.setVisibility(View.VISIBLE);

            voLog.v(TAG, "m_rlTop show " + m_rlTop.getVisibility());
        }
    }

    /* Hide media controller. Implemented by showMediaControllerImpl(), called by handler */
    private void hideController() {
        handler.sendEmptyMessage(MSG_HIDE_CONTROLLER);
    }

    /* Hide media controller implementation */
    private void hideControllerImpl() {
        if(m_timerMain != null) {
            m_timerMain.cancel();
            m_timerMain.purge();
            m_timerMain = null;
            m_ttMain = null;
        }

        m_rlBottom.setVisibility(View.INVISIBLE);
        m_rlTop.setVisibility(View.INVISIBLE);
    }

	/* Interface to input/select media source */
	private void SourceWindow() {
		LayoutInflater inflater = LayoutInflater.from(Player.this);
		View layout = inflater.inflate(R.layout.url, null);

		m_edtInputURL = (EditText)layout.findViewById(R.id.edtInputURL);
	    m_spSelectURL = (Spinner)layout.findViewById(R.id.spSelectURL);
	    m_spSelectSubtitle  = (Spinner)layout.findViewById(R.id.spSelectSubtitle);

	    m_chbOpenMAXAL = (CheckBox)layout.findViewById(R.id.chbOpenMAXAL);
	    m_chbAsync    = (CheckBox)layout.findViewById(R.id.chbAsync);

	    if (!m_bShowSubtitleSelector) {
	        layout.findViewById(R.id.tvSelectSubtitle).setVisibility(View.GONE);
	        m_spSelectSubtitle.setVisibility(View.GONE);
	    }

	    if (!m_bShowOpenMAXAL)
	        m_chbOpenMAXAL.setVisibility(View.GONE);

	    if (!m_bShowAsyncCheckBox)
	        m_chbAsync.setVisibility(View.GONE);

		// Dialog to input source URL or file path
		m_adlgMain = new AlertDialog.Builder(Player.this).setIcon(R.drawable.hyperlink)
						.setTitle(R.string.str_URL).setView(layout)
						.setNegativeButton(R.string.str_Cancel, new OnClickListener() {
							// "Cancel" button stops player and exits
							public void onClick(DialogInterface dialog, int which) {
							    stopAndFinish();
							}
						})
						.setPositiveButton(R.string.str_OK, new OnClickListener() {
								// "OK" button begins playback of inputted media source
								public void onClick(DialogInterface dialog, int which) {
								    // editbox is preferred
                                    if (m_edtInputURL.getText().toString().length() != 0)
                                        m_strVideoPath = m_edtInputURL.getText().toString();

                                    initPlayer(m_strVideoPath);
                                    playVideo(m_strVideoPath);
								}
							}
						)
						.setOnKeyListener(new OnKeyListener() {
								public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
									// "Back" button stops player and exits
									if (arg1 == KeyEvent.KEYCODE_BACK) {
									    arg0.dismiss();
									    stopAndFinish();

									    return true;
									}

									return false;
								}
							})
						.create();

		m_bSpinnerClickAutoTriggered_SelectUrl = false;
		m_bSpinnerClickAutoTriggered_SelectSubtitle = false;

		// Display media source input and selection options
		m_adlgMain.show();

		// Retrieve URL list
		m_lstSelectURL = new ArrayList<String>();
		ReadUrlInfo();

		final ArrayList<String> arrURLListWithTitle = new ArrayList<String>(m_lstSelectURL);
		arrURLListWithTitle.add(0, getResources().getString(R.string.str_SelURL_FirstLine));
    arrURLListWithTitle.add("http://csm-e-usw1-2.cds1.yospace.com/csm/live/24537081?yo.p=3&yo.l=true&ext=.m3u8&hdnea=st=~exp=1367010647~acl=/csm/restart/live/*~hmac=8093667af206bc92836fda547cc36970a6b4c312f0a73a0895744f1e00a3273d");
    arrURLListWithTitle.add("http://player.ooyala.com/player/iphone/91bThhODokcxQNhlk3ttzNZs3HoTZ12M.m3u8?secure_ios_token=QkVsRFYvaWpPdHRDZ3A5Nk9qeW1Fd3JhSWVxbHNhUTYzTlQ2L3lkY1pETWExTnVtbVJHSGFBQ2s5UGxCClRTaDdBSmNnZytFZXBpN1hnaHAzT0Zjb3N3PT0K");

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		        android.R.layout.simple_spinner_item, arrURLListWithTitle);

		m_spSelectURL.setAdapter(adapter);
		m_spSelectURL.setOnItemSelectedListener(new OnItemSelectedListener() {
			// When item is selected from URL list, begin playback of selected item
			public void onItemSelected(AdapterView<?> arg0, View arg1,
			        int arg2, long arg3) {
				if (m_bSpinnerClickAutoTriggered_SelectUrl == false) {
				    m_bSpinnerClickAutoTriggered_SelectUrl = true;
					return;
				}

				voLog.v(TAG, "Id is " +  m_spSelectURL.getSelectedItemId() + ", Pos is " + m_spSelectURL.getSelectedItemPosition()
				        + ", arg2 " +  arg2 + ", arg3 " + arg3);

				m_strVideoPath = arrURLListWithTitle.get(m_spSelectURL.getSelectedItemPosition());

			}

			public void onNothingSelected(AdapterView<?> arg0){	}
		});

		// Retrieve External Subtitle list
		if (!m_bShowSubtitleSelector)
            return;

        m_lstSelectSubtitle = new ArrayList<String>();
        ReadSubtitleInfo();

        final ArrayList<String> arrSubtitleListWithTitle = new ArrayList<String>(m_lstSelectSubtitle);
        arrSubtitleListWithTitle.add(0, getResources().getString(R.string.str_SelSubtitle_FirstLine));

        ArrayAdapter<String> adptSubtitle = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arrSubtitleListWithTitle);

        m_spSelectSubtitle.setAdapter(adptSubtitle);
        m_spSelectSubtitle.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                if (m_bSpinnerClickAutoTriggered_SelectSubtitle == false) {
                    m_bSpinnerClickAutoTriggered_SelectSubtitle = true;
                    return;
                }

                int nPos = m_spSelectSubtitle.getSelectedItemPosition();
                m_strSubtitlePath = arrSubtitleListWithTitle.get(nPos);

            }

            public void onNothingSelected(AdapterView<?> arg0) {}
        });
	}

    private void resetUIDisplayStartTime() {
        m_dateUIDisplayStartTime = new Date(System.currentTimeMillis());
    }

	private void initUI() {

	    m_rlTop    = (RelativeLayout) findViewById(R.id.tlTop);
	    m_rlBottom = (RelativeLayout) findViewById(R.id.rlBottom);

        initLayoutRight();
        initLayoutLeft();

    }

	private void initLayoutRight() {

	    m_rlRight        = (RelativeLayout) findViewById(R.id.rlRight);

	    m_svRight        = (ScrollView)  findViewById(R.id.svRight);
	    m_llRight        = (LinearLayout)findViewById(R.id.llRight);
	    m_ibSubtitle     = (ImageButton) findViewById(R.id.ibCloseCaptionSelector);
	    m_rlSubtitle     = (RelativeLayout) findViewById(R.id.rlCloseCaptionSelector);
	    m_ibBpsQuality   = (ImageButton) findViewById(R.id.ibBpsQuality);
	    m_rlBpsQuality   = (RelativeLayout) findViewById(R.id.rlBpsQuality);
	    m_ibAudioTrack   = (ImageButton) findViewById(R.id.ibAudioTrack);
	    m_rlAudioTrack   = (RelativeLayout) findViewById(R.id.rlAudioTrack);

        if (!m_bEnableVideo && !m_bEnableAudio && !m_bEnableSubtitle) {
            m_rlRight.setVisibility(View.GONE);
            return;
        }

        m_svRight.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View arg0, MotionEvent arg1) {
                resetUIDisplayStartTime();
                return false;
            }
        });

	    m_llRight.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View view, MotionEvent motionevent) {

                if (motionevent.getAction() == MotionEvent.ACTION_DOWN) {

                    if (m_rlBottom.getVisibility() == View.VISIBLE) {
                        hideControllerImpl();
                        return true;
                    }
                }

                return false;
            }
        });

	    initLayoutRightPopupMenu();

	    if (m_bEnableSubtitle) {

	        m_ibSubtitle.setOnClickListener(new View.OnClickListener() {

	            public void onClick(View v) {
	                if (m_osbasePlayer == null)
	                    return;

	                resetUIDisplayStartTime();

	                int nVisible = m_hsvSubtitlePopupMenu.getVisibility();
	                if (nVisible == View.VISIBLE) {
	                    m_hsvSubtitlePopupMenu.setVisibility(View.INVISIBLE);
	                } else {
	                    resetLayoutRightPopupMenu();
	                    m_llSubtitlePopupMenu.removeAllViews();

	                    m_assetOpMain.queryAsset(AssetType.Asset_Subtitle);

	                    inflatePopupMenu(m_llSubtitlePopupMenu, m_assetOpMain.getAssetList(AssetType.Asset_Subtitle), AssetType.Asset_Subtitle);
	                    setPopupMenuState(m_llSubtitlePopupMenu, m_assetOpMain.getAssetList(AssetType.Asset_Subtitle));

	                    m_llSubtitlePopupMenu.scheduleLayoutAnimation();
	                    m_hsvSubtitlePopupMenu.setVisibility(View.VISIBLE);
	                }

	            }
	        });

	    } else {
	        m_rlSubtitle.setVisibility(View.GONE);
	    }

	    if (m_bEnableVideo) {

	        m_ibBpsQuality.setOnClickListener(new View.OnClickListener() {

	            public void onClick(View v) {
	                if (m_osbasePlayer == null)
	                    return;

	                resetUIDisplayStartTime();

	                int nVisible = m_hsvVideoPopupMenu.getVisibility();
	                if (nVisible == View.VISIBLE) {
	                    m_hsvVideoPopupMenu.setVisibility(View.INVISIBLE);
	                }
	                else {
	                    resetLayoutRightPopupMenu();
	                    m_llVideoPopupMenu.removeAllViews();

	                    m_assetOpMain.queryAsset(AssetType.Asset_Video);

	                    inflatePopupMenu(m_llVideoPopupMenu, m_assetOpMain.getAssetList(AssetType.Asset_Video), AssetType.Asset_Video);
	                    setPopupMenuState(m_llVideoPopupMenu, m_assetOpMain.getAssetList(AssetType.Asset_Video));

	                    m_llVideoPopupMenu.scheduleLayoutAnimation();
	                    m_hsvVideoPopupMenu.setVisibility(View.VISIBLE);
	                }

	            }
	        });

	    } else {
	        m_rlBpsQuality.setVisibility(View.GONE);
	    }

	    if (m_bEnableAudio) {

	        m_ibAudioTrack.setOnClickListener(new View.OnClickListener() {

	            public void onClick(View v) {

	                if (m_osbasePlayer == null)
	                    return;

	                resetUIDisplayStartTime();

	                int nVisible = m_hsvAudioPopupMenu.getVisibility();
	                if (nVisible == View.VISIBLE) {
	                    m_hsvAudioPopupMenu.setVisibility(View.INVISIBLE);
	                }
	                else {
	                    resetLayoutRightPopupMenu();
	                    m_llAudioPopupMenu.removeAllViews();

	                    m_assetOpMain.queryAsset(AssetType.Asset_Audio);

	                    inflatePopupMenu(m_llAudioPopupMenu, m_assetOpMain.getAssetList(AssetType.Asset_Audio), AssetType.Asset_Audio);
	                    setPopupMenuState(m_llAudioPopupMenu, m_assetOpMain.getAssetList(AssetType.Asset_Audio));

	                    m_llAudioPopupMenu.scheduleLayoutAnimation();
	                    m_hsvAudioPopupMenu.setVisibility(View.VISIBLE);
	                }

	            }
	        });

	    } else {
	        m_rlAudioTrack.setVisibility(View.GONE);
	    }

	}

	private void setPopupMenuState(LinearLayout parentView, ArrayList<AssetInfo> lstAssetInfo) {

        if (parentView.getChildCount() == 0 || lstAssetInfo == null)
            return;

        for (int i = 0; i < parentView.getChildCount(); i++) {

            if (lstAssetInfo.get(i).m_bPlaying) {
                parentView.getChildAt(i).setSelected(true);
                break;
            }
        }

	}

	@SuppressWarnings("unchecked")
	private void inflatePopupMenu(LinearLayout parentView,
	        ArrayList<?> lstT, final AssetType type) {

        if (lstT.size() == 0)
            return;

        LinearLayout llBuffer;
        TextView tvBuffer;

        int size = lstT.size();
        if (size <= 3) {
            // There is a bug in HorizontalScrollView, so we have to use a workaround.
	        // http://stackoverflow.com/questions/9031817/android-horizontalscrollview-with-right-layout-gravity-working-wrong
	        HorizontalScrollView.LayoutParams params = new HorizontalScrollView.LayoutParams(
	                HorizontalScrollView.LayoutParams.WRAP_CONTENT, HorizontalScrollView.LayoutParams.FILL_PARENT);
	        params.gravity = Gravity.RIGHT;
	        parentView.setLayoutParams(params);
	    }

	    for (int i = 0; i < size; i++) {
	        if (i == 0) {
	            llBuffer = (LinearLayout)LayoutInflater.from(this)
	                .inflate(R.layout.player_popupmenu_left, parentView, false);

	        } else {
	            llBuffer = (LinearLayout)LayoutInflater.from(this)
	                .inflate(R.layout.player_popupmenu_normal, parentView, false);
	        }

	        parentView.addView(llBuffer);

	        tvBuffer = (TextView)llBuffer.findViewById(R.id.tvContent);
	        tvBuffer.setOnClickListener(new View.OnClickListener() {

	            public void onClick(View view) {

	                resetLayoutRightPopupMenu();

	                m_assetOpMain.onAssetClick(((Integer)view.getTag()).intValue(), type);

	            }
	        });

	        tvBuffer.setText( ((ArrayList<AssetInfo>)lstT).get(i).m_strDisplay);

	        tvBuffer.setTag(i);
	    }
	}

	// Right layout functions
	private void initLayoutRightPopupMenu() {
        m_llSubtitlePopupMenu   = (LinearLayout) findViewById(R.id.llCloseCaptionSelectorPopupMenu);
        m_llVideoPopupMenu      = (LinearLayout) findViewById(R.id.llBpsQualityPopupMenu);
        m_llAudioPopupMenu      = (LinearLayout) findViewById(R.id.llAudioTrackPopupMenu);

        m_hsvSubtitlePopupMenu  = (HorizontalScrollView) findViewById(R.id.hsvCloseCaptionSelectorPopupMenu);
        m_hsvSubtitlePopupMenu.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View view, MotionEvent motionevent) {
                if (motionevent.getAction() == MotionEvent.ACTION_MOVE)
                    resetUIDisplayStartTime();

                return false;
            }

        });

        m_hsvVideoPopupMenu = (HorizontalScrollView) findViewById(R.id.hsvBpsQualityPopupMenu);
        m_hsvVideoPopupMenu.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View view, MotionEvent motionevent) {
                if (motionevent.getAction() == MotionEvent.ACTION_MOVE)
                    resetUIDisplayStartTime();

                return false;
            }

        });

        m_hsvAudioPopupMenu = (HorizontalScrollView) findViewById(R.id.hsvAudioTrackPopupMenu);
        m_hsvAudioPopupMenu.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View view, MotionEvent motionevent) {
                if (motionevent.getAction() == MotionEvent.ACTION_MOVE)
                    resetUIDisplayStartTime();

                return false;
            }

        });
    }

    private void resetLayoutRightPopupMenu() {

        if (!m_bEnableVideo && !m_bEnableAudio && !m_bEnableSubtitle)
            return;

        if (m_hsvSubtitlePopupMenu.getVisibility() == View.VISIBLE) {
            m_hsvSubtitlePopupMenu.setVisibility(View.INVISIBLE);
        }

        if (m_hsvVideoPopupMenu.getVisibility() == View.VISIBLE) {
            m_hsvVideoPopupMenu.setVisibility(View.INVISIBLE);
        }

        if (m_hsvAudioPopupMenu.getVisibility() == View.VISIBLE) {
            m_hsvAudioPopupMenu.setVisibility(View.INVISIBLE);
        }
    }

 	// SDK
    private void setupParameters() {

        DisplayMetrics dm  = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        // Set display size
        m_osbasePlayer.SetDisplaySize(dm.widthPixels, dm.heightPixels);

        // Set view
        m_osbasePlayer.SetView(m_svMain);

        // Register SDK event listener
        m_osbasePlayer.setEventListener(m_listenerEvent);

        m_osbasePlayer.setRequestListener(m_listenerRequest);

        //Setup license content, or screen can green flicker.
        InputStream is = null;
        byte[] b = new byte[32*1024];
        try {
            is = getAssets().open("voVidDec.dat");
            is.read(b);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        m_osbasePlayer.SetParam(voOSType.VOOSMP_PID_LICENSE_CONTENT, b);


       String licenseText = "VOTRUST_OOYALA_754321974";        // Magic string from VisualOn, must match voVidDec.dat to work
       m_osbasePlayer.SetParam(voOSType.VOOSMP_PID_LICENSE_TEXT, licenseText);

        /* Configure DRM parameters */
        m_osbasePlayer.SetParam(voOSType.VOOSMP_SRC_PID_DRM_FILE_NAME, "voDRM");
        m_osbasePlayer.SetParam(voOSType.VOOSMP_SRC_PID_DRM_API_NAME, "voGetDRMAPI");

        /* Configure Dolby Audio Effect parameters */
        m_osbasePlayer.SetParam(voOSType.VOOSMP_PID_AUDIO_EFFECT_ENABLE, 0);

        /* Processor-specific settings */
        String capFile = CommonFunc.getUserPath(this) + "/" + "cap.xml";
        m_osbasePlayer.SetParam(voOSType.VOOSMP_SRC_PID_CAP_TABLE_PATH, capFile);

        //Enable closed caption
        m_osbasePlayer.SetParam(voOSType.VOOSMP_PID_COMMON_CCPARSER, 1);

        if (m_strSubtitlePath != null && m_strSubtitlePath.length() > 0)
            m_osbasePlayer.SetParam(voOSType.VOOSMP_PID_SUBTITLE_FILE_NAME, m_strSubtitlePath);

    }

    /* Initialize SDK player and playback selected media source */
    private void initPlayer(String strPath) {

        m_osbasePlayer = new voOSBasePlayer();

        m_assetOpMain  = new AssetOp(m_osbasePlayer, this);
        m_autoSwitchVideo = new AutoSwitchVideo(m_assetOpMain);

        m_shMain.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);

        // Location of libraries
        String apkPath = CommonFunc.getUserPath(this) + "/lib/";

        // SDK player engine type
        int nEngineType = voOSType.VOOSMP_VOME2_PLAYER;

        if (m_chbOpenMAXAL != null)
            nEngineType = m_chbOpenMAXAL.isChecked() ?
                voOSType.VOOSMP_OMXAL_PLAYER : voOSType.VOOSMP_VOME2_PLAYER;

        // Initialize SDK player
        TimeCal.printTime("Init --->");
        int nRet = m_osbasePlayer.Init(this, apkPath, null, nEngineType, 0, 0);
        TimeCal.printTime("Init <---");

        if (nRet == voOSType.VOOSMP_ERR_None) {
            voLog.v(TAG, "MediaPlayer is created.");
        }else {
            onError(m_osbasePlayer, nRet, 0);
            return;
        }

        setupParameters();
    }

    private void playVideo(String strPath) {

		/* Open media source */

		int nSourceFlag = voOSType.VOOSMP_FLAG_SOURCE_URL;
		if (m_chbAsync == null)
			nSourceFlag |= voOSType.VOOSMP_FLAG_SOURCE_OPEN_SYNC;
		else if (m_chbAsync.isChecked())
		    nSourceFlag |= voOSType.VOOSMP_FLAG_SOURCE_OPEN_ASYNC;
		else
		    nSourceFlag |= voOSType.VOOSMP_FLAG_SOURCE_OPEN_SYNC;

		TimeCal.printTime("Open --->");
		int nRet = m_osbasePlayer.Open(strPath, nSourceFlag, 0, 0, 0);
		TimeCal.printTime("Open <---");
		if (nRet == voOSType.VOOSMP_ERR_None) {
			voLog.v(TAG, "MediaPlayer is Opened.");
		} else {
			onError(m_osbasePlayer, nRet, 0);
			return;
		}

		/* Run (play) media pipeline */

		/* If Sync open */
        if ((nSourceFlag & voOSType.VOOSMP_FLAG_SOURCE_OPEN_ASYNC) == 0)
        {
            /* Run (play) media pipeline */
            TimeCal.printTime("Run --->");
            nRet = m_osbasePlayer.Run();
            TimeCal.printTime("Run <---");
            if (nRet == voOSType.VOOSMP_ERR_None) {
                voLog.v(TAG, "MediaPlayer run sync");
            } else {
                onError(m_osbasePlayer, nRet, 0);
            }

            m_nPos = m_osbasePlayer.GetPos();
            m_nDuration = m_osbasePlayer.GetDuration();

            updatePosDur();

            m_bStoped = false;
            m_ibPlayPause.setImageResource(R.drawable.pause_button);

            m_autoSwitchVideo.startAutoSwitchVideo();

        }

	    m_tvCurrentTime.setText("00:00");
	    m_sbMain.setProgress(0);

		// Show wait icon
		m_pbLoadingProgress.setVisibility(View.VISIBLE);
	}

	private void stopVideo() {
	    m_bPaused = false;
        m_bStoped = true;

        if (m_autoSwitchVideo != null)
            m_autoSwitchVideo.stopAutoSwitchVideo();

        if (m_osbasePlayer == null)
            return;

       	TimeCal.printTime("Stop --->");
        m_osbasePlayer.Stop();
        TimeCal.printTime("Stop <---");

        TimeCal.printTime("Close --->");
        m_osbasePlayer.Close();
        TimeCal.printTime("Close <---");

        voLog.v(TAG, "MediaPlayer stoped and closed.");
   	}

	/* Stop playback, close media source, and uninitialize SDK player */
	private void uninitPlayer() {

		if (m_osbasePlayer == null)
		    return;

		TimeCal.printTime("Uninit --->");
		m_osbasePlayer.Uninit();
		TimeCal.printTime("Uninit <---");
		m_osbasePlayer = null;

		m_autoSwitchVideo = null;
		m_assetOpMain = null;

		voLog.v(TAG, "MediaPlayer released.");
	}

	private void stopAndFinish() {
	    stopVideo();
	    uninitPlayer();
	    this.finish();
	}

	/* Display error messages and stop player */
	private boolean onError(voOSBasePlayer mp, int what, int extra) {
	    voLog.v(TAG, "Error message, what is " + what + " extra is " + extra);

	    stopVideo();
        uninitPlayer();

		String errStr = getString(R.string.str_ErrPlay_Message) + "\nError code is " + Integer.toHexString(what);

		// Dialog to display error message; stop player and exit on Back key or "OK"
		AlertDialog.Builder adb = new AlertDialog.Builder(Player.this);
		adb.setIcon(R.drawable.icon);
		adb.setTitle(R.string.str_ErrPlay_Title);
		adb.setMessage(errStr);
		adb.setPositiveButton(R.string.str_OK, new OnClickListener() {
            public void onClick(DialogInterface a0, int a1) {
                Player.this.finish();
            }
		});
		adb.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog,
                    int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    Player.this.finish();

                    return true;
                }

                return false;
            }

        });

		AlertDialog ad = adb.create();
		ad.show();

		return true;

	}

	/* Toggle Play/Pause button display and functionality */
	private void playerPauseRestart() {

	    if (m_osbasePlayer == null)
	        return;

	    if( m_bPaused==false) {

	        // If playing, pause media pipeline, show media controls, and change button to "Play" icon
			m_osbasePlayer.Pause();
			showMediaController();
			m_ibPlayPause.setImageResource(R.drawable.play_button);
			m_bPaused = true;

		} else if(m_bPaused==true) {

		    // Else, play media pipeline and change button to "Pause" icon
			m_osbasePlayer.Run();
			m_ibPlayPause.setImageResource(R.drawable.pause);
			m_bPaused = false;

		}

	}

	private void updatePosDur() {

	    if (m_nDuration <= 0) {
            m_tvCurrentTime.setTextColor(getResources().getColor(R.color.darkgray));
            m_tvTotalTime.setTextColor(getResources().getColor(R.color.darkgray));
            m_tvTotalTime.setText("00:00");
            m_sbMain.setEnabled(false);
        } else {
            m_tvCurrentTime.setTextColor(getResources().getColor(android.R.color.white));
            m_tvTotalTime.setTextColor(getResources().getColor(android.R.color.white));
            m_tvTotalTime.setText(DateUtils.formatElapsedTime(m_nDuration / 1000));
            m_sbMain.setEnabled(true);
        }
	}

	/* Retrieve list of media sources */
	private void ReadUrlInfo() {
		voLog.i(TAG, "Current external storage directory is %s", Environment.getExternalStorageDirectory().getAbsolutePath());
		String str = Environment.getExternalStorageDirectory().getAbsolutePath() + "/url.txt";
		if (CommonFunc.ReadUrlInfoToList(m_lstSelectURL, str) == false)
		    Toast.makeText(this, "Could not find " + str, Toast.LENGTH_LONG).show();
	}

	private void ReadSubtitleInfo() {
	    String str = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ttml_url.txt";
	    if (CommonFunc.ReadUrlInfoToList(m_lstSelectSubtitle, str) == false)
	        Toast.makeText(this, "Could not find " + str, Toast.LENGTH_LONG).show();
	}

    private boolean enableTestChannelSwitchPerf() {
        return m_bChannelSwitchPerf;
    }

    void initLayoutLeft() {

        m_rlChannel = (RelativeLayout) findViewById(R.id.rlChannel);
        m_lvChannel = (ListView) findViewById(R.id.lvChannel);

        if (m_bEnableChannel) {
            m_rlChannel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    resetUIDisplayStartTime();

                    if (m_lvChannel.getVisibility() == View.INVISIBLE) {
                        m_lvChannel.setVisibility(View.VISIBLE);
                        m_rlChannel.setBackgroundResource(R.drawable.bg_player_left_channel_on);
                        fillChannelListContent();
                    } else {
                        m_lvChannel.setVisibility(View.INVISIBLE);
                        m_rlChannel.setBackgroundResource(R.drawable.bg_player_left_channel_off);
                    }

                }
            });
        } else {
            m_rlChannel.setVisibility(View.GONE);
            m_lvChannel.setVisibility(View.GONE);
        }

    }

    private void fillChannelListContent() {

        if (m_lvChannel.getAdapter() == null) {

            if (m_lstSelectURL == null) {
                m_lstSelectURL= new ArrayList<String>();
                ReadUrlInfo();
            }

            ArrayList<HashMap<String, String>> urlList = new ArrayList<HashMap<String, String>>();
            for (int i = 0; i < m_lstSelectURL.size(); i++) {
                HashMap<String, String> urlHash = new HashMap<String, String>();
                urlHash.put("url", m_lstSelectURL.get(i));
                urlList.add(urlHash);
            }

            m_lvChannel.setAdapter(new SimpleAdapter(this, urlList,
                    R.layout.simplelistitem1, new String[] { "url" },
                    new int[] { R.id.tvUrl }));

            m_lvChannel.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1,
                                int arg2, long arg3) {

                            if (enableTestChannelSwitchPerf() == false) {
                                stopVideo();
                                playVideo(m_lstSelectURL.get(arg2));
                            } else { // for QA test purpuse
                                stopVideo();
                                uninitPlayer();

                                initPlayer(m_lstSelectURL.get(arg2));
                                playVideo(m_lstSelectURL.get(arg2));
                            }
                        }
                    });

        }
    }

}
