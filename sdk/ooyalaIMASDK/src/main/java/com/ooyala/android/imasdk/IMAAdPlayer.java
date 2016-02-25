package com.ooyala.android.imasdk;


import com.ooyala.android.AdPodInfo;
import com.ooyala.android.OoyalaException;
import com.ooyala.android.OoyalaException.OoyalaErrorCode;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.StateNotifier;
import com.ooyala.android.item.AdSpot;
import com.ooyala.android.player.AdMoviePlayer;
import com.ooyala.android.util.DebugMode;

import java.util.Observable;

/**
 * This class represents the Base Movie Player that plays IMA Ad spots.
 * And it is also a interface between OoyalaPlayer and IMA SDK for IMA ads playabck
 *
 */
public class IMAAdPlayer extends AdMoviePlayer {
  private static String TAG = IMAAdPlayer.class.getSimpleName();
  private AdSpot _ad;
  private OoyalaIMAManager _imaManager;


  /**
   * Initialize an IMAAdPlayer
   * @param parent an current OoyalaPlayer
   * @param ad an AdSpot that holds the current ads information
   * @param notifier a notifier which sends notification to OoyalaPlayer or UI Controls for state change or playback events
   */
  @Override
  public void init(final OoyalaPlayer parent, AdSpot ad, StateNotifier notifier) {
    super.init(parent, ad, notifier);
    DebugMode.logD(TAG, "init()");
    if ( ! (ad instanceof IMAAdSpot) ) {
      this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Invalid Ad");
      setState(State.ERROR);
    } else if (((IMAAdSpot)ad).getImaManager()._onAdError) {
      setState(State.COMPLETED);
    } else {
      _seekable = false;
      _ad = ad;
      super.init(parent, ((IMAAdSpot)_ad).getStreams());
    }
  }

  /**
   * Start or resume the current ad playback.
   * This "play" is called from OoyalaPlayer.play() and also responses to UI play button click.
   */
  @Override
  public void play() {
    DebugMode.logD(TAG, "play(): Playing indirectly through AdsManager");
    if (_imaManager != null && _imaManager._adsManager != null) {
      _imaManager._adsManager.resume();
    } else {
      DebugMode.logD(TAG, "play() ignored due to null adsManager");
    }
  }

  /**
   * Start or resume the current ad playback.
   * This "playIMA" is called from OoyalaPlayerIMAWrapper.playAd() when IMA SDK wants to play/resume
   * the ad playback.
   */
  public void playIMA() {
    if (_ad != null) {
      // We do not update the State to PLAYING until we hear the callback from IMA SDK
      // since there could be a while between message sent and callback received
      DebugMode.logD(TAG, "playIMA(): Playing");
      String title = _imaManager._adsManager.getCurrentAd().getTitle();
      String description = _imaManager._adsManager.getCurrentAd().getDescription();
      String url = "";
      int adsCount = 1;
      int unplayedCount = adsCount - 1;
      _notifier.notifyAdStartWithAdInfo(new AdPodInfo(title, description, url, adsCount, unplayedCount, false, false));
      super.play();
      if (_imaManager != null && _imaManager._ooyalaPlayerWrapper != null) {
        _imaManager._ooyalaPlayerWrapper.fireVideoStartCallback();
      } else {
        DebugMode.logD(TAG, "playIMA() ignored due to null adsManager");
      }
    }
  }

  /**
   * Pause the current ad playback.
   * This "pause" is called from OoyalaPlayer.pause() and also responses to UI pause button click.
   */
  @Override
  public void pause() {
    DebugMode.logD(TAG, "pause(): Pausing indirectly through AdsManager");
    if (_imaManager != null && _imaManager._adsManager != null) {
      _imaManager._adsManager.pause();
    } else {
      DebugMode.logD(TAG, "pause() ignored due to null adsManager");
    }
  }

  /**
   * Pause the current ad playback.
   * This "pauseIMA" is called from OoyalaPlayerIMAWrapper.pauseAd() when IMA SDK wants to pause
   * the ad playback.
   */
  public void pauseIMA() {
    DebugMode.logD(TAG, "pauseIMA(): Pausing");
    super.pause();
    if (_imaManager != null && _imaManager._ooyalaPlayerWrapper != null) {
      _imaManager._ooyalaPlayerWrapper.fireVideoPauseCallback();
    } else {
      DebugMode.logD(TAG, "pauseIMA() ignored due to null adsManager");
    }
  }

  /**
   * Destroy this IMAAdPlayer
   */
  @Override
  public void destroy() {
    DebugMode.logD(TAG, "destroy()");
    super.destroy();
  }

  /**
   * Send notification when playhead changed or ad completed
   * @param arg0 the Observable object
   * @param arg current notification
   */
  @Override
  public void update(Observable arg0, Object arg) {
    String notification = arg.toString();
    if (notification == OoyalaPlayer.TIME_CHANGED_NOTIFICATION_NAME) {
      getNotifier().notifyPlayheadChange(); // Notify to update the UI
    }

    super.update(arg0, arg);
    // This ad is managed by a third party, not OoyalaPlayer's ad manager! That means that this player
    // does not fire a normal "State Changed: Completed". This is so Ooyala's ad manager does not take over
    // and start playing back content.  Ooyala Player expects the ad manager to resume content.
    if (notification == OoyalaPlayer.STATE_CHANGED_NOTIFICATION_NAME && getState() == State.COMPLETED) {
      DebugMode.logD(TAG, "update(): Ad complete!");
      if (_imaManager != null && _imaManager._ooyalaPlayerWrapper != null) {
        _imaManager._ooyalaPlayerWrapper.fireIMAAdCompleteCallback();
      } else {
        DebugMode.logD(TAG, "AD_COMPLETED ignored due to null adsManager");
      }
    }

  }

  /**
   * Fetch the current AdSpot
   * @return current AdSpot
   */
  @Override
  public AdSpot getAd() {
    return _ad;
  }

  /**
   * Set current OoyalaIMAManager to be the given OoyalaIMAManager
   * @param imaManager the OoyalaIMAManager to be set
   */
  public void setIMAManager(OoyalaIMAManager imaManager) {
    _imaManager = imaManager;
  }

  /**
   * Fetch the current
   * @return current OoyalaIMAManager
   */
  public OoyalaIMAManager getIMAManager() {
    return _imaManager;
  }

  /**
   * Set current sate to be the given state
   * @param state state of ad playback
   */
  @Override
  public void setState(State state) {
    super.setState(state);
  }

  /**
   * Do nothing since IMA SDK take care of click through
   */
  @Override
  public void processClickThrough() {
  }

  public void notifyAdSkipped() {
    getNotifier().notifyAdSkipped();
  }
}
