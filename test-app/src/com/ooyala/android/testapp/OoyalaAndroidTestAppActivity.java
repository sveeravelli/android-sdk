package com.ooyala.android.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;

public class OoyalaAndroidTestAppActivity extends Activity
{
  private static final String TAG = "OoyalaSampleApp";
  private OoyalaPlayer player;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    Log.d(TAG, "TEST - onCreate");
    super.onCreate(savedInstanceState);
    Thread.setDefaultUncaughtExceptionHandler(onUncaughtException);
    try {
      setContentView(R.layout.main);
    } catch (Exception e) {
      e.printStackTrace();
    }

    OoyalaPlayerLayout layout = (OoyalaPlayerLayout)findViewById(R.id.player);
    createPlayer();
    player.setLayout(layout, true);
    // Jigish's account: "l1am06xhbSxa0OtyZsBTshW2DMtp.qDW-_", "GkUqcxL-5aeVBYG71aYQmlkMh62iBRgq8O-d6Y5w", "l1am06xhbSxa0OtyZsBTshW2DMtp", "www.ooyala.com"
    // ooyala preroll:            g3N2wxMzqxoB84c3dan5xyXTxdrhX1km
    // ooyala midroll (5 sec):    c1d3AxMzo5_lJK08LHYfpzFF02StTtfk
    // ooyala postroll:           1ndnAxMzpxA4MFMw8G-F7frGiDYD_15p
    // ooyala ad as normal video: JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B
    // no ads:                    UwN2wxMzpU1Nl_qojlX8iLlKEHfl4HLM
    // VAST preroll:              w2cXAxMzqpwY5HwqSbHMzYgu92Lj6Fer
    // Channel:                   NueXAxMzqnfCtqVrgaEoD4-N8sFrt-nt
    // Chris' account: "Uzbm46asiensk3opIgwfFn5KFemv.vaDEj", "nARMtjWQh4hIprBNK_fJBf9xG_WWbhfr8IUAsxCr", "Uzbm46asiensk3opIgwfFn5KFemv", "www.ooyala.com"
    // VAST preroll:              JjMXg3MzoVTXb63DlH3AqPBOpE8hmLLR
    if (player.setEmbedCode("g3N2wxMzqxoB84c3dan5xyXTxdrhX1km")) {
      Log.d(TAG, "TEST - yay!");
      player.play();
    } else {
      Log.d(TAG, "TEST - lame :(");
    }
  }

  private void createPlayer() {
    Log.d(TAG, "TEST - createPlayer");
    if (player == null) {
      Log.d(TAG, "TEST - fetchOrCreatePlayer - null");
      player = new OoyalaPlayer("l1am06xhbSxa0OtyZsBTshW2DMtp.qDW-_", "GkUqcxL-5aeVBYG71aYQmlkMh62iBRgq8O-d6Y5w", "l1am06xhbSxa0OtyZsBTshW2DMtp", "www.ooyala.com");
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    Log.d(TAG, "TEST - onConfigurationChangedd");
    super.onConfigurationChanged(newConfig);
  }

  private Thread.UncaughtExceptionHandler onUncaughtException = new Thread.UncaughtExceptionHandler()
  {
    public void uncaughtException(Thread thread, Throwable ex)
    {
      Log.e(TAG, "Uncaught exception", ex);
      showErrorDialog(ex);
    }
  };

  private void showErrorDialog(Throwable t)
  {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);

    builder.setTitle("Exception!");
    builder.setMessage(t.toString());
    builder.setPositiveButton("OK", null);
    builder.show();
  }
}
