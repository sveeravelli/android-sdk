package com.ooyala.android.castsdk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.ooyala.android.CastModeOptions;
import com.ooyala.android.OoyalaException;
import com.ooyala.android.OoyalaException.OoyalaErrorCode;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.player.PlayerInterface;
import com.ooyala.android.plugin.LifeCycleInterface;
import com.ooyala.android.util.DebugMode;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Arrays;
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

  private WeakReference<CastManager> castManager;
  
  private String embedCode;
  private int duration;
  private int currentTime;
  private State state = State.INIT;
  private OoyalaException error;

  private boolean isSeeking;
  private boolean seekable;

  private boolean isLiveClosedCaptionsAvailable;

  // Related info for current content
  private String castItemTitle;
  private String castItemDescription;
  private String castItemPromoImg;

  private Bitmap castImageBitmap;

  /*package private on purpose*/ CastPlayer(CastManager castManager) {
    this.castManager = new WeakReference<CastManager>(castManager);
  }

  /*package private on purpose*/ void setOoyalaPlayer(OoyalaPlayer ooyalaPlayer) {
    DebugMode.logD(TAG, "Set OoyalaPlayer = " + ooyalaPlayer);
    this.addObserver(ooyalaPlayer);
    updateMetadataFromOoyalaPlayer(ooyalaPlayer);
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
    setState(State.PAUSED);
    sendMessage(CastUtils.makeActionJSON("pause"));
  }

  @Override
  public void play() {
    DebugMode.logD(TAG, "play()");
    setState(State.PLAYING);
    sendMessage(CastUtils.makeActionJSON("play"));
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
    if (!isSeeking) {
      setState(OoyalaPlayer.State.LOADING);
    }
    isSeeking = true;
    JSONObject actionSeek = new JSONObject();
    try {
      actionSeek.put("action", "seek");
      actionSeek.put("data", String.valueOf(timeInMillis / 1000));
    } catch (JSONException e1) {
      e1.printStackTrace();
    }
    sendMessage(actionSeek.toString());
    setCurrentTime(timeInMillis);
    onPlayHeadChanged();
  }

  /*package private on purpose*/ void syncDeviceVolumeToTV() {
    DebugMode.logD(TAG, "SyncDeviceVolumeToTV");
    new RunWithWeakCastManager(castManager) {
      @Override
      protected void run( CastManager cm ) {
        JSONObject actionSetVolume = new JSONObject();
        try {
          actionSetVolume.put( "action", "volume" );
          actionSetVolume.put( "data", cm.getDataCastManager().getDeviceVolume() );
          sendMessage( actionSetVolume.toString() );
        }
        catch( Exception e ) {
          e.printStackTrace();
        }
      }
    }.safeRun();
  }
  
  private void setState(State state) {
    this.state = state;
    new RunWithWeakCastManager( castManager ) {
      @Override
      public void run( CastManager cm ) {
        cm.updateMiniControllers();
        cm.updateNotificationAndLockScreenPlayPauseButton();
      }
    }.safeRun();
    setChanged();
    notifyObservers(OoyalaPlayer.STATE_CHANGED_NOTIFICATION);
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
      sendMessage(actionSetVolume.toString());
    } catch (Exception e) {
      e.printStackTrace();
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
    return embedCode;
  }

  /**
   * @return the current asset title. Possibly null.
   */
  public String getCastItemTitle() {
    return castItemTitle;
  }

  /**
   * @return the current asset description. Possibly null.
   */
  public String getCastItemDescription() {
    return castItemDescription;
  }

  /**
   * @return the current asset promo image url. Possibly null.
   */
  public String getCastItemPromoImgUrl() {
    return castItemPromoImg;
  }

  /**
   * @return the current asset casting image. Possibly null.
   */
  public Bitmap getCastImageBitmap() {
    return castImageBitmap;
  }

  /*============================================================================================*/
  /*========== CastPlayer Receiver related =====================================================*/
  /*============================================================================================*/

  /*package private on purpose*/ void enterCastMode(CastModeOptions options, String embedToken, Map<String, String> additionalInitParams) {
    DebugMode.logD(TAG, "On Cast Mode Entered with embedCode " + options.getEmbedCode());
    if (initWithTheCastingContent(options.getEmbedCode())) {
      getReceiverPlayerState(); // for updating UI controls
    } else {
      resetStateOnVideoChange();
      this.embedCode = options.getEmbedCode();
      String initialPlayMessage = initializePlayerParams(options, embedToken, additionalInitParams);
      sendMessage(initialPlayMessage);
      setCurrentTime(options.getPlayheadTimeInMillis());
    }
  }

  private void resetStateOnVideoChange() {
    isLiveClosedCaptionsAvailable = false;
  }

  private boolean initWithTheCastingContent(String embedCode) {
    return this.embedCode != null && this.embedCode.equals(embedCode);
  }
  
  private String initializePlayerParams(CastModeOptions options, String embedToken, Map<String, String> additionalInitParams) {
    float playheadTime = options.getPlayheadTimeInMillis() / 1000;
    JSONObject playerParams = new JSONObject();
    JSONObject dataParams = new JSONObject();
    JSONObject wrap = new JSONObject();
    try {
      playerParams.put("initialTime", playheadTime);
      if (options.isPlaying()) {
        playerParams.put("autoplay", true);
      } else {
        playerParams.put("autoplay", false);
      }

      if (embedToken != null) {
        playerParams.put("embedToken", embedToken);
      }

      if (options.getCCLanguage() != null) {
        playerParams.put("ccLanguage", convertClosedCaptionsLanguageForReceiver(options.getCCLanguage()));
      }

      if (options.getAuthToken() != null) {
        playerParams.put("authToken", options.getAuthToken());
      }

      dataParams.put("ec", options.getEmbedCode());
      dataParams.put("version", null);
      dataParams.put("params", playerParams.toString());
      if (castItemTitle != null || castItemDescription != null || castItemPromoImg != null) {
        dataParams.put("title", castItemTitle);
        dataParams.put("description", castItemDescription);
        dataParams.put("promo_url", castItemPromoImg);
      } else {
        DebugMode.logE(TAG, "Title or description or PromoImage is null!!");
      }

      // Iterate through additionalInitParams (Overrides anything set by default in the init)
      if (additionalInitParams != null) {
        Iterator paramsIterator = additionalInitParams.entrySet().iterator();
        while (paramsIterator.hasNext()) {
          Map.Entry<String, String> entry = (Map.Entry) paramsIterator.next();
          dataParams.put(entry.getKey(), entry.getValue());
        }
      }

      wrap.put("action", "init");
      wrap.put("data", dataParams);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return wrap.toString();
  }

  /*package private on purpose*/ void updateMetadataFromOoyalaPlayer(OoyalaPlayer player) {
    if (player != null && player.getCurrentItem() != null) {
      castItemPromoImg = player.getCurrentItem().getPromoImageURL(2000, 2000);
      castItemTitle = player.getCurrentItem().getTitle();
      castItemDescription = player.getCurrentItem().getDescription();
      seekable = player.seekable();
      loadIcon();
    } else {
      DebugMode.logD(TAG, "OoyalaPlayer or currentItem returns null when updateMetadata()");
    }
  }
  
  private void loadIcon() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          URL imgUrl = new URL(castItemPromoImg.toString());
          castImageBitmap = BitmapFactory.decodeStream(imgUrl.openStream());
        } catch (Exception e) {
          DebugMode.logE(TAG, "setIcon(): Failed to load the image with url: " + castItemPromoImg + ", trying the default one",
              e);
          new RunWithWeakCastManager( castManager ) {
            @Override
            public void run( CastManager cm ) {
              castImageBitmap = cm.getDefaultMiniControllerImageBitmap();
            }
          }.safeRun();
        }
      }
    }).start();
  }

  private void onPlayHeadChanged() {
    setChanged();
    notifyObservers(OoyalaPlayer.TIME_CHANGED_NOTIFICATION);
  }
  
  private void sendMessage(final String message) {
    DebugMode.logD( TAG, "Sending Message: " + message );
    new RunWithWeakCastManager( castManager ) {
      @Override
      public void run( CastManager cm ) {
        try {
          cm.sendDataMessage( message );
        }
        catch( Exception e ) {
          e.printStackTrace();
        }
      }
    }.safeRun();
  }

  private void getReceiverPlayerState() {
    DebugMode.logD(TAG, "getReceiverPlayerState");
    sendMessage(CastUtils.makeActionJSON("getstatus"));
  }

  /*package private on purpose*/ void receivedMessage(String message) {
    try {
      JSONObject msg = new JSONObject(message);
      
      // The key for "State Message" is different from other messages
      // So we should check it separately
      if (msg.has("state")) {
        String state = msg.getString("state");
        DebugMode.logD(TAG, "Received State: " + state);
        if (state.equals("playing")) {
          setState(State.PLAYING);
        } else if (state.equals("paused")) {
          setState(State.PAUSED);
        } else if (state.equals("loading")) {
          setState(State.LOADING);
        } else if (state.equals("buffering")) {
          setState(State.LOADING);
        } else if (state.equals("ready")) {
          setState(State.READY);
        } else if (state.equals("error")) {
          setState(State.ERROR);
        }
      }
      if (msg.has("0")) {
        String eventType = msg.getString("0");
        if (!eventType.equals("downloading") && !eventType.equals("playheadTimeChanged")) {
          DebugMode.logD(TAG, "Received event: " + msg);
        }
        if (eventType.equalsIgnoreCase("playheadTimeChanged")) {
          String currentTime = msg.getString("1");
          setCurrentTime((int) (Double.parseDouble(currentTime) * 1000));
          onPlayHeadChanged();
          String duration = msg.getString("2");
          this.duration = ((int) Double.parseDouble(duration) * 1000);
        }
        else if (eventType.equalsIgnoreCase("buffering")) {
          setState(State.LOADING);
        }
        else if (eventType.equalsIgnoreCase("playing") || eventType.equalsIgnoreCase("streamPlaying")) {
          setState(State.PLAYING);
        } 
        else if (eventType.equalsIgnoreCase("paused")) {
          setState(State.PAUSED);
        }
        else if (eventType.equalsIgnoreCase("contentTreeFetched")) {
          String embedCode =  msg.getJSONObject("1").getString("embed_code");
          DebugMode.logD(TAG, "Disconnect from chromecast and exit cast mode because a different content is casting");
          if (this.embedCode != null && !this.embedCode.equals(embedCode)) {
            new RunWithWeakCastManager( castManager ) {
              @Override
              public void run( CastManager cm ) {
                // current content has been override on receiver side. keep play current content on content mode
                cm.getDataCastManager().disconnectDevice( false, true, true );
              }
            }.safeRun();
          }
        }
        else if (eventType.equalsIgnoreCase("playbackReady")) {
          onPlayHeadChanged();
          syncDeviceVolumeToTV();
          setState(State.READY);
          getReceiverPlayerState();
        }
        else if (eventType.equalsIgnoreCase("closedCaptionsInfoAvailable")) {
          //TODO: Need to check if the info available is "live"
          String language = msg.getJSONObject("1").getString("lang");
          if (RECEIVER_LIVE_LANGUAGE.equals(language)) {
            isLiveClosedCaptionsAvailable = true;
          }
        }
        else if (eventType.equalsIgnoreCase("played")) {
          setCurrentTime(0);
          setState(State.COMPLETED);
        } 
        else if (eventType.equalsIgnoreCase("seeked")) {
          isSeeking = false;
          getReceiverPlayerState();
        } else if (eventType.equalsIgnoreCase("error")) {
          String receiverCode = msg.getJSONObject("1").getString("code");
          this.error = new OoyalaException(getOoyalaErrorCodeForReceiverCode(receiverCode), "Error from Cast Receiver: " + receiverCode);
          setState(State.ERROR);
        }
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
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

  private abstract static class RunWithWeakCastManager {
    private final WeakReference<CastManager> wcm;
    protected RunWithWeakCastManager( WeakReference<CastManager> wcm ) {
      this.wcm = wcm;
    }
    public void safeRun() {
      final CastManager cm = wcm.get();
      if( cm != null ) {
        run( cm );
      }
      else {
        DebugMode.logE( TAG, "Weak CastManager was null. Stack = " + Arrays.toString(Thread.currentThread().getStackTrace()) );
      }
    }
    protected abstract void run( CastManager cm );
  }
}
