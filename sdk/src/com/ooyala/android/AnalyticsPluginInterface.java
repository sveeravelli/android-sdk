package com.ooyala.android;

/**
 * Currently pretty hard-coded to Nielsen requirements.
 */
public interface AnalyticsPluginInterface {

  /**
   * See the Nielsen SDK documentation around AppSdk.getInstance(),
   * in particular regarding the backgrounding of the app.
   */
  public abstract void destroy();

  public abstract void setChannelName(String channelName);

  public abstract void onTag(byte[] tag);

  public abstract void onPlay();

  public abstract void onStop();

  public abstract void onPlayheadUpdate(int playheadMsec);

}