package com.ooyala.android.imasdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.ooyala.android.DebugMode;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.item.OoyalaManagedAdSpot;

/**
 * The OoyalaPlayerIMAWrapper provides the interface between the OoyalaAdManager and the OoyalaPlayer.
 *
 * @author michael.len
 *
 */
class OoyalaPlayerIMAWrapper implements VideoAdPlayer {
  private static String TAG = "OoyalaPlayerIMAWrapper";

  final OoyalaPlayer _player;
  private final OoyalaIMAManager _imaManager;
  private OoyalaManagedAdSpot _adSpot;
  private boolean _isPlayingIMAAd;
  private final List<VideoAdPlayerCallback> _adCallbacks = new ArrayList<VideoAdPlayerCallback>(1);
  private int _liveContentTimePlayed;

  /**
   * A simple interface to allow for a callback when content is completed
   * @author michael.len
   *
   */
  interface CompleteCallback {
    void onComplete();
  }

  /**
   * Wrap an instantiated OoyalaPlayer to provide the IMA interface
   * @param player the OoyalaPlayer to use
   * @param callback a callback for when content is completed
   */
  public OoyalaPlayerIMAWrapper(OoyalaPlayer player, OoyalaIMAManager imaManager){
    DebugMode.logD(TAG, "IMA Ad Wrapper: Initializing");
    _player = player;
    _imaManager = imaManager;
    _isPlayingIMAAd = false;
    _liveContentTimePlayed = 0;
  }

  // Methods implementing VideoAdPlayer interface.
  @Override
  public void playAd() {
    DebugMode.logD(TAG, "IMA Ad Wrapper: Playing Ad");
    _imaManager._adPlayer.init(_player, _adSpot);
    _player.play();
    _isPlayingIMAAd = true;
  }

  @Override
  public void stopAd() {
    DebugMode.logD(TAG, "IMA Ad Wrapper: Stopping Ad");
    if(_isPlayingIMAAd && _player.isShowingAd()) {
      _player.suspend();
    }
    else {
      DebugMode.logI(TAG, "Stopping an ad when an IMA Ad isn't even playing!!");
    }
  }

  @Override
  public void loadAd(String url) {
    DebugMode.logD(TAG, "IMA Ad Wrapper: Loading Ad: " + url);
    _adSpot = new IMAAdSpot(url, _imaManager);
    _imaManager._adPlayer.setState(State.LOADING);
  }

  @Override
  public void pauseAd() {
    DebugMode.logD(TAG, "IMA Ad Wrapper: Pausing Ad");
    if(_isPlayingIMAAd && _player.isShowingAd()) {
      _player.pause();
    }
    else {
      DebugMode.logI(TAG, "Pausing an ad when an IMA Ad isn't even playing!!");
    }
  }

  @Override
  public void resumeAd() {
    DebugMode.logD(TAG, "IMA Ad Wrapper: Resuming Ad");
    if(_isPlayingIMAAd && _player.isShowingAd()) {
      _player.resume();
    }
    else {
      DebugMode.logI(TAG, "Resuming an ad when an IMA Ad isn't even playing!!");
    }
  }

  @Override
  public void addCallback(VideoAdPlayerCallback callback) {
    _adCallbacks.add(callback);
  }

  @Override
  public void removeCallback(VideoAdPlayerCallback callback) {
    _adCallbacks.remove(callback);
  }

  @Override
  public VideoProgressUpdate getProgress() {
    int durationMs = _player.getDuration();
    int playheadMs = _player.getPlayheadTime();

    if(!_isPlayingIMAAd) {
      playheadMs += _liveContentTimePlayed;
    }

    if (durationMs == 0) durationMs = Integer.MAX_VALUE;
    DebugMode.logV(TAG, "GetProgress time: " + playheadMs + ", duration: " + durationMs);
    return new VideoProgressUpdate(playheadMs, durationMs);
  }

  /**
   * Only called from the IMAManager when content should be paused. Note: This does not really pause content.
   * However, it informs the player wrapper that content will be paused. The Ad player pauses the content.
   */
  public void pauseContent(){
    if(_player.getCurrentItem().isLive()) {
      _liveContentTimePlayed = _liveContentTimePlayed + _player.getPlayheadTime();
    }
    _player.requestAdMode(_imaManager);
  }

  /**
   * Called when the IMAManager wants to resume content after advertisements.  This is how content is resumed
   * after IMA ads are played.
   */
  public void playContent(){
    for (VideoAdPlayerCallback callback : _adCallbacks) {
      callback.onPlay();
    }
    DebugMode.logE(TAG, "Destroy AdPlayer before play content");
    _player.exitAdMode(_imaManager);
  }

  /**
   * Called by OoyalaIMAManager when an error is encountered
   */
  public void onAdError() {
    fireIMAAdErrorCallback();
    _imaManager.destroy();
    _player.exitAdMode(_imaManager);
  }


