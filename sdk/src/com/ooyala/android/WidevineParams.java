package com.ooyala.android;

public class WidevineParams {
  String url;
  String embedCode;
  String pcode;
  String widevineServerPath;

  public WidevineParams(String url, String embedCode, String pcode, String widevinePath) {
    this.url = url;
    this.embedCode = embedCode;
    this.pcode = pcode;
    this.widevineServerPath = widevinePath;
  }
}
