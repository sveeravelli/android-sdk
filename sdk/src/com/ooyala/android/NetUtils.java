package com.ooyala.android;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

class NetUtils {
  public static void ping(URL url) {
    if (url == null) { return; }
    try {
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.connect();
      conn.getInputStream();
    } catch (IOException e) {
      Log.e(NetUtils.class.getName(), "Ping failed!!!");
    }
  }
}
