package com.ooyala.test.cases;

import com.ooyala.test.BaseInternalTestAppActivity;

import android.os.Bundle;

public class OoyalaAdsInternalTestAppActivity extends BaseInternalTestAppActivity {

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

    //Populate the embed map
    embedMap.put("Ooyala Preroll",    "M4cmp0ZDpYdy8kiL4UD910Rw_DWwaSnU");
    embedMap.put("Ooyala Midroll",    "xhcmp0ZDpnDB2-hXvH7TsYVQKEk_89di");
    embedMap.put("Ooyala Postroll", "Rjcmp0ZDr5yFbZPEfLZKUveR_2JzZjMO");
    embedMap.put("VAST Preroll",       "Zlcmp0ZDrpHlAFWFsOBsgEXFepeSXY4c");
    embedMap.put("VAST Midroll",       "pncmp0ZDp7OKlwTPJlMZzrI59j8Imefa");
    embedMap.put("VAST Postroll",       "Zpcmp0ZDpaB-90xK8MIV9QF973r1ZdUf");
    embedMap.put("VAST Wrapper",       "5ybGV3ZTrqvbymwBC6ThtupFBptOp1rP");
    embedMap.put("Ooyala And VAST",       "Ftcmp0ZDoz8tALmhPcN2vMzCdg7YU9lc");

    //Update the spinner with the embed map
    embedAdapter.addAll(embedMap.keySet());
    embedAdapter.notifyDataSetChanged();

  }

}