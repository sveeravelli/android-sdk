package com.ooyala.android.player;

import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.OoyalaPlayerState;
import com.ooyala.android.Stream;

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
  protected Stream _stream = null;
  protected int _width = 0;
  protected int _height = 0;
  private boolean _playQueued = false;
  private int _timeBeforeSuspend = -1;
  protected Timer _playheadUpdateTimer = null;

  protected static final long TIMER_DELAY  = 0;
  protected static final long TIMER_PERIOD = 250;

  private class PlayheadUpdateTimerTask extends TimerTask {
    @Override
    public void run() {
      _playheadUpdateTimerHandler.sendEmptyMessage(0);
    }
  }

  // This is required because android enjoys making things difficult. talk to jigish if you got issues.
  private final Handler _playheadUpdateTimerHandler = new Handler() {
    public void handleMessage(Message msg) {
      setChanged();
      notifyObservers(OoyalaPlayer.TIME_CHANGED_NOTIFICATION);
    }
  };

  public MoviePlayer() {
    super();
    Log.d(this.getClass().getName(), "TEST - INSTANTIATING MOVIE PLAYER");
  }

  @Override
  public void init(OoyalaPlayer parent, Object stream) {
    Log.d(this.getClass().getName(), "TEST - init");
    if (stream == null) {
      this._error = "Invalid Stream";
      setState(OoyalaPlayerState.ERROR);
      return;
    }
    if (parent == null) {
      this._error = "Invalid Parent";
      setState(OoyalaPlayerState.ERROR);
      return;
    }
    setState(OoyalaPlayerState.LOADING);
    _stream = (Stream)stream;
    setParent(parent);
  }

  @Override
  public void pause() {
    _playQueued = false;
    switch (_state) {
      case PLAYING:
        stopPlayheadTimer();
        _player.pause();
        setState(OoyalaPlayerState.PAUSED);
      default:
        break;
    }
  }

  @Override
  public void play() {
    Log.d(this.getClass().getName(), "TEST - play");
    switch (_state) {
      case INIT:
      case LOADING:
        queuePlay();
        break;
      case PAUSED:
      case READY:
      case COMPLETED:
        _player.start();
        startPlayheadTimer();
        setState(OoyalaPlayerState.PLAYING);
      default:
        break;
    }
  }

  @Override
  public void stop() {
    Log.d(this.getClass().getName(), "TEST - stop");
    stopPlayheadTimer();
    _playQueued = false;
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

  private void createMediaPlayer() {
    Log.d(this.getClass().getName(), "TEST - createMediaPlayer");
    try {
      if (_player==null) {
        Log.d(this.getClass().getName(), "TEST - createMediaPlayer - create");
        _player=new MediaPlayer();
        _player.setScreenOnWhilePlaying(true);
      }
      else {
        Log.d(this.getClass().getName(), "TEST - createMediaPlayer - create else");
        stopPlayheadTimer();
        _player.stop();
        _player.reset();
      }

      _player.setDataSource(_stream.decodedURL().toString());
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
    Log.d(this.getClass().getName(), "TEST - onError: "+what+" "+extra);
    this._error = "MediaPlayer Error: "+what+" "+extra;
    setState(OoyalaPlayerState.ERROR);
    return false;
  }

  @Override
  public void onPrepared(MediaPlayer mp) {
    Log.d(this.getClass().getName(), "TEST - onPrepared "+mp.getVideoWidth()+"x"+mp.getVideoHeight());
    setState(OoyalaPlayerState.READY);
    if (_width == 0 && _height == 0) {
      Log.d(this.getClass().getName(), "TEST - onPrepared2 "+mp.getVideoWidth()+"x"+mp.getVideoHeight());
      if (mp.getVideoHeight() > 0 && mp.getVideoWidth() > 0)
        resize(mp.getVideoWidth(), mp.getVideoHeight());
    }
  }

  @Override
  public void onBufferingUpdate(MediaPlayer mp, int percent) {
    Log.d(this.getClass().getName(), "TEST - onBufferingUpdate");
    this._buffer = percent;
    setChanged();
    notifyObservers(OoyalaPlayer.BUFFER_CHANGED_NOTIFICATION);
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    Log.d(this.getClass().getName(), "TEST - onCompletion");
    currentItemCompleted();
  }

  public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
    Log.d(this.getClass().getName(), "TEST - onVideoSizeChangedd "+width+"x"+height);
    if (_width == 0 && _height == 0) {
      resize(width, height);
    }
  }

  @Override
  public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    Log.d(this.getClass().getName(), "TEST - surfaceChanged: "+(arg0 == null ? "null" : arg0.isCreating())+" | "+(arg0 == null || arg0.getSurfaceFrame() == null ? "null" : arg0.getSurfaceFrame().toShortString()));
  }

  @Override
  public void surfaceCreated(SurfaceHolder arg0) {
    Log.d(this.getClass().getName(), "TEST - surfaceCreated: "+(arg0 == null ? "null" : arg0.isCreating())+" | "+(arg0 == null || arg0.getSurfaceFrame() == null ? "null" : arg0.getSurfaceFrame().toShortString()));
    if (_width == 0 && _height == 0) {
      resize(_stream.getWidth(), _stream.getHeight());
    }
    if (_state == OoyalaPlayerState.LOADING) {
      createMediaPlayer();
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder arg0) {
    Log.d(this.getClass().getName(), "TEST - surfaceDestroyed");
  }

  @Override
  public void setParent(OoyalaPlayer parent) {
    Log.d(this.getClass().getName(), "TEST - setParent");
    super.setParent(parent);
    _parent.getLayout().addObserver(this);
    setupView();
  }

  private void setupView() {
    _view = new SurfaceView(_parent.getLayout().getContext());
    _view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
    _parent.getLayout().addView(_view);
    _holder = _view.getHolder();
    _holder.addCallback(this);
    _holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  }

  private void removeView() {
    if (_parent != null) {
      _parent.getLayout().removeView(_view);
    }
    if (_holder != null) {
      _holder.removeCallback(this);
    }
    _view = null;
    _holder = null;
  }

  @Override
  public void onSeekComplete(MediaPlayer arg0) {
    Log.d(this.getClass().getName(), "TEST - onSeekComplete");
    dequeuePlay();
  }

  @Override
  public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
    Log.d(this.getClass().getName(), "TEST - onInfo");
    return true;
  }

  @Override
  public void suspend() {
    if (_player != null) {
      _timeBeforeSuspend = _player.getCurrentPosition();
      stop();
      _player = null;
    }
    removeView();
    _width = 0;
    _height = 0;
    _buffer = 0;
    _playQueued = false;
    _state = OoyalaPlayerState.SUSPENDED;
  }

  @Override
  public void resume() {
    if (_state != OoyalaPlayerState.SUSPENDED) { return; }
    _state = OoyalaPlayerState.LOADING;
    setupView();
    queuePlay();
  }

  @Override
  public void destroy() {
    if (_player != null) {
      stop();
      _player = null;
    }
    removeView();
    if (_parent != null) {
      _parent.getLayout().deleteObserver(this);
    }
    _parent = null;
    _width = 0;
    _height = 0;
    _buffer = 0;
    _playQueued = false;
    _timeBeforeSuspend = -1;
    _state = OoyalaPlayerState.INIT;
  }

  protected void currentItemCompleted() {
    stopPlayheadTimer();
    setState(OoyalaPlayerState.COMPLETED);
  }

  // Must queue play and wait for ready
  private void queuePlay() {
    Log.d(this.getClass().getName(), "TEST - queuePlayy");
    _playQueued = true;
  }

  private void dequeuePlay() {
    Log.d(this.getClass().getName(), "TEST - dequeuePlay");
    if (_playQueued) {
      switch (_state) {
        case PAUSED:
        case READY:
        case COMPLETED:
          if (_timeBeforeSuspend > 0) {
            _player.seekTo(_timeBeforeSuspend);
            _timeBeforeSuspend = -1;
          } else {
            _playQueued = false;
            play();
          }
        default:
          break;
      }
    }
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
      _view.getHolder().setFixedSize(lp.width, lp.height);
      _view.invalidate();
      _view.requestLayout();
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
    dequeuePlay();
  }

  @Override
  public void update(Observable o, Object arg) {
    Log.d(this.getClass().getName(), "TEST - update");
    if (_width > 0 && _height > 0) {
      resize(_width,_height);
    }
  }

  // Timer tasks for playhead updates
  protected void startPlayheadTimer() {
    if (_playheadUpdateTimer != null) {
      stopPlayheadTimer();
    }
    _playheadUpdateTimer = new Timer();
    _playheadUpdateTimer.scheduleAtFixedRate(new PlayheadUpdateTimerTask(), TIMER_DELAY, TIMER_PERIOD);
  }

  protected void stopPlayheadTimer() {
    if (_playheadUpdateTimer != null) {
      _playheadUpdateTimer.cancel();
      _playheadUpdateTimer = null;
    }
  }
}
