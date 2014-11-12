package com.ooyala.android;

import java.util.HashSet;
import java.util.Set;

/**
 * thread safe queue of collections of bytes.
 */
public class ID3TagNotifier {

  public static final ID3TagNotifier s_instance = new ID3TagNotifier();

  public static interface ID3TagNotifierListener {
    /**
     * Not guaranteed to be called on the main UI thread.
     */
    void onTag( byte[] tag );
  }

  private final Set<ID3TagNotifierListener> listeners;

  public ID3TagNotifier() {
    this.listeners = new HashSet<ID3TagNotifierListener>();
  }

  public void addListener( ID3TagNotifierListener listener ) {
    synchronized( listeners ) {
      listeners.add( listener );
    }
  }

  public void removeListener( ID3TagNotifierListener listener ) {
    synchronized( listeners ) {
      listeners.remove( listener );
    }
  }

  public void onTag( byte[] tag ) {
    synchronized( listeners ) {
      for( ID3TagNotifierListener l : listeners ) {
        l.onTag( tag );
      }
    }
  }
}
