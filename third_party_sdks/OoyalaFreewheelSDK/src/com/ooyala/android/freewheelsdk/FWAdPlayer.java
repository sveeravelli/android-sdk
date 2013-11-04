package com.ooyala.android.freewheelsdk;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.ooyala.android.AdMoviePlayer;
import com.ooyala.android.AdSpot;
import com.ooyala.android.BaseStreamPlayer;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.StreamPlayer;
import com.ooyala.android.OoyalaPlayer.State;

import tv.freewheel.ad.interfaces.IAdContext;
import tv.freewheel.ad.interfaces.IAdInstance;
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
  private List<IAdInstance> _currentAdInstances;

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
  //This fires when an individual ad instance within an ISlot completes playing
  //We don't need to call play() since _currentAd.play() in the play() function plays all ad instances
  private IEventListener _adCompleteEventListener = new IEventListener() {
    @Override
    public void run(IEvent e) {
      Log.d(TAG, "Completed playing ad instance. Play next ad instance if there is.");
      if (_currentAd != null && _currentAdInstances.size() > 0) {
        IAdInstance ad = _currentAdInstances.remove(0);
        ArrayList<String> clickThrough = ad.getEventCallbackURLs(_fwConstants.EVENT_AD_CLICK(), _fwConstants.EVENT_TYPE_CLICK());
        ArrayList<String> clickTracking = ad.getEventCallbackURLs(_fwConstants.EVENT_AD_CLICK(), _fwConstants.EVENT_TYPE_CLICK_TRACKING());
      }
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
    _fwContext.addEventListener(_fwConstants.EVENT_AD_COMPLETE(), _adCompleteEventListener);
    _fwContext.setParameter(_fwConstants.PARAMETER_CLICK_DETECTION(), "false", _fwConstants.PARAMETER_LEVEL_OVERRIDE());
  }

  @Override
  public void play() {
    if (_ads != null && _ads.size() > 0) {
      _currentAd = _ads.remove(0);
      Log.d(TAG, "FW Ad Player: Playing ad slot " + _currentAd.getCustomId());
      _currentAd.play();

      //Get click through and click tracking URLs of the current ad instance
      _currentAdInstances = _currentAd.getAdInstances();
      if (_currentAdInstances.size() > 0) {
        IAdInstance ad = _currentAdInstances.remove(0);
        ArrayList<String> clickThrough = ad.getEventCallbackURLs(_fwConstants.EVENT_AD_CLICK(), _fwConstants.EVENT_TYPE_CLICK());
        ArrayList<String> clickTracking = ad.getEventCallbackURLs(_fwConstants.EVENT_AD_CLICK(), _fwConstants.EVENT_TYPE_CLICK_TRACKING());
      }

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
      _fwContext.removeEventListener(_fwConstants.EVENT_AD_COMPLETE(), _adCompleteEventListener);
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
}