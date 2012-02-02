package com.ooyala.android;

public interface ContentTreeCallback {
  /**
   * This callback is used for asynchronous contentTree calls
   * @param item the root content item from the contentTree call (null if an error occurred)
   * @param error the OoyalaException if there was one
   */
  public void callback(ContentItem item, OoyalaException error);
}
