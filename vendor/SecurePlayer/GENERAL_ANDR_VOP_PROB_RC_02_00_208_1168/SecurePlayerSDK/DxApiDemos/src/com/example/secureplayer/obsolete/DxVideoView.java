package com.example.secureplayer.obsolete;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.discretix.vodx.VODXPlayer;
import com.discretix.vodx.VODXPlayerImpl;
import com.example.secureplayer.R;
import com.visualon.OSMPPlayer.VOCommonPlayer;
import com.visualon.OSMPPlayer.VOCommonPlayerListener;
import com.visualon.OSMPPlayer.VOOSMPInitParam;
import com.visualon.OSMPPlayer.VOOSMPOpenParam;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_PLAYER_ENGINE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FLAG;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FORMAT;
import com.visualon.OSMPUtils.voOSType;


public class DxVideoView extends LinearLayout implements SeekBar.OnSeekBarChangeListener, VOCommonPlayerListener, IDxVideoView{

	private Context mContext = null;
	/* Messages for managing the user interface */
	private static final int                MSG_SHOW_CONTROLLER = 1;
	private static final int                MSG_HIDE_CONTROLLER = 2;
	private static final int                MSG_UPDATE_UI       = 3;
	private static final int                MSG_PLAYCOMPLETE    = 5;
	private static final String TAG = "com.example.secureplayer";

	/* Media controls and User interface */
	private ImageButton						mBtnPause;						// Play/Pause button
	private SeekBar							mSeekBar;						// Seekbar
	private LinearLayout					mLayoutTime;
	private LinearLayout					mLayoutControls;
	private TextView						mPositionText;					// Current position
	private TextView						mDurationText;					// Total duration

	private ProgressBar						mWaitIcon = null;				// Wait icon for buffered or stopped video
	private Date							mLastUpdateTime;				// Last update of media controls

	private	Timer 							mTimer = null;					// Timer for display of media controls
	private TimerTask 						tmTask= null;					// Timer for display of media controls

	private VODXPlayer				        mPlayer = null;					// SDK playe

	/* Flags */
	private boolean							mTrackProgressing = false;		// Seekbar flag

	private boolean							mIsPaused = false;				// Pause flag
	private boolean							mIsStop = false;				// Stop flag

	private long							mDuration;						// Total duration
	public long								mPos = 0;						// Current position
	private String mPlaybackPath = null;
	private OnCompletionListener mOnCompletionListener = null; 
	private OnErrorListener	mOnErrorListener = null;
	SurfaceView mPlayerSurfaceView = null;
	private boolean mWasSurfaceCreated = false;


