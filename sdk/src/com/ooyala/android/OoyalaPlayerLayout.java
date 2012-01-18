package com.ooyala.android;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.view.MotionEvent;

public class OoyalaPlayerLayout extends FrameLayout {
  private LayoutController _controller = null;

  public OoyalaPlayerLayout(Context context) {
    super(context);
  }

  public OoyalaPlayerLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public OoyalaPlayerLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
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
