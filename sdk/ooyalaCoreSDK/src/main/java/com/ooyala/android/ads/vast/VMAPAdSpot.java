package com.ooyala.android.ads.vast;

import org.w3c.dom.Element;

import java.net.URL;

/**
 * Created by zchen on 3/11/16.
 */
public class VMAPAdSpot extends VASTAdSpot {
  private static final String TAG = VASTAdSpot.class.getSimpleName();

  String breakType;
  String breakId;
  Boolean allowMultipleAds;
  Boolean followRedirects;
  double repeatAfter;

  public VMAPAdSpot(double time, double repeat,String breakType, String breakId,  Boolean allowMultipleAds, Boolean followRedirects, Element vast) {
    super((int)time, null, null, null);
    this.breakType = breakType;
    this.breakId = breakId;
    this.repeatAfter = repeat;
    this.allowMultipleAds = allowMultipleAds;
    this.followRedirects = followRedirects;
    parse(vast);
  }

  public VMAPAdSpot(double time, double repeat, String breakType, String breakId, Boolean allowMultipleAds, Boolean followRedirects, URL vastUrl) {
    super((int)time, null, null, vastUrl);
    this.breakType = breakType;
    this.breakId = breakId;
    this.repeatAfter = repeat;
    this.allowMultipleAds = allowMultipleAds;
    this.followRedirects = followRedirects;
  }
}
