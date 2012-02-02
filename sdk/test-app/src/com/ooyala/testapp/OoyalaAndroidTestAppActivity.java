package com.ooyala.testapp;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.ooyala.android.LocalizationSupport;
import com.ooyala.android.OoyalaAdSpot;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OptimizedOoyalaPlayerLayoutController;
import com.ooyala.android.testapp.R;

public class OoyalaAndroidTestAppActivity extends Activity implements OnClickListener, Observer {
  private static final String TAG = "OoyalaSampleApp";
  private OoyalaPlayer player;

  private Button skipAd;
  private Button insertAd;

  private boolean metadataReady = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.d(TAG, "TEST - onCreate");
    super.onCreate(savedInstanceState);
    Thread.setDefaultUncaughtExceptionHandler(onUncaughtException);
    try {
      setContentView(R.layout.main);
    } catch (Exception e) {
      e.printStackTrace();
    }

    skipAd = (Button) findViewById(R.id.skipAd);
    skipAd.setOnClickListener(this);
    insertAd = (Button) findViewById(R.id.insertAd);
    insertAd.setOnClickListener(this);

    // optional localization
    // LocalizationSupport.useLocalizedStrings(LocalizationSupport.loadLocalizedStrings("ja_JP"));

    OptimizedOoyalaPlayerLayoutController layoutController = new OptimizedOoyalaPlayerLayoutController(
        (OoyalaPlayerLayout) findViewById(R.id.player), "l1am06xhbSxa0OtyZsBTshW2DMtp.qDW-_",
        "GkUqcxL-5aeVBYG71aYQmlkMh62iBRgq8O-d6Y5w", "l1am06xhbSxa0OtyZsBTshW2DMtp", "www.ooyala.com");
    player = layoutController.getPlayer();
    player.setAdsSeekable(true); // this will help us skip ads if need be.
    player.addObserver(this);
    // Jigish's account: "l1am06xhbSxa0OtyZsBTshW2DMtp.qDW-_", "GkUqcxL-5aeVBYG71aYQmlkMh62iBRgq8O-d6Y5w",
    // "l1am06xhbSxa0OtyZsBTshW2DMtp", "www.ooyala.com"
    // ooyala preroll: g3N2wxMzqxoB84c3dan5xyXTxdrhX1km
    // ooyala midroll (5 sec): c1d3AxMzo5_lJK08LHYfpzFF02StTtfk
    // ooyala postroll: 1ndnAxMzpxA4MFMw8G-F7frGiDYD_15p
    // ooyala ad as normal video: JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B
    // no ads: UwN2wxMzpU1Nl_qojlX8iLlKEHfl4HLM
    // VAST preroll: w2cXAxMzqpwY5HwqSbHMzYgu92Lj6Fer
    // Channel: NueXAxMzqnfCtqVrgaEoD4-N8sFrt-nt
    // Chris' account: "Uzbm46asiensk3opIgwfFn5KFemv.vaDEj", "nARMtjWQh4hIprBNK_fJBf9xG_WWbhfr8IUAsxCr",
    // "Uzbm46asiensk3opIgwfFn5KFemv", "www.ooyala.com"
    // VAST preroll: JjMXg3MzoVTXb63DlH3AqPBOpE8hmLLR
    // Greg's account: "0wcnI6LKT5GqU9sQ9MkK5kuhzAAS.aKvTv", "VKhKkuAsJ77YI8DYfBODi6r36GPPr-tj5k8oDdcd",
    // "0wcnI6LKT5GqU9sQ9MkK5kuhzAAS", "www.ooyala.com"
    // HLS: "9ydnRhMzq-roTTbvwmG20FIwMEB08xom"
    // Live Streaming: "d0b206YlI7etqD1HscU4iP3LsVa6.IFGQt", "6J20fobZxUBbXSPF8DVfQURTNTddnHuhuhhE2CZV",
    // "d0b206YlI7etqD1HscU4iP3LsVa6", "www.tcncountry.com"
    // Live with 2 prerolls: "RiOWNxMjrf8Gcexqv78Uf9b2w0PsJBzh"

    if (player.setEmbedCode("UwN2wxMzpU1Nl_qojlX8iLlKEHfl4HLM")) {
      Log.d(TAG, "TEST - yay!");
      player.play();
    } else {
      Log.d(TAG, "TEST - lame :(");
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    Log.d(TAG, "---------------- Stop -----------");
    if (player != null) {
      player.suspend();
    }
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    Log.d(TAG, "---------------- Restart -----------");
    if (player != null) {
      player.resume();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    player = null;
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    Log.d(TAG, "TEST - onConfigurationChangedd");
    super.onConfigurationChanged(newConfig);
  }

  private Thread.UncaughtExceptionHandler onUncaughtException = new Thread.UncaughtExceptionHandler() {
    public void uncaughtException(Thread thread, Throwable ex) {
      Log.e(TAG, "Uncaught exception", ex);
      showErrorDialog(ex);
    }
  };

  private void showErrorDialog(Throwable t) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);

    builder.setTitle("Exception!");
    builder.setMessage(t.toString());
    builder.setPositiveButton("OK", null);
    builder.show();
  }

  @Override
  public void onClick(View arg0) {
    if (player != null && arg0 == skipAd) {
      player.skipAd();
    } else if (player != null && arg0 == insertAd) {
      if (metadataReady) {
        Log.d(TAG, "AD - INSERTING!");
        player.getCurrentItem().insertAd(
            new OoyalaAdSpot(10000, null, null, "JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B"));
      }
    }
  }

  @Override
  public void update(Observable arg0, Object arg1) {
    Log.d(TAG, "Recieved Notification: " + arg1);
    if (arg1 == OoyalaPlayer.METADATA_READY_NOTIFICATION) {
      metadataReady = true;
      Log.d(TAG, "AD - metadata true!");
    }
    // if (((String)arg1).equals(OoyalaPlayer.STATE_CHANGED_NOTIFICATION) && ((OoyalaPlayer)arg0).getState()
    // == State.READY) {
    // player.play();
    // }
  }
}
