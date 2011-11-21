package com.ooyala.android;

import java.util.Set;

/**
 * Stores the info and metatdata for the specified movie.
 *
 */
public class Movie extends ContentItem
{
  protected Set<Ad> _ads = null;

  /**
   * Returns whether this movie has ads
   * @return isAd
   */
  public boolean hasAds()
  {
    return (_ads != null && _ads.size() > 0);
  }
}
