package com.ooyala.android.ads.vast;

import android.content.Intent;
import android.net.Uri;
import android.widget.FrameLayout;

import com.ooyala.android.AdPodInfo;
import com.ooyala.android.AdsLearnMoreButton;
import com.ooyala.android.OoyalaException;
import com.ooyala.android.OoyalaException.OoyalaErrorCode;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.StateNotifier;
import com.ooyala.android.apis.FetchPlaybackInfoCallback;
import com.ooyala.android.item.AdSpot;
import com.ooyala.android.player.AdMoviePlayer;
import com.ooyala.android.player.BaseStreamPlayer;
import com.ooyala.android.util.DebugMode;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Set;

/**
 * A MoviePlayer which helps render VAST advertisements
 */
public class VASTAdPlayer extends AdMoviePlayer {
  private VASTAdSpot _ad;
  private List<VASTLinearAd> _linearAdQueue = new ArrayList<VASTLinearAd>();
  private static String TAG = VASTAdPlayer.class.getName();
  private boolean _startSent = false;
  private boolean _firstQSent = false;
  private boolean _midSent = false;
  private boolean _thirdQSent = false;

  private boolean _playQueued = false;

  private int _topMargin;
  private FrameLayout _playerLayout;
  private AdsLearnMoreButton _learnMore;

  private Object _fetchTask;
  private int _adIndex;

  private interface TrackingEvent {
    public static final String CREATIVE_VIEW = "creativeView";
    public static final String START = "start";
    public static final String FIRST_QUARTILE = "firstQuartile";
    public static final String MIDPOINT = "midpoint";
    public static final String THIRD_QUARTILE = "thirdQuartile";
    public static final String COMPLETE = "complete";
    public static final String PAUSE = "pause";
    public static final String RESUME = "resume";
  }

  @Override
  public void init(final OoyalaPlayer parent, AdSpot ad, StateNotifier notifier) {
    super.init(parent, ad, notifier);
    if (!(ad instanceof VASTAdSpot)) {
      this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Invalid Ad");
      setState(State.ERROR);
      return;
    }
    DebugMode.logD(TAG, "VAST Ad Player Loaded");

    _seekable = false;
    _ad = (VASTAdSpot) ad;
    if (_ad.getAds() == null || _ad.getAds().isEmpty()) {
      if (_fetchTask != null) {
        this._parent.getOoyalaAPIClient().cancel(_fetchTask);
      }

      _fetchTask = _ad.fetchPlaybackInfo(new FetchPlaybackInfoCallback() {

        @Override
        public void callback(boolean result) {
          if (!result) {
            _error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Could not fetch VAST Ad");
            setState(State.ERROR);
            return;
          }
          if(!initAfterFetch(parent)) {
            _error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Bad VAST Ad");
            setState(State.ERROR);
            return;
          }
        }

      });
      return;
    }
    if(!initAfterFetch(parent)) {
      _error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Bad VAST Ad");
      setState(State.ERROR);
      return;
    }
  }

  private boolean initAfterFetch(OoyalaPlayer parent) {
    _adIndex = 0;

    for (VASTAd vastAd : _ad.getAds()) {
      // Add to the list of impression URLs to be called when player is loaded

      for (VASTSequenceItem seqItem : vastAd.getSequence()) {
        if (seqItem.hasLinear() && seqItem.getLinear().getStream() != null) {
          _linearAdQueue.add(seqItem.getLinear());
        }
      }
    }

    if (_linearAdQueue.isEmpty()) { return false; }
    if (_linearAdQueue.get(0) == null || _linearAdQueue.get(0).getStreams() == null)  { return false; }

    addQuartileBoundaryObserver();

    super.init(parent, _linearAdQueue.get(0).getStreams());

    //Get the _playerLayout and _topMargin for the Learn More button
    _playerLayout = parent.getLayout();
    _topMargin = parent.getTopBarOffset();

    //Add Learn More button if there is a click through URL
    if (currentLinearAd() != null && currentLinearAd().getClickThroughURL() != null) {
      _learnMore = new AdsLearnMoreButton(_playerLayout.getContext(), this, _topMargin);
      _playerLayout.addView(_learnMore);
    }

    if (_ad.getTrackingURLs() != null) {
      for (URL url : _ad.getTrackingURLs()) {
        ping(url);
      }
    }

    dequeuePlay();
    return true;
  }

  private void dequeuePlay() {
    if (_playQueued) {
      _playQueued = false;
      play();
    }
  }

  private void queuePlay() {
    _playQueued = true;
  }

  @Override
  public void play() {
    if (this.getBasePlayer() == null) {
      queuePlay();
      return;
    }
    if (_linearAdQueue.isEmpty()) {
      setState(State.COMPLETED);
      return;
    }

    if (currentTime() != 0) {
      sendTrackingEvent(TrackingEvent.RESUME);
    }

    super.play();
  }

