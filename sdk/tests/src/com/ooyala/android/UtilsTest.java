package com.ooyala.android;

import java.net.URL;
import java.net.URLEncoder;
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
  
  public void testClosedCaptionTimeStringPlain() {
    String timeString = "3";
    double milisecond = Utils.secondsFromTimeString(timeString);
    double expected = 3.0;
    assertEquals(milisecond, expected);
  }

  public void testClosedCaptionTimeStringDecimalPlain() {
    String timeString = "4.10";
    double milisecond = Utils.secondsFromTimeString(timeString);
    double expected = 4.1;
    assertEquals(milisecond, expected);
  }

  public void testClosedCaptionTimeStringSecond() {
    String timeString = "5.27s";
    double milisecond = Utils.secondsFromTimeString(timeString);
    double expected = 5.27;
    assertEquals(milisecond, expected);
  }

  public void testClosedCaptionTimeStringHour() {
    String timeString = "2.5h";
    double milisecond = Utils.secondsFromTimeString(timeString);
    double expected = 2.5 * 3600;
    assertEquals(milisecond, expected);
  }

  public void testClosedCaptionTimeStringMinute() {
    String timeString = "7.9m";
    double milisecond = Utils.secondsFromTimeString(timeString);
    double expected = 7.9 * 60;
    assertEquals(milisecond, expected);
  }

  public void testClosedCaptionTimeStringMillisecond() {
    String timeString = "6.8ms";
    double milisecond = Utils.secondsFromTimeString(timeString);
    double expected = 0.0068;
    assertEquals(milisecond, expected);
  }
  
  public void testClosedCaptionTimeStringFrame() {
    String timeString = "3.5f";
    double milisecond = Utils.secondsFromTimeString(timeString);
    double expected = 3.5 / 30.0;
    assertEquals(milisecond, expected);
  }

  public void testClosedCaptionTimeStringMillisecondFail() {
    String timeString = "6..8ms";
    double milisecond = Utils.secondsFromTimeString(timeString);
    double expected = 0;
    assertEquals(milisecond, expected);
  }

  public void testClosedCaptionTimeStringSecondFail() {
    String timeString = "3.5.s";
    double milisecond = Utils.secondsFromTimeString(timeString);
    double expected = 0;
    assertEquals(milisecond, expected);
  }

  public void testClosedCaptionTimeStringSecondFail1() {
    String timeString = ".5";
    double milisecond = Utils.secondsFromTimeString(timeString);
    double expected = 0;
    assertEquals(milisecond, expected);
  }

  public void testClosedCaptionTimeStringSecondFail2() {
    String timeString = "5.";
    double milisecond = Utils.secondsFromTimeString(timeString);
    double expected = 0;
    assertEquals(milisecond, expected);
  }

  public void testClosedCaptionTimeStringUnknownFormat() {
    String timeString = "3.5d";
    double milisecond = Utils.secondsFromTimeString(timeString);
    double expected = 0;
    assertEquals(milisecond, expected);
  }

  public void testClosedCaptionTimeStringHourMinuteSecond() {
    String timeString = "07:20:12.24";
    double milisecond = Utils.secondsFromTimeString(timeString);
    double expected = 7 * 3600 + 20 * 60 + 12.24;
    assertEquals(milisecond, expected);
  }

  public void testClosedCaptionTimeStringMinuteSecond() {
    String timeString = "20:12.24";
    double milisecond = Utils.secondsFromTimeString(timeString);
    double expected = 20 * 60 + 12.24;
    assertEquals(milisecond, expected);
  }

  public void testClosedCaptionTimeStringHourMinuteSecond2() {
    String timeString = ":20:12.24";
    double milisecond = Utils.secondsFromTimeString(timeString);
    double expected = 20 * 60 + 12.24;
    assertTrue(Math.abs(milisecond - expected) < 0.001);
  }

  public void testClosedCaptionTimeStringMinuteSecondFrame() {
    String timeString = "::12:15";
    double milisecond = Utils.secondsFromTimeString(timeString);
    double expected = 12 + 15 / 30.0;
    assertEquals(milisecond, expected);
  }
}
