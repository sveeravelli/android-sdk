package com.ooyala.android;

import android.os.Build;
import android.util.Base64;

import com.ooyala.android.util.DebugMode;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
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

public class Utils {
  static final String DEVICE_ANDROID_SDK = "android_sdk";
  /** TODO[jigish] change to android_hls_sdk when SAS is pushed */
  static final String DEVICE_ANDROID_HLS_SDK = "android_3plus_sdk";
  static final String DEVICE_IPAD = "ipad"; // hack for Washington Post - See PB-279

  static final String SEPARATOR_AMPERSAND = "&";
  static final String SEPARATOR_TIME = ":";
  static final String CHARSET = "UTF-8";

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
      if (key == null || params.get(key) == null) {
        continue;
      }

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

  /**
   * Get string from http get method
   * @param url the url
   * @param connectionTimeoutInMillisecond connectionTimeOut
   * @param readTimeoutInMillisecond readTimeout
   * @return a string of http response
   */
  public static String stringFromUrl (
      URL url,
      int connectionTimeoutInMillisecond,
      int readTimeoutInMillisecond) {
    DebugMode.logD(TAG, "Sending Http Request: " + url.toString());
    StringBuffer sb = new StringBuffer();
    BufferedReader rd = null;
    try {
      URLConnection conn = url.openConnection();
      conn.setConnectTimeout(connectionTimeoutInMillisecond);
      conn.setReadTimeout(readTimeoutInMillisecond);
      rd = new BufferedReader(new InputStreamReader(conn.getInputStream()), 8192);
      String line;
      while ((line = rd.readLine()) != null) {
        sb.append(line);
      }
    } catch (SocketTimeoutException e) {
      DebugMode.logE(TAG, "Connection to " + url.toString() + " timed out.");
    } catch (IOException e) {
      DebugMode.logE(TAG, "Caught!", e);
    } finally {
      if (rd != null) {
        try {
          rd.close();
        } catch (IOException e) {
          DebugMode.logE(TAG, "Caught!", e);
        }
      }
    }

    return sb.toString();
  }

  /**
   * calling http post method
   * @param url the url
   * @param body the post body
   * @param connectionTimeoutInMillisecond connectionTimeOut
   * @param readTimeoutInMillisecond readTimeout
   * @return a string of http response
   */
  public static void postUrl (
      URL url,
      Map<String, String> body,
      int connectionTimeoutInMillisecond,
      int readTimeoutInMillisecond) {
    String bodyString = getParamsString(body, SEPARATOR_AMPERSAND, true);
    StringBuffer sb = new StringBuffer();
    BufferedReader rd = null;

    try {
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setConnectTimeout(connectionTimeoutInMillisecond);
      conn.setReadTimeout(readTimeoutInMillisecond);
      conn.setDoOutput(true);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Accept-Charset", CHARSET);
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      conn.setRequestProperty("charset", CHARSET);
      OutputStream outputStream = conn.getOutputStream();
      outputStream.write(bodyString.getBytes(CHARSET));

      rd = new BufferedReader(new InputStreamReader(conn.getInputStream()), 8192);
      String line;
      while ((line = rd.readLine()) != null) {
        sb.append(line);
      }
    } catch (SocketTimeoutException e) {
      DebugMode.logE(TAG, "Connection to " + url.toString() + " timed out.");
    } catch (IOException e) {
      DebugMode.logE(TAG, "Caught!", e);
    } finally {
      if (rd != null) {
        try {
          rd.close();
        } catch (IOException e) {
          DebugMode.logE(TAG, "Caught!", e);
        }
      }
    }

    DebugMode.logD(TAG, "the http response for post method is" + sb.toString());
  }
}
