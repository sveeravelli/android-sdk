package com.ooyala.android;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import android.util.Log;

class Utils {
  public static String device() {
    if (Build.VERSION.SDK_INT >= 11) // HONEYCOMB
    return Constants.DEVICE_ANDROID_3PLUS_SDK;
    else return Constants.DEVICE_ANDROID_SDK;
  }

  public static URL makeURL(String host, String uri, Map<String, String> params) {
    return makeURL(host, uri, getParamsString(params, Constants.SEPARATOR_AMPERSAND, true));
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
          Log.e(Utils.class.getName(), "ERROR while trying to encode parameter", e);
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

  public static String join(List<String> list, String separator) {
    if (list == null) { return null; }
    StringBuffer result = new StringBuffer();
    if (!list.isEmpty()) {
      result.append(list.get(0));
      for (int i = 1; i < list.size(); i++) {
        if (separator != null) {
          result.append(separator);
        }
        result.append(list.get(i));
      }
    }
    return result.toString();
  }

  public static boolean isNullOrEmpty(String string) {
    return string == null || string.equals(Constants.SEPARATOR_EMPTY);
  }

  public static JSONObject objectFromJSON(String json) {
    try {
      return (JSONObject) new JSONTokener(json).nextValue();
    } catch (JSONException exception) {
      System.out.println("JSONException: " + exception);
      return null;
    }
  }

  public static double secondsFromTimeString(String time) {
    String[] hms = time.split(Constants.SEPARATOR_COLON);
    double multiplier = 1.0;
    double milliseconds = 0.0;
    for (int i = hms.length - 1; i >= 0; i--) {
      milliseconds += (Double.parseDouble(hms[i]) * multiplier);
      multiplier *= 60.0;
    }
    return milliseconds;
  }

  public static String timeStringFromMillis(int millis, boolean includeHours) {
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(millis + (8 * 60 * 60 * 1000));
    SimpleDateFormat sdf = new SimpleDateFormat(includeHours ? "HH:mm:ss" : "mm:ss");
    return sdf.format(c.getTime());
  }
}
