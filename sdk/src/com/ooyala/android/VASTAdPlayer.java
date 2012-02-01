package com.ooyala.android;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.ooyala.android.OoyalaPlayer.State;

class VASTAdPlayer extends MoviePlayer {
  private VASTAdSpot _ad;
  private List<VASTLinearAd> _linearAdQueue = new ArrayList<VASTLinearAd>();
  private NetUtils _pinger;

  private boolean _firstQSent = false;
  private boolean _midSent = false;
  private boolean _thirdQSent = false;

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

  private class VASTPlayheadUpdateTimerTask extends TimerTask {
    @Override
    public void run() {
      if (!_firstQSent && currentTime() > (currentAd().getDuration() / 4)) {
        sendTrackingEvent(TrackingEvent.FIRST_QUARTILE);
        _firstQSent = true;
      } else if (!_midSent && currentTime() > (currentAd().getDuration() / 2)) {
        sendTrackingEvent(TrackingEvent.MIDPOINT);
        _midSent = true;
      } else if (!_thirdQSent && currentTime() > (3 * currentAd().getDuration() / 4)) {
        sendTrackingEvent(TrackingEvent.THIRD_QUARTILE);
        _thirdQSent = true;
      }
      setChanged();
      notifyObservers(OoyalaPlayer.TIME_CHANGED_NOTIFICATION);
    }
  }

  @Override
  public void init(OoyalaPlayer parent, Object ad) {
    if (!(ad instanceof VASTAdSpot)) {
      this._error = "Invalid Ad";
      this._state = State.ERROR;
      return;
    }
    _seekable = false;
    _ad = (VASTAdSpot) ad;
    if (_ad.getAds() == null || _ad.getAds().isEmpty()) {
      // TODO async call to fetch playback info!
      initAfterFetch(parent);
      return;
    }
    initAfterFetch(parent);
  }

  private void initAfterFetch(OoyalaPlayer parent) {
    for (VASTAd vastAd : _ad.getAds()) {
      for (VASTSequenceItem seqItem : vastAd.getSequence()) {
        if (seqItem.hasLinear()) {
          _linearAdQueue.add(seqItem.getLinear());
        }
      }
    }

    if (_linearAdQueue.isEmpty()) { return; }

    super.init(parent, _linearAdQueue.get(0).getStream());

    // TODO[jigish] setup clickthrough

    _pinger = new NetUtils();
    for (URL url : _ad.getTrackingURLs()) {
      _pinger.ping(url);
    }
  }

  @Override
  public void play() {
    if (_linearAdQueue.isEmpty()) {
      setState(State.COMPLETED);
      return;
    }
    if (_player == null) {
      super.play();
      return;
    } // while state is loading, player will be null, so just call super to
      // queue
    if (currentTime() != 0) {
      sendTrackingEvent(TrackingEvent.RESUME);
    } else {
      sendTrackingEvent(TrackingEvent.START);
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

  public void sendTrackingEvent(String event) {
    if (currentAd() == null || currentAd().getTrackingEvents() == null) { return; }
    Set<String> urls = currentAd().getTrackingEvents().get(event);
    if (urls != null) {
      for (String url : urls) {
        _pinger.ping(urlFromAdUrlString(url));
      }
    }
  }

  public VASTAdSpot getAd() {
    return _ad;
  }

  @Override
  protected void currentItemCompleted() {
    _linearAdQueue.remove(0);
    sendTrackingEvent(TrackingEvent.COMPLETE);
    if (_linearAdQueue.isEmpty()) {
      super.currentItemCompleted();
    } else {
      OoyalaPlayer parent = _parent;
      destroy();
      super.init(parent, _linearAdQueue.get(0).getStream());
    }
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

  // Timer tasks for playhead updates
  @Override
  protected void startPlayheadTimer() {
    _playheadUpdateTimer = new Timer();
    _playheadUpdateTimer.scheduleAtFixedRate(new VASTPlayheadUpdateTimerTask(), TIMER_DELAY, TIMER_PERIOD);
  }
}