  @Override
  public void pause() {
    if (_linearAdQueue.isEmpty()) {
      setState(State.COMPLETED);
      return;
    }
    if (getState() != State.PLAYING) {
      sendTrackingEvent(TrackingEvent.PAUSE);
    }
    super.pause();
  }

  @Override
  public void resume() {
    super.resume();

    //Bring Learn More button to front when play resumes so it does not get hidden beneath the video view.
    if (_learnMore != null) {
      _playerLayout.bringChildToFront(_learnMore);
    }
  }

  @Override
  public VASTAdSpot getAd() {
    return _ad;
  }

  @Override
  protected void setState(State state) {
    //look for state changing to complete here to ensure it happens before any observers notified.
    if (state == State.COMPLETED) {
      if(_linearAdQueue.size() > 0) _linearAdQueue.remove(0);
      sendTrackingEvent(TrackingEvent.COMPLETE);
      if (!_linearAdQueue.isEmpty()) {
        addQuartileBoundaryObserver();
        super.init(_parent, _linearAdQueue.get(0).getStreams());
        return;
      }
    }
    super.setState(state);
  }

  private VASTLinearAd currentLinearAd() {
    return _linearAdQueue.isEmpty() ? null : _linearAdQueue.get(0);
  }

  private void addQuartileBoundaryObserver() {
    _startSent = false;
    _firstQSent = false;
    _midSent = false;
    _thirdQSent = false;
  }

  @Override
  public void update(Observable arg0, Object arg) {
    if (arg == OoyalaPlayer.TIME_CHANGED_NOTIFICATION_NAME) {
      if (!_startSent && currentTime() > 0) {


        sendTrackingEvent(TrackingEvent.CREATIVE_VIEW);
        sendTrackingEvent(TrackingEvent.START);
        _startSent = true;

        String title = _ad.getAds().get(_adIndex).getTitle();
        String description = _ad.getAds().get(_adIndex).getDescription();
        String url = currentLinearAd().getClickThroughURL();
        int adsCount = _ad.getAds().size();
        int unplayedCount = adsCount - _adIndex;
        _notifier.notifyAdStartWithAdInfo(new AdPodInfo(title,description,url,adsCount,unplayedCount,true,true));
        if(isCurrentAdIFirstLinearForAdIndex(_adIndex, _ad.getAds())){
          sendImpressionTrackingEvent(_adIndex, _ad.getAds());
        }

      } else if (!_firstQSent && currentTime() > (currentLinearAd().getDuration() * 1000 / 4)) {
        sendTrackingEvent(TrackingEvent.FIRST_QUARTILE);
        _firstQSent = true;
      } else if (!_midSent && currentTime() > (currentLinearAd().getDuration() * 1000 / 2)) {
        sendTrackingEvent(TrackingEvent.MIDPOINT);
        _midSent = true;
      } else if (!_thirdQSent && currentTime() > (3 * currentLinearAd().getDuration() * 1000 / 4)) {
        sendTrackingEvent(TrackingEvent.THIRD_QUARTILE);
        _thirdQSent = true;
      }
    }
    else if (arg == OoyalaPlayer.STATE_CHANGED_NOTIFICATION_NAME) {
      try {
        BaseStreamPlayer tempPlayer = (BaseStreamPlayer) arg0;

        // If player is completed, send completed tracking event
        if (tempPlayer.getState() == State.COMPLETED) {
          if(isCurrentAdLastLinearForAdIndex(_adIndex, _ad.getAds())){
            _adIndex++;
          }
          sendTrackingEvent(TrackingEvent.COMPLETE);
          //If there are more ads to play, play them
          if(_linearAdQueue.size() > 0) _linearAdQueue.remove(0);
          if (!_linearAdQueue.isEmpty()) {
            super.destroy();
            addQuartileBoundaryObserver();
            super.init(_parent, _linearAdQueue.get(0).getStreams());
            super.play();

            //If the next linear ad has a clickThrough URL, create the Learn More button only if it doesn't exist
            if (currentLinearAd() != null && currentLinearAd().getClickThroughURL() != null) {
              if (_learnMore == null) {
                _learnMore = new AdsLearnMoreButton(_playerLayout.getContext(), this, _topMargin);
                _playerLayout.addView(_learnMore);
              } else {
                _playerLayout.bringChildToFront(_learnMore);
              }
            }
            //If there is no clickThrough and Learn More button exists from previous ad, remove it
            else if (_learnMore != null) {
              _playerLayout.removeView(_learnMore);
              _learnMore = null;
            }
          }
        }
      } catch (Exception e) {
        // ERROR: arg0 is not a BaseStreamPlayer as expected
        DebugMode.logE(TAG, "arg0 should be a BaseStreamPlayer but is not!");
      }
    }
    super.update(arg0,  arg);
  }

