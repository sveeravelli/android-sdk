package com.ooyala.android.freewheelsampleapp;


import java.util.HashMap;
import java.util.Map;

import com.ooyala.android.freewheelsdk.OoyalaFreewheelManager;
import com.ooyala.android.freewheelsampleapp.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.PlayerDomain;
import com.ooyala.android.ui.OptimizedOoyalaPlayerLayoutController;

public class FreewheelSampleAppActivity extends Activity {

  final String EMBED  = "RlODZyZDr93PAbk-a9fY7Phq93pA-Uwt";
  final String PCODE  = "5idHc6Pt1kJ18w4u9Q5jEwAQDYCH";
  final String DOMAIN = "http://www.ooyala.com";

  OptimizedOoyalaPlayerLayoutController playerLayoutController;
  OoyalaFreewheelManager freewheelManager;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    playerLayoutController = new OptimizedOoyalaPlayerLayoutController(playerLayout, PCODE, new PlayerDomain(DOMAIN));
    OoyalaPlayer player = playerLayoutController.getPlayer();

    //Initialize Freewheel Ad Manager
    freewheelManager = new OoyalaFreewheelManager(this, playerLayoutController);

    //Set Freewheel parameters. Note that these are optional, and override configurations set in Backlot or in Ooyala internals
    Map<String, String> freewheelParameters = new HashMap<String, String>();
    //freewheelParameters.put("fw_android_mrm_network_id",  "90750");
    freewheelParameters.put("fw_android_ad_server",       "http://demo.v.fwmrm.net/");
    freewheelParameters.put("fw_android_player_profile",  "90750:ooyala_android");
    freewheelParameters.put("FRMSegment",  "channel=TEST;subchannel=TEST;section=TEST;mode=online;player=ooyala;beta=n");
    //freewheelParameters.put("fw_android_site_section_id", "ooyala_test_site_section");
    //freewheelParameters.put("fw_android_video_asset_id",  "ooyala_test_video_with_bvi_cuepoints");

    freewheelManager.overrideFreewheelParameters(freewheelParameters);

    if (player.setEmbedCode(EMBED)) {
      player.play();
    } else {
      Log.d(this.getClass().getName(), "Something Went Wrong!");
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (playerLayoutController.getPlayer() != null) {
      playerLayoutController.getPlayer().suspend();
    }
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    if (playerLayoutController.getPlayer() != null) {
      playerLayoutController.getPlayer().resume();
    }
  }
}