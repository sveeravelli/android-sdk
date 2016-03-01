package com.ooyala.android;

/**
 * Created by ukumar on 2/4/16.
 */
public class AdPodInfo {
  private String title;
  private String description;
  private String clickUrl;
  private int adsCount;
  private int unplayedCount;
  private double skipoffset;
  private boolean adbar;
  private boolean controls;

  public AdPodInfo(String title,String description, String clickUrl, int adsCount, int unplayedCount, double skipoffset, boolean adbar, boolean controls ) {
    this.title = title;
    this.description = description;
    this.clickUrl = clickUrl;
    this.adsCount = adsCount;
    this.unplayedCount = unplayedCount;
    this.skipoffset = skipoffset;
    this.adbar = adbar;
    this.controls = controls;
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

  public double getSkipoffset() {
    return skipoffset;
  }

}
