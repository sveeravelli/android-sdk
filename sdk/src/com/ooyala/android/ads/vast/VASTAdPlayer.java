package com.ooyala.android.ads.vast;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import android.content.Intent;
import android.net.Uri;
import android.widget.FrameLayout;

import com.ooyala.android.AdsLearnMoreButton;
import com.ooyala.android.DebugMode;
import com.ooyala.android.FetchPlaybackInfoCallback;
import com.ooyala.android.OoyalaException;
import com.ooyala.android.OoyalaException.OoyalaErrorCode;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.StateNotifier;
import com.ooyala.android.item.AdSpot;
import com.ooyala.android.player.AdMoviePlayer;
import com.ooyala.android.player.BaseStreamPlayer;

public class VASTAdPlayer extends AdMoviePlayer {
  private VASTAdSpot _ad;
  private List<VASTLinearAd> _linearAdQueue = new ArrayList<VASTLinearAd>();
  private static String TAG = VASTAdPlayer.class.getName();
  private List<String> _impressionURLs = new ArrayList<String>();
  private boolean _impressionSent = false;
  private boolean _startSent = false;
  private boolean _firstQSent = false;
  private boolean _midSent = false;
  private boolean _thirdQSent = false;

  private int _topMargin;
  private FrameLayout _playerLayout;
  private AdsLearnMoreButton _learnMore;

  private Object _fetchTask;

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
    for (VASTAd vastAd : _ad.getAds()) {
      // Add to the list of impression URLs to be called when player is loaded
      _impressionURLs.addAll(vastAd.getImpressionURLs());

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
    if (currentAd() != null && currentAd().getClickThroughURL() != null) {
      _learnMore = new AdsLearnMoreButton(_playerLayout.getContext(), this, _topMargin);
      _playerLayout.addView(_learnMore);
    }

    if (_ad.getTrackingURLs() != null) {
      for (URL url : _ad.getTrackingURLs()) {
        ping(url);
      }
    }

    return true;
  }

  @Override
  public void play() {
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

  private VASTLinearAd currentAd() {
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
    if (arg == OoyalaPlayer.TIME_CHANGED_NOTIFICATION) {
      if (!_startSent && currentTime() > 0) {
        if (!_impressionSent) {
          sendImpressionTrackingEvent(_impressionURLs);
        }
        sendTrackingEvent(TrackingEvent.CREATIVE_VIEW);
        sendTrackingEvent(TrackingEvent.START);
        _startSent = true;
      } else if (!_firstQSent && currentTime() > (currentAd().getDuration() * 1000 / 4)) {
        sendTrackingEvent(TrackingEvent.FIRST_QUARTILE);
        _firstQSent = true;
      } else if (!_midSent && currentTime() > (currentAd().getDuration() * 1000 / 2)) {
        sendTrackingEvent(TrackingEvent.MIDPOINT);
        _midSent = true;
      } else if (!_thirdQSent && currentTime() > (3 * currentAd().getDuration() * 1000 / 4)) {
        sendTrackingEvent(TrackingEvent.THIRD_QUARTILE);
        _thirdQSent = true;
      }
    }
    else if (arg == OoyalaPlayer.STATE_CHANGED_NOTIFICATION) {
      try {
        BaseStreamPlayer tempPlayer = (BaseStreamPlayer) arg0;

        // If player is completed, send completed tracking event
        if (tempPlayer.getState() == State.COMPLETED) {
          sendTrackingEvent(TrackingEvent.COMPLETE);
          //If there are more ads to play, play them
          if(_linearAdQueue.size() > 0) _linearAdQueue.remove(0);
          if (!_linearAdQueue.isEmpty()) {
            super.destroy();
            addQuartileBoundaryObserver();
            super.init(_parent, _linearAdQueue.get(0).getStreams());
            super.play();

            //If the next linear ad has a clickThrough URL, create the Learn More button only if it doesn't exist
            if (currentAd() != null && currentAd().getClickThroughURL() != null) {
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
    if (currentAd() != null && currentAd().getClickTrackingURLs() != null) {
      Set<String> urls = currentAd().getClickTrackingURLs();
      if (urls != null) {
        for (String urlStr : urls) {
          final URL url = VASTUtils.urlFromAdUrlString(urlStr);
          DebugMode.logI(TAG, "Sending Click Tracking Ping: " + url);
          ping(url);
        }
      }
    }

    //Open browser to click through URL
    String url = currentAd().getClickThroughURL();
    try {
      url = url.trim(); //strip leading and trailing whitespace
      Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      _playerLayout.getContext().startActivity(browserIntent);
      DebugMode.logD(TAG, "Opening brower to " + url);
    } catch (Exception e) {
      DebugMode.logE(TAG, "There was some exception on clickthrough!");
      e.printStackTrace();
    }
  }

  public void sendTrackingEvent(String event) {
    if (currentAd() == null || currentAd().getTrackingEvents() == null) { return; }
    Set<String> urls = currentAd().getTrackingEvents().get(event);
    if (urls != null) {
      for (String urlStr : urls) {
        final URL url = VASTUtils.urlFromAdUrlString(urlStr);
        DebugMode.logI(TAG, "Sending " + event + " Tracking Ping: " + url);
        ping(url);
      }
    }
  }

  private void sendImpressionTrackingEvent(List<String> impressionURLs) {
    for(String urlStr : impressionURLs) {
      final URL url = VASTUtils.urlFromAdUrlString(urlStr);
      DebugMode.logI(TAG, "Sending Impression Tracking Ping: " + url);
      ping(url);
    }
    _impressionSent = true;
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