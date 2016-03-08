package com.ooyala.android.freewheelsdk;

import android.widget.FrameLayout;

import com.ooyala.android.AdsLearnMoreButton;
import com.ooyala.android.AdsLearnMoreInterface;
import com.ooyala.android.OoyalaException;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.StateNotifier;
import com.ooyala.android.player.PlayerInterface;
import com.ooyala.android.plugin.LifeCycleInterface;
import com.ooyala.android.util.DebugMode;
import com.ooyala.android.AdPodInfo;

import java.util.ArrayList;

import tv.freewheel.ad.interfaces.IAdContext;
import tv.freewheel.ad.interfaces.IAdInstance;
import tv.freewheel.ad.interfaces.IConstants;
import tv.freewheel.ad.interfaces.IEvent;
import tv.freewheel.ad.interfaces.IEventListener;
import tv.freewheel.ad.interfaces.ISlot;

/**
 * This class represents the Base Movie Player that plays Freewheel ad spots.
 */
public class FWAdPlayer implements PlayerInterface, LifeCycleInterface,
    AdsLearnMoreInterface {
  private static String TAG = "FWAdPlayer";
  private OoyalaFreewheelManager _adManager;
  private ISlot _currentAd;
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

  private StateNotifier _notifier;
  private boolean _pauseAfterResume;

  //Create event listeners
  private IEventListener _adStartedEventListener = new IEventListener() {
    @Override
    public void run(IEvent e) {
      String customId = (String) e.getData().get(_fwConstants.INFO_KEY_CUSTOM_ID());
      ISlot slot = _fwContext.getSlotByCustomId(customId);
      int adId = (Integer) e.getData().get(_fwConstants.INFO_KEY_AD_ID());
      _currentAdInstance = null;

      DebugMode.logD(TAG, "Ad event type: " + e.getType() + " slot customID: " + customId + " adId: " + adId);
      for (IAdInstance ad : slot.getAdInstances()) {
        if (ad.getAdId() == adId) {
          _currentAdInstance = ad;
          break;
        }
      }

      //First remove the Learn More button from the view
      _playerLayout.removeView(_learnMore);
      if (_currentAdInstance != null) {
        ArrayList<String> clickThrough =
          _currentAdInstance.getEventCallbackURLs(_fwConstants.EVENT_AD_CLICK(), _fwConstants.EVENT_TYPE_CLICK());
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

  /**
   * Initialize everything for the FWAdPlayer
   * @param manager the OoyalaFreewheelManager
   * @param parent the OoyalaPlayer layout
   * @param ad the ad to be loaded
   * @param notifier the notifier to be loaded
   */
  public void init(OoyalaFreewheelManager manager, OoyalaPlayer parent,
      FWAdSpot ad, StateNotifier notifier) {
    setManager(manager);
    setLayout(parent);
    initAd(ad);
    _notifier = notifier;
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

  /**
   * Initialize the ad of FWAdPlayer.
   * @param ad the ad to be loaded
   */
  public void initAd(FWAdSpot ad) {
    _playQueued = false;
    _adError = false;
    _noPrerolls = false;
    _currentAd = ad.getAd();
  }

  /**
   * Set state to handle different error situations.
   */
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

  /**
   * Use Freewheel's renderer controller to send pings and open browser
   */
  @Override
  public void processClickThrough() {
    if (_currentAdInstance != null) {
      _currentAdInstance.getRendererController().processEvent(_fwConstants.EVENT_AD_CLICK());
    }
  }

  /**
   * Implements the PlayerInterface method.
   */
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

      DebugMode.logD(TAG, "FW Ad Player: Playing ad slot " + _currentAd.getCustomId());
      String title ="";
      String description = "";
      String url = "";
      int adsCount = 1;
      int unplayedCount = adsCount - 1;
      _notifier.notifyAdStartWithAdInfo(new AdPodInfo(title, description, url, adsCount, unplayedCount));
      setState(State.PLAYING);
      _currentAd.play();
    }
  }

  /**
   * Implements the LifeCycleInterface method.
   */
  @Override
  public void resume() {
    DebugMode.logD(TAG, "FW Ad Player: Resuming activity, pauseAfterResume "
        + String.valueOf(_pauseAfterResume));
    _fwContext.setActivityState(_fwConstants.ACTIVITY_STATE_RESUME());
    if (_pauseAfterResume) {
      _currentAd.pause();
    }
    setState(State.PLAYING);
  }

  /**
   * Implements the LifeCycleInterface method.
   */
  @Override
  public void suspend() {
    _pauseAfterResume = (getState() == State.PAUSED);
    _fwContext.setActivityState(_fwConstants.ACTIVITY_STATE_PAUSE());
    DebugMode.logD(TAG, "FW Ad Player: Suspending activity, pauseAfterResume "
        + String.valueOf(_pauseAfterResume));
    setState(State.SUSPENDED);
  }

  /**
   * Implements the LifeCycleInterface method.
   */
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
  }

  protected void setState(State state) {
    _notifier.setState(state);
  }

  /**
   * Implements the LifeCycleInterface method.
   */
  @Override
  public void reset() {
    // TODO Auto-generated method stub

  }

  /**
   * Implements the LifeCycleInterface method.
   * @param timeInMilliSecond the playhead time to set
   * @param stateToResume the player state after resume
   */
  @Override
  public void resume(int timeInMilliSecond, State stateToResume) {
    // TODO Auto-generated method stub

  }

  /**
   * Implements the PlayerInterface method.
   */
  @Override
  public void pause() {
    // TODO Auto-generated method stub

  }

  /**
   * Implements the PlayerInterface method.
   */
  @Override
  public void stop() {
    // TODO Auto-generated method stub

  }

  /**
   * Implements the the PlayerInterface method.
   * @return current time
   */
  @Override
  public int currentTime() {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * Implements the PlayerInterface method.
   * @return duration
   */
  @Override
  public int duration() {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * Implements the PlayerInterface method.
   * @return the buffer percentage (between 0 and 100 inclusive)
   */
  @Override
  public int buffer() {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * Implements the PlayerInterface method.
   * @return true if the current player is seekable, false if there is no
   *         current player or it is not seekable
   */
  @Override
  public boolean seekable() {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * Implements the PlayerInterface method.
   * @param timeInMillis time in milliseconds
   */
  @Override
  public void seekToTime(int timeInMillis) {
    // TODO Auto-generated method stub

  }

  /**
   * Implements the PlayerInterface method.
   * @return the state
   */
  @Override
  public State getState() {
    return _notifier.getState();
  }

  /**
   * Fetch the StateNotifier of current FWAdPlayer.
   * @return StateNotifier
   */
  public StateNotifier getNotifier() {
    return _notifier;
  }

  @Override
  public int livePlayheadPercentage() {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * Implements the PlayerInterface method.
   * @param percent The percent of scrubber the cursor ends after seek
   */
  @Override
  public void seekToPercentLive(int percent) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isLiveClosedCaptionsAvailable() {
    return false;
  }

  @Override
  public void setClosedCaptionsLanguage(String language) {

  }

  @Override
  public OoyalaException getError() {
    // Ad Players do not use getError to report to OoyalaPlayer yet.  They can, but they don't at the moment
    return null;
  }
}