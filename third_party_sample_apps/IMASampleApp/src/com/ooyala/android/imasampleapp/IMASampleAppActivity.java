package com.ooyala.android.imasampleapp;


import java.util.HashMap;
import java.util.Map;

import com.ooyala.android.imasdk.*;
import com.ooyala.android.imasampleapp.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OptimizedOoyalaPlayerLayoutController;
import com.ooyala.android.PlayerDomain;

public class IMASampleAppActivity extends Activity {

  final String EMBED  = "h5OWFoYTrG4YIPdrDKrIz5-VhobsuT-M";  //Embed Code, or Content ID
  final String PCODE  = "R2d3I6s06RyB712DN0_2GsQS-R-Y";
  final String DOMAIN = "http://www.ooyala.com";
  OptimizedOoyalaPlayerLayoutController playerLayoutController;
  OoyalaIMAManager imaManager;
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

    //Initialize IMA classes
    imaManager = new OoyalaIMAManager(player);
    ViewGroup companionView = (ViewGroup) findViewById(R.id.companionFrame);
    imaManager.addCompanionSlot(companionView, 300, 50);

    Map<String, String> adTagParameters = new HashMap<String, String>();

    adTagParameters.put("vid", EMBED);
    adTagParameters.put("url", "[referrer_url]");
    adTagParameters.put("pod", "2");
    adTagParameters.put("ppos", "2");
    adTagParameters.put("vpos", "preroll");
    adTagParameters.put("mridx", "2");
    imaManager.setAdTagParameters(adTagParameters);

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