package com.ooyala.android;

import java.util.Set;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ooyala.android.OoyalaPlayer.State;

public class EmptyStreamPlayer extends StreamPlayer {

  @Override
  public void init(OoyalaPlayer parent, Set<Stream> streams) {
    if (parent == null) {
      this._error = "Invalid Parent";
      setState(State.ERROR);
    }
    else {
      setState(State.LOADING);
      setParent(parent);
      setupView();
      setState(State.READY);
    }
  }

  @SuppressWarnings("deprecation")
  private void setupView() {
    createView(_parent.getLayout().getContext());
    _parent.getLayout().addView(_view);
    ((MovieView) _view).setAspectRatio( 16f/9f );
    SurfaceHolder holder = _view.getHolder();
    holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  }

  private void createView(Context c) {
    _view = new MovieView(c);
    _view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
    _view.setBackgroundColor(Color.BLACK);
  }

  @Override
  public void play() {
    switch (_state) {
    case PAUSED:
    case READY:
    case COMPLETED:
      setState(State.PLAYING);
    default:
      break;
    }
  }

  @Override
  public void reset() {
  }

  @Override
  public void suspend() {
  }

  @Override
  public void suspend(int millisToResume, State stateToResume) {
  }

  @Override
  public void resume() {
  }

  @Override
  public void resume(int millisToResume, State stateToResume) {
  }

  @Override
  public void destroy() {
    removeView();
  }

  private void removeView() {
    if (_parent != null) {
      _parent.getLayout().removeView(_view);
    }
    _view = null;
  }
  
}
