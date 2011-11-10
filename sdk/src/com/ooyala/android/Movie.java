package com.ooyala.android;

/**
 * Stores the info and metatdata for the specified movie.
 * @author chrisl
 *
 */
public interface Movie {

  /**
   * Get the embedCode for this movie.
   * @return embedCode of this movie
   */
  public String getEmbedCode();
  
  /**
   * Get the title for this movie
   * @return title of this movie
   */
  public String getTitle();
  
  /**
   * Get the description for this movie
   * @return description of this movie
   */
  public String getDescription();

  /** 
   * Get the length of the movie (in seconds with millisecond accuracy)
   * @return time in seconds
   */
  public float getTotalTime();
  
  /**
   * Returns a promo image URL for this movie that will be at least the specified dimensions
   * @param width
   * @param height
   * @return the image url
   */
  public String getPromoImageURL(int width, int height);
  
  /**
   * Returns a promo image URL for the given embed code in a channel that will be at least the specified dimensions, 
   * or null for an embed code not present in the channel.
   * @param width
   * @param height
   * @return the image url
   */
  public String getPromoImageURL(String embedCode, int width, int height);
  
  
  /**
   * Returns whether this movie is an ad
   * @return isAd
   */
  public boolean isAd();
}
