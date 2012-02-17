package com.ooyala.android;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.view.MotionEvent;

public class OoyalaPlayerLayout extends FrameLayout {
  private LayoutController _controller = null;

  private FrameLayout _playerFrame = null;

  public OoyalaPlayerLayout(Context context) {
    super(context);
    setupPlayerFrame();
  }

  public OoyalaPlayerLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    setupPlayerFrame();
  }

  public OoyalaPlayerLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setupPlayerFrame();
  }

  private void setupPlayerFrame() {
    _playerFrame = new FrameLayout(getContext());
    FrameLayout.LayoutParams frameLP = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    addView(_playerFrame, frameLP);
  }

  public FrameLayout getPlayerFrame() {
    return _playerFrame;
  }

  public LayoutController getLayoutController() {
    return _controller;
  }

  public void setLayoutController(LayoutController controller) {
    _controller = controller;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return _controller.onTouchEvent(event, this);
  }
}
