package com.ooyala.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.ooyala.android.OoyalaException.OoyalaErrorCode;
import com.ooyala.android.configuration.Options;
import com.ooyala.android.item.AuthorizableItem;
import com.ooyala.android.item.ContentItem;
import com.ooyala.android.item.PaginatedParentItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

class PlayerAPIClient {
  //private static String TAG = PlayerAPIClient.class.getName();
  protected static final String KEY_DOMAIN = "domain";
  protected static final String KEY_AUTHORIZATION_DATA = "authorization_data";
  protected static final String KEY_USER_INFO = "user_info";
  protected static final String KEY_CONTENT_TREE = "content_tree";
  protected static final String KEY_MESSAGE = "message";
  protected static final String KEY_AD_SET_CODE = "adSetCode";
  protected static final String KEY_HEARTBEAT_DATA = "heartbeat_data";
  protected static final String KEY_HEARTBEAT_INTERVAL = "heartbeat_interval";
  protected static final String KEY_AUTH_TOKEN = "auth_token";
  protected static final String KEY_CODE = "code";
  protected static final String KEY_AUTHORIZED = "authorized";
  protected static final String KEY_CHILDREN = "children";
  protected static final String KEY_EXPIRES = "expires";
  protected static final String KEY_HEIGHT = "height";
  protected static final String KEY_WIDTH = "width";
  protected static final String KEY_METADATA = "metadata";
  protected static final String KEY_DEVICE = "device";
  protected static final String KEY_ERRORS = "errors";

  protected static final String AUTHORIZE_CONTENT_ID_URI = "/sas/player_api/v%s/authorization/content_id/%s/%s";
  protected static final String AUTHORIZE_EMBED_CODE_URI = "/sas/player_api/v%s/authorization/embed_code/%s/%s";
  protected static final String AUTHORIZE_HEARTBEAT_URI = "/sas/player_api/v%s/auth_heartbeat/pcode/%s/auth_token/%s";
  protected static final String AUTHORIZE_PUBLIC_KEY_B64 = "MCgCIQD1PX86jvLr5bB3b5IFEze7TiWGEaRSHl5Ls7/3AKO5IwIDAQAB";
  protected static final String AUTHORIZE_PUBLIC_KEY_NAME = "sas_public_key";
  protected static final int AUTHORIZE_SIGNATURE_DIGEST_LENGTH = 20;

  protected static final String BACKLOT_URI_PREFIX = "/v2";

  protected static final String CONTENT_TREE_URI = "/player_api/v%s/content_tree/embed_code/%s/%s";
  protected static final String CONTENT_TREE_BY_EXTERNAL_ID_URI = "/player_api/v%s/content_tree/external_id/%s/%s";
  protected static final String CONTENT_TREE_NEXT_URI = "/player_api/v%s/content_tree/next/%s/%s";

  protected static final String METADATA_EMBED_CODE_URI = "/player_api/v%s/metadata/embed_code/%s/%s";

  protected static final String SEPARATOR_URL_IDS = ",";

  protected String _pcode = null;
  protected PlayerDomain _domain = null;
  protected int _width = -1;
  protected int _height = -1;
  protected EmbedTokenGenerator _embedTokenGenerator;
  private boolean _isHook;
  public static final String HOOK = "-hook";
  private String _authToken = null; // ALWAYS use getters and setters for this
  protected int _heartbeatInterval = 300;
  protected Context _context;
  private UserInfo _userInfo;
  private int _connectionTimeoutInMillisecond = 0;
  private int _readTimeoutInMillisecond = 0;

  public PlayerAPIClient() {}

  public PlayerAPIClient(String pcode, PlayerDomain domain, EmbedTokenGenerator embedTokenGenerator, Options options) {
    _pcode = pcode;
    _domain = domain;
    _embedTokenGenerator = embedTokenGenerator;
    if (options != null) {
      _connectionTimeoutInMillisecond = options.getConnectionTimeoutInMillisecond();
      _readTimeoutInMillisecond = options.getReadTimeoutInMillisecond();
    }
  }

  private JSONObject verifyAuthorizeJSON(JSONObject authResult, List<String> embedCodes) throws OoyalaException {
    if (authResult == null) { throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_INVALID,
        "Authorization response invalid (nil)."); }

