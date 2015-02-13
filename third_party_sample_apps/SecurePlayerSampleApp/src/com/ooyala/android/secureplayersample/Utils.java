package com.ooyala.android.secureplayersample;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.format.DateUtils;
import android.util.Log;

class Utils {

  public static final String SEPARATOR_AMPERSAND = "&";
  public static final String SEPARATOR_COMMA = ",";
  public static final String SEPARATOR_EMPTY = "";
  public static final String SEPARATOR_COLON = ":";

  public static URL makeURL(String host, String uri, Map<String, String> params) {
    return makeURL(host, uri, getParamsString(params, SEPARATOR_AMPERSAND, true));
  }

  public static URL makeURL(String host, String uri, String params) {
    try {
      return new URL(host + uri + (params == null || params.length() < 1 ? "" : "?" + params));
    } catch (MalformedURLException e) {
      DebugMode.logE(TAG, "Caught!", e);
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
    return string == null || string.equals(SEPARATOR_EMPTY);
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

  public static Map<String, String> mapFromJSONObject(JSONObject obj) {
    Map<String, String> map = new HashMap<String, String>();

    if (obj == null) {
      return map;
    }

    Iterator<?> itr = obj.keys();
    while (itr.hasNext()) {
      String key = (String)itr.next();
      try {
        map.put(key, obj.getString(key));
      } catch (JSONException e) {
        //do nothing
      }
    }

    return map;
  }

  public static double secondsFromTimeString(String time) {
    String[] hms = time.split(SEPARATOR_COLON);
    double multiplier = 1.0;
    double milliseconds = 0.0;
    for (int i = hms.length - 1; i >= 0; i--) {
      milliseconds += (Double.parseDouble(hms[i]) * multiplier);
      multiplier *= 60.0;
    }
    return milliseconds;
  }

  public static String timeStringFromMillis(int millis, boolean includeHours) {
    return DateUtils.formatElapsedTime(millis / 1000);
  }
}
