package com.ooyala.android.player;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.ooyala.android.OoyalaPlayer.OoyalaPlayerState;
import com.ooyala.android.OoyalaPlayerLayout;

// TODO support these:
//    _mediaPlayer.setOnInfoListener(this);
//    _mediaPlayer.setOnSeekCompleteListener(this);
//    _mediaPlayer.setOnVideoSizeChangedListener(this);
//    _mediaPlayer.setDisplay(holder);

/**
 * A wrapper around android.media.MediaPlayer
 * http://developer.android.com/reference/android/media/MediaPlayer.html
 *
 * For a list of Android supported media formats, see:
 * http://developer.android.com/guide/appendix/media-formats.html
 */
public class MoviePlayer extends Player implements OnBufferingUpdateListener,
                                                   OnCompletionListener,
                                                   OnErrorListener,
                                                   OnPreparedListener,
                                                   OnVideoSizeChangedListener,
                                                   SurfaceHolder.Callback {

  protected MediaPlayer _player = null;
  protected int _buffer = 0;
  protected SurfaceHolder _holder = null;
  protected int _width = 0;
  protected int _height = 0;
  protected String _url = null;

  public MoviePlayer() {
    super();
  }

  @Override
  public void init(OoyalaPlayerLayout parent, Object url) {
    Log.d(this.getClass().getName(), "TEST - init");
    if (url == null) {
      this._error = "Invalid URL";
      setState(OoyalaPlayerState.ERROR);
      return;
    }
    if (parent == null) {
      this._error = "Invalid Parent";
      setState(OoyalaPlayerState.ERROR);
      return;
    }
    setState(OoyalaPlayerState.LOADING);
    _url = url.toString();
    setParent(parent);
  }

  @Override
  public void pause() {
    _player.pause();
    setState(OoyalaPlayerState.PAUSED);
  }

  @Override
  public void play() {
    Log.d(this.getClass().getName(), "TEST - play");
    _player.start();
    setState(OoyalaPlayerState.PLAYING);
  }

  @Override
  public void stop() {
    Log.d(this.getClass().getName(), "TEST - stop");
    _player.stop();
    _player.release();
  }

  @Override
  public int currentTime() {
    return _player.getCurrentPosition();
  }

  @Override
  public int duration() {
    return _player.getDuration();
  }

  @Override
  public int buffer() {
    return this._buffer;
  }

  @Override
  public boolean seekable() {
    return true;
  }

  @Override
  public void seekToTime(int timeInMillis) {
    _player.seekTo(timeInMillis);
  }

  private void createMediaPlayer(String url) {
    Log.d(this.getClass().getName(), "TEST - createMediaPlayer");
    try {
      if (_player==null) {
        Log.d(this.getClass().getName(), "TEST - createMediaPlayer - create");
        _player=new MediaPlayer();
        _player.setScreenOnWhilePlaying(true);
      }
      else {
        Log.d(this.getClass().getName(), "TEST - createMediaPlayer - create else");
        _player.stop();
        _player.reset();
      }

      _player.setDataSource(url);
      Log.d(this.getClass().getName(), "TEST - FRAME SIZE: "+_holder.getSurfaceFrame().right+"x"+_holder.getSurfaceFrame().bottom);
      _player.setDisplay(_holder);
      _player.setAudioStreamType(AudioManager.STREAM_MUSIC);
      _player.setOnPreparedListener(this);
      _player.prepareAsync();
      _player.setOnCompletionListener(this);
    }
    catch (Throwable t) {
      Log.e(this.getClass().getName(), "TEST - Exception in media prep", t);
    }
  }

  @Override
  public boolean onError(MediaPlayer mp, int what, int extra) {
    Log.d(this.getClass().getName(), "TEST - onError");
    this._error = "MediaPlayer Error: "+what+" "+extra;
    setState(OoyalaPlayerState.ERROR);
    return false;
  }

  @Override
  public void onPrepared(MediaPlayer mp) {
    Log.d(this.getClass().getName(), "TEST - onPrepared");
    _width = _player.getVideoWidth();
    _height = _player.getVideoHeight();
    if (_width != 0 && _height != 0) {
      Log.d(this.getClass().getName(), "TEST - onPrepared - start");
      _holder.setFixedSize(_width, _height);
    } else {
      Log.d(this.getClass().getName(), "TEST - onPrepared - start else");
    }
    setState(OoyalaPlayerState.READY);
  }

  @Override
  public void onBufferingUpdate(MediaPlayer mp, int percent) {
    Log.d(this.getClass().getName(), "TEST - onBufferingUpdate");
    this._buffer = percent;
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    Log.d(this.getClass().getName(), "TEST - onCompletion");
    setState(OoyalaPlayerState.COMPLETED);
  }

  public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
    Log.d(this.getClass().getName(), "TEST - onVideoSizeChanged "+width+"x"+height);
  }

  @Override
  public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    // TODO Auto-generated method stub
    Log.d(this.getClass().getName(), "TEST - surfaceChanged: "+(arg0 == null ? "null" : arg0.isCreating())+" | "+(arg0 == null || arg0.getSurfaceFrame() == null ? "null" : arg0.getSurfaceFrame().toShortString()));
  }

  @Override
  public void surfaceCreated(SurfaceHolder arg0) {
    // TODO Auto-generated method stub
    Log.d(this.getClass().getName(), "TEST - surfaceCreated: "+(arg0 == null ? "null" : arg0.isCreating())+" | "+(arg0 == null || arg0.getSurfaceFrame() == null ? "null" : arg0.getSurfaceFrame().toShortString()));
    if (_state == OoyalaPlayerState.LOADING) {
      createMediaPlayer(_url);
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder arg0) {
    // TODO Auto-generated method stub
    Log.d(this.getClass().getName(), "TEST - surfaceDestroyed");
    switch (_state) {
    case OoyalaPlayerStatePlaying:
      _player.pause();

    default:
      break;
    }
  }

  @Override
  public void setParent(OoyalaPlayerLayout parent) {
    super.setParent(parent);
    _view = new SurfaceView(parent.getContext());
    _view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    _parent.addView(_view,300,300);
    _holder = _view.getHolder();
    _holder.addCallback(this);
    _holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  }

  @Override
  public boolean suspend() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean resume() {
    // TODO Auto-generated method stub
    return false;
  }
}
