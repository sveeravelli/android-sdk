package com.ooyala.android.sampleapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OoyalaPlayerLayoutController;

public class GettingStartedSampleAppActivity extends Activity {
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    OoyalaPlayerLayoutController playerLayoutController = new OoyalaPlayerLayoutController(playerLayout,
        "R2d3I6s06RyB712DN0_2GsQS-R-Y.nCFrd", "UpmPCeDJspTKqLHO5IyZSRbsSiC7AM_rAqGztDRN",
        "R2d3I6s06RyB712DN0_2GsQS-R-Y", "www.ooyala.com");
    OoyalaPlayer player = playerLayoutController.getPlayer();
    if (player.setEmbedCode("lrZmRiMzrr8cP77PPW0W8AsjjhMJ1BBe")) {
      // The Embed Code works
      player.play();
    } else {
      Log.d(this.getClass().getName(), "Something Went Wrong!");
    }
  }
}