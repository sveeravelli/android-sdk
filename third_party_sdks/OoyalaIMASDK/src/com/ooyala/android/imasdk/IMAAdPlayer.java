package com.ooyala.android.imasdk;


import java.util.Observable;

import com.ooyala.android.DebugMode;
import com.ooyala.android.OoyalaException;
import com.ooyala.android.OoyalaException.OoyalaErrorCode;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.StateNotifier;
import com.ooyala.android.item.AdSpot;
import com.ooyala.android.player.AdMoviePlayer;

/**
 * This class represents the Base Movie Player that plays IMA Ad spots.
 *
 *
 */
public class IMAAdPlayer extends AdMoviePlayer {
  private static String TAG = IMAAdPlayer.class.getSimpleName();
  private AdSpot _ad;
  private OoyalaIMAManager _imaManager;

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

  @Override
  public void play() {
    DebugMode.logD(TAG, "play(): Playing indirectly through AdsManager");
    _imaManager._adsManager.resume();
  }

  public void playIMA() {
    if (_ad != null) {
      // We do not update the State to PLAYING until we hear the callback from IMA SDK
      // since there could be a while between message sent and callback received
      DebugMode.logD(TAG, "playIMA(): Playing");
      super.play();
      _imaManager._ooyalaPlayerWrapper.fireVideoStartCallback();
    }
  }

  @Override
  public void pause() {
    DebugMode.logD(TAG, "pause(): Pausing indirectly through AdsManager");
    _imaManager._adsManager.pause();
  }

  public void pauseIMA() {
    DebugMode.logD(TAG, "pauseIMA(): Pausing");
    super.pause();
    _imaManager._ooyalaPlayerWrapper.fireVideoPauseCallback();
  }

  @Override
  public void destroy() {
    DebugMode.logD(TAG, "destroy()");
    super.destroy();
  }

  @Override
  public void update(Observable arg0, Object arg) {
    String notification = arg.toString();
    if (notification == OoyalaPlayer.TIME_CHANGED_NOTIFICATION) {
      getNotifier().notifyPlayheadChange(); // Notify to update the UI
    }
    // This ad is managed by a third party, not OoyalaPlayer's ad manager! That means that this player
    // does not fire a normal "State Changed: Completed". This is so Ooyala's ad manager does not take over
    // and start playing back content.  Ooyala Player expects the ad manager to resume content.
    if (notification == OoyalaPlayer.STATE_CHANGED_NOTIFICATION && getState() == State.COMPLETED) {
      arg = OoyalaPlayer.AD_COMPLETED_NOTIFICATION;
      DebugMode.logD(TAG, "update(): Ad complete!");
      _imaManager._ooyalaPlayerWrapper.fireIMAAdCompleteCallback();
    }

    super.update(arg0, arg);
  }

  @Override
  public AdSpot getAd() {
    return _ad;
  }

  public void setIMAManager(OoyalaIMAManager imaManager) {
    _imaManager = imaManager;
  }

  @Override
  public void setState(State state) {
    super.setState(state);
  }

  @Override
  public void processClickThrough() {
  }
}
