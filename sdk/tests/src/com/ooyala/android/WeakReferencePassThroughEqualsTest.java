package com.ooyala.android;

import java.util.ArrayList;
import java.util.List;

import android.test.AndroidTestCase;

public class WeakReferencePassThroughEqualsTest extends AndroidTestCase {

  public void test_same_object_equals() {
    Object anObject = new int[]{ 42 };
    WeakReferencePassThroughEquals<Object> a1 = new WeakReferencePassThroughEquals<Object>( anObject );
    WeakReferencePassThroughEquals<Object> a2 = new WeakReferencePassThroughEquals<Object>( anObject );
    assertEquals( a1, a2 );
  }

  public void test_different_objects_not_equals() {
    int[] anObject = new int[]{ 42 };
    int[] cloneObject = anObject.clone();
    WeakReferencePassThroughEquals<int[]> a1 = new WeakReferencePassThroughEquals<int[]>( anObject );
    WeakReferencePassThroughEquals<int[]> a2 = new WeakReferencePassThroughEquals<int[]>( cloneObject );
    assertFalse( a1.equals(a2) );
  }

  public void test_collection_membership_positive() {
    List<WeakReferencePassThroughEquals<?>> l = new ArrayList<WeakReferencePassThroughEquals<?>>();
    int[] anObject = new int[]{ 42 };
    WeakReferencePassThroughEquals<int[]> a1 = new WeakReferencePassThroughEquals<int[]>( anObject );
    l.add( a1 );
    assertTrue( l.contains(a1) );
  }

  public void test_collection_membership_negative() {
    List<WeakReferencePassThroughEquals<?>> l = new ArrayList<WeakReferencePassThroughEquals<?>>();
    int[] anObject = new int[]{ 42 };
    int[] cloneObject = anObject.clone();
    WeakReferencePassThroughEquals<int[]> a1 = new WeakReferencePassThroughEquals<int[]>( anObject );
    WeakReferencePassThroughEquals<int[]> a2 = new WeakReferencePassThroughEquals<int[]>( cloneObject );
    l.add( a1 );
    assertFalse( l.contains(a2) );
  }

}
