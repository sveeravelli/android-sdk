package com.ooyala.android.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.captions.ClosedCaptionsStyle;

import java.util.Timer;
import java.util.TimerTask;

public abstract class AbstractDefaultOoyalaPlayerControls implements OoyalaPlayerControls {
  protected OoyalaPlayerLayout _layout = null;
  protected OoyalaPlayer _player = null;
  protected Timer _hideTimer = null;
  protected FrameLayout _baseLayout = null;
  protected AbstractOoyalaPlayerLayoutController _playerLayoutController = null;

  protected static final int HIDE_AFTER_MILLIS = 5000;

  protected static final int PREFERRED_BUTTON_WIDTH_DP = 40;
  protected static final int PREFERRED_BUTTON_HEIGHT_DP = 35;
  protected static final int MARGIN_SIZE_DP = 5;
  protected static final int BACKGROUND_COLOR = Color.TRANSPARENT;
  protected static final int SOFT_WHITE_COLOR = Color.argb(245, 240, 240, 240);

  protected boolean _isPlayerReady = false;
  protected boolean _isVisible = true;

  protected class HideTimerTask extends TimerTask {
    @Override
    public void run() {
      _hideHandler.sendEmptyMessage(0);
    }
  }

  // This is required because android enjoys making things difficult. talk to jigish if you got issues.
  protected final Handler _hideHandler = new Handler(new Handler.Callback() {

    @Override
    public boolean handleMessage(Message msg) {
      if (_player.isPlaying()) {
        hide();
      }
      return false;
    }
  });

  protected class TouchButton extends ImageButton {
    protected boolean _touching = false;

    public TouchButton(Context context) {
      super(context);
      this.setBackgroundDrawable(null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      if (event.getAction() == MotionEvent.ACTION_DOWN) {
        _touching = true;
        invalidate();
      } else if (event.getAction() == MotionEvent.ACTION_UP) {
        _touching = false;
        invalidate();
      }
      return super.onTouchEvent(event);
    }
  }

  protected class NextButton extends TouchButton {
    public NextButton(Context context) {
      super(context);
    }

    @Override
    protected void onDraw(Canvas c) {
      if (c == null) { return; }
      Images.drawImage(Images.NEXT, this.getContext(), c, BACKGROUND_COLOR, SOFT_WHITE_COLOR, getWidth(),
          getHeight(), MARGIN_SIZE_DP, _touching);
    }
  }

  protected class PreviousButton extends TouchButton {
    public PreviousButton(Context context) {
      super(context);
    }

    @Override
    protected void onDraw(Canvas c) {
      if (c == null) { return; }
      Images.drawImage(Images.PREVIOUS, this.getContext(), c, BACKGROUND_COLOR, SOFT_WHITE_COLOR, getWidth(),
          getHeight(), MARGIN_SIZE_DP, _touching);
    }
  }

  protected class PlayPauseButton extends TouchButton {
    private boolean _playing = false;

    public PlayPauseButton(Context context) {
      super(context);
    }

    public void setPlaying(boolean playing) {
      _playing = playing;
      invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
      if (c == null) { return; }
      if (_playing) {
        Images.drawImage(Images.PAUSE, this.getContext(), c, BACKGROUND_COLOR, SOFT_WHITE_COLOR, getWidth(),
            getHeight(), MARGIN_SIZE_DP, _touching);
      } else {
        Images.drawImage(Images.PLAY, this.getContext(), c, BACKGROUND_COLOR, SOFT_WHITE_COLOR, getWidth(),
            getHeight(), MARGIN_SIZE_DP, _touching);
      }
    }
  }

  protected class FullscreenButton extends TouchButton {
    private boolean _fullscreen = false;

    public FullscreenButton(Context context) {
      super(context);
    }

    public void setFullscreen(boolean fullscreen) {
      _fullscreen = fullscreen;
      invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
      if (c == null) { return; }
      if (_fullscreen) {
        Images.drawImage(Images.SMALLSCREEN, this.getContext(), c, BACKGROUND_COLOR, SOFT_WHITE_COLOR,
            getWidth(), getHeight(), MARGIN_SIZE_DP, _touching);
      } else {
        Images.drawImage(Images.FULLSCREEN, this.getContext(), c, BACKGROUND_COLOR, SOFT_WHITE_COLOR,
            getWidth(), getHeight(), MARGIN_SIZE_DP, _touching);
      }
    }
  }

  /**
   * Closed Captons Button
   */
  protected class ClosedCaptionsButton extends TouchButton {
    public ClosedCaptionsButton(Context context) {
      super(context);
    }

    @Override
    protected void onDraw(Canvas c) {
      if (c == null) {
        return;
      }
      Images.drawImage(Images.CLOSED_CAPTIONS, this.getContext(), c,BACKGROUND_COLOR,
          SOFT_WHITE_COLOR, getWidth(), getHeight(), MARGIN_SIZE_DP,
          _touching);
    }
  }

  @Override
  public void setParentLayout(OoyalaPlayerLayout layout) {
    _layout = layout;
    _playerLayoutController = null;
    if (_layout != null  &&
        (_layout.getLayoutController() instanceof AbstractOoyalaPlayerLayoutController)) {
      _playerLayoutController = (AbstractOoyalaPlayerLayoutController)_layout.getLayoutController();
    } else {
      _playerLayoutController = null;
    }
  }

  @Override
  public void setOoyalaPlayer(OoyalaPlayer player) {
    _player = player;
  }

  @Override
  public void show() {
    if (!_isVisible || _player.showingAdWithHiddenControlls()) return;

    if (_hideTimer != null) {
      _hideTimer.cancel();
      _hideTimer = null;
    }
    _baseLayout.setVisibility(FrameLayout.VISIBLE);
    _baseLayout.bringToFront();
    updateButtonStates();
    _hideTimer = new Timer();
    _hideTimer.schedule(new HideTimerTask(), HIDE_AFTER_MILLIS);
    if (_playerLayoutController != null && _isPlayerReady) {
      ClosedCaptionsStyle ccStyle = _playerLayoutController.getClosedCaptionsStyle();
      if (ccStyle != null) {
        ccStyle.bottomMargin = this.bottomBarOffset();
        _playerLayoutController.setClosedCaptionsBottomMargin(this.bottomBarOffset());
      }
    }
  }

  @Override
  public void hide() {
    if (_playerLayoutController != null && _isPlayerReady) {
      ClosedCaptionsStyle ccStyle = _playerLayoutController.getClosedCaptionsStyle();
      if (ccStyle != null) {
        _playerLayoutController.setClosedCaptionsBottomMargin(MARGIN_SIZE_DP * 4);
      }
    }
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

  @Override
  public int bottomBarOffset() {
    return 0;
  }

  @Override
  public int topBarOffset() {
    return 0;
  }

  @Override
  public void setVisible(boolean visible) {
    _isVisible = visible;
    if (!visible) {
      hide();
    }
  }

  protected abstract void updateButtonStates();

  protected abstract void setupControls();
}
