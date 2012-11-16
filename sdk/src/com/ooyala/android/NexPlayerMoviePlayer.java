package com.ooyala.android;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ooyala.android.OoyalaPlayer.State;

/**
 * A wrapper around android.media.MediaPlayer
 * http://developer.android.com/reference/android/media/MediaPlayer.html
 * 
 * For a list of Android supported media formats, see:
 * http://developer.android.com/guide/appendix/media-formats.html
 */
class NexPlayerMoviePlayer extends Player implements OnBufferingUpdateListener, OnCompletionListener, OnErrorListener,
    OnPreparedListener, OnVideoSizeChangedListener, OnInfoListener, OnSeekCompleteListener,
    SurfaceHolder.Callback {

  protected MediaPlayer _player = null;
  protected SurfaceHolder _holder = null;
  protected String _streamUrl = "";
  protected int _width = 0;
  protected int _height = 0;
  private boolean _playQueued = false;
  private boolean _completedQueued = false;
  private int _timeBeforeSuspend = -1;
  private State _stateBeforeSuspend = State.INIT;
  protected Timer _playheadUpdateTimer = null;
  private int _lastPlayhead = -1;

  protected static final long TIMER_DELAY = 0;
  protected static final long TIMER_PERIOD = 250;

  protected class PlayheadUpdateTimerTask extends TimerTask {
    @Override
    public void run() {
      if (_player == null) return;

      if (_lastPlayhead != _player.getCurrentPosition() && _player.isPlaying()) {
        _playheadUpdateTimerHandler.sendEmptyMessage(0);
      }
      _lastPlayhead = _player.getCurrentPosition();
    }
  }

  // This is required because android enjoys making things difficult. talk to jigish if you got issues.
  private final Handler _playheadUpdateTimerHandler = new Handler() {
    public void handleMessage(Message msg) {
      setChanged();
      notifyObservers(OoyalaPlayer.TIME_CHANGED_NOTIFICATION);
    }
  };

  @Override
  public void init(OoyalaPlayer parent, Object stream) {
    if (stream == null) {
      Log.e(this.getClass().getName(), "ERROR: Invalid Stream (no valid stream available)");
      this._error = "Invalid Stream";
      setState(State.ERROR);
      return;
    }
    if (parent == null) {
      this._error = "Invalid Parent";
      setState(State.ERROR);
      return;
    }
    setState(State.LOADING);
    _streamUrl = (String)stream;
    setParent(parent);
  }

  @Override
  public void pause() {
    _playQueued = false;
    switch (_state) {
      case PLAYING:
        stopPlayheadTimer();
        _player.pause();
        setState(State.PAUSED);
      default:
        break;
    }
  }

  @Override
  public void play() {
    _playQueued = false;
    switch (_state) {
      case INIT:
      case LOADING:
        queuePlay();
        break;
      case PAUSED:
      case READY:
      case COMPLETED:
        _player.start();
        setState(State.PLAYING);
        startPlayheadTimer();
      default:
        break;
    }
  }

  @Override
  public void stop() {
    stopPlayheadTimer();
    _playQueued = false;
    _player.stop();
    _player.release();
  }

  @Override
  public void reset() {
    suspend(0, State.PAUSED);
    resume();
  }

  @Override
  public int currentTime() {
    if (_player == null) { return 0; }
    switch (_state) {
      case INIT:
      case LOADING:
      case SUSPENDED:
        return 0;
      default:
        break;
    }
    return _player.getCurrentPosition();
  }

  @Override
  public int duration() {
    if (_player == null) { return 0; }
    switch (_state) {
      case INIT:
      case LOADING:
      case SUSPENDED:
        return 0;
      default:
        break;
    }
    return _player.getDuration();
  }

  @Override
  public int buffer() {
    return this._buffer;
  }

  @Override
  public void seekToTime(int timeInMillis) {
    if (_player == null) { return; }
    _player.seekTo(timeInMillis);
  }

  protected void createMediaPlayer() {
    try {
      if (_player == null) {
        _player = new MediaPlayer();
      } else {
        stopPlayheadTimer();
        _player.stop();
        _player.reset();
      }
      // Set cookies if they exist for 4.0+ Secure HLS Support
      if (Build.VERSION.SDK_INT >= Constants.SDK_INT_ICS) {
        String cookieHeaderStr = null;
        for (String cookieName : OoyalaAPIHelper.cookies.keySet()) {
          if (cookieHeaderStr == null) {
            cookieHeaderStr = (cookieName + "=" + OoyalaAPIHelper.cookies.get(cookieName));
          } else {
            cookieHeaderStr += ("; " + cookieName + "=" + OoyalaAPIHelper.cookies.get(cookieName));
          }
        }
        Map<String, String> cookieHeader = new HashMap<String, String>();
        cookieHeader.put(Constants.HTML_COOKIE_HEADER_NAME, cookieHeaderStr);
        _player.setDataSource(OoyalaAPIHelper.context, Uri.parse(_streamUrl));
      } else {
        _player.setDataSource(_streamUrl);
      }
      _player.setDisplay(_holder);
      _player.setAudioStreamType(AudioManager.STREAM_MUSIC);
      _player.setScreenOnWhilePlaying(true);
      _player.setOnPreparedListener(this);
      _player.setOnCompletionListener(this);
      _player.setOnBufferingUpdateListener(this);
      _player.setOnErrorListener(this);
      _player.setOnInfoListener(this);
      _player.setOnSeekCompleteListener(this);
      _player.setOnVideoSizeChangedListener(this);
      _player.prepareAsync();
    } catch (Throwable t) {
    }
  }

  @Override
  public boolean onError(MediaPlayer mp, int what, int extra) {
    this._error = "MediaPlayer Error: " + what + " " + extra;
    setState(State.ERROR);
    return false;
  }

  @Override
  public void onPrepared(MediaPlayer mp) {
    if (_width == 0 && _height == 0) {
      if (mp.getVideoHeight() > 0 && mp.getVideoWidth() > 0) {
        setVideoSize(mp.getVideoWidth(), mp.getVideoHeight());
      }
    }
    if (_timeBeforeSuspend > 0) {
      seekToTime(_timeBeforeSuspend);
      _timeBeforeSuspend = -1;
    }
    setState(State.READY);
  }

  @Override
  public void onBufferingUpdate(MediaPlayer mp, int percent) {
    this._buffer = percent;
    setChanged();
    notifyObservers(OoyalaPlayer.BUFFER_CHANGED_NOTIFICATION);
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    currentItemCompleted();
  }

  public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
    if (_width == 0 && _height == 0 && height > 0) {
      setVideoSize(width, height);
    }
  }

  @Override
  public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
  }

  @Override
  public void surfaceCreated(SurfaceHolder arg0) {
    if (_state == State.LOADING) {
      createMediaPlayer();
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder arg0) {
  }

  @Override
  public void setParent(OoyalaPlayer parent) {
    super.setParent(parent);
    setupView();
  }

  @SuppressWarnings("deprecation")
  private void setupView() {
    createView(_parent.getLayout().getContext());
    _parent.getLayout().addView(_view);
    _holder = _view.getHolder();
    _holder.addCallback(this);
    _holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  }

  private void createView(Context c) {
    _view = new MovieView(c);
    _view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
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
    dequeuePlay();
    _lastPlayhead = _player.getCurrentPosition();
  }

  @Override
  public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
    return true;
  }

  @Override
  public void suspend() {
    suspend(_player != null ? _player.getCurrentPosition() : 0, _state);
  }

  @Override
  public void suspend(int millisToResume, State stateToResume) {
    if (_state == State.SUSPENDED) { return; }
    if (_player != null) {
      _timeBeforeSuspend = millisToResume;
      _stateBeforeSuspend = stateToResume;
      stop();
      _player = null;
    }
    removeView();
    _width = 0;
    _height = 0;
    _buffer = 0;
    _playQueued = false;
    setState(State.SUSPENDED);
  }

  @Override
  public void resume() {
    if (_state != State.SUSPENDED) { return; }
    setState(State.LOADING);
    setupView();
    if (_stateBeforeSuspend == State.PLAYING) {
      queuePlay();
    } else if (_stateBeforeSuspend == State.COMPLETED) {
      queueCompleted();
    }
  }

  @Override
  public void destroy() {
    if (_player != null) {
      stop();
      _player = null;
    }
    removeView();
    _parent = null;
    _width = 0;
    _height = 0;
    _buffer = 0;
    _playQueued = false;
    _timeBeforeSuspend = -1;
    _state = State.INIT;
  }

  private void setVideoSize(int width, int height) {
    _width = width;
    _height = height;
    ((MovieView) _view).setAspectRatio(((float) _width) / ((float) _height));
  }

  protected void currentItemCompleted() {
    stopPlayheadTimer();
    setState(State.COMPLETED);
  }

  private void queueCompleted() {
    _completedQueued = true;
  }

  private void dequeueCompleted() {
    if (_completedQueued) {
      _playQueued = false;
      _completedQueued = false;
      setState(State.COMPLETED);
    }
  }

  // Must queue play and wait for ready
  private void queuePlay() {
    _playQueued = true;
  }

  private void dequeuePlay() {
    if (_playQueued) {
      switch (_state) {
        case PAUSED:
        case READY:
        case COMPLETED:
          _playQueued = false;
          play();
        default:
          break;
      }
    }
  }

  private void dequeueAll() {
    dequeueCompleted();
    dequeuePlay();
  }

  @Override
  protected void setState(State state) {
    super.setState(state);
    dequeueAll();
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
