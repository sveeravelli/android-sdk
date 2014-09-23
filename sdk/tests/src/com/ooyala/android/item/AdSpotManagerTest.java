package com.ooyala.android.item;

import java.util.ArrayList;
import java.util.List;

import android.test.AndroidTestCase;

class TestAdSpot extends AdSpot {
  private int _time;

  private TestAdSpot(int time) {
    _time = time;
  }

  @Override
  public int getTime() {
    return _time;
  }

  public static TestAdSpot create(int time) {
    return new TestAdSpot(time);
  }
}

public class AdSpotManagerTest extends AndroidTestCase {
  public AdSpotManagerTest() {
    super();
  }

  @Override
  protected void setUp() {

  }

  @Override
  protected void tearDown() {

  }

  public void testInitAndClear() {
    AdSpotManager<TestAdSpot> manager = new AdSpotManager<TestAdSpot>();
    assertTrue(manager.size() == 0);
    assertTrue(manager.getAlignment() == 0);

    TestAdSpot s0 = TestAdSpot.create(0);
    manager.insertAd(s0);
    assertTrue(manager.size() == 1);

    manager.setAlignment(1000);
    assertTrue(manager.getAlignment() == 1000);

    TestAdSpot s1 = TestAdSpot.create(1000);
    manager.insertAd(s1);
    assertTrue(manager.size() == 2);

    manager.clear();
    assertTrue(manager.size() == 0);
    assertTrue(manager.getAlignment() == 0);
  }

  public void testAdsBeforeTime() {
    AdSpotManager<TestAdSpot> manager = new AdSpotManager<TestAdSpot>();

    TestAdSpot s0 = TestAdSpot.create(0);
    TestAdSpot s1 = TestAdSpot.create(1000);
    TestAdSpot s2 = TestAdSpot.create(10500);
    TestAdSpot s3 = TestAdSpot.create(25000);
    TestAdSpot s4 = TestAdSpot.create(31000);

    manager.insertAd(s1);
    manager.insertAd(s0);
    manager.insertAd(s2);
    manager.insertAd(s4);
    manager.insertAd(s3);

    assertTrue(manager.size() == 5);
    assertTrue(manager.adBeforeTime(0) == s0);
    manager.markAsPlayed(s0);
    assertTrue(manager.adBeforeTime(0) == null);
    assertTrue(manager.adBeforeTime(999) == null);
    assertTrue(manager.adBeforeTime(1000) == s1);
    manager.markAsPlayed(s1);
    assertTrue(manager.adBeforeTime(10000) == null);
    assertTrue(manager.adBeforeTime(20000) == s2);
    manager.markAsPlayed(s2);
    assertTrue(manager.adBeforeTime(25000) == s3);
    manager.markAsPlayed(s3);
    assertTrue(manager.adBeforeTime(30000) == null);
    assertTrue(manager.adBeforeTime(31000) == s4);
  }

  public void testAdsBeforeTimeWithAlignment() {
    int timeAlignment = 10000;
    List<TestAdSpot> adList = new ArrayList<TestAdSpot>();
    AdSpotManager<TestAdSpot> manager = new AdSpotManager<TestAdSpot>();
    manager.setAlignment(timeAlignment);

    TestAdSpot s0 = TestAdSpot.create(0);
    TestAdSpot s1 = TestAdSpot.create(1000);
    TestAdSpot s2 = TestAdSpot.create(10500);
    TestAdSpot s3 = TestAdSpot.create(25000);
    TestAdSpot s4 = TestAdSpot.create(31000);

    adList.add(s2);
    adList.add(s1);
    adList.add(s3);
    adList.add(s0);
    adList.add(s4);
    manager.insertAds(adList);

    assertTrue(manager.size() == 5);
    assertTrue(manager.adBeforeTime(0) == s0);
    manager.markAsPlayed(s0);
    assertTrue(manager.adBeforeTime(0) == s1);
    manager.markAsPlayed(s1);
    assertTrue(manager.adBeforeTime(10000) == s2);
    manager.markAsPlayed(s2);
    assertTrue(manager.adBeforeTime(20000) == null);
    assertTrue(manager.adBeforeTime(30000) == s3);
    manager.markAsPlayed(s3);
    assertTrue(manager.adBeforeTime(30000) == s4);
    manager.markAsPlayed(s4);
    assertTrue(manager.adBeforeTime(30000) == null);
  }

  public void testCuePoints() {
    TestAdSpot s0 = TestAdSpot.create(0);
    TestAdSpot s1 = TestAdSpot.create(1000);
    TestAdSpot s2 = TestAdSpot.create(1000);
    TestAdSpot s3 = TestAdSpot.create(2000);
    TestAdSpot s4 = TestAdSpot.create(3000);
    
    AdSpotManager<TestAdSpot> manager = new AdSpotManager<TestAdSpot>();
    manager.insertAd(s0);
    assertTrue(manager.cuePoints().isEmpty());
    manager.insertAd(s1);
    assertTrue(manager.cuePoints().size() == 1);
    assertTrue(manager.cuePoints().contains(1000));
    manager.insertAd(s2);
    assertTrue(manager.cuePoints().size() == 1);
    manager.insertAd(s3);
    assertTrue(manager.cuePoints().size() == 2);
    assertTrue(manager.cuePoints().contains(2000));
    manager.insertAd(s4);
    assertTrue(manager.cuePoints().size() == 3);
    assertTrue(manager.cuePoints().contains(3000));

    manager.markAsPlayed(s0);
    assertTrue(manager.cuePoints().size() == 3);
    assertTrue(manager.cuePoints().contains(1000));
    manager.markAsPlayed(s1);
    assertTrue(manager.cuePoints().size() == 3);
    manager.markAsPlayed(s2);
    assertTrue(manager.cuePoints().size() == 2);
    assertTrue(!manager.cuePoints().contains(1000));
    manager.markAsPlayed(s3);
    assertTrue(manager.cuePoints().size() == 1);
    assertTrue(!manager.cuePoints().contains(2000));
    manager.markAsPlayed(s4);
    assertTrue(manager.cuePoints().size() == 0);

    manager.resetAds();
    assertTrue(manager.cuePoints().size() == 3);
    assertTrue(manager.cuePoints().contains(1000));
    assertTrue(manager.cuePoints().contains(2000));
    assertTrue(manager.cuePoints().contains(3000));
  }

  public void testCuePointsWithList() {
    TestAdSpot s0 = TestAdSpot.create(0);
    TestAdSpot s1 = TestAdSpot.create(1000);
    TestAdSpot s2 = TestAdSpot.create(1000);
    TestAdSpot s3 = TestAdSpot.create(2000);
    TestAdSpot s4 = TestAdSpot.create(3000);

    List<TestAdSpot> adList = new ArrayList<TestAdSpot>();
    adList.add(s0);
    adList.add(s1);
    adList.add(s2);
    adList.add(s3);
    adList.add(s4);

    AdSpotManager<TestAdSpot> manager = new AdSpotManager<TestAdSpot>();
    manager.insertAds(adList);
    assertTrue(manager.cuePoints().size() == 3);
    assertTrue(manager.cuePoints().contains(1000));
    assertTrue(manager.cuePoints().contains(2000));
    assertTrue(manager.cuePoints().contains(3000));
  }
}
