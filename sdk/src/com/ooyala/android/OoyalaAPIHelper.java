package com.ooyala.android;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import org.json.*;

import android.util.Log;

class OoyalaAPIHelper {

  // Static context and cookies to be used for MoviePlayer instantiation for 4.0+
  public static Map<String, String> cookies = new HashMap<String, String>();

  public static JSONObject objectForAPI(String host, String uri, Map<String, String> params) {
    URL url = Utils.makeURL(host, uri, params);
    if (url == null) { return null; }
    return objectForAPI(url);
  }

  public static JSONObject objectForAPI(URL url) {
    return Utils.objectFromJSON(jsonForAPI(url));
  }

  private static String jsonForAPI(URL url) {
    Log.d(OoyalaAPIHelper.class.getName(), "Sending Request: " + url.toString());
    StringBuffer sb = new StringBuffer();
    try {
      URLConnection conn = url.openConnection();

      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line;
      while ((line = rd.readLine()) != null) {
        sb.append(line);
      }
      rd.close();

      String headerName = null;
      for (int i = 1; (headerName = conn.getHeaderFieldKey(i)) != null; i++) {
        if (headerName.equals("Set-Cookie")) {
          String fullCookie = conn.getHeaderField(i);
          Log.d(OoyalaAPIHelper.class.getName(), "FOUND COOKIE: " + fullCookie);
          if (fullCookie.indexOf(";") > 0) {
            String cookie = fullCookie.substring(0, fullCookie.indexOf(";"));
            String cookieName = cookie.substring(0, cookie.indexOf("="));
            String cookieValue = cookie.substring(cookie.indexOf("=") + 1, cookie.length());
            OoyalaAPIHelper.cookies.put(cookieName, cookieValue);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return sb.toString();
  }
}
