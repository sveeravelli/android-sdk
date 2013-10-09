package com.ooyala.android;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import android.util.Log;

import com.ooyala.android.OoyalaPlayer.State;

class VASTAdPlayer extends AdMoviePlayer {
  private VASTAdSpot _ad;
  private List<VASTLinearAd> _linearAdQueue = new ArrayList<VASTLinearAd>();
  private static String TAG = VASTAdPlayer.class.getName();
  private List<String> _impressionURLs = new ArrayList<String>();

  private boolean _impressionSent = false;
  private boolean _startSent = false;
  private boolean _firstQSent = false;
  private boolean _midSent = false;
  private boolean _thirdQSent = false;

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
  public void init(final OoyalaPlayer parent, AdSpot ad) {
    if (!(ad instanceof VASTAdSpot)) {
      this._error = "Invalid Ad";
      this._state = State.ERROR;
      return;
    }
    _seekable = false;
    _ad = (VASTAdSpot) ad;
    if (_ad.getAds() == null || _ad.getAds().isEmpty()) {
      if (_fetchTask != null) {
        this._parent.getPlayerAPIClient().cancel(_fetchTask);
      }
      _fetchTask = _ad.fetchPlaybackInfo(new FetchPlaybackInfoCallback() {

        @Override
        public void callback(boolean result) {
          if (!result) {
            _error = "Could not fetch VAST Ad";
            setState(State.ERROR);
            return;
          }
          if(!initAfterFetch(parent)) {
            _error = "Bad VAST Ad";
            setState(State.ERROR);
            return;
          }
        }

      });
      return;
    }
    if(!initAfterFetch(parent)) {
      _error = "Bad VAST Ad";
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

    // TODO[jigish] setup clickthrough
    Set<String> clickTracking = _linearAdQueue.get(0).getClickTrackingURLs();
    String clickThrough = _linearAdQueue.get(0).getClickThroughURL();

    if (_ad.getTrackingURLs() != null) {
      for (URL url : _ad.getTrackingURLs()) {
        NetUtils.ping(url);
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
    if (_state != State.PLAYING) {
      sendTrackingEvent(TrackingEvent.PAUSE);
    }
    super.pause();
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
        BaseMoviePlayer tempPlayer = (BaseMoviePlayer) arg0;

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
          }
        }
      } catch (Exception e) {
        // ERROR: arg0 is not a BaseMoviePlayer as expected
        Log.e(TAG, "arg0 should be a BaseMoviePlayer but is not!");
      }
    }
    super.update(arg0,  arg);
  }

  public void sendTrackingEvent(String event) {
    if (currentAd() == null || currentAd().getTrackingEvents() == null) { return; }
    Set<String> urls = currentAd().getTrackingEvents().get(event);
    if (urls != null) {
      for (String url : urls) {
        Log.i(TAG, "Sending " + event + " Tracking Ping: " + VASTAdSpot.urlFromAdUrlString(url));
        NetUtils.ping(VASTAdSpot.urlFromAdUrlString(url));
      }
    }
  }

  private void sendImpressionTrackingEvent(List<String> impressionURLs) {
    for(String url : impressionURLs) {
      Log.i(TAG, "Sending Impression Tracking Ping: " + VASTAdSpot.urlFromAdUrlString(url));
      NetUtils.ping(VASTAdSpot.urlFromAdUrlString(url));
    }
    _impressionSent = true;
  }

  @Override
  public void destroy() {
    if (_fetchTask != null && this._parent != null) this._parent.getPlayerAPIClient().cancel(_fetchTask);
    deleteObserver(this);
    super.destroy();
  }
}