/************************************************************************
VisualOn Proprietary
Copyright (c) 2012, VisualOn Incorporated. All Rights Reserved

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.visualon.OSMPBasePlayer.*;
import com.visualon.OSMPSubTitle.voSubTitleManager.*;
import com.visualon.OSMPUtils.voLog;
import com.visualon.OSMPUtils.voOSType.VOOSMP_SOURCE_STREAMTYPE;
import com.visualon.OSMPUtils.voOSProgramInfo;
import com.visualon.OSMPUtils.voOSStreamInfo;
import com.visualon.OSMPUtils.voOSSubtitleLanguage;
import com.visualon.OSMPUtils.voOSTrackInfo;
import com.visualon.OSMPUtils.voOSType;

/* Activity implement SurfaceHolder.Callback */
public class Player extends Activity
implements SurfaceHolder.Callback, SeekBar.OnSeekBarChangeListener,
			voOSBasePlayer.onEventListener, voOSBasePlayer.onRequestListener
{
	private static final String	 TAG = "@@@OSMP+Player";		// Tag for voLog messages

	/* Messages for managing the user interface */
	private static final int     MSG_SHOW_CONTROLLER  = 1;
	private static final int     MSG_HIDE_CONTROLLER  = 2;
	private static final int     MSG_UPDATE_UI        = 3;
	private static final int     MSG_PLAYCOMPLETE     = 5;

	/* SurfaceView must be passed to SDK */
	private SurfaceView			 m_svMain;
	private SurfaceHolder		 m_shMain;

	/* Media controls and User interface */
	private ImageButton			 m_ibPlayPause;					// Play/Pause button
	private SeekBar				 m_sbMain;						// Seekbar

	private TextView			 m_tvCurrentTime;				// Current position
	private TextView			 m_tvTotalTime;					// Total duration

	private ProgressBar			 m_pbLoadingProgress = null;	// Wait icon for buffered or stopped video
	private Date				 m_dateUIDisplayStartTime;		// Last update of media controls

	private	Timer 				 m_timerMain         = null;	// Timer for display of media controls
	private TimerTask 			 m_ttMain            = null;	// Timer for display of media controls

	private voOSBasePlayer		 m_osbasePlayer      = null;	// SDK player

	/* Flags */
	private boolean				 m_bTrackProgressing = false;	// Seekbar flag

	private boolean				 m_bPaused           = false;	// Pause flag
	private boolean				 m_bStoped           = false;	// Stop flag

	private String				 m_strVideoPath		 = "";		// URL or file path to media source
	private String               m_strSubtitlePath   = "";

	private int					 m_nVideoWidth = 0;				// Video width
	private int					 m_nVideoHeight = 0;			// video height
	private int					 m_nDuration;					// Total duration
	private int					 m_nPos              = 0;		// Current position
	private TextView			 m_tvCC              = null;	// Closed Captions text

	/* User interface for main view (media source input/selection) */
	private EditText 			 m_edtInputURL;					// User input URL or file path
	private AlertDialog 		 m_adlgMain; 					// Dialog for media source
	private Spinner 			 m_spSelectURL;				    // Media source list selector
	private ArrayList<String> 	 m_lstSelectURL         = null;	// Media source list from url.txt
	private Spinner              m_spSelectSubtitle;            // External subtitle list selector
    private ArrayList<String>    m_lstSelectSubtitle    = null; // External subtitle String list
    private boolean              m_bSpinnerClickAutoTriggered_SelectUrl;
    private boolean              m_bSpinnerClickAutoTriggered_SelectSubtitle;

	private CheckBox             m_chbOMAXAL;
	private CheckBox             m_chbAsync;

	private boolean              m_bEnableVideo         = false; // BpsQuality
	private boolean              m_bEnableAudio         = false; // AudioTrack
	private boolean              m_bEnableSubtitle      = true;  // External/Internal Subtitle or Closed Caption

	private boolean              m_bEnableChannel       = false; // Fast Channel/url switching in the left of player UI.
	private boolean              m_bChannelSwitchPerf   = false; // for QA test purpuse

	private TableLayout          m_rlTop;
	private RelativeLayout       m_rlBottom;
	private RelativeLayout       m_rlRight;
	private ScrollView           m_svRight;
    private LinearLayout         m_llRight;
    private LinearLayout         m_llVideoPopupMenu;
    private LinearLayout         m_llAudioPopupMenu;
    private LinearLayout         m_llSubtitlePopupMenu;
    private HorizontalScrollView m_hsvVideoPopupMenu;
    private HorizontalScrollView m_hsvAudioPopupMenu;
    private HorizontalScrollView m_hsvSubtitlePopupMenu;

    private ImageButton          m_ibBpsQuality;
    private RelativeLayout       m_rlBpsQuality;
    private ImageButton          m_ibAudioTrack;
    private RelativeLayout       m_rlAudioTrack;
    private ImageButton          m_ibSubtitle;
    private RelativeLayout       m_rlSubtitle;

    private RelativeLayout       m_rlChannel;
    private ListView             m_lvChannel;

    private int 			     m_nDownloadErrorTimes = 0;

    private enum AssetType {
        Asset_Video,       // BpsQuality
        Asset_Audio,       // AudioTrack
        Asset_Subtitle,    // External/Internal Subtitle or CloseCaption
    }

    private class AssetInfo {
        int        m_nIndex;    // Index of position on UI
        int        m_nID;       // Stream ID for Video, track ID for Audio, index for Subtitle
        String     m_strDisplay;// Display name on UI
        boolean    m_bPlaying;  // If it is playing

        public AssetInfo(int nIndex, int nID, String strDisplay, boolean bPlaying) {
            m_nIndex     = nIndex;
            m_nID        = nID;
            m_strDisplay = strDisplay;
            m_bPlaying   = bPlaying;
        }
    }

    ArrayList<AssetInfo> m_lstVideoAsset    = new ArrayList<AssetInfo>();
    ArrayList<AssetInfo> m_lstAudioAsset    = new ArrayList<AssetInfo>();
    ArrayList<AssetInfo> m_lstSubtitleAsset = new ArrayList<AssetInfo>();

    private static int INDEX_NONE = -1;

	private Handler handler = new Handler() {
		/* Handler to manage user interface during playback */
		public void handleMessage(Message msg)
		{

			if(m_osbasePlayer == null)
				return;

			if (msg.what == MSG_SHOW_CONTROLLER)
			{
				/* Show media controls */
				showMediaControllerImpl();
			}else if (msg.what == MSG_HIDE_CONTROLLER)
			{
				/* Hide media controls */
				hideControllerImpl();
			}else if (msg.what == MSG_UPDATE_UI)
			{
				// Update UI
				// If the controls are not visible do not update
				if(m_rlBottom.getVisibility() != View.VISIBLE)
					return;

				/* If the player is stopped do not update */
				if (m_bStoped)
					return;

				/* If the user is dragging the progress bar do not update */
				if (m_bTrackProgressing)
					return;

				Date timeNow = new Date(System.currentTimeMillis());
				long timePeriod = (timeNow.getTime() - m_dateUIDisplayStartTime.getTime())/1000;
				if (timePeriod >= 7 && m_bPaused == false)
				{
					/* If the media is being played back, hide media controls after 7 seconds if unused */
					hideController();
					return;
				}

				/* update the Seekbar and Time display with current position */
				m_nPos = m_osbasePlayer.GetPos();
				if (m_nDuration <= 0)
				{
				    m_tvCurrentTime.setText("00:00");
	                m_tvCurrentTime.setTextColor(getResources().getColor(R.color.darkgray));
	                m_tvTotalTime.setText("00:00");
	                m_tvTotalTime.setTextColor(getResources().getColor(R.color.darkgray));
	                m_sbMain.setEnabled(false);

				} else {
					m_sbMain.setProgress(m_nPos/100*100/(m_nDuration/100));
					String str = DateUtils.formatElapsedTime(m_nPos/1000);
					m_tvCurrentTime.setText(str);
				}

		  } else if (msg.what == MSG_PLAYCOMPLETE) {
			  /* Playback in complete, stop player */
			  //    setResult(RESULT_OK, (new Intent()).setAction("finish"));
		      stopAndFinish();

		  }
		}
	};

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		voLog.v(TAG, "Player onCreate");

		/*Screen always on*/
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,  WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.player);

		initUI();

		Uri uri = getIntent().getData();
		if (uri != null)
		{
			// If media source was passed as URI data, use it
			m_strVideoPath = uri.getPath();

			int i = m_strVideoPath.indexOf("/mnt/");
			if (i != -1)
			{
				//local file
				m_strVideoPath = m_strVideoPath.subSequence(i, m_strVideoPath.length()).toString();
			}
			else
			{
				//stream
				m_strVideoPath = uri.toString();
			}

			voLog.v(TAG, "Source URI: " + m_strVideoPath);
		}else
		{
			// Else bring up main view to input/select media source
			SourceWindow();
		}

		getWindow().setFormat(PixelFormat.UNKNOWN);

		// Find View and UI objects
		m_svMain = (SurfaceView) findViewById(R.id.svMain);
		m_shMain = m_svMain.getHolder();
		m_shMain.addCallback(this);
