package com.ooyala.test.cases;

import com.ooyala.test.BaseInternalTestAppActivity;

import android.os.Bundle;

public class PlaybackInternalTestAppActivity extends BaseInternalTestAppActivity {

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
    embedMap.put("HLS Video",    "Y1ZHB1ZDqfhCPjYYRbCEOz0GR8IsVRm1");
    embedMap.put("MP4 Video",    "h4aHB1ZDqV7hbmLEv4xSOx3FdUUuephx");
    embedMap.put("VOD with CCs", "92cWp0ZDpDm4Q8rzHfVK6q9m6OtFP-ww");
    embedMap.put("WV-MP4",       "N3ZnF1ZDo2cUf0JIIFMaxv-gKgmF6Dvv");

    //Update the spinner with the embed map
    embedAdapter.addAll(embedMap.keySet());
    embedAdapter.notifyDataSetChanged();

  }

}