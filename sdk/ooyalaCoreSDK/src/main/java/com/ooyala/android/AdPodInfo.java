package com.ooyala.android;

import java.net.URL;

/**
 * Created by ukumar on 2/4/16.
 */
public class AdPodInfo {

    private String title,description,clickUrl;
    private int adsCount, unplayedCount;
    private boolean adbar, controls;


    public AdPodInfo(String title,String description, String clickUrl, int adsCount, int unplayedCount, boolean adbar, boolean controls ) {
        this.title=title;
        this.description=description;
        this.clickUrl=clickUrl;
        this.adsCount=adsCount;
        this.unplayedCount=unplayedCount;
        this.adbar=adbar;
        this.controls=controls;

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

}
