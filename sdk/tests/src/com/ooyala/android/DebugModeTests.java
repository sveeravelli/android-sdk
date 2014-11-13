package com.ooyala.android;

import android.test.AndroidTestCase;

public class DebugModeTests extends AndroidTestCase {

  public void test_assertEquals_true_returns() {
    DebugMode.setMode( DebugMode.Mode.LogOnly );
    final Integer a = new Integer( 42 );
    final Integer b = new Integer( 42 );
    final boolean c = DebugMode.assertEquals( a, b, "tag", "test" );
    assertTrue( c );
  }

  public void test_assertEquals_false_returns() {
    DebugMode.setMode( DebugMode.Mode.LogOnly );
    final Integer a = new Integer( 1024 );
    final Integer b = new Integer( 2048 );
    final boolean c = DebugMode.assertEquals( a, b, "tag", "test" );
    assertFalse( c );
  }

  public void test_assertCondition_true_returns() {
    DebugMode.setMode( DebugMode.Mode.LogOnly );
    final boolean c = DebugMode.assertCondition( true, "tag", "test" );
    assertTrue( c );
  }

  public void test_assertCondition_false_returns() {
    DebugMode.setMode( DebugMode.Mode.LogOnly );
    final boolean c = DebugMode.assertCondition( false, "tag", "test" );
    assertFalse( c );
  }

}
