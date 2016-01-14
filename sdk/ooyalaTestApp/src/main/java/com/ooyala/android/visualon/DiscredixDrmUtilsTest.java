package com.ooyala.android.visualon;

import android.test.AndroidTestCase;

/**
 * Created by zchen on 1/13/16.
 */
public class DiscredixDrmUtilsTest extends AndroidTestCase {

  public void testAppendCustomDataWithNullString() {
    String customData = null;
    String appendedData = DiscredixDrmUtils.appendCustomData(customData);
    assertEquals(appendedData, "SecurePlayer=1");
  }

  public void testAppendCustomDataWithEmptyString() {
    String customData = "";
    String appendedData = DiscredixDrmUtils.appendCustomData(customData);
    assertEquals(appendedData, "SecurePlayer=1");
  }

  public void testAppendCustomDataWithAuthToken() {
    String customData = "auth_token=abc";
    String appendedData = DiscredixDrmUtils.appendCustomData(customData);
    assertEquals(appendedData, "auth_token=abc&SecurePlayer=1");
  }
}
