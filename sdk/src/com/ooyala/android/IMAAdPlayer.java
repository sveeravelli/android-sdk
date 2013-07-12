package com.ooyala.android;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import android.util.Log;

import com.ooyala.android.OoyalaPlayer.State;

class IMAAdPlayer extends AdMoviePlayer {
  private IMAAdSpot _ad;

  private boolean _startSent = false;
  private boolean _firstQSent = false;
  private boolean _midSent = false;
  private boolean _thirdQSent = false;
  static String TAG= "IMAAdPlayer";

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


  @Override
  public void init(final OoyalaPlayer parent, AdSpot ad) {
    if (!(ad instanceof IMAAdSpot)) {
      this._error = "Invalid Ad";
      this._state = State.ERROR;
      return;
    }
    _seekable = false;
    _ad = (IMAAdSpot) ad;
    super.init(parent, _ad.getStreams());
    Log.d(TAG, "I'm Initing the ad player!");
  }


  @Override
  public void play() {
    Log.d(TAG, "Playing the ad player");

    super.play();
  }

  @Override
  public void pause() {
    Log.d(TAG, "Pausing ad player");
  }


  public IMAAdSpot getAd() {
    return _ad;
  }

//  @Override
//  protected void setState(State state) {
//    //look for state changing to complete here to ensure it happens before any observers notified.
//    if (state == State.COMPLETED) {
//      if(_linearAdQueue.size() > 0) _linearAdQueue.remove(0);
//      sendTrackingEvent(TrackingEvent.COMPLETE);
//      if (!_linearAdQueue.isEmpty()) {
//        addQuartileBoundaryObserver();
//        super.init(_parent, _linearAdQueue.get(0).getStreams());
//        return;
//      }
//    }
//    super.setState(state);
//  }

  public void sendTrackingEvent(String event) {
//    if (currentAd() == null || currentAd().getTrackingEvents() == null) { return; }
//    Set<String> urls = currentAd().getTrackingEvents().get(event);
//    if (urls != null) {
//      for (String url : urls) {
//        NetUtils.ping(urlFromAdUrlString(url));
//      }
//    }
  }

  @Override
  public void destroy() {
    if (_fetchTask != null && this._parent != null) this._parent.getPlayerAPIClient().cancel(_fetchTask);
    deleteObserver(this);
    super.destroy();
  }
}