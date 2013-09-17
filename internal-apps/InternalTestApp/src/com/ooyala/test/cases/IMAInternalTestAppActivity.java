package com.ooyala.test.cases;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.ooyala.android.imasdk.OoyalaIMAManager;
import com.ooyala.test.BaseInternalTestAppActivity;
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
    OoyalaIMAManager imaManager;

    //Initialize IMA classes
    imaManager = new OoyalaIMAManager(player);
    imaManager.addCompanionSlot(companionView, 300, 50);

    //Populate the embed map
    embedMap.put("IMA Preroll", "xoaWp0ZDrsQs20cgpMqddHI1jSFaCK5U");
    embedMap.put("IMA Midroll", "pqaWp0ZDqo17Z-Dn_5YiVhjcbQYs5lhq");
    embedMap.put("IMA Postroll", "VsaWp0ZDp2xNyhf3VWecMqDlg2uPItlw");
    embedMap.put("IMA Skip Ad", "1waWp0ZDqGixRx40HtJcEmCi2wWoJokK");
    embedMap.put("IMA Wrapper", "ByaWp0ZDrGk1S_vkI-VgHaT5JkS3LDo_");

    //Update the spinner with the embed map
    embedAdapter.addAll(embedMap.keySet());
    embedAdapter.notifyDataSetChanged();

  }

}