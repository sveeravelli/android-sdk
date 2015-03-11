package com.ooyala.android;

import java.util.Set;
import android.annotation.SuppressLint;
import android.os.Build;
import android.test.AndroidTestCase;

public class DefaultPlayerInfoTest extends AndroidTestCase {
  public PlayerInfo info = new DefaultPlayerInfo();

  static final String DEVICE_ANDROID_SDK = "android_sdk";
  public DefaultPlayerInfoTest() {
    super();
  }

  @Override
  protected void setUp() {
    OoyalaPlayer.enableHLS = false;
    OoyalaPlayer.enableCustomPlayreadyPlayer = false;
  }

  @Override
  protected void tearDown() {

  }

  public void testBasicSupportedFormats() {
    Set<String> formats = info.getSupportedFormats();
    assertTrue(formats.contains("mp4"));
    assertTrue(formats.contains("wv_mp4"));

    assertFalse(formats.contains("smooth"));
    assertFalse(formats.contains("playready_hls"));
  }

  public void testEnableSmooth() {
    OoyalaPlayer.enableCustomPlayreadyPlayer = true;
    Set<String> formats = info.getSupportedFormats();
    assertTrue(formats.contains("mp4"));
    assertTrue(formats.contains("wv_mp4"));

    assertTrue(formats.contains("smooth"));
    assertTrue(formats.contains("playready_hls"));
  }
  public void testEnableHls() {
    OoyalaPlayer.enableHLS = true;
    Set<String> formats = info.getSupportedFormats();
    assertTrue(formats.contains("mp4"));
    assertTrue(formats.contains("wv_mp4"));

    assertTrue(formats.contains("m3u8"));
    assertFalse(formats.contains("smooth"));
    assertFalse(formats.contains("playready_hls"));
  }


  public void testEnableHlsAndSmooth() {
    OoyalaPlayer.enableHLS = true;
    OoyalaPlayer.enableCustomPlayreadyPlayer = true;
    Set<String> formats = info.getSupportedFormats();
    assertTrue(formats.contains("mp4"));
    assertTrue(formats.contains("wv_mp4"));

    assertTrue(formats.contains("m3u8"));
    assertTrue(formats.contains("smooth"));
    assertTrue(formats.contains("playready_hls"));
  }

  @SuppressLint("NewApi")
  public void testHlsWidevineIfNewEnough() {
    Set<String> formats = info.getSupportedFormats();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      assertTrue(formats.contains("m3u8"));
      assertTrue(formats.contains("wv_wvm"));
      assertTrue(formats.contains("wv_hls"));
      assertFalse(formats.contains("smooth"));
      assertFalse(formats.contains("playready_hls"));
    }
    else {
      assertFalse(formats.contains("m3u8"));
      assertFalse(formats.contains("wv_wvm"));
      assertFalse(formats.contains("wv_hls"));
      assertFalse(formats.contains("smooth"));
      assertFalse(formats.contains("playready_hls"));
    }
  }
}