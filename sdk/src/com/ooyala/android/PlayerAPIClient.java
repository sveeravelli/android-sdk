package com.ooyala.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.ooyala.android.OoyalaException.OoyalaErrorCode;

class PlayerAPIClient
{
  protected String _pcode = null;
  protected String _domain = null;
  protected OoyalaAPIHelper _apiHelper = null;
  protected int _width = -1;
  protected int _height = -1;

  public PlayerAPIClient()
  {
  }

  public PlayerAPIClient(OoyalaAPIHelper apiHelper, String pcode, String domain)
  {
    _apiHelper = apiHelper;
    _pcode = pcode;
    _domain = domain;
  }

  private JSONObject verifyAuthorizeJSON(String json, List<String> embedCodes) throws OoyalaException
  {
    JSONObject authResult = Utils.objectFromJSON(json);
    if (authResult == null)
    {
      throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_INVALID, "Authorization response invalid (nil).");
    }

    try
    {
      if (!authResult.isNull(Constants.KEY_ERRORS))
      {
        JSONObject errors = authResult.getJSONObject(Constants.KEY_ERRORS);
        if (!errors.isNull(Constants.KEY_CODE) && errors.getInt(Constants.KEY_CODE) != 0)
        {
          throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_INVALID,
                                    errors.isNull(Constants.KEY_MESSAGE) ? "" : errors.getString(Constants.KEY_MESSAGE));
        }
      }

