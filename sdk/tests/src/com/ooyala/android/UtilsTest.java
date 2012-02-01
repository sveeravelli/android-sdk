package com.ooyala.android;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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

  public void testTimeStringConverters() {
    String time = "00:00:00.123";
    assertEquals(Utils.secondsFromTimeString(time), 0.123);
    time = "00:00:01";
    assertEquals(Utils.secondsFromTimeString(time), 1.000);
    time = "00:00:01.123";
    assertEquals(Utils.secondsFromTimeString(time), 1.123);
    time = "00:19:01.123";
    assertEquals(Utils.secondsFromTimeString(time), 1141.123);
    time = "91:29:34.999";
    assertEquals(Utils.secondsFromTimeString(time), 329374.999);
  }

  public void testGetParamsString() {
    Map<String, String> paramDictionary = new HashMap<String, String>();;
    paramDictionary.put("device", "android");
    paramDictionary.put("api_key", TestConstants.TEST_API_KEY);
    paramDictionary.put("domain", TestConstants.TEST_DOMAIN);
    paramDictionary.put("expires", "1322007460");
    String expectedParamString = "api_key=" + TestConstants.TEST_API_KEY + "&device=android&domain="
        + TestConstants.TEST_DOMAIN + "&expires=1322007460";
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
}
