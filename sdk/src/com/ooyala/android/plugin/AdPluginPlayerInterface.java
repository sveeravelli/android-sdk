package com.ooyala.android.plugin;

public interface AdPluginPlayerInterface {
  public void pause();

  public void play();

  public void stop();

  public int currentTime();

  public int duration();

  public int buffer();

  public int seek();
}
