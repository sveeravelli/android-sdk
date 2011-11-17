package com.ooyala.android;

/**
 * Stores the info and metatdata for the specified movie.
 * @author chrisl
 *
 */
public interface Movie extends ContentItem {

  /**
   * Returns whether this movie is an ad
   * @return isAd
   */
  public boolean isAd();
}
