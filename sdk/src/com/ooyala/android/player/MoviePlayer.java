package com.ooyala.android.player;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import android.view.View;

import com.ooyala.android.DebugMode;
import com.ooyala.android.OoyalaException;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.SeekStyle;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.TVRating;
import com.ooyala.android.item.Stream;

public class MoviePlayer extends Player implements Observer {

  private static final String TAG = "MoviePlayer";
  static final String VISUALON_PLAYER = "com.ooyala.android.visualon.VisualOnStreamPlayer";
  protected static final String DRM_TENENT_PATH = "/sas/drm2/%s/%s/%s/%s"; // '/drm2/:pcode/:embed_code/:drm_type/:tenant'

  private State _stateToResume = State.INIT;
  private int _millisToResume = 0;
  private StreamPlayer _basePlayer;
  private Set<Stream> _streams;
  private TVRating _tvRating;
  private TVRatingUI _tvRatingUI;
  private boolean _suspended = true;
  protected boolean _seekable = true;
  private boolean _live = false;

  /**
   * Check which base player would be best suited for this MoviePlayer
   * @param streams
   * @return the correct default base player, possibly null.
   */
  private StreamPlayer getPlayerForStreams(Set<Stream> streams) {
    StreamPlayer player = null;

    // If custom HLS Player is enabled, and one of the following:
    //   1.) Delivery type is HLS
    //   2.) Delivery type is Remote Asset, and the url contains .m3u8
    //   3.) Delivery type is Smooth streaming
    // use VisualOn
    boolean isHls = Stream.streamSetContainsDeliveryType(streams, Stream.DELIVERY_TYPE_HLS);
    boolean isRemoteHls = Stream.streamSetContainsDeliveryType(streams, Stream.DELIVERY_TYPE_REMOTE_ASSET) &&
        Stream.getStreamWithDeliveryType(streams, Stream.DELIVERY_TYPE_REMOTE_ASSET).decodedURL().toString().contains("m3u8");
    boolean isSmooth = Stream.streamSetContainsDeliveryType(streams, Stream.DELIVERY_TYPE_SMOOTH);
    boolean isRemoteSmooth = Stream.streamSetContainsDeliveryType(streams, Stream.DELIVERY_TYPE_REMOTE_ASSET) &&
        Stream.getStreamWithDeliveryType(streams, Stream.DELIVERY_TYPE_REMOTE_ASSET).decodedURL().toString().contains(".ism");

    boolean isVisualOnHLSEnabled = OoyalaPlayer.enableCustomHLSPlayer && (isHls || isRemoteHls);
    boolean isVisualOnSmoothEnabled = OoyalaPlayer.enableCustomSmoothPlayer && (isSmooth || isRemoteSmooth);

    if( streams == null || streams.size() == 0 ) {
      player = new EmptyStreamPlayer();
    }
    else if (isVisualOnHLSEnabled || isVisualOnSmoothEnabled) {
      try {
        player = (StreamPlayer)getClass().getClassLoader().loadClass(VISUALON_PLAYER).newInstance();
      } catch(Exception e) {
        DebugMode.logE(TAG, "Tried to load VisualOn Player but failed");
        player = new BaseStreamPlayer();
      }
    } else {
      if (isSmooth || isRemoteSmooth) {
        DebugMode.assertFail(TAG, "A Smooth stream is about to load on the base stream player.  Did you mean to set enableCustomSmoothPlayer?");
      }
      if (isVisualOnSmoothEnabled && (isHls || isRemoteHls)) {
        DebugMode.logE(TAG,  "An HLS stream is loaded, while you enabled CustomSmoothPlayer.  Did you also want to set enableCustomHLSPlayer?");
      }
      player = new BaseStreamPlayer();
    }
    return player;
  }

  private void setStreams( Set<Stream> streams ) {
    if( streams == null ) {
      _streams = new HashSet<Stream>();
    }
    else {
      _streams = streams;
    }
  }

  @Override
  public void init(OoyalaPlayer parent, Set<Stream> streams) {
    setStreams( streams );
    _parent = parent;
    _streams = streams;
    _suspended = false;
    if(_basePlayer == null) {
      _basePlayer = getPlayerForStreams(streams);
    }
    _basePlayer.addObserver(this);
    _basePlayer.init(parent, streams);
    initTVRating();
    pushTVRating();
  }
  
  private void initTVRating() {
    if( _tvRatingUI != null ) {
      // todo: cleanup!
    }
    _tvRatingUI = new TVRatingUI( _basePlayer.getView(), _parent.getLayout(), _parent.getOptions().getTVRatingConfiguration() );
  }
  
  public void setTVRating( TVRating TVRating ) {
    _tvRating = TVRating;
    pushTVRating();
  }
  
