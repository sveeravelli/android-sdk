package com.ooyala.android.player;

import java.util.HashSet;
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
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.ooyala.android.DebugMode;
import com.ooyala.android.OoyalaException;
import com.ooyala.android.OoyalaException.OoyalaErrorCode;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.SeekStyle;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.TVRatings;
import com.ooyala.android.item.Stream;
import com.ooyala.android.ui.FCCTVRatingsView;

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
  private static final int MARGIN_DIP = 5;
  protected View _container;
  protected TVRatings _tvRatings;
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
      DebugMode.logE(TAG, "ERROR: Invalid Stream (no valid stream available)");
      this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Invalid Stream");
      setState(State.ERROR);
      return;
    }

    if (parent == null) {
      this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Invalid Parent");
      setState(State.ERROR);
      return;
    }
    setParent(parent);
    setState(State.LOADING);
    _streamUrl = stream.getUrlFormat().equals(Stream.STREAM_URL_FORMAT_B64) ? stream.decodedURL().toString().trim() : stream.getUrl().trim();
    setupViews(); // must come after setParent().
    if (_player != null) { _player.reset(); }
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
  public void play() {
    _playQueued = false;
    switch (getState()) {
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
    setupViews();
    resume();
  }

  @Override
  public int currentTime() {
    if (_player == null) { return 0; }
    switch (getState()) {
      case INIT:
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
    switch (getState()) {
      case INIT:
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

    if( isSeekAllowed() ) {
      _player.seekTo(timeInMillis);
    }
    else {
      _timeBeforeSuspend = timeInMillis;
    }
  }

  private void seekToTimeOnPrepared( int timeInMillis ) {
    if (_player != null) {
      _player.seekTo(timeInMillis);
    }
  }

  private boolean isSeekAllowed() {
    return getState() == State.PAUSED || getState() == State.READY
        || getState() == State.COMPLETED || getState() == State.PLAYING;
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
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
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
      DebugMode.logE(TAG, "Unsupported video type given to base media player");
    }
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
      seekToTimeOnPrepared(_timeBeforeSuspend);
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

    //Set the visibility of the View above the surface to transparent, in order to show the video
    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
      _view.setBackgroundColor(Color.TRANSPARENT);
    }
    //These refer to when mid-playback buffering happens.  This doesn't apply to initial buffer
    else if(what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
      DebugMode.logD(TAG, "onInfo: Buffering Starting! " + what + ", extra: " + extra);
      setState(State.LOADING);
    } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
      DebugMode.logD(TAG, "onInfo: Buffering Done! " + what + ", extra: " + extra);
      if (_player.isPlaying()) {
        setState(State.PLAYING);
      }
      else {
        setState(State.PAUSED);
      }
    }
    return true;
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    currentItemCompleted();
  }

  @Override
  public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
    setVideoSize(width, height);
  }

  @Override
  public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
  }

  @Override
  public void surfaceCreated(SurfaceHolder arg0) {
    DebugMode.logI(TAG, "Surface Created");

    createMediaPlayer();
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder arg0) {
    DebugMode.logI(TAG, "Surface Destroyed");
  }
  
  @Override
  public void setTVRatings( TVRatings tvRatings ) {
    _tvRatings = tvRatings;
    pushTVRatings();
  }
  
  private void pushTVRatings() {
    if( _tvRatings != null && _tvRatingsView != null && currentTime() > 250 ) {
      _tvRatingsView.setTVRatings( _tvRatings );
      _tvRatings = null; // only do it once. 
    }
  }
  
  @Override
  protected void notifyTimeChanged() {
    super.notifyTimeChanged();
    pushTVRatings();
  }

  private void setupViews() {
    createAndAddViews();
    setupVideoSize();
    setupHolder();
  }

  private void createAndAddViews() {
    Context c = _parent.getLayout().getContext();
    /*
  <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
      android:id="@+id/movie_layout"
      android:layout_width="fill_parent"
      android:layout_height="fill_parent"
      android:background="#00000000" >
    */
    RelativeLayout relativeLayout = new RelativeLayout( c );
    relativeLayout.setBackgroundColor( android.graphics.Color.TRANSPARENT );
    FrameLayout.LayoutParams paramsForRelative = new FrameLayout.LayoutParams( FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT );
    _parent.getLayout().addView( relativeLayout, paramsForRelative );
    /*
      <com.ooyala.android.player.MovieView
          android:id="@+id/movie_view"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          android:layout_centerInParent="true"
          android:background="#FF000000" />
    */
    int anId = getUnusedId();
    MovieView movieView = new MovieView( c );
    movieView.setId( anId );
    movieView.setBackgroundColor( android.graphics.Color.BLACK );
    RelativeLayout.LayoutParams paramsForMovieView = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT );
    paramsForMovieView.addRule( RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE );
    relativeLayout.addView( movieView, paramsForMovieView );
    /*
      <com.ooyala.android.ui.FCCTVRatingsView
          android:id="@+id/tvratings_view"
          android:layout_width="match_parent"
          android:layout_height="match_parent"
          android:layout_alignTop="@id/movie_view"
          android:layout_alignLeft="@id/movie_view"
          android:layout_alignBottom="@id/movie_view"
          android:layout_alignRight="@id/movie_view"
          android:layout_marginBottom="5dp"
          android:layout_marginLeft="5dp"
          android:layout_marginRight="5dp"
          android:layout_marginTop="5dp"
          android:visibility="invisible"
          android:background="#00000000" />
    */
    FCCTVRatingsView ratingsView = new FCCTVRatingsView( c );
    ratingsView.setVisibility( View.INVISIBLE );
    ratingsView.setBackgroundColor( android.graphics.Color.TRANSPARENT );
    RelativeLayout.LayoutParams paramsForRatingsView = new RelativeLayout.LayoutParams( RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT );
    paramsForRatingsView.addRule( RelativeLayout.ALIGN_TOP, anId );
    paramsForRatingsView.addRule( RelativeLayout.ALIGN_LEFT, anId );
    paramsForRatingsView.addRule( RelativeLayout.ALIGN_BOTTOM, anId );
    paramsForRatingsView.addRule( RelativeLayout.ALIGN_RIGHT, anId );
    int margin = (int)TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, MARGIN_DIP, c.getResources().getDisplayMetrics() );
    paramsForRatingsView.setMargins( margin, margin, margin, margin ); 
    relativeLayout.addView( ratingsView, paramsForRatingsView );
    
    _container = relativeLayout;
    _view = movieView;
    _tvRatingsView = ratingsView;
    _tvRatingsView.setTVRatingsConfiguration( _parent.getOptions().getTVRatingsConfiguration() );
  }
  
  private int getUnusedId() {
    // i don't know if ids are generated up or down, and
    // i've heard that some regions of int values are reserved.
    // and empirically negative values didn't work.
    // so all in all, i'm doing this heuristic.
    Set<Integer> seenIds = new HashSet<Integer>();
    View v = _parent.getLayout();
    while( v != null ) {
      seenIds.add( v.getId() );
      ViewParent vp = v.getParent();
      v = vp instanceof View ? (View)vp : null;
    }
    int id = 1;
    // magic number rumoured, from http://stackoverflow.com/questions/6790623/programmatic-views-how-to-set-unique-ids.
    while( seenIds.contains(id) && id < 0x00FFFFFF-1 ) {
      id++;
    }
    return id;
  }
  
  private void setupVideoSize() {
    // Try to figure out the video size.  If not, use our default
    if (stream.getWidth() > 0 && stream.getHeight() > 0) {
      setVideoSize(stream.getWidth(), stream.getHeight());
    } else {
      setVideoSize(16,9);
    }
  }

  @SuppressWarnings("deprecation")
  private void setupHolder() {
    _holder = _view.getHolder();
    _holder.addCallback(this);
    _holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  }

  private void removeView() {
	  if (_parent != null) {
		  _parent.getLayout().removeView(_tvRatingsView);
		  _parent.getLayout().removeView(_view);
		  _parent.getLayout().removeView(_container);
	  }
	  if (_holder != null) {
		  _holder.removeCallback(this);
	  }
	  _tvRatingsView = null;
	  _view = null;
	  _container = null;
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
      DebugMode.logI(this.getClass().getName(), "Seek failed. currentPos: " + _player.getCurrentPosition() +
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
    suspend(_player != null ? _player.getCurrentPosition() : 0, getState());
  }

  @Override
  public void suspend(int millisToResume, State stateToResume) {
    if (getState() == State.SUSPENDED) {
      return;
    }
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
    setState(State.INIT);
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

  private boolean dequeueCompleted() {
    if (_completedQueued) {
      _playQueued = false;
      _completedQueued = false;
      return true;
    }
    return false;
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
        default:
          break;
      }
    }
  }

  @Override
  public boolean isPlaying() {
    return _player != null && _player.isPlaying();
  }

  @Override
  protected void setState(State state) {
    if (dequeueCompleted()) {
      super.setState(State.COMPLETED);
    } else {
      super.setState(state);
      dequeuePlay();
    }
  }

  @Override
  public SeekStyle getSeekStyle() {
    if(stream == null || Stream.DELIVERY_TYPE_HLS.equals(stream.getDeliveryType()) || stream.decodedURL().toString().contains("m3u8")) {
      return SeekStyle.BASIC;
    }
    else {
      return SeekStyle.ENHANCED;
    }
  }
}
