package com.example.secureplayer.obsolete;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.widget.Toast;

import com.example.secureplayer.DxConstants;
import com.example.secureplayer.R;

public class PlaybackActivity extends Activity implements OnErrorListener,
		OnCompletionListener, OnPreparedListener {

	private String mPlaybackPath = DxConstants.getActiveContent().getPlayBackPath();
	private IDxVideoView mVideoView = null;
	private boolean mStartedPlayBack = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPlaybackPath = DxConstants.getActiveContent().getPlayBackPath();
		setContentView(R.layout.main);
		mVideoView = (IDxVideoView) findViewById(R.id.video_view);
		play();
	}

	private void play() {
		if (mVideoView != null) {
			// Register listeners
			mVideoView.setOnPreparedListener(this);
			mVideoView.setOnErrorListener(this);
			mVideoView.setOnCompletionListener(this);

			mVideoView.setVideoPath(mPlaybackPath);
			
		}
	}

	public void onPrepared(MediaPlayer mp) {
		// Start should be called after video is prepared.
		
		
		if(!mStartedPlayBack)
			mVideoView.start();
		
		mStartedPlayBack = true;
	}

	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
//		if (DxVideoView.VIDEO_VIEW_ERROR_CODE__OPL_VIOLATION == arg2)
//		{
//			Toast.makeText(this, "OPL violation detected.", Toast.LENGTH_LONG).show();
//		}
//		else 
//		{
//			Toast.makeText(this, "An error occurred.", Toast.LENGTH_LONG).show();
//		}
		finish();
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mVideoView != null) {
			mVideoView.onDestroy();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mVideoView != null) {
			mVideoView.onResume();

			if(!mStartedPlayBack)
			{
				mStartedPlayBack = true;
				mVideoView.start();
			}
		}
	}

	@Override
	protected void onPause() {
		if (mVideoView != null) 
		{
			if(mVideoView.isPlaying())
			{
				mStartedPlayBack = false;
			}
			mVideoView.onPause();
		}
		super.onPause();
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		Toast.makeText(this, "Playback has finished", Toast.LENGTH_LONG).show();

		// Finish should be called in order to exit the activity after playback
		// completes.
		finish();
	}
}
