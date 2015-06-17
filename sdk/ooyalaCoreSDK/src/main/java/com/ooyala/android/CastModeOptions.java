package com.ooyala.android;

public class CastModeOptions {
  private String embedCode;
  private int playheadTimeInMillis;
  private boolean isPlaying;
  private EmbedTokenGenerator generator;
  private String ccLanguage;

  public CastModeOptions(String embedCode, int playheadTimeInMillis, boolean isPlaying, EmbedTokenGenerator generator, String ccLanguage) {
    this.embedCode = embedCode;
    this.playheadTimeInMillis = playheadTimeInMillis;
    this.isPlaying = isPlaying;
    this.generator = generator;
    this.ccLanguage = ccLanguage;
  }

  public String getCCLanguage() {
    return ccLanguage;
  }

  public String getEmbedCode() {
    return embedCode;
  }

  public int getPlayheadTimeInMillis() {
    return playheadTimeInMillis;
  }

  public boolean isPlaying() {
    return isPlaying;
  }

  public EmbedTokenGenerator getGenerator() {
    return generator;
  }

}
