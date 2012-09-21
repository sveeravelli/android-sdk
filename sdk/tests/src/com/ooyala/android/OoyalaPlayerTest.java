package com.ooyala.android;

import android.test.AndroidTestCase;

public class OoyalaPlayerTest extends AndroidTestCase {
  public OoyalaPlayerTest() {
    super();
  }

  protected void setUp() {

  }

  protected void tearDown() {

  }

  public void testSetEnvironment() {
    assertEquals("Default content tree host should be http://player.ooyala.com", Constants.CONTENT_TREE_HOST,
        "http://player.ooyala.com");
    assertEquals("Default auth host should be http://player.ooyala.com", Constants.AUTHORIZE_HOST,
        "http://player.ooyala.com");
    assertEquals("Default metadata host should be http://player.ooyala.com", Constants.METADATA_HOST,
        "http://player.ooyala.com");
    assertEquals("Default drm host should be http://player.ooyala.com", Constants.DRM_HOST,
        "http://player.ooyala.com");
    assertEquals("Default js analytics host should be http://player.ooyala.com", Constants.JS_ANALYTICS_HOST,
        "http://player.ooyala.com");
    assertEquals("Default backlot host should be http://cdn.api.ooyala.com", Constants.BACKLOT_HOST,
        "http://cdn.api.ooyala.com");
    OoyalaPlayer.setEnvironment(OoyalaPlayer.Environment.STAGING);
    assertEquals("Staging content tree host should be http://player-staging.ooyala.com",
        Constants.CONTENT_TREE_HOST, "http://player-staging.ooyala.com");
    assertEquals("Staging auth host should be http://player-staging.ooyala.com", Constants.AUTHORIZE_HOST,
        "http://player-staging.ooyala.com");
    assertEquals("Staging metadata host should be http://player-staging.ooyala.com", Constants.METADATA_HOST,
        "http://player-staging.ooyala.com");
    assertEquals("Staging drm host should be http://player-staging.ooyala.com", Constants.DRM_HOST,
        "http://player-staging.ooyala.com");
    assertEquals("Staging js analytics host should be http://player-staging.ooyala.com",
        Constants.JS_ANALYTICS_HOST, "http://player-staging.ooyala.com");
    assertEquals("Staging backlot host should be http://api-staging.ooyala.com", Constants.BACKLOT_HOST,
        "http://api-staging.ooyala.com");
    OoyalaPlayer.setEnvironment(OoyalaPlayer.Environment.LOCAL);
    assertEquals("Local content tree host should be http://dev.corp.ooyala.com:3000",
        Constants.CONTENT_TREE_HOST, "http://dev.corp.ooyala.com:3000");
    assertEquals("Local auth host should be http://dev.corp.ooyala.com:4567", Constants.AUTHORIZE_HOST,
        "http://dev.corp.ooyala.com:4567");
    assertEquals("Local metadata host should be http://dev.corp.ooyala.com:3000", Constants.METADATA_HOST,
        "http://dev.corp.ooyala.com:3000");
    assertEquals("Local drm host should be http://dev.corp.ooyala.com:4567", Constants.DRM_HOST,
        "http://dev.corp.ooyala.com:4567");
    assertEquals("Local js analytics host should be http://dev.corp.ooyala.com:3000",
        Constants.JS_ANALYTICS_HOST, "http://dev.corp.ooyala.com:3000");
    assertEquals("Local backlot host should be http://api-staging.ooyala.com", Constants.BACKLOT_HOST,
        "http://api-staging.ooyala.com");
    OoyalaPlayer.setEnvironment(OoyalaPlayer.Environment.PRODUCTION);
    assertEquals("Prod content tree host should be http://player.ooyala.com", Constants.CONTENT_TREE_HOST,
        "http://player.ooyala.com");
    assertEquals("Prod auth host should be http://player.ooyala.com", Constants.AUTHORIZE_HOST,
        "http://player.ooyala.com");
    assertEquals("Prod metadata host should be http://player.ooyala.com", Constants.METADATA_HOST,
        "http://player.ooyala.com");
    assertEquals("Prod drm host should be http://player.ooyala.com", Constants.DRM_HOST,
        "http://player.ooyala.com");
    assertEquals("Prod js analytics host should be http://player.ooyala.com", Constants.JS_ANALYTICS_HOST,
        "http://player.ooyala.com");
    assertEquals("Prod backlot host should be http://cdn.api.ooyala.com", Constants.BACKLOT_HOST,
        "http://cdn.api.ooyala.com");
  }
}
