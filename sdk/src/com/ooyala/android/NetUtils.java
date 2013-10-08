package com.ooyala.android;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;

class PingTask extends AsyncTask<URL, Void, Void> {

  protected void onPostExecute() {
      return;
  }

  @Override
  protected Void doInBackground(URL... params) {
    for (URL url : params) {
      try {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        conn.getInputStream();
      } catch (Exception e) {
        Log.e(NetUtils.class.getName(), "Ping failed!!!");
      }
    }
    return null;
  }
}

class NetUtils {
  public static void ping(URL url) {
    if (url == null) { return; }
    new PingTask().execute(url);
  }
}
