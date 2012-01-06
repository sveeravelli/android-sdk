package com.ooyala.android;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.test.AndroidTestCase;

public class EmbeddedSecureURLGeneratorTest extends AndroidTestCase {
  public EmbeddedSecureURLGenerator urlGen;

  public EmbeddedSecureURLGeneratorTest() {
    super();
    urlGen = new EmbeddedSecureURLGenerator(TestConstants.TEST_API_KEY, TestConstants.TEST_SECRET);
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }

  public void testSecureURL() {
    String expecedSecureURL = "http://hello.com/uhh.../hi?api_key=l1am06xhbSxa0OtyZsBTshW2DMtp.qDW-_&device=android&expires=1234567890&signature=LvOmpUl3GMO2cqos6fdrwKkXmD/KEg3NWG7j5iz6UlU";
    Map<String,String> params = new HashMap<String,String>();
    params.put(Constants.KEY_DEVICE, Constants.DEVICE_ANDROID);
    params.put(Constants.KEY_EXPIRES, "1234567890");
    URL secureURL = urlGen.secureURL("http://hello.com", "/uhh.../hi", params);
    assertEquals(secureURL.toString(), expecedSecureURL);
  }
}

