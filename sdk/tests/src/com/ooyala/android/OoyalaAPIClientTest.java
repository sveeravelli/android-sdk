package com.ooyala.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.test.AndroidTestCase;

public class OoyalaAPIClientTest extends AndroidTestCase {
  public OoyalaAPIClient api;

  public OoyalaAPIClientTest() {
    super();
  }

  protected void setUp() {
    api = new OoyalaAPIClient(TestConstants.TEST_API_KEY, TestConstants.TEST_SECRET, TestConstants.TEST_PCODE, "www.ooyala.com");
  }

  protected void tearDown() {
  }

  public void testObjectFromBacklotAPI() throws JSONException {
    JSONObject o = api.objectFromBacklotAPI("/assets/Y3bDg0MzoD70RBbsIbOu6TP_awu9g8Cu", null);
    assertTrue(o != null);
    assertTrue(!o.isNull("name"));
    assertTrue(o.getString("name").equals("E*Trade Dancing Monkey"));
  }
}
