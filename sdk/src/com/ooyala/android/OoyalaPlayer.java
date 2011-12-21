package com.ooyala.android;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * A wrapper around android.media.MediaPlayer
 * http://developer.android.com/reference/android/media/MediaPlayer.html
 *
 * For a list of Android supported media formats, see:
 * http://developer.android.com/guide/appendix/media-formats.html
 */
public class OoyalaPlayer extends RelativeLayout implements MediaPlayer.OnPreparedListener,
                                                            MediaPlayer.OnErrorListener,
                                                            MediaPlayer.OnCompletionListener//,
//                                     MediaPlayer.OnBufferingUpdateListener,
//                                     MediaPlayer.OnInfoListener,
//                                     MediaPlayer.OnSeekCompleteListener,
//                                     MediaPlayer.OnVideoSizeChangedListener
{
  private MediaPlayer _mediaPlayer = null;

  private ContentItem _rootItem = null;
  private Video _currentItem = null;
  private OoyalaException _currentError = null;

  public OoyalaPlayer(Context context)
  {
    super(context);
    createMediaPlayer();
  }

  public OoyalaPlayer(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    createMediaPlayer();
  }

  public OoyalaPlayer(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);
    createMediaPlayer();
  }

  public OoyalaPlayer(Context context, String embedCode)
  {
    super(context);
    createMediaPlayer();
    setEmbedCode(embedCode);
  }

  public OoyalaPlayer(Context context, AttributeSet attrs, String embedCode)
  {
    super(context, attrs);
    createMediaPlayer();
    setEmbedCode(embedCode);
  }

  public OoyalaPlayer(Context context, AttributeSet attrs, int defStyle, String embedCode)
  {
    super(context, attrs, defStyle);
    createMediaPlayer();
    setEmbedCode(embedCode);
  }

  private void createMediaPlayer()
  {
    _mediaPlayer = new MediaPlayer(); // Player Idle
    _mediaPlayer.setOnPreparedListener(this);
    _mediaPlayer.setOnErrorListener(this);
    _mediaPlayer.setOnCompletionListener(this);
// TODO: Implement all of these listeners:
//    _mediaPlayer.setOnBufferingUpdateListener(this);
//    _mediaPlayer.setOnInfoListener(this);
//    _mediaPlayer.setOnSeekCompleteListener(this);
//    _mediaPlayer.setOnVideoSizeChangedListener(this);
//    _mediaPlayer.setDisplay(holder);
    _mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
  }

  /** Called when MediaPlayer is ready */
  @Override
  public void onPrepared(MediaPlayer player)
  {
    if (true)
    {
      _mediaPlayer.start();
    }
  }

  @Override
  public boolean onError(MediaPlayer player, int what, int extra)
  {
      // ... react appropriately ...
      // The MediaPlayer has moved to the Error state, must be reset!
    System.out.println("Player Error");
    return true;
  }

  @Override
  public void onCompletion(MediaPlayer player)
  {
  }

  /**
   * The current movie.
   * @return movie
   */
  public Video getCurrentItem()
  {
	  return _currentItem;
  }

  /**
   * The embedded item (movie, channel, or channel set).
   * @return movie
   */
  public ContentItem getRootItem()
  {
	  return _rootItem;
  }

  /**
   * Get the embedCode for the current player.
   * @return embedCode
   */
  public String getEmbedCode()
  {
	  return _rootItem == null ? null : _rootItem.getEmbedCode();
  }

  /**
   * Reinitializes the player with a new embedCode.
   * If embedCode is null, this method has no effect and just returns.
   * @param embedCode
   */
  public void setEmbedCode(String embedCode)
  {
    if (embedCode == null) return;

    // Look up playback URL
    String url = "http://ak.c.ooyala.com/81MTVjMjq2PJ4s41-o_iNxP5uYAHQjPy/DOcJ-FxaFrRg4gtGEwOjkzOjBrO_9K4g";
    //String url = "http://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3";
    //String url = "http://www.tools4movies.com/dvd_catalyst_profile_samples/Harold%20Kumar%203%20Christmas%20bionic.mp4";

    // Play URL in MediaPlayer
    System.out.println("Callig setDataSource");
    try
    {
      _mediaPlayer.setDataSource(url); // Player Initialized
    }
    catch (Exception exception)
    {
      System.out.println("Unable to setDataSource: "+exception);
    }
    _mediaPlayer.prepareAsync(); // Player Preparing
  }

  /**
   * Set the current video in a channel if the video is present. Returns true if accepted, false if not.
   * @param embedCode
   * @return accepted
   */
  public boolean setCurrentItem(String embedCode)
  {
    Video requestedItem = null; // look up based on embed code
    // TODO: actually change what's playing
    _currentItem = requestedItem;
    return true;
  }


  /**
   * Get the current error code, if one exists
   * @return error code
   */
  public OoyalaException getError()
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
  public void pause()
  {

  }

  /**
   * Play the current video
   */
  public void play()
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
   * Synonym for seek.
   * @param time in milliseconds
   */
  public void setPlayheadTime(int timeInMillis)
  {
    seek(timeInMillis);
  }

  /**
   * Move the playhead to a new location in seconds with millisecond accuracy
   * @param time in milliseconds
   */
  public void seek(int timeInMillis)
  {
  }

  //Closed Captions
  //@todo
//  public List<String> getCurrentItemClosedCaptionsLanguages();
//  public void setClosedCaptionsLanguage(String language);
//  public void getTimedText(float startTime, float endTime);

}
