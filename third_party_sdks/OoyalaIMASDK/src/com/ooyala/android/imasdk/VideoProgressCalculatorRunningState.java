package com.ooyala.android.imasdk;

public class VideoProgressCalculatorRunningState {
  private boolean _isPlayingIMAAd;
  private int _liveContentTimePlayed;
  private int _lastPausedMs;

  public VideoProgressCalculatorRunningState( boolean isPlayingIMAAd, int liveContentTimePlayed, int lastPausedMs ) {
    super();
    this._isPlayingIMAAd = isPlayingIMAAd;
    this._liveContentTimePlayed = liveContentTimePlayed;
    this._lastPausedMs = lastPausedMs;
  }
  public boolean isPlayingIMAAd() { return _isPlayingIMAAd; }
  public void setPlayingIMAAd(boolean _isPlayingIMAAd) { this._isPlayingIMAAd = _isPlayingIMAAd; }
  public int getLiveContentTimePlayed() { return _liveContentTimePlayed; }
  public void setLiveContentTimePlayed(int _liveContentTimePlayed) { this._liveContentTimePlayed = _liveContentTimePlayed; }
  public int getLastPausedMs() { return _lastPausedMs; }
  public void setLastPausedMs(int _lastPausedMs) { this._lastPausedMs = _lastPausedMs; }
  public void incrementLiveContentTimePlayed( int ms ) { this._liveContentTimePlayed += ms; }
}