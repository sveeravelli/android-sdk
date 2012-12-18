package com.ooyala.android;

import java.util.Set;

public interface PlayerInfo {
  public String getDevice();
  public Set<String> getSupportedFormats();
  public Set<String> getSupportedProfiles();
  public int getMaxWidth();
  public int getMaxHeight();
  public int getMaxBitrate();
}
