package com.ooyala.android;

import android.util.Log;

public final class DebugMode {

  public enum Mode {
    None,
    LogOnly,
    LogAndAbort,
  }

  private DebugMode() {}

  private static Mode mode = Mode.LogOnly;

  public static Mode getMode() {
    return DebugMode.mode;
  }

  public static void setMode( Mode mode ) {
    DebugMode.mode = mode;
  }

  public static void logI( String tag, String message ) {
    if( DebugMode.mode != Mode.None ) { Log.i( tag, message ); }
  }
  public static void logI( String tag, String message, Throwable t ) {
    if( DebugMode.mode != Mode.None ) { Log.i( tag, message + ", exception=" + t.getMessage() ); }
  }

  public static void logD( String tag, String message ) {
    if( DebugMode.mode != Mode.None ) { Log.d( tag, message ); }
  }
  public static void logD( String tag, String message, Throwable t ) {
    if( DebugMode.mode != Mode.None ) { Log.d( tag, message + ", exception=" + t.getMessage() ); }
  }

  public static void logV( String tag, String message ) {
    if( DebugMode.mode != Mode.None ) { Log.v( tag, message ); }
  }
  public static void logV( String tag, String message, Throwable t ) {
    if( DebugMode.mode != Mode.None ) { Log.v( tag, message + ", exception=" + t.getMessage() ); }
  }

  public static void logE( String tag, String message ) {
    if( DebugMode.mode != Mode.None ) { Log.e( tag, message ); }
  }
  public static void logE( String tag, String message, Throwable t ) {
    if( DebugMode.mode != Mode.None ) { Log.e( tag, message + ", exception=" + t.getMessage() ); }
  }

  public static void assertCondition( boolean condition, String tag, String message ) {
    switch( getMode() ) {
    case None:
      break;
    case LogOnly:
      Log.e( tag, message );
      break;
    case LogAndAbort:
      Log.e( tag, message );
      System.exit( -1 );
      break;
    }
  }

  public static void assertFail( String tag, String message ) {
    DebugMode.assertCondition( false, tag, message );
  }
}
