package com.ooyala.android;

import com.ooyala.android.ID3TagNotifier.ID3TagNotifierListener;

public class NielsenAnalytics implements ID3TagNotifierListener {

  private static final String TAG = "NielsenAnalytics";

  private long lastPlayhead;

  public NielsenAnalytics() {
    ID3TagNotifier.s_instance.addWeakListener( this );
    this.lastPlayhead = Long.MIN_VALUE;
  }

  public void onTag( byte[] tag ) {
    final String tagStr = new String(tag);
    DebugMode.logV( TAG, "onTag(): tagStr=" + tagStr );
    if( tagStr.contains("www.nielsen.com") ) {
      final String nielsenStr = tagStr.replaceFirst( ".*www.nielsen.com", "" );
      DebugMode.logV( TAG, "onTag(): nielsenStr=" + nielsenStr );
    }
  }

  public void onPlay() {
    DebugMode.logV( TAG, "onPlay()" );
  }

  public void onStop() {
    DebugMode.logV( TAG, "onStop()" );
  }

  public void onPlayheadUpdate( long playhead ) {
    DebugMode.logV( TAG, "onPlayheadUpdate(): playhead=" + playhead );
    if( playhead - lastPlayhead > 2000 ) {
      lastPlayhead = playhead;
      DebugMode.logV( TAG, "onPlayheadUpdate(): updating" );
    }
  }
}
