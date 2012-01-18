package com.ooyala.android;

import java.util.ArrayList;
import java.util.List;

import org.json.*;

import com.ooyala.android.Constants.ReturnState;

/**
 * Stores the info and metadata for the specified content item.
 *
 */
public abstract class ContentItem implements AuthorizableItem, OrderedMapValue<String>
{
  protected String _embedCode = null;
  protected String _externalId = null;
  protected String _contentToken = null;
  protected String _title = null;
  protected String _description = null;
  protected PlayerAPIClient _api;
  protected String _promoImageURL = null;
  protected boolean _authorized = false;
  protected int _authCode = AuthCode.NOT_REQUESTED;

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
   * Get the contentToken for this content item.
   * @return contentToken of this content item
   */
  public String getContentToken()
  {
    return _contentToken;
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
      if (!myData.isNull(Constants.KEY_AUTHORIZED))
      {
        _authorized = myData.getBoolean(Constants.KEY_AUTHORIZED);
        if (!myData.isNull(Constants.KEY_CODE)) {
          int authCode = myData.getInt(Constants.KEY_CODE);
          if (authCode < AuthCode.MIN_AUTH_CODE || authCode > AuthCode.MAX_AUTH_CODE) {
            _authCode = AuthCode.UNKNOWN;
          } else {
            _authCode = authCode;
          }
        }
        return ReturnState.STATE_MATCHED;
      }

      if (_embedCode != null && !myData.isNull(Constants.KEY_EMBED_CODE) &&
          !_embedCode.equals(myData.getString(Constants.KEY_EMBED_CODE)))
      {
        return ReturnState.STATE_FAIL;
      }

      if (!myData.isNull(Constants.KEY_EMBED_CODE)) { _embedCode = myData.getString(Constants.KEY_EMBED_CODE); }
      if (!myData.isNull(Constants.KEY_EXTERNAL_ID)) { _externalId = myData.getString(Constants.KEY_EXTERNAL_ID); }
      if (!myData.isNull(Constants.KEY_CONTENT_TOKEN)) { _contentToken = myData.getString(Constants.KEY_CONTENT_TOKEN); }
      if (!myData.isNull(Constants.KEY_TITLE)) { _title = myData.getString(Constants.KEY_TITLE); }
      if (!myData.isNull(Constants.KEY_DESCRIPTION)) { _description = myData.getString(Constants.KEY_DESCRIPTION); }
      if (!myData.isNull(Constants.KEY_PROMO_IMAGE)) { _promoImageURL = myData.getString(Constants.KEY_PROMO_IMAGE); }
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
    else if (contentType.equals(Constants.CONTENT_TYPE_VIDEO) || contentType.equals(Constants.CONTENT_TYPE_LIVE_STREAM))
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

  public List<String> embedCodesToAuthorize()
  {
    List<String> embedCodes = new ArrayList<String>();
    embedCodes.add(_embedCode);
    return embedCodes;
  }

  public abstract Video videoFromEmbedCode(String embedCode, Video currentItem);

  /**
   * Returns a promo image URL for this content item that will be at least the specified dimensions
   * @param width
   * @param height
   * @return the image url
   */
  public String getPromoImageURL(int width, int height)
  {
    return _promoImageURL;
  }

  public static List<String> getEmbedCodes(List<? extends ContentItem> items)
  {
    if (items == null) { return null; }
    List<String> result = new ArrayList<String>();
    for (ContentItem item : items)
    {
      result.add(item.getEmbedCode());
    }
    return result;
  }

  public boolean isAuthorized() {
    return _authorized;
  }

  public int getAuthCode() {
    return _authCode;
  }

  public String getKey() {
    return _embedCode;
  }
}
