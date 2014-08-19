package com.ooyala.pluginsampleapp;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.player.PlayerInterface;
import com.ooyala.android.plugin.LifeCycleInterface;

public class SampleAdPlayer extends TextView implements PlayerInterface,
    LifeCycleInterface {
  private final int DURATION = 5000;
  private final int REFRESH_RATE = 250;

  private WeakReference<SampleAdPlugin> _plugin;

  private State _state;
  private Timer _timer;
  private int _playhead = 0;
  private String _adText;
  private Handler _timerHandler;

  public SampleAdPlayer(Context context, SampleAdPlugin plugin, ViewGroup parent) {
    super(context);
    this.setLayoutParams(new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
    this.setBackgroundColor(Color.BLACK);
    parent.addView(this);
    _plugin = new WeakReference<SampleAdPlugin>(plugin);
    _timerHandler = new Handler() {
      public void handleMessage(Message msg) {
        refresh();
      }
    };
    setState(State.LOADING);
  }

  @Override
  public int buffer() {
    // TODO Auto-generated method stub
    return 100;
  }

  @Override
  public int currentTime() {
    return _playhead;
  }

  @Override
  public int duration() {
    return DURATION;
  }

  @Override
  public State getState() {
    return _state;
  }

  @Override
  public void pause() {

  }

  @Override
  public void play() {
    if (_timer == null) {
      _timer = new Timer();
      _timer.scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
          _timerHandler.sendEmptyMessage(0);
        }
      }, REFRESH_RATE, REFRESH_RATE);
    }
    setState(State.PLAYING);
  }

  @Override
  public void seekToTime(int arg0) {
  }

  @Override
  public boolean seekable() {
    return false;
  }

  @Override
  public void stop() {
  }

  private void refresh() {
    _playhead += REFRESH_RATE;
    String text = _adText + " " + String.valueOf((DURATION - _playhead) / 1000);
    this.setText(text);
    if (_playhead >= DURATION) {
      _timer.cancel();
      _timer = null;
      setState(State.COMPLETED);
    } else {
      _plugin.get().onPlayerPlayheadChange();
    }

  }

  @Override
  public void destroy() {
    // TODO Auto-generated method stub
  }

  @Override
  public void reset() {
    // TODO Auto-generated method stub

  }

  @Override
  public void resume() {
    // TODO Auto-generated method stub

  }

  @Override
  public void resume(int arg0, State arg1) {
    // TODO Auto-generated method stub

  }

  @Override
  public void suspend() {
    // TODO Auto-generated method stub

  }

  public void loadAd(SampleAdSpot ad) {
    if (ad != null) {
      _adText = ad.text();
    } else {
      _adText = "null";
    }
    this.setText(_adText);
    setState(State.READY);
  }

  public void setState(State state) {
    _state = state;
    _plugin.get().onPlayerStateChange();
  }
}
