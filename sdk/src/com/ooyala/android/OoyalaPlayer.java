package com.ooyala.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.json.JSONObject;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.FrameLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.ooyala.android.AdvertisingIdUtils.IAdvertisingIdListener;
import com.ooyala.android.AuthHeartbeat.OnAuthHeartbeatErrorListener;
import com.ooyala.android.ClosedCaptionsStyle.OOClosedCaptionPresentation;
import com.ooyala.android.Environment.EnvironmentType;
import com.ooyala.android.OoyalaException.OoyalaErrorCode;
import com.ooyala.android.ads.vast.VASTAdPlayer;
import com.ooyala.android.ads.vast.VASTAdSpot;
import com.ooyala.android.item.OoyalaManagedAdSpot;
import com.ooyala.android.item.AuthorizableItem.AuthCode;
import com.ooyala.android.item.Caption;
import com.ooyala.android.item.Channel;
import com.ooyala.android.item.ChannelSet;
import com.ooyala.android.item.ContentItem;
import com.ooyala.android.item.Stream;
import com.ooyala.android.item.Video;
import com.ooyala.android.player.AdMoviePlayer;
import com.ooyala.android.player.MoviePlayer;
import com.ooyala.android.player.Player;
import com.ooyala.android.player.PlayerInterface;
import com.ooyala.android.player.StreamPlayer;
import com.ooyala.android.player.WidevineOsPlayer;
import com.ooyala.android.plugin.AdPluginInterface;
import com.ooyala.android.ui.AbstractOoyalaPlayerLayoutController;
import com.ooyala.android.ui.LayoutController;

