package com.ooyala.android.imasampleapp;


import java.util.LinkedHashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.PlayerDomain;
import com.ooyala.android.imasdk.OoyalaIMAManager;
import com.ooyala.android.ui.OptimizedOoyalaPlayerLayoutController;

/**
 * A Sample integration of OoyalaPlayer and Google IMA Manager
 *
 * This application will not run unless you link Google Play Service's project
 *
 * http://developer.android.com/google/play-services/setup.html
 *
 * @author michael.len
 *
 */
public class IMASampleAppActivity extends Activity {

  final String EMBED  = "h5OWFoYTrG4YIPdrDKrIz5-VhobsuT-M";  //Embed Code, or Content ID
  final String PCODE  = "R2d3I6s06RyB712DN0_2GsQS-R-Y";
  final String DOMAIN = "http://www.ooyala.com";
  OptimizedOoyalaPlayerLayoutController playerLayoutController;
  OoyalaIMAManager imaManager;

  private Map<String, String> embedMap;
  private Spinner embedSpinner;
  private Button setButton;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
    playerLayoutController = new OptimizedOoyalaPlayerLayoutController(playerLayout, PCODE, new PlayerDomain(DOMAIN));
    final OoyalaPlayer player = playerLayoutController.getPlayer();

    //Initialize IMA classes
    imaManager = new OoyalaIMAManager(player);
    ViewGroup companionView = (ViewGroup) findViewById(R.id.companionFrame);
    imaManager.addCompanionSlot(companionView, 300, 50);

    embedMap = new LinkedHashMap<String, String>();
    embedMap
        .put(
            "Preroll",
            "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/7521029/pb_preroll_ad&ciu_szs&impl=s&cmsid=949&vid=FjbGRjbzp0DV_5-NtXBVo5Rgp3Sj0R5C&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=[referrer_url]&description_url=[description_url]&correlator=[timestamp]");
    embedMap
        .put(
            "Midroll",
            "http://pubads.g.doubleclick.net/gampad/ads?sz=640x360&iu=/7521029/pb_test_mid&ciu_szs=640x480&impl=s&cmsid=949&vid=FjbGRjbzp0DV_5-NtXBVo5Rgp3Sj0R5C&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=[referrer_url]&description_url=[description_url]&correlator=[timestamp]");
    embedMap
        .put(
            "Postroll",
            "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/7521029/pb_post_roll&ciu_szs&impl=s&cmsid=949&vid=FjbGRjbzp0DV_5-NtXBVo5Rgp3Sj0R5C&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=[referrer_url]&description_url=[description_url]&correlator=[timestamp]");
    embedMap
        .put(
            "Pre, Mid and Post Skippable",
            "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/7521029/pb_skippable_ad_unit&ciu_szs&impl=s&cmsid=949&vid=FjbGRjbzp0DV_5-NtXBVo5Rgp3Sj0R5C&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=[referrer_url]&description_url=[description_url]&correlator=[timestamp]");
    embedMap
        .put(
            "Pre, Mid and Post Podded",
            "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/7521029/pb_skippable_ad_unit&ciu_szs&impl=s&cmsid=949&vid=FjbGRjbzp0DV_5-NtXBVo5Rgp3Sj0R5C&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=[referrer_url]&description_url=[description_url]&correlator=[timestamp]");
    embedMap
        .put(
            "Preroll 2 Podded",
            "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/7521029/pb_preroll_2_prod&ciu_szs&impl=s&cmsid=%20949&vid=FjbGRjbzp0DV_5-NtXBVo5Rgp3Sj0R5C&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=[referrer_url]&description_url=[description_url]&correlator=[timestamp]");
    embedMap
        .put(
            "Midroll 2 Podded",
            "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/7521029/pb_midroll_2_prod&ciu_szs&impl=s&cmsid=949&vid=FjbGRjbzp0DV_5-NtXBVo5Rgp3Sj0R5C&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=[referrer_url]&description_url=[description_url]&correlator=[timestamp]");
    embedMap
        .put(
            "Postroll 2 Podded",
            "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/7521029/pb_postroll_2_prod&ciu_szs&impl=s&cmsid=949&vid=FjbGRjbzp0DV_5-NtXBVo5Rgp3Sj0R5C&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=[referrer_url]&description_url=[description_url]&correlator=[timestamp]");

    embedSpinner = (Spinner) findViewById(R.id.embedSpinner);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_spinner_item);
    for (String key : embedMap.keySet()) {
      adapter.add(key);
    }
    adapter.notifyDataSetChanged();
    embedSpinner.setAdapter(adapter);
    setButton = (Button) findViewById(R.id.setButton);
    setButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View arg0) {
        Object embedKey = embedSpinner.getSelectedItem();
        if (embedKey == null) {
          return;
        }
        String adtag = embedMap.get(embedKey.toString());
        imaManager.setAdUrlOverride(adtag);
        imaManager.setAdTagParameters(null);
        if (player.setEmbedCode(EMBED)) {
          player.play();
        } else {
          Log.d(this.getClass().getName(), "Something Went Wrong!");
        }
      }
    });
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (playerLayoutController.getPlayer() != null) {
      playerLayoutController.getPlayer().suspend();
    }
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    if (playerLayoutController.getPlayer() != null) {
      playerLayoutController.getPlayer().resume();
    }
  }

}