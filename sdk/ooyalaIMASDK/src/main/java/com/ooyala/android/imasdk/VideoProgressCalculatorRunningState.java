package com.ooyala.android.imasdk;

public class VideoProgressCalculatorRunningState {
  private boolean _isPlayingIMAAd;
  private int _lastPausedMs;
  private int _liveContentTimePlayed;

  public VideoProgressCalculatorRunningState( boolean isPlayingIMAAd, int lastPausedMs, int liveContentTimePlayed ) {
    super();
    this._isPlayingIMAAd = isPlayingIMAAd;
    this._lastPausedMs = lastPausedMs;
    this._liveContentTimePlayed = liveContentTimePlayed;
  }

  public boolean isPlayingIMAAd() {
    return _isPlayingIMAAd;
  }

  public void setPlayingIMAAd(boolean _isPlayingIMAAd) {
    this._isPlayingIMAAd = _isPlayingIMAAd;
  }

  public int getLastPausedMs() {
    return _lastPausedMs;
  }

  public void setLastPausedMs(int _lastPausedMs) {
    this._lastPausedMs = _lastPausedMs;
  }

  public int getLiveContentTimePlayed() {
    return _liveContentTimePlayed;
  }

  public void setLiveContentTimePlayed(int _liveContentTimePlayed) {
    this._liveContentTimePlayed = _liveContentTimePlayed;
  }

  public void incrementLiveContentTimePlayed(int ms) {
    this._liveContentTimePlayed += ms;
  }
}