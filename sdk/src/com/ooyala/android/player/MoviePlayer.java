package com.ooyala.android.player;

import java.net.URL;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;

import com.ooyala.android.OoyalaPlayer.PlayerState;

public class MoviePlayer extends Player implements OnBufferingUpdateListener,
                                                   OnCompletionListener,
                                                   OnErrorListener,
                                                   OnPreparedListener {

  protected MediaPlayer _player;
  protected PlayerState _previousState;
  protected int _buffer;

  public MoviePlayer(URL url) {
    super(url);
    if (url == null) {
      this._error = "Invalid URL";
      this._state = PlayerState.PlayerStateError;
      return;
    }
    this._state = PlayerState.PlayerStateLoading;
    _player = new MediaPlayer();
    _player.setOnPreparedListener(this);
    _player.setOnErrorListener(this);
    try {
      /**
       * TODO[jigish] set surface holder (similar to OoyalaPlayerView)
       * _player.setSurface(SurfaceHolder sh);
       */
      _player.setDataSource(url.toString());
      _player.prepareAsync();
    } catch (Exception e) {
      e.printStackTrace();
      this._error = "Exception: "+e.getMessage();
      this._state = PlayerState.PlayerStateError;
    }
  }
  
  @Override
  public void pause() {
    _player.pause();
    _state = PlayerState.PlayerStatePaused;
  }
  
  @Override
  public void play() {
    _player.start();
    _state = PlayerState.PlayerStatePlaying;
  }

  @Override
  public void stop() {
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
  
  @Override
  public boolean onError(MediaPlayer mp, int what, int extra) {
    this._error = "MediaPlayer Error: "+what+" "+extra;
    this._state = PlayerState.PlayerStateError;
    return false;
  }

  @Override
  public void onPrepared(MediaPlayer mp) {
    this._state = PlayerState.PlayerStateReadyToPlay;
  }

  @Override
  public void onBufferingUpdate(MediaPlayer mp, int percent) {
    this._buffer = percent;
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    this._state = PlayerState.PlayerStateCompleted;
  }

}
