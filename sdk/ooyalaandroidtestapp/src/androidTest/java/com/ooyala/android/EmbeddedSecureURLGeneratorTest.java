package com.ooyala.android;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.test.AndroidTestCase;

public class EmbeddedSecureURLGeneratorTest extends AndroidTestCase {
  public EmbeddedSecureURLGenerator urlGen;

  static final String DEVICE_ANDROID_SDK = "android_sdk";
  public EmbeddedSecureURLGeneratorTest() {
    super();
  }

  @Override
  protected void setUp() {
    urlGen = new EmbeddedSecureURLGenerator(TestConstants.TEST_API_KEY, TestConstants.TEST_SECRET);
  }

  @Override
  protected void tearDown() {}

  public void testSecureURL() {
    String expecedSecureURL = "http://hello.com/test/path?api_key="
        + TestConstants.TEST_API_KEY
        + "&device=android_sdk&expires=1234567890&signature=wiZiqAdTUnTPXiAR6yBj77%2BN%2BC4u9J8zCiFOtmmQcqQ";
    Map<String, String> params = new HashMap<String, String>();
    params.put(EmbeddedSecureURLGenerator.KEY_DEVICE, DEVICE_ANDROID_SDK);
    params.put(EmbeddedSecureURLGenerator.KEY_EXPIRES, "1234567890");
    URL secureURL = urlGen.secureURL("http://hello.com", "/test/path", params);
    assertEquals(secureURL.toString(), expecedSecureURL);
  }
}