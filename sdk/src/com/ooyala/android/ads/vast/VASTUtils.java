package com.ooyala.android.ads.vast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import android.content.Context;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.ooyala.android.DebugMode;

public class VASTUtils {
  private static final String TAG = "VASTUtils";
  private static final String SEPARATOR_TIME = ":";
  private static final List<String> TIMESTAMP_MACROS_TO_REPLACE = Arrays.asList("%5BPlace_Random_Number_Here%5D",
      "[Place_Random_Number_Here]", "%3Cnow%3E", "%3Crand-num%3E", "[TIMESTAMP]", "%5BTIMESTAMP%5E", "[timestamp]", "%5Btimestamp%5E");
  private static final List<String> DEVICEID_MACROS_TO_REPLACE = Arrays.asList("%5BLR_DEVICEID%5D", "[LR_DEVICEID]");
  private static String s_adId;

  public interface IAdIdSource {
    String getAdId();
  }

  public static final class GoogleAdIdSource implements IAdIdSource {
    private final String adId;
    public GoogleAdIdSource( Context context ) {
      Info adInfo = null;
      try {
        adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
      }
      catch (IllegalStateException e) {
        DebugMode.logE(TAG, "Failed to get ad id", e);
      }
      catch (GooglePlayServicesRepairableException e) {
        DebugMode.logE(TAG, "Failed to get ad id", e);
      }
      catch (IOException e) {
        DebugMode.logE(TAG, "Failed to get ad id", e);
      }
      catch (GooglePlayServicesNotAvailableException e) {
        DebugMode.logE(TAG, "Failed to get ad id", e);
      }
      adId = (adInfo == null) ? null : adInfo.getId();
    }
    @Override
    public String getAdId() {
      return adId;
    }
  }

  public static void initAdId( IAdIdSource source ) {
    s_adId = source.getAdId();
  }

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
    if( s_adId != null ) {
      for (String replace : DEVICEID_MACROS_TO_REPLACE) {
        url = url.replace(replace, s_adId);
      }
    }
    return url;
  }

}