//		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
		m_shMain.setFormat(PixelFormat.RGBA_8888);

		m_ibPlayPause     = (ImageButton)findViewById(R.id.ibPlayPause);
		m_sbMain          = (SeekBar)findViewById(R.id.sbMain);
		m_tvCurrentTime   = (TextView)findViewById(R.id.tvCurrentTime);
		m_tvTotalTime     = (TextView)findViewById(R.id.tvTotalTime);
		m_pbLoadingProgress = (ProgressBar) findViewById(R.id.pbBuffer);

		m_sbMain.setOnSeekBarChangeListener(this);

		voLog.v(TAG, "Video source is " + m_strVideoPath);

		m_rlTop.setVisibility(View.INVISIBLE);
		m_rlBottom.setVisibility(View.INVISIBLE);
		m_pbLoadingProgress.setVisibility(View.GONE);

		// Activate listener for Play/Pause button
		m_ibPlayPause.setOnClickListener(new ImageButton.OnClickListener()
								{
									public void onClick(View view)
									{
										playerPauseRestart();
									}
								});
		// Copy license file,
		copyfile(this, "cap.xml", "cap.xml");
	}

	/* Interface to input/select media source */
	private void SourceWindow()
	{
		LayoutInflater inflater;
		View layout;
		inflater = LayoutInflater.from(Player.this);
		layout = inflater.inflate(R.layout.url, null);

		// Dialog to input source URL or file path
		m_adlgMain = new AlertDialog.Builder(Player.this)
						.setIcon(R.drawable.hyperlink)
						.setTitle(R.string.str_URL)
						.setView(layout)
						.setNegativeButton(R.string.str_Cancel, new OnClickListener()
						{
							// ¡°Cancel¡± button stops player and exits
							public void onClick(DialogInterface dialog, int which)
							{
							    stopAndFinish();
							}
						})
						.setPositiveButton(R.string.str_OK, new OnClickListener()
							{
								// ¡°OK¡± button begins playback of inputted media source
								public void onClick(DialogInterface dialog, int which)
								{
								    // editbox is prefered
                                    if (m_edtInputURL.getText().toString().length() != 0)
                                        m_strVideoPath = m_edtInputURL.getText().toString();

                                    initPlayer(m_strVideoPath);
                                    playVideo(m_strVideoPath);
								}
							}
						)
						.setOnKeyListener(new OnKeyListener()
							{
								public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2)
								{
									// ¡°Back¡± button stops player and exits
									// TODO Auto-generated method stub
									if (arg1 == KeyEvent.KEYCODE_BACK)
									{
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

		m_edtInputURL      = (EditText)m_adlgMain.findViewById(R.id.edtInputURL);
		m_spSelectURL      = (Spinner)m_adlgMain.findViewById(R.id.spSelectURL);
		m_spSelectSubtitle = (Spinner)m_adlgMain.findViewById(R.id.spSelectSubtitle);

		//m_chbOMAXAL        = (CheckBox)m_adlgMain.findViewById(R.id.chbOMAXAL);
		//m_chbAsync         = (CheckBox)m_adlgMain.findViewById(R.id.chbAsync);

		// Retrieve URL list
		m_lstSelectURL= new ArrayList<String>();
		ReadUrlInfo();

		final ArrayList<String> arrURLListWithTitle = new ArrayList<String>(m_lstSelectURL);
		arrURLListWithTitle.add(0, getResources().getString(R.string.str_SelURL_FirstLine));

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		        android.R.layout.simple_spinner_item, arrURLListWithTitle);

		m_spSelectURL.setAdapter(adapter);
		m_spSelectURL.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			// When item is selected from URL list, begin playback of selected item
			public void onItemSelected(AdapterView<?> arg0, View arg1,
			int arg2, long arg3)
			{
				if (m_bSpinnerClickAutoTriggered_SelectUrl == false)
				{
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
        m_lstSelectSubtitle= new ArrayList<String>();
        ReadSubtitleInfo();

        final ArrayList<String> arrSubtitleListWithTitle = new ArrayList<String>(m_lstSelectSubtitle);
        arrSubtitleListWithTitle.add(0, getResources().getString(R.string.str_SelSubtitle_FirstLine));

        ArrayAdapter<String> adptSubtitle = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arrSubtitleListWithTitle);

        m_spSelectSubtitle.setAdapter(adptSubtitle);
        m_spSelectSubtitle.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                if (m_bSpinnerClickAutoTriggered_SelectSubtitle == false)
                {
                    m_bSpinnerClickAutoTriggered_SelectSubtitle = true;
                    return;
                }

                int nPos = m_spSelectSubtitle.getSelectedItemPosition();
                m_strSubtitlePath = arrSubtitleListWithTitle.get(nPos);

            }

            public void onNothingSelected(AdapterView<?> arg0) {}
        });
	}



	/* Notify SDK of configuration change */
    public void onConfigurationChanged(Configuration newConfig) {

	    if (m_osbasePlayer == null)
	    {
	        super.onConfigurationChanged(newConfig);
	        return;
	    }

	    // Retrieve new display metrics
	    DisplayMetrics dm  = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        ViewGroup.LayoutParams lp = m_svMain.getLayoutParams();
        lp.width = dm.widthPixels;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
        	// If landscape orientation, use display metrics height
            lp.height = dm.heightPixels;
            m_osbasePlayer.updateVideoAspectRatio(m_nVideoWidth, m_nVideoHeight);

        }else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
        	// If portrait orientation, scale height as a ratio of the original aspect ratio
            lp.height = dm.widthPixels * m_nVideoHeight / m_nVideoWidth;
        }
        // Pass new width/height to View
        m_svMain.setLayoutParams(lp);

        super.onConfigurationChanged(newConfig);
    }

    /* Initialize SDK player and playback selected media source */

    private void initPlayer(String strPath)
    {
        int nRet;

        m_osbasePlayer = new voOSBasePlayer();

        m_shMain.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);

        // Location of libraries
        String apkPath = getUserPath(this) + "/lib/";

        String cfgPath = getUserPath(this) + "/";


        // SDK player engine type
        int nEngineType = voOSType.VOOSMP_VOME2_PLAYER;

        if (m_chbOMAXAL != null)
            nEngineType = m_chbOMAXAL.isChecked() ?
                voOSType.VOOSMP_OMXAL_PLAYER : voOSType.VOOSMP_VOME2_PLAYER;

        // Initialize SDK player
        nRet = m_osbasePlayer.Init(this, apkPath, null, nEngineType, 0, 0);

        DisplayMetrics dm  = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        // Set display size
        m_osbasePlayer.SetDisplaySize(dm.widthPixels, dm.heightPixels);

        // Set view
        m_osbasePlayer.SetView(m_svMain);
        /* Set the license */
        String licenseText = "VOTRUST_OOYALA_754321974";        // Magic string from VisualOn, must match voVidDec.dat to work
        m_osbasePlayer.SetParam(voOSType.VOOSMP_PID_LICENSE_TEXT, licenseText);
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

        // Register SDK event listener
        m_osbasePlayer.setEventListener(this);

        m_osbasePlayer.setRequestListener(this);

        if (nRet == voOSType.VOOSMP_ERR_None)
        {
            voLog.v(TAG, "MediaPlayer is created.");
        }else
        {
            onError(m_osbasePlayer, nRet, 0);
            return;
        }

        /* Configure DRM parameters */
        m_osbasePlayer.SetParam(voOSType.VOOSMP_SRC_PID_DRM_FILE_NAME, "voDRM");
        m_osbasePlayer.SetParam(voOSType.VOOSMP_SRC_PID_DRM_API_NAME, "voGetDRMAPI");

        /* Configure Dolby Audio Effect parameters */
        m_osbasePlayer.SetParam(voOSType.VOOSMP_PID_AUDIO_EFFECT_ENABLE, 0);

        /* Processor-specific settings */
        String capFile = cfgPath + "cap.xml";
        m_osbasePlayer.SetParam(voOSType.VOOSMP_SRC_PID_CAP_TABLE_PATH, capFile);

        //Enable closed caption
        m_osbasePlayer.SetParam(voOSType.VOOSMP_PID_COMMON_CCPARSER, 1);

        if (m_strSubtitlePath != null && m_strSubtitlePath.length() > 0)
            m_osbasePlayer.SetParam(voOSType.VOOSMP_PID_SUBTITLE_FILE_NAME, m_strSubtitlePath);
    }

    private void playVideo(String strPath)
	{
		int nRet;

		/* Open media source */

		int nSourceFlag = voOSType.VOOSMP_FLAG_SOURCE_URL;
		if (m_chbAsync == null)
		{
			nSourceFlag |= voOSType.VOOSMP_FLAG_SOURCE_OPEN_SYNC;
		}else if (m_chbAsync.isChecked())
		    nSourceFlag |= voOSType.VOOSMP_FLAG_SOURCE_OPEN_ASYNC;
		else
		    nSourceFlag |= voOSType.VOOSMP_FLAG_SOURCE_OPEN_SYNC;

		nRet = m_osbasePlayer.Open(strPath, nSourceFlag, 0, 0, 0);
		if (nRet == voOSType.VOOSMP_ERR_None)
		{
			voLog.v(TAG, "MediaPlayer is Opened.");
		}else
		{
			onError(m_osbasePlayer, nRet, 0);
			return;
		}

		/* Run (play) media pipeline */
		nRet = m_osbasePlayer.Run ();

		if (nRet == voOSType.VOOSMP_ERR_None)
		{
			voLog.v(TAG, "MediaPlayer run.");
		}else
		{
			onError(m_osbasePlayer, nRet, 0);
		}

		m_bStoped = false;

		m_nDuration = m_osbasePlayer.GetDuration();
	    String strTotalTime = DateUtils.formatElapsedTime(m_nDuration / 1000);
	    m_tvTotalTime.setText(strTotalTime);
	    m_tvCurrentTime.setText("00:00");
	    m_sbMain.setProgress(0);

		// Show wait icon
		m_pbLoadingProgress.setVisibility(View.VISIBLE);
	}

    /* Notify SDK on Surface Change */
	public void surfaceChanged (SurfaceHolder surfaceholder, int format, int w, int h)
	{
		// TODO Auto-generated method stub
		voLog.i(TAG, "Surface Changed");
		if (m_osbasePlayer != null)
			m_osbasePlayer.SetParam(voOSType.VOOSMP_PID_SURFACE_CHANGED, 1);
	}

	/* Notify SDK on Surface Creation */
	public void surfaceCreated(SurfaceHolder surfaceholder)
	{
		voLog.i(TAG, "Surface Created");
		if (m_osbasePlayer !=null)
		{
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

	public void surfaceDestroyed(SurfaceHolder surfaceholder)
	{
		// TODO Auto-generated method stub
		voLog.i(TAG, "Surface Destroyed");

		if (m_osbasePlayer !=null)
        {
		    m_osbasePlayer.SetView(null);
        }
	}

	private void stopVideo()
	{
	    m_bPaused = false;
        m_bStoped = true;

        if (m_osbasePlayer != null)
        {
            m_osbasePlayer.Stop();
            m_osbasePlayer.Close();

            voLog.v(TAG, "MediaPlayer stoped.");
        }

 	}

	/* Stop playback, close media source, and uninitialize SDK player */
	public void uninitPlayer()
	{

		if (m_osbasePlayer != null)
		{

			m_osbasePlayer.Uninit();
			m_osbasePlayer = null;

			voLog.v(TAG, "MediaPlayer released.");
		}

	}

	private void stopAndFinish() {
	    stopVideo();
	    uninitPlayer();
	    this.finish();
	}

	@Override
	protected void onDestroy()
	{
		// Finish GLView rendering if 3D animation is enabled.  Do it before
		// calling parent class onDestroy function.
		super.onDestroy();
		// TODO Auto-generated method stub
		voLog.v(TAG, "Player onDestroy Completed!");
	}

	/* Pause/Stop playback on activity pause */
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
		voLog.v(TAG, "Player onPause");

		if (m_osbasePlayer!= null)// && mMediaPlayer.getStatus() == voPlayer.ePlayerState.eps_running)
		{
			if(m_osbasePlayer.GetDuration()<=0)
			{
				// If for live streaming, we stop playbacking directly.
			    stopAndFinish();
			}
			else
			{
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
	protected void onRestart()
	{
		// TODO Auto-generated method stub
		voLog.v(TAG, "Player onRestart");

		super.onRestart();
		if(m_osbasePlayer == null)
			return;
	}

	/* Show media controller. Implemented by showMediaControllerImpl(), called by handler */
	private void showMediaController()
	{
		if(m_osbasePlayer == null)
			return;

		if(m_osbasePlayer.GetDuration() <=0)
		{
			voLog.v(TAG, "live module, Don't show control bar!");
			return;
		}

		handler.sendEmptyMessage(MSG_SHOW_CONTROLLER);
	}

	/* Show media controller implementation */
	private void showMediaControllerImpl()
	{
		voLog.v(TAG, "Touch screen, layout status is " +m_rlBottom.getVisibility());
		m_dateUIDisplayStartTime = new Date(System.currentTimeMillis());		// Get current system time

		if (m_rlBottom.getVisibility() != View.VISIBLE)
		{
			voLog.v(TAG, "mIsStop is " + m_bStoped);


			// Schedule next UI update in 200 milliseconds
			if(m_ttMain!=null)
			{
				m_ttMain = null;
			}
			m_ttMain= new TimerTask()
			{
				public void run()
				{
					handler.sendEmptyMessage(MSG_UPDATE_UI);
				}
			};
			if(m_timerMain == null)
			{
				m_timerMain = new Timer();
			}
			m_timerMain.schedule(m_ttMain, 0, 200);

			// Show controls
			m_rlBottom.setVisibility(View.VISIBLE);
			m_rlTop.setVisibility(View.VISIBLE);

			voLog.v(TAG, "m_rlTop show " + m_rlTop.getVisibility());
		}
	}

	/* Show media controls on activity touch event */
	public boolean onTouchEvent(MotionEvent event)
	{
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
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		// TODO Auto-generated method stub
		voLog.v(TAG, "Key click is " + keyCode);

		if (keyCode ==KeyEvent.KEYCODE_BACK)
		{
			voLog.v(TAG, "Key click is Back key");
			stopAndFinish();

			return super.onKeyDown(keyCode, event);
		}

		return super.onKeyDown(keyCode, event);
	}

	/* (non-Javadoc)
	* @see android.app.Activity#onStart()
	*/
	@Override
	protected void onStart()
	{
		// TODO Auto-generated method stub
		voLog.v(TAG, "Player onStart");
		super.onStart();
	}

	/* (non-Javadoc)
	* @see android.app.Activity#onStop()
	*/
	@Override
	protected void onStop()
	{
		// TODO Auto-generated method stub
		voLog.v(TAG, "Player onStop");
		super.onStop();
	}


	/* Hide media controller. Implemented by showMediaControllerImpl(), called by handler */
	public void hideController()
	{
		handler.sendEmptyMessage(MSG_HIDE_CONTROLLER);
	}

	/* Hide media controller implementation */
	public void hideControllerImpl()
	{
		if(m_timerMain != null)
		{
			m_timerMain.cancel();
			m_timerMain.purge();
			m_timerMain = null;
			m_ttMain = null;
		}

		m_rlBottom.setVisibility(View.INVISIBLE);
		m_rlTop.setVisibility(View.INVISIBLE);
	}

	/* Display error messages and stop player */
	public boolean onError(voOSBasePlayer mp, int what, int extra)
	{
	    voLog.v(TAG, "Error message, what is " + what + " extra is " + extra);

	    stopVideo();
        uninitPlayer();

		String errStr = getString(R.string.str_ErrPlay_Message) + "\nError code is " + Integer.toHexString(what);

		// Dialog to display error message; stop player and exit on Back key or ¡°OK¡±
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

                if (keyCode == KeyEvent.KEYCODE_BACK)
                {
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


	/* Not implemented. Update display while Seekbar is being dragged */
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2)
	{
		// Implement this method to update the display when the Seekbar is being dragged

	}

	/* Flag when Seekbar is being dragged */
	public void onStartTrackingTouch(SeekBar arg0)
	{
		// TODO Auto-generated method stub
        m_bTrackProgressing = true;
	}

	/* Seek to new position when Seekbar drag is complete */
	public void onStopTrackingTouch(SeekBar arg0)
	{
		// Calculate new position as percentage of total duration
		int iCurrent = arg0.getProgress();
		int iMax = arg0.getMax();
		int iNewPosition = iCurrent*100/iMax*m_nDuration/100;

		m_bTrackProgressing = false;			// Disable Seekbar tracking flag

		if (m_osbasePlayer != null)
		{
			voLog.v(TAG,"Seek To " + iNewPosition);
			m_osbasePlayer.SetPos(iNewPosition);	// Set new position

		}

	}

	/* Toggle Play/Pause button display and functionality */
	private void playerPauseRestart()
	{
		if (m_osbasePlayer != null)
		{
			if(m_bPaused==false)
			{
				// If playing, pause media pipeline, show media controls, and change button to ¡°Play¡± icon
				m_osbasePlayer.Pause();
				showMediaController();
				m_ibPlayPause.setImageResource(R.drawable.play_button);
				m_bPaused = true;
			}
			else if(m_bPaused==true)
			{
				// Else, play media pipeline and change button to ¡°Pause¡± icon
				m_osbasePlayer.Run();
				m_ibPlayPause.setImageResource(R.drawable.pause);
				m_bPaused = false;
			}
		}

	}

	/* SDK event handling */
	public int onEvent(int nID, int nParam1, int nParam2, Object obj)
	{

		voLog.v(TAG, "OnEvent nID is %s, nParam1 is %s, nParam2 is %s",
				Integer.toHexString(nID),Integer.toHexString(nParam1),Integer.toHexString(nParam2));

		if(nID == voOSType.VOOSMP_SRC_CB_Adaptive_Streaming_Error){
			if(nParam1==4)//VO_SOURCE2_ADAPTIVESTREAMING_ERROREVENT_STREAMING_DOWNLOADFAIL
			{
				Log.v(TAG, "VOOSMP_SRC_CB_Adaptive_Streaming_Error, nParam1 is VO_SOURCE2_ADAPTIVESTREAMING_ERROREVENT_STREAMING_DOWNLOADFAIL");
			}
			return 0;

		}
		if(nID == voOSType.VOOSMP_SRC_CB_Adaptive_Stream_Warning){
			if( nParam1 == voOSType.VOOSMP_SRC_ADAPTIVE_STREAMING_WARNING_EVENT_CHUNK_DOWNLOADERROR ){
				Log.v(TAG, "VOOSMP_SRC_CB_Adaptive_Stream_Warning, nParam1 is VOOSMP_SRC_ADAPTIVE_STREAMING_WARNING_EVENT_CHUNK_DOWNLOADERROR");
				m_nDownloadErrorTimes ++;
				if(m_nDownloadErrorTimes>60){
					Log.v(TAG, "VOOSMP_SRC_CB_Adaptive_Stream_Warning over 60 times");

					//user can report an error when mDownloadErrorTimes over a value
					//onError(mPlayer, nID, 0);
				}
			}
			else{
				Log.v(TAG, "VOOSMP_SRC_CB_Adaptive_Stream_Warning, nParam1 is "+nParam1);
			}
			return 0;
		}
		if(nID == voOSType.VOOSMP_SRC_CB_Adaptive_Streaming_Info)
	    {
	        switch(nParam1)
	        {
	            case voOSType.VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_BITRATE_CHANGE:
	            {
	             voLog.v(TAG, "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_BITRATE_CHANGE, param2 is %d . ", nParam2);
	             break;
	            }
	            case voOSType.VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE:
	            {
	             voLog.v(TAG, "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, param2 is %d . ", nParam2);

	             switch(nParam2)
	             {
	             case voOSType.VOOSMP_AVAILABLE_PUREAUDIO:
	             {
		             voLog.v(TAG, "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_PUREAUDIO");
	            	 break;
	             }
	             case voOSType.VOOSMP_AVAILABLE_PUREVIDEO:
	             {
		             voLog.v(TAG, "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_PUREVIDEO");
	            	 break;
	             }
	             case voOSType.VOOSMP_AVAILABLE_AUDIOVIDEO:
	             {
		             voLog.v(TAG, "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_AUDIOVIDEO");
	            	 break;
	             }
	             }
	             break;
	            }
	        }
	    }
		if (nID == voOSType.VOOSMP_CB_Error)					// Error
		{
			// Display error dialog and stop player
			onError(m_osbasePlayer, 1, 0);
			return 0;
		}
		else if (nID == voOSType.VOOSMP_CB_PlayComplete)
		{
			handler.sendEmptyMessage(MSG_PLAYCOMPLETE);
			return 0;
		}
		else if (nID == voOSType.VOOSMP_CB_SeekComplete)		// Seek (SetPos) complete
		{
			return 0;
		}
		else if (nID == voOSType.VOOSMP_CB_BufferStatus)		// Updated buffer status
		{
			return 0;
		}
		else if (nID == voOSType.VOOSMP_CB_VideoSizeChanged)	// Video size changed
		{
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

			return 0;
		}
		else if (nID == voOSType.VOOSMP_CB_VideoStopBuff)		// Video buffering stopped
		{
			m_pbLoadingProgress.setVisibility(View.GONE);					// Hide wait icon
			return 0;
		}
		else if (nID == voOSType.VOOSMP_CB_VideoStartBuff)		// Video buffering started
		{
			m_pbLoadingProgress.setVisibility(View.VISIBLE);				// Show wait icon
			return 0;
		}else if (nID == voOSType.VOOSMP_CB_AudioStopBuff)
		{
			m_pbLoadingProgress.setVisibility(View.INVISIBLE);
			return 0;
		}
		else if (nID == voOSType.VOOSMP_CB_AudioStartBuff)
		{
			m_pbLoadingProgress.setVisibility(View.VISIBLE);
			return 0;
		}else if (nID == voOSType.VOOSMP_CB_ClosedCaptionData)	// CC data
		{
			// Retrieve subtitle info
			voSubtitleInfo info = (voSubtitleInfo)obj;

			// Sample text in case of missing CC info
			String str =  "Customers totally hande ClosedCaption by themselves.\nFollowing is demo of showing ClosedCaption.\nOnly show text, omit font size, style, color, position etc.\n\n";

			// Retrieve CC text
			str = str + GetCCString(info);
			voLog.i(TAG, "output VOOSMP_CB_ClosedCaptionData, text is %s . &", str);
			if (m_tvCC == null)
			{
				m_tvCC = new TextView(this);
				RelativeLayout rl = (RelativeLayout)(m_svMain.getParent());
				rl.addView(m_tvCC);
				RelativeLayout.LayoutParams rl2 = (RelativeLayout.LayoutParams)m_tvCC.getLayoutParams();

				DisplayMetrics dm  = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(dm);
				rl2.topMargin = (int) (dm.heightPixels * 0.35);
				rl2.leftMargin = (int) (dm.widthPixels * 0.2);
				m_tvCC.setBackgroundColor(R.drawable.translucent_background);

			}

			// Set subtitle text in mCCView
			m_tvCC.setText(str);
			return 0;
		}else if (nID == voOSType.VOOSMP_SRC_CB_Connection_Fail
				|| nID == voOSType.VOOSMP_SRC_CB_Download_Fail
				|| nID == voOSType.VOOSMP_SRC_CB_DRM_Fail
				|| nID == voOSType.VOOSMP_SRC_CB_Playlist_Parse_Err
				|| nID == voOSType.VOOSMP_SRC_CB_Connection_Rejected
				|| nID == voOSType.VOOSMP_SRC_CB_DRM_Not_Secure
				|| nID == voOSType.VOOSMP_SRC_CB_DRM_AV_Out_Fail)	// Errors
		{
			// Display error dialog and stop player
			onError(m_osbasePlayer, nID, 0);

		}else if (nID == voOSType.VOOSMP_SRC_CB_BA_Happened)		// Unimplemented
		{
			voLog.v(TAG, "OnEvent VOOSMP_SRC_CB_BA_Happened, param is %d . ", nParam1);
		}else if (nID == voOSType.VOOSMP_SRC_CB_Download_Fail_Waiting_Recover)
		{
			voLog.v(TAG, "OnEvent VOOSMP_SRC_CB_Download_Fail_Waiting_Recover, param is %d . ", nParam1);
		}else if (nID == voOSType.VOOSMP_SRC_CB_Download_Fail_Recover_Success)
		{
			voLog.v(TAG, "OnEvent VOOSMP_SRC_CB_Download_Fail_Recover_Success, param is %d . ", nParam1);
		}else if (nID == voOSType.VOOSMP_SRC_CB_Open_Finished)
		{
			voLog.v(TAG, "OnEvent VOOSMP_SRC_CB_Open_Finished, param is %d . ", nParam1);
			/*
			if (nParam1 != 0)
			{
				onError(mPlayer, nParam1, 0);

			}else
			{
				int nRet = mPlayer.Run ();

				if (nRet == voOSType.VOOSMP_ERR_None)
				{
					voLog.v(TAG, "MediaPlayer run.");
				}else
				{
					onError(mPlayer, nRet, 0);

				}
			}
			*/
		}

		return 0;
	}

	/* Retrieve list of media sources */
	public void ReadUrlInfoToList(List<String> listUrl, String configureFile)
	{
		String sUrl,line = "";
		sUrl = configureFile;
		listUrl.add("http://player.ooyala.com/player/iphone/91bThhODokcxQNhlk3ttzNZs3HoTZ12M.m3u8?secure_ios_token=QkVsRFYvaWpPdHRDZ3A5Nk9qeW1Fd3JhSWVxbHNhUTYzTlQ2L3lkY1pETWExTnVtbVJHSGFBQ2s5UGxCClRTaDdBSmNnZytFZXBpN1hnaHAzT0Zjb3N3PT0K");
		listUrl.add("http://bpmultihlslive257.ngcdn.telstra.com/protected/C3092-Mobile-FootyTV/index-root-iphone.m3u8?IS=0&ET=1366163155&CIP=1.2.3.4&KO=1&KN=7&US=84b1c1ac3e5dabca333e6011c534c97a");
		File UrlFile = new File(sUrl);
		if (!UrlFile.exists())
		{
			Toast.makeText(this, "Could not find "+sUrl+" file!", Toast.LENGTH_LONG).show();
			return ;
		}
		FileReader fileread;

		try
		{
			fileread = new FileReader(UrlFile);
			BufferedReader bfr = new BufferedReader(fileread);
			try
			{
				while (line != null)
				{
					line = bfr.readLine();
					if (line !=null)
					{
						listUrl.add(line);
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	/* Retrieve list of media sources */
	public void ReadUrlInfo()
	{
		voLog.i(TAG, "Current external storage directory is %s", Environment.getExternalStorageDirectory().getAbsolutePath());

		ReadUrlInfoToList(m_lstSelectURL, Environment.getExternalStorageDirectory().getAbsolutePath() + "/url.txt");
	}

	public void ReadSubtitleInfo()
	{
	    ReadUrlInfoToList(m_lstSelectSubtitle, Environment.getExternalStorageDirectory().getAbsolutePath() + "/ttml_url.txt");
	}

	/* Manage SDK requests */
	public int onRequest(int arg0, int arg1, int arg2, Object arg3) {
		// TODO Auto-generated method stub
		voLog.v(TAG, "OnEvent onRequest nID is %s, nParam1 is %s, nParam2 is %s",
				Integer.toHexString(arg0),Integer.toHexString(arg1),Integer.toHexString(arg2));
		return 0;
	}

	/* Show CC options in menu */
	 public boolean onCreateOptionsMenu(Menu menu)
	 {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	 }


	/* Enable/Disable CC output on options menu selection */
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
		case R.id.O_1:
		    {
		    	// Disable SDK CC output
				if (m_osbasePlayer != null) {
				    m_osbasePlayer.SetParam(voOSType.VOOSMP_PID_CLOSED_CAPTION_OUTPUT, 0);
				}

				// Hide CC text
				if (m_tvCC != null)
					m_tvCC.setVisibility(View.GONE);
				return true;
		    }
		case R.id.O_2:
		    {
				if (m_tvCC != null)
				{
					m_tvCC.setText("");
					m_tvCC.setVisibility(View.VISIBLE);
				}

				// Enable SDK CC output
				if (m_osbasePlayer != null) {
				    m_osbasePlayer.SetParam(voOSType.VOOSMP_PID_CLOSED_CAPTION_OUTPUT, 1);
				}

				// Show CC text
				if (m_tvCC != null)
					m_tvCC.setVisibility(View.VISIBLE);
				return true;
		    }
		default:
		        return super.onOptionsItemSelected(item);
		}
	}

	/* Extract text string from CC data. Does not handle position, color, font type, etc. */
	public String GetCCString(voSubtitleInfo subtitleInfo)
	{
		if(subtitleInfo == null)
			return "";
		if(subtitleInfo.getSubtitleEntry() == null)
			return "";

		String strTextAll = "";
		for(int i = 0; i<subtitleInfo.getSubtitleEntry().size(); i++)
		{
			// Retrieve the display info for each subtitle entry
			voSubtitleInfoEntry info = subtitleInfo.getSubtitleEntry().get(i);
			voSubtitleDisplayInfo dispInfo = info.getSubtitleDispInfo();
			if(dispInfo.getTextRowInfo() != null)
			{
				for(int j = 0; j < dispInfo.getTextRowInfo().size() ; j++)
				{
					// Retrieve the row info for display
					voSubtitleTextRowInfo rowInfo = dispInfo.getTextRowInfo().get(j);
					if( rowInfo == null)
						continue;
					if( rowInfo.getTextInfoEntry() == null)
						continue;

					String strRow = "";
					for(int k = 0; k < rowInfo.getTextInfoEntry().size() ; k++)
					{
						// Get the string for each row
						strRow+=rowInfo.getTextInfoEntry().get(k).getStringText();//.stringText;
					}
					if(strRow.length()>0)
					{
						if(strTextAll.length()>0)
							strTextAll+="\n";
						strTextAll+=strRow;

					}

				}
			}
		}
		return strTextAll;
	}

	/* Copy file from Assets directory to destination. Used for licenses and processor-specific configurations */
	private static void copyfile(Context context, String filename, String desName)
	{
		try {
			InputStream InputStreamis  = context.getAssets().open(filename);
			File desFile = new File(getUserPath(context) + "/" + desName);
			desFile.createNewFile();
			FileOutputStream  fos = new FileOutputStream(desFile);
			int bytesRead;
			byte[] buf = new byte[4 * 1024]; //4K buffer
			while((bytesRead = InputStreamis.read(buf)) != -1) {
			fos.write(buf, 0, bytesRead);
			}
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getUserPath(Context context)
    {
    	PackageManager m = context.getPackageManager();
		String path = context.getPackageName();
		String userPath = "/data/data/" + path;
		try
		{
		       PackageInfo p = m.getPackageInfo(path, 0);
		       userPath = p.applicationInfo.dataDir;
		} catch (NameNotFoundException e) {
		}
		return userPath;

    }


	private void resetUIDisplayStartTime() {
	   m_dateUIDisplayStartTime = new Date(System.currentTimeMillis());
	}


    private void initUI() {

        m_rlTop    = (TableLayout)    findViewById(R.id.tlTop);
        m_rlBottom = (RelativeLayout) findViewById(R.id.rlBottom);

        initLayoutRight();
        initLayoutLeft();

    }

    private void initLayoutRight() {

        m_rlRight        = (RelativeLayout) findViewById(R.id.rlRight);

        m_svRight        = (ScrollView)  findViewById(R.id.svRight);
        m_llRight        = (LinearLayout)findViewById(R.id.llRight);
        m_ibSubtitle	 = (ImageButton) findViewById(R.id.ibCloseCaptionSelector);
        m_rlSubtitle	 = (RelativeLayout) findViewById(R.id.rlCloseCaptionSelector);
        m_ibBpsQuality   = (ImageButton) findViewById(R.id.ibBpsQuality);
        m_rlBpsQuality   = (RelativeLayout) findViewById(R.id.rlBpsQuality);
        m_ibAudioTrack   = (ImageButton) findViewById(R.id.ibAudioTrack);
        m_rlAudioTrack   = (RelativeLayout) findViewById(R.id.rlAudioTrack);

        if (!enableSubtitle() && !enableVideo() && !enableAudio()) {
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

        if (enableSubtitle()) {

            m_ibSubtitle.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (m_osbasePlayer == null)
                    return;

                resetUIDisplayStartTime();

                int nVisible = m_hsvSubtitlePopupMenu.getVisibility();
                if (nVisible == View.VISIBLE) {
                    m_hsvSubtitlePopupMenu.setVisibility(View.INVISIBLE);
                }
                else  {
                    resetLayoutRightPopupMenu();
                    m_llSubtitlePopupMenu.removeAllViews();

                    querySubtitleAsset(m_lstSubtitleAsset);

                    inflatePopupMenu(m_llSubtitlePopupMenu, m_lstSubtitleAsset, AssetType.Asset_Subtitle);
                    setPopupMenuState(m_llSubtitlePopupMenu, m_lstSubtitleAsset);

                    m_llSubtitlePopupMenu.scheduleLayoutAnimation();
                    m_hsvSubtitlePopupMenu.setVisibility(View.VISIBLE);
                }

            }
            });

        } else {
            m_rlSubtitle.setVisibility(View.GONE);
        }

        if (enableVideo()) {

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

                    queryVideoAsset(m_lstVideoAsset);

                    inflatePopupMenu(m_llVideoPopupMenu, m_lstVideoAsset, AssetType.Asset_Video);
                    setPopupMenuState(m_llVideoPopupMenu, m_lstVideoAsset);

                    m_llVideoPopupMenu.scheduleLayoutAnimation();
                    m_hsvVideoPopupMenu.setVisibility(View.VISIBLE);
                }

            }
            });

        } else {
            m_rlBpsQuality.setVisibility(View.GONE);
        }

        if (enableAudio()) {

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

                    queryAudioAsset(m_lstAudioAsset);

                    inflatePopupMenu(m_llAudioPopupMenu, m_lstAudioAsset, AssetType.Asset_Audio);
                    setPopupMenuState(m_llAudioPopupMenu, m_lstAudioAsset);

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

                    onAssetClick(((Integer)view.getTag()).intValue(), type);

                }
            });

            tvBuffer.setText( ((ArrayList<AssetInfo>)lstT).get(i).m_strDisplay);

            tvBuffer.setTag(i);
        }
    }

    // Right layout functions
    private void initLayoutRightPopupMenu() {
        m_llSubtitlePopupMenu = (LinearLayout) findViewById(R.id.llCloseCaptionSelectorPopupMenu);
        m_llVideoPopupMenu = (LinearLayout) findViewById(R.id.llBpsQualityPopupMenu);
        m_llAudioPopupMenu = (LinearLayout) findViewById(R.id.llAudioTrackPopupMenu);

        m_hsvSubtitlePopupMenu = (HorizontalScrollView) findViewById(R.id.hsvCloseCaptionSelectorPopupMenu);
        m_hsvSubtitlePopupMenu.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View view, MotionEvent motionevent) {
                if(motionevent.getAction() == MotionEvent.ACTION_MOVE)
                    resetUIDisplayStartTime();

                return false;
            }

        });

        m_hsvVideoPopupMenu = (HorizontalScrollView) findViewById(R.id.hsvBpsQualityPopupMenu);
        m_hsvVideoPopupMenu.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View view, MotionEvent motionevent) {
                if(motionevent.getAction() == MotionEvent.ACTION_MOVE)
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

        if (!enableSubtitle() && !enableVideo() && !enableAudio())
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

    private boolean queryAudioAsset(ArrayList<AssetInfo> lstAssetInfo)
    {
        if (lstAssetInfo == null  || m_osbasePlayer == null)
            return false;

        voOSStreamInfo streamInfo = getCurrentStreamInfo();
        if (streamInfo == null)
            return false;

        voOSTrackInfo[] trackInfoArray = streamInfo.getTrackInfo();
        if (trackInfoArray == null)
            return false;

        lstAssetInfo.clear();

        int nIndexForName    = 0;
        int nIndexForPos     = 0;

        for (int i = 0; i < trackInfoArray.length; i++) {
            if (trackInfoArray[i].getTrackType() != VOOSMP_SOURCE_STREAMTYPE.VOOSMP_SS_AUDIO)
                continue;

            if (trackInfoArray[i].getAudioInfo() == null)
                continue;

            String strLanguage = trackInfoArray[i].getAudioInfo().Language();
            if (strLanguage == null || strLanguage.length() == 0)
                strLanguage = "A" + Integer.toString(nIndexForName++);

            strLanguage = strLanguage.trim();

            boolean bPlaying = (trackInfoArray[i].getSelectInfo()
                    & voOSType.VOOSMP_SRC_TRACK_SELECT_SELECTED) != 0 ?
                    true : false;

            lstAssetInfo.add(new AssetInfo(nIndexForPos++,
                    trackInfoArray[i].getTrackID(), strLanguage, bPlaying));
        }

        if (lstAssetInfo.size() == 0)
            lstAssetInfo.add(new AssetInfo(INDEX_NONE, 0,
                    getResources().getString(R.string.Player_Audio_None), true));

        return true;
    }

    private void onAudioAssetClick(int nIndex)
    {
        if (m_lstAudioAsset == null || nIndex < 0)
            return;

        if (nIndex == 0
                && m_lstAudioAsset.get(nIndex).m_nIndex == INDEX_NONE)
            return;

        selectAudio(m_lstAudioAsset.get(nIndex).m_nID);
    }

    private void selectAudio(int nAudioTrackID)
    {
        voOSStreamInfo streamInfo = getCurrentStreamInfo();
        if (streamInfo == null)
            return;

        m_osbasePlayer.SelectTrack(nAudioTrackID);

        return;
    }

    private boolean querySubtitleAsset(ArrayList<AssetInfo> lstAssetInfo) {

        if (lstAssetInfo == null || m_osbasePlayer == null)
            return false;

        lstAssetInfo.clear();

        int nCount = m_osbasePlayer.GetSubtitleLanguageInfo().size();
        if (nCount == 0) {

            lstAssetInfo.add(new AssetInfo(INDEX_NONE, 0,
                    getResources().getString(R.string.Player_CloseCaptionSelector_None), true));
            return true;
        }

        List<voOSSubtitleLanguage> lstSL = m_osbasePlayer.GetSubtitleLanguageInfo();

        int nIndexForName = 0;

        for (int i = 0; i < nCount; i++) {
            String strName = lstSL.get(i).LangName();
            if (strName == null || strName.length() == 0)
                strName = "Subt" + Integer.toString(nIndexForName++);

            boolean bPlaying = (i == 0) ? true : false;

            lstAssetInfo.add(new AssetInfo(i, i, strName, bPlaying));

        }

        return true;
    }

    private void onSubtitleAssetClick(int nIndex)
    {
        if (m_lstSubtitleAsset == null || nIndex < 0)
            return;

        if (nIndex == 0
                && m_lstSubtitleAsset.get(nIndex).m_nIndex == INDEX_NONE)
            return;

        selectSubtitle(m_lstSubtitleAsset.get(nIndex).m_nID);
    }

    private void selectSubtitle(int nSubtitleIndex)
    {
        if (nSubtitleIndex < 0)
            return;

        if (m_osbasePlayer.GetSubtitleLanguageInfo().size() != 0)
            m_osbasePlayer.SelectSubtitleLanguage(nSubtitleIndex);

        return;
    }

    private boolean queryVideoAsset(ArrayList<AssetInfo> lstAssetInfo)
    {
        if (lstAssetInfo == null || m_osbasePlayer == null ||
                m_osbasePlayer.GetProgramCount() <= 0)
            return false;

        voOSProgramInfo info = (voOSProgramInfo)m_osbasePlayer.GetProgramInfo(0);
        if (info == null || info.getStreamCount() <= 1)
            return false;

        voOSStreamInfo [] streamArr = info.getStreamInfo();
        if (streamArr == null)
            return false;

        lstAssetInfo.clear();

        final int STREAMID_AUTO = 0xffffffff;   // Auto bitrate id

        if (isVideoAutoMode()) {

            lstAssetInfo.add(new AssetInfo(0, STREAMID_AUTO, getResources()
                    .getString(R.string.Player_BpsQuality_Auto), true));

            for (int i = 0; i < streamArr.length; i++) {
                voOSStreamInfo streamInfo = streamArr[i];

                String strBitrate = bitrateToString(streamInfo.getBitrate());

                lstAssetInfo.add(new AssetInfo(i + 1, streamInfo.getStreamID(),
                        strBitrate, false));
            }

        } else {

            if (streamArr.length == 0)
                return false;

            if (streamArr.length != 1) {
                lstAssetInfo.add(new AssetInfo(0, STREAMID_AUTO, getResources()
                        .getString(R.string.Player_BpsQuality_Auto), false));
            }

            for (int i = 0; i < streamArr.length; i++) {
                voOSStreamInfo streamInfo = streamArr[i];

                String strBitrate = bitrateToString(streamInfo.getBitrate());

                boolean bPlaying = ( (streamInfo.getSelInfo() & voOSType.VOOSMP_SRC_TRACK_SELECT_SELECTED) != 0 ) ?
                        true : false;

                int nIndex = (streamArr.length != 1) ? i + 1 : i;

                lstAssetInfo.add(new AssetInfo(nIndex, streamInfo.getStreamID(),
                        strBitrate, bPlaying));

            }
        }

        return true;
    }

    private void onVideoAssetClick(int nIndex)
    {
        if (m_lstVideoAsset == null || nIndex < 0)
            return;

        selectVideo(m_lstVideoAsset.get(nIndex).m_nID);
    }

    private void selectVideo(int nStream) {

        if (m_osbasePlayer == null)
            return;

        m_osbasePlayer.SelectStream(nStream);

    }

    private boolean isVideoAutoMode() {

        if (m_osbasePlayer == null)
            return false;

        Object objParam = m_osbasePlayer.GetParam(voOSType.VOOSMP_SRC_PID_BA_WORKMODE);

        if (objParam == null) {
            voLog.e(TAG, "BA work mode return null." );
            return false;
        }

        int nParam = (Integer) objParam;

        return (nParam == voOSType.VOOSMP_SRC_ADAPTIVE_STREAMING_BA_MODE_AUTO);

    }

    private void onAssetClick(int nIndex, AssetType type)
    {
        if (type == AssetType.Asset_Video)
            onVideoAssetClick(nIndex);
        else if (type == AssetType.Asset_Audio)
            onAudioAssetClick(nIndex);
        else if (type == AssetType.Asset_Subtitle)
            onSubtitleAssetClick(nIndex);
    }

    private voOSStreamInfo getCurrentStreamInfo()
    {
        if (m_osbasePlayer == null)
            return null;

        if (m_osbasePlayer.GetProgramCount() <= 0)
            return null;

        voOSProgramInfo programInfo = (voOSProgramInfo)m_osbasePlayer.GetProgramInfo(0);
        if (programInfo == null)
            return null;

        if (programInfo.getStreamCount() <= 0)
            return null;

        voOSStreamInfo [] streamArr = programInfo.getStreamInfo();
        if (streamArr == null)
            return null;

        for (int i = 0; i < streamArr.length; i ++) {
            if ( (streamArr[i].getSelInfo() & voOSType.VOOSMP_SRC_TRACK_SELECT_SELECTED) != 0)
                return streamArr[i];
        }

        return null;
    }

    private String bitrateToString(int nBitr)
    {
        String s;
        nBitr/=1024;
        if(nBitr<1024) {
            s = Integer.toString(nBitr) + "k";
        }
        else {
            String str = Float.toString(nBitr/1024.0f);
            int n = str.indexOf('.');
            if(n>=0 && n<str.length()-2)
                str = str.substring(0, n+2);

            s = (str + "m");
        }
        return s;
    }

    private boolean enableSubtitle() // External/Internal Subtitle or CloseCaption
    {
        return m_bEnableSubtitle;
    }

    private boolean enableVideo()    // BpsQuality
    {
        return m_bEnableVideo;
    }

    private boolean enableAudio()    // AudioTrack
    {
        return m_bEnableAudio;
    }

    private boolean enableChannel()
    {
        return m_bEnableChannel;
    }

    private boolean enableTestChannelSwitchPerf() {
        return m_bChannelSwitchPerf;
    }

    void initLayoutLeft()
    {

        m_rlChannel = (RelativeLayout) findViewById(R.id.rlChannel);
        m_lvChannel = (ListView) findViewById(R.id.lvChannel);

        if (enableChannel()) {
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
