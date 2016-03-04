package com.ooyala.android.castsdk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.common.images.WebImage;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.CastException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.NoConnectionException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.ooyala.android.CastModeOptions;
import com.ooyala.android.OoyalaException;
import com.ooyala.android.OoyalaException.OoyalaErrorCode;
import com.ooyala.android.OoyalaNotification;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.item.Video;
import com.ooyala.android.player.PlayerInterface;
import com.ooyala.android.player.PlayerInterfaceUtil;
import com.ooyala.android.plugin.LifeCycleInterface;
import com.ooyala.android.util.DebugMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;

/**
 * Control the playback e.g. pausing, seeking.
 * The lifecycle of the CastPlayer is to be managed by the CastManager, not by the 3rd party Android application.
 */
public class CastPlayer extends Observable implements PlayerInterface, LifeCycleInterface {

  private static final String TAG = CastPlayer.class.getSimpleName();
  private String RECEIVER_LIVE_LANGUAGE = "live";
  private String RECEIVER_DISABLE_LANGUAGE = "";

  private CastManager castManager;
  private String embedCode;
  private Video currentItem;
  private int currentTime;
  private State state = State.INIT;
  private OoyalaException error;

  private boolean seekable;

  private boolean isLiveClosedCaptionsAvailable;

  private Bitmap castImageBitmap;

  /*package private on purpose*/ CastPlayer(CastManager cm) {
    this.castManager = cm;
  }


  /*package private on purpose*/ void setOoyalaPlayer(OoyalaPlayer ooyalaPlayer) {
    DebugMode.logD(TAG, "Set OoyalaPlayer = " + ooyalaPlayer);
    this.addObserver(ooyalaPlayer);
    updateMetadataFromOoyalaPlayer(ooyalaPlayer);
    setChanged();
    notifyObservers(new OoyalaNotification(OoyalaPlayer.STATE_CHANGED_NOTIFICATION_NAME));
  }

  /*package private on purpose*/ void disconnectFromCurrentOoyalaPlayer() {
    DebugMode.logD(TAG, "Disconnect from current OoyalaPlayer by removing observers");
    this.deleteObservers();
  }
  
  /*============================================================================================*/
  /*========== CastPlayer Controls =======================================================*/
  /*============================================================================================*/
  
  @Override
  public void pause() {
    DebugMode.logD(TAG, "pause()");

    try {
      VideoCastManager.getInstance().pause();
    } catch (CastException | NoConnectionException | TransientNetworkDisconnectionException e) {
      DebugMode.logE(TAG, "PAUSE FAILED due to Exception", e);
    }
  }

  @Override
  public void play() {
    DebugMode.logD(TAG, "play()");

    try {
      VideoCastManager.getInstance().play();
    } catch (CastException | NoConnectionException | TransientNetworkDisconnectionException e) {
      DebugMode.logE(TAG, "PLAY FAILED due to exception", e);
    }
  }

  @Override
  public int currentTime() {
    return currentTime;
  }

  /**
   * Change the playhead to the given time.
   * @param curTime position to seek to, in milliseconds.
   */
  private void setCurrentTime(int curTime) {
    currentTime = curTime;
    onPlayHeadChanged();
  }

  @Override
  public int duration() {
    int duration = 0;
    try {
      duration = (int)CastManager.getVideoCastManager().getMediaDuration();
    } catch (NoConnectionException | TransientNetworkDisconnectionException e) {
      DebugMode.logD(TAG, "failed to get duration from cast due to exception", e);
    }

    if (duration <= 0 && currentItem != null) {
      duration = currentItem.getDuration();
    }

    return duration;
  }

  /**
   * Tell the UI if the scrubber should allow seeking.
   * @param seekable true for scrubbing/seeking, false to prevent it.
   */
  public void setSeekable(boolean seekable) {
    this.seekable = seekable;
  }
  
  @Override
  public boolean seekable() {
    return seekable;
  }

  @Override
  public void seekToTime(int timeInMillis) {
    DebugMode.logD(TAG, "Seek to time in seconds: " + timeInMillis / 1000);
    JSONObject actionSeek = new JSONObject();
    try {
      castManager.getVideoCastManager().seek(timeInMillis);
    } catch (NoConnectionException | TransientNetworkDisconnectionException e) {
      DebugMode.logE(TAG, "PLAY FAILED due to exception", e);
      return;
    }

    setState(State.LOADING);
    setCurrentTime(timeInMillis);
    onPlayHeadChanged();
  }

  /*package private on purpose*/ void syncDeviceVolumeToTV() {
    if (castManager != null) {
      castManager.syncVolume();
    }
  }
  
