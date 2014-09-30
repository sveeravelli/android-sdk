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
    embedMap.put("Freewheel Preroll", "Q5MXg2bzq0UAXXMjLIFWio_6U0Jcfk6v");
    embedMap.put("Freewheel Midroll", "NwcGg4bzrwxc6rqAZbYij4pWivBsX57a");
    embedMap.put("Freewheel Postroll", "NmcGg4bzqbeqXO_x9Rfj5IX6gwmRRrse");
    embedMap.put("Freewheel PreMidPost", "NqcGg4bzoOmMiV35ZttQDtBX1oNQBnT-");
    embedMap.put("Freewheel Overlay", "NucGg4bzrVrilZrMdlSA9tyg6Vty46DN");
    embedMap.put("Freewheel Skip Ad (Can't Skip)", "NocGg4bzoRJ1yu3JY3LP44tVlsC2E_gi");

    embedMap.put("Freewheel Pre, Mid, Postroll + Overlay (OLD)", "VmMnA4ZzoSrZEiptnTUbroi7BqpNqotP");
    embedMap.put("Freewheel Channel (OLD)", "4xc2FoaToqPyQMG0Phi4h2Ca9NYrx6-K");
    //Update the spinner with the embed map
    embedAdapter.addAll(embedMap.keySet());
    embedAdapter.notifyDataSetChanged();
  }

  @Override
  public void onClick(View v) {
    playerLayoutController.getControls().setVisible(true); //when ads are destroyed, controls may be invisible

    if (embedSpinner.getSelectedItem().toString().contains("OLD")) {
      Map<String, String> freewheelParameters = new HashMap<String, String>();
      freewheelParameters.put("fw_android_mrm_network_id",  "90750");
      freewheelParameters.put("fw_android_ad_server", "http://g1.v.fwmrm.net/");
      freewheelParameters.put("fw_android_player_profile",  "90750:ooyala_android");
      freewheelParameters.put("fw_android_site_section_id", "ooyala_test_site_section");
      freewheelParameters.put("fw_android_video_asset_id",  "ooyala_test_video_with_bvi_cuepoints");
      freewheelManager.overrideFreewheelParameters(freewheelParameters);

    } else {
      Map<String, String> freewheelParameters = new HashMap<String, String>();
      freewheelParameters.put("fw_android_ad_server", "http://g1.v.fwmrm.net/");
      freewheelParameters.put("fw_android_player_profile",  "90750:ooyala_android");
      freewheelParameters.put("fw_android_site_section_id", "ooyala_android_internalapp");
      freewheelParameters.put("fw_android_video_asset_id",  embedMap.get(embedSpinner.getSelectedItem()));

      freewheelManager.overrideFreewheelParameters(freewheelParameters);
    }
    super.onClick(v);
  }
}