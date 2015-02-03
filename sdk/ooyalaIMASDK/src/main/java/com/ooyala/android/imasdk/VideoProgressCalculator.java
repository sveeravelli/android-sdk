package com.ooyala.android.imasdk;

import android.util.Pair;

import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.ooyala.android.DebugMode;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;

public class VideoProgressCalculator {

  private final static String TAG = VideoProgressCalculator.class.getSimpleName();

  private static boolean s_debug = false; // set this to true e.g. in the debugger to generate logs.
  private static void logV( String message ) { if(s_debug) { DebugMode.logV( TAG, message ); } }

  private OoyalaPlayer _player;
  private VideoProgressCalculatorRunningState _runningState;

  public VideoProgressCalculator( OoyalaPlayer player, boolean isPlayingIMAAd, int lastPausedMs, int liveContentTimePlayed ) {
    this._player = player;
    this._runningState = new VideoProgressCalculatorRunningState( isPlayingIMAAd, lastPausedMs, liveContentTimePlayed );
  }

  public VideoProgressCalculatorRunningState getRunningState() {
    return _runningState;
  }

  public VideoProgressUpdate getContentProgress() {
    final boolean isContent = ! _player.isAdPlaying();
    logV( "getContentProgress(): isContent=" + isContent );
    final VideoProgressUpdate vpu = isContent ? calculateContentProgress() : VideoProgressUpdate.VIDEO_TIME_NOT_READY;
    logV( "getContentProgress(): " + vpu );
    return vpu;
  }

  private VideoProgressUpdate calculateContentProgress() {
    Pair<Integer, Integer> playheadAndDuration = calculateBasicPlayheadAndDuration();
    final int playheadMs = playheadAndDuration.first;
    final int durationMs = playheadAndDuration.second;
    logV( "calculateContentProgress(): playheadAndDuration=" + playheadAndDuration );
    if( durationMs == 0 ) {
      return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
    }
    else {
      return calculateReadyContentProgress( playheadMs, durationMs );
    }
  }

  private VideoProgressUpdate calculateReadyContentProgress( int playheadMs, int durationMs ) {
    final boolean isPaused = _player.getState() == State.PAUSED;
    logV( "calculateReadyContentProgress(): isPaused=" + isPaused );
    playheadMs = _runningState.getLiveContentTimePlayed() + (isPaused ? _runningState.getLastPausedMs() : playheadMs);
    logV( "calculateReadyContentProgress(): playheadMs=" + playheadMs );
    _runningState.setLastPausedMs( playheadMs );
    return new VideoProgressUpdate( playheadMs, durationMs );
  }

  public VideoProgressUpdate getAdProgress() {
    final boolean isIMAad = _player.isAdPlaying() && _runningState.isPlayingIMAAd();
    final VideoProgressUpdate vpu = isIMAad ? calculateAdProgress() : VideoProgressUpdate.VIDEO_TIME_NOT_READY;
    logV( "getAdProgress(): " + vpu );
    return vpu;
  }

  private VideoProgressUpdate calculateAdProgress() {
    Pair<Integer, Integer> playheadAndDuration = calculateBasicPlayheadAndDuration();
    final int playheadMs = playheadAndDuration.first;
    final int durationMs = playheadAndDuration.second;
    return playheadAndDuration.first == 0 ?
        VideoProgressUpdate.VIDEO_TIME_NOT_READY :
          new VideoProgressUpdate( playheadMs, durationMs );
  }

  private PairToString<Integer, Integer> calculateBasicPlayheadAndDuration() {
    int playheadMs = 0;
    int durationMs = 0;
    switch( _player.getState() ) {
    case READY:
    case PLAYING:
      playheadMs = _player.getPlayheadTime();
      durationMs = _player.getDuration();
      break;
    default:
      break;
    }
    return new PairToString<Integer, Integer>( playheadMs, durationMs );
  }

  private static final class PairToString<A,B> extends Pair<A,B> {
    public PairToString( A a, B b ) { super( a, b ); }
    public String toString() { return getClass().getSimpleName() + "@" + hashCode() + ":" + first.toString() + "," + second.toString(); }
  }
}
