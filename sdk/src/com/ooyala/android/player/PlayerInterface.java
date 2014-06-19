package com.ooyala.android.player;


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

  public void resume();

  public void destroy();

  public boolean seekable();
}
