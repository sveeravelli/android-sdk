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
  private OoyalaPlayer player;
  private String mediaName;
  private double mediaLength;

  final String EMBED  = "9xam9iYTrS-frHrel1D9hpzw669Xu88p";  //Embed Code, or Content ID
  final String PCODE  = "duNXc6f4D_IbiXlga2Hf4mmeNcs4";
  final String APIKEY = "duNXc6f4D_IbiXlga2Hf4mmeNcs4.i1kV7";
  final String SECRET = "e7GbSZZ_Q4lospCtCltE5eLWUElv7DrYnsaoJ4-j";
  final String DOMAIN = "www.ooyala.com";

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    TrackingHelper.configureAppMeasurement(this);

    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    OoyalaPlayerLayoutController playerLayoutController = new OoyalaPlayerLayoutController(playerLayout,
        APIKEY, SECRET, PCODE, DOMAIN);
    player = playerLayoutController.getPlayer();
    if (player.setEmbedCode(EMBED)) {
      TrackingHelper.configureMediaMeasurement();
      player.addObserver(this);
      player.setActionAtEnd(ActionAtEnd.STOP);
    } else {
      Log.d(this.getClass().getName(), "Something Went Wrong!");
    }
  }

  @Override
  public void update(Observable arg0, Object arg1) {
    if (((String)arg1).equals(OoyalaPlayer.STATE_CHANGED_NOTIFICATION)) {
      double playheadTime = ((double) ((OoyalaPlayer) arg0).getPlayheadTime()) / 1000;
      switch (((OoyalaPlayer)arg0).getState()) {
        case READY:
          mediaName = player.getCurrentItem().getTitle();
          mediaLength = ((double) player.getDuration()) / 1000;
          TrackingHelper.open(mediaName, mediaLength, "Android_Player");
          Log.i(TAG, "omni:open " + mediaName + " " + mediaLength + " " + "Android_Player");
          break;
        case PLAYING:
          TrackingHelper.play(mediaName, playheadTime);
          Log.i(TAG, "omni:play " + mediaName + " " + playheadTime);
          break;
        case PAUSED:
        case SUSPENDED:
        case COMPLETED:
        case ERROR:
          TrackingHelper.stop(mediaName, playheadTime);
          Log.i(TAG, "omni:stop " + mediaName + " " + playheadTime);
          break;
      }
    } else if (((String)arg1).equals(OoyalaPlayer.ERROR_NOTIFICATION) ||
              ((String)arg1).equals(OoyalaPlayer.PLAY_COMPLETED_NOTIFICATION))
    {
      TrackingHelper.close(mediaName);
      Log.i(TAG, "omni:close " + mediaName);
    }
  }
}