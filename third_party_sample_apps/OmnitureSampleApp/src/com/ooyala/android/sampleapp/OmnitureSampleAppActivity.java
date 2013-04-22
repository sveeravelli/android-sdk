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
  private boolean shouldReopenOmni = false;

  final String EMBED  = "VvM2RuNzpA4jP_f7RZwlL5gke4hsFqOv";  //Embed Code, or Content ID
  final String PCODE  = "Uzbm46asiensk3opIgwfFn5KFemv";
  final String APIKEY = "";
  final String SECRET = "";
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
    } else {
      Log.d(this.getClass().getName(), "Something Went Wrong!");
    }
  }

  @Override
  public void update(Observable observable, Object data) {
    OoyalaPlayer player = (OoyalaPlayer) observable;
    // Omniture integration, reports to Omniture when player's STATE changes
    if (((String)data).equals(OoyalaPlayer.STATE_CHANGED_NOTIFICATION) &&
        !player.isShowingAd())
    {
      double playheadTime = ((double) player.getPlayheadTime()) / 1000;
      switch (player.getState()) {
        case INIT:
        case LOADING:
          // state of the player when seeking
          // don't need to do anything here since omniture only
          // cares about 'pause' and 'playing' state
          break;
        case READY:
          // state of the player when ready to play content
          // don't need to do anything here since we use the
          // currentItemChanged notification instead
          break;
        case PLAYING:
          if (shouldReopenOmni) {
            TrackingHelper.open(mediaName, mediaLength, playerName);
            Log.i(TAG, "omni:open " + mediaName + " " + mediaLength + " " + playerName);
          }
          TrackingHelper.play(mediaName, playheadTime);
          Log.i(TAG, "omni:play " + mediaName + " " + playheadTime);
          break;
        case PAUSED:
        case SUSPENDED:
          TrackingHelper.stop(mediaName, playheadTime);
          Log.i(TAG, "omni:stop " + mediaName + " " + playheadTime);
          break;
        case COMPLETED:
          // state of the player when video ends
          // don't need to do anything here since we use the
          // playCompleted notification instead
          break;
        case ERROR:
          // Player has error, call Media.stop() and Media.close()
          TrackingHelper.stop(mediaName, playheadTime);
          Log.i(TAG, "omni:stop " + mediaName + " " + playheadTime);
          TrackingHelper.close(mediaName);
          Log.i(TAG, "omni:close " + mediaName);
          break;
        default:
          // should never enter this!
          Log.e(TAG, "Player in unknown state");
          TrackingHelper.stop(mediaName, playheadTime);
          Log.i(TAG, "omni:stop " + mediaName + " " + playheadTime);
          TrackingHelper.close(mediaName);
          Log.i(TAG, "omni:close " + mediaName);
          break;
      }
    }
    else if (((String)data).equals(OoyalaPlayer.CURRENT_ITEM_CHANGED_NOTIFICATION))
    {
      mediaName = player.getCurrentItem().getTitle();
      mediaLength = ((double) player.getDuration()) / 1000;
      TrackingHelper.open(mediaName, mediaLength, playerName);
      Log.i(TAG, "omni:open " + mediaName + " " + mediaLength + " " + playerName);
    }
    else if (((String)data).equals(OoyalaPlayer.PLAY_COMPLETED_NOTIFICATION))
    {
      TrackingHelper.stop(mediaName, mediaLength);
      Log.i(TAG, "omni:stop " + mediaName + " " + mediaLength);
      TrackingHelper.close(mediaName);
      Log.i(TAG, "omni:close " + mediaName);
      shouldReopenOmni = true;
    }
  }
}