  /**
   * Called by OoyalaPlayer when going in and out of fullscreen using OoyalaPlayerLayoutController
   * @param layout the new layout to add the Learn More button
   * @param topMargin the pixels to shift the Learn More button down
   */
  @Override
  public void updateLearnMoreButton(FrameLayout layout, int topMargin) {
    //If topMargin did not change, return
    if (_topMargin == topMargin) {
      return;
    }

    //If Learn More button exists, add it to the new playerLayout with the topMargin
    if (_learnMore != null) {
      //Remove the Learn More button from the old playerLayout and set the new playerLayout and topMargin
      _playerLayout.removeView(_learnMore);
      _playerLayout = layout;
      _topMargin = topMargin;

      //Set the new topMargin and add the Learn More button to new playerLayout
      _learnMore.setTopMargin(_topMargin);
      _playerLayout.addView(_learnMore);
    }
    //Else, keep track of the new player layout and topMargin for next linear ad
    else {
      _playerLayout = layout;
      _topMargin = topMargin;
    }
  }

  /**
   * Called by the Learn More button's onClick event.
   * Sends the click tracking pings and opens the browser.
   */
  @Override
  public void processClickThrough() {
    suspend();
    if (currentLinearAd() != null && currentLinearAd().getClickTrackingURLs() != null) {
      Set<String> urls = currentLinearAd().getClickTrackingURLs();
      if (urls != null) {
        for (String urlStr : urls) {
          final URL url = VASTUtils.urlFromAdUrlString(urlStr);
          DebugMode.logI(TAG, "Sending Click Tracking Ping: " + url);
          ping(url);
        }
      }
    }

    //Open browser to click through URL
    String url = currentLinearAd().getClickThroughURL();
    try {
      url = url.trim(); //strip leading and trailing whitespace
      Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      _playerLayout.getContext().startActivity(browserIntent);
      DebugMode.logD(TAG, "Opening browser to " + url);
    } catch (Exception e) {
      DebugMode.logE(TAG, "There was some exception on clickthrough!");
      DebugMode.logE(TAG, "Caught!", e);
    }
  }

  public void sendTrackingEvent(String event) {
    if (currentLinearAd() == null || currentLinearAd().getTrackingEvents() == null) { return; }
    Set<String> urls = currentLinearAd().getTrackingEvents().get(event);
    if (urls != null) {
      for (String urlStr : urls) {
        final URL url = VASTUtils.urlFromAdUrlString(urlStr);
        DebugMode.logI(TAG, "Sending " + event + " Tracking Ping: " + url);
        ping(url);
      }
    }
  }


  private void sendImpressionTrackingEvent(int adIndex, List<VASTAd> ads) {
    List<String> urls = impressionUrlForAdIndex(adIndex, ads);
    if(urls != null){
      for(String urlStr: urls) {
        final URL url = VASTUtils.urlFromAdUrlString(urlStr);
        DebugMode.logI(TAG, "Sending Impression Tracking Ping: " + url);
        ping(url);
      }
    }
  }

  private List<String> impressionUrlForAdIndex(int adIndex, List<VASTAd> ads){
    VASTAd vastAd = ads.get(adIndex);
    return vastAd.getImpressionURLs();
  }

  private List<VASTLinearAd> vastLinearAdsForAdIndex(int adIndex, List<VASTAd> ads){
    List<VASTLinearAd> vastLinearAds = new ArrayList<VASTLinearAd>();

    VASTAd vastAd = ads.get(adIndex);
    for (VASTSequenceItem seqItem : vastAd.getSequence()) {
      if (seqItem.hasLinear() && seqItem.getLinear().getStream() != null) {
        vastLinearAds.add(seqItem.getLinear());
      }
    }

    return vastLinearAds;
  }

  private boolean isCurrentAdIFirstLinearForAdIndex(int adIndex, List<VASTAd> ads){
    if(ads != null && ads.size() != 0) {
      return currentLinearAd().equals(vastLinearAdsForAdIndex(adIndex, ads).get(0));
    }else{
      return false;
    }
  }

  private boolean isCurrentAdLastLinearForAdIndex(int _adIndex, List<VASTAd> ads){
    if(ads != null && ads.size() != 0) {
      return currentLinearAd().equals(vastLinearAdsForAdIndex(_adIndex, ads).get(ads.size() - 1));
    }else{
      return false;
    }
  }

  @Override
  public void destroy() {
    //Remove Learn More button if it exists
    if (_learnMore != null) {
      _playerLayout.removeView(_learnMore);
      _learnMore.destroy();
      _learnMore = null;
    }

    if (_fetchTask != null && this._parent != null) this._parent.getOoyalaAPIClient().cancel(_fetchTask);
    deleteObserver(this);
    super.destroy();
  }
}