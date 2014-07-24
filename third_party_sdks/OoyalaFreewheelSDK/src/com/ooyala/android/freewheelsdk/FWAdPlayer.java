package com.ooyala.android.freewheelsdk;

import java.util.ArrayList;
import java.util.List;

import tv.freewheel.ad.interfaces.IAdContext;
import tv.freewheel.ad.interfaces.IAdInstance;
import tv.freewheel.ad.interfaces.IConstants;
import tv.freewheel.ad.interfaces.IEvent;
import tv.freewheel.ad.interfaces.IEventListener;
import tv.freewheel.ad.interfaces.ISlot;
import android.widget.FrameLayout;

import com.ooyala.android.AdsLearnMoreButton;
import com.ooyala.android.DebugMode;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.item.AdSpotBase;
import com.ooyala.android.player.AdMoviePlayer;
import com.ooyala.android.player.BaseStreamPlayer;
import com.ooyala.android.player.StreamPlayer;

/**
 * This class represents the Base Movie Player that plays Freewheel ad spots.
 */
public class FWAdPlayer extends AdMoviePlayer {
  private static String TAG = "FWAdPlayer";
  private OoyalaFreewheelManager _adManager;
  private FWAdSpot _adSpot;
  private ISlot _currentAd;
  private List<IAdInstance> _adInstances;
  private IAdInstance _currentAdInstance;

  private IAdContext _fwContext;
  private IConstants _fwConstants;
  private FrameLayout _playerLayout;
  private AdsLearnMoreButton _learnMore;

  //Booleans to keep track of when to play, error out, or set state
  //to complete so that content doesn't play before it should
  private boolean _playQueued;
  private boolean _adError;
  private boolean _noPrerolls;

  //Create event listeners
  private IEventListener _adStartedEventListener = new IEventListener() {
    @Override
    public void run(IEvent e) {
      if (_adInstances.size() > 0) {
        _currentAdInstance = _adInstances.remove(0);
        ArrayList<String> clickThrough = _currentAdInstance.getEventCallbackURLs(_fwConstants.EVENT_AD_CLICK(), _fwConstants.EVENT_TYPE_CLICK());

        //First remove the Learn More button from the view
        _playerLayout.removeView(_learnMore);

        //If there is a click through URL, add the Learn More button
        if (clickThrough != null && clickThrough.size() > 0) {
          _playerLayout.addView(_learnMore);
        }
      }
    }
  };

