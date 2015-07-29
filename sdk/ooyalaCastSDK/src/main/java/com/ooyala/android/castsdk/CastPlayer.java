package com.ooyala.android.castsdk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ooyala.android.CastModeOptions;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.player.PlayerInterface;
import com.ooyala.android.plugin.LifeCycleInterface;
import com.ooyala.android.util.DebugMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;

import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGE;

public class CastPlayer extends Observable implements PlayerInterface, LifeCycleInterface {
  private static final String TAG = "OOCastPlayer";
  private String RECEIVER_LIVE_LANGUAGE = "live";
  private String RECEIVER_DISABLE_LANGUAGE = "";

  private WeakReference<CastManager> castManager;
  
  private String embedCode;
  private int duration;
  private int currentTime;
  private State state = State.INIT;

  private boolean isSeeking;
  private boolean seekable;

  private boolean isLiveClosedCaptionsAvailable;

  // Related info for current content
  private String castItemTitle;
  private String castItemDescription;
  private String castItemPromoImg;

  private Bitmap castImageBitmap;
  
  
  public CastPlayer(CastManager castManager) {
    this.castManager = new WeakReference<CastManager>(castManager);
  }

  public void setOoyalaPlayer(OoyalaPlayer ooyalaPlayer) {
    DebugMode.logD(TAG, "Set OoyalaPlayer = " + ooyalaPlayer);
    this.addObserver(ooyalaPlayer);
    updateMetadataFromOoyalaPlayer(ooyalaPlayer);
  }

  public void disconnectFromCurrentOoyalaPlayer() {
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

  private void setCurrentTime(int curTime) {
    currentTime = curTime;
    onPlayHeadChanged();
  }

  @Override
  public int duration() {
    return duration;
  }

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

  public void syncDeviceVolumeToTV() {
    DebugMode.logD(TAG, "SyncDeviceVolumeToTV");
    JSONObject actionSetVolume = new JSONObject();
    try {
      actionSetVolume.put("action", "volume");
      actionSetVolume.put("data", castManager.get().getDeviceVolume());
      sendMessage(actionSetVolume.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  protected void setState(State state) {
    this.state = state;
    castManager.get().updateMiniControllersState();
    castManager.get().updateNotificationAndLockScreenPlayPauseButton();
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
  public boolean isLiveClosedCaptionsAvailable() {
    return isLiveClosedCaptionsAvailable;
  }

  public String getEmbedCode() {
    return embedCode;
  }
  
  public String getCastItemTitle() {
    return castItemTitle;
  }

  public String getCastItemDescription() {
    return castItemDescription;
  }

  public String getCastItemPromoImgUrl() {
    return castItemPromoImg;
  }
  
  public Bitmap getCastImageBitmap() {
    return castImageBitmap;
  }

  /*============================================================================================*/
  /*========== CastPlayer Receiver related =====================================================*/
  /*============================================================================================*/

  public void enterCastMode(CastModeOptions options, String embedToken, Map<String, String> additionalInitParams) {
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
  
  public void updateMetadataFromOoyalaPlayer(OoyalaPlayer player) {
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
          LOGE(TAG, "setIcon(): Failed to load the image with url: " + castItemPromoImg + ", using the default one",
              e);
          castImageBitmap = castManager.get().getDefaultMiniControllerImageBitmap();
        }
      }
    }).start();
  }

  private void onPlayHeadChanged() {
    setChanged();
    notifyObservers(OoyalaPlayer.TIME_CHANGED_NOTIFICATION);
  }
  
  private void sendMessage(final String message) {
    try {
      DebugMode.logD(TAG, "Sending Message: " + message);
      castManager.get().sendDataMessage(message);
    }  catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private void getReceiverPlayerState() {
    DebugMode.logD(TAG, "getReceiverPlayerState");
    sendMessage(CastUtils.makeActionJSON("getstatus"));
  }
  
  public void receivedMessage(String message) {
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
            // current content has been override on receiver side. keep play current content on content mode
            castManager.get().disconnectDevice(false, true, true);
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

  public int livePlayheadPercentage() {
    return 0;
  }

  public void seekToPercentLive(int percent) {

  }
}
