package com.ooyala.android.visualon;

interface AcquireRightsCallback {
  /**
   * This callback is used for the AcquireRightsAsyncTask asynchronous calls
   * @param success
   */
  public void afterAcquireRights(Exception success);
}
