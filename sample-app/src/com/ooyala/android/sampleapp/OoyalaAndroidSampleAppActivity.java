package com.ooyala.android.sampleapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;

public class OoyalaAndroidSampleAppActivity extends Activity
{
  private static final String TAG = "OoyalaSampleApp";

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
    OoyalaPlayer p = new OoyalaPlayer("l1am06xhbSxa0OtyZsBTshW2DMtp.qDW-_", "GkUqcxL-5aeVBYG71aYQmlkMh62iBRgq8O-d6Y5w", "l1am06xhbSxa0OtyZsBTshW2DMtp", "www.ooyala.com");
    p.setLayout(layout);
    if (p.setEmbedCode("UwN2wxMzpU1Nl_qojlX8iLlKEHfl4HLM")) {
      Log.d(TAG, "TEST - yay!");
      p.play();
    } else {
      Log.d(TAG, "TEST - lame :(");
    }
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
