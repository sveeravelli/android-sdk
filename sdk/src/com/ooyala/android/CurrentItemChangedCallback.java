package com.ooyala.android;

public interface CurrentItemChangedCallback {
  /**
   * This callback will be called every time the current item is changed
   * @param currentItem the new current item
   */
  public void callback(Video currentItem);
}
