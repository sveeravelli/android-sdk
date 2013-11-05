package com.ooyala.android.freewheelsdk;

import java.util.List;

import android.util.Log;

import com.ooyala.android.AdMoviePlayer;
import com.ooyala.android.AdSpot;
import com.ooyala.android.BaseStreamPlayer;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.StreamPlayer;
import com.ooyala.android.OoyalaPlayer.State;

import tv.freewheel.ad.interfaces.IAdContext;
import tv.freewheel.ad.interfaces.IConstants;
import tv.freewheel.ad.interfaces.IEvent;
import tv.freewheel.ad.interfaces.IEventListener;
import tv.freewheel.ad.interfaces.ISlot;

/**
 * This class represents the Base Movie Player that plays Freewheel ad spots.
 */
public class FWAdPlayer extends AdMoviePlayer {
  private static String TAG = "FWAdPlayer";
  private FWAdSpot _adSpot;
  private List<ISlot> _ads;
  private ISlot _currentAd;

  private IAdContext _fwContext;
  private IConstants _fwConstants;

  //Create event listeners
  private IEventListener _slotEndedEventListener = new IEventListener() {
    @Override
    public void run(IEvent e) {
      String completedSlotID = (String)e.getData().get(_fwConstants.INFO_KEY_CUSTOM_ID());
      Log.d(TAG, "Completed playing slot: " + completedSlotID);
      //Every time we complete playing a linear slot, play the next slot until we're done with the stack.
      //NOTE: Overlay ad (non-linear) slots also fire EVENT_SLOT_ENDED so make sure not to call play() when overlay ads are complete.
      //      This only happens if an overlay slot starts playing during content playback and ends during ad playback.
      if (!completedSlotID.equals("overlay-slot")) {
        _currentAd = null;
        play();
      }
    }
  };
  private IEventListener _adPauseEventListener = new IEventListener() {
    @Override
    public void run(IEvent e) {
      setState(State.PAUSED);
    }
  };
  private IEventListener _adResumeEventListener = new IEventListener() {
    @Override
    public void run(IEvent e) {
      setState(State.PLAYING);
    }
  };

  @Override
  public void init(final OoyalaPlayer parent, AdSpot ad) {
    Log.d(TAG, "FW Ad Player: Initializing");

    if (!(ad instanceof FWAdSpot)) {
      this._error = "Invalid Ad";
      this._state = State.ERROR;
      return;
    }
    _seekable = false;
    _adSpot = (FWAdSpot) ad;
    _ads = _adSpot.getAdsList();

    _fwContext = _adSpot.getContext();
    _fwConstants = _fwContext.getConstants();

    //Add event listeners and set parameter to prevent ad click detection
    _fwContext.addEventListener(_fwConstants.EVENT_SLOT_ENDED(), _slotEndedEventListener);
    _fwContext.addEventListener(_fwConstants.EVENT_AD_PAUSE(), _adPauseEventListener);
    _fwContext.addEventListener(_fwConstants.EVENT_AD_RESUME(), _adResumeEventListener);
    _fwContext.setParameter(_fwConstants.PARAMETER_CLICK_DETECTION(), "false", _fwConstants.PARAMETER_LEVEL_OVERRIDE());
  }

  @Override
  public void play() {
    if (_ads != null && _ads.size() > 0) {
      _currentAd = _ads.remove(0);
      Log.d(TAG, "FW Ad Player: Playing ad slot " + _currentAd.getCustomId());
      _currentAd.play();

      //TODO: Get the click through and click tracking URLs (see 580ec10aa674f6c5721410e0581a78cad05d6b86)

      //Only set state if ad wasn't playing already
      if (this.getState() != State.PLAYING) {
        setState(State.PLAYING);
      }
    } else {
      Log.d(TAG, "Finished ad. Setting state to complete.");
      setState(State.COMPLETED);

      //Remove the event listeners
      _fwContext.removeEventListener(_fwConstants.EVENT_SLOT_ENDED(), _slotEndedEventListener);
      _fwContext.removeEventListener(_fwConstants.EVENT_AD_PAUSE(), _adPauseEventListener);
      _fwContext.removeEventListener(_fwConstants.EVENT_AD_RESUME(), _adResumeEventListener);
    }
  }

  @Override
  public void resume() {
    Log.d(TAG, "FW Ad Player: Resuming activity");
    _fwContext.setActivityState(_fwConstants.ACTIVITY_STATE_RESUME());
  }

  @Override
  public void suspend() {
    Log.d(TAG, "FW Ad Player: Suspending activity");
    _fwContext.setActivityState(_fwConstants.ACTIVITY_STATE_PAUSE());
    setState(State.SUSPENDED);
  }

  @Override
  public AdSpot getAd() {
    return _adSpot;
  }

  @Override
  public StreamPlayer getBasePlayer() {
    return new BaseStreamPlayer();
  }

  @Override
  public void destroy() {
	Log.d(TAG, "FW Ad Player: Destroying ad player");
	if (_currentAd != null) {
	  _currentAd.stop();
	  _currentAd = null;
	}
	super.destroy();
  }
}