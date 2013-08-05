package com.ooyala.android.imasampleapp;


import com.ooyala.android.imasdk.*;
import com.ooyala.android.imasampleapp.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OoyalaPlayerLayoutController;

public class IMASampleAppActivity extends Activity {

  final String EMBED  = "h5OWFoYTrG4YIPdrDKrIz5-VhobsuT-M";  //Embed Code, or Content ID
  final String PCODE  = "R2d3I6s06RyB712DN0_2GsQS-R-Y";
  final String DOMAIN = "ooyala.com";
  OoyalaPlayerLayoutController playerLayoutController;
  OoyalaIMAManager imaManager;
  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    playerLayoutController = new OoyalaPlayerLayoutController(playerLayout, PCODE, DOMAIN);
    OoyalaPlayer player = playerLayoutController.getPlayer();

    //Initialize IMA classes
    imaManager = new OoyalaIMAManager(player);
    ViewGroup companionView = (ViewGroup) findViewById(R.id.companionFrame);

    imaManager.addCompanionSlot(companionView, 300, 50);

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
  protected void onStart() {
    super.onRestart();
    if (playerLayoutController.getPlayer() != null) {
      playerLayoutController.getPlayer().resume();
    }
  }

}