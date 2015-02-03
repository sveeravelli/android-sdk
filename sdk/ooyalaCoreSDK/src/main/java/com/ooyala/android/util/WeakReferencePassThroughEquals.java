package com.ooyala.android.util;

import java.lang.ref.WeakReference;

public final class WeakReferencePassThroughEquals<T> extends WeakReference<T> {

  public WeakReferencePassThroughEquals( T t ) {
    super( t );
  }

  @Override
  public boolean equals( Object other ) {
    boolean e = false;
    if( other instanceof WeakReferencePassThroughEquals<?> ) {
      Object a = this.get();
      Object b = ((WeakReferencePassThroughEquals<?>)other).get();
      if( a != null ) { return a.equals(b); }
      if( b != null ) { return b.equals(a); }
      assert( a == null );
      assert( b == null );
      return true;
    }
    return e;
  }

}