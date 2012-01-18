package com.ooyala.android;

import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class DefaultOoyalaPlayerControls implements OoyalaPlayerControls,
                                                    SeekBar.OnSeekBarChangeListener,
                                                    Button.OnClickListener,
                                                    Observer {
  private OoyalaPlayerLayout _layout = null;
  private OoyalaPlayer _player = null;
  private FrameLayout _baseLayout = null;
  private LinearLayout _controls = null;
  private LinearLayout _fullscreenWrapper = null;
  private LinearLayout _buttonWrapper = null;
  private LinearLayout _seekWrapper = null;
  private Button _playPause = null;
  private Button _previous = null;
  private Button _next = null;
  private Button _fullscreen = null;
  private SeekBar _seek = null;
  private TextView _currTime = null;
  private TextView _duration = null;
  private Timer _hideTimer = null;

  private class HideTimerTask extends TimerTask {
    @Override
    public void run() {
      _hideHandler.sendEmptyMessage(0);
    }
  }

  //This is required because android enjoys making things difficult. talk to jigish if you got issues.
  private final Handler _hideHandler = new Handler() {
    public void handleMessage(Message msg) {
      hide();
    }
  };

  public DefaultOoyalaPlayerControls(OoyalaPlayer player, OoyalaPlayerLayout layout) {
    setParentLayout(layout);
    setOoyalaPlayer(player);
    setupControls();
  }

  @Override
  public void setParentLayout(OoyalaPlayerLayout layout) {
    _layout = layout;
  }

  @Override
  public void setOoyalaPlayer(OoyalaPlayer player) {
    _player = player;
  }

  @Override
  public void show() {
    if (_hideTimer != null) {
      _hideTimer.cancel();
      _hideTimer = null;
    }
    _baseLayout.setVisibility(FrameLayout.VISIBLE);
    _baseLayout.bringToFront();
    updateButtonStates();
    _hideTimer = new Timer();
    _hideTimer.schedule(new HideTimerTask(), 3000);
  }

  @Override
  public void hide() {
    if (_hideTimer != null) {
      _hideTimer.cancel();
      _hideTimer = null;
    }
    _baseLayout.setVisibility(FrameLayout.GONE);
  }

  @Override
  public boolean isShowing() {
    return _baseLayout.getVisibility() == FrameLayout.VISIBLE;
  }

  private void updateButtonStates() {
    _playPause.setText(_player.isPlaying() ? "||" : ">");
    _fullscreen.setText(_player.isFullscreen() ? "><" : "<>");
  }

  private void setupControls() {
    if (_layout == null) { return; }
    _baseLayout = new FrameLayout(_layout.getContext());
    _baseLayout.setBackgroundColor(Color.TRANSPARENT);

    _controls = new LinearLayout(_baseLayout.getContext());
    _controls.setOrientation(LinearLayout.VERTICAL);
    _controls.setBackgroundColor(Color.argb(200, 0, 0, 0));

    _seekWrapper = new LinearLayout(_controls.getContext());
    _seekWrapper.setOrientation(LinearLayout.HORIZONTAL);
    _currTime = new TextView(_seekWrapper.getContext());
    _currTime.setText("00:00:00");
    LinearLayout.LayoutParams currTimeLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    currTimeLP.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
    _currTime.setLayoutParams(currTimeLP);
    _seek = new SeekBar(_seekWrapper.getContext());
    LinearLayout.LayoutParams seekLP = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
    seekLP.gravity = Gravity.CENTER;
    seekLP.leftMargin = 5;
    seekLP.rightMargin = 5;
    _seek.setLayoutParams(seekLP);
    _seek.setOnSeekBarChangeListener(this);
    _duration = new TextView(_seekWrapper.getContext());
    _duration.setText("00:00:00");
    LinearLayout.LayoutParams durationLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    durationLP.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
    _duration.setLayoutParams(durationLP);
    _seekWrapper.addView(_currTime);
    _seekWrapper.addView(_seek);
    _seekWrapper.addView(_duration);
    LinearLayout.LayoutParams seekWrapperLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    seekWrapperLP.gravity = Gravity.CENTER_HORIZONTAL;
    _seekWrapper.setLayoutParams(seekWrapperLP);

    _buttonWrapper = new LinearLayout(_controls.getContext());
    _buttonWrapper.setOrientation(LinearLayout.HORIZONTAL);
    _previous = new Button(_buttonWrapper.getContext());
    _previous.setText("|<<");
    _previous.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    _previous.setOnClickListener(this);
    _playPause = new Button(_buttonWrapper.getContext());
    _playPause.setText(">");
    _playPause.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    _playPause.setOnClickListener(this);
    _next = new Button(_buttonWrapper.getContext());
    _next.setText(">>|");
    _next.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    _next.setOnClickListener(this);
    _buttonWrapper.addView(_previous);
    _buttonWrapper.addView(_playPause);
    _buttonWrapper.addView(_next);
    LinearLayout.LayoutParams buttonWrapperLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    buttonWrapperLP.gravity = Gravity.CENTER_HORIZONTAL;
    _buttonWrapper.setLayoutParams(buttonWrapperLP);

    _controls.addView(_seekWrapper);
    _controls.addView(_buttonWrapper);
    FrameLayout.LayoutParams controlsLP = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
    _baseLayout.addView(_controls, controlsLP);

    _fullscreenWrapper = new LinearLayout(_baseLayout.getContext());
    _fullscreenWrapper.setOrientation(LinearLayout.VERTICAL);
    _fullscreenWrapper.setBackgroundColor(Color.argb(200, 0, 0, 0));
    _fullscreen = new Button(_fullscreenWrapper.getContext());
    _fullscreen.setText("<>");
    _fullscreen.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    _fullscreen.setOnClickListener(this);
    _fullscreenWrapper.addView(_fullscreen);
    FrameLayout.LayoutParams fsWrapperLP = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.RIGHT);
    _baseLayout.addView(_fullscreenWrapper, fsWrapperLP);

    FrameLayout.LayoutParams baseLP = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    _layout.addView(_baseLayout, baseLP);
    hide();
    _player.addObserver(this);
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    if (fromUser) {
      _player.seekToPercent(progress);
    }
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
    // noop
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
    // noop
  }

  @Override
  public void onClick(View v) {
    if (v == _previous) {
      _player.previousVideo(_player.isPlaying() ? OoyalaPlayer.DO_PLAY : OoyalaPlayer.DO_PAUSE);
    } else if (v == _next) {
      _player.nextVideo(_player.isPlaying() ? OoyalaPlayer.DO_PLAY : OoyalaPlayer.DO_PAUSE);
    } else if (v == _playPause) {
      if (_player.isPlaying()) { _player.pause(); }
      else { _player.play(); }
      updateButtonStates();
    } else if (v == _fullscreen) {
      _player.setFullscreen(!_player.isFullscreen());
      updateButtonStates();
      hide();
    }
  }

  @Override
  public void update(Observable arg0, Object arg1) {
    _seek.setProgress(_player.getPlayheadPercentage());
    _seek.setSecondaryProgress(_player.getBufferPercentage());
    _duration.setText(Utils.timeStringFromMillis(_player.getDuration()));
    _currTime.setText(Utils.timeStringFromMillis(_player.getPlayheadTime()));
  }
}
