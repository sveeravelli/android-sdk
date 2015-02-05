package com.ooyala.android.ads.vast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import com.ooyala.android.AdvertisingIdUtils;
import com.ooyala.android.util.DebugMode;

public class VASTUtils {
  private static final String TAG = "VASTUtils";
  private static final String SEPARATOR_TIME = ":";
  private static final List<String> TIMESTAMP_MACROS_TO_REPLACE = Arrays.asList("%5BPlace_Random_Number_Here%5D",
      "[Place_Random_Number_Here]", "%3Cnow%3E", "%3Crand-num%3E", "[TIMESTAMP]", "%5BTIMESTAMP%5E", "[timestamp]", "%5Btimestamp%5E");
  private static final List<String> DEVICEID_MACROS_TO_REPLACE = Arrays.asList("%5BLR_DEVICEID%5D", "[LR_DEVICEID]");

  public static boolean isNullOrEmpty(String string) {
    return string == null || string.equals("");
  }

  public static double secondsFromTimeString(String time) {
    String[] hms = time.split(SEPARATOR_TIME);
    double multiplier = 1.0;
    double milliseconds = 0.0;
    for (int i = hms.length - 1; i >= 0; i--) {
      milliseconds += (Double.parseDouble(hms[i]) * multiplier);
      multiplier *= 60.0;
    }
    return milliseconds;
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
