package com.ooyala.android.ads.vast;

public class VASTUtils {
  static final String SEPARATOR_TIME = ":";

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
}
