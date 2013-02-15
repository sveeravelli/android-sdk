package com.ooyala.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;
import com.ooyala.android.NexPlayerMoviePlayer;

import com.ooyala.android.AuthHeartbeat.OnAuthHeartbeatErrorListener;
import com.ooyala.android.AuthorizableItem.AuthCode;

public class OoyalaPlayer extends Observable implements Observer, OnAuthHeartbeatErrorListener {
  public static final String PLAYER_VISUALON = "VisualOn";
  public static final String PLAYER_ANDROID = "Android Default";
  public static enum ActionAtEnd {
    CONTINUE, PAUSE, STOP, RESET
  };

  public static enum State {
    INIT, LOADING, READY, PLAYING, PAUSED, COMPLETED, SUSPENDED, ERROR
  };

  /**
   * Used by previousVideo and nextVideo. When passed to them, it will cause the video to be played after it
   * is set.
   */
  public static final int DO_PLAY = 0;
  /**
   * Used by previousVideo and nextVideo. When passed to them, it will cause the video to be paused after it
   * is set.
   */
  public static final int DO_PAUSE = 1;

  public static final String TIME_CHANGED_NOTIFICATION = "timeChanged";
  public static final String STATE_CHANGED_NOTIFICATION = "stateChanged";
  public static final String BUFFER_CHANGED_NOTIFICATION = "bufferChanged";
  public static final String CONTENT_TREE_READY_NOTIFICATION = "contentTreeReady";
  public static final String AUTHORIZATION_READY_NOTIFICATION = "authorizationReady";
  public static final String ERROR_NOTIFICATION = "error";
  public static final String PLAY_STARTED_NOTIFICATION = "playStarted";
  public static final String PLAY_COMPLETED_NOTIFICATION = "playCompleted";
  public static final String CURRENT_ITEM_CHANGED_NOTIFICATION = "currentItemChanged";
  public static final String AD_STARTED_NOTIFICATION = "adStarted";
  public static final String AD_COMPLETED_NOTIFICATION = "adCompleted";
  public static final String AD_SKIPPED_NOTIFICATION = "adSkipped";
  public static final String AD_ERROR_NOTIFICATION = "adError";

  /**
   * If set to true, this will allow HLS streams regardless of the Android version. WARNING: Ooyala's internal
   * testing has shown that Android 3.x HLS support is unstable. Android 2.x does not support HLS at all. If
   * set to false, HLS streams will only be allowed on Android 4.x and above
   */
  public static boolean enableHLS = false;

  /**
   * If set to true, this will allow Higher Resolution HLS streams regardless of the Android version. WARNING:
   * Ooyala's internal testing has shown that Android 3.x HLS support is unstable. Android 2.x does not
   * support HLS at all. If set to false, HLS streams will only be allowed on Android 4.x and above. Also this
   * will internally make Ooyala's APIs think that the device is iPad and may have undesired results.
   */
  public static boolean enableHighResHLS = false;

  /**
   * If set to true, HLS content will be played using our custom HLS implementation rather than native the Android one.
   * To achieve HLS playback on Android versions before 4, set this to true and also set the enableHLS flag to true.
   * This will have no affect unless the custom playback engine is linked and loaded in addition to the standard Ooyala Android SDK
   */
  public static boolean enableCustomHLSPlayer = false;

  /**
   * For internal use only
   */
  public static enum Environment {
    PRODUCTION, STAGING, LOCAL
  };

  /**
   * For internal use only
   */
  public static void setEnvironment(Environment e) {
    Constants.setEnvironment(e);
  }

