package com.ooyala.android;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.Build;
import android.util.Base64;

import com.ooyala.android.util.DebugMode;

public class Utils {
  static final String DEVICE_ANDROID_SDK = "android_sdk";
  /** TODO[jigish] change to android_hls_sdk when SAS is pushed */
  static final String DEVICE_ANDROID_HLS_SDK = "android_3plus_sdk";
  static final String DEVICE_IPAD = "ipad"; // hack for Washington Post - See PB-279

  static final String SEPARATOR_AMPERSAND = "&";
  static final String SEPARATOR_TIME = ":";

  private static final String TAG = Utils.class.getSimpleName();

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

  public static Object getJSONValueOrElse( JSONObject json, String key, Object orElse ) {
    Object value;
    try {
      value = json.get( key );
    }
    catch( JSONException e ) {
      value = orElse;
    }
    return value;
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

  public static void overwriteJSONObject( JSONObject src, JSONObject dst ) throws JSONException {
    if( src != null && dst != null ) {
      final Iterator<?> ccks = src.keys();
      while( ccks.hasNext() ) {
        final String ck = String.valueOf( ccks.next() );
        final Object cv = src.get( ck );
        dst.put( ck, cv );
      }
    }
  }

  public static String encryptString(String rawString) {
    byte[] bytes = rawString.getBytes();
    MessageDigest digest = null;
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      DebugMode.logE(TAG, "Caught!", e);
      return null;
    }
    digest.reset();
    String encrypted = Base64.encodeToString(digest.digest(bytes),
        Base64.DEFAULT);
    return encrypted;
  }

  /**
   * Block the current thread while getting the Ooyala Player Token
   * @param generator an implemented EmbedTokenGenerator to generate an embed code, can be null
   * @param embedCodes the List of embed codes which need a generated embed token
   * @return a string of the Ooyala Player Token, or null if there is no generator or an error
   */
  public static String blockingGetEmbedTokenForEmbedCodes(EmbedTokenGenerator generator, List<String> embedCodes) {
    if (generator != null) {
      DebugMode.logD(TAG, "Requesting an OPT for Chromecast");
      final Semaphore sem = new Semaphore(0);
      final AtomicReference<String> tokenReference = new AtomicReference<String>();
      generator.getTokenForEmbedCodes(embedCodes, new EmbedTokenGeneratorCallback() {

        @Override
        public void setEmbedToken(String token) {
          tokenReference.set(token);
          sem.release();
        }
      });
      try {
        sem.acquire();
      } catch (InterruptedException e) {
        DebugMode.logE(TAG, "Embed Token request was interrupted");
        return null;
      }
      return tokenReference.get();
    }
    else {
      DebugMode.logD(TAG, "No embed token generator to get an OPT");
      return null;
    }
  }
}
