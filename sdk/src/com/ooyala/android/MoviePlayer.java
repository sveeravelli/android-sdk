package com.ooyala.android;

import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import android.view.View;
import com.ooyala.android.OoyalaPlayer.State;

public class MoviePlayer extends Player implements Observer {
  private State _stateToResume = State.INIT;
  private int _millisToResume = 0;
  private StreamPlayer _basePlayer;
  private Set<Stream> _streams;
  private boolean _suspended = true;


  /**
   * Check which base player would be best suited for this MoviePlayer
   * @param streams
   * @return the correct default base player
   */
  private StreamPlayer getPlayerForStreams(Set<Stream> streams) {
    if (streams == null || streams.size() == 0) { return null; }
    StreamPlayer player;
    if (OoyalaPlayer.enableCustomHLSPlayer &&
        (Stream.streamSetContainsDeliveryType(streams, Constants.DELIVERY_TYPE_HLS) ||
         Stream.streamSetContainsDeliveryType(streams, Constants.DELIVERY_TYPE_REMOTE_ASSET))) {
      player =  new VisualOnMoviePlayer();
    } else {
      player = new BaseMoviePlayer();
    }
    return player;
  }

  public void init(OoyalaPlayer parent, Set<Stream> streams) {
   // super.init(parent, stream);
    _parent = parent;
    _streams = streams;
    _suspended = false;
    _basePlayer = getPlayerForStreams(streams);
    _basePlayer.addObserver(this);
    _basePlayer.init(parent, streams);
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
    if (basePlayer == null) {
      _basePlayer = getPlayerForStreams(_streams);
    }

    _basePlayer = basePlayer;

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
    if (_basePlayer != null) {
      _basePlayer.addObserver(this);
     // if (stateToResume == State.INIT) {
        _basePlayer.init(_parent, _streams);
        _basePlayer.seekToTime(millisToResume);
        
        if (stateToResume == State.PLAYING) {
          _basePlayer.play();
        }
        //} else {
        //_basePlayer.resume(millisToResume, stateToResume);
     // }
    }
  }

  @Override
  public void destroy() {
    _basePlayer.destroy();
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
  public int currentTime() { return _basePlayer.currentTime(); }
  public int duration() { return _basePlayer.duration(); }
  public int buffer() { return _basePlayer.buffer(); }
  public boolean seekable() { return _basePlayer.seekable(); }
  public void setSeekable(boolean seekable) { _basePlayer.setSeekable(seekable); }
  public void seekToTime(int timeInMillis) { _basePlayer.seekToTime(timeInMillis); }
  public State getState() { return _basePlayer.getState(); }
  protected void setState(State state) { _basePlayer.setState(state); }
  public String getError() { return _basePlayer.getError(); }
  public int getBufferPercentage() { return _basePlayer.getBufferPercentage(); }
}