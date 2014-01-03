package com.ooyala.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.ooyala.android.OoyalaException.OoyalaErrorCode;

class PlayerAPIClient {
  protected String _pcode = null;
  protected String _domain = null;
  protected int _width = -1;
  protected int _height = -1;
  protected EmbedTokenGenerator _embedTokenGenerator;
  private boolean _isHook;
  public static final String HOOK = "-hook";
  private String _authToken = null; // ALWAYS use getters and setters for this
  protected int _heartbeatInterval = 300;
  protected Context _context;
  private UserInfo _userInfo;

  public PlayerAPIClient() {}

  public PlayerAPIClient(String pcode, String domain, EmbedTokenGenerator embedTokenGenerator) {
    _pcode = pcode;
    _domain = domain;
    _embedTokenGenerator = embedTokenGenerator;
  }

  private JSONObject verifyAuthorizeJSON(JSONObject authResult, List<String> embedCodes) throws OoyalaException {
    if (authResult == null) { throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_INVALID,
        "Authorization response invalid (nil)."); }

    try {
      if (!authResult.isNull(Constants.KEY_ERRORS)) {
        JSONObject errors = authResult.getJSONObject(Constants.KEY_ERRORS);
        if (!errors.isNull(Constants.KEY_CODE) && errors.getInt(Constants.KEY_CODE) != 0) { throw new OoyalaException(
            OoyalaErrorCode.ERROR_AUTHORIZATION_INVALID, errors.isNull(Constants.KEY_MESSAGE) ? ""
                : errors.getString(Constants.KEY_MESSAGE)); }
      }

      if (authResult.isNull(Constants.KEY_USER_INFO)) {
        throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_INVALID,
            "User info data does not exist.");
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

  private JSONObject verifyAuthorizeHeartbeatJSON(JSONObject result) throws OoyalaException {
    if (result == null) { throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_HEARTBEAT_FAILED,
        "response invalid (nil)."); }

    if (result.isNull(Constants.KEY_MESSAGE)) {
      throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_HEARTBEAT_FAILED,
          "response invalid (nil).");
    }
    try {
      if(!result.getString(Constants.KEY_MESSAGE).equals("OK")) {
        throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_HEARTBEAT_FAILED,
            "response code (" + result.getString(Constants.KEY_MESSAGE) + ").");
      } else if(result.getInt(Constants.KEY_EXPIRES) < System.currentTimeMillis()/1000  ) {
    	  throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_HEARTBEAT_FAILED,
    	          "response expired.");
      }
    } catch (JSONException e) {
      throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_HEARTBEAT_FAILED,
          "response invalid (error).");
    }
    return result;
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

  private Map<String, String> authorizeParams(List<String> embedCodes) {
    final Map<String, String> params = new HashMap<String, String>();
    params.put(Constants.KEY_DEVICE, Utils.device() + (_isHook ? HOOK : ""));
    params.put(Constants.KEY_DOMAIN, _domain);

    if (getAuthToken().length() > 0) {
      params.put(Constants.KEY_AUTH_TOKEN, getAuthToken());
    }

    if (_embedTokenGenerator != null) {
      final Semaphore sem = new Semaphore(0);
      _embedTokenGenerator.getTokenForEmbedCodes(embedCodes, new EmbedTokenGeneratorCallback() {
        @Override
        public void setEmbedToken(String token) {
          params.put("embedToken", token);
          sem.release();
        }
      });
      try {
        sem.acquire();
      } catch (InterruptedException e) {
        return params;
      }
    }
    return params;
  }

  private Map<String, String> contentTreeParams(Map<String, String> additionalParams) {
    Map<String, String> params = new HashMap<String, String>();
    if (additionalParams != null) {
      params.putAll(additionalParams);
    }
    params.put(Constants.KEY_DEVICE, Utils.device() + (_isHook ? HOOK : ""));
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

  public boolean authorize(AuthorizableItemInternal item, PlayerInfo playerInfo) throws OoyalaException {
    List<String> embedCodes = item.embedCodesToAuthorize();
    return authorizeEmbedCodes(embedCodes, item, playerInfo);
  }

  public boolean authorizeEmbedCodes(List<String> embedCodes, AuthorizableItemInternal parent)
      throws OoyalaException {
    String uri = String.format(Constants.AUTHORIZE_EMBED_CODE_URI, Constants.API_VERSION, _pcode,
        Utils.join(embedCodes, Constants.SEPARATOR_COMMA));
    JSONObject json = OoyalaAPIHelper.objectForAPI(Constants.AUTHORIZE_HOST, uri, authorizeParams(embedCodes));
    JSONObject authData = null;
    try {
      authData = verifyAuthorizeJSON(json, embedCodes);

      //parse out and save auth token and heartbeat data
      if (!json.isNull(Constants.KEY_AUTH_TOKEN)) {
        setAuthToken(json.getString(Constants.KEY_AUTH_TOKEN));
      }

      if (!json.isNull(Constants.KEY_HEARTBEAT_DATA)) {
        JSONObject heartbeatData = json.getJSONObject(Constants.KEY_HEARTBEAT_DATA);
        if (!heartbeatData.isNull(Constants.KEY_HEARTBEAT_INTERVAL)) {
          _heartbeatInterval = heartbeatData.getInt(Constants.KEY_HEARTBEAT_INTERVAL);
        }
      }

      if (!json.isNull(Constants.KEY_USER_INFO)) {
        _userInfo = new UserInfo(json.getJSONObject(Constants.KEY_USER_INFO));
      }
    } catch (OoyalaException e) {
      System.out.println("Unable to authorize: " + e);
      throw e;
    } catch (JSONException exception) {
      System.out.println("JSONException: " + exception);
      throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_INVALID,
          "Authorization response invalid (exception).");
    }

    if (parent != null) {
      parent.update(authData);
    }
    return true;
  }

  public boolean authorizeEmbedCodes(List<String> embedCodes, AuthorizableItemInternal parent, PlayerInfo playerInfo)
      throws OoyalaException {
    String uri = String.format(Constants.AUTHORIZE_EMBED_CODE_URI, Constants.API_VERSION, _pcode,
        Utils.join(embedCodes, Constants.SEPARATOR_COMMA));
    Map<String, String> params = authorizeParams(embedCodes);
    params.put("device", playerInfo.getDevice() + (_isHook ? HOOK : ""));

    if (playerInfo.getSupportedFormats() != null)
      params.put("supportedFormats", Utils.join(playerInfo.getSupportedFormats(), ","));

    if (playerInfo.getSupportedProfiles() != null)
      params.put("profiles", Utils.join(playerInfo.getSupportedProfiles(), ","));

    if (playerInfo.getMaxHeight() > 0)
      params.put("maxHeight", Integer.toString(playerInfo.getMaxHeight()));

    if (playerInfo.getMaxWidth() > 0)
      params.put("maxWidth", Integer.toString(playerInfo.getMaxWidth()));

    if (playerInfo.getMaxBitrate() > 0) {
      params.put("br", Integer.toString(playerInfo.getMaxBitrate()));
    }

    JSONObject json = OoyalaAPIHelper.objectForAPI(Constants.AUTHORIZE_HOST, uri, params);
    JSONObject authData = null;
    try {
      authData = verifyAuthorizeJSON(json, embedCodes);
      //parse out and save auth token and heartbeat data
      if (!json.isNull(Constants.KEY_AUTH_TOKEN)) {
        setAuthToken(json.getString(Constants.KEY_AUTH_TOKEN));
      }

      if (!json.isNull(Constants.KEY_HEARTBEAT_DATA)) {
        JSONObject heartbeatData = json.getJSONObject(Constants.KEY_HEARTBEAT_DATA);
        if (!heartbeatData.isNull(Constants.KEY_HEARTBEAT_INTERVAL)) {
          _heartbeatInterval = heartbeatData.getInt(Constants.KEY_HEARTBEAT_INTERVAL);
        }
      }

      if (!json.isNull(Constants.KEY_USER_INFO)) {
        _userInfo = new UserInfo(json.getJSONObject(Constants.KEY_USER_INFO));
      }
    } catch (OoyalaException e) {
      System.out.println("Unable to authorize: " + e);
      throw e;
    } catch (JSONException exception) {
      System.out.println("JSONException: " + exception);
      throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_INVALID,
          "Authorization response invalid (exception).");
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
    protected Boolean doInBackground(Object... params) { // List<String> embedCodes
                                                         // AuthorizableItemInternal parent
      if (params.length < 2) { return false; }
      if (!(params[0] instanceof List<?>)) { return false; }

      List<String> embedCodes = (List<String>)params[0];
      AuthorizableItemInternal parent = params[1] instanceof AuthorizableItemInternal ?
          (AuthorizableItemInternal) params[1] : null;

      switch (params.length) {
        case 2:
          try {
            return authorizeEmbedCodes(embedCodes, parent);
          } catch (OoyalaException e) {
            _error = e;
            return false;
          }
        case 3:
          PlayerInfo playerInfo = params[2] instanceof PlayerInfo ? (PlayerInfo)params[2] : null;
          try {
            return authorizeEmbedCodes(embedCodes,  parent, playerInfo);
          } catch (OoyalaException e) {
            _error = e;
            return false;
          }
          default:
            return false;
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

  public Object authorize(AuthorizableItemInternal item, PlayerInfo playerInfo, AuthorizeCallback callback) {
    return authorizeEmbedCodes(item.embedCodesToAuthorize(), item, playerInfo, callback);
  }

  public Object authorizeEmbedCodes(List<String> embedCodes, AuthorizableItemInternal parent,
      AuthorizeCallback callback) {
    AuthorizeTask task = new AuthorizeTask(callback);
    task.execute(embedCodes, parent);
    return task;
  }

  // boolean here refers to the response.
  public boolean authorizeHeartbeat() throws OoyalaException {
    String uri = String.format(Constants.AUTHORIZE_HEARTBEAT_URI, Constants.API_VERSION, _pcode, getAuthToken());
    JSONObject json = OoyalaAPIHelper.objectForAPI(Constants.AUTHORIZE_HOST, uri, null);
    try {
      return verifyAuthorizeHeartbeatJSON(json) != null;  // any returned result is valid
    } catch (OoyalaException e) {
      System.out.println("Unable to authorize: " + e);
      throw e;
    }
  }

  private class AuthorizeHeartbeatTask extends AsyncTask<Void, Void, Boolean> {
    protected OoyalaException _error = null;
    protected AuthorizeHeartbeatCallback _callback = null;

    public AuthorizeHeartbeatTask(AuthorizeHeartbeatCallback callback) {
      super();
      _callback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... params) { //params should be null here
      try {
        return authorizeHeartbeat();
      } catch (OoyalaException e) {
        _error = e;
        return Boolean.FALSE;
      }
    }

    @Override
    protected void onPostExecute(Boolean result) {
      _callback.callback(result.booleanValue(), _error);
    }
  }

  public interface AuthorizeHeartbeatCallback {
    /**
     * This callback is used for asynchronous authorize heartbeat calls
     * @param result true if the authorize call succeeded, false otherwise
     * @param error the OoyalaException if there was one
     */
    public void callback(boolean result, OoyalaException error);
  }

  public Object authorizeHeartbeat(AuthorizeHeartbeatCallback callback) {
    AuthorizeHeartbeatTask task = new AuthorizeHeartbeatTask(callback);
    task.execute();
    return task;
  }
  public Object authorizeEmbedCodes(List<String> embedCodes, AuthorizableItemInternal parent,
      PlayerInfo playerInfo, AuthorizeCallback callback) {
    AuthorizeTask task = new AuthorizeTask(callback);
    task.execute(embedCodes, parent, playerInfo);
    return task;
  }

  public ContentItem contentTree(List<String> embedCodes) throws OoyalaException {
    return contentTreeWithAdSet(embedCodes, null);
  }

  public ContentItem contentTreeWithAdSet(List<String> embedCodes, String adSetCode) throws OoyalaException {
    Map<String, String> params = null;
    if (adSetCode != null) {
      params = new HashMap<String, String>(1);
      params.put(Constants.KEY_AD_SET_CODE, adSetCode);
    }

    String uri = String.format(Constants.CONTENT_TREE_URI, Constants.API_VERSION, _pcode,
        Utils.join(embedCodes, Constants.SEPARATOR_COMMA));
    JSONObject obj = OoyalaAPIHelper.objectForAPI(Constants.CONTENT_TREE_HOST, uri, contentTreeParams(params));
    JSONObject contentTree = null;
    try {
      contentTree = verifyContentTreeObject(obj, embedCodes);
    } catch (OoyalaException e) {
      System.out.println("Unable to create objects: " + e);
      throw e;
    }
    ContentItem item = ContentItem.create(contentTree, embedCodes, this);
    if (item == null) { throw new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID,
        "Unknown Content Type"); }
    return item;
  }

  private class ContentTreeTaskParam {
    public List<String> idList;
    public String adSetCode;
  }

  private class ContentTreeTask extends AsyncTask<ContentTreeTaskParam, Integer, ContentItem> {
    protected OoyalaException _error = null;
    protected ContentTreeCallback _callback = null;

    public ContentTreeTask(ContentTreeCallback callback) {
      super();
      _callback = callback;
    }

    @Override
    protected ContentItem doInBackground(ContentTreeTaskParam... taskParams) {
      if (taskParams.length == 0 || taskParams[0] == null || taskParams[0].idList == null ||
          taskParams[0].idList.isEmpty()) { return null; }
      try {
        return contentTreeWithAdSet(taskParams[0].idList, taskParams[0].adSetCode);
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

  public Object contentTree(List<String> embedCodes, ContentTreeCallback callback) {
    return contentTreeWithAdSet(embedCodes, null, callback);
  }

  public Object contentTreeWithAdSet(List<String> embedCodes, String adSetCode, ContentTreeCallback callback) {
    ContentTreeTask task = new ContentTreeTask(callback);
    ContentTreeTaskParam taskParam = new ContentTreeTaskParam();
    taskParam.idList = embedCodes;
    taskParam.adSetCode = adSetCode;
    task.execute(taskParam);
    return task;
  }

  public ContentItem contentTreeByExternalIds(List<String> externalIds) throws OoyalaException {
    String uri = String.format(Constants.CONTENT_TREE_BY_EXTERNAL_ID_URI, Constants.API_VERSION, _pcode,
        Utils.join(externalIds, Constants.SEPARATOR_COMMA));
    JSONObject obj = OoyalaAPIHelper.objectForAPI(Constants.CONTENT_TREE_HOST, uri, contentTreeParams(null));
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

    ContentItem item = ContentItem.create(contentTree, embedCodes, this);
    if (item == null) { throw new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID,
        "Unknown Content Type"); }
    return item;
  }

  private class ContentTreeByExternalIdsTask extends ContentTreeTask {
    public ContentTreeByExternalIdsTask(ContentTreeCallback callback) {
      super(callback);
    }

    @Override
    protected ContentItem doInBackground(ContentTreeTaskParam... taskParams) {
      if (taskParams.length == 0 || taskParams[0] == null || taskParams[0].idList == null ||
          taskParams[0].idList.isEmpty()) { return null; }
      try {
        return contentTreeByExternalIds(taskParams[0].idList);
      } catch (OoyalaException e) {
        _error = e;
        return null;
      }
    }
  }

  public Object contentTreeByExternalIds(List<String> externalIds, ContentTreeCallback callback) {
    return contentTreeByExternalIdsWithAdSetCode(externalIds, null, callback);
  }

  public Object contentTreeByExternalIdsWithAdSetCode(List<String> externalIds, String adSetCode, ContentTreeCallback callback) {
    ContentTreeByExternalIdsTask task = new ContentTreeByExternalIdsTask(callback);
    ContentTreeTaskParam taskParam = new ContentTreeTaskParam();
    taskParam.idList = externalIds;
    task.execute(taskParam);
    return task;
  }

  public PaginatedItemResponse contentTreeNext(PaginatedParentItem parent) {
    if (!parent.hasMoreChildren()) { return null; }
    String uri = String.format(Constants.CONTENT_TREE_NEXT_URI, Constants.API_VERSION, _pcode,
        parent.getNextChildren());
    JSONObject obj = OoyalaAPIHelper.objectForAPI(Constants.CONTENT_TREE_HOST, uri, contentTreeParams(null));
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


  public boolean fetchMetadataForEmbedCodes(List<String> embedCodes, AuthorizableItem parent) throws OoyalaException {
    // fetch metadata
    String uri = String.format(Constants.METADATA_EMBED_CODE_URI, Constants.API_VERSION, _pcode,
        Utils.join(embedCodes, Constants.SEPARATOR_COMMA));
    JSONObject root = OoyalaAPIHelper.objectForAPI(Constants.METADATA_HOST, uri, contentTreeParams(null));

    // validate the result
    if (root == null) {
      throw new OoyalaException(OoyalaErrorCode.ERROR_METADATA_FETCH_FAILED, "Empty metadata response");
    }

    try {
      int errorCode = root.getJSONObject("errors").getInt("code");
      if(errorCode != 0) {
        throw new OoyalaException(OoyalaErrorCode.ERROR_METADATA_FETCH_FAILED, "Non-zero metadata response code");
      }

      ((ContentItem)parent).update(root.getJSONObject(Constants.KEY_METADATA));

    } catch (JSONException je) {
      throw new OoyalaException(OoyalaErrorCode.ERROR_METADATA_FETCH_FAILED, "Failed to parse metadata");
    }

    // return the JSON data
    return true;
  }

  public boolean fetchMetadata(ContentItem item) throws OoyalaException {
    return fetchMetadataForEmbedCodes(item.embedCodesToAuthorize(), item);
  }

  private class MetadataFetchTaskParam {
    public ContentItem item;
  }

  private class MetadataFetchTask extends AsyncTask<MetadataFetchTaskParam, Integer, Boolean> {
    protected OoyalaException _error = null;
    protected MetadataFetchedCallback _callback = null;

    public MetadataFetchTask(MetadataFetchedCallback callback) {
      super();
      _callback = callback;
    }

    @Override
    protected Boolean doInBackground(MetadataFetchTaskParam... taskParams) {
      if (taskParams.length == 0 || taskParams[0] == null || taskParams[0].item == null) { return false; }
      try {
        return fetchMetadata(taskParams[0].item);
      } catch (OoyalaException e) {
        _error = e;
        return null;
      }
    }

    @Override
    protected void onPostExecute(Boolean result) {
      _callback.callback(result, _error);
    }
  }

  public Object metadata(ContentItem item, MetadataFetchedCallback callback) {
    MetadataFetchTask task = new MetadataFetchTask(callback);
    MetadataFetchTaskParam taskParam = new MetadataFetchTaskParam();
    taskParam.item = item;
    task.execute(taskParam);
    return task;
  }

  public String getPcode() {
    return _pcode;
  }

  public String getDomain() {
    return _domain;
  }

  private void setAuthToken(String authToken) {
    _authToken = authToken;
    if (_context != null) {
      SharedPreferences preferences = _context.getSharedPreferences("com.ooyala.android_preferences", 4);
      SharedPreferences.Editor editor = preferences.edit();
      editor.putString("authToken", authToken);
      editor.commit();
    }
  }

  public String getAuthToken() {
    if (_authToken == null) {
      if (_context != null) {
        SharedPreferences preferences = _context.getSharedPreferences("com.ooyala.android_preferences", 4);
        _authToken = preferences.getString("authToken", "");
      } else {
        _authToken = "";
      }
    }
    return _authToken;
  }

  public int getHeartbeatInterval() {
    return _heartbeatInterval;
  }

  public UserInfo getUserInfo() {
    return _userInfo;
  }

  public void setHook() {
    _isHook = true;
  }

  public void setContext(Context context) {
    _context = context;
  }

  @SuppressWarnings("rawtypes")
  public void cancel(Object task) {
    ((AsyncTask) task).cancel(true);
  }
}
