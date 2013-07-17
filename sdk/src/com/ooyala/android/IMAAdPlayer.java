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
    Log.d(TAG, "Initializing the IMA player");

    if (!(ad instanceof IMAAdSpot)) {
      this._error = "Invalid Ad";
      this._state = State.ERROR;
      return;
    }
    _seekable = false;
    _ad = (IMAAdSpot) ad;
    super.init(parent, _ad.getStreams());
  }


  @Override
  public void play() {
    Log.d(TAG, "Playing the IMA player");
    super.play();
  }

  @Override
  public void pause() {
    Log.d(TAG, "Pausing IMA player");
    super.pause();
  }

  @Override
  public void destroy() {
    if (_fetchTask != null && this._parent != null) this._parent.getPlayerAPIClient().cancel(_fetchTask);
    deleteObserver(this);
    super.destroy();
  }


  public IMAAdSpot getAd() {
    return _ad;
  }

}