package com.ooyala.android;

import android.util.Log;

import com.ooyala.android.OoyalaPlayer.State;

public class IMAAdPlayer extends AdMoviePlayer {
  private IMAAdSpot _ad;

  static String TAG= "IMAAdPlayer";

  private Object _fetchTask;

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


  @Override
  public void setState(State state){
    Log.d(TAG, "Setting IMA State: " + state);
    super.setState(state);
  }

  public IMAAdSpot getAd() {
    return _ad;
  }

}