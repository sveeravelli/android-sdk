package com.ooyala.android;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.graphics.Color;
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
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ooyala.android.OoyalaException.OoyalaErrorCode;
import com.ooyala.android.OoyalaPlayer.State;

/**
 * A wrapper around android.media.MediaPlayer
 * http://developer.android.com/reference/android/media/MediaPlayer.html
 *
 * For a list of Android supported media formats, see:
 * http://developer.android.com/guide/appendix/media-formats.html
 */
public class BaseStreamPlayer extends StreamPlayer implements OnBufferingUpdateListener, OnCompletionListener, OnErrorListener,
    OnPreparedListener, OnVideoSizeChangedListener, OnInfoListener, OnSeekCompleteListener,
    SurfaceHolder.Callback {

  private static final String TAG = BaseStreamPlayer.class.getName();
  protected MediaPlayer _player = null;
  protected SurfaceHolder _holder = null;
  protected String _streamUrl = "";
  protected int _width = 0;
  protected int _height = 0;
  private boolean _playQueued = false;
  private boolean _completedQueued = false;
  private int _timeBeforeSuspend = -1;
  private State _stateBeforeSuspend = State.INIT;
  Stream stream = null;

  @Override
  public void init(OoyalaPlayer parent, Set<Stream> streams) {
    stream =  Stream.bestStream(streams);
    if (stream == null) {
      Log.e(TAG, "ERROR: Invalid Stream (no valid stream available)");
      this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Invalid Stream");
      setState(State.ERROR);
      return;
    }

    if (parent == null) {
      this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Invalid Parent");
      setState(State.ERROR);
      return;
    }
    setState(State.LOADING);
    _streamUrl = stream.getUrlFormat().equals(Constants.STREAM_URL_FORMAT_B64) ? stream.decodedURL().toString().trim() : stream.getUrl().trim();
    setParent(parent);
    setupView();
    if (_player != null) { _player.reset(); }
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
    setState(State.LOADING);
    setupView();
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
    if (_player == null) {
      _timeBeforeSuspend = timeInMillis;
      return;
    }
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
        _player.setDataSource(_parent.getLayout().getContext(), Uri.parse(_streamUrl));
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
    this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "MediaPlayer Error: " + what + " " + extra);
    if (what == -10 && extra == -10) {  //I think this means unsupported format
      Log.e(TAG, "Unsupported video type given to base media player");
    }
    setState(State.ERROR);
    return false;
  }

  @Override
  public void onPrepared(MediaPlayer mp) {
    _view.setBackgroundColor(Color.TRANSPARENT);
    if (_width == 0 && _height == 0) {
      if (mp.getVideoHeight() > 0 && mp.getVideoWidth() > 0) {
        setVideoSize(mp.getVideoWidth(), mp.getVideoHeight());
      }
    }
    if (_timeBeforeSuspend > 0) {
      seekToTime(_timeBeforeSuspend);
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
    currentItemCompleted();
  }

  public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
    setVideoSize(width, height);
  }

  @Override
  public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
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

  @Override
  public void setParent(OoyalaPlayer parent) {
    super.setParent(parent);
  }

  @SuppressWarnings("deprecation")
  private void setupView() {
    createView(_parent.getLayout().getContext());
    _parent.getLayout().addView(_view);

    // Try to figure out the video size.  If not, use our default
    if (stream.getWidth() > 0 && stream.getHeight() > 0) {
      setVideoSize(stream.getWidth(), stream.getHeight());
    } else {
      setVideoSize(16,9);
    }

    _holder = _view.getHolder();
    _holder.addCallback(this);
    _holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  }

  private void createView(Context c) {
    _view = new MovieView(c);
    _view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
    _view.setBackgroundColor(Color.BLACK);
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

    // For m3u8s on 3+ phones, seeking before start() doesn't work.  If we're told seek is done
    // but seek isn't actaully done, try it again

    setChanged();
    notifyObservers(OoyalaPlayer.SEEK_COMPLETED_NOTIFICATION);
    // If we're resuming, and we're not near the desired seek position, try again
    if(_timeBeforeSuspend >= 0 && Math.abs(_player.getCurrentPosition() - _timeBeforeSuspend) > 3000) {
      Log.i(this.getClass().getName(), "Seek failed. currentPos: " + _player.getCurrentPosition() +
          ", timeBefore" + _timeBeforeSuspend + "duration: " + _player.getDuration());

      // This looks pretty nasty, but it's a very specific case of HLS videos during the race condition of
      // seek right when play starts
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      _player.seekTo(_timeBeforeSuspend);
    }

    // Seeking SHOULD work if our duration actually exists.  This is just in case, so we don't infinite loop
    if (_player.getDuration() != 0) {
      _timeBeforeSuspend = -1;
    }
    dequeuePlay();
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

      _player.stop();
      _player.release();
      _player = null;
    }
    removeView();
    _width = 0;
    _height = 0;
    _buffer = 0;
    setState(State.SUSPENDED);
  }

  @Override
  public void resume() {
    resume(_timeBeforeSuspend, _stateBeforeSuspend);
  }

  @Override
  public void resume(int millisToResume, State stateToResume) {
    _timeBeforeSuspend = millisToResume;
    if (stateToResume == State.PLAYING) {
      queuePlay();
    } else if (stateToResume == State.COMPLETED) {
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
  public boolean isPlaying() {
    return _player != null && _player.isPlaying();
  }

  @Override
  protected void setState(State state) {
    super.setState(state);
    dequeueAll();
  }
}
