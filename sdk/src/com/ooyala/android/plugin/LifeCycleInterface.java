package com.ooyala.android.plugin;

public interface LifeCycleInterface {
  /**
   * This is called when plugin should be reset
   */
  public void reset();

  /**
   * This is called when plugin should be suspended
   */
  public void suspend();

  /**
   * This is called when plugin should be resumed
   */
  public void resume();

  /**
   * This is called when plugin should be destryed
   */
  public void destroy();

}
