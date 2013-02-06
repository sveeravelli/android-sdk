package com.ooyala.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.ooyala.android.OoyalaPlayer.State;
import com.visualon.OSMPBasePlayer.voOSBasePlayer;
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
class VisualOnMoviePlayer extends Player implements
    voOSBasePlayer.onEventListener, voOSBasePlayer.onRequestListener,
    SurfaceHolder.Callback {
  private static final String TAG = "[PLAYER_SAMPLE]:VisualOn";

  protected voOSBasePlayer _player = null;
  protected SurfaceHolder _holder = null;
  protected String _streamUrl = "";
  protected int _videoWidth = 16;
  protected int _videoHeight = 9;

  private boolean _playQueued = false;
  private boolean _completedQueued = false;
  private int _timeBeforeSuspend = -1;
  private State _stateBeforeSuspend = State.INIT;
  protected Timer _playheadUpdateTimer = null;
  private int _lastPlayhead = -1;
  private boolean mTrackProgressing = false;
  private boolean _isLiveClosedCaptionsAvailable = false;
  private boolean _isLiveClosedCaptionsEnabled = false;

  protected static final long TIMER_DELAY = 0;
  protected static final long TIMER_PERIOD = 250;

  /* Copy file from Assets directory to destination. Used for licenses and processor-specific configurations */
  private static void copyfile(Context context, String filename, String desName)
  {
    try {
      InputStream InputStreamis  = context.getAssets().open(filename);
      File desFile = new File("/data/data/" +
            context.getPackageName() + "/" + desName);
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

      if (_lastPlayhead != _player.GetPos()) {
        _playheadUpdateTimerHandler.sendEmptyMessage(0);
      }
      _lastPlayhead = _player.GetPos();
    }
  }

  // This is required because android enjoys making things difficult. talk to
  // jigish if you got issues.
  private final Handler _playheadUpdateTimerHandler = new Handler() {
    public void handleMessage(Message msg) {
      setChanged();
      notifyObservers(OoyalaPlayer.TIME_CHANGED_NOTIFICATION);
    }
  };

  @Override
  public void init(OoyalaPlayer parent, Object stream) {
    Log.d(this.getClass().getName(), "Using VOPlayer");
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


    // Copy license file,
    copyfile(_parent.getLayout().getContext(), "voVidDec.dat", "voVidDec.dat");
    copyfile(_parent.getLayout().getContext(), "cap.xml", "cap.xml");


    setupView();
  }

  @Override
  public void pause() {
    _playQueued = false;
    switch (_state) {
    case PLAYING:
      stopPlayheadTimer();
      _player.Pause();
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
        _player.SetPos(_timeBeforeSuspend);
        _timeBeforeSuspend = -1;
      }
      int nRet = _player.Run();
      if (nRet == voOSType.VOOSMP_ERR_None) {
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
    _player.Stop();
    _player.Close();
  }

  @Override
  public void reset() {
    suspend(0, State.PAUSED);
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
    return _player.GetPos();
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
    return _player.GetDuration();
  }

  @Override
  public int buffer() {
    return this._buffer;
  }

  @Override
  public void seekToTime(int timeInMillis) {
    if (_player == null) {
      return;
    }
    _player.SetPos(timeInMillis);
  }

  protected void createMediaPlayer() {
    try {
      if (_player == null) {
        _player = new voOSBasePlayer();
      } else {

    	  Log.e(TAG, "DANGER DANGER: Creating a Media player when one already exists");
        _player.Uninit();
        return;
      }

      // SDK player engine type
      int nParam = voOSType.VOOSMP_VOME2_PLAYER;

      // Location of libraries
      String apkPath = "/data/data/"
          + _parent.getLayout().getContext().getPackageName() + "/lib/";

      // Initialize SDK player
      int nRet = _player.Init(_parent.getLayout().getContext(), apkPath, null,
          nParam, 0, 0);
      if (nRet == voOSType.VOOSMP_ERR_None) {
        Log.v(TAG, "MediaPlayer is created.");
      } else {
        onError(_player, nRet, 0);
        return;
      }

      DisplayMetrics dm  = new DisplayMetrics();
      WindowManager wm = (WindowManager) _view.getContext().getSystemService(Context.WINDOW_SERVICE);
      Display display = wm.getDefaultDisplay();
      display.getMetrics(dm);

      _player.SetDisplaySize(dm.widthPixels, dm.heightPixels);
      _player.SetView(_view);
      // Register SDK event listener
      _player.setEventListener(this);

      /* Configure DRM parameters */
      _player.SetParam(voOSType.VOOSMP_SRC_PID_DRM_FILE_NAME, "voDRM");
      _player.SetParam(voOSType.VOOSMP_SRC_PID_DRM_API_NAME, "voGetDRMAPI");

      /* Set the license */
      String licenseText = "VOTRUST_OOYALA_754321974";		// Magic string from VisualOn, must match voVidDec.dat to work
      _player.SetParam(voOSType.VOOSMP_PID_LICENSE_TEXT, licenseText);

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
      _player.SetParam(voOSType.VOOSMP_PID_LICENSE_CONTENT, b);

      /* Configure Dolby Audio Effect parameters */
      _player.SetParam(voOSType.VOOSMP_PID_AUDIO_EFFECT_ENABLE, 0);

      // Enable CC
      _player.SetParam(voOSType.VOOSMP_PID_CLOSED_CAPTION_OUTPUT, 1);

      /* Processor-specific settings */
        String cfgPath = "/data/data/" + _parent.getLayout().getContext().getPackageName() + "/";
        String capFile = cfgPath + "cap.xml";
        _player.SetParam(voOSType.VOOSMP_SRC_PID_CAP_TABLE_PATH, capFile);

        //Open then run the player
        nRet = _player.Open(
            _streamUrl,
            voOSType.VOOSMP_FLAG_SOURCE_URL, 0, 0, 0);
          if (nRet == voOSType.VOOSMP_ERR_None) {
            Log.v(TAG, "MediaPlayer is Opened.");
          } else {

            Toast.makeText(_parent.getLayout().getContext(), "Could not connect to " + _streamUrl + "!", Toast.LENGTH_LONG).show();
            //onError(_player, nRet, 0);
            return;
          }
      setState(State.READY);

    } catch (Throwable t) {
      t.printStackTrace(); }
  }

  public boolean onError(voOSBasePlayer mp, int what, int extra) {
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
    	_player.SetParam(voOSType.VOOSMP_PID_SURFACE_CHANGED, 1);
    }

  }

  @Override
  public void surfaceCreated(SurfaceHolder arg0) {
    Log.i(TAG, "Surface Created");

    if (_player !=null)
    {
      // If SDK player already exists, show media controls
      _player.SetParam(voOSType.VOOSMP_PID_VIEW_ACTIVE, _view);
      return;
    }

    if (_state == State.LOADING) {
      createMediaPlayer();
      dequeuePlay();
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder arg0) {
    Log.i(TAG, "Surface Destroyed");
    if (_player != null) {
      _player.SetView(null);
    }
  }

  @Override
  public void setParent(OoyalaPlayer parent) {
    super.setParent(parent);
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
private void setupView() {
    if (_view == null) {
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
    else {
      if (_state == State.LOADING) {
        createMediaPlayer();
      }
    }
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
    suspend(_player != null ? _player.GetPos() : 0, _state);
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
      _player.Uninit();
      _player = null;
    }
    removeView();
    _buffer = 0;
    _playQueued = false;
    setState(State.SUSPENDED);
  }

  @Override
  public void resume() {
    Log.v(TAG, "Player Resume");
    if (_state != State.SUSPENDED) {
      return;
    }
    setState(State.LOADING);
    setupView();
    if (_stateBeforeSuspend == State.PLAYING || _stateBeforeSuspend == State.LOADING) {
      play();
    } else if (_stateBeforeSuspend == State.COMPLETED) {
      queueCompleted();
    }
  }

  @Override
  public void destroy() {
    if (_player != null) {
      stop();
      _player.Uninit();
      _player = null;
    }
    removeView();
    _parent = null;
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
  public int onEvent(int id, int param1, int param2, Object obj) {
    if (id == voOSType.VOOSMP_SRC_CB_Adaptive_Streaming_Info) {

        _view.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));


        switch (param1) {
      case voOSType.VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_BITRATE_CHANGE: {
        Log.v(
            TAG,
            "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_BITRATE_CHANGE, param2 is %d . "
                + param2);


        break;
      }
      case voOSType.VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE: {
        Log.v(
            TAG,
            "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, param2 is %d . "
                + param2);

        switch (param2) {
        case voOSType.VOOSMP_AVAILABLE_PUREAUDIO: {
          Log.v(
              TAG,
              "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_PUREAUDIO");
          break;
        }
        case voOSType.VOOSMP_AVAILABLE_PUREVIDEO: {
          Log.v(
              TAG,
              "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_PUREVIDEO");
          break;
        }
        case voOSType.VOOSMP_AVAILABLE_AUDIOVIDEO: {
          Log.v(
              TAG,
              "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_MEDIATYPE_CHANGE, VOOSMP_AVAILABLE_AUDIOVIDEO");
          break;
        }
        }
        break;
      }
      }
    } else if (id == voOSType.VOOSMP_CB_Error) // Error
    {
      // Display error dialog and stop player
      Log.e(TAG, "onEvent: Error. " + param1);
      onError(_player, 1, 0);
      return 0;
    } else if (id == voOSType.VOOSMP_CB_PlayComplete) {
      Log.v(TAG, "onEvent: Play Complete");
      currentItemCompleted();
      return 0;
    } else if (id == voOSType.VOOSMP_CB_SeekComplete) // Seek (SetPos) complete
    {
      Log.v(TAG, "onEvent: Seek Complete");
      dequeuePlay();
      return 0;
    } else if (id == voOSType.VOOSMP_CB_BufferStatus) // Updated buffer status
    {
      this._buffer = param1;
      return 0;
    } else if (id == voOSType.VOOSMP_CB_VideoSizeChanged) // Video size changed
    {

      _videoWidth = param1;
      _videoHeight = param2;
      Log.v(TAG, "onEvent: Video Size Changed, " + _videoWidth + ", " + _videoHeight);

      _view.setLayoutParams(new FrameLayout.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));

      return 0;
    } else if (id == voOSType.VOOSMP_CB_VideoStopBuff) // Vid seteo buffering
                                                       // stopped
    {
      return 0;
    } else if (id == voOSType.VOOSMP_CB_VideoStartBuff) // Video buffering
                                                        // started
    {
      return 0;
    } else if (id == voOSType.VOOSMP_SRC_CB_Connection_Fail
        || id == voOSType.VOOSMP_SRC_CB_Download_Fail
        || id == voOSType.VOOSMP_SRC_CB_DRM_Fail
        || id == voOSType.VOOSMP_SRC_CB_Playlist_Parse_Err
        || id == voOSType.VOOSMP_SRC_CB_Connection_Rejected
        || id == voOSType.VOOSMP_SRC_CB_DRM_Not_Secure
        || id == voOSType.VOOSMP_SRC_CB_DRM_AV_Out_Fail) // Errors
    {
      // Display error dialog and stop player
      onError(_player, id, 0);

    } else if (id == voOSType.VOOSMP_SRC_CB_BA_Happened) // Unimplemented
    {
      Log.v(TAG, "OnEvent VOOSMP_SRC_CB_BA_Happened, param is %d . " + param1);
    } else if (id == voOSType.VOOSMP_SRC_CB_Download_Fail_Waiting_Recover) {
      Log.v(TAG,
          "OnEvent VOOSMP_SRC_CB_Download_Fail_Waiting_Recover, param is %d . "
              + param1);
    } else if (id == voOSType.VOOSMP_SRC_CB_Download_Fail_Recover_Success) {
      Log.v(TAG,
          "OnEvent VOOSMP_SRC_CB_Download_Fail_Recover_Success, param is %d . "
              + param1);
    } else if (id == voOSType.VOOSMP_SRC_CB_Open_Finished) {
      Log.v(TAG, "OnEvent VOOSMP_SRC_CB_Open_Finished, param is %d . " + param1);
    } else if (id == voOSType.VOOSMP_CB_ClosedCaptionData) { //CC data

      // Remember if we have recieved live closed captions at some point duirng playback
      _isLiveClosedCaptionsAvailable = true;
      if (_isLiveClosedCaptionsEnabled) {
        // Retrieve subtitle info, get text, and display it
        voSubtitleInfo info = (voSubtitleInfo)obj;
        String cc = GetCCString(info);
        _parent.displayClosedCaptionText(cc);
      }
    } else {
    Log.v(TAG, "OnEvent UNHANDLED MESSAGE!, id is: " + id + ". param is "
          + param1 + ", " + param2);
    }

    return 0;
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

	//Sets enablement of live CC, which is checked every time VisualOn recieves CC data on stream
  @Override
  public void setLiveClosedCaptionsEnabled(boolean enabled){
    _isLiveClosedCaptionsEnabled = enabled;
  }

	@Override
	public boolean isLiveClosedCaptionsAvailable() {
	  return _isLiveClosedCaptionsAvailable;
	}

}
