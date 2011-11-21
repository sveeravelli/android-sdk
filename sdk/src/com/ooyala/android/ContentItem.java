package com.ooyala.android;

import java.util.Map;
import java.util.HashMap;

/**
 * Stores the info and metadata for the specified content item.
 *
 */
public class ContentItem
{
  protected String _embedCode = null;
  protected String _title = null;
  protected String _description = null;
  protected int _totalTime = 0;
  protected static Map<String,String> _promoImageURLCache = new HashMap<String,String>();

  public ContentItem()
  {
  }

  public ContentItem(String embedCode, String title, String description, int totalTime)
  {
    _embedCode = embedCode;
    _title = title;
    _description = description;
    _totalTime = totalTime;
  }

  /**
   * Get the embedCode for this content item.
   * @return embedCode of this content item
   */
  public String getEmbedCode()
  {
    return _embedCode;
  }

  /**
   * Get the title for this content item
   * @return title of this content item
   */
  public String getTitle()
  {
    return _title;
  }


  /**
   * Get the description for this content item
   * @return description of this content item
   */
  public String getDescription()
  {
    return _description;
  }


  /**
   * Get the length of the content item (in seconds with millisecond accuracy)
   * @return time in seconds
   */
  public int getTotalTime()
  {
    return _totalTime;
  }


  /**
   * Returns a promo image URL for this content item that will be at least the specified dimensions
   * @param width
   * @param height
   * @return the image url
   */
  public String getPromoImageURL(int width, int height)
  {
    return _embedCode == null? null : getPromoImageURL(_embedCode, width, height);
  }


  /**
   * Returns a promo image URL for the given embed code in a channel that will be at least the specified dimensions,
   * or null for an embed code not present in the channel.
   * @param width
   * @param height
   * @return the image url
   */
  public static String getPromoImageURL(String embedCode, int width, int height)
  {
    String key = embedCode + "|" + width + "x" + height;
    String url = _promoImageURLCache.get(key);
    if (url == null)
    {
      // TODO: Look up URL
      url = "figurethisout";
      _promoImageURLCache.put(key, url);
    }
    return url;
  }

}
