package com.ooyala.android;

import android.media.MediaPlayer;

/**
 * A wrapper around android.media.MediaPlayer
 * http://developer.android.com/reference/android/media/MediaPlayer.html
 *
 * For a list of Android supported media formats, see:
 * http://developer.android.com/guide/appendix/media-formats.html
 */
public class OoyalaPlayer {

  private MediaPlayer _mediaPlayer = null;

  private ContentItem _item = null;
  private Movie _currentItem = null;
  private OoyalaError _currentError = null;

  public OoyalaPlayer(String embedCode)
  {
    _mediaPlayer = new MediaPlayer();
  }

  /**
   * The current movie.
   * @return movie
   */
  public Movie getCurrentItem()
  {
	  return _currentItem;
  }

  /**
   * The embedded item (movie, channel, or channel set).
   * @return movie
   */
  public ContentItem getItem()
  {
	  return _item;
  }

  /**
   * Get the embedCode for the current player
   * @return embedCode
   */
  public String getEmbedCode()
  {
	  return _item == null ? null : _item.getEmbedCode();
  }

  /**
   * Reinitializes the player with a new embedCode.
   * @param embedCode
   */
  public void setEmbedCode(String embedCode)
  {
    // TODO
  }

  /**
   * Set the current video in a channel if the video is present. Returns true if accepted, false if not.
   * @param embedCode
   * @return accepted
   */
  public boolean changeCurrentItem(String embedCode)
  {
    Movie requestedItem = null; // look up based on embed code
    // TODO: actually change what's playing
    _currentItem = requestedItem;
    return true;
  }


  /**
   * Get the current error code, if one exists
   * @return error code
   */
  public OoyalaError getError()
  {
    return _currentError;
  }

  /**
   * Get current player state. One of playing, paused, buffering, channel, or error
   * @return state
   */
  public String getState()
  {
    return "TODO";
  }

  /**
   * Pause the current video
   */
  public void pauseMovie()
  {

  }

  /**
   * Play the current video
   */
  public void playMovie()
  {

  }

  /**
   * Returns true if in fullscreen mode, false if not
   * @return fullscreen mode
   */
  public boolean getFullscreen()
  {
    return false;
  }

  /**
   * Find where the playhead is with millisecond accuracy
   * @return time in seconds
   */
  public int getPlayheadTime()
  {
    return 0;
  }

  /**
   * Move the playhead to a new location in seconds with millisecond accuracy
   * @param time in seconds
   */
  public void setPlayheadTime(int time)
  {
    seek(time);
  }

  public void seek(int time)
  {
  }

  //Closed Captions
  //@todo
//  public List<String> getCurrentItemClosedCaptionsLanguages();
//  public void setClosedCaptionsLanguage(String language);
//  public void getTimedText(float startTime, float endTime);

}
