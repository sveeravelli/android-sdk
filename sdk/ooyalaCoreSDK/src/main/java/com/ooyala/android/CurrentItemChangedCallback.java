package com.ooyala.android;

import com.ooyala.android.item.Video;

public interface CurrentItemChangedCallback {
  /**
   * This callback will be called every time the current item is changed
   * @param currentItem the new current item
   */
  public void callback(Video currentItem);
}
