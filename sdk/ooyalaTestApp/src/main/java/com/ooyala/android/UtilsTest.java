package com.ooyala.android;

import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.test.AndroidTestCase;

public class UtilsTest extends AndroidTestCase {
  public UtilsTest() {
    super();
  }

  protected void setUp() {}

  protected void tearDown() {}

  public void testIsNullOrEmpty() {
    String empty = "";
    String nullString = null;
    String str = "helo";
    assertTrue(Utils.isNullOrEmpty(empty));
    assertTrue(Utils.isNullOrEmpty(nullString));
    assertFalse(Utils.isNullOrEmpty(str));
  }

  public void testGetParamsString() {
    Map<String, String> paramDictionary = new HashMap<String, String>();;
    paramDictionary.put("device", "android");
    paramDictionary.put("api_key", TestConstants.TEST_API_KEY);
    paramDictionary.put("domain", TestConstants.TEST_DOMAIN);
    paramDictionary.put("expires", "1322007460");
    String expectedParamString = "api_key=" + TestConstants.TEST_API_KEY + "&device=android&domain="
        + URLEncoder.encode(TestConstants.TEST_DOMAIN) + "&expires=1322007460";
    String paramString = Utils.getParamsString(paramDictionary, "&", true);
    assertEquals(paramString, expectedParamString);
  }

  public void testMakeURL() {
    Map<String, String> params = new HashMap<String, String>();
    params.put("paramName", "paramVal");
    params.put("otherParamName", "otherParamVal");
    URL url = Utils.makeURL("http://hello.com", "/omggggggg/omg", params);
    String expected = "http://hello.com/omggggggg/omg?otherParamName=otherParamVal&paramName=paramVal";
    assertEquals(url.toString(), expected);
  }

  public void testOverwriteJSONObject_overwrites() {
    try {
      final String key1 = "key1";

      final JSONObject src = new JSONObject();
      src.put( key1, new Integer(1) );

      final JSONObject dst = new JSONObject();
      dst.put( key1, new Integer(42) );

      Utils.overwriteJSONObject( src, dst );
      assertEquals( src.get(key1), dst.get(key1) );
    } catch (JSONException e) {
      fail(e.toString());
    }
  }

  public void testOverwriteJSONObject_doesNotOverwrite() {
    try {
      final String key1 = "key1";
      final String key2 = "key2";

      final JSONObject src = new JSONObject();
      src.put( key1, new Integer(1) );

      final JSONObject dst = new JSONObject();
      dst.put( key2, new Integer(42) );

      Utils.overwriteJSONObject( src, dst );
      assertEquals( dst.get(key2), dst.get(key2) );
    } catch (JSONException e) {
      fail(e.toString());
    }
  }

}
