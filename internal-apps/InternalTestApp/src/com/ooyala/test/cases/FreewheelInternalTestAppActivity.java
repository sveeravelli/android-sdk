package com.ooyala.test.cases;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.view.View;

import com.ooyala.android.freewheelsdk.OoyalaFreewheelManager;
import com.ooyala.test.BaseInternalTestAppActivity;

/**
 * This is the test cases for Freewheel Ad Manager integration
 * NOTE: Only the first request will properly show ads.
 * @author hannah.kang
 *
 */
public class FreewheelInternalTestAppActivity extends BaseInternalTestAppActivity {

//  //From the BaseInternalTestAppActivity
//  Map<String, String> embedMap;
//  OptimizedOoyalaPlayerLayoutController playerLayoutController;
//  OoyalaPlayer player;
//  Spinner embedSpinner;

  OoyalaFreewheelManager freewheelManager;
  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //Initialize Freewheel Manager
    freewheelManager = new OoyalaFreewheelManager(this, playerLayoutController);

    //Populate the embed map
    embedMap.put("Freewheel Preroll", "BtbG54Zjq5mU5cWzCXtanps0pfQ99ckS");
    embedMap.put("Freewheel Pre, Mid, Postroll + Overlay", "VmMnA4ZzoSrZEiptnTUbroi7BqpNqotP");
    embedMap.put("Freewheel Channel", "4xc2FoaToqPyQMG0Phi4h2Ca9NYrx6-K");

    //Update the spinner with the embed map
    embedAdapter.addAll(embedMap.keySet());
    embedAdapter.notifyDataSetChanged();
  }

  @Override
  public void onClick(View v) {
    playerLayoutController.getControls().setVisible(true); //when ads are destroyed, controls may be invisible

    if (embedSpinner.getSelectedItem() == "Freewheel Preroll") {
      Map<String, String> freewheelParameters = new HashMap<String, String>();

      freewheelParameters.put("fw_android_mrm_network_id",  "112214");
      freewheelParameters.put("fw_android_ad_server",       "http://1b656.v.fwmrm.net");
      freewheelParameters.put("fw_android_player_profile",  "112214:univision_live_android");
      freewheelParameters.put("fw_android_site_section_id", "NEWS_NOTICIEROUNIVISION_VIDEOS");
      freewheelParameters.put("fw_android_video_asset_id",  "2802886");

      freewheelManager.overrideFreewheelParameters(freewheelParameters);
    }
    else {
      Map<String, String> freewheelParameters = new HashMap<String, String>();

      freewheelParameters.put("fw_android_mrm_network_id",  "90750");
      freewheelParameters.put("fw_android_ad_server",       "http://demo.v.fwmrm.net/");
      freewheelParameters.put("fw_android_player_profile",  "90750:ooyala_android");
      freewheelParameters.put("fw_android_site_section_id", "ooyala_test_site_section");
      freewheelParameters.put("fw_android_video_asset_id",  "ooyala_test_video_with_bvi_cuepoints");

      freewheelManager.overrideFreewheelParameters(freewheelParameters);
    }
    super.onClick(v);
  }
}