    try {
      if (!authResult.isNull(KEY_ERRORS)) {
        JSONObject errors = authResult.getJSONObject(KEY_ERRORS);
        if (!errors.isNull(KEY_CODE) && errors.getInt(KEY_CODE) != 0) { throw new OoyalaException(
            OoyalaErrorCode.ERROR_AUTHORIZATION_INVALID, errors.isNull(KEY_MESSAGE) ? ""
                : errors.getString(KEY_MESSAGE)); }
      }

      if (authResult.isNull(KEY_USER_INFO)) {
        throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_INVALID,
            "User info data does not exist.");
      }

      if (authResult.isNull(KEY_AUTHORIZATION_DATA)) {
        throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_INVALID,
            "Authorization data does not exist.");
      } else {
        JSONObject authData = authResult.getJSONObject(KEY_AUTHORIZATION_DATA);
        for (String embedCode : embedCodes) {
          if (authData.isNull(embedCode)
              || authData.getJSONObject(embedCode).isNull(KEY_AUTHORIZED)) { throw new OoyalaException(
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

    if (result.isNull(KEY_MESSAGE)) {
      throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_HEARTBEAT_FAILED,
          "response invalid (nil).");
    }
    try {
      if(!result.getString(KEY_MESSAGE).equals("OK")) {
        throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_HEARTBEAT_FAILED,
            "response code (" + result.getString(KEY_MESSAGE) + ").");
      } else if(result.getInt(KEY_EXPIRES) < System.currentTimeMillis()/1000  ) {
    	  throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_HEARTBEAT_FAILED,
    	          "response expired.");
      }
      if (!result.isNull(KEY_AUTH_TOKEN)) {
          setAuthToken(result.getString(KEY_AUTH_TOKEN));
      }
    } catch (JSONException e) {
      throw new OoyalaException(OoyalaErrorCode.ERROR_AUTHORIZATION_HEARTBEAT_FAILED,
          "response invalid (error).");
    }
    return result;
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
    params.put(KEY_DEVICE, Utils.device() + (_isHook ? HOOK : ""));
    params.put(KEY_DOMAIN, _domain.toString());

    if (getAuthToken().length() > 0) {
      params.put(KEY_AUTH_TOKEN, getAuthToken());
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
                                                         // AuthorizableItem parent
      if (params.length < 2) { return false; }
      if (!(params[0] instanceof List<?>)) { return false; }

      List<String> embedCodes = (List<String>)params[0];
      AuthorizableItem parent = params[1] instanceof AuthorizableItem ?
          (AuthorizableItem) params[1] : null;

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

  public boolean authorize(AuthorizableItem item) throws OoyalaException {
    List<String> embedCodes = item.embedCodesToAuthorize();
    return authorizeEmbedCodes(embedCodes, item);
  }

  public boolean authorize(AuthorizableItem item, PlayerInfo playerInfo) throws OoyalaException {
    List<String> embedCodes = item.embedCodesToAuthorize();
    return authorizeEmbedCodes(embedCodes, item, playerInfo);
  }

  public Object authorize(AuthorizableItem item, AuthorizeCallback callback) {
    return authorizeEmbedCodes(item.embedCodesToAuthorize(), item, callback);
  }

  public Object authorize(AuthorizableItem item, PlayerInfo playerInfo, AuthorizeCallback callback) {
    return authorizeEmbedCodes(item.embedCodesToAuthorize(), item, playerInfo, callback);
  }

  public boolean authorizeEmbedCodes(List<String> embedCodes, AuthorizableItem parent)
      throws OoyalaException {
    String uri = String.format(AUTHORIZE_EMBED_CODE_URI, OoyalaPlayer.API_VERSION, _pcode,
        Utils.join(embedCodes, SEPARATOR_URL_IDS));
    JSONObject json = OoyalaAPIHelper.objectForAPI(Environment.AUTHORIZE_HOST,
            uri, authorizeParams(embedCodes),
            _connectionTimeoutInMillisecond, _readTimeoutInMillisecond);
    JSONObject authData = null;
    try {
      authData = verifyAuthorizeJSON(json, embedCodes);

      //parse out and save auth token and heartbeat data
      if (!json.isNull(KEY_AUTH_TOKEN)) {
        setAuthToken(json.getString(KEY_AUTH_TOKEN));
      }

      if (!json.isNull(KEY_HEARTBEAT_DATA)) {
        JSONObject heartbeatData = json.getJSONObject(KEY_HEARTBEAT_DATA);
        if (!heartbeatData.isNull(KEY_HEARTBEAT_INTERVAL)) {
          _heartbeatInterval = heartbeatData.getInt(KEY_HEARTBEAT_INTERVAL);
        }
      }

      if (!json.isNull(KEY_USER_INFO)) {
        _userInfo = new UserInfo(json.getJSONObject(KEY_USER_INFO));
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

  public boolean authorizeEmbedCodes(List<String> embedCodes, AuthorizableItem parent, PlayerInfo playerInfo)
      throws OoyalaException {
    String uri = String.format(AUTHORIZE_EMBED_CODE_URI, OoyalaPlayer.API_VERSION, _pcode,
        Utils.join(embedCodes, SEPARATOR_URL_IDS));
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

    JSONObject json = OoyalaAPIHelper.objectForAPI(Environment.AUTHORIZE_HOST, uri, params,
            _connectionTimeoutInMillisecond, _readTimeoutInMillisecond);
    JSONObject authData = null;
    try {
      authData = verifyAuthorizeJSON(json, embedCodes);
      //parse out and save auth token and heartbeat data
      if (!json.isNull(KEY_AUTH_TOKEN)) {
        setAuthToken(json.getString(KEY_AUTH_TOKEN));
      }

      if (!json.isNull(KEY_HEARTBEAT_DATA)) {
        JSONObject heartbeatData = json.getJSONObject(KEY_HEARTBEAT_DATA);
        if (!heartbeatData.isNull(KEY_HEARTBEAT_INTERVAL)) {
          _heartbeatInterval = heartbeatData.getInt(KEY_HEARTBEAT_INTERVAL);
        }
      }

      if (!json.isNull(KEY_USER_INFO)) {
        _userInfo = new UserInfo(json.getJSONObject(KEY_USER_INFO));
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

  public Object authorizeEmbedCodes(List<String> embedCodes, AuthorizableItem parent,
      AuthorizeCallback callback) {
    AuthorizeTask task = new AuthorizeTask(callback);
    task.execute(embedCodes, parent);
    return task;
  }

  public Object authorizeEmbedCodes(List<String> embedCodes, AuthorizableItem parent,
      PlayerInfo playerInfo, AuthorizeCallback callback) {
    AuthorizeTask task = new AuthorizeTask(callback);
    task.execute(embedCodes, parent, playerInfo);
    return task;
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

  // boolean here refers to the response.
  public boolean authorizeHeartbeat() throws OoyalaException {
    String uri = String.format(AUTHORIZE_HEARTBEAT_URI, OoyalaPlayer.API_VERSION, _pcode, getAuthToken());
    JSONObject json = OoyalaAPIHelper.objectForAPI(Environment.AUTHORIZE_HOST, uri, null,
            _connectionTimeoutInMillisecond, _readTimeoutInMillisecond);
    try {
      return verifyAuthorizeHeartbeatJSON(json) != null;  // any returned result is valid
    } catch (OoyalaException e) {
      System.out.println("Unable to authorize: " + e);
      throw e;
    }
  }

  public Object authorizeHeartbeat(AuthorizeHeartbeatCallback callback) {
    AuthorizeHeartbeatTask task = new AuthorizeHeartbeatTask(callback);
    task.execute();
    return task;
  }
  private Map<String, String> contentTreeParams(Map<String, String> additionalParams) {
    Map<String, String> params = new HashMap<String, String>();
    if (additionalParams != null) {
      params.putAll(additionalParams);
    }
    params.put(KEY_DEVICE, Utils.device() + (_isHook ? HOOK : ""));
    if (_height > 0 && _width > 0) {
      params.put(KEY_WIDTH, Integer.toString(_width));
      params.put(KEY_HEIGHT, Integer.toString(_height));
    }
    return params;
  }

  private JSONObject getContentTreeData(JSONObject contentTree) throws OoyalaException {
    if (contentTree == null) { throw new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID,
        "Content Tree response invalid (nil)."); }

    try {
      if (!contentTree.isNull(KEY_ERRORS)) {
        JSONObject errors = contentTree.getJSONObject(KEY_ERRORS);
        if (!errors.isNull(KEY_CODE) && errors.getInt(KEY_CODE) != 0) { throw new OoyalaException(
            OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID, errors.isNull(KEY_MESSAGE) ? ""
                : errors.getString(KEY_MESSAGE)); }
      }

      // TODO(mikhail): currently we do not check signature. fix this once we properly implement signatures
      // server side.

      if (contentTree.isNull(KEY_CONTENT_TREE)) {
        throw new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID,
            "Content tree data does not exist.");
      } else {
        return contentTree.getJSONObject(KEY_CONTENT_TREE);
      }
    } catch (JSONException exception) {
      System.out.println("JSONException: " + exception);
      throw new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID,
          "Content tree response invalid (exception).");
    }
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

  public ContentItem contentTree(List<String> embedCodes) throws OoyalaException {
    return contentTreeWithAdSet(embedCodes, null);
  }

  public ContentItem contentTreeWithAdSet(List<String> embedCodes, String adSetCode) throws OoyalaException {
    Map<String, String> params = null;
    if (adSetCode != null) {
      params = new HashMap<String, String>(1);
      params.put(KEY_AD_SET_CODE, adSetCode);
    }

    String uri = String.format(CONTENT_TREE_URI, OoyalaPlayer.API_VERSION, _pcode,
        Utils.join(embedCodes, SEPARATOR_URL_IDS));
    JSONObject obj = OoyalaAPIHelper.objectForAPI(Environment.CONTENT_TREE_HOST, uri, contentTreeParams(params),
            _connectionTimeoutInMillisecond, _readTimeoutInMillisecond);
    JSONObject contentTree = null;
    try {
      contentTree = verifyContentTreeObject(obj, embedCodes);
    } catch (OoyalaException e) {
      System.out.println("Unable to create objects: " + e);
      throw e;
    }
    ContentItem item = ContentItem.create(contentTree, embedCodes, new OoyalaAPIClient(this));
    if (item == null) { throw new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID,
        "Unknown Content Type"); }
    return item;
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
    String uri = String.format(CONTENT_TREE_BY_EXTERNAL_ID_URI, OoyalaPlayer.API_VERSION, _pcode,
        Utils.join(externalIds, SEPARATOR_URL_IDS));
    JSONObject obj = OoyalaAPIHelper.objectForAPI(Environment.CONTENT_TREE_HOST, uri, contentTreeParams(null),
            _connectionTimeoutInMillisecond, _readTimeoutInMillisecond);
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

    ContentItem item = ContentItem.create(contentTree, embedCodes, new OoyalaAPIClient(this));
    if (item == null) { throw new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID,
        "Unknown Content Type"); }
    return item;
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
    String uri = String.format(CONTENT_TREE_NEXT_URI, OoyalaPlayer.API_VERSION, _pcode,
        parent.getNextChildren());
    JSONObject obj = OoyalaAPIHelper.objectForAPI(Environment.CONTENT_TREE_HOST, uri, contentTreeParams(null),
            _connectionTimeoutInMillisecond, _readTimeoutInMillisecond);
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
      return new PaginatedItemResponse(startIdx, tokenDict.isNull(KEY_CHILDREN) ? 0 : tokenDict
          .getJSONArray(KEY_CHILDREN).length());
    } catch (JSONException e) {
      System.out.println("Unable to create next objects due to JSON Exception: " + e);
      return null;
    }
  }

  public Object contentTreeNext(PaginatedParentItem parent, ContentTreeNextCallback callback) {
    ContentTreeNextTask task = new ContentTreeNextTask(callback);
    task.execute(parent);
    return task;
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

  public boolean fetchMetadataForEmbedCodes(List<String> embedCodes, AuthorizableItem parent) throws OoyalaException {
    // fetch metadata
    String uri = String.format(METADATA_EMBED_CODE_URI, OoyalaPlayer.API_VERSION, _pcode,
        Utils.join(embedCodes, SEPARATOR_URL_IDS));
    JSONObject root = OoyalaAPIHelper.objectForAPI(Environment.METADATA_HOST, uri, contentTreeParams(null),
            _connectionTimeoutInMillisecond, _readTimeoutInMillisecond);

    // validate the result
    if (root == null) {
      throw new OoyalaException(OoyalaErrorCode.ERROR_METADATA_FETCH_FAILED, "Empty metadata response");
    }

    try {
      int errorCode = root.getJSONObject("errors").getInt("code");
      if(errorCode != 0) {
        throw new OoyalaException(OoyalaErrorCode.ERROR_METADATA_FETCH_FAILED, "Non-zero metadata response code");
      }

      ((ContentItem)parent).update(root.getJSONObject(KEY_METADATA));

    } catch (JSONException je) {
      throw new OoyalaException(OoyalaErrorCode.ERROR_METADATA_FETCH_FAILED, "Failed to parse metadata");
    }

    // return the JSON data
    return true;
  }

  public boolean fetchMetadata(ContentItem item) throws OoyalaException {
    return fetchMetadataForEmbedCodes(item.embedCodesToAuthorize(), item);
  }

  public Object metadata(ContentItem item, MetadataFetchedCallback callback) {
    MetadataFetchTask task = new MetadataFetchTask(callback);
    MetadataFetchTaskParam taskParam = new MetadataFetchTaskParam();
    taskParam.item = item;
    task.execute(taskParam);
    return task;
  }

  private boolean _isFetchingMoreChildren = false;
  public boolean fetchMoreChildrenForPaginatedParentItem(PaginatedParentItem parent, PaginatedItemListener listener) {
    // The two lines below aren't within a synchronized block because we assume
    // single thread
    // of execution except for the threads we explicitly spawn below, but those
    // set
    // _isFetchingMoreChildren = false at the very end of their execution.
    if (!parent.hasMoreChildren() || _isFetchingMoreChildren) { return false; }
    _isFetchingMoreChildren = true;

    Thread thread = new Thread(new NextChildrenRunner(parent, listener));
    thread.start();
    return true;
  }

  private class NextChildrenRunner implements Runnable {
    private PaginatedItemListener _listener = null;
    private PaginatedParentItem _parent = null;

    public NextChildrenRunner(PaginatedParentItem parent, PaginatedItemListener listener) {
      _parent = parent;
      _listener = listener;
    }

    @Override
    public void run() {
      PaginatedItemResponse response = contentTreeNext(_parent);
      if (response == null) {
        _listener.onItemsFetched(-1, 0, new OoyalaException(OoyalaErrorCode.ERROR_CONTENT_TREE_NEXT_FAILED,
            "Null response"));
        _isFetchingMoreChildren = false;
        return;
      }

      if (response.firstIndex < 0) {
        _listener.onItemsFetched(response.firstIndex, response.count, new OoyalaException(
            OoyalaErrorCode.ERROR_CONTENT_TREE_NEXT_FAILED, "No additional children found"));
        _isFetchingMoreChildren = false;
        return;
      }

      List<String> childEmbedCodesToAuthorize = ContentItem.getEmbedCodes(_parent.getAllAvailableChildren().subList(
          response.firstIndex, response.firstIndex + response.count));
      try {
        if (authorizeEmbedCodes(childEmbedCodesToAuthorize, (ContentItem)_parent) &&
            fetchMetadataForEmbedCodes(childEmbedCodesToAuthorize, (ContentItem)_parent)) {
          _listener.onItemsFetched(response.firstIndex, response.count, null);
        } else {
          _listener.onItemsFetched(response.firstIndex, response.count, new OoyalaException(
              OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED, "Additional child authorization failed"));
        }
      } catch (OoyalaException e) {
        _listener.onItemsFetched(response.firstIndex, response.count, e);
      }
      _isFetchingMoreChildren = false;
      return;
    }
  }

  public String getPcode() {
    return _pcode;
  }

  public PlayerDomain getDomain() {
    return _domain;
  }

  private void setAuthToken(String authToken) {
    _authToken = authToken;
    if (_context != null) {
      SharedPreferences preferences = _context.getSharedPreferences(OoyalaPlayer.PREFERENCES_NAME, 4);
      SharedPreferences.Editor editor = preferences.edit();
      editor.putString("authToken", authToken);
      editor.commit();
    }
  }

  public String getAuthToken() {
    if (_authToken == null) {
      if (_context != null) {
        SharedPreferences preferences = _context.getSharedPreferences(OoyalaPlayer.PREFERENCES_NAME, 4);
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

  public int getConnectionTimeoutInMillisecond() {
    return _connectionTimeoutInMillisecond;
  }

  public int getReadTimeoutInMillisecond() {
    return _readTimeoutInMillisecond;
  }
}
