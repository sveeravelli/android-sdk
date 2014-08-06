package com.ooyala.android.imasdk;


import java.util.Observable;

import com.ooyala.android.DebugMode;
import com.ooyala.android.OoyalaException;
import com.ooyala.android.OoyalaException.OoyalaErrorCode;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.item.AdSpot;
import com.ooyala.android.player.AdMoviePlayer;

/**
 * This class represents the Base Movie Player that plays IMA Ad spots.
 *
 * @author michael.len
 *
 */
public class IMAAdPlayer extends AdMoviePlayer {
  private static String TAG = "IMAAdPlayer";
  private AdSpot _ad;
  private OoyalaIMAManager _imaManager;

  @Override
  public void init(final OoyalaPlayer parent, AdSpot ad) {
    DebugMode.logD(TAG, "IMA Ad Player: Initializing");
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
    if (_ad != null) {
      // We do not update the State to PLAYING until we hear the callback from IMA SDK
      // since there could be a while between message sent and callback received
      DebugMode.logD(TAG, "IMA Ad Player: Playing");
      super.play();
      _imaManager._ooyalaPlayerWrapper.fireVideoStartCallback();
    }
  }

  @Override
  public void pause() {
    DebugMode.logD(TAG, "IMA Ad Player: Pausing");
    super.pause();
    _imaManager._ooyalaPlayerWrapper.fireVideoPauseCallback();
  }

  @Override
  public void destroy() {
    DebugMode.logD(TAG, "IMA Ad Player: Destroy");
    super.destroy();
  }

  @Override
  public void update(Observable arg0, Object arg) {
    String notification = arg.toString();
    if (notification == OoyalaPlayer.TIME_CHANGED_NOTIFICATION) {
      OoyalaPlayer.notifyTimeChange(this); // Notify to update the UI
    }
    // This ad is managed by a third party, not OoyalaPlayer's ad manager! That means that this player
    // does not fire a normal "State Changed: Completed". This is so Ooyala's ad manager does not take over
    // and start playing back content.  Ooyala Player expects the ad manager to resume content.
    if (notification == OoyalaPlayer.STATE_CHANGED_NOTIFICATION && getState() == State.COMPLETED) {
      arg = OoyalaPlayer.AD_COMPLETED_NOTIFICATION;
      DebugMode.logE(TAG, "Ad complete!");
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

  public void setState(State state) {
    super.setState(state);
    OoyalaPlayer.notifyStateChange(this);
  }
}