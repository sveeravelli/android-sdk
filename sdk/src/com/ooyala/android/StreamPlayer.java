package com.ooyala.android;

import java.util.HashSet;
import java.util.Set;
import android.os.Build;

public abstract class StreamPlayer extends Player {
  public static PlayerInfo defaultPlayerInfo = new PlayerInfo() {
      @Override
      public Set<String> getSupportedProfiles() {
        return null;
      }

      @Override
      public Set<String> getSupportedFormats() {
        HashSet<String> supportedFormats = new HashSet<String>();
        supportedFormats.add("mp4");
        supportedFormats.add("wv_mp4");

        /*if (OoyalaPlayer.enableHLS || Build.VERSION.SDK_INT >= Constants.SDK_INT_ICS) {
          supportedFormats.add("m3u8");
          supportedFormats.add("wv_wvm");
          supportedFormats.add("wv_hls");
        }*/

        return supportedFormats;
      }
    
      @Override
      public int getMaxWidth() { return -1; }

      @Override
      public int getMaxHeight() { return -1; }

      @Override
      public int getMaxBitrate() { return -1; }

      @Override
      public String getDevice() { return "android-html5"; }
    };

  public PlayerInfo getPlayerInfo() {
    return defaultPlayerInfo;
  }
  
  public abstract void init(OoyalaPlayer parent, Set<Stream> streams);
}
