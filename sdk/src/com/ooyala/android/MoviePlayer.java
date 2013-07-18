package com.ooyala.android;

import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import android.util.Log;
import android.view.View;
import com.ooyala.android.OoyalaPlayer.State;

public class MoviePlayer extends Player implements Observer {
  private State _stateToResume = State.INIT;
  private int _millisToResume = 0;
  private StreamPlayer _basePlayer;
  private Set<Stream> _streams;
  private boolean _suspended = true;
  protected boolean _seekable = true;
  private boolean _live = false;

  /**
   * Check which base player would be best suited for this MoviePlayer
   * @param streams
   * @return the correct default base player
   */
  private StreamPlayer getPlayerForStreams(Set<Stream> streams) {
    StreamPlayer player = null;

    // If custom HLS Player is enabled, and one of the following:
    //   1.) Delviery type is HLS
    //   2.) Delviery type is Remote Asset, and the url contains .m3u8
    // use VisualOn
    if (OoyalaPlayer.enableCustomHLSPlayer &&
        (Stream.streamSetContainsDeliveryType(streams, Constants.DELIVERY_TYPE_HLS) ||
         (Stream.streamSetContainsDeliveryType(streams, Constants.DELIVERY_TYPE_REMOTE_ASSET) &&
          Stream.getStreamWithDeliveryType(streams, Constants.DELIVERY_TYPE_REMOTE_ASSET).decodedURL()
            .toString().contains("m3u8"))
        )
       ) {
      try {
        player = (StreamPlayer)getClass().getClassLoader().loadClass(Constants.VISUALON_PLAYER).newInstance();
      } catch(Exception e) {
        player = new BaseMoviePlayer();
      }
    } else {
      player = new BaseMoviePlayer();
    }
    return player;
  }

  public void init(OoyalaPlayer parent, Set<Stream> streams) {
   // super.init(parent, stream);
    if (streams == null || streams.size() == 0) {
      _error = "There are no streams to play";
      Log.e(this.getClass().toString(), _error);
      return;
    }

    _parent = parent;
    _streams = streams;
    _suspended = false;
    if(_basePlayer == null) {
      _basePlayer = getPlayerForStreams(streams);
    }
    _basePlayer.addObserver(this);
    _basePlayer.init(parent, streams);
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
    _streams = streams;
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
      Log.i(this.getClass().toString(), "Trying to suspend an already suspended MoviePlayer");
      return;
    }
    Log.d(this.getClass().toString(), "Movie Player Suspending. ms to resume: " + millisToResume + ". State to resume: " + stateToResume);
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

    if(_live) millisToResume = 0;

    Log.d(this.getClass().toString(), "Movie Player Resuming. ms to resume: " + millisToResume + ". State to resume: " + stateToResume);
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

  public View getView() {
    return _basePlayer.getView();
  }

  public void setParent(OoyalaPlayer parent) {
    _parent = parent;
    _basePlayer.setParent(parent);
  }

  //Delegated to base player
  public void pause() { _basePlayer.pause(); }
  public void play() { _basePlayer.play(); }
  public void stop() { _basePlayer.stop(); }

  public int currentTime() { return _basePlayer != null ? _basePlayer.currentTime() : 0; }
  public int duration() { return _basePlayer != null ? _basePlayer.duration() : 0; }
  public int buffer() { return _basePlayer != null ? _basePlayer.buffer() : 0; }
  public int getBufferPercentage() { return _basePlayer != null ? _basePlayer.getBufferPercentage() : 0; }

  public boolean seekable() { return _seekable; }
  public void setSeekable(boolean seekable) { _seekable = seekable; }
  public void seekToTime(int timeInMillis) { if (_seekable) { _basePlayer.seekToTime(timeInMillis); } }

  public State getState() { return _basePlayer != null ? _basePlayer.getState() : super.getState(); }
  protected void setState(State state) {
    if (_basePlayer != null) {
      _basePlayer.setState(state);
    } else {
      super.setState(state);
    }
  }

  public String getError() { return _basePlayer != null ? _basePlayer.getError() : _error; }
  public boolean isLiveClosedCaptionsAvailable() { return _basePlayer != null ? _basePlayer.isLiveClosedCaptionsAvailable() : false; }
  public void setLiveClosedCaptionsEnabled(boolean enabled) { _basePlayer.setLiveClosedCaptionsEnabled(enabled); }
}