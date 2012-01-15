package com.ooyala.android;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController;

public class OoyalaPlayerLayout extends FrameLayout {
  MediaController _controller = null;
  OoyalaPlayer _player = null;

  public OoyalaPlayerLayout(Context context) {
    super(context);
  }

  public OoyalaPlayerLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public OoyalaPlayerLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public void useDefaultControls(OoyalaPlayer player) {
    _player = player;
    _controller = new MediaController(this.getContext(), false);
    _controller.setAnchorView(this);
    _controller.setMediaPlayer(_player);

    _controller.setPrevNextListeners(new View.OnClickListener() {
      @Override
      public void onClick(View v) { // next
        _player.nextVideo(OoyalaPlayer.DO_PLAY);
      }
    }, new View.OnClickListener() {
      @Override
      public void onClick(View v) { // previous
        _player.previousVideo(OoyalaPlayer.DO_PLAY);
      }
    });
  }

  public MediaController getController() {
    return _controller;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    //the MediaController will hide after 3 seconds - tap the screen to make it appear again
    switch(_player.getState()) {
      case INIT:
      case LOADING:
      case ERROR:
        return false;
      default:
        _controller.show();
        return false;
    }
  }
}