  private void setState(State state) {
    final State oldState = this.state;
    this.state = state;
    setChanged();
    notifyObservers( PlayerInterfaceUtil.buildSetStateNotification( oldState, this.state ) );
  }

  @Override
  public State getState() {
    return state;
  }

  @Override
  public int buffer() {
    return 0;
  }

  @Override
  public void setClosedCaptionsLanguage(String language) {
    DebugMode.logD(TAG, "Sending Closed Captions information to Cast: " + language);

    JSONObject actionSetVolume = new JSONObject();
    try {
      actionSetVolume.put("action", "setCCLanguage");
      actionSetVolume.put("data", convertClosedCaptionsLanguageForReceiver(language));
    } catch (JSONException e) {
      DebugMode.logE(TAG, "FAILED to set CC due to JSON exception", e);
      return;
    }

    try {
      castManager.getVideoCastManager().sendDataMessage(actionSetVolume.toString());
    } catch (NoConnectionException | TransientNetworkDisconnectionException e) {
      DebugMode.logD(TAG, "FAILED to set CC to " + language + " due to exception ", e);
    }
  }

  private String convertClosedCaptionsLanguageForReceiver(String language) {
    if (OoyalaPlayer.LIVE_CLOSED_CAPIONS_LANGUAGE.equalsIgnoreCase(language)) {
      return RECEIVER_LIVE_LANGUAGE;
    } else if (language == null) {
      return RECEIVER_DISABLE_LANGUAGE;
    } else {
      return language;
    }
  }

  @Override
  public OoyalaException getError() {
    return error;
  }

  @Override
  public boolean isLiveClosedCaptionsAvailable() {
    return isLiveClosedCaptionsAvailable;
  }

  /**
   * For Ooyala internal use only.
   */
  // TODO: maybe this should be returning 100%, not 0%?
  public int livePlayheadPercentage() {
    return 0;
  }

  /**
   * For Ooyala internal use only.
   * Seeking to live is not currently supported.
   */
  public void seekToPercentLive(int percent) {
  }

  /**
   * @return the current asset embed code. Possibly null.
   */
  public String getEmbedCode() {
    return currentItem == null ? "" : currentItem.getEmbedCode();
  }

  /*============================================================================================*/
  /*========== CastPlayer Receiver related =====================================================*/
  /*============================================================================================*/

  /*package private on purpose*/
  void enterCastMode(CastModeOptions options, String embedToken, Map<String, String> additionalInitParams) {
    DebugMode.logD(TAG, "On Cast Mode Entered with embedCode " + options.getEmbedCode());
    if (!initWithTheCastingContent(options.getEmbedCode())) {
      resetStateOnVideoChange();
      this.embedCode = options.getEmbedCode();
      this.loadMedia(options, embedToken, additionalInitParams);
      setCurrentTime(options.getPlayheadTimeInMillis());
    }
  }

  private void resetStateOnVideoChange() {
    isLiveClosedCaptionsAvailable = false;
  }

  private boolean initWithTheCastingContent(String embedCode) {
    return this.embedCode != null && this.embedCode.equals(embedCode);
  }
  
