package com.ooyala.android.player;

import java.util.Set;

import android.content.Context;
import android.graphics.Color;
import android.view.SurfaceHolder;

import com.ooyala.android.OoyalaException;
import com.ooyala.android.OoyalaException.OoyalaErrorCode;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.item.Stream;

public class EmptyStreamPlayer extends StreamPlayer {

  @Override
  public void init(OoyalaPlayer parent, Set<Stream> streams) {
    if (parent == null) {
      this._error = new OoyalaException(OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "Invalid Parent");
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
    _parent.addVideoView( _view );
    ((MovieView) _view).setAspectRatio( 16f/9f );
    SurfaceHolder holder = _view.getHolder();
    holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  }

  private void createView(Context c) {
    _view = new MovieView(c);
    _view.setBackgroundColor(Color.BLACK);
  }

  @Override
  public void play() {
    switch (getState()) {
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
  public void resume() {
  }

  @Override
  public void destroy() {
    removeView();
  }

  private void removeView() {
    _parent.removeVideoView();
    _view = null;
  }
}