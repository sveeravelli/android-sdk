package com.ooyala.android.imasampleapp;

import java.util.ArrayList;

import com.google.ads.interactivemedia.v3.api.CompanionAdSlot;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.ooyala.android.imasdk.*;
import com.ooyala.android.imasampleapp.R;
import com.ooyala.android.imasampleapp.R.id;
import com.ooyala.android.imasampleapp.R.layout;

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
    imaManager = new OoyalaIMAManager(this, playerLayoutController);

    ArrayList<CompanionAdSlot> companionAdSlots = new ArrayList<CompanionAdSlot>();
    ViewGroup companionView = (ViewGroup) findViewById(R.id.companionFrame);
    ImaSdkFactory sdkFactory = ImaSdkFactory.getInstance();
    CompanionAdSlot companionAdSlot = sdkFactory.createCompanionAdSlot();
    companionAdSlot.setContainer(companionView);
    companionAdSlot.setSize(300, 50);
    companionAdSlots.add(companionAdSlot);

    imaManager.setCompanionAdSlots(companionAdSlots);

    if (player.setEmbedCode(EMBED)) {
      player.play();
    } else {
      Log.d(this.getClass().getName(), "Something Went Wrong!");
    }
  }

}