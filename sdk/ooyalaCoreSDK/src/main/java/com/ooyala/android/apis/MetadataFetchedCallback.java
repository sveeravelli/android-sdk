package com.ooyala.android.apis;

import com.ooyala.android.OoyalaException;

public interface MetadataFetchedCallback {
  /**
   * This callback is used for asynchronous metadata calls
   *
   * @param error the OoyalaException if there was one
   */
  public void callback(boolean result, OoyalaException error);
}
