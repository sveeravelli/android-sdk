package com.ooyala.android;

import android.view.MotionEvent;
import android.widget.FrameLayout;

public interface LayoutController {
  /**
   * Get the current active layout to display the video on.
   * @return the current active layout.
   */
  public FrameLayout getLayout();

  /**
   * Set the fullscreen state
   * @param fullscreen true for fullscreen, false for inline
   */
  public void setFullscreen(boolean fullscreen);

  /**
   * Get the fullscreen state
   * @return true for fullscreen, false for inline
   */
  public boolean isFullscreen();

  /**
   * Handle the touch events from OoyalaPlayerLayout
   * @param event the event
   * @param source the layout that created the event
   * @return
   */
  public boolean onTouchEvent(MotionEvent event, OoyalaPlayerLayout source);
}
