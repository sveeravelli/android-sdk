package com.ooyala.android.player;

import java.net.URL;

import com.ooyala.android.OoyalaPlayer.PlayerState;

public abstract class Player {
  protected PlayerState _state; /**< the current state of the player */
  protected int _playheadTime; /**< KVO compatible playhead time */
  protected String _error; /**< The Player's current error if it exists */

  /**
   * Init the player
   */
  protected Player(URL url) {
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

  public PlayerState getState() {
    return _state;
  }

  public void setState(PlayerState state) {
    this._state = state;
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
}
