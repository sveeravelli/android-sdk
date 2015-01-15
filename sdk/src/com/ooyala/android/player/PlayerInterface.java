package com.ooyala.android.player;

import com.ooyala.android.OoyalaPlayer.State;

/**
 * The interface that must be implemented in order to receive control events
 * from Ooyala UI
 * 
 * 
 */
public interface PlayerInterface {
  /**
   * This is called when pause is clicked
   */
  public void pause();

  /**
   * This is called when play is clicked
   */
  public void play();

  /**
   * This is called when stop is clicked
   */
  public void stop();

  /**
   * @return current time
   */
  public int currentTime();

  /**
   * @return duration.
   */
  public int duration();

  /**
   * @return the buffer percentage (between 0 and 100 inclusive)
   */
  public int buffer();

  /**
   * @return true if the current player is seekable, false if there is no
   *         current player or it is not seekable
   */
  public boolean seekable();

  /**
   * Move the playhead to a new location in seconds with millisecond accuracy
   * 
   * @param timeInMillis
   *          time in milliseconds
   */
  public void seekToTime(int timeInMillis);

  /**
   * This returns the player state
   * 
   * @return the state
   */
  public State getState();
  
  public int livePlayheadPercentage();
  
  public void seekToPercentLive(int percent);
}