  private IEventListener _slotEndedEventListener = new IEventListener() {
    @Override
    public void run(IEvent e) {
      String completedSlotID = (String)e.getData().get(_fwConstants.INFO_KEY_CUSTOM_ID());
      DebugMode.logD(TAG, "Completed playing slot: " + completedSlotID);
      //Every time we complete playing a linear slot, play the next slot until we're done with the stack.
      //NOTE: Overlay ad (non-linear) slots also fire EVENT_SLOT_ENDED so make sure not to call play() when overlay ads are complete.
      //      This only happens if an overlay slot starts playing during content playback and ends during ad playback.
      if (!completedSlotID.equals("overlay-slot")) {
        _currentAd = null;
        DebugMode.logD(TAG, "Finished ad. Setting state to complete.");
        setState(State.COMPLETED);
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

  public void init(OoyalaFreewheelManager manager, OoyalaPlayer parent,
      FWAdSpot ad) {
    setManager(manager);
    setLayout(parent);
    initAd(ad);
  }

  @Override
  public void init(final OoyalaPlayer parent, AdSpotBase ad) {
    DebugMode.assertFail(TAG,
        "FW Ad Player: Init should not be called!!! This is not a valid init");
  }

  private void setManager(OoyalaFreewheelManager manager) {
    _adManager = manager;
    _fwContext = _adManager.getFreewheelContext();
    _fwConstants = _fwContext.getConstants();

    // Add event listeners and set parameter to prevent ad click detection
    _fwContext.addEventListener(_fwConstants.EVENT_AD_IMPRESSION(),
        _adStartedEventListener);
    _fwContext.addEventListener(_fwConstants.EVENT_SLOT_ENDED(),
        _slotEndedEventListener);
    _fwContext.addEventListener(_fwConstants.EVENT_AD_PAUSE(),
        _adPauseEventListener);
    _fwContext.addEventListener(_fwConstants.EVENT_AD_RESUME(),
        _adResumeEventListener);
    _fwContext.setParameter(_fwConstants.PARAMETER_CLICK_DETECTION(), "false",
        _fwConstants.PARAMETER_LEVEL_OVERRIDE());
  }

  private void setLayout(OoyalaPlayer parent) {
    // Initialize the Learn More button. Note that we don't have a click through
    // URL yet.
    if (_playerLayout == null) {
      _playerLayout = parent.getLayout();
      _learnMore = new AdsLearnMoreButton(_playerLayout.getContext(), this,
          parent.getTopBarOffset());
    }
  }

  public void initAd(FWAdSpot ad) {

    _seekable = false;
    _playQueued = false;
    _adError = false;
    _noPrerolls = false;

    _adSpot = ad;
    _currentAd = ad.getAd();
  }

  public void onError() {
    //If play() has already been called, set state to error to resume content. Else, we set state to ready and
    //wait until play() is called to error out (or else, content may play when we haven't called play() yet).
    if (_playQueued) {
      setState(State.ERROR);
    } else {
      setState(State.READY);
      _adError = true;
    }
  }

  @Override
  public void processClickThrough() {
    //Use Freewheel's renderer controller to send pings and open browser
    _currentAdInstance.getRendererController().processEvent(_fwConstants.EVENT_AD_CLICK());
  }

  @Override
  public void play() {
    if (_adError) {
      setState(State.ERROR);
    } else if (_noPrerolls) {
      setState(State.COMPLETED);
    } else if (_currentAd == null) {
      _playQueued = true;
    } else {
      _adManager.adsPlaying();
      _adInstances = _currentAd.getAdInstances();

      DebugMode.logD(TAG, "FW Ad Player: Playing ad slot " + _currentAd.getCustomId());
      setState(State.PLAYING);
      _currentAd.play();
    }
  }

  @Override
  public void resume() {
    DebugMode.logD(TAG, "FW Ad Player: Resuming activity");
    _fwContext.setActivityState(_fwConstants.ACTIVITY_STATE_RESUME());
  }

  @Override
  public void suspend() {
    DebugMode.logD(TAG, "FW Ad Player: Suspending activity");
    _fwContext.setActivityState(_fwConstants.ACTIVITY_STATE_PAUSE());
    setState(State.SUSPENDED);
  }

  @Override
  public AdSpotBase getAd() {
    return _adSpot;
  }

  @Override
  public StreamPlayer getBasePlayer() {
    return new BaseStreamPlayer();
  }

  @Override
  public void destroy() {
    //Remove Learn More button if it exists
    if (_learnMore != null) {
      _playerLayout.removeView(_learnMore);
      _learnMore.destroy();
      _learnMore = null;
    }

    //Remove the event listeners
    _fwContext.removeEventListener(_fwConstants.EVENT_AD_IMPRESSION(), _adStartedEventListener);
    _fwContext.removeEventListener(_fwConstants.EVENT_SLOT_ENDED(), _slotEndedEventListener);
    _fwContext.removeEventListener(_fwConstants.EVENT_AD_PAUSE(), _adPauseEventListener);
    _fwContext.removeEventListener(_fwConstants.EVENT_AD_RESUME(), _adResumeEventListener);

    DebugMode.logD(TAG, "FW Ad Player: Destroying ad player");
    if (_currentAd != null) {
      _currentAd.stop();
      _currentAd = null;
    }

    super.destroy();
  }

  @Override
  protected void setState(State state) {
    if (state == State.COMPLETED) {
      _adManager.onAdCompleted();
    } else if (state == State.ERROR) {
      _adManager.onAdError();
    }
    super.setState(state);
  }
}