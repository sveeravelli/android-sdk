package com.ooyala.test.ima;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OptimizedOoyalaPlayerLayoutController;
import com.ooyala.android.imasdk.OoyalaIMAManager;
import com.ooyala.test.ima.R;
import com.ooyala.test.ima.R.id;

public class IMAInternalTestAppActivity extends Activity {

  Map<String, String> embedMap;

  final String EMBED  = "h5OWFoYTrG4YIPdrDKrIz5-VhobsuT-M";  //Embed Code, or Content ID
  final String PCODE  = "R2d3I6s06RyB712DN0_2GsQS-R-Y";
  final String DOMAIN = "ooyala.com";
  OptimizedOoyalaPlayerLayoutController playerLayoutController;
  OoyalaIMAManager imaManager;
  OoyalaPlayer player;
  Spinner embedSpinner;
  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    playerLayoutController = new OptimizedOoyalaPlayerLayoutController(playerLayout, PCODE, DOMAIN);
    player = playerLayoutController.getPlayer();


    embedMap = new HashMap<String, String>();
    embedMap.put("Pac-12 BasketBall, with VAST Midroll", "91bThhODokcxQNhlk3ttzNZs3HoTZ12M");
    embedMap.put("Wolverine",                            "1teWtjOjyk4Bc451kG4Obp1EjjQNWad1");
    embedMap.put("BMW Ad with IMA Preroll",              "h5OWFoYTrG4YIPdrDKrIz5-VhobsuT-M");

    //Initialize IMA classes
    imaManager = new OoyalaIMAManager(player);
    ViewGroup companionView = (ViewGroup) findViewById(R.id.companionFrame);

    imaManager.addCompanionSlot(companionView, 300, 50);

    if (player.setEmbedCode(EMBED)) {
    } else {
      Log.d(this.getClass().getName(), "Something Went Wrong!");
    }
    embedSpinner = (Spinner) findViewById(R.id.embedSpinner);
    Button setButton = (Button) findViewById(R.id.setButton);
    ArrayAdapter<String> embedAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
    embedAdapter.addAll(embedMap.keySet());
    embedSpinner.setAdapter(embedAdapter);
    setButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        player.setEmbedCode(embedMap.get(embedSpinner.getSelectedItem()));
      }
    });
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