public class OoyalaPlayer extends Observable implements Observer,
    OnAuthHeartbeatErrorListener, AdPluginManagerInterface {
  /**
   * NOTE[jigish] do NOT change the name or location of this variable without
   * changing pub_release.sh
   */
  static final String SDK_VERSION = "2.5.0_RC5";
  static final String API_VERSION = "1";

  public static enum ActionAtEnd {
    CONTINUE, PAUSE, STOP, RESET
  };

  public static enum State {
    INIT, LOADING, READY, PLAYING, PAUSED, COMPLETED, SUSPENDED, ERROR
  };

  public static enum SeekStyle {
    NONE, BASIC, ENHANCED
  };

  /**
   * Used by previousVideo and nextVideo. When passed to them, it will cause the
   * video to be played after it is set.
   */
  public static final int DO_PLAY = 0;
  /**
   * Used by previousVideo and nextVideo. When passed to them, it will cause the
   * video to be paused after it is set.
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
  public static final String SEEK_COMPLETED_NOTIFICATION = "seekCompleted";
  public static final String CURRENT_ITEM_CHANGED_NOTIFICATION = "currentItemChanged";
  public static final String AD_STARTED_NOTIFICATION = "adStarted";
  public static final String AD_COMPLETED_NOTIFICATION = "adCompleted";
  public static final String AD_SKIPPED_NOTIFICATION = "adSkipped";
  public static final String AD_ERROR_NOTIFICATION = "adError";
  public static final String METADATA_READY_NOTIFICATION = "metadataReady";

  static final String WIDEVINE_LIB_PLAYER = "com.ooyala.android.WidevineLibPlayer";

  public static final String LIVE_CLOSED_CAPIONS_LANGUAGE = "Closed Captions";
  /**
   * If set to true, this will allow HLS streams regardless of the Android
   * version. WARNING: Ooyala's internal testing has shown that Android 3.x HLS
   * support is unstable. Android 2.x does not support HLS at all. If set to
   * false, HLS streams will only be allowed on Android 4.x and above
   */
  public static boolean enableHLS = false;

  /**
   * If set to true, this will allow Higher Resolution HLS streams regardless of
   * the Android version. WARNING: Ooyala's internal testing has shown that
   * Android 3.x HLS support is unstable. Android 2.x does not support HLS at
   * all. If set to false, HLS streams will only be allowed on Android 4.x and
   * above. Also this will internally make Ooyala's APIs think that the device
   * is iPad and may have undesired results.
   */
  public static boolean enableHighResHLS = false;

  /**
   * If set to true, HLS content will be played using our custom HLS
   * implementation rather than native the Android one. To achieve HLS playback
   * on Android versions before 4, set this to true and also set the enableHLS
   * flag to true. This will have no affect unless the custom playback engine is
   * linked and loaded in addition to the standard Ooyala Android SDK
   */
  public static boolean enableCustomHLSPlayer = false;

  /**
   * If set to true, DRM enabled players will perform DRM requests in a debug environment if available
   */
  public static boolean enableDebugDRMPlayback = false;


  public static void setEnvironment(EnvironmentType e) {
    Environment.setEnvironment(e);
  }

  private static final String TAG = OoyalaPlayer.class.getName();

  private final Handler _handler = new Handler();
  private Video _currentItem = null;
  private ContentItem _rootItem = null;
  private final JSONObject _metadata = null;
  private OoyalaException _error = null;
  private ActionAtEnd _actionAtEnd;
  private PlayerAPIClient _playerAPIClient = null;
  private State _state = State.INIT;
  private LayoutController _layoutController = null;
  private ClosedCaptionsView _closedCaptionsView = null;
  private boolean _streamBasedCC = false;
  private Analytics _analytics = null;
  private String _language = null;
  private boolean _seekable = true;
  private boolean _playQueued = false;
  private int _queuedSeekTime;
  private String _lastAccountId = null;
  private ClosedCaptionsStyle _closedCaptionsStyle;
  private final Map<String, Object> _openTasks = new HashMap<String, Object>();
  private CurrentItemChangedCallback _currentItemChangedCallback = null;
  private AuthHeartbeat _authHeartbeat;
  private long _suspendTime = System.currentTimeMillis();
  private StreamPlayer _basePlayer = null;
  private final Map<Class<? extends OoyalaManagedAdSpot>, Class<? extends AdMoviePlayer>> _adPlayers;
  private String _customDRMData = null;
  private AdPluginManager _adManager = null;
  private MoviePlayer _player = null;
  private OoyalaManagedAdsPlugin _managedAdsPlugin = null;
  private BroadcastReceiver _receiver = null;

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * 
   * @param ooyalaAPIClient
   *          an initialized OoyalaApiClient
   * @param an
   *          android context
   */
  public OoyalaPlayer(OoyalaAPIClient apiClient, Context context) {
    this(apiClient.getPcode(), apiClient.getDomain(), null, context);
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * 
   * @param pcode
   *          Your Provider Code
   * @param domain
   *          Your Embed Domain
   * @param an
   *          android context
   */
  public OoyalaPlayer(String pcode, PlayerDomain domain, Context context) {
    this(pcode, domain, null, context);
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * 
   * @param pcode
   *          Your Provider Code
   * @param domain
   *          Your Embed Domain
   * @param generator
   *          An embedTokenGenerator used to sign SAS requests
   * @param an
   *          android context
   */
  public OoyalaPlayer(String pcode, PlayerDomain domain,
      EmbedTokenGenerator generator, Context context) {
    _playerAPIClient = new PlayerAPIClient(pcode, domain, generator);
    _actionAtEnd = ActionAtEnd.CONTINUE;

    // Initialize Ad Players
    _adPlayers = new HashMap<Class<? extends OoyalaManagedAdSpot>, Class<? extends AdMoviePlayer>>();
    registerAdPlayer(OoyalaAdSpot.class, OoyalaAdPlayer.class);
    registerAdPlayer(VASTAdSpot.class, VASTAdPlayer.class);
    if (context != null) {
      setContext(context);
      // use broadcastreceiver for the message bus
      _receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          String action = intent.getAction();
          Integer sender = intent.getIntExtra("sender", 0);
          PlayerInterface p = currentPlayer();
          if (p == null || sender != currentPlayer().hashCode()) {
            return;
          }
          processAdNotifications(action);
        }
      };
      registerReceiver(_receiver);
    }

    // Initialize third party plugin managers
    _adManager = new AdPluginManager(this);
    _managedAdsPlugin = new OoyalaManagedAdsPlugin(this);
    _adManager.registerPlugin(_managedAdsPlugin);

    DebugMode.logI(this.getClass().getName(),
        "Ooyala SDK Version: " + OoyalaPlayer.getVersion());
  }

  /**
   * Set the layout controller from which the OoyalaPlayer should fetch the
   * layout to display to
   *
   * @param layoutController
   *          the layoutController to use.
   */
  public void setLayoutController(LayoutController layoutController) {
    _layoutController = layoutController;
    _playerAPIClient.setContext(getLayout().getContext());
  }

  public void setHook() {
    _playerAPIClient.setHook();
  }

  /**
   * Get the current OoyalaPlayerLayout
   *
   * @return the current OoyalaPlayerLayout
   */
  public FrameLayout getLayout() {
    return _layoutController.getLayout();
  }

  /**
   * Start obtaining the Advertising Id, which internally is then used in e.g. VAST Ad URL 'device id' macro expansion.
   * This method will: 1st check that the Google Play Services are available, which may fail and return a non-SUCCESS code.
   * If they are available (code SUCCESS) then: 2nd an attempt will be made to load the Advertising Id from those Google Play Services.
   * If the 2nd step fails an OoyalaException will be thrown, wrapping the original exception.
   * Callers of this method should:
   * 1) update AndroidManifest.xml to include meta-data tag per Google Play Services docs.
   * 2) obtain and pass in a valid Android Context;
   * 3) check the return code and decide if the App should prompt the user to install Google Play Services.
   * 4) handle subsequent asynchronous onAdvertisingIdSuccess() and onAdvertisingIdError() callbacks: due to the asynchronous nature of the Google Play Services call used,
   * there can be a long delay either before the Advertising Id is successfully obtained, or a long delay before a failure happens.
   * An invocation of onAdvertisingIdSuccess() means the Ooyala SDK now has an advertising id for using with e.g. 'device id' macros. Nothing further must be done by the App.
   * An invocation of onAdvertisingIdError() means the App might try this whole process again since fetching failed.
   * These callbacks will be invoked on the main thread.
   * @param context must be non-null.
   * @param listener must be non-null.
   * @see http://developer.android.com/google/play-services/setup.html
   * @see http://developer.android.com/reference/com/google/android/gms/common/GooglePlayServicesUtil.html#isGooglePlayServicesAvailable(android.content.Context)
   * @see com.ooyala.android.OoyalaException#getCode()
   * @return status code, can be one of following in ConnectionResult: SUCCESS, SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRED, SERVICE_DISABLED, SERVICE_INVALID, DATE_INVALID.
   */
  public int beginFetchingAdvertisingId(final Context context,
      final IAdvertisingIdListener listener) {
    final int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable( context );
    if( status == ConnectionResult.SUCCESS ) {
      final IAdvertisingIdListener listenerWrapper = new IAdvertisingIdListener() {
        @Override
        public void onAdvertisingIdSuccess(String advertisingId) {
          AdvertisingIdUtils.setAdvertisingId(advertisingId);
          listener.onAdvertisingIdSuccess(advertisingId);
        }
        @Override
        public void onAdvertisingIdError(OoyalaException oe) {
          listener.onAdvertisingIdError(oe);
        }
      };
      AdvertisingIdUtils.getAndSetAdvertisingId( context, listenerWrapper );
    }
    return status;
  }

  /**
   * Reinitializes the player with a new embed code. If embedCode is null, this
   * method has no effect and just returns false.
   *
   * @param embedCode
   * @return true if the embed code was successfully set, false if not.
   */
  public boolean setEmbedCode(String embedCode) {
    return setEmbedCodeWithAdSetCode(embedCode, null);
  }

  /**
   * Reinitializes the player with a new set of embed codes. If embedCodes is
   * null, this method has no effect and just returns false.
   *
   * @param embedCodes
   * @return true if the embed codes were successfully set, false if not.
   */
  public boolean setEmbedCodes(List<String> embedCodes) {
    return setEmbedCodesWithAdSetCode(embedCodes, null);
  }

  /**
   * Reinitializes the player with a new embed code. If embedCode is null, this
   * method has no effect and just returns false. An ad set can be dynamically
   * associated using the adSetCode param.
   *
   * @param embedCode
   * @param adSetCode
   * @return true if the embed code was successfully set, false if not.
   */
  public boolean setEmbedCodeWithAdSetCode(String embedCode, String adSetCode) {
    if (embedCode == null) {
      return false;
    }
    List<String> embeds = new ArrayList<String>();
    embeds.add(embedCode);
    return setEmbedCodesWithAdSetCode(embeds, adSetCode);
  }

  /**
   * Reinitializes the player with a new set of embed codes. If embedCodes is
   * null, this method has no effect and just returns false. An ad set can be
   * dynamically associated using the adSetCode param.
   *
   * @param embedCodes
   * @param adSetCode
   * @return true if the embed codes were successfully set, false if not.
   */
  public boolean setEmbedCodesWithAdSetCode(List<String> embedCodes,
      String adSetCode) {
    if (embedCodes == null || embedCodes.isEmpty()) {
      return false;
    }
    cancelOpenTasks();
    setState(State.LOADING);
    _playQueued = false;
    _queuedSeekTime = 0;
    cleanupPlayers();

    // request content tree
    final String taskKey = "setEmbedCodes" + System.currentTimeMillis();
    taskStarted(taskKey, _playerAPIClient.contentTreeWithAdSet(embedCodes,
        adSetCode, new ContentTreeCallback() {
          @Override
          public void callback(ContentItem item, OoyalaException error) {
            taskCompleted(taskKey);
            if (error != null) {
              _error = error;
              DebugMode.logD(TAG, "Exception in setEmbedCodes!", error);
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
   * Reinitializes the player with a new external ID. If externalId is null,
   * this method has no effect and just returns false.
   *
   * @param externalId
   * @return true if the external ID was successfully set, false if not.
   */
  public boolean setExternalId(String externalId) {
    if (externalId == null) {
      return false;
    }
    List<String> ids = new ArrayList<String>();
    ids.add(externalId);
    return setExternalIds(ids);
  }

  /**
   * Reinitializes the player with a new set of external IDs. If externalIds is
   * null, this method has no effect and just returns false.
   *
   * @param externalIds
   * @return true if the external IDs were successfully set, false if not.
   */
  public boolean setExternalIds(List<String> externalIds) {
    if (externalIds == null || externalIds.isEmpty()) {
      return false;
    }
    cancelOpenTasks();
    setState(State.LOADING);
    cleanupPlayers();
    final String taskKey = "setExternalIds" + System.currentTimeMillis();
    taskStarted(taskKey, _playerAPIClient.contentTreeByExternalIds(externalIds,
        new ContentTreeCallback() {
          @Override
          public void callback(ContentItem item, OoyalaException error) {
            taskCompleted(taskKey);
            if (error != null) {
              _error = error;
              DebugMode.logD(TAG, "Exception in setExternalIds!", error);
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
   *
   * @param rootItem
   *          the ContentItem to reinitialize the player with
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
   *
   * @param embedCode
   * @return true if the change was successful, false if not
   */
  public boolean changeCurrentItem(String embedCode) {
    return changeCurrentItem(_rootItem.videoFromEmbedCode(embedCode,
        _currentItem));
  }

  /**
   * Set the current video in a channel if the video is present.
   *
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

    _currentItem = video;
    if (_currentItemChangedCallback != null) {
      _currentItemChangedCallback.callback(_currentItem);
    }
    cancelOpenTasks();

    // request metadata
    final String metadataTaskKey = "getMetadata" + System.currentTimeMillis();
    taskStarted(metadataTaskKey,
        _playerAPIClient.metadata(_rootItem, new MetadataFetchedCallback() {
          @Override
          public void callback(boolean result, OoyalaException error) {
            taskCompleted(metadataTaskKey);
            if (error != null) {
              _error = error;
              DebugMode.logD(TAG, "Exception fetching metadata from setEmbedCodes!",
                  error);
              setState(State.ERROR);
              sendNotification(ERROR_NOTIFICATION);
            } else {
              sendNotification(METADATA_READY_NOTIFICATION);
              changeCurrentItemAfterAuth();
            }
          }
        }));

    if (_currentItem.getAuthCode() == AuthCode.NOT_REQUESTED) {
      PlayerInfo playerInfo = _basePlayer == null ? StreamPlayer.defaultPlayerInfo
          : _basePlayer.getPlayerInfo();

      // Async authorize;
      final String taskKey = "changeCurrentItem" + System.currentTimeMillis();
      taskStarted(taskKey, _playerAPIClient.authorize(_currentItem, playerInfo,
          new AuthorizeCallback() {
            @Override
            public void callback(boolean result, OoyalaException error) {
              taskCompleted(taskKey);
              if (error != null) {
                _error = error;
                DebugMode.logD(TAG, "Exception in changeCurrentVideo!", error);
                setState(State.ERROR);
                sendNotification(ERROR_NOTIFICATION);
                return;
              }
              sendNotification(AUTHORIZATION_READY_NOTIFICATION);
              changeCurrentItemAfterAuth();
            }
          }));
      return true;
    }

    sendNotification(AUTHORIZATION_READY_NOTIFICATION);
    return changeCurrentItemAfterAuth();
  }

  /**
   * This is a helper function ONLY to be used with changeCurrentItem.
   *
   * @return
   */
  private boolean changeCurrentItemAfterAuth() {
    // wait for metadata and auth to return
    if (_currentItem.getModuleData() == null
        || _currentItem.getAuthCode() == AuthCode.NOT_REQUESTED) {
      return false;
    }

    sendNotification(CURRENT_ITEM_CHANGED_NOTIFICATION);

    if (!_currentItem.isAuthorized()) {
      this._error = getAuthError(_currentItem);
      setState(State.ERROR);
      sendNotification(ERROR_NOTIFICATION);
      return false;
    }

    if (_currentItem.isHeartbeatRequired()) {
      if (_authHeartbeat == null) {
        _authHeartbeat = new AuthHeartbeat(_playerAPIClient);
        _authHeartbeat.setAuthHeartbeatErrorListener(this);
      }
      _authHeartbeat.start();
    }

    cancelOpenTasks();
    final String taskKey = "changeCurrentItemAfterAuth"
        + System.currentTimeMillis();
    taskStarted(taskKey,
        _currentItem.fetchPlaybackInfo(new FetchPlaybackInfoCallback() {
          @Override
          public void callback(boolean result) {
            taskCompleted(taskKey);
            if (!result) {
              _error = new OoyalaException(
                  OoyalaException.OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
              setState(State.ERROR);
              return;
            }
            if (!changeCurrentItemAfterFetch()) {
              _error = new OoyalaException(
                  OoyalaException.OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
              setState(State.ERROR);
            }
          }
        }));
    return true;
  }

  /**
   * This is a helper function ONLY to be used with changeCurrentItem (in
   * changeCurrentItemAfterAuth).
   *
   * @return
   */
  private boolean changeCurrentItemAfterFetch() {
    String accountId = _playerAPIClient.getUserInfo().getAccountId();

    // If analytics is uninitialized, OR
    // If has account ID that was different than before, OR
    // If no account ID, but last time there _was_ an account id, we need to
    // re-initialize
    boolean needToLoadAnalytics = _analytics == null;
    needToLoadAnalytics |= accountId != null
        && !accountId.equals(_lastAccountId);
    needToLoadAnalytics |= accountId == null && _lastAccountId != null;

    if (needToLoadAnalytics) {
      _analytics = new Analytics(getLayout().getContext(), _playerAPIClient);
    }

    // last account ID seen. Could be null
    _lastAccountId = _playerAPIClient.getUserInfo().getAccountId();

    _analytics.initializeVideo(_currentItem.getEmbedCode(),
        _currentItem.getDuration());
    if (!processAdModes(AdMode.ContentChanged, 0)) {
      switchToContent(false);
    }
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

    PlayerInfo playerInfo = _basePlayer == null ? StreamPlayer.defaultPlayerInfo
        : _basePlayer.getPlayerInfo();

    // Async Authorize
    cancelOpenTasks();
    final String taskKey = "reinitialize" + System.currentTimeMillis();
    taskStarted(taskKey,
        _playerAPIClient.authorize(tree, playerInfo, new AuthorizeCallback() {
          @Override
          public void callback(boolean result, OoyalaException error) {
            taskCompleted(taskKey);
            if (error != null) {
              _error = error;
              DebugMode.logD(TAG, "Exception in reinitialize!", error);
              setState(State.ERROR);
              sendNotification(ERROR_NOTIFICATION);
              return;
            }
            changeCurrentItem(_rootItem.firstVideo());
          }
        }));
    return true;
  }

  private MoviePlayer getCorrectMoviePlayer(Video currentItem) {
    Set<Stream> streams = currentItem.getStreams();

    // Get correct type of Movie Player
    if (Stream.streamSetContainsDeliveryType(streams,
        Stream.DELIVERY_TYPE_WV_WVM)
        || Stream.streamSetContainsDeliveryType(streams,
            Stream.DELIVERY_TYPE_WV_HLS)) {
      return new WidevineOsPlayer();
    } else if (Stream.streamSetContainsDeliveryType(streams,
        Stream.DELIVERY_TYPE_WV_MP4)) {
      try {
        return (MoviePlayer) getClass().getClassLoader()
            .loadClass(WIDEVINE_LIB_PLAYER).newInstance();
      } catch (Exception e) {
        _error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED,
            "Could not initialize Widevine Player");
        DebugMode.logD(TAG, "Please include the Widevine Library in your project",
            _error);
        setState(State.ERROR);
      }
    }

    return new MoviePlayer();
  }

  /**
   * Create and initialize a content player for an item.
   * 
   * @return
   */
  private MoviePlayer createAndInitPlayer(Video item) {
    if (item == null) {
      DebugMode.assertFail(TAG, "current item is null when initialze player");
      return null;
    }

    MoviePlayer p = getCorrectMoviePlayer(item);
    if (p == null) {
      DebugMode.assertFail(TAG, "movie player is null when initialze player");
      return null;
    }

    Set<Stream> streams = item.getStreams();

    // Initialize this player
    p.addObserver(this);
    if (_basePlayer != null) {
      p.setBasePlayer(_basePlayer);
    }
    p.init(this, streams);

    p.setLive(item.isLive());

    addClosedCaptionsView();

    // Player must have been initialized, as well as player's basePlayer, in
    // order to continue
    if (p == null || p.getError() != null) {
      DebugMode.assertFail(TAG,
          "movie player has an error when initialze player");
      return null;
    }
    p.setSeekable(_seekable);
    return p;
  }

  private void cleanupPlayers() {
    if (_authHeartbeat != null) {
      _authHeartbeat.stop();
    }

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
   *
   * @return movie
   */
  public Video getCurrentItem() {
    return _currentItem;
  }

  /**
   * The embedded item (movie, channel, or channel set).
   *
   * @return movie
   */
  public ContentItem getRootItem() {
    return _rootItem;
  }

  /**
   * The metadata for current root item
   *
   * @return metadata
   */
  public JSONObject getMetadata() {
    return _metadata;
  }

  /**
   * Get the current error code, if one exists
   *
   * @return error code
   */
  public OoyalaException getError() {
    return _error;
  }

  /**
   * Get the embedCode for the current player.
   *
   * @return embedCode
   */
  public String getEmbedCode() {
    return _rootItem == null ? null : _rootItem.getEmbedCode();
  }

  /**
   * Get the authToken for the current player.
   *
   * @return authToken
   */
  public String getAuthToken() {
    return _playerAPIClient.getAuthToken();
  }

  /**
   * Get the customDRMData for the current player.
   *
   * @return _customDRMData
   */
  public String getCustomDRMData() {
    return _customDRMData;
  }

  /**
   * Set the customDRMData for the current player.
   *
   * @return _customDRMData
   */
  public void setCustomDRMData(String data) {
      _customDRMData = data;
  }

  /**
   * Get current player state. One of playing, paused, buffering, channel, or
   * error
   *
   * @return state
   */
  public State getState() {
    PlayerInterface p = currentPlayer();
    return p == null ? _state : p.getState();
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
    if (_analytics != null) {
      _analytics.reportPlayRequested();
    }
    if (currentPlayer() != null) {
      if (isAdPlaying()) {
        sendNotification(AD_STARTED_NOTIFICATION);
      }

      if (!initialContentPlay() || !this.processAdModes(AdMode.InitialPlay, 0)) {
        currentPlayer().play();
      }
    } else {
      queuePlay();
    }
  }

  /**
   * Determine if play is the initial play for the content, required to insert
   * preroll properly.
   * 
   * @return true if it is initial play
   */
  private boolean initialContentPlay() {
    if (_player.getState() == State.READY && _player.currentTime() == 0) {
      return true;
    }

    return false;
  }

  /**
   * Play the current video with an initialTime
   *
   * @param initialTimeInMillis
   *          the time to start the video.
   */
  public void play(int initialTimeInMillis) {
    play();
    seek(initialTimeInMillis);
  }

  /**
   * Suspend the current video (can be resumed later by calling resume). This
   * differs from pause in that it completely deconstructs the view so the
   * layout can be changed.
   */
  public void suspend() {
    suspendCurrentPlayer();

    if (_authHeartbeat != null) {
      _suspendTime = System.currentTimeMillis();
      _authHeartbeat.stop();
    }

    setState(State.SUSPENDED);
    if (_receiver != null) {
      unregisterReceiver(_receiver);
    }
  }

  /**
   * Resume the current video from a suspended state
   */
  public void resume() {
    if (_receiver != null) {
      registerReceiver(_receiver);
    }
    if (getCurrentItem() != null && getCurrentItem().isHeartbeatRequired()) {
      if (System.currentTimeMillis() > _suspendTime
          + (_playerAPIClient._heartbeatInterval * 1000)) {
        PlayerInfo playerInfo = _basePlayer == null ? StreamPlayer.defaultPlayerInfo
            : _basePlayer.getPlayerInfo();
        cancelOpenTasks();
        final String taskKey = "changeCurrentItem" + System.currentTimeMillis();
        taskStarted(taskKey, _playerAPIClient.authorize(_currentItem,
            playerInfo, new AuthorizeCallback() {
              @Override
              public void callback(boolean result, OoyalaException error) {
                taskCompleted(taskKey);
                if (error != null) {
                  _error = error;
                  DebugMode.logD(TAG, "Error Reauthorizing Video", error);
                  setState(State.ERROR);
                  sendNotification(ERROR_NOTIFICATION);
                  return;
                }
                sendNotification(AUTHORIZATION_READY_NOTIFICATION);
                if (!_currentItem.isAuthorized()) {
                  _error = new OoyalaException(
                      OoyalaException.OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED);
                  return;
                }
                _suspendTime = System.currentTimeMillis();
                resume();
              }
            }));
        return;
      } else {
        if (_authHeartbeat == null) {
          _authHeartbeat = new AuthHeartbeat(_playerAPIClient);
        }
        _authHeartbeat.start();
      }
    }

    if (currentPlayer() != null) {
      resumeCurrentPlayer();
    } else if (_currentItem != null && _currentItem.isAuthorized()) {
      prepareContent(false);
    } else {
      _error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED,
          "Resuming video from an invalid state");
      DebugMode.logD(TAG, "Resuming video from an improper state", _error);
      setState(State.ERROR);
      return;
    }

  }

  /**
   * Returns true if in fullscreen mode, false if not. Fullscreen currently does
   * not work due to limitations in Android.
   *
   * @return fullscreen mode
   */
  public boolean isFullscreen() {
    return _layoutController != null && _layoutController.isFullscreen();
  }

  /**
   * Set fullscreen mode (will only work if fullscreenLayout is set) This will
   * call the setFullscreen method on the associated LayoutController. If you
   * are implementing your own LayoutController here are some things to keep in
   * mind: <li>If the setFullscreen method of your LayoutController creates a
   * new OoyalaPlayerLayout or switches to a different one, you *must* call
   * OoyalaPlayer.suspend() before doing so and call OoyalaPlayer.resume() after
   * doing so. <li>If the setFullscreen method of your LayoutController uses the
   * same OoyalaPlayerLayout, you do not need to do any special handling.
   *
   * @param fullscreen
   *          true to switch to fullscreen, false to switch out of fullscreen
   */
  public void setFullscreen(boolean fullscreen) {
    if (isFullscreen() == !fullscreen) { // this is so we don't add/remove cc
                                         // view if we are not actually
      // changing state.
      removeClosedCaptionsView();
      _layoutController.setFullscreen(fullscreen);
      addClosedCaptionsView();

      // Create Learn More button when going in and out of fullscreen
      if (isShowingAd()) {
        ((AdMoviePlayer) _player).updateLearnMoreButton(getLayout(),
            getTopBarOffset());
      }
    }
  }

  /**
   * Get the absolute pixel of the top bar's distance from the top of the
   * device.
   *
   * @return pixels to shift the Learn More button down
   */
  public int getTopBarOffset() {
    return ((AbstractOoyalaPlayerLayoutController) _layoutController)
        .getControls().topBarOffset();
  }

  /**
   * Find where the playhead is with millisecond accuracy
   *
   * @return time in milliseconds
   */
  public int getPlayheadTime() {
    if (currentPlayer() == null) {
      return -1;
    }
    return currentPlayer().currentTime();
  }

  /**
   * Synonym for seek.
   *
   * @param timeInMillis
   *          in milliseconds
   */
  public void setPlayheadTime(int timeInMillis) {
    seek(timeInMillis);
  }

  /**
   * @return true if the current player is seekable, false if there is no
   *         current player or it is not seekable
   */
  public boolean seekable() {
    DebugMode.logV(TAG, "seekable(): !null=" + (currentPlayer() != null) + ", seekable="
        + (currentPlayer() == null ? "false" : currentPlayer().seekable()));
    return currentPlayer() != null && currentPlayer().seekable();
  }

  /**
   * Move the playhead to a new location in seconds with millisecond accuracy
   *
   * @param timeInMillis
   *          in milliseconds
   */
  public void seek(int timeInMillis) {
    DebugMode.logV(TAG, "seek()...: msec=" + timeInMillis);
    if (seekable()) {
      currentPlayer().seekToTime(timeInMillis);
      _queuedSeekTime = 0;
    } else {
      _queuedSeekTime = timeInMillis;
    }
    DebugMode.logV(TAG, "...seek(): _queuedSeekTime=" + _queuedSeekTime);
  }

  private void addClosedCaptionsView() {
    removeClosedCaptionsView();
    if (_currentItem != null && _currentItem.hasClosedCaptions()
        || _streamBasedCC) {
      // Initialize ClosedCaptionsStyle
      OOClosedCaptionPresentation presentation = OOClosedCaptionPresentation.OOClosedCaptionPopOn;

      // If enter the if statement then it means this "addClosedCaptionsView" is
      // called during resuming
      if (_closedCaptionsStyle != null
          && _closedCaptionsStyle.presentationStyle != OOClosedCaptionPresentation.OOClosedCaptionPopOn) {
        // This closed caption view had other presentation when the app went to
        // background
        presentation = _closedCaptionsStyle.presentationStyle;
      }
      _closedCaptionsStyle = new ClosedCaptionsStyle(getLayout().getContext());
      _closedCaptionsStyle.presentationStyle = presentation;
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

  /**
   * Get the current player, can be either content player or ad player
   * 
   * @return the player
   */
  private PlayerInterface currentPlayer() {
    return _adManager.inAdMode() ? _adManager.getPlayerInterface() : _player;
  }

  private boolean fetchMoreChildren(PaginatedItemListener listener) {
    Channel parent = _currentItem.getParent();
    if (parent != null) {
      ChannelSet parentOfParent = parent.getParent();
      if (parent.hasMoreChildren()) {
        return _playerAPIClient.fetchMoreChildrenForPaginatedParentItem(parent, listener);
      } else if (parentOfParent != null && parentOfParent.hasMoreChildren()) {
        return _playerAPIClient.fetchMoreChildrenForPaginatedParentItem(parentOfParent, listener);
      }
    }
    return false;
  }

  /**
   * Change the current video to the previous video in the Channel or
   * ChannelSet. If there is no previous video, this will seek to the beginning
   * of the video.
   *
   * @param what
   *          OoyalaPlayerControl.DO_PLAY or OoyalaPlayerControl.DO_PAUSE
   *          depending on what to do after the video is set.
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
   * Change the current video to the next video in the Channel or ChannelSet. If
   * there is no next video, nothing will happen. Note that this will trigger a
   * fetch of additional children if the Channel or ChannelSet is paginated. If
   * so, it may take some time before the video is actually set.
   *
   * @param what
   *          OoyalaPlayerControl.DO_PLAY or OoyalaPlayerControl.DO_PAUSE
   *          depending on what to do after the video is set.
   * @return true if there was a next video, false if not.
   */
  public boolean nextVideo(int what) {
    // This is required because android enjoys making things difficult. talk to
    // jigish if you got issues.
    if (_currentItem.nextVideo() != null) {
      changeCurrentItem(_currentItem.nextVideo());
      if (what == DO_PLAY) {
        play();
      } else if (what == DO_PAUSE) {
        pause();
      }
      return true;
    } else if (what == DO_PLAY
        && fetchMoreChildren(new PaginatedItemListener() {
          @Override
          public void onItemsFetched(int firstIndex, int count,
              OoyalaException error) {
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
    } else if (what == DO_PAUSE
        && fetchMoreChildren(new PaginatedItemListener() {
          @Override
          public void onItemsFetched(int firstIndex, int count,
              OoyalaException error) {
            _handler.post(new Runnable() {
              @Override
              public void run() {
                changeCurrentItem(_currentItem.nextVideo());
                pause();
              }
            });
          }
        })) {
      return true;
    }
    return false;
  }

  /**
   * reset the content player, only called by onComplete.
   */
  private void reset() {
    removeClosedCaptionsView();
    _playQueued = false;
    _player.reset();
    addClosedCaptionsView();
  }

  private void onComplete() {
    switch (_actionAtEnd) {
    case CONTINUE:
      if (nextVideo(DO_PLAY)) {
      } else {
        reset();
        sendNotification(PLAY_COMPLETED_NOTIFICATION);
      }
      break;
    case PAUSE:
      if (nextVideo(DO_PAUSE)) {
      } else {
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
    String notification = arg1.toString();
    DebugMode.logD(TAG, "update from" + arg0.toString() + " notification "
        + notification);

    if (arg0 instanceof Player) {
      processContentNotifications((Player) arg0, notification);
    }
  }

  /**
   * For Internal Use Only. Process content player notification.
   * 
   * @param player
   *          the notification sender
   * @param notification
   *          the notification
   */
  private void processContentNotifications(Player player, String notification) {
    if (notification.equals(TIME_CHANGED_NOTIFICATION)) {
      sendNotification(TIME_CHANGED_NOTIFICATION);
        // send analytics ping
      _analytics.reportPlayheadUpdate((_player.currentTime()) / 1000);
      processAdModes(AdMode.Playhead, _player.currentTime());
      // closed captions
      displayCurrentClosedCaption();
    } else if (notification.equals(STATE_CHANGED_NOTIFICATION)) {
      State state = player.getState();
      switch (state) {
      case COMPLETED:
        processAdModes(AdMode.ContentFinished, 0);
        break;
      case ERROR:
        DebugMode.logE(TAG,
            "Error recieved from content.  Cleaning up everything");
        _error = player.getError();
        int errorCode = _error == null ? 0 : _error.getCode().ordinal();
        processAdModes(AdMode.ContentError, _error == null ? 0 : errorCode);
        break;
      case PLAYING:
        if (getState() == State.READY) {
          if (_analytics != null) {
            _analytics.reportPlayStarted();
            } else {
              DebugMode.logE(TAG, "analytics is null when playing");
            }
            sendNotification(PLAY_STARTED_NOTIFICATION);
          }
          setState(State.PLAYING);
          break;
      case READY:
        if (_queuedSeekTime > 0) {
          seek(_queuedSeekTime);
        }
        setState(State.READY);
        break;
      case INIT:
      case LOADING:
      case PAUSED:
      default:
        setState(player.getState());
        break;
      }
    }
  }

  /**
   * For Internal Use Only. Process ad player notification.
   * 
   * @param notification
   *          the notification
   */
  private void processAdNotifications(String notification) {
    DebugMode.logD(TAG, "processAdNotification " + notification);
    if (!isShowingAd()) {
      DebugMode.assertFail(TAG, "not in ad mode, skipping");
      return;
    }

    sendNotification(notification);
  }

  /**
   * Get what the player will do at the end of playback.
   *
   * @return the OoyalaPlayer.OoyalaPlayerActionAtEnd to use
   */
  public ActionAtEnd getActionAtEnd() {
    return _actionAtEnd;
  }

  /**
   * Set what the player should do at the end of playback.
   *
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
   *
   * @param language
   *          2 letter country code of the language to display or nil to hide
   *          closed captions
   */
  public void setClosedCaptionsLanguage(String language) {
    _language = language;

    if (_player == null || !(_player instanceof MoviePlayer)) {
      return;
    }

    MoviePlayer mp = (MoviePlayer) _player;
    // If we're given the "cc" language, we know it's live closed captions
    if (_language == LIVE_CLOSED_CAPIONS_LANGUAGE) {
      mp.setLiveClosedCaptionsEnabled(true);
      return;
    }
    if (_language == null) {
      mp.setLiveClosedCaptionsEnabled(false);
    }
    if (_closedCaptionsView != null)
      _closedCaptionsView.setCaption(null);
    displayCurrentClosedCaption();
  }

  public void setClosedCaptionsPresentationStyle(
      OOClosedCaptionPresentation presentationStyle) {
    removeClosedCaptionsView();
    _closedCaptionsView = new ClosedCaptionsView(getLayout().getContext());
    if( _closedCaptionsStyle != null ) {
      _closedCaptionsStyle.presentationStyle = presentationStyle;
      _closedCaptionsView.setStyle(_closedCaptionsStyle);
    }
    getLayout().addView(_closedCaptionsView);
    _closedCaptionsView.setCaption(null);
    displayCurrentClosedCaption();
  }

  /**
   * Get the current closed caption language
   *
   * @return the current closed caption language
   */
  public String getClosedCaptionsLanguage() {
    return _language;
  }

  /**
   * Get the available closed captions languages
   *
   * @return a Set of Strings containing the available closed captions languages
   */
  public Set<String> getAvailableClosedCaptionsLanguages() {

    // If our player found live closed captions, only show option for CC.
    if (_player != null && (_player instanceof MoviePlayer)
        && ((MoviePlayer) _player).isLiveClosedCaptionsAvailable()) {
      Set<String> retval = new HashSet<String>();
      retval.add(LIVE_CLOSED_CAPIONS_LANGUAGE);
      return retval;
    }
    if (_currentItem == null || _currentItem.getClosedCaptions() == null) {
      return new HashSet<String>();
    }
    return getCurrentItem().getClosedCaptions().getLanguages();
  }

  /**
   * @return get the bitrate of the current item
   */
  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
  public double getBitrate() {
    Stream currentStream = null;
    if (getCurrentItem() == null
        || (currentStream = Stream.bestStream(getCurrentItem().getStreams())) == null) {
      return -1;
    }
    String deliveryType = currentStream.getDeliveryType();
    if (deliveryType != null
        && !deliveryType.equals(Stream.DELIVERY_TYPE_MP4)) {
      return -2;
    }
    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      // Query for bitrate
      MediaMetadataRetriever metadataRetreiver = new MediaMetadataRetriever();
      metadataRetreiver.setDataSource(
          Stream.bestStream(getCurrentItem().getStreams()).getUrl(),
          new HashMap<String, String>());
      return Double.parseDouble(metadataRetreiver
          .extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
    } else {
      return Stream.bestStream(getCurrentItem().getStreams()).getVideoBitrate() * 1000;
    }
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
    return isShowingAd();
  }

  /**
   * Seek to the given percentage
   *
   * @param percent
   *          percent (between 0 and 100) to seek to
   */
  public void seekToPercent(int percent) {
    DebugMode.logV(TAG, "seekToPercent()...: percent=" + percent);
    if (percent < 0 || percent > 100) {
      return;
    }
    if (seekable()) {
      seek(percentToMillis(percent));
    }
    DebugMode.logV(TAG, "...seekToPercent()");
  }

  /**
   * Get the current item's duration
   *
   * @return the duration in milliseconds
   */
  public int getDuration() {
    if (currentPlayer() == null) {
      return 0;
    }
    int playerDuration = currentPlayer().duration();
    if (playerDuration > 0)
      return playerDuration;
    if (getCurrentItem() == null)
      return 0;
    return getCurrentItem().getDuration();
  }

  /**
   * Get the current item's buffer percentage
   *
   * @return the buffer percentage (between 0 and 100 inclusive)
   */
  public int getBufferPercentage() {
    if (currentPlayer() == null) {
      return 0;
    }
    return currentPlayer().buffer();
  }

  /**
   * Get the current item's playhead time as a percentage
   *
   * @return the playhead time percentage (between 0 and 100 inclusive)
   */
  public int getPlayheadPercentage() {
    if (currentPlayer() == null) {
      return 0;
    }
    return millisToPercent(currentPlayer().currentTime());
  }

  /**
   * Set whether ads played by this OoyalaPlayer are seekable (default is false)
   *
   * @param seekable
   *          true if seekable, false if not.
   */
  public void setAdsSeekable(boolean seekable) {
    _managedAdsPlugin.setSeekable(seekable);
  }

  /**
   * Set whether videos played by this OoyalaPlayer are seekable (default is
   * true)
   *
   * @param seekable
   *          true if seekable, false if not.
   */
  public void setSeekable(boolean seekable) {
    _seekable = seekable;
    if (_player != null) {
      _player.setSeekable(_seekable);
    }
  }

  /**
   * This will reset the state of all the ads to "unplayed" causing any ad that
   * has already played to play again.
   */
  public void resetAds() {
    _adManager.resetAds();
  }

  /**
   * Skip the currently playing ad. Do nothing if no ad is playing
   */
  public void skipAd() {
    if (isShowingAd()) {
      sendNotification(AD_SKIPPED_NOTIFICATION);
      _adManager.skipAd();
    }
  }

  /**
   * @return true if the OoyalaPlayer is currently showing an ad (in any state).
   *         false if not.
   */
  public boolean isShowingAd() {
    return (_adManager.inAdMode());
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
    DebugMode.logV(TAG, "queuePlay()");
    _playQueued = true;
  }

  private void dequeuePlay() {
    if (_playQueued && currentPlayer() != null) {
      _playQueued = false;
      play();
    }
  }

  private void taskStarted(String key, Object task) {
    if (task != null)
      _openTasks.put(key, task);
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
   *
   * @param closedCaptionsStyle
   *          the ClosedCaptionsStyle to use
   */
  public void setClosedCaptionsStyle(ClosedCaptionsStyle closedCaptionsStyle) {
    _closedCaptionsStyle = closedCaptionsStyle;
    if (_closedCaptionsStyle != null) {
      if( _closedCaptionsView != null ) {
        _closedCaptionsView.setStyle(_closedCaptionsStyle);
        _closedCaptionsView.setStyle(_closedCaptionsStyle);
      }
    }
    displayCurrentClosedCaption();
  }

  /**
   * Set the bottomMargin of closedCaptions view
   *
   * @param bottomMargin
   *          the bottom margin to use
   */
  public void setClosedCaptionsBottomMargin(int bottomMargin) {
    if( _closedCaptionsStyle != null ) {
      _closedCaptionsStyle.bottomMargin = bottomMargin;
      if( _closedCaptionsView != null ) {
        _closedCaptionsView.setStyle(_closedCaptionsStyle);
      }
    }
  }

  private void displayCurrentClosedCaption() {
    if (_closedCaptionsView == null || _currentItem == null)
      return;
    if (_streamBasedCC)
      return;

    // PB-3090: we currently only support captions for the main content, not
    // also the advertisements.
    if (_language != null && _currentItem.hasClosedCaptions() && !isShowingAd()) {
      double currT = (currentPlayer().currentTime()) / 1000d;
      if (_closedCaptionsView.getCaption() == null
          || currT > _closedCaptionsView.getCaption().getEnd()
          || currT < _closedCaptionsView.getCaption().getBegin()) {
        Caption caption = _currentItem.getClosedCaptions().getCaption(
            _language, currT);
        if (caption != null && caption.getBegin() <= currT
            && caption.getEnd() >= currT) {
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
   *
   * @param callback
   *          the CurrentItemChangedCallback that should be called every time
   *          the current item changes
   */
  public void setCurrentItemChangedCallback(CurrentItemChangedCallback callback) {
    _currentItemChangedCallback = callback;
  }

  public StreamPlayer getBasePlayer() {
    return _basePlayer;
  }

  /**
   * Check to ensure the BasePlayer is authorized to play streams. If it is, set
   * this base player onto our players and remember it
   *
   * @param basePlayer
   */
  public void setBasePlayer(StreamPlayer basePlayer) {
    _basePlayer = basePlayer;

    _analytics.setUserAgent(_basePlayer != null ? _basePlayer.getPlayerInfo()
        .getUserAgent() : null);

    if (getCurrentItem() == null) {
      return;
    }

    this.cancelOpenTasks();

    final String taskKey = "setBasePlayer" + System.currentTimeMillis();
    PlayerInfo playerInfo = basePlayer == null ? StreamPlayer.defaultPlayerInfo
        : basePlayer.getPlayerInfo();
    taskStarted(taskKey, _playerAPIClient.authorize(_currentItem, playerInfo,
        new AuthorizeCallback() {
          @Override
          public void callback(boolean result, OoyalaException error) {
            taskCompleted(taskKey);
            if (error != null) {
              _error = error;
              DebugMode.logD(TAG, "Movie is not authorized for this device!", error);
              setState(State.ERROR);
              sendNotification(ERROR_NOTIFICATION);
              return;
            }

            if (_player != null && (_player instanceof MoviePlayer)) {
              ((MoviePlayer) _player).setBasePlayer(_basePlayer);
            }
          }
        }));
  }

  /**
   * set the analytics tags
   * 
   * @param tags
   *          the list of tags to set
   */
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

  /**
   * Generate the authorization error of a video item.
   *
   * @param currentItem
   * @return a properly described OoyalaException
   */
  private OoyalaException getAuthError(Video currentItem) {
    // Get description and make the exception
    String description = "Authorization Error: "
        + ContentItem.getAuthError(currentItem.getAuthCode());
    DebugMode.logE(this.getClass().toString(), "This video was not authorized! "
        + description);
    return new OoyalaException(
        OoyalaException.OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED, description);
  }

  /**
   * get the seek style
   * 
   * @return the seek style of current player
   */
  public SeekStyle getSeekStyle() {
    if (getBasePlayer() != null) {
      return getBasePlayer().getSeekStyle();
    } else if (currentPlayer() != null && currentPlayer() instanceof MoviePlayer) {
      return ((MoviePlayer)currentPlayer()).getSeekStyle();
    } else if (currentPlayer() != null) {
      //TODO: the PlayerInterface may need getSeekStyle();
      return SeekStyle.BASIC;
    } else {
      Log.w(this.getClass().toString(), "We are seeking without a MoviePlayer!");
      return SeekStyle.NONE;
    }
  }

  /**
   * Register an Ad player our players and remember it
   *
   * @param adTypeClass
   *          A type of AdSpot that the player is capable of playing
   * @param adPlayerClass
   *          A player that plays the ad
   */
  void registerAdPlayer(Class<? extends OoyalaManagedAdSpot> adTypeClass,
      Class<? extends AdMoviePlayer> adPlayerClass) {
    _adPlayers.put(adTypeClass, adPlayerClass);
  }

  /**
   * get the ad player class for a certain ad spot
   * 
   * @param ad
   *          the adspot
   * @return the adplayer class
   */
  Class<? extends AdMoviePlayer> getAdPlayerClass(OoyalaManagedAdSpot ad) {
    return _adPlayers.get(ad.getClass());
  }

  /**
   * Get the SDK version and RC number of this Ooyala Player SDK
   *
   * @return the SDK version as a string
   */
  public static String getVersion() {
    return SDK_VERSION;
  }

  /**
   * register a ad plugin
   * 
   * @param plugin
   *          the plugin to be registered
   * @return true if registration succeeded, false otherwise
   */
  @Override
  public boolean registerPlugin(final AdPluginInterface plugin) {
    return _adManager.registerPlugin(plugin);
  }

  /**
   * deregister a ad plugin
   * 
   * @param plugin
   *          the plugin to be deregistered
   * @return true if deregistration succeeded, false otherwise
   */
  @Override
  public boolean deregisterPlugin(final AdPluginInterface plugin) {
    return _adManager.deregisterPlugin(plugin);
  }

  private void switchToAdMode(AdMode mode) {
    DebugMode.logD(TAG, "switchToAdMode");

    if (_player != null) {
        _player.suspend();
    }
    removeClosedCaptionsView();
    _adManager.onAdModeEntered();
  }

  private void switchToContent(boolean forcePlay) {
    if (_player == null) {
      prepareContent(forcePlay);
    } else if (_player.getState() == State.SUSPENDED) {
      if (forcePlay) {
        _player.resume(_player.currentTime(), State.PLAYING);
      } else {
        _player.resume();
      }
      addClosedCaptionsView();
    }
  }

  /**
   * called by a plugin when it finishes ad play and return the control to
   * ooyalaplayer
   * 
   * @param plugin
   *          the caller plugin
   * @return true if exit succeeded, false otherwise
   */
  @Override
  public boolean exitAdMode(final AdPluginInterface plugin) {
    return _adManager.exitAdMode(plugin);
  }

  private boolean prepareContent(boolean forcePlay) {
    if (_player != null) {
      DebugMode.assertFail(TAG,
          "try to allocate player while player already exist");
      return false;
    }

    MoviePlayer mp = createAndInitPlayer(_currentItem);
    if (mp == null) {
      return false;
    }

    _player = mp;
    if (forcePlay) {
      play();
    } else {
      dequeuePlay();
    }
    return true;
  }

  void processExitAdModes(AdMode mode, boolean adsDidPlay) {
    DebugMode.logD(TAG, "exit admode from mode " + mode.toString()
        + "ads did play " + String.valueOf(adsDidPlay));
    switch (mode) {
    case ContentChanged:
      switchToContent(false);
      break;
    case InitialPlay:
    case Playhead:
    case CuePoint:
      if (adsDidPlay) {
        _handler.post(new Runnable() {
          @Override
          public void run() {
            switchToContent(true);
          }
        });
      }
      break;
    case ContentFinished:
      onComplete();
      break;
    case ContentError:
      onContentError();
      break;
    default:
      DebugMode.assertFail(TAG,
          "exitAdMode with unknown mode " + mode.toString()
 + "adsDidPlay "
              + String.valueOf(adsDidPlay));
      break;
    }
  }

  private void onContentError() {
    cleanupPlayers();
    setState(State.ERROR);
    sendNotification(ERROR_NOTIFICATION);
  }

  /*
   * process adMode event
   * 
   * @return true if adManager require ad mode, false otherwise
   */
  private boolean processAdModes(AdMode mode, int parameter) {
    boolean result = _adManager.onAdMode(mode, parameter);
    if (result) {
      switchToAdMode(mode);
    } else {
      processExitAdModes(mode, false);
    }
    return result;
  }

  private void suspendCurrentPlayer() {
    if (_adManager.inAdMode()) {
      _adManager.suspend();
    } else if (_player != null) {
      _player.suspend();
      removeClosedCaptionsView();
    }
  }

  private void resumeCurrentPlayer() {
    if (_adManager.inAdMode()) {
      _adManager.resume();
    } else if (_player != null) {
      _player.resume();
      dequeuePlay();
      this.addClosedCaptionsView();
    }
  }

  private static Context _context;

  /**
   * called by a ad player when buffer changed to update the UI
   * 
   * @param sender
   *          the player who triggers the notification
   */
  public static void notifyBufferChange(PlayerInterface sender) {
    if (_context == null) {
      DebugMode.assertFail(TAG, "notify buffer change when context is null");
      return;
    }
    Intent intent = new Intent(BUFFER_CHANGED_NOTIFICATION);
    intent.putExtra("sender", sender.hashCode());
    LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
  }

  /**
   * called by a ad player when player state changed to update the UI
   * 
   * @param sender
   *          the player who triggers the notification
   */
  public static void notifyStateChange(PlayerInterface sender) {
    if (_context == null) {
      DebugMode.assertFail(TAG, "notify state change when context is null");
      return;
    }
    Intent intent = new Intent(STATE_CHANGED_NOTIFICATION);
    intent.putExtra("sender", sender.hashCode());
    LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
  }

  /**
   * called by a ad player when ad playhead changed
   * 
   * @param sender
   *          the player who triggers the notification
   */
  public static void notifyTimeChange(PlayerInterface sender) {
    if (_context == null) {
      DebugMode.assertFail(TAG, "notify time change when context is null");
      return;
    }
    Intent intent = new Intent(TIME_CHANGED_NOTIFICATION);
    intent.putExtra("sender", sender.hashCode());
    LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
  }

  private static void registerReceiver(BroadcastReceiver receiver) {
    if (_context == null) {
      DebugMode.logE(TAG, "context is null");
    }
    IntentFilter filter = new IntentFilter(TIME_CHANGED_NOTIFICATION);
    filter.addAction(BUFFER_CHANGED_NOTIFICATION);
    filter.addAction(STATE_CHANGED_NOTIFICATION);
    LocalBroadcastManager.getInstance(_context).registerReceiver(receiver,
        filter);
  }

  private static void unregisterReceiver(BroadcastReceiver receiver) {
    LocalBroadcastManager.getInstance(_context).unregisterReceiver(receiver);
  }

  private static void setContext(Context c) {
    if (_context == null) {
      _context = c;
    }
  }
}