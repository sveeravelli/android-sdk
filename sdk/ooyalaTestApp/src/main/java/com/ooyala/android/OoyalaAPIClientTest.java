package com.ooyala.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.test.AndroidTestCase;
import com.ooyala.android.util.DebugMode;

public class OoyalaAPIClientTest extends AndroidTestCase {
  private static final String TAG = OoyalaAPIClientTest.class.getSimpleName();
  public OoyalaAPIClient api;

  public OoyalaAPIClientTest() {
    super();
  }

  protected void setUp() {
    PlayerDomain domain = null;
    try {
      domain = new PlayerDomain("http://www.ooyala.com");
    } catch (Exception e) {
      // TODO Auto-generated catch block
      DebugMode.logE( TAG, "Caught!", e );
    }
    api = new OoyalaAPIClient(TestConstants.TEST_API_KEY, TestConstants.TEST_SECRET,
        TestConstants.TEST_PCODE, domain);
  }

  protected void tearDown() {}

  public void testObjectFromBacklotAPI() throws JSONException {
    JSONObject o = api.objectFromBacklotAPI("/assets/Y3bDg0MzoD70RBbsIbOu6TP_awu9g8Cu", null);
    assertTrue(o != null);
    assertTrue(!o.isNull("name"));
    assertTrue(o.getString("name").equals("E*Trade Dancing Monkey"));
  }
}
