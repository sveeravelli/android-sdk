package com.ooyala.android.sampleapp;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OoyalaPlayerLayoutController;
import com.ooyala.android.sampleapp.OoyalaIMAManager;

public class GettingStartedSampleAppActivity extends Activity implements Observer {

  //final String EMBED  = "91bThhODokcxQNhlk3ttzNZs3HoTZ12M";  //Embed Code, or Content ID
  final String EMBED  = "Rva245YTpHWP-9bchhJL25BMl1shI2fG"; //BYU Live Closed
  final String PCODE  = "R2d3I6s06RyB712DN0_2GsQS-R-Y";
  final String DOMAIN = "byu.edu";
  OoyalaPlayerLayoutController playerLayoutController;
  OoyalaIMAManager imaManager;
  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main); OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    playerLayoutController = new OoyalaPlayerLayoutController(playerLayout, PCODE, DOMAIN);
    OoyalaPlayer player = playerLayoutController.getPlayer();
    player.addObserver(this);
    //String url = "https://dl.dropboxusercontent.com/u/98081242/ad.xml";
    //String url="http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=%2F15018773%2Feverything2&ciu_szs=300x250%2C468x60%2C728x90&impl=s&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&cmsid=133&vid=10XWSh7W4so&ad_rule=1";

    imaManager = new OoyalaIMAManager(this, playerLayoutController);

    if (player.setEmbedCode(EMBED)) {
      player.play();
    } else {
      Log.d(this.getClass().getName(), "Something Went Wrong!");
    }
  }

  @Override
  public void update(Observable observable, Object data) {

    if(data.toString().equals(OoyalaPlayer.METADATA_READY_NOTIFICATION)) {
      String url="http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=%2F15018773%2Feverything2&ciu_szs=300x250%2C468x60%2C728x90&impl=s&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=[referrer_url]&correlator=[timestamp]&cmsid=133&vid=10XWSh7W4so&ad_rule=1";
      imaManager.loadAds(url);
    }

  }
}