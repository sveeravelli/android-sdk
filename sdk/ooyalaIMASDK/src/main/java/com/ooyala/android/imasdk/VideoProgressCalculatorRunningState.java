package com.ooyala.android.imasdk;

/**
 * This class saves the states related to the IMA Ads playback
 */

public class VideoProgressCalculatorRunningState {
  private boolean _isPlayingIMAAd;
  private int _lastPausedMs;
  private int _liveContentTimePlayed;

  /**
   * Initialize a VideoProgressCalculatorRunningState with given params
   * @param isPlayingIMAAd true if a IMA Ad is playing
   * @param lastPausedMs the last time that the content is paused in millisecond
   * @param liveContentTimePlayed the playhead time of live content in millisecond
   */
  public VideoProgressCalculatorRunningState( boolean isPlayingIMAAd, int lastPausedMs, int liveContentTimePlayed ) {
    super();
    this._isPlayingIMAAd = isPlayingIMAAd;
    this._lastPausedMs = lastPausedMs;
    this._liveContentTimePlayed = liveContentTimePlayed;
  }

  /**
   *
   * @return true if a IMA ad is playing, false otherwise
   */
  public boolean isPlayingIMAAd() {
    return _isPlayingIMAAd;
  }

  /**
   * Set the private filed _isPlayingIMAAd to the given param
   * @param _isPlayingIMAAd given value for _isPlayingIMAAd
   */
  public void setPlayingIMAAd(boolean _isPlayingIMAAd) {
    this._isPlayingIMAAd = _isPlayingIMAAd;
  }

  /**
   * Fetch the time when the content is paused last time
   * @return the time that content paused last time in millisecond
   */
  public int getLastPausedMs() {
    return _lastPausedMs;
  }

  /**
   * Set the time when the content is paused last time to the given time
   * @param _lastPausedMs the given time in millisecond
   */
  public void setLastPausedMs(int _lastPausedMs) {
    this._lastPausedMs = _lastPausedMs;
  }

  /**
   * Fetch playhead time of the current Live content
   * @return the playhead time of the current live content in millisecond
   */
  public int getLiveContentTimePlayed() {
    return _liveContentTimePlayed;
  }

  /**
   * Set played time of the current Live content to the given time
   * @param _liveContentTimePlayed the played time of the current Live in millisecond
   */
  public void setLiveContentTimePlayed(int _liveContentTimePlayed) {
    this._liveContentTimePlayed = _liveContentTimePlayed;
  }

  /**
   * Increment the played time of the current Live content by the given time
   * @param ms time to be added to played time of the current Live content in millisecond
   */
  public void incrementLiveContentTimePlayed(int ms) {
    this._liveContentTimePlayed += ms;
  }
}