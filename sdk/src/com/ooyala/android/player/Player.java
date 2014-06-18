package com.ooyala.android.player;

import java.util.Observable;
import java.util.Set;

import android.view.SurfaceView;
import android.view.View;

import com.ooyala.android.OoyalaException;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.SeekStyle;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.item.Stream;

/**
 * The interface that must be implemented in order to plug into OoyalaPlayer and Ooyala UI
 * @author michael.len
 *
 */
public class Player extends Observable implements PlayerInterface {
  protected OoyalaPlayer _parent = null;
  /** the current state of the player */
  private State _state = State.INIT;
  /** The Player's current error if it exists */
  protected OoyalaException _error = null;
  protected SurfaceView _view = null;
  protected boolean _resizeQueued = false;
  protected int _buffer = 0;
  protected boolean _fullscreen = false;
  protected boolean _pausable = true;

  /**
   * Init the player
   */
  protected Player() {}

  public void init(OoyalaPlayer parent, Set<Stream> streams) {}

  public State getState() {
    return _state;
  }

  protected void setState(State state) {
    this._state = state;
    setChanged();
    notifyObservers(OoyalaPlayer.STATE_CHANGED_NOTIFICATION);
  }

  public OoyalaException getError() {
    return _error;
  }

  public View getView() {
    return _view;
  }

  public void setParent(OoyalaPlayer parent) {
    _parent = parent;
  }

  public int getBufferPercentage() {
    return _buffer;
  }

  public boolean isPlaying() {
    return false;
  }

  /**
   * Returns if the current player has found live closed caption info in the stream
   * @return true if the stream has embedded live closed captions
   */
  public boolean isLiveClosedCaptionsAvailable(){
    return false;
  }

  /**
   * Enables and disables live closed captions on the player
   * @param enabled weather to disable or enable live closed captions
   */
  public void setLiveClosedCaptionsEnabled(boolean enabled){}

  public SeekStyle getSeekStyle() {
    return SeekStyle.ENHANCED;
  }

  @Override
  public void reset() {
    // TODO Auto-generated method stub

  }

  @Override
  public void suspend() {
    // TODO Auto-generated method stub

  }

  @Override
  public void suspend(int millisToResume, State stateToResume) {
    // TODO Auto-generated method stub

  }

  @Override
  public void resume() {
    // TODO Auto-generated method stub

  }

  @Override
  public void resume(int millisToResume, State stateToResume) {
    // TODO Auto-generated method stub

  }

  @Override
  public void destroy() {
    // TODO Auto-generated method stub

  }

  @Override
  public void pause() {
    // TODO Auto-generated method stub

  }

  @Override
  public void play() {
    // TODO Auto-generated method stub

  }

  @Override
  public void stop() {
    // TODO Auto-generated method stub

  }

  @Override
  public int currentTime() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int duration() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int buffer() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void seekToTime(int timeInMillis) {
    // TODO Auto-generated method stub

  }
}
