package com.ooyala.android;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;

import android.util.Log;

public final class WidevineStuckMonitor implements Observer {

  public interface Listener {
    /**
     * When this Listener callback is invoked, the WidevineStuckMonitor
     * will not detect any further freezes until reset() is called on it.
     */
    void onFrozen();
  }

  private static final String TAG = "WidevineStuckMonitor";
  private static final int END_TIME_WINDOW_MILLISECONDS = 15*1000; // empirical heuristic!
  private static final int MAX_FREEZE_MILLISECONDS = 2*1000; // empirical heuristic!
  private final OoyalaPlayer ooyalaPlayer;
  private final Player drmPlayer;
  private final Listener listener;
  private final int monitorAfterMsec;
  private VideoAtWallMsec lastRecord;
  private final AtomicBoolean onFrozenSent;

  public WidevineStuckMonitor( final OoyalaPlayer ooyalaPlayer, final Player drmPlayer, final Listener listener ) {
    this.ooyalaPlayer = ooyalaPlayer;
    this.drmPlayer = drmPlayer;
    this.listener = listener;
    this.onFrozenSent = new AtomicBoolean();

    // note: calculating this only once assumes the duration doesn't change during playback.
    // note: assumes getCurrentItem() matches the streams being used by the WidevineOsPlayer.
    final Integer oi = calculateMonitorAfterMsec( ooyalaPlayer.getCurrentItem() );
    if( oi != null ) {
      this.ooyalaPlayer.addObserver( this );
      this.monitorAfterMsec = oi.intValue();
      Log.v( TAG, "Constructor(): enabled, monitorAfterMsec=" + monitorAfterMsec );
    }
    else {
      this.monitorAfterMsec = Integer.MAX_VALUE;
      Log.v( TAG, "Constructor(): disabled, monitorAfterMsec=" + monitorAfterMsec );
    }
  }

  public void reset() {
    Log.v( TAG, "reset" );
    ooyalaPlayer.addObserver( this );
    onFrozenSent.set( false );
  }

  public void destroy() {
    ooyalaPlayer.deleteObserver( this );
  }

  private Integer calculateMonitorAfterMsec( final Video video ) {
    Integer oi = null;
    if( video != null ) {
      int duration = video.getDuration();
      if( duration > END_TIME_WINDOW_MILLISECONDS ) {
        oi = Math.max( 0, duration - END_TIME_WINDOW_MILLISECONDS );
      }
    }
    Log.v( TAG, "calculaeMonitorAfterMsec(): duration=" + video.getDuration() + ", oi=" + oi );
    return oi;
  }

  public void update( Observable o, Object arg ) {
    final String notification = arg.toString();
    if( drmPlayer.isPlaying() && notification.equals( OoyalaPlayer.TIME_CHANGED_NOTIFICATION ) ) {
      checkWhilePlaying();
    }
  }

  private void checkWhilePlaying() {
    final int videoMsec = drmPlayer.currentTime();
    if( videoMsec >= monitorAfterMsec ) {
      checkInWindow( videoMsec );
    }
  }

  private void checkInWindow( final int videoMsec ) {
    // regular playing, or ffwd, or rew:
    if( lastRecord == null || videoMsec != lastRecord.videoMsec ) {
      updateLastRecord( videoMsec );
    }
    // video playhead time is stuck:
    else {
      checkFrozen();
    }
  }

  private void updateLastRecord( final int videoMsec ) {
    lastRecord = new VideoAtWallMsec( videoMsec );
  }

  private void checkFrozen() {
    if( lastRecord != null ) {
      final long wallNow = System.currentTimeMillis();
      final long wallDelta = wallNow - lastRecord.wallMsec;
      if( wallDelta >= MAX_FREEZE_MILLISECONDS ) {
        Log.v( TAG, "doFreezeCheck(): looks frozen to me!" );
        sendOnFrozen();
      }
    }
  }

  private void sendOnFrozen() {
    if( onFrozenSent.compareAndSet( false, true ) ) {
      Log.v( TAG, "sendOnFrozen(): sending" );
      ooyalaPlayer.deleteObserver( this );
      listener.onFrozen();
    }
  }

  // ---------- helpers ----------

  private static final class VideoAtWallMsec {
    public final int videoMsec;
    public final long wallMsec;
    public VideoAtWallMsec( final int videoMsec ) {
      this.videoMsec = videoMsec;
      this.wallMsec = System.currentTimeMillis();
    }
    public String toString() {
      return "[" + getClass().getSimpleName() + ":videoMsec=" + videoMsec + ",wallMsec=" + wallMsec + "]";
    }
  }

}