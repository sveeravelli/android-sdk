package com.ooyala.android.sampleapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import com.ooyala.android.OoyalaPlayer;

public class OoyalaAndroidSampleAppActivity extends Activity
{
  private static final String TAG = "OoyalaSampleApp";

  private OoyalaPlayer player;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
      super.onCreate(savedInstanceState);
      Thread.setDefaultUncaughtExceptionHandler(onUncaughtException);
      setContentView(R.layout.main);

//      ((TextView)findViewById(R.id.start)).setText(Ad.FOO);
//
      player = (OoyalaPlayer)findViewById(R.id.player);
      player.setEmbedCode("someEmbedCode");

//      surface = (TappableSurfaceView)findViewById(R.id.surface);
//      surface.addTapListener(onTap);
//      holder=surface.getHolder();
//      holder.addCallback(this);
//      holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//
//      topPanel=findViewById(R.id.top_panel);
//      bottomPanel=findViewById(R.id.bottom_panel);
//
//      timeline = (ProgressBar)findViewById(R.id.timeline);
//
//      media = (ImageButton)findViewById(R.id.media);
//      media.setOnClickListener(onMedia);
//
//      go = (Button)findViewById(R.id.go);
//      go.setOnClickListener(onGo);
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
