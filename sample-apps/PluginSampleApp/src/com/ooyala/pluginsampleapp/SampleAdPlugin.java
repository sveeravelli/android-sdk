package com.ooyala.pluginsampleapp;

import java.lang.ref.WeakReference;
import java.util.Set;

import android.content.Context;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.StateNotifier;
import com.ooyala.android.StateNotifierListener;
import com.ooyala.android.player.PlayerInterface;
import com.ooyala.android.plugin.AdPluginInterface;

public class SampleAdPlugin implements AdPluginInterface, StateNotifierListener {
  private WeakReference<OoyalaPlayer> _player;
  private Context _context;

  private SampleAdPlayer _adPlayer;
  private SampleAdSpot _preroll;
  private SampleAdSpot _midroll;
  private SampleAdSpot _postroll;
  private SampleAdSpot _adToPlay;
  private StateNotifier _stateNotifier;

  SampleAdPlugin(Context context, OoyalaPlayer player) {
    _player = new WeakReference<OoyalaPlayer>(player);
    _context = context;
    _stateNotifier = _player.get().createStateNotifier();
    _stateNotifier.addListener(this);
  }

  @Override
  public void destroy() {
    if (_adPlayer != null) {
      _adPlayer.destroy();
    }
  }

  @Override
  public void reset() {
  }

  @Override
  public void resume() {
  }

  @Override
  public void resume(int arg0, State arg1) {
  }

  @Override
  public void suspend() {
  }

  @Override
  public PlayerInterface getPlayerInterface() {
    return _adPlayer;
  }

  @Override
  public void onAdModeEntered() {
    if (_adToPlay == null) {
      _player.get().exitAdMode(this);
    } else {
      this.playAd(_adToPlay);
      // Mark ad as played.
      _adToPlay.setPlayed(true);
      _adToPlay = null;
    }
  }

  @Override
  public boolean onContentChanged() {
    // load ads info.
    _preroll = new SampleAdSpot(0, "PREROLL");
    _midroll = new SampleAdSpot(6000, "MIDROLL");
    _postroll = new SampleAdSpot(Integer.MAX_VALUE - 1000, "POSTROLL");
    return false;
  }

  @Override
  public boolean onContentError(int arg0) {
    return false;
  }

  @Override
  public boolean onContentFinished() {
    if (!_postroll.isPlayed()) {
      _adToPlay = _postroll;
      return true;
    }
    return false;
  }

  @Override
  public boolean onCuePoint(int arg0) {
    return false;
  }

  @Override
  public boolean onInitialPlay() {
    if (!_preroll.isPlayed()) {
      _adToPlay = _preroll;
      return true;
    }
    return false;
  }

  @Override
  public boolean onPlayheadUpdate(int playhead) {
    if (playhead >= _midroll.getTime() && !_midroll.isPlayed()) {
      _adToPlay = _midroll;
      return true;
    }
    return false;
  }

  @Override
  public void resetAds() {
    _preroll.setPlayed(false);
    _midroll.setPlayed(false);
    _postroll.setPlayed(false);
  }

  @Override
  public void skipAd() {

  }

  private void playAd(SampleAdSpot ad) {
    _adPlayer = new SampleAdPlayer(_context, _stateNotifier, _player.get()
        .getLayout());
    _adPlayer.loadAd(_adToPlay);
    _adPlayer.play();
  }

  @Override
  public void onStateChange(StateNotifier notifier) {
    if (_adPlayer == null) {
      return;
    }

    if (_stateNotifier.getState() == State.COMPLETED) {
      _player.get().getLayout().removeView(_adPlayer);
      _adPlayer.destroy();
      _adPlayer = null;
      _player.get().exitAdMode(this);
    }
  }

  @Override
  public Set<Integer> getCuePointsInMilliSeconds() {
    return null;
  }
}