  private void loadMedia(CastModeOptions options, String embedToken, Map<String, String> additionalInitParams) {
    JSONObject playerParams = new JSONObject();
    String itemTitle = null;
    String itemPromoImageUrl = null;
    try {
      if (embedToken != null) {
        playerParams.put("embedToken", embedToken);
      }

      if (options.getCCLanguage() != null) {
        playerParams.put("ccLanguage", convertClosedCaptionsLanguageForReceiver(options.getCCLanguage()));
      }

      if (options.getAuthToken() != null) {
        playerParams.put("authToken", options.getAuthToken());
      }

      if (options.getDomain() != null) {
        playerParams.put("domain", options.getDomain().toString());
      }

      playerParams.put("ec", options.getEmbedCode());
      playerParams.put("version", null);
      playerParams.put("params", playerParams.toString());
      if (currentItem != null) {
        itemTitle = currentItem.getTitle();
        if (itemTitle != null) {
          playerParams.put("title", itemTitle);
        }
        String itemDescription = currentItem.getDescription();
        if (itemDescription != null) {
          playerParams.put("description", itemDescription);
        }
        itemPromoImageUrl = currentItem.getPromoImageURL(2000, 2000);
        if (itemPromoImageUrl != null) {
          playerParams.put("promo_url", itemPromoImageUrl);
        }
      } else {
        DebugMode.logE(TAG, "Title or description or PromoImage is null!!");
      }

      // Iterate through additionalInitParams (Overrides anything set by default in the init)
      if (additionalInitParams != null) {
        Iterator paramsIterator = additionalInitParams.entrySet().iterator();
        while (paramsIterator.hasNext()) {
          Map.Entry<String, String> entry = (Map.Entry) paramsIterator.next();
          playerParams.put(entry.getKey(), entry.getValue());
        }
      }
    } catch (JSONException e) {
      DebugMode.logE(TAG, "FAILED to compose load media due to json exception", e);
      return;
    }

    MediaMetadata metadata= new MediaMetadata();
    if (itemTitle != null) {
      metadata.putString(MediaMetadata.KEY_TITLE, itemTitle);
    }
    if (itemPromoImageUrl != null ) {
      Uri uri = Uri.parse(itemPromoImageUrl);
      if (uri != null) {
        WebImage image = new WebImage(uri);
        metadata.addImage(image);
      }
    }
    MediaInfo mediaInfo = new MediaInfo.Builder(options.getEmbedCode())
        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
        .setContentType("video/mp4")
        .setCustomData(playerParams)
        .setMetadata(metadata)
        .build();
    try {
      setState(State.LOADING);
      DebugMode.logD(TAG, "LoadMedia MediaInfo" + mediaInfo.toString() + "Playhead" + options.getPlayheadTimeInMillis());
      this.castManager.getVideoCastManager().loadMedia(mediaInfo, true, options.getPlayheadTimeInMillis());
    } catch (NoConnectionException | TransientNetworkDisconnectionException e) {
      DebugMode.logE(TAG, "FAILED to load media due to cast exception" + e.getMessage());
      this.error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Chromecast load media exception.");
      setState(State.ERROR);
    }
  }

  /*package private on purpose*/ void updateMetadataFromOoyalaPlayer(OoyalaPlayer player) {
    if (player != null && player.getCurrentItem() != null) {
      currentItem = player.getCurrentItem();
      seekable = player.seekable();
      loadIcon();
    } else {
      DebugMode.logD(TAG, "OoyalaPlayer or currentItem returns null when updateMetadata()");
    }
  }
  
