package com.ooyala.android.player;

import com.ooyala.android.plugin.StateNotifier;

/**
 * The interface that must be implemented in order to receive control events
 * from Ooyala UI
 * 
 * @author michael.len
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
   * This returns a state notifier object. Ooyala will listen to state changes
   * on this object to update UI controls correspondingly
   * 
   * @return the object to be listened. null if plug-in does not want to send
   *         notifications
   */
  public StateNotifier getStateNotifier();
}
