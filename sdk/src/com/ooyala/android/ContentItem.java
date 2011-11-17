package com.ooyala.android;

/**
 * Stores the info and metatdata for the specified content item.
 *
 */
public interface ContentItem {

  /**
   * Get the embedCode for this content item.
   * @return embedCode of this content item
   */
  public String getEmbedCode();
  
  /**
   * Get the title for this content item
   * @return title of this content item
   */
  public String getTitle();
  
  /**
   * Get the description for this content item
   * @return description of this content item
   */
  public String getDescription();

  /** 
   * Get the length of the content item (in seconds with millisecond accuracy)
   * @return time in seconds
   */
  public float getTotalTime();
  
  /**
   * Returns a promo image URL for this content item that will be at least the specified dimensions
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
}
