package com.ooyala.android.player;

import java.util.Observable;
import java.util.Set;

import android.view.SurfaceView;
import android.view.View;

import com.ooyala.android.OoyalaException;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.SeekStyle;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.TVRatings;
import com.ooyala.android.item.Stream;
import com.ooyala.android.ui.FCCTVRatingsView;

/**
 * The interface that must be implemented in order to plug into OoyalaPlayer and Ooyala UI
 * @author michael.len
 *
 */
public abstract class Player extends Observable {
  protected OoyalaPlayer _parent = null;
  /** the current state of the player */
  private State _state = State.INIT;
  /** The Player's current error if it exists */
  protected OoyalaException _error = null;
  protected SurfaceView _view = null;
  protected FCCTVRatingsView _tvRatingsView;
  protected boolean _resizeQueued = false;
  protected int _buffer = 0;
  protected boolean _fullscreen = false;
  protected boolean _pausable = true;

  /**
   * Init the player
   */
  protected Player() {}

  public void init(OoyalaPlayer parent, Set<Stream> streams) {}
  
  /**
   * Optional feature for Players: support for showing TVRatings.
   * Defaults to no support - nothing is shown for the given data.
   * Subclasses must override if need be.
   */
  public void setTVRatings( TVRatings tvRatings ) {}

  /**
   * Pause the current video
   */
  public void pause() {}

  /**
   * Play the current video
   */
  public void play() {}

  /**
   * Stop playback, remove listeners
   */
  public void stop() {}

  /**
   * Get the current playhead time
   * @return the current playhead time in milliseconds as an int
   */
  public int currentTime() {
    return 0;
  }

  /**
   * Get the current item's duration
   * @return duration in milliseconds as an int
   */
  public int duration() {
    return 0;
  }

  /**
   * Get the current item's buffer
   * @return buffer+played percentage as an int
   */
  public int buffer() {
    return 0;
  }

  /**
   * Set the current playhead time of the player
   * @param timeInMillis int millis to set the playhead time to
   */
  public void seekToTime(int timeInMillis) {}

  public State getState() {
    return _state;
  }

  protected void setState(State state) {
    this._state = state;
    setChanged();
    notifyObservers(OoyalaPlayer.STATE_CHANGED_NOTIFICATION);
  }

  public OoyalaException getError() {
    return _error;
  }

  public View getView() {
    return _view;
  }

  public void setParent(OoyalaPlayer parent) {
    _parent = parent;
  }

  public int getBufferPercentage() {
    return _buffer;
  }

  public boolean isPlaying() {
    return false;
  }

  /**
   * Returns if the current player has found live closed caption info in the stream
   * @return true if the stream has embedded live closed captions
   */
  public boolean isLiveClosedCaptionsAvailable(){
    return false;
  }

  /**
   * Enables and disables live closed captions on the player
   * @param enabled weather to disable or enable live closed captions
   */
  public void setLiveClosedCaptionsEnabled(boolean enabled){}

  public SeekStyle getSeekStyle() {
    return SeekStyle.ENHANCED;
  }

  public abstract void reset();

  public abstract void suspend();

  public abstract void suspend(int millisToResume, State stateToResume);

  public abstract void resume();

  public abstract void resume(int millisToResume, State stateToResume);

  public abstract void destroy();
}
