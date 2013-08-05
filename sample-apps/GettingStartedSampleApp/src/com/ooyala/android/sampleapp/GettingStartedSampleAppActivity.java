package com.ooyala.android.sampleapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OoyalaPlayerLayoutController;

public class GettingStartedSampleAppActivity extends Activity {

  final String EMBED  = "lrZmRiMzrr8cP77PPW0W8AsjjhMJ1BBe";  //Embed Code, or Content ID
  final String PCODE  = "R2d3I6s06RyB712DN0_2GsQS-R-Y";
  final String DOMAIN = "www.ooyala.com";
  OoyalaPlayer player;
  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    OoyalaPlayerLayoutController playerLayoutController = new OoyalaPlayerLayoutController(playerLayout, PCODE, DOMAIN);
    player = playerLayoutController.getPlayer();
    if (player.setEmbedCode(EMBED)) {
      player.play();
    } else {
      Log.d(this.getClass().getName(), "Something Went Wrong!");
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (player != null) {
      player.suspend();
    }
  }

  @Override
  protected void onStart() {
    super.onRestart();
    if (player != null) {
      player.resume();
    }
  }
}