      if (authResult.isNull(Constants.KEY_AUTHORIZATION_DATA))
      {
        throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_INVALID, "Authorization data does not exist.");
      }
      else
      {
        JSONObject authData = authResult.getJSONObject(Constants.KEY_AUTHORIZATION_DATA);
        for (String embedCode : embedCodes)
        {
          if (authData.isNull(embedCode) || authResult.getJSONObject(embedCode).isNull(Constants.KEY_AUTHORIZED))
          {
            throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_INVALID,
                                      "Authorization invalid for embed code: " + embedCode);
          }
        }

        // TODO(mikhail): currently we do not check signature. fix this once we properly implement signatures server side.

        return authData;
      }
    }
    catch (JSONException exception)
    {
      System.out.println("JSONException: " + exception);
      throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_INVALID, "Authorization response invalid (exception).");
    }
  }

  private JSONObject getContentTreeData(JSONObject contentTree) throws OoyalaException
  {
    if (contentTree == null)
    {
      throw new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID, "Content Tree response invalid (nil).");
    }

    try
    {
      if (!contentTree.isNull(Constants.KEY_ERRORS))
      {
        JSONObject errors = contentTree.getJSONObject(Constants.KEY_ERRORS);
        if (!errors.isNull(Constants.KEY_CODE) && errors.getInt(Constants.KEY_CODE) != 0)
        {
          throw new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID,
                                    errors.isNull(Constants.KEY_MESSAGE) ? "" : errors.getString(Constants.KEY_MESSAGE));
        }
      }

      // TODO(mikhail): currently we do not check signature. fix this once we properly implement signatures server side.

      if (contentTree.isNull(Constants.KEY_CONTENT_TREE))
      {
        throw new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID, "Content tree data does not exist.");
      }
      else
      {
        return contentTree.getJSONObject(Constants.KEY_CONTENT_TREE);
      }
    }
    catch (JSONException exception)
    {
      System.out.println("JSONException: " + exception);
      throw new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID,
                                "Content tree response invalid (exception).");
    }
  }

  private JSONObject verifyContentTreeObject(JSONObject contentTree, List<String> keys) throws OoyalaException
  {
    JSONObject contentTreeData = getContentTreeData(contentTree); // let any thrown exceptions propagate up
    if (contentTreeData != null && keys != null)
    {
      for (String key : keys)
      {
        if (contentTreeData.isNull(key))
        {
          throw new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID,
                                    "Content Tree response invalid (no key for: " + key +").");
        }
      }
    }
    return contentTreeData;
  }

  // embedCodes should be an empty list that will be populated with embedCodes corresponding to the externalIds.
  private JSONObject verifyContentTreeObject(JSONObject contentTree, List<String> externalIds, List<String> embedCodes) throws OoyalaException
  {
    JSONObject contentTreeData = getContentTreeData(contentTree); // let any thrown exceptions propagate up
    if (contentTreeData != null && externalIds != null)
    {
      JSONArray embeds = contentTreeData.names();
      if ((embeds == null || embeds.length() == 0) && externalIds.size() > 0)
      {
        throw new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID,
                                  "Content Tree response did not contain any values.  Expected: " + externalIds.size());
      }
      try
      {
        for (int i = 0; i < embeds.length(); i++)
        {
          embedCodes.add(embeds.getString(i));
        }
      }
      catch (JSONException exception)
      {
        System.out.println("JSONException: " + exception);
        throw new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID,
                                  "Content tree response invalid (exception casting embedCode to String)");
      }
      // Size comparison is done after filling in embedCodes on purpose.
      if (embedCodes.size() != externalIds.size())
      {
        throw new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID,
                                  "Content Tree response did not contain values for all external IDs. Found " +
                                  embedCodes.size() + " of " + externalIds.size());
      }
      for (String embedCode : embedCodes)
      {
        if (contentTreeData.isNull(embedCode))
        {
          throw new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID,
                                    "Content Tree response invalid (no key for: " + embedCode +").");
        }
      }
    }
    return contentTreeData;
  }

  private Map<String,String> authorizeParams()
  {
    Map<String,String> params = new HashMap<String,String>();
    params.put(Constants.KEY_DEVICE, "DEVICE_ANDROID");
    params.put(Constants.KEY_DOMAIN, _domain);
    return params;
  }

  private Map<String,String> contentTreeParams()
  {
    Map<String,String> params = new HashMap<String,String>();
    params.put(Constants.KEY_DEVICE, "DEVICE_ANDROID");
    if (_height > 0 && _width > 0)
    {
      params.put(Constants.KEY_WIDTH, Integer.toString(_width));
      params.put(Constants.KEY_HEIGHT, Integer.toString(_height));
    }
    return params;
  }

  public boolean authorize(AuthorizableItem item)
  {
    List<String> embedCodes = item.embedCodesToAuthorize();
    String uri = String.format(Constants.AUTHORIZE_EMBED_CODE_URI, _pcode, Utils.join(embedCodes, Constants.SEPARATOR_COMMA));
    String json = _apiHelper.jsonForSecureAPI(Constants.AUTHORIZE_HOST, uri, authorizeParams());
    JSONObject authData = null;
    try
    {
      authData = verifyAuthorizeJSON(json, embedCodes);
    }
    catch (Exception e)
    {
      System.out.println("Unable to authorize: " + e);
      return false;
    }
    item.update(authData);
    return true;
  }

  public boolean authorizeEmbedCodes(List<String> embedCodes, AuthorizableItem parent)
  {
    String uri = String.format(Constants.AUTHORIZE_EMBED_CODE_URI, _pcode, Utils.join(embedCodes, Constants.SEPARATOR_COMMA));
    String json = _apiHelper.jsonForSecureAPI(Constants.AUTHORIZE_HOST, uri, authorizeParams());
    JSONObject authData = null;
    try
    {
      authData = verifyAuthorizeJSON(json, embedCodes);
    }
    catch (Exception e)
    {
      System.out.println("Unable to authorize: " + e);
      return false;
    }
    if (parent != null) { parent.update(authData); }
    return true;
  }

  public ContentItem contentTree(List<String> embedCodes)
  {
    String uri = String.format(Constants.CONTENT_TREE_URI, _pcode, Utils.join(embedCodes, Constants.SEPARATOR_COMMA));
    JSONObject obj = OoyalaAPIHelper.objectForAPI(Constants.CONTENT_TREE_HOST, uri, contentTreeParams());
    if (obj == null) { return null; }
    JSONObject contentTree = null;
    try
    {
      contentTree = verifyContentTreeObject(obj, embedCodes);
    }
    catch (Exception e)
    {
      System.out.println("Unable to create objects: " + e);
      return null;
    }
    return ContentItem.create(contentTree, embedCodes, this);
  }

  public ContentItem contentTreeByExternalIds(List<String> externalIds)
  {
    String uri = String.format(Constants.CONTENT_TREE_BY_EXTERNAL_ID_URI, _pcode, Utils.join(externalIds, Constants.SEPARATOR_COMMA));
    JSONObject obj = OoyalaAPIHelper.objectForAPI(Constants.CONTENT_TREE_HOST, uri, contentTreeParams());
    if (obj == null) { return null; }
    List<String> embedCodes = new ArrayList<String>(); // will be filled in by verifyContentTreeObject call below
    JSONObject contentTree = null;
    try
    {
      contentTree = verifyContentTreeObject(obj, externalIds, embedCodes);
    }
    catch (Exception e)
    {
      System.out.println("Unable to create externalId objects: " + e);
      return null;
    }
    return ContentItem.create(contentTree, embedCodes, this);
  }

  public PaginatedItemResponse contentTreeNext(String nextToken, PaginatedParentItem parent)
  {
    String uri = String.format(Constants.CONTENT_TREE_NEXT_URI, _pcode, nextToken);
    JSONObject obj = OoyalaAPIHelper.objectForAPI(Constants.CONTENT_TREE_HOST, uri, contentTreeParams());
    if (obj == null) { return null; }
    JSONObject contentTree = null;
    List<String> keys = new ArrayList<String>();
    keys.add(nextToken);
    try
    {
      contentTree = verifyContentTreeObject(obj, keys);
    }
    catch (Exception e)
    {
      System.out.println("Unable to create next objects: " + e);
      return null;
    }

    /**
     * NOTE: We have to convert the content token keyed dictionary to one that is embed code keyed in order for it to work with update.
     * We could just create a new update in each class, but this seemed better because that would have a lot of duplicate code.
     */
    if (contentTree.isNull(nextToken))
    {
      System.out.println("Could not find token in content_tree_next response.");
      return null;
    }
    try
    {
      JSONObject tokenDict = contentTree.getJSONObject(nextToken);
      JSONObject parentDict = new JSONObject();
      parentDict.put(parent.getEmbedCode(), tokenDict);

      int startIdx = parent.childrenCount();
      parent.update(parentDict);
      return new PaginatedItemResponse(startIdx, tokenDict.isNull(Constants.KEY_CHILDREN) ? 0 : tokenDict.getJSONObject(Constants.KEY_CHILDREN).length());
    }
    catch (JSONException e)
    {
      System.out.println("Unable to create next objects due to JSON Exception: " + e);
      return null;
    }
  }

  public boolean metadataForContentIDs(List<String> contentIDs)
  {
    // TODO
    return false;
  }

  public boolean metadataForEmbedCodes(List<String> embedCodes)
  {
    // TODO
    return false;
  }
}
