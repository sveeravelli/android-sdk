package com.ooyala.android;

import java.util.Observable;

import com.ooyala.android.OoyalaPlayer.State;

import android.view.SurfaceView;
import android.view.View;


abstract class Player extends Observable {
  protected OoyalaPlayer _parent = null;
  protected State _state = State.INIT; /** the current state of the player */
  protected String _error = null; /** The Player's current error if it exists */
  protected SurfaceView _view = null;
  protected boolean _resizeQueued = false;
  protected int _buffer = 0;
  protected boolean _fullscreen = false;
  protected boolean _seekable = true;
  protected boolean _pausable = true;

  /**
   * Init the player
   */
  protected Player() {
  }

  public void init(OoyalaPlayer parent, Object param) {
  }

  /**
   * Pause the current video
   */
  public void pause() {
  }

  /**
   * Play the current video
   */
  public void play() {
  }

  /**
   * Stop playback, remove listeners
   */
  public void stop() {
  }

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
   * Returns whether this player is seekable
   * @return true if seekable, false if not
   */
  public boolean seekable() {
    return _seekable;
  }

  /**
   * Set whether this player is seekable
   * @param seekable true if seekable, false if not
   */
  public void setSeekable(boolean seekable) {
    _seekable = seekable;
  }

  /**
   * Set the current playhead time of the player
   * @param timeInMillis int millis to set the playhead time to
   */
  public void seekToTime(int timeInMillis) {
  }

  public State getState() {
    return _state;
  }

  protected void setState(State state) {
    this._state = state;
    setChanged();
    notifyObservers(OoyalaPlayer.STATE_CHANGED_NOTIFICATION);
  }

  public String getError() {
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

  public abstract void reset();

  public abstract void suspend();

  public abstract void suspend(int millisToResume, State stateToResume);

  public abstract void resume();

  public abstract void destroy();
}
