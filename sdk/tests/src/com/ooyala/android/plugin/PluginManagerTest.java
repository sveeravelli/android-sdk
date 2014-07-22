package com.ooyala.android.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.test.AndroidTestCase;

import com.ooyala.android.OoyalaManagedAdsPlugin;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.PlayerDomain;
import com.ooyala.android.item.AdSpot;

class TestAdSpot extends AdSpot {
  @Override
  public boolean fetchPlaybackInfo() {
    // TODO Auto-generated method stub
    return false;
  }

  private TestAdSpot(int time) {
    _time = time;
  }

  public static AdSpot create(int time) {
    return new TestAdSpot(time);
  }
}

public class PluginManagerTest extends AndroidTestCase {

  public PluginManagerTest() {
    super();
  }

  @Override
  protected void setUp() {

  }

  @Override
  protected void tearDown() {

  }

  public void testRegisterPlugin() {
    OoyalaPlayer player = new OoyalaPlayer("PCODE", new PlayerDomain(
        "http://www.ooyala.com"), null);
    TestPlugin plugin = new TestPlugin();
    assertTrue(player.registerPlugin(plugin));
  }

  public void testAdsBeforeTime() {
    List<AdSpot> adList = new ArrayList<AdSpot>();
    Set<AdSpot> playedAds = new HashSet<AdSpot>();
    int timeAlignment = 0;

    AdSpot s0 = TestAdSpot.create(0);
    AdSpot s1 = TestAdSpot.create(1000);
    AdSpot s2 = TestAdSpot.create(10500);
    AdSpot s3 = TestAdSpot.create(25000);
    AdSpot s4 = TestAdSpot.create(31000);

    adList.add(s2);
    adList.add(s1);
    adList.add(s3);
    adList.add(s0);
    adList.add(s4);
    Collections.sort(adList);

    assertTrue(adList.get(0) == s0);
    assertTrue(adList.get(4) == s4);
    assertTrue(OoyalaManagedAdsPlugin.adBeforeTime(adList, playedAds, 0,
        timeAlignment) == s0);
    playedAds.add(s0);
    assertTrue(OoyalaManagedAdsPlugin.adBeforeTime(adList, playedAds, 0,
        timeAlignment) == null);
    assertTrue(OoyalaManagedAdsPlugin.adBeforeTime(adList, playedAds, 999,
        timeAlignment) == null);
    assertTrue(OoyalaManagedAdsPlugin.adBeforeTime(adList, playedAds, 1000,
        timeAlignment) == s1);
    playedAds.add(s1);
    assertTrue(OoyalaManagedAdsPlugin.adBeforeTime(adList, playedAds, 10000,
        timeAlignment) == null);
    assertTrue(OoyalaManagedAdsPlugin.adBeforeTime(adList, playedAds, 20000,
        timeAlignment) == s2);
    playedAds.add(s2);
    assertTrue(OoyalaManagedAdsPlugin.adBeforeTime(adList, playedAds, 25000,
        timeAlignment) == s3);
    playedAds.add(s3);
    assertTrue(OoyalaManagedAdsPlugin.adBeforeTime(adList, playedAds, 30000,
        timeAlignment) == null);
    assertTrue(OoyalaManagedAdsPlugin.adBeforeTime(adList, playedAds, 31000,
        timeAlignment) == s4);
  }

  public void testAdsBeforeTimeWithAlignment() {
    int timeAlignment = 10000;
    List<AdSpot> adList = new ArrayList<AdSpot>();
    Set<AdSpot> playedAds = new HashSet<AdSpot>();
    AdSpot s0 = TestAdSpot.create(0);
    AdSpot s1 = TestAdSpot.create(1000);
    AdSpot s2 = TestAdSpot.create(10500);
    AdSpot s3 = TestAdSpot.create(25000);
    AdSpot s4 = TestAdSpot.create(31000);

    adList.add(s2);
    adList.add(s1);
    adList.add(s3);
    adList.add(s0);
    adList.add(s4);
    Collections.sort(adList);
    
    assertTrue(adList.get(0) == s0);
    assertTrue(adList.get(4) == s4);
    assertTrue(OoyalaManagedAdsPlugin.adBeforeTime(adList, playedAds, 0,
        timeAlignment) == s0);
    playedAds.add(s0);
    assertTrue(OoyalaManagedAdsPlugin.adBeforeTime(adList, playedAds, 0,
        timeAlignment) == s1);
    playedAds.add(s1);
    assertTrue(OoyalaManagedAdsPlugin.adBeforeTime(adList, playedAds, 10000,
        timeAlignment) == s2);
    playedAds.add(s2);
    assertTrue(OoyalaManagedAdsPlugin.adBeforeTime(adList, playedAds, 20000,
        timeAlignment) == null);
    assertTrue(OoyalaManagedAdsPlugin.adBeforeTime(adList, playedAds, 30000,
        timeAlignment) == s3);
    playedAds.add(s3);
    assertTrue(OoyalaManagedAdsPlugin.adBeforeTime(adList, playedAds, 30000,
        timeAlignment) == s4);
    playedAds.add(s4);
    assertTrue(OoyalaManagedAdsPlugin.adBeforeTime(adList, playedAds, 30000,
        timeAlignment) == null);
  }
}
