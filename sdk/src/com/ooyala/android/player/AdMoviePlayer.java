package com.ooyala.android.player;

import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.FrameLayout;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.item.AdSpot;

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
        Log.e(PingTask.class.getName(), "Ping failed!!!");
      }
    }
    return null;
  }
}

public abstract class AdMoviePlayer extends MoviePlayer {

  public void init(OoyalaPlayer parent, AdSpot ad) { }

  public abstract AdSpot getAd();

  public void processClickThrough() { }

  public void updateLearnMoreButton(FrameLayout layout, int topMargin) { }

  public static void ping(URL url) {
    if (url == null) { return; }
    new PingTask().execute(url);
  }
}
