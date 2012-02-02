package com.ooyala.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.ooyala.android.OoyalaException.OoyalaErrorCode;

class PlayerAPIClient {
  protected String _pcode = null;
  protected String _domain = null;
  protected OoyalaAPIHelper _apiHelper = null;
  protected int _width = -1;
  protected int _height = -1;

  public PlayerAPIClient() {}

  public PlayerAPIClient(OoyalaAPIHelper apiHelper, String pcode, String domain) {
    _apiHelper = apiHelper;
    _pcode = pcode;
    _domain = domain;
  }

  private JSONObject verifyAuthorizeJSON(String json, List<String> embedCodes) throws OoyalaException {
    JSONObject authResult = Utils.objectFromJSON(json);
    if (authResult == null) { throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_INVALID,
        "Authorization response invalid (nil)."); }

    try {
      if (!authResult.isNull(Constants.KEY_ERRORS)) {
        JSONObject errors = authResult.getJSONObject(Constants.KEY_ERRORS);
        if (!errors.isNull(Constants.KEY_CODE) && errors.getInt(Constants.KEY_CODE) != 0) { throw new OoyalaException(
            OoyalaErrorCode.ERROR_AUTHORIZATION_INVALID, errors.isNull(Constants.KEY_MESSAGE) ? ""
                : errors.getString(Constants.KEY_MESSAGE)); }
      }

      if (authResult.isNull(Constants.KEY_AUTHORIZATION_DATA)) {
        throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_INVALID,
            "Authorization data does not exist.");
      } else {
        JSONObject authData = authResult.getJSONObject(Constants.KEY_AUTHORIZATION_DATA);
        for (String embedCode : embedCodes) {
          if (authData.isNull(embedCode)
              || authData.getJSONObject(embedCode).isNull(Constants.KEY_AUTHORIZED)) { throw new OoyalaException(
              OoyalaErrorCode.ERROR_AUTHORIZATION_INVALID, "Authorization invalid for embed code: "
                  + embedCode); }
        }

        // TODO(mikhail): currently we do not check signature. fix this once we properly implement signatures
        // server side.

        return authData;
      }
    } catch (JSONException exception) {
      System.out.println("JSONException: " + exception);
      throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_INVALID,
          "Authorization response invalid (exception).");
    }
  }

  private JSONObject getContentTreeData(JSONObject contentTree) throws OoyalaException {
    if (contentTree == null) { throw new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID,
        "Content Tree response invalid (nil)."); }

    try {
      if (!contentTree.isNull(Constants.KEY_ERRORS)) {
        JSONObject errors = contentTree.getJSONObject(Constants.KEY_ERRORS);
        if (!errors.isNull(Constants.KEY_CODE) && errors.getInt(Constants.KEY_CODE) != 0) { throw new OoyalaException(
            OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID, errors.isNull(Constants.KEY_MESSAGE) ? ""
                : errors.getString(Constants.KEY_MESSAGE)); }
      }

      // TODO(mikhail): currently we do not check signature. fix this once we properly implement signatures
      // server side.

      if (contentTree.isNull(Constants.KEY_CONTENT_TREE)) {
        throw new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID,
            "Content tree data does not exist.");
      } else {
        return contentTree.getJSONObject(Constants.KEY_CONTENT_TREE);
      }
    } catch (JSONException exception) {
      System.out.println("JSONException: " + exception);
      throw new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID,
          "Content tree response invalid (exception).");
    }
  }

  private JSONObject verifyContentTreeObject(JSONObject contentTree, List<String> keys)
      throws OoyalaException {
    JSONObject contentTreeData = getContentTreeData(contentTree); // let any thrown exceptions propagate up
    if (contentTreeData != null && keys != null) {
      for (String key : keys) {
        if (contentTreeData.isNull(key)) { throw new OoyalaException(
            OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID, "Content Tree response invalid (no key for: " + key
                + ")."); }
      }
    }
    return contentTreeData;
  }

  // embedCodes should be an empty list that will be populated with embedCodes corresponding to the
  // externalIds.
  private JSONObject verifyContentTreeObject(JSONObject contentTree, List<String> externalIds,
      List<String> embedCodes) throws OoyalaException {
    JSONObject contentTreeData = getContentTreeData(contentTree); // let any thrown exceptions propagate up
    if (contentTreeData != null && externalIds != null) {
      JSONArray embeds = contentTreeData.names();
      if ((embeds == null || embeds.length() == 0) && externalIds.size() > 0) { throw new OoyalaException(
          OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID,
          "Content Tree response did not contain any values.  Expected: " + externalIds.size()); }
      try {
        for (int i = 0; i < embeds.length(); i++) {
          embedCodes.add(embeds.getString(i));
        }
      } catch (JSONException exception) {
        System.out.println("JSONException: " + exception);
        throw new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID,
            "Content tree response invalid (exception casting embedCode to String)");
      }
      // Size comparison is done after filling in embedCodes on purpose.
      if (embedCodes.size() != externalIds.size()) { throw new OoyalaException(
          OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID,
          "Content Tree response did not contain values for all external IDs. Found " + embedCodes.size()
              + " of " + externalIds.size()); }
      for (String embedCode : embedCodes) {
        if (contentTreeData.isNull(embedCode)) { throw new OoyalaException(
            OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID, "Content Tree response invalid (no key for: "
                + embedCode + ")."); }
      }
    }
    return contentTreeData;
  }

  private Map<String, String> authorizeParams() {
    Map<String, String> params = new HashMap<String, String>();
    params.put(Constants.KEY_DEVICE, Utils.device());
    params.put(Constants.KEY_DOMAIN, _domain);
    return params;
  }

  private Map<String, String> contentTreeParams() {
    Map<String, String> params = new HashMap<String, String>();
    params.put(Constants.KEY_DEVICE, Utils.device());
    if (_height > 0 && _width > 0) {
      params.put(Constants.KEY_WIDTH, Integer.toString(_width));
      params.put(Constants.KEY_HEIGHT, Integer.toString(_height));
    }
    return params;
  }

  public boolean authorize(AuthorizableItemInternal item) throws OoyalaException {
    List<String> embedCodes = item.embedCodesToAuthorize();
    return authorizeEmbedCodes(embedCodes, item);
  }

  public boolean authorizeEmbedCodes(List<String> embedCodes, AuthorizableItemInternal parent)
      throws OoyalaException {
    String uri = String.format(Constants.AUTHORIZE_EMBED_CODE_URI, _pcode,
        Utils.join(embedCodes, Constants.SEPARATOR_COMMA));
    String json = _apiHelper.jsonForSecureAPI(Constants.AUTHORIZE_HOST, uri, authorizeParams());
    JSONObject authData = null;
    try {
      authData = verifyAuthorizeJSON(json, embedCodes);
    } catch (OoyalaException e) {
      System.out.println("Unable to authorize: " + e);
      throw e;
    }
    if (parent != null) {
      parent.update(authData);
    }
    return true;
  }

  private class AuthorizeTask extends AsyncTask<Object, Integer, Boolean> {
    protected OoyalaException _error = null;
    protected AuthorizeCallback _callback = null;

    public AuthorizeTask(AuthorizeCallback callback) {
      super();
      _callback = callback;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Boolean doInBackground(Object... params) { // first param is List<String> of embed codes to
                                                         // authorize, second param is
                                                         // AuthorizableItemInternal parent
      if (params.length == 0 || params[0] == null || !(params[0] instanceof List<?>)) { return new Boolean(
          false); }
      List<String> embedCodeList = ((List<String>) (params[0]));
      try {
        return authorizeEmbedCodes(embedCodeList, params.length >= 2 && params[1] != null
            && params[1] instanceof AuthorizableItemInternal ? (AuthorizableItemInternal) params[1] : null);
      } catch (OoyalaException e) {
        _error = e;
        return new Boolean(false);
      }
    }

    @Override
    protected void onPostExecute(Boolean result) {
      _callback.callback(result.booleanValue(), _error);
    }
  }

  public Object authorize(AuthorizableItemInternal item, AuthorizeCallback callback) {
    return authorizeEmbedCodes(item.embedCodesToAuthorize(), item, callback);
  }

  public Object authorizeEmbedCodes(List<String> embedCodes, AuthorizableItemInternal parent,
      AuthorizeCallback callback) {
    AuthorizeTask task = new AuthorizeTask(callback);
    task.execute(embedCodes, parent);
    return task;
  }

  public ContentItem contentTree(List<String> embedCodes) throws OoyalaException {
    String uri = String.format(Constants.CONTENT_TREE_URI, _pcode,
        Utils.join(embedCodes, Constants.SEPARATOR_COMMA));
    JSONObject obj = OoyalaAPIHelper.objectForAPI(Constants.CONTENT_TREE_HOST, uri, contentTreeParams());
    if (obj == null) { return null; }
    JSONObject contentTree = null;
    try {
      contentTree = verifyContentTreeObject(obj, embedCodes);
    } catch (OoyalaException e) {
      System.out.println("Unable to create objects: " + e);
      throw e;
    }
    return ContentItem.create(contentTree, embedCodes, this);
  }

  private class ContentTreeTask extends AsyncTask<List<String>, Integer, ContentItem> {
    protected OoyalaException _error = null;
    protected ContentTreeCallback _callback = null;

    public ContentTreeTask(ContentTreeCallback callback) {
      super();
      _callback = callback;
    }

    @Override
    protected ContentItem doInBackground(List<String>... embedCodeLists) {
      if (embedCodeLists.length == 0 || embedCodeLists[0] == null || embedCodeLists[0].isEmpty()) { return null; }
      try {
        return contentTree(embedCodeLists[0]);
      } catch (OoyalaException e) {
        _error = e;
        return null;
      }
    }

    @Override
    protected void onPostExecute(ContentItem result) {
      _callback.callback(result, _error);
    }
  }

  @SuppressWarnings("unchecked")
  public Object contentTree(List<String> embedCodes, ContentTreeCallback callback) {
    ContentTreeTask task = new ContentTreeTask(callback);
    task.execute(embedCodes);
    return task;
  }

  public ContentItem contentTreeByExternalIds(List<String> externalIds) throws OoyalaException {
    String uri = String.format(Constants.CONTENT_TREE_BY_EXTERNAL_ID_URI, _pcode,
        Utils.join(externalIds, Constants.SEPARATOR_COMMA));
    JSONObject obj = OoyalaAPIHelper.objectForAPI(Constants.CONTENT_TREE_HOST, uri, contentTreeParams());
    if (obj == null) { return null; }
    List<String> embedCodes = new ArrayList<String>(); // will be filled in by verifyContentTreeObject call
                                                       // below
    JSONObject contentTree = null;
    try {
      contentTree = verifyContentTreeObject(obj, externalIds, embedCodes);
    } catch (OoyalaException e) {
      System.out.println("Unable to create externalId objects: " + e);
      throw e;
    }
    return ContentItem.create(contentTree, embedCodes, this);
  }

  private class ContentTreeByExternalIdsTask extends ContentTreeTask {
    public ContentTreeByExternalIdsTask(ContentTreeCallback callback) {
      super(callback);
    }

    @Override
    protected ContentItem doInBackground(List<String>... externalIdLists) {
      if (externalIdLists.length == 0 || externalIdLists[0] == null || externalIdLists[0].isEmpty()) { return null; }
      try {
        return contentTreeByExternalIds(externalIdLists[0]);
      } catch (OoyalaException e) {
        _error = e;
        return null;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public Object contentTreeByExternalIds(List<String> externalIds, ContentTreeCallback callback) {
    ContentTreeByExternalIdsTask task = new ContentTreeByExternalIdsTask(callback);
    task.execute(externalIds);
    return task;
  }

  public PaginatedItemResponse contentTreeNext(PaginatedParentItem parent) {
    if (!parent.hasMoreChildren()) { return null; }
    String uri = String.format(Constants.CONTENT_TREE_NEXT_URI, _pcode, parent.getNextChildren());
    JSONObject obj = OoyalaAPIHelper.objectForAPI(Constants.CONTENT_TREE_HOST, uri, contentTreeParams());
    if (obj == null) { return null; }
    JSONObject contentTree = null;
    List<String> keys = new ArrayList<String>();
    keys.add(parent.getNextChildren());
    try {
      contentTree = verifyContentTreeObject(obj, keys);
    } catch (Exception e) {
      System.out.println("Unable to create next objects: " + e);
      return null;
    }

    /**
     * NOTE: We have to convert the content token keyed dictionary to one that is embed code keyed in order
     * for it to work with update. We could just create a new update in each class, but this seemed better
     * because that would have a lot of duplicate code.
     */
    if (contentTree.isNull(parent.getNextChildren())) {
      System.out.println("Could not find token in content_tree_next response.");
      return null;
    }
    try {
      JSONObject tokenDict = contentTree.getJSONObject(parent.getNextChildren());
      JSONObject parentDict = new JSONObject();
      parentDict.put(parent.getEmbedCode(), tokenDict);

      int startIdx = parent.childrenCount();
      parent.update(parentDict);
      return new PaginatedItemResponse(startIdx, tokenDict.isNull(Constants.KEY_CHILDREN) ? 0 : tokenDict
          .getJSONArray(Constants.KEY_CHILDREN).length());
    } catch (JSONException e) {
      System.out.println("Unable to create next objects due to JSON Exception: " + e);
      return null;
    }
  }

  private class ContentTreeNextTask extends AsyncTask<Object, Integer, PaginatedItemResponse> {
    protected OoyalaException _error = null;
    protected ContentTreeNextCallback _callback = null;

    public ContentTreeNextTask(ContentTreeNextCallback callback) {
      super();
      _callback = callback;
    }

    @Override
    protected PaginatedItemResponse doInBackground(Object... params) {
      if (params.length < 1 || params[0] == null || !(params[0] instanceof PaginatedParentItem)) { return null; }
      return contentTreeNext((PaginatedParentItem) (params[1]));
    }

    @Override
    protected void onPostExecute(PaginatedItemResponse result) {
      _callback.callback(result, _error);
    }
  }

  public Object contentTreeNext(PaginatedParentItem parent, ContentTreeNextCallback callback) {
    ContentTreeNextTask task = new ContentTreeNextTask(callback);
    task.execute(parent);
    return task;
  }

  public String getPcode() {
    return _pcode;
  }

  public String getDomain() {
    return _domain;
  }

  public OoyalaAPIHelper getAPIHelper() {
    return _apiHelper;
  }

  @SuppressWarnings("rawtypes")
  public void cancel(Object task) {
    ((AsyncTask) task).cancel(true);
  }
}
