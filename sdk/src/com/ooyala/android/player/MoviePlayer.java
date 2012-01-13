package com.ooyala.android.player;

import java.util.Observable;
import java.util.Observer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.OoyalaPlayerState;

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
                                                   OnInfoListener,
                                                   OnSeekCompleteListener,
                                                   SurfaceHolder.Callback,
                                                   Observer {

  protected MediaPlayer _player = null;
  protected int _buffer = 0;
  protected SurfaceHolder _holder = null;
  protected String _url = null;
  protected int _width = 0;
  protected int _height = 0;

  public MoviePlayer() {
    super();
  }

  @Override
  public void init(OoyalaPlayer parent, Object url) {
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
    Log.d(this.getClass().getName(), "TEST - play - w:"+_player.getVideoWidth()+" h:"+_player.getVideoHeight());
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
      _player.setOnCompletionListener(this);
      _player.setOnBufferingUpdateListener(this);
      _player.setOnErrorListener(this);
      _player.setOnInfoListener(this);
      _player.setOnSeekCompleteListener(this);
      _player.setOnVideoSizeChangedListener(this);
      _player.prepareAsync();
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
    Log.d(this.getClass().getName(), "TEST - onVideoSizeChangedd "+width+"x"+height);
    if (_width == 0 && _height == 0) {
      resize(width, height);
    }
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
  }

  @Override
  public void setParent(OoyalaPlayer parent) {
    Log.d(this.getClass().getName(), "TEST - setParent");
    super.setParent(parent);
    _parent.getLayout().addObserver(this);
    _view = new SurfaceView(parent.getLayout().getContext());
    Log.d(this.getClass().getName(), "TEST - setParent setSize");
    _view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
    // TODO set height/width based on aspect ratio!
    Log.d(this.getClass().getName(), "TEST - setParent addView");
    _parent.getLayout().addView(_view);
    Log.d(this.getClass().getName(), "TEST - setParent after addView");
    _holder = _view.getHolder();
    _holder.addCallback(this);
    _holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  }

  @Override
  public void onSeekComplete(MediaPlayer arg0) {
    Log.d(this.getClass().getName(), "TEST - onSeekComplete");
  }

  @Override
  public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
    Log.d(this.getClass().getName(), "TEST - onInfo");
    return true;
  }

  // Resizing related crap. damn android for making me do this.
  public void resize(int width, int height) {
    Log.d(this.getClass().getName(), "TEST - resize: "+width+","+height);
    _width = width;
    _height = height;
    switch(_state) {
      case INIT:
      case LOADING:
        queueResize();
        return;
      default:
        break;
    }
    Log.d(this.getClass().getName(), "TEST - resizing: "+width+","+height);
    ViewGroup.LayoutParams parentLP = _parent.getLayout().getLayoutParams();
    if (parentLP.width == ViewGroup.LayoutParams.WRAP_CONTENT || parentLP.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
      // Wrap Content -> just use video's height and width.
      ViewGroup.LayoutParams lp = _view.getLayoutParams();
      lp.width = _width;
      lp.height = _height;
      _view.setLayoutParams(lp);
    } else {
      // Fill Parent or explicit width/height -> maintain aspect ratio
      ViewGroup.LayoutParams lp = _view.getLayoutParams();
      int pWidth = _parent.getLayout().getWidth();
      int pHeight = _parent.getLayout().getHeight();
      Log.d(this.getClass().getName(), "TEST - resizing else lp: "+parentLP.width+","+parentLP.height);
      Log.d(this.getClass().getName(), "TEST - resizing else pl: "+pWidth+","+pHeight);
      if (_width == 0 || _height == 0 || pWidth == 0 || pHeight == 0) {
        Log.e(this.getClass().getName(), "ERROR: cannot set video size");
        return;
      }
      float availableAspectRatio = ((float)pWidth)/((float)pHeight);
      float videoAspectRatio = ((float)_width)/((float)_height);
      if (availableAspectRatio > videoAspectRatio) {
        // bounded by the available height
        lp.width = (int)(((float)_width)*((float)pHeight)/((float)_height));
        lp.height = pHeight;
        Log.d(this.getClass().getName(), "TEST - resizing bounded by height: "+lp.width+","+lp.height);
      } else if (availableAspectRatio < videoAspectRatio) {
        // bounded by the available width
        lp.width = pWidth;
        lp.height = (int)(((float)_height)*((float)pWidth)/((float)_width));;
        Log.d(this.getClass().getName(), "TEST - resizing bounded by width: "+lp.width+","+lp.height);
      } else {
        // no bound, aspect ratios are the same.
        lp.width = pWidth;
        lp.height = pHeight;
        Log.d(this.getClass().getName(), "TEST - resizing bounded by nothing: "+lp.width+","+lp.height);
      }
      _view.setLayoutParams(lp);
    }
  }

  protected void queueResize() {
    Log.d(this.getClass().getName(), "TEST - queueResize");
    _resizeQueued = true;
  }

  protected void dequeueResize() {
    Log.d(this.getClass().getName(), "TEST - dequeueResize");
    if (_resizeQueued) {
      _resizeQueued = false;
      resize(_width, _height);
    }
  }

  @Override
  protected void setState(OoyalaPlayerState state) {
    if (state != OoyalaPlayerState.INIT && state != OoyalaPlayerState.LOADING) {
      dequeueResize();
    }
    super.setState(state);
  }

  @Override
  public void update(Observable o, Object arg) {
    Log.d(this.getClass().getName(), "TEST - update");
    if (_width > 0 && _height > 0) {
      resize(_width,_height);
    }
  }
}
