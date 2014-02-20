package com.ooyala.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.discretix.drmdlc.api.DxDrmDlc;
import com.discretix.drmdlc.api.DxLogConfig;
import com.discretix.drmdlc.api.IDxDrmDlc;
import com.discretix.drmdlc.api.exceptions.DrmClientInitFailureException;
import com.discretix.drmdlc.api.exceptions.DrmGeneralFailureException;
import com.discretix.drmdlc.api.exceptions.DrmInvalidFormatException;
import com.discretix.drmdlc.api.exceptions.DrmNotProtectedException;
import com.discretix.drmdlc.api.exceptions.DrmNotSupportedException;
import com.discretix.drmdlc.api.exceptions.DrmServerSoapErrorException;
import com.discretix.drmdlc.api.exceptions.DrmUpdateRequiredException;
import com.discretix.vodx.VODXPlayer;
import com.discretix.vodx.VODXPlayerImpl;
import com.ooyala.android.OoyalaPlayer.SeekStyle;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.visualon.AcquireRightsAsyncTask;
import com.ooyala.android.visualon.AcquireRightsCallback;
import com.ooyala.android.visualon.FileDownloadAsyncTask;
import com.ooyala.android.visualon.FileDownloadCallback;
import com.ooyala.android.visualon.PersonalizationAsyncTask;
import com.ooyala.android.visualon.PersonalizationCallback;
import com.ooyala.android.visualon.VisualOnUtils;
import com.visualon.OSMPBasePlayer.voOSBasePlayer;
import com.visualon.OSMPPlayer.VOCommonPlayerListener;
import com.visualon.OSMPPlayer.VOOSMPInitParam;
import com.visualon.OSMPPlayer.VOOSMPOpenParam;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_PLAYER_ENGINE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FLAG;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FORMAT;
import com.visualon.OSMPSubTitle.voSubTitleManager.voSubtitleDisplayInfo;
import com.visualon.OSMPSubTitle.voSubTitleManager.voSubtitleInfo;
import com.visualon.OSMPSubTitle.voSubTitleManager.voSubtitleInfoEntry;
import com.visualon.OSMPSubTitle.voSubTitleManager.voSubtitleTextRowInfo;
import com.visualon.OSMPUtils.voOSType;

/**
 * A wrapper around android.media.MediaPlayer
 * http://developer.android.com/reference/android/media/MediaPlayer.html
 *
 * For a list of Android supported media formats, see:
 * http://developer.android.com/guide/appendix/media-formats.html
 */
