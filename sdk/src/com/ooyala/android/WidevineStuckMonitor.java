package com.ooyala.android;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;

import android.os.Environment;
import android.util.Log;

public final class WidevineStuckMonitor implements Observer {
  
  public interface Listener {
    void onFrozen();
  }
  
  private static final String TAG = "WidevineStuckMonitor";
  private static final int END_TIME_WINDOW_MILLISECONDS = 7000; // empirical heuristic!
  private static final int MAX_FREEZE_MILLISECONDS = 3000; // empirical heuristic!
  private final LogVToFile logger;
  private final Player drmPlayer;
  private final Listener listener;
  private final int monitorAfterMsec;
  private VideoAtWallMsec lastRecord;
  private final AtomicBoolean onFrozenSent;
  
  public WidevineStuckMonitor( final OoyalaPlayer ooyalaPlayer, final Player drmPlayer, final Listener listener ) {
    this.logger = new LogVToFile();
    this.drmPlayer = drmPlayer;
    this.listener = listener;
    this.onFrozenSent = new AtomicBoolean();
    
    // calculating this only once assumes the duration doesn't change during playback.
    final Integer oi = calculateMonitorAfterMsec( ooyalaPlayer.getCurrentItem() );
    if( oi != null ) {
      ooyalaPlayer.addObserver( this );
      this.monitorAfterMsec = oi.intValue();
      logger.logV( TAG, "Constructor(): enabled, monitorAfterMsec=" + monitorAfterMsec );
    }
    else {
      this.monitorAfterMsec = Integer.MAX_VALUE;
      logger.logV( TAG, "Constructor(): disabled, monitorAfterMsec=" + monitorAfterMsec );
    }
  }
  
  // todo: double check / really figure out when the WidevineOsPlayer would best reset us.
  // probably when the player State changes to anything other than
  // the COMPLETE state it goes into onFrozen()?
  public void reset() {
    Log.v( TAG, "reset" );
    onFrozenSent.set( false );
  }
  
  private Integer calculateMonitorAfterMsec( final Video video ) {
    Integer oi = null;
    if( video != null ) {
      int duration = video.getDuration();
      if( duration > 0 ) {
        // debatable: non-zero, even small, durations cause us to have a monitoring window.
        oi = Math.max( 0, duration - END_TIME_WINDOW_MILLISECONDS );
      }
      // debatable: non-positive duration means don't have a monitoring window.
    }
    logger.logV( TAG, "calculaeMonitorAfterMsec(): duration=" + video.getDuration() + ", oi=" + oi );
    return oi;
  }
  
  public void update( Observable o, Object arg ) {
    final String notification = arg.toString();
    Log.v( TAG, "update(): isPlaying=" + drmPlayer.isPlaying() + ", notification=" + notification );
    if( drmPlayer.isPlaying() && notification.equals( OoyalaPlayer.TIME_CHANGED_NOTIFICATION ) ) {
      checkWhilePlaying();
    }
  }
  
  private void checkWhilePlaying() {
    final int videoMsec = drmPlayer.currentTime();
    Log.v( TAG, "checkWhilePlaying(): videoMsec=" + videoMsec + ", monitorAfterMsec=" + monitorAfterMsec );
    if( videoMsec >= monitorAfterMsec ) {
      checkInWindow( videoMsec );
    }
  }
  
  private void checkInWindow( final int videoMsec ) {
    logger.logV( TAG, "checkInWindow(): videoMsec=" + videoMsec + ", duration=" + drmPlayer.duration() + ", monitorAfterMsec=" + monitorAfterMsec + ", lastRecord=" + lastRecord );
    // regular playing, or ffwd, or rew:
    if( lastRecord == null ) {
      updateLastRecord( videoMsec );
    }
    else if( videoMsec != lastRecord.videoMsec ) {
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
      logger.logV( TAG, "doFreezeCheck(): wallDelta=" + wallDelta + ", lastRecord=" + lastRecord + ", wallNow=" + wallNow );
      if( wallDelta >= MAX_FREEZE_MILLISECONDS ) {
        logger.logV( TAG, "doFreezeCheck(): looks frozen to me!" );
        sendOnFrozen();
      }
    }
  }
  
  private void sendOnFrozen() {
    if( onFrozenSent.compareAndSet( false, true ) ) {
      logger.logV( TAG, "sendOnFrozen(): sending" );
      listener.onFrozen();
    }
  }

  // ---------- helpers ----------
  
  private static final class LogVToFile {
    private static final String TAG = "LogVToFile";
    private OutputStream stream;    
    public LogVToFile() {
      if( isExternalStorageWritable() ) {
        final File file = getFullPath( "WidevineStuckMonitor.log" );
        try {
          stream = new BufferedOutputStream( new FileOutputStream( file ) );
        }
        catch( FileNotFoundException fnfe ) {
          stream = null;
        }
      }
    }

    public void logV( String tag, String msg ) {
      Log.v( tag, msg );
      if( stream != null ) {
        try {
          stream.write( tag.getBytes() );
          stream.write( ": ".getBytes() );
          stream.write( msg.getBytes() );
          stream.write( System.getProperty("line.separator").getBytes() );
          stream.flush();
        }
        catch( IOException ioe ) {
          Log.e( TAG, "failed to write/flush: tag=" + tag + ", msg=" + msg );
        }
      }
    }
    
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
    
    private static File getFullPath(final String fileName) {
      final File dir = Environment.getExternalStoragePublicDirectory(/*whatever!*/Environment.DIRECTORY_DOWNLOADS);
      final File file = new File(dir, fileName);
      if (!file.getParentFile().mkdirs()) {
          Log.e( TAG, "Directory " + dir + "/" + fileName + " not created");
      }
      return file;
    }
  }
  
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