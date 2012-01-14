package com.ooyala.android.player;

import java.util.Observable;

import android.view.SurfaceView;
import android.view.View;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.OoyalaPlayerState;

public abstract class Player extends Observable {
  protected OoyalaPlayer _parent = null;
  protected OoyalaPlayerState _state = OoyalaPlayerState.INIT; /**< the current state of the player */
  protected String _error = null; /**< The Player's current error if it exists */
  protected SurfaceView _view = null;
  protected boolean _resizeQueued = false;
  protected int _buffer = 0;

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
   * @returns the current playhead time in milliseconds as an int
   */
  public int currentTime() {
    return 0;
  }

  /**
   * Get the current item's duration
   * @returns duration in milliseconds as an int
   */
  public int duration() {
    return 0;
  }

  /**
   * Get the current item's buffer
   * @returns buffer+played percentage as an int
   */
  public int buffer() {
    return 0;
  }

  /**
   * Returns whether this player is pauseable
   * @returns A boolean value specifiying whether the player is pauseable
   */
  public boolean pauseable() {
    return true;
  }

  /**
   * Returns whether this player is seekable
   * @returns A boolean value specifiying whether the player is seekable
   */
  public boolean seekable() {
    return true;
  }

  /**
   * Set the current playhead time of the player
   * @param[time] int millis to set the playhead time to
   */
  public void seekToTime(int timeInMillis) {
  }

  public OoyalaPlayerState getState() {
    return _state;
  }

  protected void setState(OoyalaPlayerState state) {
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

  public abstract void suspend();

  public abstract void resume();

  public abstract void destroy();
}
