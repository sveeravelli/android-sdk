package com.ooyala.test;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.ooyala.android.imasdk.OoyalaIMAManager;
import com.ooyala.test.R;

public class IMAInternalTestAppActivity extends BaseInternalTestAppActivity {

//  //From the BaseInternalTestAppActivity
//  Map<String, String> embedMap;
//  OptimizedOoyalaPlayerLayoutController playerLayoutController;
//  OoyalaIMAManager imaManager;
//  OoyalaPlayer player;
//  Spinner embedSpinner;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //Add the companion ad view to the layout
    LinearLayout controlsLayout = (LinearLayout) findViewById(R.id.controlsLayout);
    ViewGroup companionView = (ViewGroup) getLayoutInflater().inflate(R.layout.companion_slot, null);
    controlsLayout.addView(companionView, 0);

    //Initialize IMA classes
    imaManager = new OoyalaIMAManager(player);
    imaManager.addCompanionSlot(companionView, 300, 50);

    //Populate the embed map
    embedMap.put("Pac-12 BasketBall, with VAST Midroll", "91bThhODokcxQNhlk3ttzNZs3HoTZ12M");
    embedMap.put("Wolverine",                            "1teWtjOjyk4Bc451kG4Obp1EjjQNWad1");
    embedMap.put("BMW Ad with IMA Preroll",              "h5OWFoYTrG4YIPdrDKrIz5-VhobsuT-M");

    //Update the spinner with the embed map
    embedAdapter.addAll(embedMap.keySet());
    embedAdapter.notifyDataSetChanged();

  }

}