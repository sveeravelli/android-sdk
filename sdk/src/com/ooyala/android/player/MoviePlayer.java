package com.ooyala.android.player;

import java.net.URL;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.view.SurfaceView;
import android.view.View;

import com.ooyala.android.OoyalaPlayer.OoyalaPlayerState;

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
                                                   OnPreparedListener {

  protected MediaPlayer _player;
  protected OoyalaPlayerState _previousState;
  protected int _buffer;
  protected SurfaceView _view;

  public MoviePlayer() {
    super();
  }

  public MoviePlayer(Context c, URL url) {
    super(c, url);
    init(c, url);
  }

  public void init(Context c, Object url) {
    if (url == null) {
      this._error = "Invalid URL";
      this._state = OoyalaPlayerState.OoyalaPlayerStateError;
      return;
    }
    this._state = OoyalaPlayerState.OoyalaPlayerStateLoading;
    _player = new MediaPlayer();
    _player.setOnPreparedListener(this);
    _player.setOnErrorListener(this);
    _view = new SurfaceView(c);
    _player.setDisplay(_view.getHolder());
    try {
      _player.setDataSource(url.toString());
      _player.prepareAsync();
    } catch (Exception e) {
      e.printStackTrace();
      this._error = "Exception: "+e.getMessage();
      this._state = OoyalaPlayerState.OoyalaPlayerStateError;
    }
  }

  @Override
  public void pause() {
    _player.pause();
    _state = OoyalaPlayerState.OoyalaPlayerStatePaused;
  }

  @Override
  public void play() {
    _player.start();
    _state = OoyalaPlayerState.OoyalaPlayerStatePlaying;
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
    this._state = OoyalaPlayerState.OoyalaPlayerStateError;
    return false;
  }

  @Override
  public void onPrepared(MediaPlayer mp) {
    this._state = OoyalaPlayerState.OoyalaPlayerStateReadyToPlay;
  }

  @Override
  public void onBufferingUpdate(MediaPlayer mp, int percent) {
    this._buffer = percent;
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    this._state = OoyalaPlayerState.OoyalaPlayerStateCompleted;
  }

  public View getView() {
    return _view;
  }

}
