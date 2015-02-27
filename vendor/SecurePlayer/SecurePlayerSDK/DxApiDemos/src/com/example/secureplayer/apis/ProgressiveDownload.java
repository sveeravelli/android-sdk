package com.example.secureplayer.apis;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.discretix.drmdlc.api.DxDrmDlc;
import com.discretix.drmdlc.api.IDxDrmDlc;
import com.discretix.drmdlc.api.exceptions.DrmClientInitFailureException;
import com.discretix.drmdlc.api.exceptions.DrmGeneralFailureException;
import com.discretix.drmdlc.api.exceptions.DrmInvalidFormatException;
import com.discretix.vodx.VODXPlayer;
import com.discretix.vodx.VODXPlayerImpl;
import com.example.secureplayer.DxConstants;
import com.example.secureplayer.R;
import com.visualon.OSMPPlayer.VOCommonPlayerListener;
import com.visualon.OSMPPlayer.VOOSMPInitParam;
import com.visualon.OSMPPlayer.VOOSMPOpenParam;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_PLAYER_ENGINE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FLAG;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FORMAT;


public class ProgressiveDownload extends Activity implements VOCommonPlayerListener {

	public static final long PLAYBACK_BEGIN_THRESHOLD =  1024 * 1024; // Start playing after 1 MB download

	// Pause playback if 10 seconds are not available
	public static final long PLAYBACK_PAUSE_THRESHOLD = 5 * 1000;
	private static final String TAG = "com.example.secureplayer";

