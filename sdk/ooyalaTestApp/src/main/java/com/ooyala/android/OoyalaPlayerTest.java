package com.ooyala.android;

import android.test.AndroidTestCase;

public class OoyalaPlayerTest extends AndroidTestCase {
  final String PCODE  = "R2d3I6s06RyB712DN0_2GsQS-R-Y";
  final String DOMAIN = "http://ooyala.com";

  public OoyalaPlayerTest() {
    super();
  }

  @Override
  protected void setUp() {

  }

  @Override
  protected void tearDown() {

  }

  public void testSetEnvironment() {
    assertEquals("Default content tree host should be http://player.ooyala.com", Environment.CONTENT_TREE_HOST,
        "http://player.ooyala.com");
    assertEquals("Default auth host should be http://player.ooyala.com", Environment.AUTHORIZE_HOST,
        "http://player.ooyala.com");
    assertEquals("Default metadata host should be http://player.ooyala.com", Environment.METADATA_HOST,
        "http://player.ooyala.com");
    assertEquals("Default drm host should be http://player.ooyala.com", Environment.DRM_HOST,
        "http://player.ooyala.com");
    assertEquals("Default js analytics host should be http://player.ooyala.com", Environment.JS_ANALYTICS_HOST,
        "http://player.ooyala.com");
    assertEquals("Default backlot host should be http://cdn.api.ooyala.com", Environment.BACKLOT_HOST,
        "http://cdn.api.ooyala.com");
    OoyalaPlayer.setEnvironment(Environment.EnvironmentType.STAGING);
    assertEquals("Staging content tree host should be http://player-staging.ooyala.com",
        Environment.CONTENT_TREE_HOST, "http://player-staging.ooyala.com");
    assertEquals("Staging auth host should be http://player-staging.ooyala.com", Environment.AUTHORIZE_HOST,
        "http://player-staging.ooyala.com");
    assertEquals("Staging metadata host should be http://player-staging.ooyala.com", Environment.METADATA_HOST,
        "http://player-staging.ooyala.com");
    assertEquals("Staging drm host should be http://player-staging.ooyala.com", Environment.DRM_HOST,
        "http://player-staging.ooyala.com");
    assertEquals("Staging js analytics host should be http://player-staging.ooyala.com",
        Environment.JS_ANALYTICS_HOST, "http://player-staging.ooyala.com");
    assertEquals("Staging backlot host should be http://api-staging.ooyala.com", Environment.BACKLOT_HOST,
        "http://api-staging.ooyala.com");
    OoyalaPlayer.setEnvironment(Environment.EnvironmentType.LOCAL);
    assertEquals("Local content tree host should be http://dev.corp.ooyala.com:3000",
        Environment.CONTENT_TREE_HOST, "http://dev.corp.ooyala.com:3000");
    assertEquals("Local auth host should be http://dev.corp.ooyala.com:4567", Environment.AUTHORIZE_HOST,
        "http://dev.corp.ooyala.com:4567");
    assertEquals("Local metadata host should be http://dev.corp.ooyala.com:3000", Environment.METADATA_HOST,
        "http://dev.corp.ooyala.com:3000");
    assertEquals("Local drm host should be http://dev.corp.ooyala.com:4567", Environment.DRM_HOST,
        "http://dev.corp.ooyala.com:4567");
    assertEquals("Local js analytics host should be http://dev.corp.ooyala.com:3000",
        Environment.JS_ANALYTICS_HOST, "http://dev.corp.ooyala.com:3000");
    assertEquals("Local backlot host should be http://api-staging.ooyala.com", Environment.BACKLOT_HOST,
        "http://api-staging.ooyala.com");
    OoyalaPlayer.setEnvironment(Environment.EnvironmentType.PRODUCTION);
    assertEquals("Prod content tree host should be http://player.ooyala.com", Environment.CONTENT_TREE_HOST,
        "http://player.ooyala.com");
    assertEquals("Prod auth host should be http://player.ooyala.com", Environment.AUTHORIZE_HOST,
        "http://player.ooyala.com");
    assertEquals("Prod metadata host should be http://player.ooyala.com", Environment.METADATA_HOST,
        "http://player.ooyala.com");
    assertEquals("Prod drm host should be http://player.ooyala.com", Environment.DRM_HOST,
        "http://player.ooyala.com");
    assertEquals("Prod js analytics host should be http://player.ooyala.com", Environment.JS_ANALYTICS_HOST,
        "http://player.ooyala.com");
    assertEquals("Prod backlot host should be http://cdn.api.ooyala.com", Environment.BACKLOT_HOST,
        "http://cdn.api.ooyala.com");
  }

  public void testChangeCurrentItemNull() {
    OoyalaPlayer player = new OoyalaPlayer(PCODE, new PlayerDomain(DOMAIN));
    assertFalse(player.changeCurrentItem(null));
  }

  public void testChangeCurrentItemInvalid() {
    OoyalaPlayer player = new OoyalaPlayer(PCODE, new PlayerDomain(DOMAIN));
    assertFalse(player.changeCurrentItem("invalid"));
  }

  public void testPcode() {
    OoyalaPlayer player = new OoyalaPlayer(PCODE, new PlayerDomain(DOMAIN));
    assertEquals(PCODE, player.getPcode());
  }

}
