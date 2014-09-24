package com.ooyala.mediaPlayer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * A wrapper around android.media.MediaPlayer
 * http://developer.android.com/reference/android/media/MediaPlayer.html
 *
 * For a list of Android supported media formats, see:
 * http://developer.android.com/guide/appendix/media-formats.html
 */
public class SimpleMediaPlayer implements OnBufferingUpdateListener, OnCompletionListener, OnErrorListener,
    OnPreparedListener, OnVideoSizeChangedListener, OnInfoListener, SurfaceHolder.Callback {

  private static final String TAG = SimpleMediaPlayer.class.getName();
  public MediaPlayer _player = null;
  public SurfaceView _view = null;
  protected SurfaceHolder _holder = null;
  protected String _streamUrl = "";
  protected ViewGroup _vg = null;

  public SimpleMediaPlayer(String url, ViewGroup vg) {
    _streamUrl = url;
    _vg = vg;

    _view = new SurfaceView(vg.getContext());
    _view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
    vg.addView(_view);

    _holder = _view.getHolder();
    _holder.addCallback(this);

  }

  @Override
  public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    Log.i(TAG, "Surface Changed");
  }

  @Override
  public void surfaceCreated(SurfaceHolder arg0) {
    Log.i(TAG, "Surface Created");
    createMediaPlayer();
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder arg0) {
    Log.i(TAG, "Surface Destroyed");
  }

  protected void createMediaPlayer() {
    try {
      if (_player == null) {
        _player = new MediaPlayer();
      } else {
        _player.stop();
        _player.reset();
      }

      _player.setDataSource(_streamUrl);
      _player.setDisplay(_holder);
      _player.setAudioStreamType(AudioManager.STREAM_MUSIC);
      _player.setScreenOnWhilePlaying(true);
      _player.setOnPreparedListener(this);
      _player.setOnCompletionListener(this);
      _player.setOnBufferingUpdateListener(this);
      _player.setOnErrorListener(this);
      _player.setOnInfoListener(this);
      _player.setOnVideoSizeChangedListener(this);
      _player.prepareAsync();
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  @Override
  public void onPrepared(MediaPlayer mp) {
    Log.d(TAG, "Prepared!");
    _player.start();
  }

  @Override
  public boolean onError(MediaPlayer mp, int what, int extra) {
    Log.e(TAG, "onERROR! " + what + ", " + extra);
    return false;
  }

  @Override
  public void onBufferingUpdate(MediaPlayer mp, int percent) {
    Log.d(TAG, "Buffering update! " + percent);

  }

  @Override
  public boolean onInfo(MediaPlayer mp, int what, int extra) {
    //These refer to when mid-playback buffering happens.  This doesn't apply to initial buffer
    if(what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
      Log.d(TAG, "onInfo: Buffering Starting! " + what + ", extra: " + extra);
    } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
      Log.d(TAG, "onInfo: Buffering Done! " + what + ", extra: " + extra);
    }
    return true;
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    Log.d(TAG, "onCompletion fired");
    _player.release();
  }

  @Override
  public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
    Log.d(TAG, "video size changed");
  }

}