  private void pushTVRating() {
    if( _tvRating != null && _tvRatingUI != null ) {
      _tvRatingUI.pushTVRating( _tvRating );
      // prevent setTVRating from happening again with this data.
      _tvRating = null;
    }
  }

  /**
   * Specify if this baseplayer will be playing a live video or not (default false)
   * @param isLive
   */
  public void setLive(boolean isLive) {
    _live = isLive;
  }

  public StreamPlayer getBasePlayer() {
    return _basePlayer;
  }

  public void setBasePlayer(StreamPlayer basePlayer, Set<Stream> streams) {
    setStreams( streams );
    setBasePlayer(basePlayer);
  }

  public void setBasePlayer(StreamPlayer basePlayer) {
    boolean shouldResume = !_suspended;
    if (shouldResume) {
      suspend();
    }

    _basePlayer = basePlayer != null ? basePlayer : getPlayerForStreams(_streams);

    if (shouldResume) {
      resume();
    }
  }

  @Override
  public void reset() {
    if (_basePlayer != null) {
      _basePlayer.reset();
    }
  }

  @Override
  public void suspend() {
    if (_basePlayer != null) {
      suspend(_basePlayer.currentTime(), _basePlayer.getState());
    } else {
      suspend(0, State.INIT);
    }
  }

  @Override
  public void suspend(int millisToResume, State stateToResume) {
    // If we're already suspended, we don't need to do it again
    if (stateToResume == State.SUSPENDED) {
      DebugMode.logI(this.getClass().toString(), "Trying to suspend an already suspended MoviePlayer");
      return;
    }
    DebugMode.logD(this.getClass().toString(), "Movie Player Suspending. ms to resume: " + millisToResume + ". State to resume: " + stateToResume);
    _suspended = true;
    _millisToResume = millisToResume;
    _stateToResume = stateToResume;
    if (_basePlayer != null) {
      _basePlayer.deleteObserver(this);
      _basePlayer.suspend(millisToResume, stateToResume);
    }
  }

  @Override
  public void resume() {
    resume(_millisToResume, _stateToResume);
  }

  @Override
  public void resume(int millisToResume, State stateToResume) {  // TODO: Wtf to do here?
    _suspended = false;
    _basePlayer.init(_parent, _streams);
    _basePlayer.addObserver(this);
    initTVRating();

    if(_live) millisToResume = 0;

    DebugMode.logD(this.getClass().toString(), "Movie Player Resuming. ms to resume: " + millisToResume + ". State to resume: " + stateToResume);
    _basePlayer.resume(millisToResume, stateToResume);
  }

  @Override
  public void destroy() {
    if (_basePlayer != null) _basePlayer.destroy();
  }

  @Override
  public void update(Observable arg0, Object arg) {
    setChanged();
    notifyObservers(arg);
  }

  @Override
  public View getView() {
    return _basePlayer.getView();
  }

  @Override
  public void setParent(OoyalaPlayer parent) {
    _parent = parent;
    _basePlayer.setParent(parent);
  }

  //Delegated to base player
  @Override
  public void pause() { _basePlayer.pause(); }
  @Override
  public void play() { DebugMode.logV( TAG, "play()" ); _basePlayer.play(); }
  @Override
  public void stop() { _basePlayer.stop(); }

  @Override
  public int currentTime() { return _basePlayer != null ? _basePlayer.currentTime() : 0; }
  @Override
  public int duration() { return _basePlayer != null ? _basePlayer.duration() : 0; }
  @Override
  public int buffer() { return _basePlayer != null ? _basePlayer.buffer() : 0; }
  @Override
  public int getBufferPercentage() { return _basePlayer != null ? _basePlayer.getBufferPercentage() : 0; }

  public boolean seekable() { return _seekable; }
  public void setSeekable(boolean seekable) { _seekable = seekable; }
  @Override
  public void seekToTime(int timeInMillis) { if (_seekable) { _basePlayer.seekToTime(timeInMillis); } }

  @Override
  public SeekStyle getSeekStyle() {
    return _basePlayer != null ? _basePlayer.getSeekStyle() : SeekStyle.ENHANCED;
  }

  @Override
  public State getState() { return _basePlayer != null ? _basePlayer.getState() : super.getState(); }
  @Override
  protected void setState(State state) {
    if (_basePlayer != null) {
      _basePlayer.setState(state);
    } else {
      super.setState(state);
    }
  }

  @Override
  public OoyalaException getError() { return _error != null ? _error : _basePlayer.getError(); }
  @Override
  public boolean isPlaying() { return _basePlayer != null ? _basePlayer.isPlaying() : false; }
  @Override
  public boolean isLiveClosedCaptionsAvailable() { return _basePlayer != null ? _basePlayer.isLiveClosedCaptionsAvailable() : false; }
  @Override
  public void setLiveClosedCaptionsEnabled(boolean enabled) { _basePlayer.setLiveClosedCaptionsEnabled(enabled); }
}