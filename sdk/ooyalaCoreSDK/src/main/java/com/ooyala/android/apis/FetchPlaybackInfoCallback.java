package com.ooyala.android.apis;

public interface FetchPlaybackInfoCallback {
  /**
   * This callback is used for the fetchPlaybackInfo asynchronous calls
   * @param result true if the fetchPlaybackInfo call succeeded, false otherwise
   */
  public void callback(boolean result);
}
