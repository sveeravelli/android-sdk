package com.ooyala.android.plugin;

public interface ControlRequesterInterface {
  public void onControlGranted();

  public void onSuspend();

  public void onResume();

  public void onDestroy();
}
