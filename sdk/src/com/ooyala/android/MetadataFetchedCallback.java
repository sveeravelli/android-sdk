package com.ooyala.android;

import org.json.JSONObject;

public interface MetadataFetchedCallback {
  /**
   * This callback is used for asynchronous metadata calls
   * @param item metadata map as fetched from APIs (null if an error occurred)
   * @param error the OoyalaException if there was one
   */
  public void callback(JSONObject metadata, OoyalaException error);
}
