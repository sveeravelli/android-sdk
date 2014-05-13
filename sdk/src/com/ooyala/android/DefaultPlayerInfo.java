package com.ooyala.android;

import java.util.HashSet;
import java.util.Set;

import android.os.Build;

public class DefaultPlayerInfo implements PlayerInfo {
    @Override
    public Set<String> getSupportedProfiles() {
      return null;
    }

    @Override
    public Set<String> getSupportedFormats() {
      HashSet<String> supportedFormats = new HashSet<String>();
      supportedFormats.add("mp4");
      supportedFormats.add("wv_mp4");

      if (OoyalaPlayer.enableHLS || OoyalaPlayer.enableCustomHLSPlayer) {
        supportedFormats.add("m3u8");
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        supportedFormats.add("m3u8");
        supportedFormats.add("wv_wvm");
        supportedFormats.add("wv_hls");
      }

      return supportedFormats;
    }

    @Override
    public int getMaxWidth() { return -1; }

    @Override
    public int getMaxHeight() { return -1; }

    @Override
    public int getMaxBitrate() { return -1; }

    @Override
    public String getDevice() { return "android_html"; }

    @Override
    public String getUserAgent() { return null; }
}