public class VisualOnStreamPlayer extends StreamPlayer implements
  VOCommonPlayerListener, voOSBasePlayer.onRequestListener, SurfaceHolder.Callback,
  FileDownloadCallback, PersonalizationCallback, AcquireRightsCallback{
  private static final String TAG = "VisualOnStreamPlayer";

  protected VODXPlayer _player = null;
  protected SurfaceHolder _holder = null;
  protected String _streamUrl = "";
  private String _localFilePath;
  protected int _videoWidth = 16;
  protected int _videoHeight = 9;

  private boolean _playQueued = false;
  private boolean _completedQueued = false;
  private int _timeBeforeSuspend = -1;
  private State _stateBeforeSuspend = State.INIT;
  protected Timer _playheadUpdateTimer = null;
  private int _lastPlayhead = -1;
  private boolean _isLiveClosedCaptionsAvailable = false;
  private boolean _isLiveClosedCaptionsEnabled = false;

  protected static final long TIMER_DELAY = 0;
  protected static final long TIMER_PERIOD = 250;

  /* Copy file from Assets directory to destination. Used for licenses and processor-specific configurations */
  private static void copyfile(Context context, String filename, String desName)
  {
    try {
      InputStream InputStreamis  = context.getAssets().open(filename);
      File desFile = new File(context.getFilesDir().getParentFile().getPath() + "/" + desName);
      desFile.createNewFile();
      FileOutputStream  fos = new FileOutputStream(desFile);
      int bytesRead;
      byte[] buf = new byte[4 * 1024]; //4K buffer
      while((bytesRead = InputStreamis.read(buf)) != -1) {
      fos.write(buf, 0, bytesRead);
      }
      fos.flush();
      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected class PlayheadUpdateTimerTask extends TimerTask {
    @Override
    public void run() {
      if (_player == null)
        return;

      if (_lastPlayhead != _player.getPosition()) {
        _playheadUpdateTimerHandler.sendEmptyMessage(0);
      }
      _lastPlayhead = (int) _player.getPosition();
    }
  }

  public SeekStyle getSeekStyle() {
    return SeekStyle.BASIC;
  }

  // This is required because android enjoys making things difficult. talk to
  // jigish if you got issues.
  private final Handler _playheadUpdateTimerHandler = new Handler(new Handler.Callback() {

    @Override
    public boolean handleMessage(Message msg) {
      setChanged();
      notifyObservers(OoyalaPlayer.TIME_CHANGED_NOTIFICATION);
      return false;
    }
  });

  @Override
  public void init(OoyalaPlayer parent, Set<Stream> streams) {
    Log.d(this.getClass().getName(), "Using VOPlayer");
    Stream stream = null;
    stream = Stream.bestStream(streams);

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
    _streamUrl = stream.decodedURL().toString();
    setParent(parent);

    // Copy license file,
    copyfile(_parent.getLayout().getContext(), "voVidDec.dat", "voVidDec.dat");
    copyfile(_parent.getLayout().getContext(), "cap.xml", "cap.xml");

    FileDownloadAsyncTask downloadTask = new FileDownloadAsyncTask(this, parent.getEmbedCode(), _streamUrl);
    downloadTask.execute();

    setupView();
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
      Log.v(TAG, "Play: still laoding, queued");
      break;
    case PAUSED:
    case READY:
    case COMPLETED:

      Log.v(TAG, "Play: ready - about to run");
      if (_timeBeforeSuspend >=0 ) {
        _player.setPosition(_timeBeforeSuspend);
        _timeBeforeSuspend = -1;
      }
      VO_OSMP_RETURN_CODE nRet = _player.start();
      if (nRet == VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
        Log.v(TAG, "MediaPlayer run.");
      } else {
        onError(_player, nRet, 0);
      }
      setState(State.PLAYING);
      startPlayheadTimer();
    break;
    case SUSPENDED:
      queuePlay();
      Log.v(TAG, "Play: Suspended already. re-queue" + _state);
      break;
    default:
      Log.v(TAG, "Play: invalid status?" + _state);
      break;
    }
  }

  @Override
  public void stop() {
    Log.v(TAG, "MediaPlayer stopped.");
    stopPlayheadTimer();
    _playQueued = false;
    _player.stop();
    _player.close();
  }

  @Override
  public void reset() {
    suspend(0, State.PAUSED);
    setupView();
    resume();
  }

  @Override
  public int currentTime() {
    if (_player == null) {
      return 0;
    }
    switch (_state) {
    case INIT:
    case LOADING:
    case SUSPENDED:
      return 0;
    default:
      break;
    }
    //Log.v(TAG, "currentTime: " + _player.GetPos());
    return (int) _player.getPosition();
  }

  @Override
  public int duration() {
    if (_player == null) {
      return 0;
    }
    switch (_state) {
    case INIT:
    case LOADING:
    case SUSPENDED:
      return 0;
    default:
      break;
    }
    //Log.v(TAG, "currentDuration: " + _player.GetDuration());
    return (int) _player.getDuration();
  }

  @Override
  public int buffer() {
    return this._buffer;
  }

  //Sets enablement of live CC, which is checked every time VisualOn recieves CC data on stream
  @Override
  public void setLiveClosedCaptionsEnabled(boolean enabled){
    _isLiveClosedCaptionsEnabled = enabled;
  }

  @Override
  public boolean isLiveClosedCaptionsAvailable() {
    return _isLiveClosedCaptionsAvailable;
  }

  @Override
  public void seekToTime(int timeInMillis) {
    if (_player == null) {
      return;
    }
    Log.d(TAG, "Seeking to "+timeInMillis);
    _player.setPosition(timeInMillis);
  }

  protected String downloadFile(String streamUrl) {
    String contentDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Ooyala_SecurePlayer";
    String localFile = String.format("%s/%s", contentDir, _parent.getEmbedCode());
    try {
      //Create content directory.
      if (new File(contentDir).mkdirs() == false){
        if (new File(contentDir).exists() == false){
          Log.e(TAG, "Cannot create content directory on SD-CARD");
        }
      }
      VisualOnUtils.DownloadFile(streamUrl, localFile);
      return localFile;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  protected boolean getPersonalization() {
    boolean success = true;
    DxLogConfig config = null;
    IDxDrmDlc dlc;

    String PERSONALIZATION_URL = "172.16.8.137:8000/Personalization";
    String SESSION_ID = "session";
    try {
      dlc = DxDrmDlc.getDxDrmDlc(_parent.getLayout().getContext(), config);

      dlc.getDebugInterface().setClientSideTestPersonalization(true);
      //Check for verification.
      if (!dlc.personalizationVerify()) {
        dlc.performPersonalization(OoyalaPlayer.getVersion(), PERSONALIZATION_URL, SESSION_ID);
      } else {
        Log.d(TAG, "Device is already personalized");
      }
    } catch (DrmGeneralFailureException e) {
      e.printStackTrace();
      success = false;
    } catch (DrmUpdateRequiredException e) {
      e.printStackTrace();
      success = false;
    } catch (DrmNotSupportedException e) {
      e.printStackTrace();
      success = false;
    } catch (DrmClientInitFailureException e) {
      e.printStackTrace();
      success = false;
    }
    return success;
  }

  protected boolean acquireRights(String localFilename) {
    boolean success = true;
    DxLogConfig config = null;
    IDxDrmDlc dlc;
    try {
      dlc = DxDrmDlc.getDxDrmDlc(_parent.getLayout().getContext(), config);
      String customData = "Unlimited";
      String customUrl = null;
      if(!dlc.verifyRights(localFilename)){
        dlc.acquireRights(localFilename, customData, customUrl);
        dlc.setCookies(null);
      }
    } catch (DrmClientInitFailureException e) {
      e.printStackTrace();
      success = false;
    } catch (IOException e) {
      e.printStackTrace();
      success = false;
    } catch (DrmGeneralFailureException e) {
      e.printStackTrace();
      success = false;
    } catch (DrmNotProtectedException e) {
      e.printStackTrace();
      success = false;
    } catch (DrmInvalidFormatException e) {
      e.printStackTrace();
      success = false;
    } catch (DrmServerSoapErrorException e) {
      e.printStackTrace();
      success = false;
    }

    return success;
  }

  protected boolean isStreamProtected(String streamUrl) {
    DxLogConfig config = null;
    IDxDrmDlc dlc;
    boolean isDrmContent = false;
    try {
      dlc = DxDrmDlc.getDxDrmDlc(_parent.getLayout().getContext(), config);
      isDrmContent = dlc.isDrmContent(streamUrl);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (DrmClientInitFailureException e) {
      e.printStackTrace();
    }
    return isDrmContent;
  }

  protected void createMediaPlayer() {
    try {

      if (_player == null) {
        _player = new VODXPlayerImpl();
      } else {

    	  Log.e(TAG, "DANGER DANGER: Creating a Media player when one already exists");
        _player.destroy();
        return;
      }

      // SDK player engine type
      VO_OSMP_PLAYER_ENGINE engine = VO_OSMP_PLAYER_ENGINE.VO_OSMP_VOME2_PLAYER;

      // Location of libraries
      String apkPath = _parent.getLayout().getContext().getFilesDir().getParentFile().getPath() + "/lib/";

      // Initialize SDK player
      VOOSMPInitParam initParam = new VOOSMPInitParam();
      initParam.setLibraryPath(apkPath);
      initParam.setContext(_parent.getLayout().getContext());
      VO_OSMP_RETURN_CODE nRet = _player.init(engine, initParam);
      if (nRet == VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
        Log.v(TAG, "MediaPlayer is created.");
      } else {
        onError(_player, nRet, 0);
        return;
      }

      DisplayMetrics dm  = new DisplayMetrics();
      WindowManager wm = (WindowManager) _view.getContext().getSystemService(Context.WINDOW_SERVICE);
      Display display = wm.getDefaultDisplay();
      display.getMetrics(dm);

      _player.setViewSize(dm.widthPixels, dm.heightPixels);
      _player.setView(_view);
      // Register SDK event listener
      _player.setOnEventListener(this);

      /* Configure DRM parameters */
      _player.setParameter(voOSType.VOOSMP_SRC_PID_DRM_FILE_NAME, "voDRM");
      _player.setParameter(voOSType.VOOSMP_SRC_PID_DRM_API_NAME, "voGetDRMAPI");

      /* Set the license */
      String licenseText = "VOTRUST_OOYALA_754321974";        // Magic string from VisualOn, must match voVidDec.dat to work
      _player.setParameter(voOSType.VOOSMP_PID_LICENSE_TEXT, licenseText);
      //Setup license content, or screen can green flicker.
      InputStream is = null;
      byte[] b = new byte[32*1024];
      try {
          is = _view.getContext().getAssets().open("voVidDec.dat");
          is.read(b);
          is.close();
      } catch (IOException e) {
          e.printStackTrace();
      }
      _player.setParameter(voOSType.VOOSMP_PID_LICENSE_CONTENT, b);

      /* Configure Dolby Audio Effect parameters */
      _player.setParameter(voOSType.VOOSMP_PID_AUDIO_EFFECT_ENABLE, 0);

      // Enable CC
      _player.setParameter(voOSType.VOOSMP_PID_CLOSED_CAPTION_OUTPUT, 1);

      /* Processor-specific settings */
        String cfgPath = _parent.getLayout().getContext().getFilesDir().getParentFile().getPath() + "/";
        String capFile = cfgPath + "cap.xml";
        _player.setParameter(voOSType.VOOSMP_SRC_PID_CAP_TABLE_PATH, capFile);

        //Open then run the player
        VOOSMPOpenParam openParam = new VOOSMPOpenParam();
        nRet = _player.open(_streamUrl, VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_ASYNC, VO_OSMP_SRC_FORMAT.VO_OSMP_SRC_AUTO_DETECT, openParam);
        if (nRet == VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
            Log.v(TAG, "MediaPlayer is Opened.");
          } else {

            Toast.makeText(_parent.getLayout().getContext(), "Could not connect to " + _streamUrl + "!", Toast.LENGTH_LONG).show();
            //onError(_player, nRet, 0);
            return;
          }

    } catch (Throwable t) {
      t.printStackTrace(); }
  }

  public boolean onError(VODXPlayer mp, VO_OSMP_RETURN_CODE what, int extra) {
    this._error = "voOSPBasePlayer Error: " + what + " " + extra;
    setState(State.ERROR);
    return false;
  }

  @Override
  public void surfaceChanged(SurfaceHolder arg0, int arg1, int width, int height) {
    Log.v(TAG, "Surface Changed: " + width + ","+ height);

    if (_view != null) {
      _view.setLayoutParams(new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
    }
    if (_player != null) {
    	_player.setParameter(voOSType.VOOSMP_PID_SURFACE_CHANGED, 1);
    }

  }

  boolean surfaceExists = false;
  @Override
  public void surfaceCreated(SurfaceHolder arg0) {
    Log.i(TAG, "Surface Created");
    surfaceExists = true;
    if (_player !=null)
    {
      // If SDK player already exists, show media controls
      _player.setParameter(voOSType.VOOSMP_PID_VIEW_ACTIVE, _view);
      return;
    }

    if(canFileBePlayed(_localFilePath)) {
      createMediaPlayer();
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder arg0) {
    Log.i(TAG, "Surface Destroyed");
    surfaceExists = false;
    if (_player != null) {
      _player.stop();
      _player.setView(null);
    }
  }

  @Override
  public void setParent(OoyalaPlayer parent) {
    super.setParent(parent);
  }

  @SuppressWarnings("deprecation")
  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private void setupView() {
    if (_view != null) {
      Log.e(TAG, "DANGER DANGER: setupView while we still have a view");
      return;
    }

    _view = new SurfaceView(_parent.getLayout().getContext()) {

      @Override
      protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    		Log.v(TAG, "MEASURE SPEC: " + MeasureSpec.toString(widthMeasureSpec) + "," + MeasureSpec.toString(heightMeasureSpec));

    		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
    		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);


    		Log.v(TAG, "MEASURE PARENT: " + _parent.getLayout().getMeasuredWidth() + "," + _parent.getLayout().getMeasuredHeight());

     	// assume to much vertical space, so need to align vertically
  			int wantedWidth = parentWidth;
  			int wantedHeight = wantedWidth * _videoHeight / _videoWidth;
  			int offset = (parentHeight - wantedHeight) / 2;

  			if(offset < 0) {
  				// oops, too much width, let's align horizontally
  				wantedHeight = parentHeight;
  				wantedWidth = parentHeight * _videoWidth / _videoHeight;
  				offset = (parentWidth - wantedWidth) / 2;
  			}

        setMeasuredDimension(wantedWidth, wantedHeight);
        Log.v(TAG, "MEASURED: " + wantedWidth + "," + wantedHeight);
      }
    };


    _view.setLayoutParams(new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));

    _parent.getLayout().addView(_view);

    _holder = _view.getHolder();
    _holder.addCallback(this);
    _holder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
    _holder.setFormat(PixelFormat.RGBA_8888);
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
    suspend(_player != null ? (int)_player.getPosition() : 0, _state);
  }

  @Override
  public void suspend(int millisToResume, State stateToResume) {
    Log.v(TAG, "Player Suspend");
    if (_state == State.SUSPENDED) {
      return;
    }
    if (_player != null) {
      _timeBeforeSuspend = millisToResume;
      _stateBeforeSuspend = stateToResume;
      stop();
      _player.setView(null);
      _player.destroy();
      _player = null;
    }
    removeView();
    _buffer = 0;
    _playQueued = false;
    setState(State.SUSPENDED);
  }

  @Override
  public void resume(int millisToResume, State stateToResume) {
    _timeBeforeSuspend = millisToResume;
    _stateBeforeSuspend = stateToResume;

    Log.v(TAG, "Player Resume");

    tryToAcquireRights();
    if (_stateBeforeSuspend == State.PLAYING || _stateBeforeSuspend == State.LOADING) {
      play();
    } else if (_stateBeforeSuspend == State.COMPLETED) {
      queueCompleted();
    }
  }

  public void resume() {
    resume(_timeBeforeSuspend, _stateBeforeSuspend);
  }

  @Override
  public void destroy() {
    if (_player != null) {
      stop();
      _player.destroy();
      _player = null;
    }
    removeView();
    _buffer = 0;
    _playQueued = false;
    _timeBeforeSuspend = -1;
    _state = State.INIT;
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
    Log.v(TAG, "Set State: " + state.name());
    super.setState(state);
    dequeueAll();
  }

  // Timer tasks for playhead updates
  protected void startPlayheadTimer() {
    if (_playheadUpdateTimer != null) {
      stopPlayheadTimer();
    }
    _playheadUpdateTimer = new Timer();
    _playheadUpdateTimer.scheduleAtFixedRate(new PlayheadUpdateTimerTask(),
        TIMER_DELAY, TIMER_PERIOD);
  }

  protected void stopPlayheadTimer() {
    if (_playheadUpdateTimer != null) {
      _playheadUpdateTimer.cancel();
      _playheadUpdateTimer = null;
    }
  }

  @Override
  public int onRequest(int arg0, int arg1, int arg2, Object arg3) {
    Log.i(TAG, "onRequest arg0 is %d" + arg0);
    return 0;
  }

  @Override
  public VO_OSMP_RETURN_CODE onVOEvent(VO_OSMP_CB_EVENT_ID id, int param1, int param2, Object obj) {

    switch (id) {
    case VO_OSMP_SRC_CB_OPEN_FINISHED:
      // After createMediaPlayer is complete, mark as ready
      setState(State.READY);
      break;

    case VO_OSMP_CB_PLAY_COMPLETE:
      currentItemCompleted();
      break;

    case VO_OSMP_CB_SEEK_COMPLETE:
      dequeuePlay();
      break;

    case VO_OSMP_CB_SRC_BUFFER_TIME:
      this._buffer = param1;
      break;

    case VO_OSMP_CB_VIDEO_SIZE_CHANGED:
      _videoWidth = param1;
      _videoHeight = param2;
      Log.v(TAG, "onEvent: Video Size Changed, " + _videoWidth + ", " + _videoHeight);

      _view.setLayoutParams(new FrameLayout.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
      break;

    case VO_OSMP_CB_VIDEO_STOP_BUFFER:
      Log.d(TAG, "onEvent: Buffering Done! " + param1 + ", " + param2);
      break;

    case VO_OSMP_CB_VIDEO_START_BUFFER:
      Log.d(TAG, "onEvent: Buffering Starting " + param1 + ", " + param2);
      break;

    case VO_OSMP_CB_ERROR:
      Log.e(TAG, "onEvent: Error. " + param1);
      onError(_player, null, id.getValue());
      break;

    case VO_OSMP_SRC_CB_CONNECTION_FAIL:
    case VO_OSMP_SRC_CB_DOWNLOAD_FAIL:
    case VO_OSMP_SRC_CB_DRM_FAIL:
    case VO_OSMP_SRC_CB_DRM_NOT_SECURE:
    case VO_OSMP_SRC_CB_CONNECTION_REJECTED:
    case VO_OSMP_SRC_CB_PLAYLIST_PARSE_ERR:
    case VO_OSMP_SRC_CB_DRM_AV_OUT_FAIL:
      // Display error dialog and stop player
      onError(_player, null, id.getValue());
      break;

    case VO_OSMP_CB_LANGUAGE_INFO_AVAILABLE:
      // Remember if we have received live closed captions at some point during playback
      // NOTE: Some reason we might receive false alarm for Closed Captions check if it's empty here
      voSubtitleInfo info = (voSubtitleInfo)obj;
      String cc = GetCCString(info);
      if (!cc.equals("")) {
        _isLiveClosedCaptionsAvailable = true;
      }

      //Show closed captions if someone enabled them
      if (_isLiveClosedCaptionsEnabled) {
        _parent.displayClosedCaptionText(cc);
      }

      break;
    case VO_OSMP_SRC_CB_ADAPTIVE_STREAMING_INFO:
      switch (param1) {
      case voOSType.VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_BITRATE_CHANGE: {
        Log.v(TAG, "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_BITRATE_CHANGE, param2 is %d . " + param2);
        break;
      }
      case voOSType.VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE: {
        Log.v(TAG, "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, param2 is %d . " + param2);

        switch (param2) {
        case voOSType.VOOSMP_AVAILABLE_PUREAUDIO: {
          Log.v(TAG, "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_PUREAUDIO");
          break;
        }
        case voOSType.VOOSMP_AVAILABLE_PUREVIDEO: {
          Log.v(TAG, "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_PUREVIDEO");
          break;
        }
        case voOSType.VOOSMP_AVAILABLE_AUDIOVIDEO: {
          Log.v(TAG, "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_AUDIOVIDEO");
          break;
        }
        }
        break;
      }
      }
      //Return now to avoid constant messages
      return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;

    default:
      break;
    }
    Log.v(TAG, "VisualOn Message: " + id + ". param is " + param1 + ", " + param2);
    return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;

  }

	/* Extract text string from CC data. Does not handle position, color, font type, etc. */
	public String GetCCString(voSubtitleInfo subtitleInfo)
	{
		if(subtitleInfo == null)
			return "";
		if(subtitleInfo.getSubtitleEntry() == null)
			return "";

		String strTextAll = "";
		for(int i = 0; i<subtitleInfo.getSubtitleEntry().size(); i++)
		{
			// Retrieve the display info for each subtitle entry
			voSubtitleInfoEntry info = subtitleInfo.getSubtitleEntry().get(i);
			voSubtitleDisplayInfo dispInfo = info.getSubtitleDispInfo();
			if(dispInfo.getTextRowInfo() != null)
			{
				for(int j = 0; j < dispInfo.getTextRowInfo().size() ; j++)
				{
					// Retrieve the row info for display
					voSubtitleTextRowInfo rowInfo = dispInfo.getTextRowInfo().get(j);
					if( rowInfo == null)
						continue;
					if( rowInfo.getTextInfoEntry() == null)
						continue;

					String strRow = "";
					for(int k = 0; k < rowInfo.getTextInfoEntry().size() ; k++)
					{
						// Get the string for each row
						strRow+=rowInfo.getTextInfoEntry().get(k).getStringText();//.stringText;
					}
					if(strRow.length()>0)
					{
						if(strTextAll.length()>0)
							strTextAll+="\n";
						strTextAll+=strRow;

					}

				}
			}
		}
		return strTextAll;
	}


  @Override
  public VO_OSMP_RETURN_CODE onVOSyncEvent(VO_OSMP_CB_SYNC_EVENT_ID arg0,
      int arg1, int arg2, Object arg3) {
    return null;
  }
/**
 * After file download on init(), check the file for DRM.
 * If DRM'ed, move on to personalization -> acquireRights.  Otherwise, continue video playback
 */
  @Override
  public void afterFileDownload(String localFilename) {
    _localFilePath = localFilename;
    if (_localFilePath == null) {
      Log.e(TAG, "File Download failed!");
    }
    else {
      if (isStreamProtected(_localFilePath)) {
        Log.d(TAG, "File Download Succeeded: Need to acquire rights!");
        PersonalizationAsyncTask personalizationTask = new PersonalizationAsyncTask(this, _parent.getLayout().getContext());
        personalizationTask.execute();
      }
      else {
        Log.d(TAG, "File Download Succeeded: No rights needed");
        if (surfaceExists && _player == null) {
          createMediaPlayer();
       }
      }
    }
  }

  /**
   * After we personalize, we can try to acquire rights
   */
  @Override
  public void afterPersonalization(boolean success) {
    if (!isDevicePersonalized()) {
      Log.e(TAG, "Personalization failed!");
    }
    else {
      Log.d(TAG, "Personalization successful!");
      tryToAcquireRights();
    }
  }

  /**
   * After we acquire rights, we can begin video playback, as long as the surface was created already
   */
  @Override
  public void afterAcquireRights(boolean success) {
    if (!success) {
      Log.e(TAG, "Acquire Rights failed!");
    }
    else {
      Log.d(TAG, "Acquire Rights successful!");
      if (surfaceExists && _player == null) {
         createMediaPlayer();
      }
    }
  }

  /**
   * Ensure that we have enough information to acquire rights (personalization, file download)
   * then try to acquire rights.
   */
  public void tryToAcquireRights() {
    boolean isdevicePersonalized = isDevicePersonalized();
    if(!isdevicePersonalized || _localFilePath == null) {
      Log.i(TAG, "Acquire Rights not available yet: Personalization = " + isdevicePersonalized + ", localFilePath = " + _localFilePath);
    }
    else {
      Log.d(TAG, "Acquiring rights");
      AcquireRightsAsyncTask acquireRightsTask = new AcquireRightsAsyncTask(this, _parent.getLayout().getContext(), _localFilePath);
      acquireRightsTask.execute();
    }
  }

  /**
   * Checks if the device has been personalized
   * @return true if personalized, false if not
   */
  public boolean isDevicePersonalized() {
    DxLogConfig config = null;
    IDxDrmDlc dlc;
    try {
      dlc = DxDrmDlc.getDxDrmDlc(_parent.getLayout().getContext(), config);
      return dlc.personalizationVerify();
    } catch (DrmClientInitFailureException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Checks if the file is DRM enabled, and if it is, if the file's DRM rights are valid.
   * @param localFilename file path to locally downloaded file
   * @return true if file can now be played, false otherwise.
   */
  public boolean canFileBePlayed(String localFilename){
    if (localFilename == null) return false;
    if (!isStreamProtected(localFilename)) return true;

    DxLogConfig config = null;
    IDxDrmDlc dlc;
    boolean areRightsVerified = false;
    try {
      dlc = DxDrmDlc.getDxDrmDlc(_parent.getLayout().getContext(), config);
      areRightsVerified = dlc.verifyRights(localFilename);

    } catch (DrmClientInitFailureException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (DrmGeneralFailureException e) {
      e.printStackTrace();
    } catch (DrmInvalidFormatException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    }
    return areRightsVerified;
  }

}
