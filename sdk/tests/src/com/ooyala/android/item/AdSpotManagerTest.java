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

  public void testIdenticalSpots() {
    AdSpotManager<TestAdSpot> manager = new AdSpotManager<TestAdSpot>();

    TestAdSpot s0 = TestAdSpot.create(0);
    manager.insertAd(s0);
    assertTrue(manager.size() == 1);

    TestAdSpot s1 = TestAdSpot.create(1000);
    manager.insertAd(s1);
    assertTrue(manager.size() == 2);
    manager.insertAd(s1);
    assertTrue(manager.size() == 2);
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
    TestAdSpot s1 = TestAdSpot.create(10500);
    TestAdSpot s2 = TestAdSpot.create(35000);
    TestAdSpot s3 = TestAdSpot.create(51000);

    adList.add(s2);
    adList.add(s1);
    adList.add(s3);
    adList.add(s0);
    manager.insertAds(adList);

    assertTrue(manager.adBeforeTime(0) == s0);
    assertTrue(manager.adBeforeTime(10000) == s1);
    assertTrue(manager.adBeforeTime(20000) == s1);
    assertTrue(manager.adBeforeTime(30000) == s1);
    assertTrue(manager.adBeforeTime(40000) == s2);
    assertTrue(manager.adBeforeTime(50000) == s3);
  }

  public void testAdBeforeTimePreroll() {
    AdSpotManager<TestAdSpot> manager = new AdSpotManager<TestAdSpot>();
    TestAdSpot s0 = TestAdSpot.create(0);
    manager.insertAd(s0);

    assertTrue(manager.adBeforeTime(0) == s0);
    assertTrue(manager.adBeforeTime(1) == s0);
    assertTrue(manager.adBeforeTime(1000000) == s0);
  }

  public void testAdsBeforePlaySingleMidroll() {
    AdSpotManager<TestAdSpot> manager = new AdSpotManager<TestAdSpot>();
    TestAdSpot s0 = TestAdSpot.create(2000);
    manager.insertAd(s0);

    assertTrue(manager.adBeforeTime(0) == null);
    assertTrue(manager.adBeforeTime(1000) == null);
    assertTrue(manager.adBeforeTime(2000) == s0);
    assertTrue(manager.adBeforeTime(3000) == s0);
  }

  public void testAdsBeforePlayMultipleMidroll() {
    AdSpotManager<TestAdSpot> manager = new AdSpotManager<TestAdSpot>();
    TestAdSpot s0 = TestAdSpot.create(2000);
    TestAdSpot s1 = TestAdSpot.create(5000);
    TestAdSpot s2 = TestAdSpot.create(10000);
    manager.insertAd(s0);
    manager.insertAd(s1);
    manager.insertAd(s2);

    assertTrue(manager.adBeforeTime(0) == null);
    assertTrue(manager.adBeforeTime(1000) == null);
    assertTrue(manager.adBeforeTime(2000) == s0);
    assertTrue(manager.adBeforeTime(4000) == s0);
    assertTrue(manager.adBeforeTime(5000) == s1);
    assertTrue(manager.adBeforeTime(8000) == s1);
    assertTrue(manager.adBeforeTime(10000) == s2);
    assertTrue(manager.adBeforeTime(2800000) == s2);
  }

  public void testAdBeforeTimePrerollMarkedAsPlayed() {
    AdSpotManager<TestAdSpot> manager = new AdSpotManager<TestAdSpot>();
    TestAdSpot s0 = TestAdSpot.create(0);
    manager.insertAd(s0);
    assertTrue(manager.adBeforeTime(0) == s0);

    manager.markAsPlayed(s0);
    assertTrue(manager.adBeforeTime(0) == null);
  }

  public void testAdsBeforePlayMultipleMidrollMarkedAsPlayed() {
    AdSpotManager<TestAdSpot> manager = new AdSpotManager<TestAdSpot>();
    TestAdSpot s0 = TestAdSpot.create(2000);
    TestAdSpot s1 = TestAdSpot.create(5000);
    TestAdSpot s2 = TestAdSpot.create(10000);

    manager.insertAd(s0);
    manager.insertAd(s1);
    manager.insertAd(s2);

    assertTrue(manager.adBeforeTime(0) == null);
    assertTrue(manager.adBeforeTime(1000) == null);
    assertTrue(manager.adBeforeTime(2000) == s0);
    assertTrue(manager.adBeforeTime(4000) == s0);
    assertTrue(manager.adBeforeTime(5000) == s1);
    assertTrue(manager.adBeforeTime(8000) == s1);
    assertTrue(manager.adBeforeTime(10000) == s2);
    assertTrue(manager.adBeforeTime(2800000) == s2);

    manager.markAsPlayed(s0);

    assertTrue(manager.adBeforeTime(0) == null);
    assertTrue(manager.adBeforeTime(1000) == null);
    assertTrue(manager.adBeforeTime(2000) == null);
    assertTrue(manager.adBeforeTime(4000) == null);
    assertTrue(manager.adBeforeTime(5000) == s1);
    assertTrue(manager.adBeforeTime(8000) == s1);
    assertTrue(manager.adBeforeTime(10000) == s2);
    assertTrue(manager.adBeforeTime(2800000) == s2);
  }

  public void testAdsBeforePlayMultipleMidrollMarkedAsPlayedSeek() {
    AdSpotManager<TestAdSpot> manager = new AdSpotManager<TestAdSpot>();
    TestAdSpot s0 = TestAdSpot.create(2000);
    TestAdSpot s1 = TestAdSpot.create(5000);
    TestAdSpot s2 = TestAdSpot.create(10000);

    manager.insertAd(s0);
    manager.insertAd(s1);
    manager.insertAd(s2);

    assertTrue(manager.adBeforeTime(0) == null);
    assertTrue(manager.adBeforeTime(1000) == null);
    assertTrue(manager.adBeforeTime(2000) == s0);
    assertTrue(manager.adBeforeTime(4000) == s0);
    assertTrue(manager.adBeforeTime(5000) == s1);
    assertTrue(manager.adBeforeTime(8000) == s1);
    assertTrue(manager.adBeforeTime(10000) == s2);
    assertTrue(manager.adBeforeTime(2800000) == s2);

    manager.markAsPlayed(s1);

    assertTrue(manager.adBeforeTime(0) == null);
    assertTrue(manager.adBeforeTime(1000) == null);
    assertTrue(manager.adBeforeTime(2000) == s0);
    assertTrue(manager.adBeforeTime(4000) == s0);
    assertTrue(manager.adBeforeTime(5000) == null);
    assertTrue(manager.adBeforeTime(8000) == null);
    assertTrue(manager.adBeforeTime(10000) == s2);
    assertTrue(manager.adBeforeTime(2800000) == s2);
  }
  
  public void testAdsBeforePlayMultiplePreroll() {
    AdSpotManager<TestAdSpot> manager = new AdSpotManager<TestAdSpot>();
    TestAdSpot s0 = TestAdSpot.create(0);
    TestAdSpot s1 = TestAdSpot.create(2000);
    TestAdSpot s2 = TestAdSpot.create(4000);
    manager.insertAd(s0);
    manager.insertAd(s1);
    manager.insertAd(s2);
    manager.setAlignment(10000);

    TestAdSpot s = manager.adBeforeTime(0);
    assertTrue(s == s2 || s == s1 || s == s0 );
    manager.markAsPlayed(s0);
    s = manager.adBeforeTime(0);
    assertTrue(s == s1 || s == s2);
    manager.markAsPlayed(s1);
    s = manager.adBeforeTime(0);
    assertTrue(s == s2);
    manager.markAsPlayed(s2);
    s = manager.adBeforeTime(0);
    assertTrue(s == null);
  }
  
  public void testAdsBeforePlayMultipleMidrollSameTime() {
    AdSpotManager<TestAdSpot> manager = new AdSpotManager<TestAdSpot>();

    TestAdSpot s0 = TestAdSpot.create(10000);
    TestAdSpot s1 = TestAdSpot.create(10000);
    TestAdSpot s2 = TestAdSpot.create(10000);
    manager.insertAd(s0);
    manager.insertAd(s1);
    manager.insertAd(s2);

    assertTrue(manager.adBeforeTime(0) == null);
    TestAdSpot s = manager.adBeforeTime(10000);
    assertTrue(s == s2 || s == s1 || s == s0);
    manager.markAsPlayed(s0);
    s = manager.adBeforeTime(10000);
    assertTrue(s == s1 || s == s2);
    manager.markAsPlayed(s1);
    s = manager.adBeforeTime(10000);
    assertTrue(s == s2);
    manager.markAsPlayed(s2);
    s = manager.adBeforeTime(10000);
    assertTrue(s == null);
  }

}