  public void update(Observable arg0, Object arg) {
    OoyalaPlayer player = (OoyalaPlayer) arg0;
    String notification = arg.toString();

    //If playing a non-IMA ad
    if(!_isPlayingIMAAd && player.isShowingAd()) {

      //If starting a non-IMA Ad, we're pausing the content
      if (notification.equals(OoyalaPlayer.AD_STARTED_NOTIFICATION)) {
        DebugMode.logD(TAG, "IMA Ad Update: Non IMA ad playing");
        for (VideoAdPlayerCallback callback : _adCallbacks) {
          callback.onPause();
        }
      }
      //If completing a non-IMA ad
      else if (notification.equals(OoyalaPlayer.AD_COMPLETED_NOTIFICATION)) {
        DebugMode.logD(TAG, "IMA Ad Update: Non IMA ad completed");
      }
    }

    //If an IMA ad is playing while state is being changed
    else if (_isPlayingIMAAd){
      if(notification.equals(OoyalaPlayer.STATE_CHANGED_NOTIFICATION) && player.isShowingAd()) {
        switch (player.getState()) {
        case PLAYING:
          DebugMode.logD(TAG, "IMA Ad Update: Player Ad start");
          for (VideoAdPlayerCallback callback : _adCallbacks) {
            callback.onPlay();
          }
          break;
        case PAUSED:
          DebugMode.logD(TAG, "IMA Ad Update: Player Ad Pause");
          for (VideoAdPlayerCallback callback : _adCallbacks) {
            callback.onPause();
          }
          break;
        case SUSPENDED:
          DebugMode.logD(TAG, "IMA Ad Update: Player Ad Pause on Suspend");

          for (VideoAdPlayerCallback callback : _adCallbacks) {
            callback.onPause();
          }
          break;
        default:
          break;
        }
      }

      //If we get an AD_COMPLETE during an IMA ad, our ad has finished
      else if (notification.equals(OoyalaPlayer.AD_COMPLETED_NOTIFICATION)) {
        DebugMode.logD(TAG, "IMA Ad Update: Player Ad Complete");
        _isPlayingIMAAd = false;
        for (VideoAdPlayerCallback callback : _adCallbacks) {
          callback.onEnded();
        }
      }
      else if(notification.equals(OoyalaPlayer.CURRENT_ITEM_CHANGED_NOTIFICATION)) {
        for (VideoAdPlayerCallback callback : _adCallbacks) {
          callback.onEnded();
        }
      }
    }

    //Notifications from content playback
    else {
      if (notification.equals(OoyalaPlayer.STATE_CHANGED_NOTIFICATION)) {
        switch (player.getState()) {
        case PLAYING:
          DebugMode.logD(TAG, "IMA Ad Update: Player Content start");
          for (VideoAdPlayerCallback callback : _adCallbacks) {
            callback.onPlay();
          }
          break;
        case PAUSED:
          DebugMode.logD(TAG, "IMA Ad Update: Player Content Pause");
          for (VideoAdPlayerCallback callback : _adCallbacks) {
            callback.onPause();
          }
          break;
        case SUSPENDED:
          DebugMode.logD(TAG, "IMA Ad Update: Player Content Pause on Suspend");
          for (VideoAdPlayerCallback callback : _adCallbacks) {
            callback.onPause();
          }
          break;
        default:
          break;
        }
      }
    }
  }

  /**
   * Fire callback to IMASDK when a video(ima-ad or content) starts
   */
  public void fireVideoStartCallback() {
    if (_player.isShowingAd()) {
      DebugMode.logD(TAG, "IMASDK callback fired: Player Ad start");
    } else {
      DebugMode.logD(TAG, "IMASDK callback fired: Content start");
    }

    for (VideoAdPlayerCallback callback : _adCallbacks) {
      callback.onPlay();
    }
  }

  /**
   * Fire callback to IMASDK when a video(ima-ad or content) pauses
   */
  public void fireVideoPauseCallback() {
    if (_player.isShowingAd()) {
      DebugMode.logD(TAG, "IMASDK callback fired: Player Ad pauses");
    } else {
      DebugMode.logD(TAG, "IMASDK callback fired: Content pauses");
    }
    for (VideoAdPlayerCallback callback : _adCallbacks) {
      callback.onPause();
    }
  }

  /**
   * Fire callback to IMASDK when a video(ima-ad or content) suspends
   */
  public void fireVideoSuspendCallback() {
    if (_player.isShowingAd()) {
      DebugMode.logD(TAG, "IMASDK callback fired: Player Ad suspends");
    } else {
      DebugMode.logD(TAG, "IMASDK callback fired: Content suspends");
    }
    for (VideoAdPlayerCallback callback : _adCallbacks) {
      callback.onPause();
    }
  }

  /**
   * Fire callback to IMASDK when current item changed
   */
  public void fireCurrentItemChangedCallback() {
    DebugMode.logD(TAG, "IMASDK callback fired: Current item changed");
    for (VideoAdPlayerCallback callback : _adCallbacks) {
      callback.onEnded();
    }
  }

  /**
   * Fire callback to IMASDK when IMA-Ad complete
   */
  public void fireIMAAdCompleteCallback() {
    DebugMode.logD(TAG, "IMASDK callback: Player Ad Complete");
    _isPlayingIMAAd = false;
    for (VideoAdPlayerCallback callback : _adCallbacks) {
      callback.onEnded();
      _imaManager._adPlayer.destroy();
    }
  }

  /**
   * Fire callback to IMASDK when IMA-Ad resume
   */
  public void fireIMAAdResumeCallback() {
    DebugMode.logD(TAG, "IMASDK callback: Player Ad Resume");
    for (VideoAdPlayerCallback callback : _adCallbacks) {
      callback.onResume();
    }
  }

  /**
   * Fire callback to IMASDK when IMA-Ad resume
   */
  public void fireIMAAdErrorCallback() {
    DebugMode.logD(TAG, "IMASDK callback: Player Ad on Error");
    for (VideoAdPlayerCallback callback : _adCallbacks) {
      callback.onError();
    }
  }
}
