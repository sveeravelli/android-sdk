package com.ooyala.android;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.Build;

class Utils {
  static final String DEVICE_ANDROID_SDK = "android_sdk";
  /** TODO[jigish] change to android_hls_sdk when SAS is pushed */
  static final String DEVICE_ANDROID_HLS_SDK = "android_3plus_sdk";
  static final String DEVICE_IPAD = "ipad"; // hack for Washington Post - See PB-279

  static final String SEPARATOR_AMPERSAND = "&";
  static final String SEPARATOR_TIME = ":";

  private static final String TAG = Utils.class.getName();

  public static String device() {
    // temporarily disable HLS
    if (OoyalaPlayer.enableHighResHLS) { // hack for Washington Post - See PB-279
      return DEVICE_IPAD;
    } else if (OoyalaPlayer.enableHLS || Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      return DEVICE_ANDROID_HLS_SDK;
    } else return DEVICE_ANDROID_SDK;
  }

  public static URL makeURL(String host, String uri, Map<String, String> params) {
    return makeURL(host, uri, getParamsString(params, SEPARATOR_AMPERSAND, true));
  }

  public static URL makeURL(String host, String uri, String params) {
    try {
      return new URL(host + uri + (params == null || params.length() < 1 ? "" : "?" + params));
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
    List<T> list = new ArrayList<T>(c);
    java.util.Collections.sort(list);
    return list;
  }

  public static String getParamsString(Map<String, String> params, String separator, boolean urlEncode) {
    if (params == null || params.isEmpty()) { return ""; }

    StringBuffer result = new StringBuffer();
    boolean first = true;
    for (String key : asSortedList(params.keySet())) {
      if (first) {
        first = false;
      } else {
        result.append(separator);
      }

      result.append(key);
      result.append("=");
      if (urlEncode) {
        try {
          result.append(URLEncoder.encode(params.get(key), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          DebugMode.logE(Utils.class.getName(), "ERROR while trying to encode parameter", e);
          result.append(params.get(key));
        }
      } else {
        result.append(params.get(key));
      }
    }
    return result.toString();
  }

  public static <T> Set<T> getSubset(Map<String, T> objects, int firstIndex, int count) {
    if (firstIndex < 0 || firstIndex + count > objects.size()) { return null; }

    Iterator<T> iterator = objects.values().iterator();
    for (int i = 0; i < firstIndex && iterator.hasNext(); i++) {
      iterator.next();
    }

    Set<T> result = new HashSet<T>();
    for (int i = 0; i < count && iterator.hasNext(); i++) {
      result.add(iterator.next());
    }
    return result;
  }

  public static String join(Collection<? extends Object> list, String separator) {
    if (list == null) { return null; }

    StringBuffer result = new StringBuffer();

    for (Object o : list) {
      result.append(o.toString());
      result.append(separator);
    }

    if (result.length() > 0) {
      result = result.deleteCharAt(result.length() -1);
    }

    return result.toString();
  }

  public static boolean isNullOrEmpty(String string) {
    return string == null || string.equals("");
  }

  public static JSONObject objectFromJSON(String json) {
    try {
      return (JSONObject) new JSONTokener(json).nextValue();
    } catch (JSONException exception) {
      System.out.println("JSONException: " + exception);
      return null;
    } catch (ClassCastException exception) {
      System.out.println("ClassCastException: " + exception);
      return null;
    }
  }


  public static double secondsFromTimeString(String time) {
    if (time == null) {
      DebugMode.assertFail(TAG, "secondsFromTimeString the string is null");
      return 0;
    }
    String[] hms = time.split(SEPARATOR_TIME);
    double timeMetric = 1.0;
    double milliseconds = 0.0;

    // set time metric
    switch (hms.length) {
      case 1:
        return Utils.bareMilliSecondsFromTimeString(time);
      case 2:
      case 3:
        // use default time metric
        break;
      case 4:
        // if four items are presented, set the last one is frame
        timeMetric = 1.0 / 30;
        break;
      default:
        DebugMode.assertFail(TAG, "invalid time format: " + time);
        return 0;
    }

    for (int i = hms.length - 1; i >= 0; i--) {
      if (hms[i].length() > 0) {
        try {
          milliseconds += Double.valueOf(hms[i]) * timeMetric;
        } catch (NumberFormatException e) {
          DebugMode.assertFail(TAG, e.toString());
          return 0;
        }
      }
      if (timeMetric < 1) {
        // reset metric from frame metric to default(second) matic
        timeMetric = 1.0;
      } else {
        timeMetric *= 60.0;
      }
    }
    return milliseconds;
  }

  private static double bareMilliSecondsFromTimeString(String time) {
    int dotCount = 0;
    int i = 0;
    for (; i < time.length(); ++i ) {
      char c = time.charAt(i);
      if (c >= '0' && c <= '9') {
        continue;
      } else if (c == '.') {
        dotCount++;
        if (dotCount > 1 || i == 0) {
          DebugMode.assertFail(TAG,
              "bareMilliSecondsFromTimeString invalid format: " + time);
          return 0;
        }
      } else {
        break;
      }
    }

    if (time.charAt(i - 1) == '.') {
      DebugMode.assertFail(TAG,
          "bareMilliSecondsFromTimeString invalid format: " + time);
      return 0;
    }

    double value = Double.valueOf(time.substring(0, i));
    double timeMetric = 1.0;
    if (i < time.length()) {
      String metricString = time.substring(i);
      if (metricString.equals("h")) {
        timeMetric = 3600;
      } else if (metricString.equals("m")) {
        timeMetric = 60;
      } else if (metricString.equals("s")) {
        // default value, do nothing.
      } else if (metricString.equals("ms")) {
        timeMetric = 0.001;
      } else if (metricString.equals("f")) {
        timeMetric = 1.0/30;
      } else {
        DebugMode.assertFail(TAG, "invalid cc bare time string, unknown time metric: " + time);
        return 0;
      }
    }
    return value * timeMetric;
  }
}
