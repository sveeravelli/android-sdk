package com.ooyala.android.item;

import java.util.ArrayList;
import java.util.List;

import android.test.AndroidTestCase;

class TestAdSpot extends AdSpotBase {
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

  public void testAdsBeforeTime() {
    AdSpotManager<TestAdSpot> manager = new AdSpotManager<TestAdSpot>();
    int timeAlignment = 0;

    TestAdSpot s0 = TestAdSpot.create(0);
    TestAdSpot s1 = TestAdSpot.create(1000);
    TestAdSpot s2 = TestAdSpot.create(10500);
    TestAdSpot s3 = TestAdSpot.create(25000);
    TestAdSpot s4 = TestAdSpot.create(31000);

    manager.insertAd(s1);
    manager.insertAd(s0);
    manager.insertAd(s2);
    manager.insertAd(s3);
    manager.insertAd(s1);
    manager.insertAd(s3);
    manager.insertAd(s4);

    assertTrue(manager.size() == 5);
    assertTrue(manager.adBeforeTime(0, timeAlignment) == s0);
    manager.markAsPlayed(s0);
    assertTrue(manager.adBeforeTime(0, timeAlignment) == null);
    assertTrue(manager.adBeforeTime(999, timeAlignment) == null);
    assertTrue(manager.adBeforeTime(1000, timeAlignment) == s1);
    manager.markAsPlayed(s1);
    assertTrue(manager.adBeforeTime(10000, timeAlignment) == null);
    assertTrue(manager.adBeforeTime(20000, timeAlignment) == s2);
    manager.markAsPlayed(s2);
    assertTrue(manager.adBeforeTime(25000, timeAlignment) == s3);
    manager.markAsPlayed(s3);
    assertTrue(manager.adBeforeTime(30000, timeAlignment) == null);
    assertTrue(manager.adBeforeTime(31000, timeAlignment) == s4);
  }

  public void testAdsBeforeTimeWithAlignment() {
    int timeAlignment = 10000;
    List<TestAdSpot> adList = new ArrayList<TestAdSpot>();
    AdSpotManager<TestAdSpot> manager = new AdSpotManager<TestAdSpot>();

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
    assertTrue(manager.adBeforeTime(0, timeAlignment) == s0);
    manager.markAsPlayed(s0);
    assertTrue(manager.adBeforeTime(0, timeAlignment) == s1);
    manager.markAsPlayed(s1);
    assertTrue(manager.adBeforeTime(10000, timeAlignment) == s2);
    manager.markAsPlayed(s2);
    assertTrue(manager.adBeforeTime(20000, timeAlignment) == null);
    assertTrue(manager.adBeforeTime(30000, timeAlignment) == s3);
    manager.markAsPlayed(s3);
    assertTrue(manager.adBeforeTime(30000, timeAlignment) == s4);
    manager.markAsPlayed(s4);
    assertTrue(manager.adBeforeTime(30000, timeAlignment) == null);
  }

}
