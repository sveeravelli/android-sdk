package com.ooyala.android.player;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Observable;

import android.os.AsyncTask;
import android.widget.FrameLayout;

import com.ooyala.android.AdsLearnMoreInterface;
import com.ooyala.android.OoyalaNotification;
import com.ooyala.android.util.DebugMode;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.StateNotifier;
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
        DebugMode.logE(PingTask.class.getName(), "Ping failed!!!");
      }
    }
    return null;
  }
}

public abstract class AdMoviePlayer extends MoviePlayer implements
    AdsLearnMoreInterface {
  protected StateNotifier _notifier;

  public void init(OoyalaPlayer parent, AdSpot ad, StateNotifier notifier) {
    _notifier = notifier;
  }

  @Override
  protected void setState(State state) {
    if (_notifier != null) {
      _notifier.setState(state);
    }
    super.setState(state);
  }

  @Override
  public void update(Observable arg0, Object arg1) {
    if (_notifier == null) {
      super.update(arg0, arg1);
    } else {
      final String name = ((OoyalaNotification)arg1).getName();
      if (name == OoyalaPlayer.STATE_CHANGED_NOTIFICATION_NAME) {
        _notifier.setState(getState());
      } else if (name == OoyalaPlayer.TIME_CHANGED_NOTIFICATION_NAME) {
        _notifier.notifyPlayheadChange();
      }
    }
  }

  public StateNotifier getNotifier() {
    return _notifier;
  }

  public abstract AdSpot getAd();

  public void updateLearnMoreButton(FrameLayout layout, int topMargin) { }

  public static void ping(URL url) {
    if (url == null) { return; }
    new PingTask().execute(url);
  }

}
