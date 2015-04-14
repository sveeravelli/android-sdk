package com.ooyala.android.castsdk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import com.ooyala.android.CastPlayer;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.util.DebugMode;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.Observable;

import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGE;

public class OOCastPlayer extends Observable implements CastPlayer {
  private static final String TAG = "OOCastPlayer";
 
  private static OOCastManager castManager;
  private OoyalaPlayer ooyalaPlayer;
  
  private String embedCode;
  private JSONObject contentTreeMsg;
  private int duration;
  private int currentTime;
  private State state = State.INIT;

  private int playheadToResume;
  private boolean autoPlayerWhenResume;
  private  boolean suspended;

  private boolean isSeeking;
  private boolean seekable;
 
  // Related info for current content
  private String castItemTitle;
  private String castItemDescription;
  private String castItemPromoImg;
  private Bitmap castImageBitmap;
  private View castView;
  
  
  public OOCastPlayer(OOCastManager castManager, OoyalaPlayer ooyalaPlayer) {
    this.ooyalaPlayer = ooyalaPlayer;
    OOCastPlayer.castManager = castManager;
  }
  
  /*============================================================================================*/
  /*========== CastPlayer Controls =======================================================*/
  /*============================================================================================*/
  
  @Override
  public void pause() {
    DebugMode.logD(TAG, "pause()");
    setState(State.PAUSED);
    sendMessage(OOCastUtils.makeActionJSON("pause"));
  }

