package com.ooyala.android.imasdk;


import java.util.Observable;

import android.util.Log;

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

  @Override
  public void init(final OoyalaPlayer parent, AdSpot ad) {
    Log.d(TAG, "IMA Ad Player: Initializing");

    if ( ! (ad instanceof IIMAAdSpot) ) {
      this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Invalid Ad");
      this._state = State.ERROR;
    } else if (((IIMAAdSpot)ad).getImaManager()._onAdError) {
    	this._state = State.COMPLETED;
    } else {
      _seekable = false;
      _ad = ad;
      super.init(parent, ((IIMAAdSpot)_ad).getStreams());
    }
  }

  @Override
  public void play() {
    Log.d(TAG, "IMA Ad Player: Playing");
    super.play();
  }

  @Override
  public void pause() {
    Log.d(TAG, "IMA Ad Player: Pausing");
    super.pause();
  }

  @Override
  public void destroy() {
    super.destroy();
  }

  @Override
  public void update(Observable arg0, Object arg) {
    String notification = arg.toString();

    // This ad is managed by a third party, not OoyalaPlayer's ad manager! That means that this player
    // does not fire a normal "State Changed: Completed". This is so Ooyala's ad manager does not take over
    // and start playing back content.  Ooyala Player expects the ad manager to resume content.
    if (notification == OoyalaPlayer.STATE_CHANGED_NOTIFICATION && getState() == State.COMPLETED) {
      arg = OoyalaPlayer.AD_COMPLETED_NOTIFICATION;
    }

    super.update(arg0, arg);
  }

  @Override
  public AdSpot getAd() {
    return _ad;
  }

}