	public static final int BUFFER_SIZE = 4 * 1024;
	public static final int CONNECTION_TIMEOUT = 10000;
	public static final int READ_DATA_TIMEOUT = 10000;	
	private String mContentUrl = DxConstants.getActiveContent().getContentUrl();
	private String mLocalContentPath = DxConstants.getActiveContent().getTemplocalFile();
	private int mContentLength;
	private PlaybackControl mVideoView = null;
	SurfaceView mPlayerSurfaceView = null;
	private VODXPlayer mPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
		    getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
		}	
		mLocalContentPath = DxConstants.getActiveContent().getTemplocalFile();
		
		//Get into full screen mode. And prevent it from sleeping
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.main);
		File localFile = new File(mLocalContentPath);
		if (localFile.exists()) {
			localFile.delete();
		}
		initSurfaceHolder();
		play();

	}

	private void initSurfaceHolder() {
		mVideoView = (PlaybackControl) findViewById(R.id.video_view);
		mPlayerSurfaceView =  (SurfaceView) findViewById(R.id.dxvo_Surface_view);

		SurfaceHolder surfaceHolder = mPlayerSurfaceView.getHolder();
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);  
		surfaceHolder.addCallback(mSHCallback);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL); 
		surfaceHolder.setFormat(PixelFormat.RGBA_8888);
		
	}

	private void play() {
		Log.i("thread: " + Thread.currentThread().getName(), "play()");

		Thread playthread = new Thread(new DownloadingPlayer());
		playthread.start(); 
	}

	private void prepareVideo(int contentlength) {
		if ( mLocalContentPath != null) {
			// Register listeners
			initPlayer();
			
			VOOSMPOpenParam openParam = new VOOSMPOpenParam();
			openParam.setFileSize(contentlength);
			mPlayer.open(mLocalContentPath, VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_SYNC, VO_OSMP_SRC_FORMAT.VO_OSMP_SRC_AUTO_DETECT, openParam);
			
			mPlayer.start();
		}
	}

	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
		Toast.makeText(this, "An error occured", Toast.LENGTH_LONG).show();
		finish();
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	class DownloadingPlayer implements Runnable {

		public DownloadingPlayer() {
		}

		public void run() {
			try {
				boolean wasPlaybackRequested = false;
				boolean wasForcePaused = false;

				Log.i("DownloadFile", "Downloading url: " + mContentUrl
						+ ", dest: " + mLocalContentPath);
				FileOutputStream fos = new FileOutputStream(mLocalContentPath);

				byte[] buffer = new byte[BUFFER_SIZE];

				URL urlObj = new URL(mContentUrl);
				URLConnection conn = urlObj.openConnection();
				conn.setConnectTimeout(CONNECTION_TIMEOUT);
				conn.setReadTimeout(READ_DATA_TIMEOUT);
				mContentLength = conn.getContentLength();

				InputStream is = conn.getInputStream();

				int bytesRead = 0;
				int bytesReadSoFar = 0;
				int mbsDownloadedSoFar = 0;
				
				while (true) {
					bytesRead = is.read(buffer, 0, BUFFER_SIZE);
					if (bytesRead == -1)
					{
						fos.close();
						if(wasForcePaused)
							mPlayer.start();
						break;
					}
						
					bytesReadSoFar += bytesRead;
					fos.write(buffer, 0, bytesRead);
					fos.flush(); // Important to flush the data since we are
									// currently playing it
					
					//Assume that the content is bigger than one MB
					if ((!wasPlaybackRequested)
							&& (bytesReadSoFar >= PLAYBACK_BEGIN_THRESHOLD)) {
						boolean failure = false;
						try {
							IDxDrmDlc dlc = DxDrmDlc.getDxDrmDlc(ProgressiveDownload.this);
							if (false == dlc.isDrmContent(mLocalContentPath) || dlc.verifyRights(mLocalContentPath)) {
								ProgressiveDownload.this
								.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										prepareVideo(mContentLength);
									}
								});

								wasPlaybackRequested = true;
								Log.i("Download progress:",
										"Initiated Playeback");
							} else {
								failure = true;
							}
						} catch (DrmGeneralFailureException e) {
							failure = true;
							DebugMode.logE(TAG, "Caught!", e);
						} catch (DrmInvalidFormatException e) {
							failure = true;
							DebugMode.logE(TAG, "Caught!", e);
						} catch (DrmClientInitFailureException e) {
							failure = true;
							DebugMode.logE(TAG, "Caught!", e);
						}
						if (failure) {
							Log.e("Download progress:",
									"No rights to play this file");
							break;
						}
					}

					// We need to protect against a playback that is faster than
					// the download. here we do it inside the download thread
					// but this can be a problem if the download will freeze
					// or if the download will be killed
				/*	if (mPlayer != null && 
							(mPlayer.getPlayerStatus() == VOOSMPType.VO_OSMP_STATUS.VO_OSMP_STATUS_PLAYING
							|| (wasForcePaused))) {
						long position = mPlayer.getPosition();
						long duration = mPlayer.getDuration();
						// These calculation incorrectly assumes file is in
						// constant bitrate
						// An accurate implementation should read file last
						// video time stamp
						long availbleDuration = (long) Math.ceil(bytesReadSoFar
								* ((double) duration / (double) mContentLength));

						if (position >= (availbleDuration - PLAYBACK_PAUSE_THRESHOLD) && !wasForcePaused) {
							mPlayer.pause();
							wasForcePaused = true;
							bufferSizeOnStop = bytesReadSoFar;
							Log.e("ProgressiveDownload",
									"Playback was pasued, not eonugh data availble");
						} else if (mPlayer.getPlayerStatus() != VOOSMPType.VO_OSMP_STATUS.VO_OSMP_STATUS_PLAYING 
								&& bufferSizeOnStop + playBackMinDataThreshold <= bytesReadSoFar) {
							playBackMinDataThreshold = playBackMinDataThreshold * 105 / 100;
							Log.e("ProgressiveDownload", "Playback was resumed");
							wasForcePaused = false;
							mPlayer.start();
						}
					}
				 	*/
					// print log after each MB of download
					if (bytesReadSoFar > mbsDownloadedSoFar * 1024 * 1024) {
						Log.i("Download progress:", mbsDownloadedSoFar
								+ " Mb Downloaded.");
						mbsDownloadedSoFar++;
					}
				}
				Log.i("Download progress:", "Download was completed");

			} catch (IOException e) {
				DebugMode.logE(TAG, "Caught!", e);
			}
		}
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
			if(mPlayer != null)
			{
				mPlayer.setView(mPlayerSurfaceView);
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.i(TAG, "Surface Destroyed");
			if(mPlayer != null)
				mPlayer.setView(null);
		}
	};
	
	/* Stop player and exit on Back key */
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			StopPlayback();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public VO_OSMP_RETURN_CODE onVOEvent(VO_OSMP_CB_EVENT_ID nID, int nParam1, int nParam2, Object obj)
	{
		if (nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_CB_VIDEO_SIZE_CHANGED)	// Video size changed
		{
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
		}	
		else if (nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_CB_ERROR)					// Error
		{
			// Display error dialog and stop player
			onError(mPlayer, 1, 0);
		}
		else if (nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_CB_PLAY_COMPLETE)
		{
			StopPlayback();
			Toast.makeText(this, "Playback has finished", Toast.LENGTH_LONG).show();
			// Finish should be called in order to exit the activity after playback
			// completes.
			finish();
		}else if (nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_CONNECTION_FAIL
				|| nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_DOWNLOAD_FAIL
				|| nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_DRM_FAIL
				|| nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_PLAYLIST_PARSE_ERR
				|| nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_CONNECTION_REJECTED
				|| nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_DRM_NOT_SECURE
				|| nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_CB_CODEC_NOT_SUPPORT
				|| nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_DRM_AV_OUT_FAIL)	// Errors
		{
			// Display error dialog and stop player
			onError(mPlayer, nID.getValue(), 0);

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
		String errStr = getString(R.string.str_ErrPlay_Message) + "\nError code is " + Integer.toHexString(what);

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
	
	private void initPlayer()
	{
		mPlayer = new VODXPlayerImpl();
		// Location of libraries
		String apkPath = getFilesDir().getParent() + "/lib/";	

		// Initialize SDK player		
		VOOSMPInitParam initParam = new VOOSMPInitParam();
		initParam.setLibraryPath(apkPath);
		initParam.setContext(this);
		VO_OSMP_RETURN_CODE nRet = mPlayer.init( VO_OSMP_PLAYER_ENGINE.VO_OSMP_VOME2_PLAYER, initParam);
		if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
			//TODO handle error
		}
			
		// Copy license file, 
		loadLicenseFile("voVidDec.dat");
		
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
		
		setSubtitles();
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
			DebugMode.logE(TAG, "Caught!", e);
		}
	}
	
	private void setSubtitles()
	{
		String customSubtitle = DxConstants.getSubtitleCustom();
		if (customSubtitle != null) {
			mPlayer.enableSubtitle(true);
			mPlayer.setSubtitlePath(customSubtitle);
		}
		else {
			mPlayer.enableSubtitle(false);
		}
	}

	@Override
	public VO_OSMP_RETURN_CODE onVOSyncEvent(VO_OSMP_CB_SYNC_EVENT_ID arg0,
			int arg1, int arg2, Object arg3) {
	
		return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
	}
}