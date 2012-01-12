package com.ooyala.android.player;

import java.util.Observable;

import android.view.SurfaceView;
import android.view.View;

import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OoyalaPlayer.OoyalaPlayerState;

public abstract class Player extends Observable {
  protected OoyalaPlayerLayout _parent = null;
  protected OoyalaPlayerState _state = OoyalaPlayerState.OoyalaPlayerStateInit; /**< the current state of the player */
  protected int _playheadTime = 0; /**< KVO compatible playhead time */
  protected String _error = null; /**< The Player's current error if it exists */
  protected SurfaceView _view = null;

  /**
   * Init the player
   */
  protected Player() {
  }

  public void init(OoyalaPlayerLayout parent, Object param) {
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

  public void setState(OoyalaPlayerState state) {
    this._state = state;
    setChanged();
    notifyObservers();
  }

  public float getPlayheadTime() {
    return _playheadTime;
  }

  public void setPlayheadTime(int playheadTime) {
    this._playheadTime = playheadTime;
  }

  public String getError() {
    return _error;
  }

  public View getView() {
    return _view;
  }

  public void setParent(OoyalaPlayerLayout parent) {
    _parent = parent;
  }

  public abstract boolean suspend();

  public abstract boolean resume();
}
