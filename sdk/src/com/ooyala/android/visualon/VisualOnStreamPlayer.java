package com.ooyala.android.visualon;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.ooyala.android.DebugMode;
import com.ooyala.android.OoyalaException;
import com.ooyala.android.OoyalaException.OoyalaErrorCode;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.SeekStyle;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.configuration.VisualOnConfiguration;
import com.ooyala.android.item.Stream;
import com.ooyala.android.player.StreamPlayer;
import com.visualon.OSMPPlayer.VOCommonPlayer;
import com.visualon.OSMPPlayer.VOCommonPlayerAssetSelection;
import com.visualon.OSMPPlayer.VOCommonPlayerAssetSelection.VOOSMPAssetProperty;
import com.visualon.OSMPPlayer.VOCommonPlayerListener;
import com.visualon.OSMPPlayer.VOOSMPInitParam;
import com.visualon.OSMPPlayer.VOOSMPOpenParam;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_MODULE_TYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_PLAYER_ENGINE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FLAG;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FORMAT;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_STATUS;
import com.visualon.OSMPPlayerImpl.VOCommonPlayerImpl;
import com.visualon.OSMPSubTitle.voSubTitleManager.voSubtitleDisplayInfo;
import com.visualon.OSMPSubTitle.voSubTitleManager.voSubtitleInfo;
import com.visualon.OSMPSubTitle.voSubTitleManager.voSubtitleInfoEntry;
import com.visualon.OSMPSubTitle.voSubTitleManager.voSubtitleTextRowInfo;
import com.visualon.OSMPUtils.voOSType;

/**
 * A StreamPlayer which wraps around VisualOn's OSMP Player
 * Provides consistent HLS and Smooth/Playready playback
 */
