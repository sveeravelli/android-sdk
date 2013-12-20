package com.ooyala.test.cases;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.view.View;
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

  private final static String IMA_OVERRIDE_URL_FROM_CODE_LABEL = "IMA override url from code";
  OoyalaIMAManager imaManager;
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
    embedMap.put("IMA Preroll", "xoaWp0ZDrsQs20cgpMqddHI1jSFaCK5U");  //Created by us
    embedMap.put("IMA with Custom Ad Tag Parameters", "xoaWp0ZDrsQs20cgpMqddHI1jSFaCK5U");
    //embedMap.put("IMA Postroll", "VsaWp0ZDp2xNyhf3VWecMqDlg2uPItlw");
    embedMap.put("IMA Skip Ad (Google Provided)", "1waWp0ZDqGixRx40HtJcEmCi2wWoJokK");  //Google provided
    embedMap.put("IMA Wrapper (Google Provided)", "ByaWp0ZDrGk1S_vkI-VgHaT5JkS3LDo_");  //Google provided
    embedMap.put(IMA_OVERRIDE_URL_FROM_CODE_LABEL, "Q0Y21vaToE56_FVxaLFoXZxJlc0YVoiY"); 

    //Update the spinner with the embed map
    embedAdapter.addAll(embedMap.keySet());
    embedAdapter.notifyDataSetChanged();
  }

  @Override
  public void onClick(View v) {
    if (embedSpinner.getSelectedItem().equals( "IMA with Custom Ad Tag Parameters" )) {
      Map<String, String> adTagParameters = new HashMap<String, String>();

      adTagParameters.put("vid", embedMap.get(embedSpinner.getSelectedItem()));
      adTagParameters.put("pod", "2");
      adTagParameters.put("ppos", "2");
      adTagParameters.put("vpos", "preroll");
      adTagParameters.put("mridx", "2");

      imaManager.setAdTagParameters(adTagParameters);
    }
    else if (embedSpinner.getSelectedItem().equals( IMA_OVERRIDE_URL_FROM_CODE_LABEL )) {
      imaManager.setAdUrlOverride("http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/40797597/test_preroll_only&ciu_szs&impl=s&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=[referrer_url]&correlator=[timestamp]");
      imaManager.setAdTagParameters(null);
    }
    else {
      imaManager.setAdUrlOverride(null);
      imaManager.setAdTagParameters(null);
    }

    super.onClick(v);
  }

}