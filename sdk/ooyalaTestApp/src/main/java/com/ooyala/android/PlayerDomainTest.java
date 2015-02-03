package com.ooyala.android;

import java.util.ArrayList;

import android.test.AndroidTestCase;

public class PlayerDomainTest extends AndroidTestCase {
  private ArrayList<String> validDomains;
  private ArrayList<String> invalidDomains;

  public PlayerDomainTest() {
    super();
  }

  protected void setUp() {
    validDomains = new ArrayList<String>();
    validDomains.add("http://ooyala.com");
    validDomains.add("https://www.ooyala.com");

    invalidDomains = new ArrayList<String>();
    invalidDomains.add("");
    invalidDomains.add("ooyala.com");
    invalidDomains.add("ftp://ooyala.com");
    invalidDomains.add("ht");
    invalidDomains.add("http s://ooyala.com");
    invalidDomains.add("http:/ /ooyala.com");
  }

  protected void tearDown() {
  }

  /**
   * Tests Valid PlayerDomain Strings
   */
  public void testValidDomainStrings() {
    for (String s : validDomains) {
      PlayerDomain domain = new PlayerDomain(s);
      assertEquals(domain.toString(), s);
    }
  }

  /**
   * Tests invalid PlayerDomain Strings
   */
  public void testInvalidDomainStrings() {
    for (String s : invalidDomains) {
      try {
        @SuppressWarnings("unused")
        PlayerDomain domain = new PlayerDomain(s);
        fail("RuntimeExceptions should be thrown for " + s);
      } catch (RuntimeException ex) {
        assertTrue(ex.toString().endsWith(s));
      }
    }
  }
}