  private void loadIcon() {
    final String imageUrl = (currentItem == null ? "" : currentItem.getPromoImageURL(2000, 2000));
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          URL imgUrl = new URL(imageUrl);
          castImageBitmap = BitmapFactory.decodeStream(imgUrl.openStream());
        } catch (IOException e) {
          DebugMode.logE(
              TAG, "setIcon(): Failed to load the image with url: " + imageUrl + ", trying the default one",
              e);
          castImageBitmap = castManager.getDefaultIcon();
        }
      }
    }).start();
  }

  private void onPlayHeadChanged() {
    setChanged();
    notifyObservers(new OoyalaNotification(OoyalaPlayer.TIME_CHANGED_NOTIFICATION_NAME));
  }

  /*package private on purpose*/ void onPlayerStatusChanged(int remotePlayerStatus) {
    switch (remotePlayerStatus) {
      case MediaStatus.PLAYER_STATE_BUFFERING:
        DebugMode.logD(TAG, "castplayerStateChanged: Buffering");
        setState(State.LOADING);
        break;
      case MediaStatus.PLAYER_STATE_PAUSED:
        DebugMode.logD(TAG, "castplayerStateChanged: Paused");
        setState(State.PAUSED);
        break;
      case MediaStatus.PLAYER_STATE_PLAYING:
        DebugMode.logD(TAG, "castplayerStateChanged: Playing");
        setState(State.PLAYING);
        break;
      default:
        break;
    }
  }

  /*package private on purpose*/ void receivedMessage(String message) {
    try {
      JSONObject msg = new JSONObject(message);
      
      // The key for "State Message" is different from other messages
      // So we should check it separately

      if (msg.has("0")) {
        String eventType = msg.getString("0");
        if (eventType.equals("downloading")) {
          // do nothing.
        } else if (eventType.equalsIgnoreCase("playheadTimeChanged")) {
          String currentTime = msg.getString("1");
          setCurrentTime((int) (Double.parseDouble(currentTime) * 1000));
          onPlayHeadChanged();
        } else if (eventType.equalsIgnoreCase("contentTreeFetched")) {
          String embedCode =  msg.getJSONObject("1").getString("embed_code");
          if (this.embedCode != null && !this.embedCode.equals(embedCode)) {
            DebugMode.logD(TAG, "Disconnect from chromecast and exit cast mode because a different content is casting");
            castManager.getVideoCastManager().disconnectDevice( false, true, true );
          }
        } else if (eventType.equalsIgnoreCase("playbackReady")) {
          onPlayHeadChanged();
          syncDeviceVolumeToTV();
        } else if (eventType.equalsIgnoreCase("closedCaptionsInfoAvailable")) {
          //TODO: Need to check if the info available is "live"
          String language = msg.getJSONObject("1").getString("lang");
          if (RECEIVER_LIVE_LANGUAGE.equals(language)) {
            isLiveClosedCaptionsAvailable = true;
          }
        } else if (eventType.equalsIgnoreCase("played")) {
          setCurrentTime(0);
          embedCode = null;
          setSeekable(false);
          setState(State.COMPLETED);
          castManager.hideMiniController();
        } else if (eventType.equalsIgnoreCase("error")) {
          String receiverCode = msg.getJSONObject("1").getString("code");
          this.error = new OoyalaException(getOoyalaErrorCodeForReceiverCode(receiverCode), "Error from Cast Receiver: " + receiverCode);
          setState(State.ERROR);
          setChanged();
          notifyObservers(new OoyalaNotification(OoyalaPlayer.ERROR_NOTIFICATION_NAME));
        }
      }
    } catch (JSONException e) {
      DebugMode.logE(TAG, "Ill formatted message" + message, e);
    }
  }

  /*package private on purpose*/ void onCastManagerError(OoyalaException error) {
    this.error = error;
    setState(State.ERROR);
    setChanged();
    notifyObservers(new OoyalaNotification(OoyalaPlayer.ERROR_NOTIFICATION_NAME));
  }
  
  /*============================================================================================*/
  /*========== CastPlayer Lifecycle Interface =======================================================*/
  /*============================================================================================*/

  @Override
  public void resume() {
  }

  @Override
  public void stop() {
  }

  @Override
  public void reset() {
  }

  @Override
  public void suspend() {
  }

  @Override
  public void resume(int timeInMilliSecond, State stateToResume) {
  }
  
  @Override
  public void destroy() {
    deleteObservers();
  }

  // The translations between HTML5 Error codes and OoyalaPlayer Error Codes
  static Map<String, OoyalaErrorCode> errorMap;
  static {
    Map<String, OoyalaErrorCode> map = new HashMap<String, OoyalaErrorCode>();
    map.put("network", OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
    map.put("sas", OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED);
    map.put("geo", OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED);
    map.put("domain", OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED);
    map.put("future", OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED);
    map.put("past", OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED);
    map.put("device", OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED);
    map.put("proxy", OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED);
    map.put("concurrent_streams", OoyalaErrorCode.ERROR_DEVICE_CONCURRENT_STREAMS);
    map.put("invalid_heartbeat", OoyalaErrorCode.ERROR_AUTHORIZATION_HEARTBEAT_FAILED);
    map.put("device_invalid_auth_token", OoyalaErrorCode.ERROR_DEVICE_INVALID_AUTH_TOKEN);
    map.put("device_limit_reached", OoyalaErrorCode.ERROR_DEVICE_LIMIT_REACHED);
    map.put("device_binding_failed", OoyalaErrorCode.ERROR_DEVICE_BINDING_FAILED);
    map.put("device_id_too_long", OoyalaErrorCode.ERROR_DEVICE_ID_TOO_LONG);
    map.put("drm_server_error", OoyalaErrorCode.ERROR_DRM_RIGHTS_SERVER_ERROR);
    map.put("drm_general_failure", OoyalaErrorCode.ERROR_DRM_GENERAL_FAILURE);
    map.put("invalid_entitlements", OoyalaErrorCode.ERROR_UNKNOWN);
    map.put("playback", OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
    map.put("stream", OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
    map.put("livestream", OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
    map.put("network_error", OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
    map.put("chromecast_manifest", OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
    map.put("chromecast_mediakeys", OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
    map.put("chromecast_network", OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
    map.put("chromecast_playback", OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
    map.put("unplayable_content", OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
    map.put("invalid_external_id", OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
    map.put("empty_channel", OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
    map.put("empty_channel_set", OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
    map.put("channel_content", OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
    map.put("content_tree", OoyalaErrorCode.ERROR_CONTENT_TREE_INVALID);
    map.put("metadata", OoyalaErrorCode.ERROR_METADATA_FETCH_FAILED);
    errorMap = Collections.unmodifiableMap(map);
  }

  private OoyalaErrorCode getOoyalaErrorCodeForReceiverCode(String receiverCode) {
     return errorMap.get(receiverCode) == null ? errorMap.get(receiverCode) : OoyalaErrorCode.ERROR_UNKNOWN;
  }
}
