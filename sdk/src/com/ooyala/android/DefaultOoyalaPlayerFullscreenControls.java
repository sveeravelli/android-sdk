package com.ooyala.android;

import java.util.Observable;
import java.util.Observer;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ooyala.android.OoyalaPlayer.State;

public class DefaultOoyalaPlayerFullscreenControls extends AbstractDefaultOoyalaPlayerControls implements
    SeekBar.OnSeekBarChangeListener, Button.OnClickListener, Observer {
  private LinearLayout _bottomOverlay = null;
  private LinearLayout _topBar = null;
  private LinearLayout _seekWrapper = null;
  private LinearLayout _liveWrapper = null;
  private PlayPauseButton _playPause = null;
  private NextButton _next = null;
  private PreviousButton _previous = null;
  private FullscreenButton _fullscreen = null;
  private SeekBar _seek = null;
  private TextView _currTime = null;
  private TextView _duration = null;
  private TextView _liveIndicator = null;
  private ProgressBar _spinner = null;

  private static final float OVERLAY_SCALE = 1.2f;
  private static final int OVERLAY_PREFERRED_BUTTON_WIDTH_DP = (int) ((float) PREFERRED_BUTTON_WIDTH_DP * OVERLAY_SCALE);
  private static final int OVERLAY_PREFERRED_BUTTON_HEIGHT_DP = (int) ((float) PREFERRED_BUTTON_HEIGHT_DP * OVERLAY_SCALE);
  private static final int OVERLAY_MARGIN_SIZE_DP = 10;
  private static final int OVERLAY_BACKGROUND_COLOR = Color.argb(200, 0, 0, 0);

  public DefaultOoyalaPlayerFullscreenControls(OoyalaPlayer player, OoyalaPlayerLayout layout) {
    setParentLayout(layout);
    setOoyalaPlayer(player);
    setupControls();
  }

  @Override
  protected void updateButtonStates() {
    _playPause.setPlaying(_player.isPlaying());
    _fullscreen.setFullscreen(_player.isFullscreen());

    if (_seekWrapper != null)
      _seekWrapper.setVisibility(_player.getCurrentItem().isLive() ? View.GONE : View.VISIBLE);
    if (_liveWrapper != null) {
      _liveWrapper.setVisibility(_player.getCurrentItem().isLive() ? View.VISIBLE : View.GONE);
      if (Build.VERSION.SDK_INT >= Constants.SDK_INT_HONEYCOMB) {
        _liveWrapper.setAlpha(_player.isShowingAd() ? 0.4f : 1f); // supported only 11+
      }
    }
  }

  @Override
  protected void setupControls() {
    if (_layout == null) { return; }
    _baseLayout = new FrameLayout(_layout.getContext());
    _baseLayout.setBackgroundColor(Color.TRANSPARENT);

    _bottomOverlay = new LinearLayout(_baseLayout.getContext());
    _bottomOverlay.setOrientation(LinearLayout.HORIZONTAL);
    _bottomOverlay.setBackgroundColor(OVERLAY_BACKGROUND_COLOR);

    _previous = new PreviousButton(_bottomOverlay.getContext());
    LinearLayout.LayoutParams previousLP = new LinearLayout.LayoutParams(Images.dpToPixels(
        _baseLayout.getContext(), OVERLAY_PREFERRED_BUTTON_WIDTH_DP), Images.dpToPixels(
        _baseLayout.getContext(), OVERLAY_PREFERRED_BUTTON_HEIGHT_DP));
    previousLP.leftMargin = Images.dpToPixels(_baseLayout.getContext(), OVERLAY_MARGIN_SIZE_DP * 2);
    previousLP.rightMargin = Images.dpToPixels(_baseLayout.getContext(), 0);
    previousLP.topMargin = Images.dpToPixels(_baseLayout.getContext(), OVERLAY_MARGIN_SIZE_DP);
    previousLP.bottomMargin = Images.dpToPixels(_baseLayout.getContext(), OVERLAY_MARGIN_SIZE_DP);
    _previous.setLayoutParams(previousLP);
    _previous.setOnClickListener(this);

    _playPause = new PlayPauseButton(_bottomOverlay.getContext());
    _playPause.setPlaying(_player.isPlaying());
    LinearLayout.LayoutParams ppLP = new LinearLayout.LayoutParams(Images.dpToPixels(
        _baseLayout.getContext(), OVERLAY_PREFERRED_BUTTON_WIDTH_DP), Images.dpToPixels(
        _baseLayout.getContext(), OVERLAY_PREFERRED_BUTTON_HEIGHT_DP));
    ppLP.leftMargin = Images.dpToPixels(_baseLayout.getContext(), OVERLAY_MARGIN_SIZE_DP);
    ppLP.rightMargin = Images.dpToPixels(_baseLayout.getContext(), OVERLAY_MARGIN_SIZE_DP);
    ppLP.topMargin = Images.dpToPixels(_baseLayout.getContext(), OVERLAY_MARGIN_SIZE_DP);
    ppLP.bottomMargin = Images.dpToPixels(_baseLayout.getContext(), OVERLAY_MARGIN_SIZE_DP);
    _playPause.setLayoutParams(ppLP);
    _playPause.setOnClickListener(this);

    _next = new NextButton(_bottomOverlay.getContext());
    LinearLayout.LayoutParams nextLP = new LinearLayout.LayoutParams(Images.dpToPixels(
        _baseLayout.getContext(), OVERLAY_PREFERRED_BUTTON_WIDTH_DP), Images.dpToPixels(
        _baseLayout.getContext(), OVERLAY_PREFERRED_BUTTON_HEIGHT_DP));
    nextLP.leftMargin = Images.dpToPixels(_baseLayout.getContext(), 0);
    nextLP.rightMargin = Images.dpToPixels(_baseLayout.getContext(), OVERLAY_MARGIN_SIZE_DP * 2);
    nextLP.topMargin = Images.dpToPixels(_baseLayout.getContext(), OVERLAY_MARGIN_SIZE_DP);
    nextLP.bottomMargin = Images.dpToPixels(_baseLayout.getContext(), OVERLAY_MARGIN_SIZE_DP);
    _next.setLayoutParams(nextLP);
    _next.setOnClickListener(this);

    _bottomOverlay.addView(_previous);
    _bottomOverlay.addView(_playPause);
    _bottomOverlay.addView(_next);
    FrameLayout.LayoutParams bottomOverlayLP = new FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM
            | Gravity.CENTER_HORIZONTAL);
    bottomOverlayLP.bottomMargin = Images.dpToPixels(_baseLayout.getContext(), OVERLAY_MARGIN_SIZE_DP);
    _baseLayout.addView(_bottomOverlay, bottomOverlayLP);

    _topBar = new LinearLayout(_baseLayout.getContext());
    _topBar.setOrientation(LinearLayout.HORIZONTAL);
    _topBar.setBackgroundDrawable(Images.gradientBackground(GradientDrawable.Orientation.TOP_BOTTOM));

    _seekWrapper = new LinearLayout(_topBar.getContext());
    _seekWrapper.setOrientation(LinearLayout.HORIZONTAL);
    _currTime = new TextView(_seekWrapper.getContext());
    _currTime.setText("00:00:00");
    LinearLayout.LayoutParams currTimeLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);
    currTimeLP.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
    _currTime.setLayoutParams(currTimeLP);
    _seek = new SeekBar(_seekWrapper.getContext());
    LinearLayout.LayoutParams seekLP = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,
        1f);
    seekLP.gravity = Gravity.CENTER;
    seekLP.leftMargin = Images.dpToPixels(_baseLayout.getContext(), MARGIN_SIZE_DP);
    seekLP.rightMargin = Images.dpToPixels(_baseLayout.getContext(), MARGIN_SIZE_DP);
    _seek.setLayoutParams(seekLP);
    _seek.setOnSeekBarChangeListener(this);
    _duration = new TextView(_seekWrapper.getContext());
    _duration.setText("00:00:00");
    LinearLayout.LayoutParams durationLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);
    durationLP.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
    _duration.setLayoutParams(durationLP);
    _seekWrapper.addView(_currTime);
    _seekWrapper.addView(_seek);
    _seekWrapper.addView(_duration);
    LinearLayout.LayoutParams seekWrapperLP = new LinearLayout.LayoutParams(0,
        ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
    seekWrapperLP.gravity = Gravity.CENTER;
    seekWrapperLP.leftMargin = Images.dpToPixels(_baseLayout.getContext(), MARGIN_SIZE_DP);
    seekWrapperLP.rightMargin = Images.dpToPixels(_baseLayout.getContext(), MARGIN_SIZE_DP);
    _seekWrapper.setLayoutParams(seekWrapperLP);

    _liveWrapper = new LinearLayout(_topBar.getContext());
    _liveWrapper.setVisibility(View.GONE);
    _liveWrapper.setOrientation(LinearLayout.HORIZONTAL);
    _liveIndicator = new TextView(_liveWrapper.getContext());
    _liveIndicator.setText(LocalizationSupport.localizedStringFor("LIVE"));
    _liveIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
    LinearLayout.LayoutParams liveIndicatorLP = new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    liveIndicatorLP.gravity = Gravity.CENTER;
    _liveIndicator.setLayoutParams(liveIndicatorLP);
    _liveWrapper.addView(_liveIndicator);
    LinearLayout.LayoutParams liveWrapperLP = new LinearLayout.LayoutParams(0,
        ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
    liveWrapperLP.gravity = Gravity.CENTER;
    liveWrapperLP.leftMargin = Images.dpToPixels(_baseLayout.getContext(), MARGIN_SIZE_DP
        + PREFERRED_BUTTON_WIDTH_DP);
    liveWrapperLP.rightMargin = Images.dpToPixels(_baseLayout.getContext(), MARGIN_SIZE_DP);
    _liveWrapper.setLayoutParams(liveWrapperLP);

    _fullscreen = new FullscreenButton(_topBar.getContext());
    _fullscreen.setFullscreen(_player.isFullscreen());
    LinearLayout.LayoutParams fsLP = new LinearLayout.LayoutParams(Images.dpToPixels(
        _baseLayout.getContext(), PREFERRED_BUTTON_HEIGHT_DP), Images.dpToPixels(_baseLayout.getContext(),
        PREFERRED_BUTTON_HEIGHT_DP));
    fsLP.leftMargin = (PREFERRED_BUTTON_WIDTH_DP - PREFERRED_BUTTON_HEIGHT_DP) / 2;
    fsLP.rightMargin = (PREFERRED_BUTTON_WIDTH_DP - PREFERRED_BUTTON_HEIGHT_DP) / 2;
    _fullscreen.setLayoutParams(fsLP);
    _fullscreen.setOnClickListener(this);

    _topBar.addView(_seekWrapper);
    _topBar.addView(_liveWrapper);
    _topBar.addView(_fullscreen);
    FrameLayout.LayoutParams topBarLP = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
    _baseLayout.addView(_topBar, topBarLP);

    FrameLayout.LayoutParams baseLP = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT);
    _layout.addView(_baseLayout, baseLP);
    hide();

    _spinner = new ProgressBar(_layout.getContext());
    FrameLayout.LayoutParams spinnerLP = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
        FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER | Gravity.CENTER_HORIZONTAL);
    _layout.addView(_spinner, spinnerLP);

    _player.addObserver(this);
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    if (fromUser) {
      _player.seekToPercent(progress);
      _player.play();
      updateButtonStates();
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
      if (_player.isPlaying()) {
        _player.pause();
      } else {
        _player.play();
      }
      show();
    } else if (v == _fullscreen && _isPlayerReady) {
      _player.setFullscreen(!_player.isFullscreen());
      updateButtonStates();
      hide();
    }
  }

  @Override
  public void update(Observable arg0, Object arg1) {
    _seek.setProgress(_player.getPlayheadPercentage());
    _seek.setSecondaryProgress(_player.getBufferPercentage());
    boolean includeHours = _player.getDuration() >= 1000 * 60 * 60;
    _duration.setText(Utils.timeStringFromMillis(_player.getDuration(), includeHours));
    _currTime.setText(Utils.timeStringFromMillis(_player.getPlayheadTime(), includeHours));

    // update spinner
    if (arg1 == OoyalaPlayer.STATE_CHANGED_NOTIFICATION) {
      State currentState = _player.getState();
      if (currentState == State.INIT || currentState == State.LOADING) {
        _spinner.setVisibility(View.VISIBLE);
      } else {
        _spinner.setVisibility(View.INVISIBLE);
      }
      Log.d("Rui full", currentState.toString());
      if (currentState == State.READY) _isPlayerReady = true;
      if (currentState == State.SUSPENDED) {
        _isPlayerReady = false;
        hide();
      }
      if (currentState == State.PLAYING) {
        updateButtonStates();
      }
      if (!isShowing() && currentState != State.INIT && currentState != State.LOADING
          && currentState != State.ERROR && currentState != State.SUSPENDED && _player.isFullscreen()) {
        show();
      }
    }
  }

  @Override
  public int bottomBarOffset() {
    if (_baseLayout == null) return 0;
    int pixelValue = OVERLAY_PREFERRED_BUTTON_HEIGHT_DP + OVERLAY_MARGIN_SIZE_DP * 4;
    return Images.dpToPixels(_baseLayout.getContext(), pixelValue);
  }
}