  @Override
  public void play() {
    DebugMode.logD(TAG, "play()");
    setState(State.PLAYING);
    sendMessage(OOCastUtils.makeActionJSON("play"));
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

  public void setDuration(int duration) {
    this.duration = duration;
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
  
  protected void setState(State state) {
    this.state = state;
    setChanged();
    castManager.updateMiniControllersState();
    castManager.updateNotificationAndLockScreenPlayPauseButton();
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
  
  public void setCastView(View view) {
    clearCastView();
    castView = view;
  }
  
  public void clearCastView() {
    if (ooyalaPlayer != null && ooyalaPlayer.getLayout().getChildCount() != 0) {
      ooyalaPlayer.getLayout().removeView(castView);
      ooyalaPlayer.getLayout().removeAllViews();
    }
    castView = null;
  }
  
  public void displayCastView() {
    DebugMode.logD(TAG, "Display castView if avaiable");
    if (ooyalaPlayer != null && castView != null) {
      if (ooyalaPlayer.getLayout().getChildCount() != 0) {
        ooyalaPlayer.getLayout().removeView(castView);
        ooyalaPlayer.getLayout().removeAllViews();
      }
      if (castView.getParent() == null) {
        ooyalaPlayer.getLayout().addView(castView);
      }
    }
  }

  /*============================================================================================*/
  /*========== CastPlayer Receiver related =====================================================*/
  /*============================================================================================*/

  public void initReceiverPlayer(String embedCode, int playheadTimeInMillis, boolean isPlaying) {
    DebugMode.logD(TAG, "On Cast Mode Entered with playhead time: " + playheadTimeInMillis + ", isPlaying: "
        + isPlaying);
    if (initWithTheCastingContent(embedCode)) {
      getReceiverPlayerState(); // for updating UI controls
      displayCastView();
      return;
    }
    this.embedCode = embedCode;
    updateMetadataFromOoyalaPlayer(ooyalaPlayer);
    displayCastView();
    String initialPlayMessage = initializePlayerParams(embedCode, null, playheadTimeInMillis, isPlaying);
    sendMessage(initialPlayMessage);
    setCurrentTime(playheadTimeInMillis);
  }

  private boolean initWithTheCastingContent(String embedCode) {
    return this.embedCode != null && this.embedCode.equals(embedCode);
  }
  
  private String initializePlayerParams(String ec, String version, int playheadTimeInMillis, boolean isPlaying) {
    float playheadTime = playheadTimeInMillis / 1000;
    JSONObject playerParams = new JSONObject();
    JSONObject dataParams = new JSONObject();
    JSONObject wrap = new JSONObject();
    try {
      playerParams.put("initialTime", playheadTime);
      if (isPlaying) {
        playerParams.put("autoplay", true);
      } else {
        playerParams.put("autoplay", false);
      }
      dataParams.put("ec", ec);
      dataParams.put("version", version);
      dataParams.put("params", playerParams.toString());
      if (castItemTitle != null || castItemDescription != null || castItemPromoImg != null) {
        dataParams.put("title", castItemTitle);
        dataParams.put("description", castItemDescription);
        dataParams.put("promo_url", castItemPromoImg);
      } else {
        DebugMode.logE(TAG, "Title or description or PromoImage is null!!");
      }

      wrap.put("action", "init");
      wrap.put("data", dataParams);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return wrap.toString();
  }
  
  private void updateMetadataFromOoyalaPlayer(OoyalaPlayer player) {
    if (player != null) {
      castItemPromoImg = player.getCurrentItem().getPromoImageURL(2000, 2000);
      castItemTitle = player.getCurrentItem().getTitle();
      castItemDescription = player.getCurrentItem().getDescription();
      seekable = ooyalaPlayer.seekable();
      loadIcon();
    } else {
      DebugMode.logD(TAG, "OoyalaPlayer returns null when updateMetadata()");
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
          castImageBitmap = castManager.getDefaultMiniControllerImageBitmap();
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
      castManager.sendDataMessage(message);
    }  catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private void getReceiverPlayerState() {
    DebugMode.logD(TAG, "getReceiverPlayerState");
    sendMessage(OOCastUtils.makeActionJSON("getstatus"));
  }
  
  public void receivedMessage(String message) {
    try {
      JSONObject msg = new JSONObject(message);
      
      // The key for "State Message" is different from other messages
      // So we should check it separately
      DebugMode.logD(TAG, "Received Message: " + message);
      if (msg.has("state")) {
        String state = msg.getString("state");
        if (state.equals("playing")) {
          play();
          setState(State.PLAYING);
        } else if (state.equals("paused")) {
          pause();
          setState(State.PAUSED);
        } else if (state.equals("loading")) {
          setState(State.LOADING);
        } else if (state.equals("ready")) {
          setState(State.READY);
        } else if (state.equals("error")) {
          setState(State.ERROR);
        } 
        if (msg.has("playhead")) {
          JSONObject playheadInfo = new JSONObject( msg.getString("playhead"));
          if (playheadInfo.has("1")) {
            String currentTime = playheadInfo.getString("1");
            setCurrentTime((int) (Double.parseDouble(currentTime) * 1000));
          }
        }
      }
      if (msg.has("0")) {
        String eventType = msg.getString("0");
        if (eventType.equalsIgnoreCase("playheadTimeChanged")) {
          String currentTime = msg.getString("1");
          setCurrentTime((int) (Double.parseDouble(currentTime) * 1000));
          onPlayHeadChanged();
          String duration = msg.getString("2");
          setDuration((int) Double.parseDouble(duration) * 1000);
        } 
        else if (eventType.equalsIgnoreCase("downloading")) {
          String duration = msg.getString("2");
          setDuration((int) Double.parseDouble(duration) * 1000);
        } 
        else if (eventType.equalsIgnoreCase("playing") || eventType.equalsIgnoreCase("streamPlaying")) {
          setState(State.PLAYING);
        } 
        else if (eventType.equalsIgnoreCase("paused")) {
          setState(State.PAUSED);
        }
        else if (eventType.equalsIgnoreCase("contentTreeFetched")) {
          contentTreeMsg = msg.getJSONObject("1");
          String embedCode = contentTreeMsg.getString("embed_code");
          if (this.embedCode != null && !this.embedCode.equals(embedCode)) {
            // current content has been override on receiver side. keep play current content on content mode
            castManager.disconnectDevice(false, true, true);
          }
        }
        else if (eventType.equalsIgnoreCase("playbackReady")) {
          onPlayHeadChanged();
          setState(State.READY);
          getReceiverPlayerState();
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
    if (!shouldResumeToCastMode()) {
      ooyalaPlayer.exitCastMode(currentTime, state, embedCode);
    } else if (suspended) {
      suspended = false;
      initReceiverPlayer(embedCode, playheadToResume, autoPlayerWhenResume);
    }
  }

  private boolean shouldResumeToCastMode() {
    return castManager != null && castManager.isConnected();
  }

  @Override
  public void stop() {
  }
  
  @Override
  public void reset() {
  }

  /**
   * Only called from OOCastManager.createNewCastPlayer
   */
  @Override
  public void suspend() {
    autoPlayerWhenResume = (getState() == State.PLAYING);
    playheadToResume = currentTime;
    suspended = true;
  }

  @Override
  public void resume(int timeInMilliSecond, State stateToResume) {
  }
  
  @Override
  public void destroy() {
  }

  public int livePlayheadPercentage() {
    return 0;
  }

  public void seekToPercentLive(int percent) {

  }
}
