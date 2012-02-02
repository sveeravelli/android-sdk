package com.ooyala.android;

interface AuthorizeCallback {
  /**
   * This callback is used for asynchronous authorize calls
   * @param result true if the authorize call succeeded, false otherwise
   * @param error the OoyalaException if there was one
   */
  public void callback(boolean result, OoyalaException error);
}
