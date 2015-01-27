package com.ooyala.android.apis;

import com.ooyala.android.OoyalaException;
import com.ooyala.android.PaginatedItemResponse;

public interface ContentTreeNextCallback {
  /**
   * This callback is used for asynchronous contentTreeNext calls
   * @param response the PaginatedItemResponse from the contentTreeNext call (null if an error occurred)
   * @param error the OoyalaException if there was one
   */
  public void callback(PaginatedItemResponse response, OoyalaException error);
}
