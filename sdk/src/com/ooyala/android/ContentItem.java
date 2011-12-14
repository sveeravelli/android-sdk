package com.ooyala.android;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.json.*;

import com.ooyala.android.Constants.ReturnState;

/**
 * Stores the info and metadata for the specified content item.
 *
 */
public abstract class ContentItem
{
  protected String _embedCode = null;
  protected String _externalId = null;
  protected String _contentToken = null;
  protected String _title = null;
  protected String _description = null;
  protected PlayerAPIClient _api;
  protected static Map<String,String> _promoImageURLCache = new HashMap<String,String>();

  public ContentItem()
  {
  }

  public ContentItem(String embedCode, String title, String description)
  {
    this(embedCode, null, title, description);
  }

  public ContentItem(String embedCode, String contentToken, String title, String description)
  {
    _embedCode = embedCode;
    _contentToken = contentToken;
    _title = title;
    _description = description;
  }

  public ContentItem(JSONObject data, String embedCode, PlayerAPIClient api)
  {
    _embedCode = embedCode;
    _api = api;
    update(data);
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
   * Get the externalId for this content item.
   * @return externalId of this content item
   */
  public String getExternalId()
  {
    return _externalId;
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
   * Subclasses must override this.
   */
  public abstract Video firstVideo();

  /**
   * Subclasses must override this.
   */
  public abstract int getDuration();

  public ReturnState update(JSONObject data)
  {
    if (data == null) { return ReturnState.STATE_FAIL; }

    if (_embedCode == null || data.isNull(_embedCode)) { return ReturnState.STATE_UNMATCHED; }

    try
    {
      JSONObject myData = data.getJSONObject(_embedCode);
      if (!myData.isNull(Constants.KEY_AUTHORIZED) && myData.getBoolean(Constants.KEY_AUTHORIZED))
      {
        return ReturnState.STATE_MATCHED;
      }

      if (_embedCode != null && !myData.isNull(Constants.KEY_EMBED_CODE) &&
          !_embedCode.equals(myData.getString(Constants.KEY_EMBED_CODE)))
      {
        return ReturnState.STATE_FAIL;
      }

      if (!myData.isNull(Constants.KEY_EMBED_CODE)) { _embedCode = myData.getString(Constants.KEY_EMBED_CODE); }
      if (!myData.isNull(Constants.KEY_EXTERNAL_ID)) { _embedCode = myData.getString(Constants.KEY_EXTERNAL_ID); }
      if (!myData.isNull(Constants.KEY_CONTENT_TOKEN)) { _contentToken = myData.getString(Constants.KEY_CONTENT_TOKEN); }
      if (!myData.isNull(Constants.KEY_TITLE)) { _title = myData.getString(Constants.KEY_TITLE); }
      if (!myData.isNull(Constants.KEY_DESCRIPTION)) { _description = myData.getString(Constants.KEY_DESCRIPTION); }
    }
    catch (JSONException exception)
    {
      System.out.println("JSONException: " + exception);
      return ReturnState.STATE_FAIL;
    }
    return ReturnState.STATE_MATCHED;
  }

  public static ContentItem create(JSONObject data, List<String> embedCodes, PlayerAPIClient api)
  {
    if (data == null || embedCodes == null || embedCodes.size() == 0) { return null; }
    if (embedCodes.size() == 1) { return create(data, embedCodes.get(0), api); }
    return new DynamicChannel(data, embedCodes, api);
  }

  public static ContentItem create(JSONObject data, String embedCode, PlayerAPIClient api)
  {
    if (data == null || embedCode == null || data.isNull(embedCode)) { return null; }
    String contentType = null;
    try
    {
      JSONObject myData = data.getJSONObject(embedCode);
      if (myData.isNull(Constants.KEY_CONTENT_TYPE))
      {
        return null;
      }
      contentType = (String)myData.getString(Constants.KEY_CONTENT_TYPE);
    }
    catch (JSONException exception)
    {
      System.out.println("Create failed due to JSONException: " + exception);
      return null;
    }

    if (contentType == null)
    {
      return null;
    }
    else if (contentType.equals(Constants.CONTENT_TYPE_VIDEO))
    {
      return new Video(data, embedCode, api);
    }
    else if (contentType.equals(Constants.CONTENT_TYPE_CHANNEL))
    {
      return new Channel(data, embedCode, api);
    }
    else if (contentType.equals(Constants.CONTENT_TYPE_CHANNEL_SET))
    {
      return new ChannelSet(data, embedCode, api);
    }
    else
    {
      System.out.println("Unknown content_type: " + contentType);
      return null;
    }
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

  public static <T> Set<String> getEmbedCodes(Set<T> items)
  {
    if (items == null) { return null; }
    Set<String> result = new HashSet<String>();
    for (ContentItem item : (Set<ContentItem>)items)
    {
      result.add(item.getEmbedCode());
    }
    return result;
  }
}
