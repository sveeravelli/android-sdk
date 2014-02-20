package com.ooyala.android.visualon;

public interface AcquireRightsCallback {
  /**
   * This callback is used for the AcquireRightsAsyncTask asynchronous calls
   * @param result true if the AcquireRightsAsyncTask call succeeded, false otherwise
   */
  public void afterAcquireRights(boolean success);
}
