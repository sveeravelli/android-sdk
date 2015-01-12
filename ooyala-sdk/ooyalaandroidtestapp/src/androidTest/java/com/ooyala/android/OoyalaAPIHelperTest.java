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

  protected void setUp() {}

  protected void tearDown() {}

  public void testJSONForAPI() {
    Map<String, String> params = new HashMap<String, String>();
    params.put("device", "android");
    JSONObject json = OoyalaAPIHelper
        .objectForAPI("http://player.ooyala.com", "/player_api/v1/content_tree/embed_code/"
            + TestConstants.TEST_PCODE + "/" + TestConstants.TEST_VIDEO, params);
    assertTrue(json.toString().contains("\"embed_code\":\"" + TestConstants.TEST_VIDEO + "\""));
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
