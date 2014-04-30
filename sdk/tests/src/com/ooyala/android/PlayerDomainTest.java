package com.ooyala.android;

import android.test.AndroidTestCase;

public class PlayerDomainTest extends AndroidTestCase {
  private final String validDomain1 = "http://ooyala.com";
  private final String validDomain2 = "https://www.ooyala.com";
  private final String invalidDomain1 = "ooyala.com";
  private final String invalidDomain2 = "ftp://ooyala.com";

  public PlayerDomainTest() {
    super();
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }

  /**
   * Tests Valid PlayerDomains strings
   */
  public void testValidDomainss() {
    PlayerDomain domain;
    try {
      domain = new PlayerDomain(validDomain1);
      assertEquals(domain.toString(), validDomain1);
    } catch (RuntimeException e) {
      assertTrue(e.toString(), false);
    }
    
    try {
      domain = new PlayerDomain(validDomain2);
      assertEquals(domain.toString(), validDomain2);
    } catch (RuntimeException e) {
      assertTrue(e.toString(), false);
    }
  }

  /**
   * Tests invalid PlayerDomain strings
   */
  public void testInvalidDomains() {
    PlayerDomain domain = null;
    try {
      domain = new PlayerDomain(invalidDomain1);
      
    } catch (RuntimeException e) {
      assertTrue(e.toString().endsWith("Invalid Domain String: " + invalidDomain1));
    }
    assertNull(domain);
    
    try {
      domain = new PlayerDomain(invalidDomain2);
    } catch (RuntimeException e) {
      assertTrue(e.toString().endsWith("Invalid Domain String: " + invalidDomain2));
    }
    assertNull(domain);
  }
}

