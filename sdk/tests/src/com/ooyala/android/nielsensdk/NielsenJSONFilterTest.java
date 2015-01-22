package com.ooyala.android.nielsensdk;

import android.test.AndroidTestCase;

public class NielsenJSONFilterTest extends AndroidTestCase {

  private final String[] s_escapes = new String[]{ "\\b", "\\f", "\\n", "\\r", "\\t" };

  // ----------

  private void single_remove_test( String s ) {
    NielsenJSONFilter f = new NielsenJSONFilter();
    final String r = f.filter( s );
    assertEquals( "", r );
  }

  private void prefix_remove_test( String s ) {
    NielsenJSONFilter f = new NielsenJSONFilter();
    final String r = f.filter( s+"pdq" );
    assertEquals( "pdq", r );
  }

  private void midfix_remove_test( String s ) {
    NielsenJSONFilter f = new NielsenJSONFilter();
    final String r = f.filter( "xyz" + s + "pdq" );
    assertEquals( "xyzpdq", r );
  }

  private void postfix_remove_test( String s ) {
    NielsenJSONFilter f = new NielsenJSONFilter();
    final String r = f.filter( "xyz" + s );
    assertEquals( "xyz", r );
  }

  public void test_single_remove() {
    for( String s : s_escapes ) {
      single_remove_test( s );
    }
  }

  public void test_prefix_remove() {
    for( String s : s_escapes ) {
      prefix_remove_test( s );
    }
  }

  public void test_midfix_remove() {
    for( String s : s_escapes ) {
      midfix_remove_test( s );
    }
  }

  public void test_postfix_remove() {
    for( String s : s_escapes ) {
      postfix_remove_test( s );
    }
  }

  // ----------

  private void minimal_noharm_test( String s ) {
    final NielsenJSONFilter f = new NielsenJSONFilter();
    final String full = "\\" + s;
    final String r = f.filter( full );
    assertEquals( full, r );
  }

  private void prefix_noharm_test( String s ) {
    final NielsenJSONFilter f = new NielsenJSONFilter();
    final String full = "\\" + s + "pdq";
    final String r = f.filter( full );
    assertEquals( full, r );
  }

  private void midfix_noharm_test( String s ) {
    final NielsenJSONFilter f = new NielsenJSONFilter();
    final String full = "xyz\\" + s + "pdq";
    final String r = f.filter( full );
    assertEquals( full, r );
  }
  private void postfix_noharm_test( String s ) {
    final NielsenJSONFilter f = new NielsenJSONFilter();
    final String full = "xyz\\" + s;
    final String r = f.filter( full );
    assertEquals( full, r );
  }

  public void test_minimal_noharm() {
    for( String s : s_escapes ) {
      minimal_noharm_test( s );
    }
  }

  public void test_prefix_noharm() {
    for( String s : s_escapes ) {
      prefix_noharm_test( s );
    }
  }

  public void test_midfix_noharm() {
    for( String s : s_escapes ) {
      midfix_noharm_test( s );
    }
  }

  public void test_postfix_noharm() {
    for( String s : s_escapes ) {
      postfix_noharm_test( s );
    }
  }

  // ----------

  public void test_single_quote_prefix() {
    NielsenJSONFilter f = new NielsenJSONFilter();
    final String r = f.filter( "\\'pdq" );
    assertEquals( "'pdq", r );
  }

  public void test_single_quote_mid() {
    NielsenJSONFilter f = new NielsenJSONFilter();
    final String r = f.filter( "xyz\\'pdq" );
    assertEquals( "xyz'pdq", r );
  }

  public void test_single_quote_postfix() {
    NielsenJSONFilter f = new NielsenJSONFilter();
    final String r = f.filter( "xyz\\'" );
    assertEquals( "xyz'", r );
  }

  // ----------

  public void test_doublequote_standard_json() {
    NielsenJSONFilter f = new NielsenJSONFilter();
    final String s = "\\\"";
    final String r = f.filter( s );
    assertEquals( s, r );
  }

  public void test_backslash_standard_json() {
    NielsenJSONFilter f = new NielsenJSONFilter();
    final String s = "\\\\";
    final String r = f.filter( s );
    assertEquals( s, r );
  }
}
