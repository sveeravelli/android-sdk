package com.ooyala.android;

import java.util.List;

/**
 * A wrapper around android.media.MediaPlayer
 * @author chrisl
 *
 */
public interface OOMediaPlayer {

  /**
   * Get a movie describing the current video.
   * @return movie
   */
  public Movie getCurrentItem();

  /**
   * set the embedCode for the current video in a channel
   * @param embedCode
   */
  public void setCurrentItemEmbedCode(String embedCode);

  /**
   * Get a movie describing the embedded item.
   * @return movie
   */
  public Movie getItem();
  
  /**
   * Get the embedCode for the current player
   * @return embedCode
   */
  public String getEmbedCode();

  /**
   * Set the embedCode for the current player
   * @param embedCode
   */
  public void setEmbedCode(String embedCode);
  
  /**
   * Get a list of objects describing the current channel
   * @return list
   */
  public List<Movie> getLineup();
  
  /**
   * Set the current video in a channel if the video is present. Returns true if accepted, false if not.
   * @param embedCode
   * @return accepted
   */
  public boolean changeCurrentItem(String embedCode);

  
  /**
   * Get the current error code, if one exists
   * @return error code
   */
  public int getErrorCode();
  
  /**
   * Get the current text of the error, if one exists
   * @return error text
   */
  public String getErrorText();
  
  /**
   * Get current player state. One of playing, paused, buffering, channel,or error
   * @return state
   */
  public String getState();
  
  /**
   * Pause the current video
   */
  public void pauseMovie();
  
  /**
   * Play the current video
   */
  public void playMovie();
  
  /**
   * Skip the current ad. (Note that this method will throw an exception if no ads are associated with the current embed code.)
   */
  public void skipAd();
  
  
  /**
   * Returns true if in fullscreen mode, false if not
   * @return fullscreen mode
   */
  public boolean getFullscreen();
  
  /**
   * Find where the playhead is with millisecond accuracy
   * @return time in seconds
   */
  public float getPlayheadTime();
  
  /**
   * Move the playhead to a new location in seconds with millisecond accuracy
   * @param time in seconds
   */
  public void setPlayheadTime(float time);

  //Closed Captions
  //@todo
  public List<String> getCurrentItemClosedCaptionsLanguages();
  public void setClosedCaptionsLanguage(String language);
  public void getTimedText(float startTime, float endTime);

  
}
