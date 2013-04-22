//
//  OmnitureSampeAppActivity.java
//  OmnitureSampleApp
//
//  Created by Aldi Gunawan on 4/16/13.
//  Copyright (c) 2013 Ooyala, Inc. All rights reserved.
//

package com.ooyala.android.sampleapp;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.ActionAtEnd;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OoyalaPlayerLayoutController;

public class OmnitureSampleAppActivity extends Activity implements Observer {
  private static final String TAG = "OmnitureSampleApp";
  private static final String playerName = "Android_Sample_Player";
  private String mediaName;
  private double mediaLength;

  final String EMBED  = "ZlMDJ4NTp8nbPls_7lJX8AJD3Nm0UNC8";  //Embed Code, or Content ID
  final String PCODE  = "B3MDExOuTldXc1CiXbzAauYN7Iui";
  final String APIKEY = "B3MDExOuTldXc1CiXbzAauYN7Iui.SMqJf";
  final String SECRET = "M6Cj01VpLjTXzS65Xeb4Y9KrvO1B-ZhyUPH8kNKE";
  final String DOMAIN = "www.ooyala.com";

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Initialized omniture App Measurement
    TrackingHelper.configureAppMeasurement(this);
    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    OoyalaPlayerLayoutController playerLayoutController = new OoyalaPlayerLayoutController(playerLayout,
        APIKEY, SECRET, PCODE, DOMAIN);
    OoyalaPlayer player = playerLayoutController.getPlayer();
    if (player.setEmbedCode(EMBED)) {
      // Initialized Omniture media Measurement
      TrackingHelper.configureMediaMeasurement();
      player.addObserver(this);
      player.setActionAtEnd(ActionAtEnd.STOP);
    } else {
      Log.d(this.getClass().getName(), "Something Went Wrong!");
    }
  }

  @Override
  public void update(Observable observable, Object data) {
    // Omniture integration, reports to Omniture when player's STATE changes
    if (((String)data).equals(OoyalaPlayer.STATE_CHANGED_NOTIFICATION)) {
      OoyalaPlayer player = (OoyalaPlayer) observable;
      double playheadTime = ((double) player.getPlayheadTime()) / 1000;
      switch (player.getState()) {

        // Player is ready, call Media.open()
        case READY:
          mediaName = player.getCurrentItem().getTitle();
          mediaLength = ((double) player.getDuration()) / 1000;
          TrackingHelper.open(mediaName, mediaLength, playerName);
          Log.i(TAG, "omni:open " + mediaName + " " + mediaLength + " " + playerName);
          break;

        // Player is playing, call Media.play()
        case PLAYING:
          TrackingHelper.play(mediaName, playheadTime);
          Log.i(TAG, "omni:play " + mediaName + " " + playheadTime);
          break;

        // Player is paused, call Media.stop()
        case PAUSED:
        case SUSPENDED:
          TrackingHelper.stop(mediaName, playheadTime);
          Log.i(TAG, "omni:stop " + mediaName + " " + playheadTime);
          break;

        // Player is completed, call Media.stop() with the full duration of video, then Media.close()
        case COMPLETED:
          TrackingHelper.stop(mediaName, mediaLength);
          Log.i(TAG, "omni:stop " + mediaName + " " + mediaLength);
          TrackingHelper.close(mediaName);
          Log.i(TAG, "omni:close " + mediaName);
          break;

        // Player has error, call Media.stop() and Media.close()
        case ERROR:
          TrackingHelper.stop(mediaName, playheadTime);
          Log.i(TAG, "omni:stop " + mediaName + " " + playheadTime);
          TrackingHelper.close(mediaName);
          Log.i(TAG, "omni:close " + mediaName);
          break;
      }
    }
  }
}