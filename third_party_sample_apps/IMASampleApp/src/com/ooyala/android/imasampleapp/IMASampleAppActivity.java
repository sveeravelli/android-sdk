package com.ooyala.android.imasampleapp;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.PlayerDomain;
import com.ooyala.android.imasdk.OoyalaIMAManager;
import com.ooyala.android.ui.OptimizedOoyalaPlayerLayoutController;

/**
 * A Sample integration of OoyalaPlayer and Google IMA Manager
 *
 * This application will not run unless you link Google Play Service's project
 *
 * http://developer.android.com/google/play-services/setup.html
 *
 *
 */
public class IMASampleAppActivity extends Activity implements Observer {

  final String PCODE  = "R2d3I6s06RyB712DN0_2GsQS-R-Y";
  final String DOMAIN = "http://www.ooyala.com";
  OptimizedOoyalaPlayerLayoutController playerLayoutController;
  OoyalaIMAManager imaManager;
  OoyalaPlayer player;

  private Map<String, String> embedMap;
  private Spinner embedSpinner;
  private Button setButton;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    playerLayoutController = new OptimizedOoyalaPlayerLayoutController(playerLayout, PCODE, new PlayerDomain(DOMAIN));
    player = playerLayoutController.getPlayer();
    player.addObserver(this);

    //Initialize IMA classes
    imaManager = new OoyalaIMAManager(player);
    ViewGroup companionView = (ViewGroup) findViewById(R.id.companionFrame);
    imaManager.addCompanionSlot(companionView, 300, 50);

    embedMap = new LinkedHashMap<String, String>();
    embedMap.put("Ad-Rules Preroll", "EzZ29lcTq49IswgZYkMknnU4Ukb9PQMH");
    embedMap.put("Ad-Rules Midroll", "VlaG9lcTqeUU18adfd1DVeQ8YekP3H4l");
    embedMap.put("Ad-Rules Postroll", "BnaG9lcTqLXQNyod7ON8Yv3eDas2Oog6");
    embedMap.put("Podded Preroll", "1wNjE3cDox0G3hQIWxTjsZ8MPUDLSkDY");
    embedMap.put("Podded Midroll", "1yNjE3cDodUEfUfp2WNzHkCZCMb47MUP");
    embedMap.put("Podded Postroll", "1sNjE3cDoN3ZewFm1238ce730J4BMrEJ");
    embedMap.put("Podded Pre-Mid-Post", "ZrOTE3cDoXo2sLOWzQPxjS__M-Qk32Co");
    embedMap.put("Skippable", "FhbGRjbzq8tfaoA3dhfxc2Qs0-RURJfO");
    embedMap.put("Pre, Mid and Post Skippable", "10NjE3cDpj8nUzYiV1PnFsjC6nEvPQAE");

    embedSpinner = (Spinner) findViewById(R.id.embedSpinner);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        R.layout.spinner_layout);
    for (String key : embedMap.keySet()) {
      adapter.add(key);
    }
    adapter.notifyDataSetChanged();
    embedSpinner.setAdapter(adapter);
    setButton = (Button) findViewById(R.id.setButton);
    setButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View arg0) {
        Object embedKey = embedSpinner.getSelectedItem();
        if (embedKey == null) {
          return;
        }
        String embed = embedMap.get(embedKey.toString());
       // imaManager.setAdUrlOverride(adtag);
       // imaManager.setAdTagParameters(null);
        if (player.setEmbedCode(embed)) {
          player.play();
        } else {
          Log.d(this.getClass().getName(), "Something Went Wrong!");
        }
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

  @Override
  public void update(Observable arg0, Object arg1) {
    if (arg1 == OoyalaPlayer.TIME_CHANGED_NOTIFICATION) {
      return;
    }
    Log.d(IMASampleAppActivity.class.getSimpleName(), "Notification Received: " + arg1 + " - state: " + player.getState());
  }
}
