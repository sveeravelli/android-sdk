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

class TestAdsPlugin extends ManagedAdsPlugin<TestAdSpot> {
  private TestAdSpot _playedAd;

  public TestAdsPlugin() {
    super();
    _playedAd = null;
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
    _playedAd = ad;
    return true;
  }

  public TestAdSpot playedAd() {
    return _playedAd;
  }

  public void insertAd(TestAdSpot ad) {
    _adSpotManager.insertAd(ad);
  }
}

class TestAdPlayer implements PlayerInterface {

  @Override
  public void pause() {
    // TODO Auto-generated method stub

  }

  @Override
  public void play() {
    // TODO Auto-generated method stub

  }

  @Override
  public void stop() {
    // TODO Auto-generated method stub

  }

  @Override
  public int currentTime() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int duration() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int buffer() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean seekable() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void seekToTime(int timeInMillis) {
    // TODO Auto-generated method stub

  }

  @Override
  public State getState() {
    // TODO Auto-generated method stub
    return null;
  }

@Override
public int livePlayheadPercentage() {
	// TODO Auto-generated method stub
	return 0;
}

@Override
public void seekToPercentLive(int percent) {
	// TODO Auto-generated method stub
	
}

  @Override
  public void setClosedCaptionsLanguage(String language) {

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
      OoyalaPlayer player = new OoyalaPlayer("PCODE", new PlayerDomain("http://www.ooyala.com"), null,null);
    TestAdsPlugin plugin = new TestAdsPlugin();
    assertTrue(player.registerPlugin(plugin));
  }

  public void testPreroll() {
    TestAdsPlugin plugin = new TestAdsPlugin();
    TestAdSpot s0 = TestAdSpot.create(0);
    plugin.insertAd(s0);

    // test preroll
    assertTrue(plugin.onInitialPlay());
    plugin.onAdModeEntered();
    assertTrue(plugin.playedAd() == s0);

    // test preroll again, ad should not play
    assertFalse(plugin.onInitialPlay());
  }

  public void testMidroll() {
    TestAdsPlugin plugin = new TestAdsPlugin();
    TestAdSpot s0 = TestAdSpot.create(10);
    plugin.insertAd(s0);

    // test midroll
    assertFalse(plugin.onPlayheadUpdate(5));
    assertTrue(plugin.onPlayheadUpdate(10));
    plugin.onAdModeEntered();
    assertTrue(plugin.playedAd() == s0);
    assertFalse(plugin.onPlayheadUpdate(15));
  }

  public void testPostroll() {
    TestAdsPlugin plugin = new TestAdsPlugin();
    TestAdSpot s0 = TestAdSpot.create(100);
    plugin.insertAd(s0);

    assertTrue(plugin.onContentFinished());
    plugin.onAdModeEntered();
    assertTrue(plugin.playedAd() == s0);

    assertFalse(plugin.onContentFinished());
  }

  public void testPreMidPostrolls() {
    TestAdsPlugin plugin = new TestAdsPlugin();
    TestAdSpot s0 = TestAdSpot.create(0);
    TestAdSpot s1 = TestAdSpot.create(10000);
    TestAdSpot s2 = TestAdSpot.create(30000);

    plugin.insertAd(s0);
    plugin.insertAd(s1);
    plugin.insertAd(s2);

    assertTrue(plugin.onInitialPlay());
    plugin.onAdModeEntered();
    assertTrue(plugin.playedAd() == s0);
    assertFalse(plugin.onPlayheadUpdate(5000));
    assertTrue(plugin.onPlayheadUpdate(10000));
    plugin.onAdModeEntered();
    assertTrue(plugin.playedAd() == s1);

    assertFalse(plugin.onPlayheadUpdate(15000));
    assertFalse(plugin.onPlayheadUpdate(20000));
    assertFalse(plugin.onPlayheadUpdate(25000));

    assertTrue(plugin.onContentFinished());
    plugin.onAdModeEntered();
    assertTrue(plugin.playedAd() == s2);
  }
}