  private Handler _handler = new Handler();
  private Video _currentItem = null;
  private ContentItem _rootItem = null;
  private OoyalaException _error = null;
  private ActionAtEnd _actionAtEnd;
  private Player _player = null;
  private Player _adPlayer = null;
  private PlayerAPIClient _playerAPIClient = null;
  private State _state = State.INIT;
  private final List<AdSpot> _playedAds = new ArrayList<AdSpot>();
  private int _lastPlayedTime = 0;
  private LayoutController _layoutController = null;
  private ClosedCaptionsView _closedCaptionsView = null;
  private boolean _streamBasedCC = false;
  private Analytics _analytics = null;
  private String _language = Locale.getDefault().getLanguage();
  private boolean _adsSeekable = false;
  private boolean _seekable = true;
  private boolean _playQueued = false;
  private int _queuedSeekTime;
  private ClosedCaptionsStyle _closedCaptionsStyle = new ClosedCaptionsStyle(Color.WHITE, Color.BLACK,
      Typeface.DEFAULT);
  private final Map<String, Object> _openTasks = new HashMap<String, Object>();
  private CurrentItemChangedCallback _currentItemChangedCallback = null;
  private AuthHeartbeat _authHeartbeat;

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param apiKey Your API Key
   * @param secret Your Secret
   * @param pcode Your Provider Code
   * @param domain Your Embed Domain
   */
  public OoyalaPlayer(String apiKey, String secret, String pcode, String domain) {
    this(new EmbeddedSecureURLGenerator(apiKey, secret), pcode, domain);
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param apiKey Your API Key
   * @param secret Your Secret
   * @param pcode Your Provider Code
   * @param domain Your Embed Domain
   * @param generator An embedTokenGenerator used to sign SAS requests
   */
  public OoyalaPlayer(String apiKey, String secret, String pcode, String domain, EmbedTokenGenerator generator) {
    this(new EmbeddedSecureURLGenerator(apiKey, secret), pcode, domain, generator);
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param lc the LayoutController to use
   * @param apiKey Your API Key
   * @param secret Your Secret
   * @param pcode Your Provider Code
   * @param domain Your Embed Domain
   */
  public OoyalaPlayer(LayoutController lc, String apiKey, String secret, String pcode, String domain) {
    this(lc, new EmbeddedSecureURLGenerator(apiKey, secret), pcode, domain);
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param lc the LayoutController to use
   * @param apiKey Your API Key
   * @param secret Your Secret
   * @param pcode Your Provider Code
   * @param domain Your Embed Domain
   * @param generator An embedTokenGenerator used to sign SAS requests
   */
  public OoyalaPlayer(LayoutController lc, String apiKey, String secret, String pcode, String domain,
      EmbedTokenGenerator generator) {
    this(lc, new EmbeddedSecureURLGenerator(apiKey, secret), pcode, domain, generator);
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param apiKey Your API Key
   * @param signatureGenerator the SignatureGenerator to use
   * @param pcode Your Provider Code
   * @param domain Your Embed Domain
   */
  public OoyalaPlayer(String apiKey, SignatureGenerator signatureGenerator, String pcode, String domain) {
    this(new EmbeddedSecureURLGenerator(apiKey, signatureGenerator), pcode, domain);
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param apiKey Your API Key
   * @param signatureGenerator the SignatureGenerator to use
   * @param pcode Your Provider Code
   * @param domain Your Embed Domain
   * @param generator An embedTokenGenerator used to sign SAS requests
   */
  public OoyalaPlayer(String apiKey, SignatureGenerator signatureGenerator, String pcode, String domain,
      EmbedTokenGenerator generator) {
    this(new EmbeddedSecureURLGenerator(apiKey, signatureGenerator), pcode, domain, generator);
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param lc the LayoutController to use
   * @param apiKey Your API Key
   * @param signatureGenerator the SignatureGenerator to use
   * @param pcode Your Provider Code
   * @param domain Your Embed Domain
   */
  public OoyalaPlayer(LayoutController lc, String apiKey, SignatureGenerator signatureGenerator,
      String pcode, String domain) {
    this(lc, new EmbeddedSecureURLGenerator(apiKey, signatureGenerator), pcode, domain);
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param lc the LayoutController to use
   * @param apiKey Your API Key
   * @param signatureGenerator the SignatureGenerator to use
   * @param pcode Your Provider Code
   * @param domain Your Embed Domain
   * @param generator An embedTokenGenerator used to sign SAS requests
   */
  public OoyalaPlayer(LayoutController lc, String apiKey, SignatureGenerator signatureGenerator,
      String pcode, String domain, EmbedTokenGenerator generator) {
    this(lc, new EmbeddedSecureURLGenerator(apiKey, signatureGenerator), pcode, domain, generator);
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param apiClient the initialized OoyalaAPIClient to use
   */
  public OoyalaPlayer(OoyalaAPIClient apiClient) {
    this(apiClient.getSecureURLGenerator(), apiClient.getPcode(), apiClient.getDomain());
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param apiClient the initialized OoyalaAPIClient to use
   * @param generator An embedTokenGenerator used to sign SAS requests
   */
  public OoyalaPlayer(OoyalaAPIClient apiClient, EmbedTokenGenerator generator) {
    this(apiClient.getSecureURLGenerator(), apiClient.getPcode(), apiClient.getDomain(), generator);
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param lc the LayoutController to use
   * @param apiClient the initialized OoyalaAPIClient to use
   */
  public OoyalaPlayer(LayoutController lc, OoyalaAPIClient apiClient) {
    this(lc, apiClient.getSecureURLGenerator(), apiClient.getPcode(), apiClient.getDomain());
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param lc the LayoutController to use
   * @param apiClient the initialized OoyalaAPIClient to use
   * @param generator An embedTokenGenerator used to sign SAS requests
   */
  public OoyalaPlayer(LayoutController lc, OoyalaAPIClient apiClient, EmbedTokenGenerator generator) {
    this(lc, apiClient.getSecureURLGenerator(), apiClient.getPcode(), apiClient.getDomain(), generator);
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param secureURLGenerator the SecureURLGenerator to use
   * @param pcode Your Provider Code
   * @param domain Your Embed Domain
   */
  public OoyalaPlayer(SecureURLGenerator secureURLGenerator, String pcode, String domain) {
    _playerAPIClient = new PlayerAPIClient(new OoyalaAPIHelper(secureURLGenerator), pcode, domain, null);
    _actionAtEnd = ActionAtEnd.CONTINUE;
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param secureURLGenerator the SecureURLGenerator to use
   * @param pcode Your Provider Code
   * @param domain Your Embed Domain
   * @param generator An embedTokenGenerator used to sign SAS requests
   */
  public OoyalaPlayer(SecureURLGenerator secureURLGenerator, String pcode, String domain,
      EmbedTokenGenerator generator) {
    _playerAPIClient = new PlayerAPIClient(new OoyalaAPIHelper(secureURLGenerator), pcode, domain, generator);
    _actionAtEnd = ActionAtEnd.CONTINUE;
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param lc the LayoutController to use
   * @param secureURLGenerator the SecureURLGenerator to use
   * @param pcode Your Provider Code
   * @param domain Your Embed Domain
   */
  public OoyalaPlayer(LayoutController lc, SecureURLGenerator secureURLGenerator, String pcode, String domain) {
    this(secureURLGenerator, pcode, domain);
    setLayoutController(lc);
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param lc the LayoutController to use
   * @param secureURLGenerator the SecureURLGenerator to use
   * @param pcode Your Provider Code
   * @param domain Your Embed Domain
   * @param generator An embedTokenGenerator used to sign SAS requests
   */
  public OoyalaPlayer(LayoutController lc, SecureURLGenerator secureURLGenerator, String pcode,
      String domain, EmbedTokenGenerator generator) {
    this(secureURLGenerator, pcode, domain, generator);
    setLayoutController(lc);
  }

  public void setLoki()
  {
    _playerAPIClient.setLoki();
  }
  /**
   * Set the layout controller from which the OoyalaPlayer should fetch the layout to display to.
   * @param lc the LayoutController to use.
   */
  public void setLayoutController(LayoutController lc) {
    _layoutController = lc;
    if (getLayout() == null) { return; }
    if (_adPlayer != null) {
      _adPlayer.setParent(this);
    }
    if (_player != null) {
      _player.setParent(this);
    }
    /*
     * NOTE(jigish): we have to do this here because we need the context from the layout. Theoretically all of
     * our customers should actually call setLayout right after initializing the player so this is ok.
     */
    _analytics = new Analytics(getLayout().getContext(), _playerAPIClient);
    _playerAPIClient.setContext(getLayout().getContext());
    OoyalaAPIHelper.context = getLayout().getContext();
  }

  /**
   * Get the current OoyalaPlayerLayout
   * @return the current OoyalaPlayerLayout
   */
  public FrameLayout getLayout() {
    return _layoutController.getLayout();
  }

  /**
   * Reinitializes the player with a new embed code. If embedCode is null, this method has no effect and just
   * returns false.
   * @param embedCode
   * @return true if the embed code was successfully set, false if not.
   */
  public boolean setEmbedCode(String embedCode) {
    return setEmbedCodeWithAdSetCode(embedCode, null);
  }

  /**
   * Reinitializes the player with a new set of embed codes. If embedCodes is null, this method has no effect
   * and just returns false.
   * @param embedCodes
   * @return true if the embed codes were successfully set, false if not.
   */
  public boolean setEmbedCodes(List<String> embedCodes) {
    return setEmbedCodesWithAdSetCode(embedCodes, null);
  }

  /**
   * Reinitializes the player with a new embed code. If embedCode is null, this method has no effect and just
   * returns false. An ad set can be dynamically associated using the adSetCode param.
   * @param embedCode
   * @param adSetCode
   * @return true if the embed code was successfully set, false if not.
   */
  public boolean setEmbedCodeWithAdSetCode(String embedCode, String adSetCode) {
    if (embedCode == null) { return false; }
    List<String> embeds = new ArrayList<String>();
    embeds.add(embedCode);
    return setEmbedCodesWithAdSetCode(embeds, adSetCode);
  }

  /**
   * Reinitializes the player with a new set of embed codes. If embedCodes is null, this method has no effect
   * and just returns false. An ad set can be dynamically associated using the adSetCode param.
   * @param embedCodes
   * @param adSetCode
   * @return true if the embed codes were successfully set, false if not.
   */
  public boolean setEmbedCodesWithAdSetCode(List<String> embedCodes, String adSetCode) {
    if (embedCodes == null || embedCodes.isEmpty()) { return false; }
    cancelOpenTasks();
    setState(State.LOADING);
    _playQueued = false;
    _queuedSeekTime = 0;
    cleanupPlayers();
    final String taskKey = "setEmbedCodes" + System.currentTimeMillis();
    taskStarted(taskKey, _playerAPIClient.contentTreeWithAdSet(embedCodes, adSetCode, new ContentTreeCallback() {
      @Override
      public void callback(ContentItem item, OoyalaException error) {
        taskCompleted(taskKey);
        if (error != null) {
          _error = error;
          Log.d(this.getClass().getName(), "Exception in setEmbedCodes!", error);
          setState(State.ERROR);
          sendNotification(ERROR_NOTIFICATION);
          return;
        }
        reinitialize(item);
      }
    }));
    return true;
  }

  /**
   * Reinitializes the player with a new external ID. If externalId is null, this method has no effect and
   * just returns false.
   * @param externalId
   * @return true if the external ID was successfully set, false if not.
   */
  public boolean setExternalId(String externalId) {
    if (externalId == null) { return false; }
    List<String> ids = new ArrayList<String>();
    ids.add(externalId);
    return setExternalIds(ids);
  }

  /**
   * Reinitializes the player with a new set of external IDs. If externalIds is null, this method has no
   * effect and just returns false.
   * @param externalIds
   * @return true if the external IDs were successfully set, false if not.
   */
  public boolean setExternalIds(List<String> externalIds) {
    if (externalIds == null || externalIds.isEmpty()) { return false; }
    cancelOpenTasks();
    setState(State.LOADING);
    cleanupPlayers();
    final String taskKey = "setExternalIds" + System.currentTimeMillis();
    taskStarted(taskKey, _playerAPIClient.contentTreeByExternalIds(externalIds, new ContentTreeCallback() {
      @Override
      public void callback(ContentItem item, OoyalaException error) {
        taskCompleted(taskKey);
        if (error != null) {
          _error = error;
          Log.d(this.getClass().getName(), "Exception in setExternalIds!", error);
          setState(State.ERROR);
          sendNotification(ERROR_NOTIFICATION);
          return;
        }
        reinitialize(item);
      }
    }));
    return true;
  }

  /**
   * Reinitializes the player with the rootItem specified.
   * @param rootItem the ContentItem to reinitialize the player with
   * @return true if the change was successful, false if not
   */
  public boolean setRootItem(ContentItem rootItem) {
    cancelOpenTasks();
    setState(State.LOADING);
    cleanupPlayers();
    return reinitialize(rootItem);
  }

  /**
   * Set the current video in a channel if the video is present.
   * @param embedCode
   * @return true if the change was successful, false if not
   */
  public boolean changeCurrentItem(String embedCode) {
    return changeCurrentItem(_rootItem.videoFromEmbedCode(embedCode, _currentItem));
  }

  /**
   * Set the current video in a channel if the video is present.
   * @param video
   * @return true if the change was successful, false if not
   */
  public boolean changeCurrentItem(Video video) {
    if (video == null) {
      cleanupPlayers();
      return false;
    }
    setState(State.LOADING);
    cleanupPlayers();
    _playedAds.clear();
    _lastPlayedTime = 0;
    _currentItem = video;
    if (_currentItemChangedCallback != null) {
      _currentItemChangedCallback.callback(_currentItem);
    }
    sendNotification(CURRENT_ITEM_CHANGED_NOTIFICATION);
    if (_currentItem.getAuthCode() == AuthCode.NOT_REQUESTED) {
      // Async authorize;
      cancelOpenTasks();
      final String taskKey = "changeCurrentItem" + System.currentTimeMillis();
      taskStarted(taskKey, _playerAPIClient.authorize(_currentItem, new AuthorizeCallback() {
        @Override
        public void callback(boolean result, OoyalaException error) {
          taskCompleted(taskKey);
          if (error != null) {
            _error = error;
            Log.d(this.getClass().getName(), "Exception in changeCurrentVideo!", error);
            setState(State.ERROR);
            sendNotification(ERROR_NOTIFICATION);
            return;
          }
          changeCurrentItemAfterAuth();
        }
      }));
      return true;
    }

    return changeCurrentItemAfterAuth();
  }

/*
  private String _playerType = "ViusalOn";
  public void playerType(String playerType) {
    _playerType = playerType;
  }

  public String playerType() {
    return _playerType;
  }
*/

  private String _url;
  public void changeHardCodedUrl(String url) {
    _url = url;
  }
  /**
   * This is a helper function ONLY to be used with changeCurrentItem.
   * @return
   */
  private boolean changeCurrentItemAfterAuth() {
    sendNotification(AUTHORIZATION_READY_NOTIFICATION);
    if (!_currentItem.isAuthorized()) {
      this._error = new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED);
      return false;
    }

    if (_currentItem.isReauthRequired()) {
      if (_authHeartbeat == null) {
        _authHeartbeat = new AuthHeartbeat(_playerAPIClient);
        _authHeartbeat.setAuthHeartbeatErrorListener(this);
      }
      _authHeartbeat.start();
    }

    cancelOpenTasks();
    final String taskKey = "changeCurrentItemAfterAuth" + System.currentTimeMillis();
    taskStarted(taskKey, _currentItem.fetchPlaybackInfo(new FetchPlaybackInfoCallback() {
      @Override
      public void callback(boolean result) {
        taskCompleted(taskKey);
        if (!result) {
          _error = new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
          setState(State.ERROR);
          return;
        }
        if (!changeCurrentItemAfterFetch()) {
          _error = new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
          setState(State.ERROR);
        }
      }
    }));
    return true;
  }

  /**
   * This is a helper function ONLY to be used with changeCurrentItem (in changeCurrentItemAfterAuth).
   * @return
   */
  private boolean changeCurrentItemAfterFetch() {
    Stream s = _currentItem.getStream();
    if (s == null) { return false; }
    if (s.getDeliveryType().equals(Constants.DELIVERY_TYPE_WV_WVM)
        || s.getDeliveryType().equals(Constants.DELIVERY_TYPE_WV_HLS)) {
      _player = new WidevineOsPlayer();
      initializePlayer(_player, new WidevineParams(s.decodedURL().toString(), getEmbedCode(),
          getPlayerAPIClient().getPcode(), s.getWidevineServerPath()));
    } else if (s.getDeliveryType().equals(Constants.DELIVERY_TYPE_WV_MP4)) {
      _player = new WidevineLibPlayer();
      initializePlayer(_player, new WidevineParams(s.decodedURL().toString(), getEmbedCode(),
          getPlayerAPIClient().getPcode(), s.getWidevineServerPath()));
    } else if (enableCustomHLSPlayer
        && (s.getDeliveryType().equals(Constants.DELIVERY_TYPE_HLS)
            || s.getDeliveryType().equals(Constants.DELIVERY_TYPE_REMOTE_ASSET))) {
      _player = new VisualOnMoviePlayer();
      initializePlayer(_player, _url != null ? _url : s.decodedURL().toString());
    } else {
      _player = new MoviePlayer();
      initializePlayer(_player, _url != null ? _url : s.decodedURL().toString());
    }

    // _player = new WidevineOsPlayer();
    // initializePlayer(_player, new
    // WidevineParams("http://widevine-test.s3.amazonaws.com/Bloomberg_1200_encrypted.mp4", getEmbedCode(),
    // getPlayerAPIClient().getPcode()));
    // initializePlayer(_player, new WidevineParams("https://dl.dropbox.com/u/39391582/expendable.wvm",
    // getEmbedCode(), getPlayerAPIClient().getPcode()));

    if (_player == null || _player.getError() != null) { return false; }
    _player.setSeekable(_seekable);

    addClosedCaptionsView();

    _analytics.initializeVideo(_currentItem.getEmbedCode(), _currentItem.getDuration());
    _analytics.reportPlayerLoad();

    dequeuePlay();
    return true;
  }

  private boolean reinitialize(ContentItem tree) {
    if (tree == null) {
      _rootItem = null;
      _currentItem = null;
      return false;
    }
    _rootItem = tree;
    _currentItem = tree.firstVideo();
    sendNotification(CONTENT_TREE_READY_NOTIFICATION);

    // Async Authorize
    cancelOpenTasks();
    final String taskKey = "reinitialize" + System.currentTimeMillis();
    taskStarted(taskKey, _playerAPIClient.authorize(tree, new AuthorizeCallback() {
      @Override
      public void callback(boolean result, OoyalaException error) {
        taskCompleted(taskKey);
        if (error != null) {
          _error = error;
          Log.d(this.getClass().getName(), "Exception in reinitialize!", error);
          setState(State.ERROR);
          sendNotification(ERROR_NOTIFICATION);
          return;
        }
        changeCurrentItem(_rootItem.firstVideo());
      }
    }));
    return true;
  }

  private Player initializePlayer(Player p, Object param) {
    p.addObserver(this);
    p.init(this, param);
    return p;
  }

  private void cleanupPlayers() {
    if (_authHeartbeat != null) {
      _authHeartbeat.stop();
    }
    cleanupPlayer(_adPlayer);
    _adPlayer = null;
    cleanupPlayer(_player);
    _player = null;
    removeClosedCaptionsView();
  }

  private void cleanupPlayer(Player p) {
    if (p != null) {
      p.deleteObserver(this);
      p.destroy();
    }
  }

  /**
   * The current movie.
   * @return movie
   */
  public Video getCurrentItem() {
    return _currentItem;
  }

  /**
   * The embedded item (movie, channel, or channel set).
   * @return movie
   */
  public ContentItem getRootItem() {
    return _rootItem;
  }

  /**
   * Get the current error code, if one exists
   * @return error code
   */
  public OoyalaException getError() {
    return _error;
  }

  /**
   * Get the embedCode for the current player.
   * @return embedCode
   */
  public String getEmbedCode() {
    return _rootItem == null ? null : _rootItem.getEmbedCode();
  }

  /**
   * Get current player state. One of playing, paused, buffering, channel, or error
   * @return state
   */
  public State getState() {
    return _state;
  }

  /**
   * Pause the current video
   */
  public void pause() {
    _playQueued = false;
    if (currentPlayer() != null) {
      currentPlayer().pause();
    }
  }

  /**
   * Play the current video
   */
  public void play() {
    if (currentPlayer() != null) {
      currentPlayer().play();
    } else {
      queuePlay();
    }
  }

  /**
   * Suspend the current video (can be resumed later by calling resume). This differs from pause in that it
   * completely deconstructs the view so the layout can be changed.
   */
  public void suspend() {
    _playQueued = false;
    if (currentPlayer() != null) {
      currentPlayer().suspend();
      removeClosedCaptionsView();
    }
  }

  /**
   * Resume the current video from a suspended state
   */
  public void resume() {
    if (currentPlayer() != null) {
      currentPlayer().resume();
      addClosedCaptionsView();
    }
  }

  /**
   * Returns true if in fullscreen mode, false if not. Fullscreen currently does not work due to limitations
   * in Android.
   * @return fullscreen mode
   */
  public boolean isFullscreen() {
    return _layoutController != null && _layoutController.isFullscreen();
  }

  /**
   * Set fullscreen mode (will only work if fullscreenLayout is set) This will call the setFullscreen method
   * on the associated LayoutController. If you are implementing your own LayoutController here are some
   * things to keep in mind: <li>If the setFullscreen method of your LayoutController creates a new
   * OoyalaPlayerLayout or switches to a different one, you *must* call OoyalaPlayer.suspend() before doing so
   * and call OoyalaPlayer.resume() after doing so. <li>If the setFullscreen method of your LayoutController
   * uses the same OoyalaPlayerLayout, you do not need to do any special handling.
   * @param fullscreen true to switch to fullscreen, false to switch out of fullscreen
   */
  public void setFullscreen(boolean fullscreen) {
    if (isFullscreen() == !fullscreen) { // this is so we don't add/remove cc view if we are not actually
                                         // changing state.
      removeClosedCaptionsView();
      _layoutController.setFullscreen(fullscreen);
      addClosedCaptionsView();
    }
  }

  /**
   * Find where the playhead is with millisecond accuracy
   * @return time in milliseconds
   */
  public int getPlayheadTime() {
    if (currentPlayer() == null) { return -1; }
    return currentPlayer().currentTime();
  }

  /**
   * Synonym for seek.
   * @param timeInMillis in milliseconds
   */
  public void setPlayheadTime(int timeInMillis) {
    seek(timeInMillis);
  }

  /**
   * @return true if the current player is seekable, false if there is no current player or it is not seekable
   */
  public boolean seekable() {
    return currentPlayer() != null && currentPlayer().seekable();
  }

  /**
   * Move the playhead to a new location in seconds with millisecond accuracy
   * @param timeInMillis in milliseconds
   */
  public void seek(int timeInMillis) {
    if (seekable()) {
      currentPlayer().seekToTime(timeInMillis);
      _queuedSeekTime = 0;
    } else {
      _queuedSeekTime = timeInMillis;
    }
  }

  private void addClosedCaptionsView() {
    removeClosedCaptionsView();
    if (_currentItem != null && _currentItem.hasClosedCaptions() || _streamBasedCC) {
      _closedCaptionsView = new ClosedCaptionsView(getLayout().getContext());
      _closedCaptionsView.setStyle(_closedCaptionsStyle);
      getLayout().addView(_closedCaptionsView);
    }
  }

  private void removeClosedCaptionsView() {
    if (_closedCaptionsView != null) {
      getLayout().removeView(_closedCaptionsView);
      _closedCaptionsView = null;
    }
  }

  private boolean playAdsBeforeTime(int time) {
    this._lastPlayedTime = time;
    for (AdSpot ad : _currentItem.getAds()) {
      int adTime = ad.getTime();
      // Align ad times to 10 second (HLS chunk length) boundaries
      if (getCurrentItem().getStream().getDeliveryType().equals(Constants.DELIVERY_TYPE_HLS)) {
        adTime = ((adTime + 5000) / 10000) * 10000;
      }
      if (adTime <= time && !this._playedAds.contains(ad)) {
        _playedAds.add(ad);
        if (playAd(ad)) { return true; }
      }
    }
    return false;
  }

  private boolean playAd(AdSpot ad) {
    if (ad instanceof OoyalaAdSpot) {
      _adPlayer = new OoyalaAdPlayer();
    } else if (ad instanceof VASTAdSpot) {
      _adPlayer = new VASTAdPlayer();
    }

    if (_adPlayer == null) { return false; }

    initializePlayer(_adPlayer, ad);

    if (_adPlayer == null || _adPlayer.getState() == State.ERROR) { return false; }
    _adPlayer.setSeekable(_adsSeekable);

    _player.suspend();
    removeClosedCaptionsView();
    sendNotification(AD_STARTED_NOTIFICATION);
    _adPlayer.play();
    return true;
  }

  private Player currentPlayer() {
    return (_adPlayer != null) ? _adPlayer : _player;
  }

  private boolean fetchMoreChildren(PaginatedItemListener listener) {
    Channel parent = _currentItem.getParent();
    if (parent != null) {
      ChannelSet parentOfParent = parent.getParent();
      if (parent.hasMoreChildren()) {
        return parent.fetchMoreChildren(listener);
      } else if (parentOfParent != null && parentOfParent.hasMoreChildren()) { return parentOfParent
          .fetchMoreChildren(listener); }
    }
    return false;
  }

  /**
   * Change the current video to the previous video in the Channel or ChannelSet. If there is no previous
   * video, this will seek to the beginning of the video.
   * @param what OoyalaPlayerControl.DO_PLAY or OoyalaPlayerControl.DO_PAUSE depending on what to do after the
   *          video is set.
   * @return true if there was a previous video, false if not.
   */
  public boolean previousVideo(int what) {
    if (_currentItem.previousVideo() != null) {
      changeCurrentItem(_currentItem.previousVideo());
      if (what == DO_PLAY) {
        play();
      } else if (what == DO_PAUSE) {
        pause();
      }
      return true;
    }
    seek(0);
    return false;
  }

  /**
   * Change the current video to the next video in the Channel or ChannelSet. If there is no next video,
   * nothing will happen. Note that this will trigger a fetch of additional children if the Channel or
   * ChannelSet is paginated. If so, it may take some time before the video is actually set.
   * @param what OoyalaPlayerControl.DO_PLAY or OoyalaPlayerControl.DO_PAUSE depending on what to do after the
   *          video is set.
   * @return true if there was a next video, false if not.
   */
  public boolean nextVideo(int what) {
    // This is required because android enjoys making things difficult. talk to jigish if you got issues.
    if (_currentItem.nextVideo() != null) {
      changeCurrentItem(_currentItem.nextVideo());
      if (what == DO_PLAY) {
        play();
      } else if (what == DO_PAUSE) {
        pause();
      }
      return true;
    } else if (what == DO_PLAY && fetchMoreChildren(new PaginatedItemListener() {
      @Override
      public void onItemsFetched(int firstIndex, int count, OoyalaException error) {
        _handler.post(new Runnable() {
          @Override
          public void run() {
            changeCurrentItem(_currentItem.nextVideo());
            play();
          }
        });
      }
    })) {
      return true;
    } else if (what == DO_PAUSE && fetchMoreChildren(new PaginatedItemListener() {
      @Override
      public void onItemsFetched(int firstIndex, int count, OoyalaException error) {
        _handler.post(new Runnable() {
          @Override
          public void run() {
            changeCurrentItem(_currentItem.nextVideo());
            pause();
          }
        });
      }
    })) { return true; }
    return false;
  }

  private void reset() {
    removeClosedCaptionsView();
    _playQueued = false;
    _player.reset();
    addClosedCaptionsView();
  }

  private void onComplete() {
    switch (_actionAtEnd) {
      case CONTINUE:
        if (nextVideo(DO_PLAY)) {} else {
          reset();
          sendNotification(PLAY_COMPLETED_NOTIFICATION);
        }
        break;
      case PAUSE:
        if (nextVideo(DO_PAUSE)) {} else {
          reset();
          sendNotification(PLAY_COMPLETED_NOTIFICATION);
        }
        break;
      case STOP:
        cleanupPlayers();
        setState(State.COMPLETED);
        sendNotification(PLAY_COMPLETED_NOTIFICATION);
        break;
      case RESET:
        reset();
        sendNotification(PLAY_COMPLETED_NOTIFICATION);
        break;
    }
  }

  @Override
  /**
   * For Internal Use Only.
   */
  public void update(Observable arg0, Object arg1) {
    Player player = (Player) arg0;
    String notification = arg1.toString();

    if (currentPlayer() != null && currentPlayer() != player) { return; }

    if (notification.equals(TIME_CHANGED_NOTIFICATION)) {
      sendNotification(TIME_CHANGED_NOTIFICATION);

      if (player == _player) {
        // send analytics ping
        _analytics.reportPlayheadUpdate((_player.currentTime()) / 1000);

        // play ads
        _lastPlayedTime = _player.currentTime();
        playAdsBeforeTime(this._lastPlayedTime);

        // closed captions
        displayCurrentClosedCaption();
      }
    } else if (notification.equals(STATE_CHANGED_NOTIFICATION)) {
      switch (player.getState()) {
        case COMPLETED:
          if (player == _player) {
            if (!playAdsBeforeTime(Integer.MAX_VALUE - 1)) {
              onComplete();
            }
          } else {
            cleanupPlayer(_adPlayer);
            _adPlayer = null;
            sendNotification(AD_COMPLETED_NOTIFICATION);
            if (!playAdsBeforeTime(this._lastPlayedTime)) {
              if (_player.getState() == State.COMPLETED) {
                onComplete();
              } else {
                _player.resume();
                addClosedCaptionsView();
              }
            }
          }
          break;
        case ERROR:
          if (player == _player) {
            cleanupPlayers();
            _error = new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_PLAYBACK_FAILED,
                player.getError());
            setState(State.ERROR);
            sendNotification(ERROR_NOTIFICATION);
          } else {
            sendNotification(AD_ERROR_NOTIFICATION);
            cleanupPlayer(_adPlayer);
            _adPlayer = null;
            sendNotification(AD_COMPLETED_NOTIFICATION);
            if (!playAdsBeforeTime(this._lastPlayedTime)) {
              if (_player.getState() == State.COMPLETED) {
                onComplete();
              } else {
                _player.resume();
                addClosedCaptionsView();
              }
            }
          }
          break;
        case PLAYING:
          if (_lastPlayedTime == 0) {
            _analytics.reportPlayStarted();
            playAdsBeforeTime(250);
            sendNotification(PLAY_STARTED_NOTIFICATION);
          }
          setState(State.PLAYING);
          break;
        case READY:
          if (_queuedSeekTime > 0) {
            seek(_queuedSeekTime);
          }
          if (player == _adPlayer) break;
        case INIT:
        case LOADING:
        case PAUSED:
        default:
          setState(player.getState());
      }
    }
  }

  /**
   * Get what the player will do at the end of playback.
   * @return the OoyalaPlayer.OoyalaPlayerActionAtEnd to use
   */
  public ActionAtEnd getActionAtEnd() {
    return _actionAtEnd;
  }

  /**
   * Set what the player should do at the end of playback.
   * @param actionAtEnd
   */
  public void setActionAtEnd(ActionAtEnd actionAtEnd) {
    this._actionAtEnd = actionAtEnd;
  }

  /**
   * @return the OoyalaAPIClient used by this player
   */
  public OoyalaAPIClient getOoyalaAPIClient() {
    return new OoyalaAPIClient(_playerAPIClient);
  }

  private void setState(State state) {
    if (state != _state) {
      this._state = state;
      sendNotification(STATE_CHANGED_NOTIFICATION);
    }
  }

  private void sendNotification(String obj) {
    setChanged();
    notifyObservers(obj);
  }

  /**
   * Set the displayed closed captions language
   * @param language 2 letter country code of the language to display or nil to hide closed captions
   */
  public void setClosedCaptionsLanguage(String language) {
    _language = language;
    if (_closedCaptionsView != null) _closedCaptionsView.setCaption(null);
    displayCurrentClosedCaption();
  }

  /**
   * Get the current closed caption language
   * @return the current closed caption language
   */
  public String getClosedCaptionsLanguage() {
    return _language;
  }

  /**
   * Get the available closed captions languages
   * @return a Set of Strings containing the available closed captions languages
   */
  public Set<String> getAvailableClosedCaptionsLanguages() {
    if (_currentItem == null || _currentItem.getClosedCaptions() == null) { return null; }
    return getCurrentItem().getClosedCaptions().getLanguages();
  }

  /**
   * @return get the bitrate of the current item
   */
  @TargetApi(10)
  public double getBitrate() {
    if (getCurrentItem() == null || getCurrentItem().getStream() == null) { return -1; }
    return getCurrentItem().getStream().getVideoBitrate() * 1000;
  }

  /**
   * @return true if the current state is State.Playing, false otherwise
   */
  public boolean isPlaying() {
    return _state == State.PLAYING;
  }

  /**
   * @return true if currently playing ad, false otherwise
   */
  public boolean isAdPlaying() {
    return currentPlayer() == _adPlayer;
  }

  /**
   * Seek to the given percentage
   * @param percent percent (between 0 and 100) to seek to
   */
  public void seekToPercent(int percent) {
    if (percent < 0 || percent > 100) { return; }
    if (seekable()) {
      seek(percentToMillis(percent));
    }
  }

  /**
   * Get the current item's duration
   * @return the duration in milliseconds
   */
  public int getDuration() {
    if (currentPlayer() == null) { return 0; }
    int playerDuration = currentPlayer().duration();
    if (playerDuration > 0) return playerDuration;
    if (getCurrentItem() == null) return 0;
    return getCurrentItem().getDuration();
  }

  /**
   * Get the current item's buffer percentage
   * @return the buffer percentage (between 0 and 100 inclusive)
   */
  public int getBufferPercentage() {
    if (currentPlayer() == null) { return 0; }
    return currentPlayer().buffer();
  }

  /**
   * Get the current item's playhead time as a percentage
   * @return the playhead time percentage (between 0 and 100 inclusive)
   */
  public int getPlayheadPercentage() {
    if (currentPlayer() == null) { return 0; }
    return millisToPercent(currentPlayer().currentTime());
  }

  /**
   * Set whether ads played by this OoyalaPlayer are seekable (default is false)
   * @param seekable true if seekable, false if not.
   */
  public void setAdsSeekable(boolean seekable) {
    _adsSeekable = seekable;
    if (_adPlayer != null) {
      _adPlayer.setSeekable(_adsSeekable);
    }
  }

  /**
   * Set whether videos played by this OoyalaPlayer are seekable (default is true)
   * @param seekable true if seekable, false if not.
   */
  public void setSeekable(boolean seekable) {
    _seekable = seekable;
    if (_player != null) {
      _player.setSeekable(_seekable);
    }
  }

  /**
   * This will reset the state of all the ads to "unplayed" causing any ad that has already played to play
   * again.
   */
  public void resetAds() {
    _playedAds.clear();
  }

  /**
   * Skip the currently playing ad. Do nothing if no ad is playing
   */
  public void skipAd() {
    if (isShowingAd()) {
      cleanupPlayer(_adPlayer);
      _adPlayer = null;
      sendNotification(AD_SKIPPED_NOTIFICATION);
      if (!playAdsBeforeTime(this._lastPlayedTime)) {
        if (_player.getState() == State.COMPLETED) {
          onComplete();
        } else {
          _player.resume();
          addClosedCaptionsView();
        }
      }
    }
  }

  /**
   * @return true if the OoyalaPlayer is currently showing an ad (in any state). false if not.
   */
  public boolean isShowingAd() {
    return _adPlayer != null;
  }

  private int percentToMillis(int percent) {
    float fMillis = ((percent) / (100f)) * (getDuration());
    return (int) fMillis;
  }

  private int millisToPercent(int millis) {
    float fPercent = (((float) millis) / ((float) getDuration())) * (100f);
    return (int) fPercent;
  }

  private void queuePlay() {
    _playQueued = true;
  }

  private void dequeuePlay() {
    if (_playQueued && currentPlayer() != null) {
      _playQueued = false;
      play();
    }
  }

  private void taskStarted(String key, Object task) {
    if (task != null) _openTasks.put(key, task);
  }

  private void taskCompleted(String key) {
    _openTasks.remove(key);
  }

  private void cancelOpenTasks() {
    for (String key : _openTasks.keySet()) {
      this._playerAPIClient.cancel(_openTasks.get(key));
    }
    _openTasks.clear();
  }

  /**
   * @return the current ClosedCaptionsStyle
   */
  public ClosedCaptionsStyle getClosedCaptionsStyle() {
    return _closedCaptionsStyle;
  }

  /**
   * Set the ClosedCaptionsStyle
   * @param closedCaptionsStyle the ClosedCaptionsStyle to use
   */
  public void setClosedCaptionsStyle(ClosedCaptionsStyle closedCaptionsStyle) {
    this._closedCaptionsStyle = closedCaptionsStyle;
    if (_closedCaptionsView != null) {
      _closedCaptionsView.setStyle(closedCaptionsStyle);
      _closedCaptionsView.setCaption(null);
    }
    displayCurrentClosedCaption();
  }

  private void displayCurrentClosedCaption() {
    if (_closedCaptionsView == null) return;
    if (_streamBasedCC) return;

    if (_language != null && _currentItem.hasClosedCaptions()) {
      double currT = (currentPlayer().currentTime()) / 1000d;
      if (_closedCaptionsView.getCaption() == null || currT > _closedCaptionsView.getCaption().getEnd()
          || currT < _closedCaptionsView.getCaption().getBegin()) {
        Caption caption = _currentItem.getClosedCaptions().getCaption(_language, currT);
        if (caption != null && caption.getBegin() <= currT && caption.getEnd() >= currT) {
          _closedCaptionsView.setCaption(caption);
        } else {
          _closedCaptionsView.setCaption(null);
        }
      }
    } else {
      _closedCaptionsView.setCaption(null);
    }
  }

  public void displayClosedCaptionText(String text) {
    _streamBasedCC = true;
    if (_closedCaptionsView == null) {
      addClosedCaptionsView();
    }
    _closedCaptionsView.setCaptionText(text);
  }

  PlayerAPIClient getPlayerAPIClient() {
    return this._playerAPIClient;
  }

  /**
   * Set the callback that will be called every time the current item changes
   * @param callback the CurrentItemChangedCallback that should be called every time the current item changes
   */
  public void setCurrentItemChangedCallback(CurrentItemChangedCallback callback) {
    _currentItemChangedCallback = callback;
  }

  public void setCustomAnalyticsTags(List<String> tags) {
    if (_analytics != null) {
      _analytics.setTags(tags);
    }
  }

  @Override
  public void onAuthHeartbeatError(OoyalaException e) {
      cleanupPlayers();
      _error = e;
      setState(State.ERROR);
      sendNotification(ERROR_NOTIFICATION);
  }
}
