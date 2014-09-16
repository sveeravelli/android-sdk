package com.ooyala.android;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import com.ooyala.android.OoyalaPlayer.State;

public class StateNotifier {
  private WeakReference<OoyalaPlayer> _player;
  private State _state;
  private Set<StateNotifierListener> _listeners;

  StateNotifier(OoyalaPlayer player) {
    _player = new WeakReference<OoyalaPlayer>(player);
    _listeners = new HashSet<StateNotifierListener>();
  }
  
  public void setState(State state) {
    State oldState = _state;
    _state = state;
    for (StateNotifierListener l : _listeners) {
      l.onStateChange(this);
    }
    _player.get().notifyPluginStateChange(this, oldState, state);
  }

  public State getState() {
    return _state;
  }
  
  public void notifyPlayheadChange() {
    _player.get().notifyPluginEvent(this,
        OoyalaPlayer.TIME_CHANGED_NOTIFICATION);
  }

  public void notifyBufferChange() {
    _player.get().notifyPluginEvent(this,
        OoyalaPlayer.BUFFER_CHANGED_NOTIFICATION);
  }

  public void notifyAdSkipped() {
    _player.get().notifyPluginEvent(this, OoyalaPlayer.AD_SKIPPED_NOTIFICATION);
  }

  public void addListener(StateNotifierListener l) {
    _listeners.add(l);
  }

  public void removeListener(StateNotifierListener l) {
    _listeners.remove(l);
  }

}
