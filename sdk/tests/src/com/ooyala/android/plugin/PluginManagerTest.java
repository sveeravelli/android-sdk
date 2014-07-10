package com.ooyala.android.plugin;

import android.test.AndroidTestCase;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.PlayerDomain;

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
}
