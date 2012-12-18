package com.ooyala.android;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import com.ooyala.android.OoyalaPlayer.State;

class VASTAdPlayer extends MoviePlayer {
  private VASTAdSpot _ad;
  private List<VASTLinearAd> _linearAdQueue = new ArrayList<VASTLinearAd>();

  private boolean _startSent = false;
  private boolean _firstQSent = false;
  private boolean _midSent = false;
  private boolean _thirdQSent = false;

  private Object _fetchTask;

  private interface TrackingEvent {
    public static final String START = "start";
    public static final String FIRST_QUARTILE = "firstQuartile";
    public static final String MIDPOINT = "midpoint";
    public static final String THIRD_QUARTILE = "thirdQuartile";
    public static final String COMPLETE = "complete";
    public static final String PAUSE = "pause";
    public static final String RESUME = "resume";
  }

  private static final List<String> URL_STRINGS_TO_REPLACE = Arrays.asList("%5BPlace_Random_Number_Here%5D",
      "[Place_Random_Number_Here]", "%3Cnow%3E", "%3Crand-num%3E", "[TIMESTAMP]", "%5BTIMESTAMP%5E");


  @Override
  public void init(final OoyalaPlayer parent, Object ad) {
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
      for (VASTSequenceItem seqItem : vastAd.getSequence()) {
        if (seqItem.hasLinear()) {
          _linearAdQueue.add(seqItem.getLinear());
        }
      }
    }

    if (_linearAdQueue.isEmpty()) { return false; }
    if (_linearAdQueue.get(0) == null || _linearAdQueue.get(0).getStreams() == null)  { return false; }

    addQuartileBoundaryObserver();
    
    super.init(parent, _linearAdQueue.get(0).getStreams());

    // TODO[jigish] setup clickthrough

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
    if (_state == State.PLAYING) {
      sendTrackingEvent(TrackingEvent.PAUSE);
    }
    super.pause();
  }



  public VASTAdSpot getAd() {
    return _ad;
  }

  @Override
  protected void setState(State state) {
    //look for state changing to complete here to ensure it happens before any observers notified.
    if (state == State.COMPLETED) {
      _linearAdQueue.remove(0);
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

  private URL urlFromAdUrlString(String url) {
    String timestamp = "" + (System.currentTimeMillis() / 1000);
    String newURL = url;
    for (String replace : URL_STRINGS_TO_REPLACE) {
      newURL.replaceAll(replace, timestamp);
    }
    try {
      return new URL(newURL);
    } catch (Exception e) {
      return null;
    }
  }

  private void addQuartileBoundaryObserver() {
    _startSent = false;
    _firstQSent = false;
    _midSent = false;
    _thirdQSent = false;
    addObserver(this);
  }

  public void update(Observable arg0, Object arg) {
    if (arg == OoyalaPlayer.TIME_CHANGED_NOTIFICATION) {
      if (!_startSent && currentTime() > 0) {
        sendTrackingEvent(TrackingEvent.START);
        _firstQSent = true;
      } else if (!_firstQSent && currentTime() > (currentAd().getDuration() / 4)) {
        sendTrackingEvent(TrackingEvent.FIRST_QUARTILE);
        _firstQSent = true;
      } else if (!_midSent && currentTime() > (currentAd().getDuration() / 2)) {
        sendTrackingEvent(TrackingEvent.MIDPOINT);
        _midSent = true;
      } else if (!_thirdQSent && currentTime() > (3 * currentAd().getDuration() / 4)) {
        sendTrackingEvent(TrackingEvent.THIRD_QUARTILE);
        _thirdQSent = true;
      }
    }
  }

  public void sendTrackingEvent(String event) {
    if (currentAd() == null || currentAd().getTrackingEvents() == null) { return; }
    Set<String> urls = currentAd().getTrackingEvents().get(event);
    if (urls != null) {
      for (String url : urls) {
        NetUtils.ping(urlFromAdUrlString(url));
      }
    }
  }

  @Override
  public void destroy() {
    if (_fetchTask != null && this._parent != null) this._parent.getPlayerAPIClient().cancel(_fetchTask);
    deleteObserver(this);
    super.destroy();
  }
}