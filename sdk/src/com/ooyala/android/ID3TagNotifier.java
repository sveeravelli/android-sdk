package com.ooyala.android;

import java.util.HashSet;
import java.util.Set;

/**
 * thread safe queue of collections of bytes.
 */
public class ID3TagNotifier {

  private static final ID3TagNotifier s_instance = new ID3TagNotifier();
  public static final ID3TagNotifier s_getInstance() { return s_instance; }

  public static interface ID3TagNotifierListener {
    /**
     * Not guaranteed to be called on the main UI thread.
     */
    void onTag( byte[] tag );
  }

  private final Set<WeakReferencePassThroughEquals<ID3TagNotifierListener>> listeners;

  public ID3TagNotifier() {
    this.listeners = new HashSet<WeakReferencePassThroughEquals<ID3TagNotifierListener>>();
  }

  public void addWeakListener( ID3TagNotifierListener listener ) {
    synchronized( listeners ) {
      listeners.add( new WeakReferencePassThroughEquals<ID3TagNotifierListener>(listener) );
    }
  }

  public void removeWeakListener( ID3TagNotifierListener listener ) {
    synchronized( listeners ) {
      listeners.remove( new WeakReferencePassThroughEquals<ID3TagNotifierListener>(listener) );
    }
  }

  public void onTag( byte[] tag ) {
    synchronized( listeners ) {
      for( WeakReferencePassThroughEquals<ID3TagNotifierListener> wl : listeners ) {
        final ID3TagNotifierListener l = wl.get();
        if( l != null ) {
          l.onTag( tag );
        }
      }
    }
  }
}
