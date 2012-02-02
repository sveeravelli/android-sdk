package com.ooyala.android;

public interface ContentTreeNextCallback {
  /**
   * This callback is used for asynchronous contentTreeNext calls
   * @param item the PaginatedItemResponse from the contentTreeNext call (null if an error occurred)
   * @param error the OoyalaException if there was one
   */
  public void callback(PaginatedItemResponse response, OoyalaException error);
}
