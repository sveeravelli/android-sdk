package com.ooyala.android;

import java.util.concurrent.atomic.AtomicBoolean;

import android.test.AndroidTestCase;

public class ID3TagNotifierTest extends AndroidTestCase {

  public void test_listen_for_1() {
    final AtomicBoolean seen = new AtomicBoolean();
    final ID3TagNotifier.ID3TagNotifierListener l = new ID3TagNotifier.ID3TagNotifierListener() {
      @Override
      public void onTag( byte[] tag ) {
        seen.set( true );
      }
    };
    final ID3TagNotifier q = new ID3TagNotifier();
    q.addWeakListener( l );
    q.onTag( new byte[]{} );
    assertTrue( seen.get() );
  }

}