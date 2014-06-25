package com.ooyala.android.plugin;

import java.util.Observable;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;

public class StateNotifier extends Observable {
  private State _state = State.INIT;

  public State getState() {
    return _state;
  }

  protected void setState(State state) {
    this._state = state;
    setChanged();
    notifyObservers(OoyalaPlayer.STATE_CHANGED_NOTIFICATION);
  }
}
