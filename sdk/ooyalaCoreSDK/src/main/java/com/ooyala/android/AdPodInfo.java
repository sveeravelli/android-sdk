package com.ooyala.android;

import java.util.List;

/**
 * Created by ukumar on 2/4/16.
 */
public class AdPodInfo {
  private final String title;
  private final String description;
  private final String clickUrl;
  private final int adsCount;
  private final int unplayedCount;
  private final double skipoffset;
  private final boolean adbar;
  private final boolean controls;
  private final List<AdIconInfo> icons;

  public AdPodInfo(String title, String description, String clickUrl, int adsCount, int unplayedCount) {
    this(title, description, clickUrl, adsCount, unplayedCount, -1.0, false, false, null);
  }

  public AdPodInfo(String title, String description, String clickUrl, int adsCount, int unplayedCount, double skipoffset, boolean adbar, boolean controls, List<AdIconInfo> icons) {
    this.title = title;
    this.description = description;
    this.clickUrl = clickUrl;
    this.adsCount = adsCount;
    this.unplayedCount = unplayedCount;
    this.skipoffset = skipoffset;
    this.adbar = adbar;
    this.controls = controls;
    this.icons = icons;
  }



  public boolean isAdbar() {
      return adbar;
  }
  public String getDescription() {
      return description;
  }

  public String getTitle() {
      return title;
  }

  public String getClickUrl() {
      return clickUrl;
  }

  public int getAdsCount() {
      return adsCount;
  }

  public int getUnplayedCount() {
      return unplayedCount;
  }

  public boolean isControls() {
      return controls;
  }

  public double getSkipOffset() {
    return skipoffset;
  }

  public List<AdIconInfo> getIcons() {
    return icons;
  }
}
