package com.ooyala.android.plugin;

import android.test.AndroidTestCase;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.PlayerDomain;
import com.ooyala.android.item.AdSpot;
import com.ooyala.android.player.PlayerInterface;

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

class TestAdsPlugin extends DefaultAdsPlugin<TestAdSpot> {

  public TestAdsPlugin() {
    super();
  }

  @Override
  public PlayerInterface getPlayerInterface() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void reset() {
    // TODO Auto-generated method stub

  }

  @Override
  public void suspend() {
    // TODO Auto-generated method stub

  }

  @Override
  public void resume() {
    // TODO Auto-generated method stub

  }

  @Override
  public void resume(int timeInMilliSecond, State stateToResume) {
    // TODO Auto-generated method stub

  }

  @Override
  public void destroy() {
    // TODO Auto-generated method stub

  }

  @Override
  protected boolean playAd(TestAdSpot ad) {
    // TODO Auto-generated method stub
    return false;
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
    TestAdsPlugin plugin = new TestAdsPlugin();
    assertTrue(player.registerPlugin(plugin));
  }
}