public class VisualOnStreamPlayer extends StreamPlayer implements
VOCommonPlayerListener, SurfaceHolder.Callback,
FileDownloadCallback, PersonalizationCallback, AcquireRightsCallback{
  private static boolean didCleanupLocalFiles = false;
  private static final String TAG = "VisualOnStreamPlayer";
  private static final String DISCREDIX_MANAGER_CLASS = "com.discretix.drmdlc.api.DxDrmDlc";
  private static final String EXPECTED_VISUALON_VERSION = "3.13.0-B71738";
  private static final String EXPECTED_SECUREPLAYER_VO_VERSION = "3.13.10-B72949";
  private VisualOnConfiguration _visualOnConfiguration = null;

  protected VOCommonPlayer _player = null;
  protected SurfaceHolder _holder = null;
  protected String _streamUrl = "";
  private String _localFilePath;
  protected int _videoWidth = 16;
  protected int _videoHeight = 9;
  boolean _surfaceExists = false;
  Stream _stream = null;

  private boolean _playQueued = false;
  private boolean _completedQueued = false;
  private Integer _timeBeforeSuspend = null;
  private State _stateBeforeSuspend = State.INIT;
  protected Timer _playheadUpdateTimer = null;
  private int _lastPlayhead = -1;
  private boolean _isLiveClosedCaptionsAvailable = false;
  private boolean _isLiveClosedCaptionsEnabled = false;
  private int _selectedSubtitleIndex = 0;
  private List<String> _subtitleDescriptions = null;

  protected static final long TIMER_DELAY = 0;
  protected static final long TIMER_PERIOD = 1000;

  protected boolean _hasDiscredix = false;

  private boolean isDiscredixLoaded() {
    return _hasDiscredix;
  }

  private boolean isDiscredixNeeded() {
    return "smooth".equals(_stream.getDeliveryType());
  }

  @Override
  public void init(OoyalaPlayer parent, Set<Stream> streams) {
    DebugMode.logD(TAG, "Using VOPlayer");
    _stream = Stream.bestStream(streams);

    if (_stream == null) {
      DebugMode.logE(TAG, "ERROR: Invalid Stream (no valid stream available)");
      this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Invalid Stream");
      setState(State.ERROR);
      return;
    }

    if (parent == null) {
      DebugMode.logE(TAG, "ERROR: Invalid parent (no parent provided to Stream Player)");
      this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Invalid Parent");
      setState(State.ERROR);
      return;
    }

    _visualOnConfiguration = parent.getOptions().getVisualOnConfiguration();

    if (!isDiscredixNeeded()) {
      DebugMode.logD(TAG, "This asset doesn't need Discredix");
    }

    try {
      getClass().getClassLoader().loadClass(DISCREDIX_MANAGER_CLASS);
      DebugMode.logD(TAG, "This app has the ability to play protected content");
      _hasDiscredix = true;
    } catch(Exception e) {
      DebugMode.logD(TAG, "This app cannot play protected content");
      _hasDiscredix = false;
    }

    setState(State.LOADING);
    _streamUrl = _stream.decodedURL().toString();
    _subtitleDescriptions = new ArrayList<String>();
    setParent(parent);

    // Copy license file,
    VisualOnUtils.copyFile(_parent.getLayout().getContext(), "voVidDec.dat", "voVidDec.dat");
    VisualOnUtils.copyFile(_parent.getLayout().getContext(), "cap.xml", "cap.xml");

    // Do a cleanup of all saved manifests first open of the app
    if (!didCleanupLocalFiles) {
      didCleanupLocalFiles = true;
      VisualOnUtils.cleanupLocalFiles(_parent.getLayout().getContext());
    }

    if(isDiscredixNeeded() && isDiscredixLoaded() && _localFilePath == null) {

      // Check if the Discredix version string matches what we expect
      if (!DiscredixDrmUtils.isDiscredixVersionCorrect(_parent.getLayout().getContext())) {
        if (!_visualOnConfiguration.disableLibraryVersionChecks) {
          this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "SecurePlayer Initialization error: Unexpected Discredix Version");
          setState(State.ERROR);
          return;
        }
        else {
          DebugMode.logE(TAG, "Disabled Library version checks. Attempting to continue playback");
        }
      }
      else {
        DebugMode.logI(TAG, "Discredix Version correct for this SDK version");
      }

      FileDownloadAsyncTask downloadTask = new FileDownloadAsyncTask(_parent.getLayout().getContext(), this, parent.getEmbedCode(), _streamUrl);
      downloadTask.execute();
    }
    if(isDiscredixNeeded() && !isDiscredixLoaded()) {
      this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Trying to play protected content without DRM-capable Libraries");
      setState(State.ERROR);
      return;
    }
    setupView();
  }

  @Override
  public void play() {
    _playQueued = false;
    switch (getState()) {
    case INIT:
    case LOADING:
      queuePlay();
      DebugMode.logV(TAG, "Play: still loading, queued");
      break;
    case PAUSED:
    case READY:
    case COMPLETED:
      DebugMode.logV(TAG, "Play: ready - about to start");
      if (_timeBeforeSuspend == null) {
        if (_stream.isLiveStream()) {
          _timeBeforeSuspend = 1;
        } else {
          _timeBeforeSuspend = -1;
        }
      } else if (_timeBeforeSuspend >= 0  && !_stream.isLiveStream()) {
        seekToTime(_timeBeforeSuspend);
        _timeBeforeSuspend = -1;
      } else if (_timeBeforeSuspend <= 0 && _stream.isLiveStream()) {
        // current item is a Live stream, which can only seek to where playhead is less or equals to zero
        seekToTime(_timeBeforeSuspend);
        _timeBeforeSuspend = 1;
      }

      VO_OSMP_RETURN_CODE nRet = _player.start();
      if (nRet == VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
        DebugMode.logV(TAG, "MediaPlayer started.");
        setState(State.PLAYING);
        startPlayheadTimer();
      } else {
        onError(_player, nRet, 0);
      }
      break;
    case SUSPENDED:
      queuePlay();
      DebugMode.logD(TAG, "Play: Suspended already. re-queue: " + getState());
      break;
    default:
      DebugMode.logD(TAG, "Play: invalid status? " + getState());
      break;
    }
  }

  @Override
  public void pause() {
    _playQueued = false;
    switch (getState()) {
    case PLAYING:
      stopPlayheadTimer();
      _player.pause();
      setState(State.PAUSED);
    default:
      break;
    }
  }

  @Override
  public void stop() {
    DebugMode.logV(TAG, "MediaPlayer stopped.");
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
    switch (getState()) {
    case INIT:
    case SUSPENDED:
      return 0;
    case LOADING:
    default:
      break;
    }
    return (int) _player.getPosition();
  }

  @Override
  public int duration() {
    if (_player == null) {
      return 0;
    }
    switch (getState()) {
    case INIT:
    case SUSPENDED:
      return 0;
    case LOADING:
    default:
      break;
    }
    return (int) _player.getDuration();
  }

  @Override
  public int buffer() {
    return this._buffer;
  }

  //Sets enablement of live CC, which is checked every time VisualOn receives CC data on stream
  @Override
  public void setLiveClosedCaptionsEnabled(boolean enabled) {
    _isLiveClosedCaptionsEnabled = enabled;
    applySubtitleSettings();
  }

  @Override
  public boolean isLiveClosedCaptionsAvailable() {
    return _isLiveClosedCaptionsAvailable;
  }

  @Override
  public SeekStyle getSeekStyle() {
    return SeekStyle.BASIC;
  }

  @Override
  public void setParent(OoyalaPlayer parent) {
    super.setParent(parent);
  }

  @Override
  public void seekToTime(int timeInMillis) {
    if (_player == null) {
      return;
    }
    DebugMode.logD(TAG, "Seeking to " + timeInMillis);
    if (_player.setPosition(timeInMillis) < 0) {
      DebugMode.logE(TAG, "setPosition failed.");
    }
//    TODO: setting this will cause initialTime to fail.  For some reason initialTime is saved in two places, and this causes the issue to manifest
//    setState(State.LOADING);
  }

  @Override
  public void seekToPercentLive(int percent) {
    int max = (int)_player.getMaxPosition();
    int min = (int)_player.getMinPosition();
    int duration = max - min;
    int newPosition = duration * percent / 100 + min;
    if (_player.setPosition(newPosition) < 0) {
      DebugMode.logE(TAG, "setPosition failed.");
    }
  }

  @Override
  public int livePlayheadPercentage() {
    if (_player != null) {
      long max = _player.getMaxPosition();
      long min = _player.getMinPosition();
      long cur = _player.getPosition();

      float fPercent = ((cur - min) / ((float) max - min)) * (100f);
      DebugMode.logD(TAG, "Inside LivePlayheadPercentage = " + fPercent);
      return (int)fPercent;
    }
    return 100;
  }

  protected void createMediaPlayer() {
    try {
      if (!_surfaceExists) {
        DebugMode.logE(TAG, "Trying to create a player without a valid surface");
        return;
      }

      if (isDiscredixNeeded() && isDiscredixLoaded() &&
          !DiscredixDrmUtils.canFileBePlayed(_parent.getLayout().getContext(), _stream, _localFilePath)) {
        DebugMode.logE(TAG, "File cannot be played yet, we haven't gotten rights yet");
        return;
      }

      DebugMode.logD(TAG, "File can be played, surface created. Creating media player");

      if (_player != null) {
        DebugMode.logE(TAG, "DANGER: Creating a Media player when one already exists");
      }
      else if (isDiscredixNeeded() && isDiscredixLoaded()) {
        _player = DiscredixDrmUtils.getVODXPlayerImpl();
      }
      else {
        _player = new VOCommonPlayerImpl();
      }

      // SDK player engine type
      VO_OSMP_PLAYER_ENGINE engine = VO_OSMP_PLAYER_ENGINE.VO_OSMP_VOME2_PLAYER;

      // Location of libraries
      String apkPath = _parent.getLayout().getContext().getFilesDir().getParentFile().getPath() + "/lib/";

      //This needs to be called at least once in order to initialize the video player
      if (isDiscredixLoaded()) {
        DiscredixDrmUtils.warmDxDrmDlc(_parent.getLayout().getContext());
      }

      // Initialize SDK player
      VOOSMPInitParam initParam = new VOOSMPInitParam();
      initParam.setLibraryPath(apkPath);
      initParam.setContext(_parent.getLayout().getContext());
      VO_OSMP_RETURN_CODE nRet = _player.init(engine, initParam);
      if (nRet == VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
        DebugMode.logV(TAG, "MediaPlayer is created.");
      } else {
        onError(_player, nRet, 0);
        return;
      }

      String visualOnVersion = _player.getVersion(VO_OSMP_MODULE_TYPE.VO_OSMP_MODULE_TYPE_SDK);
      DebugMode.logI(TAG, "VisualOn Version: " + visualOnVersion);

      String expectedVersion = isDiscredixLoaded() ? EXPECTED_SECUREPLAYER_VO_VERSION : EXPECTED_VISUALON_VERSION;
      String libraryUsed = isDiscredixLoaded() ? "SecurePlayer" : "VisualOn";
      if (expectedVersion.compareTo(visualOnVersion) != 0) {
        DebugMode.logE(TAG, libraryUsed + " Version was not expected! Expected: " + expectedVersion + ", Actual: " + visualOnVersion);
        DebugMode.logE(TAG, "Please ask your CSM for updated versions of the " + libraryUsed + " libraries");

        if (!_visualOnConfiguration.disableLibraryVersionChecks) {
          // Errors here cannot be run on async thread - This is run in SurfaceCreated, and erroring will nullpointer exception SurfaceChanged
          Handler mainHandler = new Handler(Looper.getMainLooper());
          Runnable runner =  new Runnable() {
            @Override
            public void run() {
              setState(State.ERROR);
            }

          };
          this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, libraryUsed + " Initialization error: Unexpected VisualOn Player Version");
          mainHandler.post(runner);
          return;
        }
        else {
          DebugMode.logE(TAG, "Disabled Library version checks. Attempting to continue playback");
        }
      }
      else {
        DebugMode.logI(TAG, libraryUsed + " libraries version correct for this SDK version");
      }


      DisplayMetrics dm  = new DisplayMetrics();
      WindowManager wm = (WindowManager) _view.getContext().getSystemService(Context.WINDOW_SERVICE);
      Display display = wm.getDefaultDisplay();
      display.getMetrics(dm);

      _player.setViewSize(Math.max(dm.widthPixels, dm.heightPixels), Math.max(dm.widthPixels, dm.heightPixels));
      _player.setView(_view);

      // Register SDK event listener
      _player.setOnEventListener(this);

      // If we are using VisualON OSMP player without Discredix, enable eHLS playback
      // eHLS playback will not work using the SecurePlayer
      if (!isDiscredixLoaded()) {
        _player.setDRMLibrary("voDRM", "voGetDRMAPI");
      }
      /* Set the license */
      String licenseText = "VOTRUST_OOYALA_754321974";        // Magic string from VisualOn, must match voVidDec.dat to work
      _player.setPreAgreedLicense(licenseText);
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
      _player.setLicenseContent(b);

      /* Configure Dolby Audio Effect parameters */
      _player.enableAudioEffect(false);

      /* Processor-specific settings */
      String cfgPath = _parent.getLayout().getContext().getFilesDir().getParentFile().getPath() + "/";
      String capFile = cfgPath + "cap.xml";
      _player.setDeviceCapabilityByFile(capFile);

      //Open then run the player
      VOOSMPOpenParam openParam = new VOOSMPOpenParam();
      nRet = _player.open(_streamUrl, VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_ASYNC, VO_OSMP_SRC_FORMAT.VO_OSMP_SRC_AUTO_DETECT, openParam);
      if (nRet == VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
        DebugMode.logV(TAG, "MediaPlayer is Opened.");
      } else {
      	DebugMode.logE(TAG, "Could not open VisualOn Player");
      	onError(_player, nRet, 0);
        return;
      }

    } catch (Throwable t) {
      t.printStackTrace(); }
  }

  public boolean onError(VOCommonPlayer mp, VO_OSMP_RETURN_CODE what, int extra) {
    this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "VisualOn Playback Error: " + what + " " + extra);
    setState(State.ERROR);
    return false;
  }

  @Override
  public void surfaceChanged(SurfaceHolder arg0, int arg1, int width, int height) {
    DebugMode.logV(TAG, "Surface Changed: " + width + ","+ height);
    if (_player != null) {
      _player.setSurfaceChangeFinished();
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder arg0) {
    DebugMode.logI(TAG, "Surface Created");
    _surfaceExists = true;
    if (_player !=null)
    {
      // If SDK player already exists, show media controls
      _player.resume(_view);
      return;
    }
    createMediaPlayer();
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder arg0) {
    DebugMode.logI(TAG, "Surface Destroyed");
    _surfaceExists = false;
    if (_player != null) {
      _player.stop();
      _player.setView(null);
    }
  }

  private void setupView() {
    if (_view != null) {
      DebugMode.logE(TAG, "DANGER DANGER: setupView while we still have a view");
      return;
    }

    _view = new SurfaceView(_parent.getLayout().getContext()) {
      @Override
      protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DebugMode.logV(TAG, "Remeasuring Surface: " + MeasureSpec.toString(widthMeasureSpec) + "," + MeasureSpec.toString(heightMeasureSpec));

        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        // assume to much vertical space, so we need to align vertically
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
        DebugMode.logV(TAG, "Surface remeasured to: " + wantedWidth + "," + wantedHeight);
      }
    };

    _parent.addVideoView( _view );
    _view.setBackgroundColor(Color.TRANSPARENT);

    _holder = _view.getHolder();
    _holder.addCallback(this);
    _holder.setFormat(PixelFormat.RGBA_8888);
  }

  private void removeView() {
    _parent.removeVideoView();
    if (_holder != null) {
      _holder.removeCallback(this);
    }
    _view = null;
    _holder = null;
  }

  @Override
  public void suspend() {
    suspend(_player != null ? (int) _player.getPosition() : 0, getState());
  }

  private void suspend(int millisToResume, State stateToResume) {
    DebugMode.logV(TAG, "Player Suspend");
    if (getState() == State.SUSPENDED) {
      return;
    }
    if (_player != null) {
      _timeBeforeSuspend = millisToResume;
      _stateBeforeSuspend = stateToResume;
      destroyBasePlayer();
    }

    removeView();
    _buffer = 0;
    _playQueued = false;
    setState(State.SUSPENDED);
  }

  @Override
  public void resume() {
    resume(_timeBeforeSuspend, _stateBeforeSuspend);
  }

  @Override
  public void resume(int millisToResume, State stateToResume) {
    _timeBeforeSuspend = millisToResume;
    _stateBeforeSuspend = stateToResume;

    DebugMode.logV(TAG, "Player Resume");

    if (isDiscredixNeeded() && isDiscredixLoaded() &&
        DiscredixDrmUtils.isStreamProtected(_parent.getLayout().getContext(), _localFilePath) &&
        !DiscredixDrmUtils.canFileBePlayed(_parent.getLayout().getContext(), _stream, _localFilePath)) {
      tryToAcquireRights();
    }

    if (_stateBeforeSuspend == State.PLAYING || _stateBeforeSuspend == State.LOADING) {
      play();
    } else if (_stateBeforeSuspend == State.COMPLETED) {
      queueCompleted();
    }
  }

  public void destroyBasePlayer() {
    if (_player != null) {
      stop();
      _player.destroy();
      _player.setView(null);
      _player = null;
    }
  }

  @Override
  public void destroy() {
    destroyBasePlayer();
    removeView();
    _buffer = 0;
    _playQueued = false;
    _timeBeforeSuspend = -1;
    setState(State.INIT);
  }

  private void currentItemCompleted() {
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
      switch (getState()) {
      case PAUSED:
      case READY:
      case COMPLETED:
        _playQueued = false;
        play();
        break;
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
    DebugMode.logV(TAG, "Set State: " + state.name());
    super.setState(state);
    dequeueAll();
  }

  /* Playhead Timer Updating methods */
  protected class PlayheadUpdateTimerTask extends TimerTask {
    @Override
    public void run() {
      synchronized (_player) {
        if (_player == null) {
          return;
        }
        try {
          if (_lastPlayhead != _player.getPosition()) {
            _playheadUpdateTimerHandler.sendEmptyMessage(0);
          }
          _lastPlayhead = (int) _player.getPosition();
        } catch (Exception e) {
          DebugMode.logE(TAG, "Player is not null, yet position fails, player state: " + getState().name());
        }
      }
    }
  }

  // This is required because android enjoys making things difficult. talk to jigish if you got issues.
  private final Handler _playheadUpdateTimerHandler = new Handler(new Handler.Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      setChanged();
      notifyObservers(OoyalaPlayer.TIME_CHANGED_NOTIFICATION);
      return false;
    }
  });

  // Timer tasks for playhead updates
  @Override
  protected void startPlayheadTimer() {
    if (_playheadUpdateTimer != null) {
      stopPlayheadTimer();
    }
    _playheadUpdateTimer = new Timer();
    _playheadUpdateTimer.scheduleAtFixedRate(new PlayheadUpdateTimerTask(),
        TIMER_DELAY, TIMER_PERIOD);
  }

  @Override
  protected void stopPlayheadTimer() {
    if (_playheadUpdateTimer != null) {
      _playheadUpdateTimer.cancel();
      _playheadUpdateTimer = null;
    }
  }

  /** Extract text string from CC data. Does not handle position, color, font type, etc. */
  public String GetCCString(voSubtitleInfo subtitleInfo)
  {
    if(subtitleInfo == null)
      return "";
    if(subtitleInfo.getSubtitleEntry() == null)
      return "";

    String strTextAll = "";
    for(int i = 0; i < subtitleInfo.getSubtitleEntry().size(); i++)
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

  private void handleSubtitles() {
    VO_OSMP_RETURN_CODE returnValue;
    if (_player == null) {
      DebugMode.logE(TAG, "handleSubtitles: player is null");
      return;
    }

    applySubtitleSettings();
    // retrieve subtitle descriptions.
    // TODO: expose it via UI.
    VOCommonPlayerAssetSelection asset = _player;
    int subtitleCount = asset.getSubtitleCount();
    _subtitleDescriptions.clear();
    _isLiveClosedCaptionsAvailable = false;
    for (int index = 0; index < subtitleCount; ++index) {
      VOOSMPAssetProperty property = asset.getSubtitleProperty(index);

      String description;
      int propertyCount = property.getPropertyCount();
      if (propertyCount == 0) {
        description = "CC" + String.valueOf(index);
      } else {
        final int KEY_DESCRIPTION_INDEX = 1;
        description = (String) property.getValue(KEY_DESCRIPTION_INDEX);
      }
      if (asset.isSubtitleAvailable(index)) {
        _isLiveClosedCaptionsAvailable = true;
      }
      _subtitleDescriptions.add(description);
    }

    int selectedSubtitleIndex = asset.getPlayingAsset().getSubtitleIndex();
    DebugMode.logD(TAG, "handleSubtitles: selected subtitle "
        + selectedSubtitleIndex);

    if (_isLiveClosedCaptionsEnabled && this._isLiveClosedCaptionsAvailable) {
      returnValue = asset.selectSubtitle(_selectedSubtitleIndex);
      if (returnValue != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
      DebugMode.logD(
          TAG,
            "handleSubtitles: selectSubtitle("
                + String.valueOf(_selectedSubtitleIndex)
                + ") failed with error: "
              + returnValue.toString());
      }
    }
  }

  private void applySubtitleSettings() {
    if (_player == null) {
      DebugMode.logE(TAG, "enableSubtitles: player is null");
      return;
    }

    VO_OSMP_RETURN_CODE returnValue = _player
        .enableSubtitle(_isLiveClosedCaptionsEnabled);
    if (returnValue != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
      DebugMode.logE(TAG,
          "enable subtitles(" + String.valueOf(_isLiveClosedCaptionsEnabled)
              + ") failed with error:" + returnValue.toString());
    }
  }

  @Override
  public VO_OSMP_RETURN_CODE onVOEvent(VO_OSMP_CB_EVENT_ID id, int param1, int param2, Object obj) {

    switch (id) {
    case VO_OSMP_SRC_CB_OPEN_FINISHED:
      // After createMediaPlayer is complete, mark as ready
      DebugMode.logV(TAG, "OnEvent VO_OSMP_SRC_CB_OPEN_FINISHED");
      setState(State.READY);
      handleSubtitles();
      break;

    case VO_OSMP_CB_PLAY_COMPLETE:
      currentItemCompleted();
      break;

    case VO_OSMP_CB_SEEK_COMPLETE:
      // If first param is 0, seek is actaully complete
      if (param1 <= 0) {
        setChanged();
        notifyObservers(OoyalaPlayer.SEEK_COMPLETED_NOTIFICATION);
        if (_player.getPlayerStatus() == VO_OSMP_STATUS.VO_OSMP_STATUS_PLAYING) {
          setState(State.PLAYING);
        } else {
          setState(State.PAUSED);
          dequeuePlay();
        }
      }
      break;
    case VO_OSMP_CB_SRC_BUFFER_TIME:
      this._buffer = param1;
      break;

    case VO_OSMP_CB_VIDEO_SIZE_CHANGED:
      _videoWidth = param1;
      _videoHeight = param2;
      DebugMode.logV(TAG, "onEvent: Video Size Changed, " + _videoWidth + ", " + _videoHeight);
      _view.requestLayout();
      break;

    case VO_OSMP_CB_VIDEO_STOP_BUFFER:
      DebugMode.logD(TAG, "onEvent: Buffering Done! " + param1 + ", " + param2);
      if (_player.getPlayerStatus() == VO_OSMP_STATUS.VO_OSMP_STATUS_PLAYING) {
        setState(State.PLAYING);
      } else {
        setState(State.PAUSED);
        dequeuePlay();
      }
      break;

    case VO_OSMP_CB_VIDEO_START_BUFFER:
      DebugMode.logD(TAG, "onEvent: Buffering Starting " + param1 + ", " + param2);
      setState(State.LOADING);
      break;

    case VO_OSMP_CB_ERROR:
    case VO_OSMP_SRC_CB_CONNECTION_FAIL:
    case VO_OSMP_SRC_CB_DOWNLOAD_FAIL:
    case VO_OSMP_SRC_CB_DRM_FAIL:
    case VO_OSMP_SRC_CB_DRM_NOT_SECURE:
    case VO_OSMP_SRC_CB_CONNECTION_REJECTED:
    case VO_OSMP_SRC_CB_PLAYLIST_PARSE_ERR:
    case VO_OSMP_SRC_CB_DRM_AV_OUT_FAIL:
      // Display error dialog and stop player
      DebugMode.logE(TAG, "onEvent: Error. " + param1);
      onError(_player, null, id.getValue());
      break;

    case VO_OSMP_SRC_CB_ADAPTIVE_STREAMING_INFO:
      switch (param1) {
      case voOSType.VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_BITRATE_CHANGE: {
        DebugMode
            .logV(
                TAG,
            "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_BITRATE_CHANGE, param2 is "
                    + param2);
          break;
        }
      case voOSType.VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE: {
        DebugMode
            .logV(
                TAG,
            "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, param2 is"
                    + param2);
        switch (param2) {
        case voOSType.VOOSMP_AVAILABLE_PUREAUDIO: {
          DebugMode
              .logV(
                  TAG,
                  "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_PUREAUDIO");
          break;
        }
        case voOSType.VOOSMP_AVAILABLE_PUREVIDEO: {
          DebugMode
              .logV(
                  TAG,
                  "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_PUREVIDEO");
          break;
        }
        case voOSType.VOOSMP_AVAILABLE_AUDIOVIDEO: {
          DebugMode
              .logV(
                  TAG,
                  "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_AUDIOVIDEO");
          break;
        }
        }
          break;
        }
      }
      //Return now to avoid constant messages
      return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
    case VO_OSMP_SRC_CB_PROGRAM_CHANGED:
      DebugMode.logV(TAG, "OnEvent VO_OSMP_SRC_CB_PROGRAM_CHANGED");
      handleSubtitles();
      break;
    default:
      break;
    }
    DebugMode.logV(TAG, "VisualOn Message: " + id + ". param is " + param1 + ", " + param2);
    return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
  }

  @Override
  public VO_OSMP_RETURN_CODE onVOSyncEvent(VO_OSMP_CB_SYNC_EVENT_ID arg0,
      int arg1, int arg2, Object arg3) {
    return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
  }

  /**
   * After file download on init(), check the file for DRM.
   * If DRM'ed, move on to personalization -> acquireRights.  Otherwise, continue video playback
   */
  @Override
  public void afterFileDownload(String localFilename) {
    _localFilePath = localFilename;
    if (_localFilePath == null) {
      DebugMode.logE(TAG, "File Download failed!");
      _error = new OoyalaException(OoyalaErrorCode.ERROR_DRM_FILE_DOWNLOAD_FAILED);
      setState(State.ERROR);
    }
    else {
      if (isDiscredixNeeded() && isDiscredixLoaded() &&
          DiscredixDrmUtils.isStreamProtected(_parent.getLayout().getContext(), _localFilePath)) {
        DebugMode.logD(TAG, "File Download Succeeded: Need to acquire rights");
        PersonalizationAsyncTask personalizationTask = new PersonalizationAsyncTask(this, _parent.getLayout().getContext(), _parent.getOoyalaAPIClient().getPcode());
        personalizationTask.execute();
      }
      else {
        DebugMode.logD(TAG, "File Download Succeeded: No rights needed");
        createMediaPlayer();
      }
    }
  }

  /**
   * After we personalize, we can try to acquire rights
   */
  @Override
  public void afterPersonalization(Exception returnedException) {
    if (!isDiscredixLoaded()) {
      DebugMode.logE(TAG, "Personalzied without Discredix loaded");
      _error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Personalzied without Discredix loaded");
      setState(State.ERROR);
    }
    else if (returnedException != null) {
      DebugMode.logE(TAG, "Personalization resulted in an exception! " + returnedException);
      _error = new OoyalaException(OoyalaErrorCode.ERROR_DRM_GENERAL_FAILURE, returnedException);
      setState(State.ERROR);
    }
    else if (!DiscredixDrmUtils.isDevicePersonalized(_parent.getLayout().getContext())) {
      DebugMode.logE(TAG, "Personalization failed");
      _error = new OoyalaException(OoyalaErrorCode.ERROR_DRM_PERSONALIZATION_FAILED, "Personalization Failed");
      setState(State.ERROR);
    }
    else {
      DebugMode.logD(TAG, "Personalization successful");
      tryToAcquireRights();
    }
  }

  /**
   * After we acquire rights, we can begin video playback, as long as the surface was created already
   */
  @Override
  public void afterAcquireRights(Exception returnedException) {
    if (returnedException != null) {
      DebugMode.logE(TAG, "Acquire Rights failed: " + returnedException.getClass());
      _error = DiscredixDrmUtils.handleDRMError(returnedException);
      setState(State.ERROR);
    }
    else {
      DebugMode.logD(TAG, "Acquire Rights successful");
      createMediaPlayer();
    }
  }

  /**
   * Ensure that we have enough information to acquire rights (personalization, file download)
   * then try to acquire rights.
   */
  public void tryToAcquireRights() {
    if (!isDiscredixLoaded()) {
      DebugMode.logE(TAG, "Trying to acquire rights when Discredix doesn't exist");
      _error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Trying to acquire rights when Discredix doesn't exist");
      setState(State.ERROR);
    }
    else {
      boolean isdevicePersonalized = DiscredixDrmUtils.isDevicePersonalized(_parent.getLayout().getContext());
      if(!isdevicePersonalized || _localFilePath == null) {
        DebugMode.logE(TAG, "We are not able to acquire rights: We are either not personalized or no file: Personalization = " + isdevicePersonalized + ", localFilePath = " + _localFilePath);
        _error = new OoyalaException(OoyalaErrorCode.ERROR_DRM_GENERAL_FAILURE, "Acquire Rights being called when personalization/download did not happen");
        setState(State.ERROR);
      }
      else {
        DebugMode.logD(TAG, "Acquiring rights");
        String authToken = _parent.getAuthToken();
        String customDRMData = _parent.getCustomDRMData();
        AcquireRightsAsyncTask acquireRightsTask = new AcquireRightsAsyncTask(this, _parent.getLayout().getContext(), _localFilePath,
            authToken, customDRMData);
        acquireRightsTask.execute();
      }
    }
  }

}
