package com.ooyala.android;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.test.AndroidTestCase;

public class OoyalaAPIHelperTest extends AndroidTestCase {
  public OoyalaAPIHelper apiHelper;

  public OoyalaAPIHelperTest() {
    super();
  }

  protected void setUp() {
    apiHelper = new OoyalaAPIHelper(TestConstants.TEST_API_KEY, TestConstants.TEST_SECRET);
  }

  protected void tearDown() {}

  public void testJSONForSecureAPI() {
    Map<String, String> params = new HashMap<String, String>();
    params.put("device", "android");
    params.put("domain", "www.ooyala.com");
    // TODO once we push SAS, go to player.ooyala.com
    String json = apiHelper.jsonForSecureAPI("http://player.ooyala.com",
        "/sas/player_api/v1/authorization/embed_code/" + TestConstants.TEST_PCODE + "/"
            + TestConstants.TEST_VIDEO, params);
    assertTrue(json.contains("\"authorized\":true"));
  }

  public void testObjectForSecureAPI() {
    Map<String, String> params = new HashMap<String, String>();
    params.put("device", "android");
    params.put("domain", "www.ooyala.com");
    // TODO once we push SAS, go to player.ooyala.com
    JSONObject json = apiHelper.objectForSecureAPI("http://player.ooyala.com",
        "/sas/player_api/v1/authorization/embed_code/" + TestConstants.TEST_PCODE + "/"
            + TestConstants.TEST_VIDEO, params);
    assertFalse(json.isNull("authorization_data"));
    try {
      assertFalse(json.getJSONObject("authorization_data").isNull(TestConstants.TEST_VIDEO));
      assertTrue(json.getJSONObject("authorization_data").getJSONObject(TestConstants.TEST_VIDEO)
          .getBoolean("authorized"));
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testJSONForAPI() {
    Map<String, String> params = new HashMap<String, String>();
    params.put("device", "android");
    String json = OoyalaAPIHelper
        .jsonForAPI("http://player.ooyala.com", "/player_api/v1/content_tree/embed_code/"
            + TestConstants.TEST_PCODE + "/" + TestConstants.TEST_VIDEO, params);
    assertTrue(json.contains("\"embed_code\":\"" + TestConstants.TEST_VIDEO + "\""));
  }

  public void testObjectForAPI() {
    Map<String, String> params = new HashMap<String, String>();
    params.put("device", "android");
    JSONObject json = OoyalaAPIHelper
        .objectForAPI("http://player.ooyala.com", "/player_api/v1/content_tree/embed_code/"
            + TestConstants.TEST_PCODE + "/" + TestConstants.TEST_VIDEO, params);
    assertFalse(json.isNull("content_tree"));
    try {
      assertFalse(json.getJSONObject("content_tree").isNull(TestConstants.TEST_VIDEO));
      assertEquals(
          json.getJSONObject("content_tree").getJSONObject(TestConstants.TEST_VIDEO).getString("embed_code"),
          TestConstants.TEST_VIDEO);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }
}
