package com.ooyala.android.sampleapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OoyalaPlayerLayoutController;

public class OmnitureSampleAppActivity extends Activity {

  final String EMBED  = "I1cmRoNjpD5gJC7ZwNPQO7ZO7M1oahn5";  //Embed Code, or Content ID
  final String PCODE  = "duNXc6f4D_IbiXlga2Hf4mmeNcs4";
  final String APIKEY = "duNXc6f4D_IbiXlga2Hf4mmeNcs4.i1kV7";
  final String SECRET = "e7GbSZZ_Q4lospCtCltE5eLWUElv7DrYnsaoJ4-j";
  final String DOMAIN = "www.ooyala.com";

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    TrackingHelper.configureAppMeasurement(this);
    TrackingHelper.configureMediaMeasurement();

    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    OoyalaPlayerLayoutController playerLayoutController = new OoyalaPlayerLayoutController(playerLayout,
        APIKEY, SECRET, PCODE, DOMAIN);
    OoyalaPlayer player = playerLayoutController.getPlayer();
    if (player.setEmbedCode(EMBED)) {
      player.play();
    } else {
      Log.d(this.getClass().getName(), "Something Went Wrong!");
    }
  }
}