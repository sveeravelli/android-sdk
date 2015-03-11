package com.ooyala.android.visualon;

interface PersonalizationCallback {
  /**
   * This callback is used for the PersonalizationAsyncTask asynchronous calls
   * @param success
   */
  public void afterPersonalization(Exception success);
}
