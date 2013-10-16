package com.ooyala.test.cases;

import com.ooyala.test.BaseInternalTestAppActivity;
import com.ooyala.test.R;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class PlaybackInternalTestAppActivity extends BaseInternalTestAppActivity {

//  //From the BaseInternalTestAppActivity
//  Map<String, String> embedMap;
//  OptimizedOoyalaPlayerLayoutController playerLayoutController;
//  OoyalaIMAManager imaManager;
//  OoyalaPlayer player;
//  Spinner embedSpinner;
  Button nextVideo;

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
    embedMap.put("Channel",      "ozNTJ2ZDqvPWyXTriQF_Ovcd1VuKHGdH");
    embedMap.put("Channel with pre-roll",    "FncDB0YTrvdMGK3Sva1NUmeQMuB33wbV");
    embedMap.put("Multiple Google IMA",      "4wbjhoYTp6oRqD6lslRn0xYVTbm2GBzh");

    //Update the spinner with the embed map
    embedAdapter.addAll(embedMap.keySet());
    embedAdapter.notifyDataSetChanged();

  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.setButton) {
      if (embedSpinner.getSelectedItem() == "Channel" || embedSpinner.getSelectedItem() == "Channel with pre-roll"
          || embedSpinner.getSelectedItem() == "Multiple Google IMA") {
        //If Channel is selected, add nextVideo button to controlsLayout if it doesn't already exist
        if (nextVideo == null) {
          nextVideo = new Button(this);
          nextVideo.setText("Next Video");
          nextVideo.setOnClickListener(this);
          nextVideo.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

          LinearLayout controlsLayout = (LinearLayout) findViewById(R.id.controlsLayout);
          controlsLayout.addView(nextVideo, 0);
        }
      } else {
        //Otherwise, if nextVideo button exists, remove it from controlsLayout
        if (nextVideo != null) {
          LinearLayout controlsLayout = (LinearLayout) findViewById(R.id.controlsLayout);
          controlsLayout.removeView(nextVideo);
          nextVideo = null;
        }
      }
      super.onClick(v);
    }
    else {
      //If nextVideo button was clicked on, play the next video
      player.nextVideo(0);
    }
  }
}