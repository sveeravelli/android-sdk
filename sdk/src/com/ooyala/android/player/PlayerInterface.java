package com.ooyala.android.player;

import com.ooyala.android.OoyalaPlayer.State;

public interface PlayerInterface {
  public void pause();

  public void play();

  public void stop();

  public void seekToTime(int timeInMillis);

  public int currentTime();

  public int duration();

  public int buffer();
  
  public void reset();

  public void suspend();

  public void suspend(int millisToResume, State stateToResume);

  public void resume();

  public void resume(int millisToResume, State stateToResume);

  public void destroy();

}
