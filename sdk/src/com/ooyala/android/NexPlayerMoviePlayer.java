package com.ooyala.android;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nextreaming.nexplayerengine.GLRenderer;
import com.nextreaming.nexplayerengine.NexClosedCaption;
import com.nextreaming.nexplayerengine.NexContentInformation;
import com.nextreaming.nexplayerengine.NexID3TagInformation;
import com.nextreaming.nexplayerengine.NexPlayer;
import com.ooyala.android.OoyalaPlayer.State;

/**
 * A wrapper around android.media.MediaPlayer
 * http://developer.android.com/reference/android/media/MediaPlayer.html
 *
 * For a list of Android supported media formats, see:
 * http://developer.android.com/guide/appendix/media-formats.html
 */
class NexPlayerMoviePlayer extends Player implements NexPlayer.IListener,
    GLRenderer.IListener, SurfaceHolder.Callback {
  private static final String TAG = "[PLAYER_SAMPLE]";

  protected NexPlayer _player = null;
  protected SurfaceHolder _holder = null;
  protected String _streamUrl = "";
  protected int _width = 0;
  protected int _height = 0;
  private GLRenderer glRenderer = null;
  private boolean _playQueued = false;
  private boolean _completedQueued = false;
  private State _stateBeforeSuspend = State.INIT;
  private int _playheadTime = 0;
  private boolean _useOpenGL = true;
  protected static final long TIMER_DELAY = 0;
  protected static final long TIMER_PERIOD = 250;

  @Override
  public void init(OoyalaPlayer parent, Object stream) {
    if (stream == null) {
      Log.e(this.getClass().getName(),
          "ERROR: Invalid Stream (no valid stream available)");
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
    _streamUrl = (String) stream;
    setParent(parent);
  }

  @Override
  public void pause() {
    _playQueued = false;
    switch (_state) {
      case PLAYING:
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
        _player.start(0);
        setState(State.PLAYING);
      default:
        break;
    }
  }

  @Override
  public void stop() {
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
    return _playheadTime;
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
    return _player.getContentInfo().mMediaDuration;
  }

  @Override
  public int buffer() {
    return this._buffer;
  }

  @Override
  public void seekToTime(int timeInMillis) {
    if (_player == null) { return; }
    _player.seek(timeInMillis);
  }

  protected void createMediaPlayer() {
    try {
      if (_player == null) {
        _player = new NexPlayer();
      } else {
        _player.stop();
        _player.resume();
      }

      _player.open("http://www.playon.tv/online/iphone5/main.m3u8", null, null,
          NexPlayer.NEXPLAYER_SOURCE_TYPE_STREAMING,
          NexPlayer.NEXPLAYER_TRANSPORT_TYPE_TCP, 0);

      System.gc();
    } catch (Throwable t) {
      Log.e(TAG, "COULD NOT CREATE MEDIA PLAYER", t);
    }
  }

  public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
    if (_width == 0 && _height == 0 && height > 0) {
      setVideoSize(width, height);
    }
  }

  @Override
  public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

    Log.d(TAG, "onTextRenderInit ClassNum : " + arg0 + ", " + arg1 + ", "
        + arg2 + ", " + arg3);
    _player.GLInit(arg1, arg2);
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
    // _view = new MovieView(c);
    _player = new NexPlayer();

    _player.init(OoyalaAPIHelper.context, android.os.Build.MODEL,
        NexPlayer.NEX_DEVICE_USE_OPENGL, 3, 1);
    if (_player.GetRenderMode() == NexPlayer.NEX_USE_RENDER_OPENGL) {
      _useOpenGL = true;
    }
    _player.setListener(this);
    glRenderer = new GLRenderer(c, _player, this, 1);
    _view = glRenderer;
    _view.setLayoutParams(new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
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
  public void suspend() {
    suspend(_player != null ? _playheadTime : 0, _state);
  }

  @Override
  public void suspend(int millisToResume, State stateToResume) {
    if (_state == State.SUSPENDED) {
      return;
    }
    if (_player != null) {
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
    _state = State.INIT;
  }

  private void setVideoSize(int width, int height) {
    _width = width;
    _height = height;
    ((MovieView) _view).setAspectRatio(((float) _width) / ((float) _height));
  }

  protected void currentItemCompleted() {
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

  // NexPlayer::IListener implementation
  // onEndofContent is called when content meets end.
  @Override
  public void onEndOfContent(NexPlayer mp) {

  }

  // onStartVideoTask is called when VideoTask is created.
  @Override
  public void onStartVideoTask(NexPlayer mp) {
    Log.d(TAG, "onStartVideoTask called");
  }

  // onStartAudioTask is called when AudioTask is created.
  @Override
  public void onStartAudioTask(NexPlayer mp) {
    Log.d(TAG, "void onStartAudioTask called");
  }

  // onTime is called periodically during playing.
  @Override
  public void onTime(NexPlayer mp, int sec) {
    Log.d(TAG, "onTime called (" + sec + " msec)");
    _playheadTime = sec;

    setChanged();
    notifyObservers(OoyalaPlayer.TIME_CHANGED_NOTIFICATION);
  }

  // onError is called when error is generated.
  @Override
  public void onError(NexPlayer mp, NexPlayer.NexErrorCode errorCode) {
    if (errorCode == null) {
      Log.d(TAG, "onError: Unknown");
    } else {
      Log.d(TAG,
          "onError: 0x" + Integer.toHexString(errorCode.getIntegerCode())
              + " (" + errorCode.getCategory() + "/" + errorCode.name() + ")");
    }

  }

  // this is called when nexPlayer change state.
  @Override
  public void onStateChanged(NexPlayer mp, int pre, int now) {
    Log.d(TAG, "onStateChanged called (" + pre + "->" + now + ")");
  }

  // this is called when nexPlayer's signal status is changed.
  @Override
  public void onSignalStatusChanged(NexPlayer mp, int pre, int now) {
    Log.d(TAG, "onSignalStatusChanged called before: " + pre + ", after : "
        + now);
  }

  // not support to record now
  @Override
  public void onRecordingErr(NexPlayer mp, int err) {
    Log.d(TAG, "onRecordingErr called " + err);
  }

  // not support to record now
  @Override
  public void onRecording(NexPlayer mp, int recDuration, int recSize) {
    Log.d(TAG, "onRecording called Duratoin: " + recDuration + ", Size: "
        + recSize);
  }

  // not support to record now
  @Override
  public void onRecordingEnd(NexPlayer mp, int success) {
    Log.d(TAG, "onRecordingEnd called " + success);
  }

  // not support timeshift now
  @Override
  public void onTimeshiftErr(NexPlayer mp, int err) {
    Log.d(TAG, "onTimeshiftErr called " + err);
  }

  // not support timeshift now
  @Override
  public void onTimeshift(NexPlayer mp, int currTime, int TotalTime) {
    Log.d(TAG, "onTimeshift called curTime: " + currTime + ", TotalTime: "
        + TotalTime);
  }

  // this is called when audio renderer is created.
  // HLS, SmoothStreaming cases, this is called several times.
  @Override
  public void onAudioRenderCreate(NexPlayer mp, int samplingRate, int channelNum) {
    Log.d(TAG, "onAudioRenderCreate called (SamplingRate:" + samplingRate
        + " ChannelNum : " + channelNum);
  }

  // this is called when audio renderer is deleted.
  // HLS, SmoothStreaming cases, this is called several times.
  @Override
  public void onAudioRenderDelete(NexPlayer mp) {
    Log.d(TAG, "mAudioTrack.release() Done");
  }

  // this is called when video renderer is created.
  // HLS, SmoothStreaming cases, this is called several times.
  @Override
  public void onVideoRenderCreate(NexPlayer mp, int width, int height,
      Object rgbBuffer) {
    Log.d(TAG, "onVideoRenderCreate called ( Width:" + width + " Height : "
        + height + ")");

    Log.d(TAG, "VideoRender Created :" + mVideoWidth + " " + mVideoHeight + " ");
    _player.setOutputPos(0, 0, mVideoWidth, mVideoHeight);
  }

  // this is called when video renderer is deleted.
  @Override
  public void onVideoRenderDelete(NexPlayer mp) {
    Log.d(TAG, "onVideoRenderDelete called");
  }

  // this is called after app call captureVideo() API.
  // rgbBuffer is byte buffer. if pixelbyte is 2 data is RGB5565 and if
  // pixelbyte is 4 data is RGB 8888
  @Override
  public void onVideoRenderCapture(NexPlayer mp, int width, int height,
      int pixelbyte, Object rgbBuffer) {
    Log.d(TAG, "onVideoRenderCapture called");

  }

  // this is called during nexplayer draws video.
  // JAVA Renderer and OpenGL renderer uses this callback.
  @Override
  public void onVideoRenderRender(NexPlayer mp) {
    Log.d(TAG, "onVideoRenderRender called");
    if (_useOpenGL) {
      glRenderer.requestRender();
    }
  }

  // this is called when nexplayer has subtitle(caption)and text renderer is
  // created.
  @Override
  public void onTextRenderInit(NexPlayer mp, int classNum) {
    Log.d(TAG, "onTextRenderInit ClassNum : " + classNum);
  }

  // this is called during playing.
  @Override
  public void onTextRenderRender(NexPlayer mp, int classIndex,
      NexClosedCaption textInfo) {

  }

  // this is called after calling open(), start(),pause(), resume(), seek(),
  // stop() and close().
  // you need pair each api. you have to call close() after you call open().
  // you have to call start() after you call stop().
  // and app have to call with flow.
  @Override
  public void onAsyncCmdComplete(NexPlayer mp, int command, int result,
      int param1, int param2) {
    Log.d(TAG, "onAsyncCmdComplete playerID: " + mp + ", called " + command
        + " " + result);
    NexContentInformation info;
    info = _player.getContentInfo();

    Log.d(TAG, "------------------- CONTENTS INFORMATION -------------------");
    Log.d(TAG, "MEDIA TYPE        : " + info.mMediaType);
    Log.d(TAG, "MEDIA DURATION      : " + info.mMediaDuration);
    Log.d(TAG, "VIDEO CODEC       : " + info.mVideoCodec);
    Log.d(TAG, "VIDEO WIDTH       : " + info.mVideoWidth);
    Log.d(TAG, "VIDEO HEIGHT      : " + info.mVideoHeight);
    Log.d(TAG, "VIDEO FRAMERATE     : " + info.mVideoFrameRate);
    Log.d(TAG, "VIDEO BITRATE     : " + info.mVideoBitRate);
    Log.d(TAG, "AUDIO CODEC       : " + info.mAudioCodec);
    Log.d(TAG, "AUDIO SAMPLINGRATE    : " + info.mAudioSamplingRate);
    Log.d(TAG, "AUDIO NUMOFCHANNEL    : " + info.mAudioNumOfChannel);
    Log.d(TAG, "AUDIO BITRATE     : " + info.mAudioBitRate);
    Log.d(TAG, "MEDIA IS SEEKABLE   : " + info.mIsSeekable);
    Log.d(TAG, "MEDIA IS PAUSABLE   : " + info.mIsPausable);
    Log.d(TAG, "------------------------------------------------------------");

    switch (command) {
    case NexPlayer.NEXPLAYER_ASYNC_CMD_OPEN_LOCAL:
    case NexPlayer.NEXPLAYER_ASYNC_CMD_OPEN_STREAMING:
      _player.start(0);
    }
  }

  @Override
  public void onRTSPCommandTimeOut(NexPlayer mp) {
    if (mp.getState() == NexPlayer.NEXPLAYER_STATE_PLAY) {
      mp.stop();
    }
  }

  @Override
  public void onPauseSupervisionTimeOut(NexPlayer mp) {
    if (mp.getState() == NexPlayer.NEXPLAYER_STATE_PLAY) {
      mp.stop();
    }
  }

  @Override
  public void onDataInactivityTimeOut(NexPlayer mp) {
    if (mp.getState() == NexPlayer.NEXPLAYER_STATE_PLAY) {
      mp.stop();
    }
  }

  // this is called when nexplayer enters buffering state.
  @Override
  public void onBufferingBegin(NexPlayer mp) {
    Log.d(TAG, "Buffering begin");
  }

  // this is called after nexplayer comes out form buffering state.
  @Override
  public void onBufferingEnd(NexPlayer mp) {
    Log.d(TAG, "Buffering end");
  }

  // this is called periodically during buffering
  @Override
  public void onBuffering(NexPlayer mp, int progress_in_percent) {
    Log.d(TAG, "Buffering " + progress_in_percent + " %");
  }

  // nexPlayer have some change, this is called
  // for example, track change, DSI Change,...
  @Override
  public void onStatusReport(NexPlayer mp, int msg, int param1) {
    Log.d(TAG, "onStatusReport  msg:" + msg + "  param1:" + param1);
  }

  @Override
  public void onTimedMetaRenderRender(NexPlayer mp,
      NexID3TagInformation TimedMeta) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onDownloaderError(NexPlayer mp, int msg, int param1) {
    // TODO Auto-generated method stub
    Log.d(TAG, "onDownloaderError  msg:" + msg + "  param1:" + param1);

  }

  @Override
  public void onDownloaderAsyncCmdComplete(NexPlayer mp, int msg, int param1,
      int param2) {
    // TODO Auto-generated method stub
    Log.d(TAG, "onDownloaderCmdCmpt  msg:" + msg + "  param1:" + param1);

  }

  @Override
  public void onDownloaderEventBegin(NexPlayer mp, int param1, int param2) {
    // TODO Auto-generated method stub
    Log.d(TAG, "onDownloaderEventBegin  param1:" + param1 + "  param2:"
        + param2);

  }

  @Override
  public void onDownloaderEventProgress(NexPlayer mp, int param1, int param2,
      long param3, long param4) {
    // TODO Auto-generated method stub
    Log.d(TAG, "onDownloaderEventProgress  param1:" + param1 + "  param2:"
        + param2);

  }

  @Override
  public void onDownloaderEventComplete(NexPlayer mp, int param1) {
    // TODO Auto-generated method stub
    Log.d(TAG, "onDownloaderEventComplete  param1:" + param1);

  }

  @Override
  public void onDownloaderEventState(NexPlayer mp, int param1, int param2) {
    // TODO Auto-generated method stub
    Log.d(TAG, "onDownloaderEventState param1:" + param1 + "  param2:" + param2);

  }

  private int mVideoWidth = 0;
  private int mVideoHeight = 0;

  @Override
  public void onGLChangeSurfaceSize(int width, int height) {
    Log.d(TAG, "GLsurfaceChanged called width : " + width + "   height : "
        + height);

    mVideoWidth = width;
    mVideoHeight = height;
    int surfaceWidth = width;
    int surfaceHeight = height;

    int tx = 0;
    int ty = 0;
    int regwidth = 0;
    int regheight = 0;
    boolean isExistTextbox = false;

    int w = width;
    int h = height;
    int top = (surfaceHeight - h) / 2;
    int left = (surfaceWidth - w) / 2;

    Log.d(TAG, "GLSurface - FILLSCREEN : " + left + " " + top + " " + w + " "
        + h + " ");
    Log.d(TAG, "Surface Width : " + surfaceWidth + " SurfaceHeight : "
        + surfaceHeight);
    _player.setOutputPos(left, top, w, h);

    glRenderer.requestRender();

  }

  // JDKIM : end

}