	private Handler handler = new Handler() {
		/* Handler to manage user interface during playback */
		public void handleMessage(Message msg)
		{

			if(mPlayer == null)
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
				if(mLayoutControls.getVisibility() == View.GONE)
					return;

				/* If the player is stopped do not update */
				if (mIsStop)
					return;

				/* If the user is dragging the progress bar do not update */
				if (mTrackProgressing)
					return;

				Date timeNow = new Date(System.currentTimeMillis());
				long timePeriod = (timeNow.getTime() - mLastUpdateTime.getTime())/1000;
				if (timePeriod >= 3 && mIsPaused == false)
				{
					/* If the media is being played back, hide media controls after 3 seconds if unused */
					hideController();
				}
				else
				{
					/* Else update the Seekbar and Time display with current position */
					mPos = mPlayer.getPosition();
					if (mDuration==0)
					{
						mLayoutTime.setVisibility(View.GONE);
					}
					else
					{
						mSeekBar.setProgress((int) (mPos/(mDuration/100)));
						String str = DateUtils.formatElapsedTime(mPos/1000);
						mPositionText.setText(str);
					}
				}
			} else if (msg.what == MSG_PLAYCOMPLETE) {
				/* Playback in complete, stop player */
				//    setResult(RESULT_OK, (new Intent()).setAction("finish"));
				if(mOnCompletionListener != null)
				{
					mOnCompletionListener.onCompletion(null);
				}
				PlayStop();

			}
		}
	};


	public DxVideoView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public DxVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();

	}

	/** Called when the activity is first created. */
	public void init()
	{
		Log.i(TAG, "DxVoExamplePlayerView onCreate");


		LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
		this.setLayoutParams(layoutParams);

		LayoutInflater inflater = LayoutInflater.from(mContext);
		View v = inflater.inflate(R.layout.player, null, false);
		this.addView(v,layoutParams);

		mPlayerSurfaceView =  (SurfaceView) findViewById(R.id.dxvo_Surface_view);
		SurfaceHolder surfaceHolder = mPlayerSurfaceView.getHolder();
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);  
		surfaceHolder.addCallback(mSHCallback);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL); 
		surfaceHolder.setFormat(PixelFormat.RGBA_8888);
		
		//Control bar UI components
		mBtnPause = (ImageButton) findViewById(R.id.pause); 
		mSeekBar = (SeekBar) findViewById(R.id.SeekBar01);
		mLayoutControls = (LinearLayout) findViewById(R.id.layout1);
		mLayoutTime = (LinearLayout) findViewById(R.id.layout2);
		mPositionText = (TextView)findViewById(R.id.CurrentPosition);
		mDurationText = (TextView)findViewById(R.id.Duration); 
		mWaitIcon = (ProgressBar) findViewById(R.id.pbBuffer);

		mSeekBar.setOnSeekBarChangeListener(this);

		mLayoutControls.setVisibility(View.GONE);
		mLayoutTime.setVisibility(View.GONE);
		mWaitIcon.setVisibility(View.VISIBLE);

		// Activate listener for Play/Pause button
		mBtnPause.setOnClickListener(new ImageButton.OnClickListener()
		{
			public void onClick(View view)
			{
				playerPauseRestart();
			}
		});
	}

	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			Log.i(TAG, "Surface Changed");
			mPlayer.setSurfaceChangeFinished();
		}

		public void surfaceCreated(SurfaceHolder holder) {
			Log.i(TAG, "Surface Created");
			mWasSurfaceCreated = true;
			initPlayerAndStartPlayback();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.i(TAG, "Surface Destroyed");
			mWasSurfaceCreated = false;;
			if(mPlayer != null)
				mPlayer.setView(null);
		}
	};
	private void initPlayerAndStartPlayback() {
		
		initPlayer();
		if ( mPlaybackPath != null) {
			// Register listeners
			
			VOOSMPOpenParam openParam = new VOOSMPOpenParam();
			mPlayer.open(mPlaybackPath, VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_SYNC, VO_OSMP_SRC_FORMAT.VO_OSMP_SRC_AUTO_DETECT, openParam);
			mPlayer.start();
		}
	}
	private void initPlayer()
	{
		mPlayer = new VODXPlayerImpl();
		// Location of libraries
		String apkPath = "/data/data/" + mContext.getPackageName() + "/lib/";	

		// Copy license file, 
		loadLisenceFile("voVidDec.dat");
		
		// Initialize SDK player
		VO_OSMP_RETURN_CODE nRet = mPlayer.init(mContext, apkPath, VO_OSMP_PLAYER_ENGINE.VO_OSMP_VOME2_PLAYER, null, voOSType.VOOSMP_FLAG_INIT_NOUSE);
		if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
			//TODO handle error
		}
		// Set view
		mPlayer.setView(mPlayerSurfaceView);
		
		DisplayMetrics dm  = getResources().getDisplayMetrics();
		// Set surface view size
		mPlayer.setViewSize(dm.widthPixels, dm.heightPixels);
		
		mPlayer.setOnEventListener(this);
	}

	/* Stop playback, close media source, and uninitialize SDK player */
	public void PlayStop() 
	{
		mIsPaused = false;
		mIsStop = true;

		if (mPlayer != null) 
		{
			mPlayer.stop();
			mPlayer.close();
			mPlayer.destroy();
			mPlayer = null;
			Log.i(TAG, "MediaPlayer release.");
		}
	}






	/* Show media controller. Implemented by showMediaControllerImpl(), called by handler */
	private void showMediaController()
	{
		if(mPlayer == null)
			return;

		if(mPlayer.getDuration() <=0)
		{
			Log.i(TAG, "live module, Don't show control bar!");
			return;
		}

		handler.sendEmptyMessage(MSG_SHOW_CONTROLLER);
	}

	/* Hide media controller. Implemented by showMediaControllerImpl(), called by handler */
	public void hideController()
	{
		handler.sendEmptyMessage(MSG_HIDE_CONTROLLER);
	}

	/* Hide media controller implementation */
	public void hideControllerImpl()
	{
		if(mTimer != null)
		{
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
			tmTask = null;
		}

		mLayoutControls.setVisibility(View.GONE);
		mLayoutTime.setVisibility(View.GONE);
	}

	/* Show media controller implementation */
	private void showMediaControllerImpl()
	{
		Log.i(TAG, "Touch screen, layout status is " +mLayoutControls.getVisibility());
		mLastUpdateTime = new Date(System.currentTimeMillis());		// Get current system time

		if (mLayoutControls.getVisibility() == View.GONE)
		{
			Log.i(TAG, "mIsStop is " + mIsStop);

			if ((mIsStop == false))
			{
				// Get duration in milliseconds and set duration text in HH:MM:SS
				mDuration = mPlayer.getDuration();
				mPos = 0;
				String str = DateUtils.formatElapsedTime(mDuration/1000);
				mDurationText.setText(str);
				Log.i(TAG, String.format("Duration is %s.", str));				
			}

			// Schedule next UI update in 200 milliseconds
			if(tmTask!=null)
			{	
				tmTask = null;
			}
			tmTask= new TimerTask()
			{
				public void run()
				{
					handler.sendEmptyMessage(MSG_UPDATE_UI);
				}
			};
			if(mTimer == null)
			{
				mTimer = new Timer();
			}
			mTimer.schedule(tmTask, 0, 200);

			// Show controls
			mLayoutControls.setVisibility(View.VISIBLE);
			mLayoutTime.setVisibility(View.VISIBLE);
			mLayoutTime.bringToFront();

			Log.i(TAG, "mLayoutTime show " + mLayoutTime.getVisibility());
		}
	}

	/* Show media controls on activity touch event */
	public boolean onTouchEvent(MotionEvent event)
	{
		showMediaController();
		try 
		{
			Thread.sleep(100);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}

		showMediaController();
		return super.onTouchEvent(event);
	}

	/* Display error messages and stop player */
	public boolean onError(VOCommonPlayer mp, int what, int extra) 
	{
		Log.i(TAG, "Error message, what is " + what + " extra is " + extra);

		if(mOnErrorListener != null)
		{
			mOnErrorListener.onError(null, what, extra);
		}
		return true;


	}

	/* Flag when Seekbar is being dragged */
	public void onStartTrackingTouch(SeekBar arg0) 
	{
		mTrackProgressing = true;
	}

	/* Seek to new position when Seekbar drag is complete */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(mTrackProgressing)
		{
			int currentPos = (int) (progress*mDuration/mSeekBar.getMax());
			String str = DateUtils.formatElapsedTime(currentPos/1000);
			mPositionText.setText(str);
		}

	}
	public void onStopTrackingTouch(SeekBar arg0) 
	{
		// Calculate new position as percentage of total duration
		int iCurrent = arg0.getProgress();
		int iMax = arg0.getMax();
		int iNewPosition = (int) (iCurrent*mDuration/iMax);

		mTrackProgressing = false;			// Disable Seekbar tracking flag

		if (mPlayer != null)
		{
			Log.i(TAG,"Seek To " + iNewPosition);
			mPlayer.setPosition(iNewPosition);	// Set new position
		}

	}

	/* Toggle Play/Pause button display and functionality */
	private void playerPauseRestart()
	{
		if (mPlayer != null)
		{
			if(mIsPaused==false)
			{
				// If playing, pause media pipeline, show media controls, and change button to the <Play> icon
				mPlayer.pause();
				showMediaController();
				mBtnPause.setImageResource(R.drawable.play_button);
				mIsPaused = true;
			}
			else if(mIsPaused==true)
			{
				// Else, play media pipeline and change button to the <Pause> icon
				mPlayer.start();
				mBtnPause.setImageResource(R.drawable.pause);
				mIsPaused = false;
			}
		}

	}

	/* SDK event handling */
	public VO_OSMP_RETURN_CODE onVOEvent(VO_OSMP_CB_EVENT_ID nID, int nParam1, int nParam2, Object obj) 
	{
		switch(nID)
		{ 
			case VO_OSMP_SRC_CB_ADAPTIVE_STREAMING_INFO:
		    {
		    	VO_OSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT event = VO_OSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT.valueOf(nParam1);
		        switch(event)
		        {
		            case VO_OSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_BITRATE_CHANGE:
		            {
						Log.v(TAG, "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_BITRATE_CHANGE, param2 is " + nParam2);
						break;
		            }
		            case VO_OSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE:
		            {
		            	Log.v(TAG, "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, param2 is " + nParam2);
		            	VO_OSMP_AVAILABLE_TRACK_TYPE type = VO_OSMP_AVAILABLE_TRACK_TYPE.valueOf(nParam2);
						switch(type)
						{
							case VO_OSMP_AVAILABLE_PUREAUDIO:
							{
							    Log.v(TAG, "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_PUREAUDIO");
							 break;
							}
							case VO_OSMP_AVAILABLE_PUREVIDEO:
							{
							    Log.v(TAG, "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_PUREVIDEO");
							 break;
							}
							case VO_OSMP_AVAILABLE_AUDIOVIDEO:
							{
							    Log.v(TAG, "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_AUDIOVIDEO");
							 break;
							}
						}
						break;
		            }
		        }
		        break;
		    }
			case VO_OSMP_CB_ERROR:	
			case VO_OSMP_SRC_CB_CONNECTION_FAIL:
			case VO_OSMP_SRC_CB_DOWNLOAD_FAIL:
			case VO_OSMP_SRC_CB_DRM_FAIL:
			case VO_OSMP_SRC_CB_PLAYLIST_PARSE_ERR:
			case VO_OSMP_SRC_CB_CONNECTION_REJECTED:
			case VO_OSMP_SRC_CB_DRM_NOT_SECURE:
			case VO_OSMP_SRC_CB_DRM_AV_OUT_FAIL:	// Error
			{
				// Display error dialog and stop player
				onError(mPlayer, nID.getValue(), 0);
				break;
			}
			case VO_OSMP_CB_PLAY_COMPLETE:
			{
				handler.sendEmptyMessage(MSG_PLAYCOMPLETE);
				break;
			}	
			case VO_OSMP_CB_SEEK_COMPLETE:		// Seek (SetPos) complete
			{
				break;
			}
			case VO_OSMP_CB_VIDEO_STOP_BUFFER:		// Video buffering stopped
			{
				mWaitIcon.setVisibility(View.GONE);					// Hide wait icon
				break;
			}
			case VO_OSMP_CB_AUDIO_START_BUFFER:		// Video buffering started
			{
				mWaitIcon.setVisibility(View.VISIBLE);				// Show wait icon	
				break;
			}
			case VO_OSMP_SRC_CB_BA_HAPPENED:		// Unimplemented
			{
				Log.v(TAG, "OnEvent VOOSMP_SRC_CB_BA_Happened, param is %d " + nParam1);
				break;
			}
			case VO_OSMP_SRC_CB_DOWNLOAD_FAIL_WAITING_RECOVER:
			{
				Log.v(TAG, "OnEvent VOOSMP_SRC_CB_Download_Fail_Waiting_Recover, param is %d " + nParam1);
				break;
			}
			case VO_OSMP_SRC_CB_DOWNLOAD_FAIL_RECOVER_SUCCESS:
			{
				Log.v(TAG, "OnEvent VOOSMP_SRC_CB_Download_Fail_Recover_Success, param is %d " + nParam1);
				break;
			}
			case VO_OSMP_SRC_CB_OPEN_FINISHED:
			{
				Log.v(TAG, "OnEvent VOOSMP_SRC_CB_Open_Finished, param is %d " + nParam1);

				break;
			}
		}
		return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;		
	}
	

	/* Manage SDK requests */
	public int onRequest(int arg0, int arg1, int arg2, Object arg3) {
		Log.i(TAG, "onRequest arg0 is "+arg0);
		return 0;
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	@Override
	public int getBufferPercentage() {
		return 0;
	}

	@Override
	public int getCurrentPosition() {
		if(mPlayer != null)
			return (int) mPlayer.getPosition();
		return 0;
	}

	@Override
	public int getDuration() {
		if(mPlayer != null)
			return (int) mPlayer.getDuration();
		return 0;
	}

	@Override
	public boolean isPlaying() {
		//TODO need to implement 
		return false;
	}

	@Override
	public void pause() {
		if(mPlayer != null)
		{
			mPlayer.pause();
			mIsPaused = true;
		}
	}

	@Override
	public int resolveAdjustedSize(int desiredSize, int measureSpec) {
		return 0;
	}

	@Override
	public void seekTo(int msec) {

		long ret = mPlayer.setPosition((long)msec);
		if(ret == -1)
		{
			Log.e(TAG, "seekTo filed");
		}
	}

	// The setMediaController in not implemented in the new API.
	@Override
	public void setMediaController(MediaController controller) {
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener l) {
		mOnCompletionListener = l;

	}

	@Override
	public void setOnErrorListener(OnErrorListener l) {
		mOnErrorListener = l;

	}

	// The OnPreparedListener listener in not implemented in the new API.
	@Override
	public void setOnPreparedListener(OnPreparedListener l) {
	}

	@Override
	public void setVideoPath(String path) {
		setVideoPath(path, 0); 

	}

	@Override
	public void setVideoPath(String path, int contentSize) {
		mPlaybackPath = path;
		if(mWasSurfaceCreated && mPlayer != null)
		{		
			VOOSMPOpenParam openParam = new VOOSMPOpenParam();
			if (contentSize != 0) {
				openParam.setFileSize(contentSize);
			}
			mPlayer.open(mPlaybackPath, VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_SYNC, VO_OSMP_SRC_FORMAT.VO_OSMP_SRC_AUTO_DETECT, openParam);
		
			mPlayer.start ();
		}
	}

	@Override
	public void setVideoURI(Uri uri) {
		setVideoURI(uri, 0);

	}

	@Override
	public void setVideoURI(Uri uri, int contentSize) {
		setVideoPath(uri.getPath(), contentSize);

	}

	@Override
	public void start() {
		if(mPlayer != null)
		{
			mPlayer.start();
			mIsPaused = false;
			mIsStop = false;
		}
	}

	@Override
	public void stopPlayback() {
		if(mPlayer != null)
		{
			mPlayer.stop();
			mPlayer.close();
		}
	}

	// The onResume listener in not implemented in the new API.
	@Override
	public void onResume() {

	}


	@Override
	public void onPause() {

		if (mPlayer!= null)// && mMediaPlayer.getStatus() == voPlayer.ePlayerState.eps_running)
		{
			if(mPlayer.canBePaused())
			{
				// Else pause playback and show media controls
				mPlayer.pause();
				mBtnPause.setImageResource(R.drawable.play_button);
				mIsPaused = true;
				Log.v(TAG, "Player pause");
				showMediaController();
			}else
			{
				// If for live streaming, we stop playbacking directly.
				PlayStop();
			}
		}
	}

	@Override
	public void onDestroy() {
		if(mPlayer != null)
		{
			mPlayer.stop();
			mPlayer.close();
			mPlayer.destroy();
		}
	}
	
	/* Copy file from Assets directory to destination. Used for licenses and processor-specific configurations */
	private void loadLisenceFile(String fileName)
	{
		try {
			String filePath = "/data/data/" + mContext.getPackageName() + "/" + fileName;
			InputStream InputStreamis  = mContext.getAssets().open(fileName);
			File desFile = new File(filePath);
			desFile.createNewFile();			 
			FileOutputStream  fos = new FileOutputStream(desFile);
			int bytesRead;
			byte[] buf = new byte[4 * 1024]; //4K buffer
			while((bytesRead = InputStreamis.read(buf)) != -1) {
			fos.write(buf, 0, bytesRead);
			}
			fos.flush();
			fos.close();
			
			if(mPlayer != null)
				mPlayer.setLicenseFilePath(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public VO_OSMP_RETURN_CODE onVOSyncEvent(VO_OSMP_CB_SYNC_EVENT_ID arg0,
			int arg1, int arg2, Object arg3) {
		// TODO Auto-generated method stub
		return null;
	}

}
