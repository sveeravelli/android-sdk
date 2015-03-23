package com.ooyala.android.imasdk;

import com.google.ads.interactivemedia.v3.api.player.ContentProgressProvider;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.item.OoyalaManagedAdSpot;
import com.ooyala.android.util.DebugMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * The OoyalaPlayerIMAWrapper provides the interface between the OoyalaAdManager and IMA SDK.
 * IMA SDK and OoyalaIMAManager manage the playback through OoyalaPlayerIMAWrapper, including
 * loading ads, play/pause ads playback, and sync playback events.
 *
 */
class OoyalaPlayerIMAWrapper implements VideoAdPlayer, ContentProgressProvider {
  private static String TAG = "OoyalaPlayerIMAWrapper";

  final OoyalaPlayer _player;
  private final OoyalaIMAManager _imaManager;
  private OoyalaManagedAdSpot _adSpot;
  private final List<VideoAdPlayerCallback> _adCallbacks = new ArrayList<VideoAdPlayerCallback>(1);
  private VideoProgressCalculator _videoProgressCalculator;

  /**
   * A simple interface to allow for a callback when content is completed
   *
   */
  interface CompleteCallback {
    void onComplete();
  }

  /**
   * Wrap an instantiated OoyalaPlayer to provide the IMA interface
   * @param player the OoyalaPlayer to use
   * @param imaManager the current OoyalaIMAManager
   */
  public OoyalaPlayerIMAWrapper(OoyalaPlayer player, OoyalaIMAManager imaManager){
    DebugMode.logD(TAG, "IMA Ad Wrapper: Initializing");
    _player = player;
    _imaManager = imaManager;
    _videoProgressCalculator = new VideoProgressCalculator( _player, false, 0, 0 );
  }

  private VideoProgressCalculatorRunningState getVideoProgressState() {
    return _videoProgressCalculator.getRunningState();
  }

  // Methods implementing VideoAdPlayer interface.

  /**
   * Play or resume IMA Ads playback.
   * This method will be called from IMA SDK when it wants to play/resume IMA Ads playback
   */
  @Override
  public void playAd() {
    DebugMode.logD(TAG, "IMA Ad Wrapper: Playing Ad");
    _imaManager._adPlayer.playIMA();
    getVideoProgressState().setPlayingIMAAd( true );
  }

  /**
   * Stop IMA Ads playback.
   * This method will be called from IMA SDK when it wants to stop IMA Ads playback
   */
  @Override
  public void stopAd() {
    DebugMode.logD(TAG, "IMA Ad Wrapper: Stopping Ad");
    if(getVideoProgressState().isPlayingIMAAd() && _player.isShowingAd()) {
      _player.suspend();
    }
    else {
      DebugMode.logI(TAG, "Stopping an ad when an IMA Ad isn't even playing!!");
    }
  }

  /**
   * Load the given url into IMAAdPlayer for playback
   * @param url the ad url to be played
   */
  @Override
  public void loadAd(String url) {
    DebugMode.logD(TAG, "IMA Ad Wrapper: Loading Ad: " + url);
    _adSpot = new IMAAdSpot(url, _imaManager);
    _imaManager._adPlayer.init(_player, _adSpot, _player.createStateNotifier());
  }

  /**
   * Pause IMA Ads playback
   * This method will be called from IMA SDK when it wants to pause IMA Ads playback
   */
  @Override
  public void pauseAd() {
    DebugMode.logD(TAG, "IMA Ad Wrapper: Pausing Ad");
    if(getVideoProgressState().isPlayingIMAAd() && _player.isShowingAd() && _imaManager._adPlayer != null ) {
      _imaManager._adPlayer.pauseIMA();
    }
    else {
      DebugMode.logI(TAG, "Pausing an ad when an IMA Ad isn't even playing!!");
    }
  }

  /**
   * Resume ads playback from suspended state
   * This method will be called from IMA SDK
   */
  @Override
  public void resumeAd() {
    DebugMode.logD(TAG, "IMA Ad Wrapper: Resuming Ad");
    if(getVideoProgressState().isPlayingIMAAd() && _player.isShowingAd()) {
      _player.resume();
    }
    else {
      DebugMode.logI(TAG, "Resuming an ad when an IMA Ad isn't even playing!!");
    }
  }

  /**
   * Add a callback to IMA  VideoAdPlayer.VideoAdPlayerCallback
   * @param callback the callback to be added
   */
  @Override
  public void addCallback(VideoAdPlayerCallback callback) {
    _adCallbacks.add(callback);
  }

  /**
   * Remove a callback to IMA  VideoAdPlayer.VideoAdPlayerCallback
   * @param callback the callback to be added
   */
  @Override
  public void removeCallback(VideoAdPlayerCallback callback) {
    _adCallbacks.remove(callback);
  }

  /**
   * Get current VideoProgressUpdate of content video
   * @return current VideoProgressUpdate
   */
  @Override
  public VideoProgressUpdate getContentProgress() {
    return _videoProgressCalculator.getContentProgress();
  }

  /**
   * Get current VideoProgressUpdate of current ads
   * @return current VideoProgressUpdate
   */
  @Override
  public VideoProgressUpdate getAdProgress() {
    return _videoProgressCalculator.getAdProgress();
  }

  /**
   * Only called from the IMAManager when content should be paused. Note: This does not really pause content.
   * However, it informs the player wrapper that content will be paused. The Ad player pauses the content.
   */
  public void pauseContent(){
    if(_player.getCurrentItem().isLive()) {
      getVideoProgressState().incrementLiveContentTimePlayed( _player.getPlayheadTime() );
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
    DebugMode.logD(TAG, "Destroy AdPlayer before play content");
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

  /**
   * Update ad playback events and state changes
   * @param arg0
   * @param arg
   */
  public void update(Observable arg0, Object arg) {
    OoyalaPlayer player = (OoyalaPlayer) arg0;
    String notification = arg.toString();

    //If playing a non-IMA ad
    if(!getVideoProgressState().isPlayingIMAAd() && player.isShowingAd()) {

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
    else if (getVideoProgressState().isPlayingIMAAd()){
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
        getVideoProgressState().setPlayingIMAAd( false );
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
    getVideoProgressState().setPlayingIMAAd( false );
    for (VideoAdPlayerCallback callback : _adCallbacks) {
      callback.onEnded();
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
