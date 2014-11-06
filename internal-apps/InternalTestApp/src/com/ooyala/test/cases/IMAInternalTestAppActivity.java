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
    embedMap.put("Ad-Rules Preroll", "EzZ29lcTq49IswgZYkMknnU4Ukb9PQMH");
    embedMap.put("Ad-Rules Midroll", "VlaG9lcTqeUU18adfd1DVeQ8YekP3H4l");
    embedMap.put("Ad-Rules Postroll", "BnaG9lcTqLXQNyod7ON8Yv3eDas2Oog6");
    embedMap.put("Podded Preroll", "1wNjE3cDox0G3hQIWxTjsZ8MPUDLSkDY");
    embedMap.put("Podded Midroll", "1yNjE3cDodUEfUfp2WNzHkCZCMb47MUP");
    embedMap.put("Podded Postroll", "1sNjE3cDoN3ZewFm1238ce730J4BMrEJ");
    embedMap.put("Podded Pre-Mid-Post", "ZrOTE3cDoXo2sLOWzQPxjS__M-Qk32Co");
    embedMap.put("Skippable", "FhbGRjbzq8tfaoA3dhfxc2Qs0-RURJfO");
    embedMap.put("Non Ad-Rules Preroll", "FlbGRjbzptyEbStMiMLcyNQE6l6TMgwq");
    embedMap.put("Non Ad-Rules Midroll", "xrbGRjbzoBJUwtSLOHrcceTvMBe5pZdN");
    embedMap.put("Non Ad-Rules Postroll", "FjbGRjbzp0DV_5-NtXBVo5Rgp3Sj0R5C");
    embedMap.put("Pre, Mid and Post Skippable", "FhbGRjbzq8tfaoA3dhfxc2Qs0-RURJfO");

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