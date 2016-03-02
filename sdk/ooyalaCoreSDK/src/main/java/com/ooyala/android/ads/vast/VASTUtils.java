package com.ooyala.android.ads.vast;

import com.ooyala.android.AdvertisingIdUtils;
import com.ooyala.android.util.DebugMode;

import org.w3c.dom.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

class VASTUtils {
  private static final String TAG = "VASTUtils";
  private static final String SEPARATOR_TIME = ":";
  private static final List<String> TIMESTAMP_MACROS_TO_REPLACE = Arrays.asList("%5BPlace_Random_Number_Here%5D",
      "[Place_Random_Number_Here]", "%3Cnow%3E", "%3Crand-num%3E", "[TIMESTAMP]", "%5BTIMESTAMP%5E", "[timestamp]", "%5Btimestamp%5E");
  private static final List<String> DEVICEID_MACROS_TO_REPLACE = Arrays.asList("%5BLR_DEVICEID%5D", "[LR_DEVICEID]");

  public static boolean isNullOrEmpty(String string) {
    return string == null || string.equals("");
  }

  /*
   * parse string to time
   * legal format HH:MM:SS.mmm
   * @param time the time string
   * @param defaultValue the default value to be used.
   * @return time value in seconds, negative if failed;
   */
  public static double secondsFromTimeString(String time, double defaultValue) {
    if (time == null) {
      return defaultValue;
    }
    double seconds = 0;
    String[] hms = time.split(SEPARATOR_TIME);
    for (int i = 0; i < hms.length; ++i) {
      try {
        double value = Double.parseDouble(hms[i]);
        seconds = seconds * 60 + value;
      } catch (NumberFormatException e) {
        DebugMode.logE(TAG, "invalid time string: " + time);
        return defaultValue;
      }
    }
    return seconds;
  }

  /*
   * parse string to integer
   * @param e the xml element
   * @param attributeName the attribute name
   * @return the integer value, default is used if attribute does not present or is invalid
   */
  public static int getIntAttribute(Element e, String attributeName, int defaultValue) {
    String attributeString = e.getAttribute(attributeName);
    if (attributeString == null) {
      DebugMode.logD(TAG, "Attribute " + attributeName + " does not exist");
      return defaultValue;
    }

    try {
      return Integer.parseInt(attributeString);
    } catch (NumberFormatException ex) {
      DebugMode.logE(TAG, "Invalid Attribute " + attributeName + " :" + attributeString);
      return defaultValue;
    }
  }

  public static URL urlFromAdUrlString(String urlStr) {
    urlStr = replaceTimestampMacros( urlStr );
    urlStr = replaceAdIdMacros( urlStr );

    URL url = null;
    try {
      url = new URL( urlStr );
    } catch (MalformedURLException e) {
      DebugMode.logE(TAG, "Malformed VAST URL: " + url);
    }
    return url;
  }

  private static String replaceTimestampMacros( String url ) {
    final String timestamp = "" + (System.currentTimeMillis() / 1000);
    for (String replace : TIMESTAMP_MACROS_TO_REPLACE) {
      url = url.replace(replace, timestamp);
    }
    return url;
  }

  private static String replaceAdIdMacros( String url ) {
    final String advertisingId = AdvertisingIdUtils.getAdvertisingId();
    if( advertisingId != null ) {
      for (String replace : DEVICEID_MACROS_TO_REPLACE) {
        url = url.replace(replace, advertisingId);
      }
    }
    return url;
  }

}
