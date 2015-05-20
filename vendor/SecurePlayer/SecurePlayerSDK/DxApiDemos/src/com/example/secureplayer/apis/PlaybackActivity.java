package com.example.secureplayer.apis;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.discretix.vodx.VODXPlayer;
import com.discretix.vodx.VODXPlayerImpl;
import com.example.secureplayer.DxConstants;
import com.example.secureplayer.R;
import com.visualon.OSMPPlayer.VOCommonPlayerAssetSelection.VOOSMPAssetProperty;
import com.visualon.OSMPPlayer.VOCommonPlayerListener.VO_OSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT;
import com.visualon.OSMPPlayer.VOCommonPlayerListener;
import com.visualon.OSMPPlayer.VOOSMPInitParam;
import com.visualon.OSMPPlayer.VOOSMPOpenParam;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_DECODER_TYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_PLAYER_ENGINE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FLAG;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FORMAT;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_STATUS;


public class PlaybackActivity extends Activity implements VOCommonPlayerListener
{
	private static final String TAG = "com.example.secureplayer";
	private String mPlaybackPath = DxConstants.getActiveContent().getPlayBackPath();
	private PlaybackControl mVideoView = null;
	private VODXPlayer mPlayer;
	private boolean mStartedPlayBack = false;
	SurfaceView mPlayerSurfaceView = null;
	private int m_DrmErrorCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
		    getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
		}
		mPlaybackPath = DxConstants.getActiveContent().getPlayBackPath();
		
		//Get into full screen mode. And prevent it from sleeping
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.main);
		mVideoView = (PlaybackControl) findViewById(R.id.video_view);
		mPlayerSurfaceView =  (SurfaceView) findViewById(R.id.dxvo_Surface_view);		
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		SurfaceHolder surfaceHolder = mPlayerSurfaceView.getHolder();
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);  
		surfaceHolder.addCallback(mSHCallback);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL); 
		surfaceHolder.setFormat(PixelFormat.RGBA_8888);
		
	}
	/*MAIN_DLC_PROB_U_02_00_101_0000*/
	private void initPlayer()
	{
		mPlayer = new VODXPlayerImpl();
		
		// Location of libraries
		String apkPath = getFilesDir().getParent() + "/lib/";
				
		// Initialize SDK player		
		VOOSMPInitParam initParam = new VOOSMPInitParam();
		initParam.setLibraryPath(apkPath);
		initParam.setContext(this);
		VO_OSMP_RETURN_CODE nRet = mPlayer.init(VO_OSMP_PLAYER_ENGINE.VO_OSMP_VOME2_PLAYER, initParam);
		if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
			//TODO handle error
		}

		// Copy license file, 
		loadLicenseFile("voVidDec.dat");
		setDeviceCapability("Android-cap.xml");
		
		// Set view
		mPlayer.setView(mPlayerSurfaceView);		

		DisplayMetrics dm  = getResources().getDisplayMetrics();
		// Set surface view size
		mPlayer.setViewSize(dm.widthPixels, dm.heightPixels);

		mPlayer.setOnEventListener(this);
		if(mVideoView != null)
		{
			mVideoView.setPlayer(mPlayer);
		}
	}

	private void setSubtitles()
	{
		String customSubtitle = DxConstants.getSubtitleCustom();
		if (customSubtitle != null) {
			mPlayer.enableSubtitle(true);
			mPlayer.setSubtitlePath(customSubtitle);
		}
		else if (DxConstants.getSubtitleSelected() != -1) {
			mPlayer.enableSubtitle(true);
			mPlayer.selectSubtitle(DxConstants.getSubtitleSelected());
		}
		else {
			mPlayer.enableSubtitle(false);
		}
	}

	private void setAudioChannel()
	{
		if (DxConstants.getAudioSelected() != -1) {
			mPlayer.selectAudio(DxConstants.getAudioSelected());
		}
	}
	
	
	private void playVideo() {
		if ( mPlaybackPath != null) {
			// Register listeners
			initPlayer();
			
			VOOSMPOpenParam openParam = new VOOSMPOpenParam();
			if (DxConstants.isHardwareAccelerated())
				openParam.setDecoderType(VO_OSMP_DECODER_TYPE.VO_OSMP_DEC_VIDEO_IOMX.getValue());
						
			VO_OSMP_RETURN_CODE nRet = mPlayer.open(mPlaybackPath, VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_ASYNC, VO_OSMP_SRC_FORMAT.VO_OSMP_SRC_AUTO_DETECT, openParam);
				if (nRet == null)
			{
				Log.e(TAG, "mPlayer.open returned null - using default onError with 1.");
				onError(mPlayer, 1, 0);
			}
			else if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE){
				// Display error dialog and stop player
				onError(mPlayer, nRet.getValue(), 0);				
			}
			//will be called after VO_OSMP_SRC_CB_OPEN_FINISHED event will be raised;
			//mPlayer.start();		
		}
	}
    private void updatePlaybackInfoBPS(){
    	TextView tView = (TextView) findViewById(R.id.playback_info_bps);
    	String value=DxConstants.getPlaybackInformationBPS();
    	if (value == null){
    		// if Value hasn't been set;null; then use default value
    		value = getString(R.string.playback_information_BPS_default_value);
    	}
    	else{
    		// Set units
    		value = value +getString(R.string.playback_information_BPS_tView_units);
    	}
    	value = getString(R.string.playback_information_BPS_tView_header) + value;
    	tView.setText(value);
    	tView.invalidate();
    }
    
    private void updatePlaybackInfoResolution(){
    	TextView tView = (TextView) findViewById(R.id.playback_info_res);
    	String value = DxConstants.getPlaybackInformationResolution();
    	if (value == null){
    		// if Value hasn't been set;null; then use default value
    		value = getString(R.string.playback_information_Resolution_default_value);
    	}
    	value = getString(R.string.playback_information_Resolution_tView_header) + value;
    	tView.setText(value);
    	tView.invalidate();
    }
    

	private void updatePlaybackInfoLayout(){
		updatePlaybackInfoBPS();
		updatePlaybackInfoResolution();
		View playbackInfoLayout = (View) findViewById(R.id.playbackInfoLayout);
		
		if (DxConstants.getDisplayPlaybackInformation() == true){
			playbackInfoLayout.setVisibility(android.view.View.VISIBLE);
		}
		else{
			playbackInfoLayout.setVisibility(android.view.View.INVISIBLE);	
		}	
	}
	private void resetPlayBackInformation(){
		DxConstants.setPlaybackInformationBPS(getString(R.string.playback_information_BPS_default_value));
		DxConstants.setPlaybackInformationResolution(getString(R.string.playback_information_Resolution_default_value));
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mPlayer != null) {
			mPlayer.destroy();
		}
		resetPlayBackInformation();
		
	}

	@Override
	public void onResume() {
		super.onResume();
		updatePlaybackInfoLayout();
	}

	@Override
	protected void onPause() {
		if(mPlayer != null)
		{
			if(mPlayer.getPlayerStatus() == VO_OSMP_STATUS.VO_OSMP_STATUS_PLAYING)
			{
				mStartedPlayBack = false;
			}

			if (mPlayer.canBePaused()) 
			{
				mPlayer.pause();
			}
			else
			{
				mPlayer.stop();
			}
		}
		super.onPause();
	}
	/* Stop player and exit on Back key */
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			StopPlayback();
		}
		return super.onKeyDown(keyCode, event);
	}

	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			Log.i(TAG, "Surface Changed");
			if(mPlayer != null)
				mPlayer.setSurfaceChangeFinished();
		}

		public void surfaceCreated(SurfaceHolder holder) {
			Log.i(TAG, "Surface Created");
			//			mWasSurfeceCreated = true;
			if(mPlayer == null)
			{
				playVideo();
			}
			else
			{
				mPlayer.resume(mPlayerSurfaceView);

				if(!mStartedPlayBack)
				{
					mStartedPlayBack = true;
					mPlayer.start();
				}

				setAudioChannel();
				setSubtitles();
				mPlayer.commitSelection();
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.i(TAG, "Surface Destroyed");
			if(mPlayer != null)
				mPlayer.setView(null);
		}
	};

	@Override
	public VO_OSMP_RETURN_CODE onVOEvent(VO_OSMP_CB_EVENT_ID nID, int nParam1, int nParam2, Object obj)
	{
		switch (nID) {
		case VO_OSMP_SRC_CB_ADAPTIVE_STREAMING_INFO: {
			VO_OSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT event = VO_OSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT.valueOf(nParam1);
			switch (event) {
			case VO_OSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_BITRATE_CHANGE:
				Log.v(TAG,"OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_BITRATE_CHANGE, param2 is " + nParam2);
				DxConstants.setPlaybackInformationBPS(Integer.toString(nParam2)+" ");
				updatePlaybackInfoBPS();
				break;

			default:
				break;
			}
			break;
		}
		case VO_OSMP_SRC_CB_ADAPTIVE_STREAM_WARNING:
			if ( nParam1 == VO_OSMP_SRC_ADAPTIVE_STREAMING_WARNING_EVENT.VO_OSMP_SRC_ADAPTIVE_STREAMING_WARNING_EVENT_CHUNK_DRMERROR.getValue()) 
			{
				Log.v(TAG, "VO_OSMP_SRC_CB_ADAPTIVE_STREAM_WARNING, nParam1 is VOOSMP_SRC_ADAPTIVE_STREAMING_WARNING_EVENT_CHUNK_DRMERROR");
				m_DrmErrorCount++;
				if(m_DrmErrorCount == 10)
					onError(mPlayer, nID.getValue(), nParam1);
			}
			else
			{
				Log.v(TAG, "VOOSMP_SRC_CB_Adaptive_Stream_Warning, nParam1 is "+nParam1);
			}
			break;
		
		case VO_OSMP_SRC_CB_BA_HAPPENED :{
			break;
		}
		case VO_OSMP_CB_VIDEO_SIZE_CHANGED:{
			int videoWidth = nParam1;
			int videoHeight = nParam2;

			// Retrieve new display metrics
			DisplayMetrics dm  = new DisplayMetrics();
			WindowManager windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
			windowManager.getDefaultDisplay().getMetrics(dm);
			if (getResources().getConfiguration().orientation
					== Configuration.ORIENTATION_PORTRAIT) {

				// If portrait orientation, scale height as a ratio of the new aspect ratio
				ViewGroup.LayoutParams lp = mPlayerSurfaceView.getLayoutParams();
				lp.width = dm.widthPixels;
				lp.height = dm.widthPixels * videoHeight / videoWidth;
				mPlayerSurfaceView.setLayoutParams(lp);
			}
			// now update playback info
			DxConstants.setPlaybackInformationResolution(videoWidth + "x" + videoHeight);
			updatePlaybackInfoResolution();
			break;
		}
		case VO_OSMP_CB_PLAY_COMPLETE:{
			StopPlayback();
			Toast.makeText(this, "Playback has finished", Toast.LENGTH_LONG).show();
			// Finish should be called in order to exit the activity after playback
			// completes.
			finish();
			break;
		}
		case VO_OSMP_CB_ERROR:
		case VO_OSMP_SRC_CB_CONNECTION_FAIL:
		case VO_OSMP_SRC_CB_DOWNLOAD_FAIL:
		case VO_OSMP_SRC_CB_DRM_FAIL:
		case VO_OSMP_SRC_CB_PLAYLIST_PARSE_ERR:
		case VO_OSMP_SRC_CB_CONNECTION_REJECTED:
		case VO_OSMP_SRC_CB_DRM_NOT_SECURE:
		case VO_OSMP_SRC_CB_DRM_AV_OUT_FAIL:
		case VO_OSMP_CB_CODEC_NOT_SUPPORT:
		case VO_OSMP_SRC_CB_ADAPTIVE_STREAMING_ERROR:{			
			// Display error dialog and stop player
			onError(mPlayer, nID.getValue(), nParam1);
			break;
		}
		
		default:
			Log.w(TAG,"Unhandled VO_OSMP_CB_EVENT_ID:"+nID);
			break;
		}
		 

		return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;		
	}

	/* Display error messages and stop player */
	public void onError(final VODXPlayer mp, final int what, final int extra) 
	{
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				onErrorImpl(mp, what, extra);			
			}
		});	
	}
	
	private void onErrorImpl(VODXPlayer mp, int what, int extra) 
	{
		Log.v(TAG, "Error message, what is " + what + " extra is " + extra);
		String errStr =  "Error code is " + Integer.toHexString(what) + "\nExtra(" + extra + ")";

		Log.v(TAG, "Stop Playback");
		StopPlayback();
		
		// Dialog to display error message; stop player and exit on Back key or <OK>
		AlertDialog ad = new AlertDialog.Builder(this)
		.setIcon(R.drawable.icon)
		.setTitle(R.string.str_ErrPlay_Title)
		.setMessage(errStr)
		.setOnKeyListener(new OnKeyListener()
		{
			public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) 
			{
				if (arg1 == KeyEvent.KEYCODE_BACK) 
				{ 
					// do something on back. 
					finish();
				} 
				return false;
			}
		})
		.setPositiveButton(R.string.str_OK, new OnClickListener() 
		{
			public void onClick(DialogInterface a0, int a1)
			{
				finish();
			}
		}).create();
		ad.show();
	}

	private void StopPlayback()
	{
		if(mPlayer != null)
		{
			mPlayer.stop();
			mPlayer.close();
			mPlayer.destroy();
			mPlayer = null;
			mVideoView.setPlayer(null);
		}
		
		DxConstants.setVideoSpecifics(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_playback, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (mPlayer!= null && !DxConstants.isVideoSpecificsSet())
		{
			int subtitleCount = mPlayer.getSubtitleCount();
			if (subtitleCount > 0) {
				DxConstants.setSubtitleArray(subtitleCount);
				for (int iIndex=0 ; iIndex<subtitleCount ; ++iIndex) {
					VOOSMPAssetProperty property = mPlayer.getSubtitleProperty(iIndex);
					
					String propertyDescription = null;
					String propertyLanguage = null;
					String propertyCodec = null;
					String subtitleName = null;
					
					int propertyCount = property.getPropertyCount();
					for (int propertyIndex=0 ; propertyIndex<propertyCount ; ++propertyIndex) {
						String propertyKey = property.getKey(propertyIndex);
						if (propertyKey.equals("description")) {
							propertyDescription = (String) (property.getValue(propertyIndex));
						}
						else if (propertyKey.equals("language")) {
							propertyLanguage = (String) (property.getValue(propertyIndex));
						}
						else if (propertyKey.equals("codec")) {
							propertyCodec = (String) (property.getValue(propertyIndex));
						}
					}					
					
					if (propertyDescription != null)
					{
						propertyDescription = " (" + propertyDescription + ")";
					}
					if(propertyCodec != null)
					{
						propertyCodec = " - [" + propertyCodec + "]";
					}
					subtitleName = propertyLanguage + propertyDescription + propertyCodec;
					if (subtitleName.equalsIgnoreCase("")) {
						subtitleName = "Unknown";
					}
					DxConstants.setSubtitle(iIndex, subtitleName);
				}
				
			}
			
			int audioCount = mPlayer.getAudioCount();
			if (audioCount > 0) {
				DxConstants.setAudioArray(audioCount);
				for (int iIndex=0 ; iIndex<audioCount ; ++iIndex) {
					VOOSMPAssetProperty property = mPlayer.getAudioProperty(iIndex);
					
					String propertyDescription = null;
					String propertyLanguage = null;
					String propertyCodec = null;
					String audioName = null;
					
					int propertyCount = property.getPropertyCount();
					for (int propertyIndex=0 ; propertyIndex<propertyCount ; ++propertyIndex) {
						String propertyKey = property.getKey(propertyIndex);
						if (propertyKey.equals("description")) {
							propertyDescription = (String) (property.getValue(propertyIndex));
						}
						else if (propertyKey.equals("language")) {
							propertyLanguage = (String) (property.getValue(propertyIndex));
						}
						else if (propertyKey.equals("codec")) {
							propertyCodec = (String) (property.getValue(propertyIndex));
						}
					}
					
					if (propertyDescription != null)
					{
						propertyDescription = " (" + propertyDescription + ")";
					}
					if(propertyCodec != null)
					{
						propertyCodec = " - [" + propertyCodec + "]";
					}
					audioName = propertyLanguage + propertyDescription + propertyCodec;
					if (audioName.equalsIgnoreCase("")) {
						audioName = "Unknown";
					}
					DxConstants.setAudio(iIndex, audioName);
				}
				
			}

			DxConstants.setVideoSpecifics(true);
		}
		
		switch (item.getItemId())
		{
		case R.id.menu_options:
			Intent optionsActivity = new Intent(this, PlaybackOptionsActivity.class); 
			startActivity(optionsActivity);
			return true;
		}

		return true;
	}

	/* Copy file from Assets directory to destination. Used for licenses and processor-specific configurations */
	private void loadLicenseFile( String fileName)
	{
		try {
			String fileDir = getFilesDir().getParent() + "/";
			String filePath = fileDir + fileName;
			InputStream InputStreamis  = getAssets().open(fileName);
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
			{
				VO_OSMP_RETURN_CODE nRet = mPlayer.setLicenseFilePath(fileDir);
				if(nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE)
				{
					Log.e(TAG,"setLicenseFilePath failed with error: " + nRet);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setDeviceCapability(String fileName)
	{
		try {
			String filePath = getFilesDir().getParent() + "/" + fileName;
			InputStream InputStreamis  = getAssets().open(fileName);
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
				mPlayer.setDeviceCapabilityByFile(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public VO_OSMP_RETURN_CODE onVOSyncEvent(VO_OSMP_CB_SYNC_EVENT_ID arg0,
			int arg1, int arg2, Object arg3) {

		return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;		
	}
}
