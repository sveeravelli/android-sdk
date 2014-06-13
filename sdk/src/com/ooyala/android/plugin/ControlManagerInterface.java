package com.ooyala.android.plugin;


public interface ControlManagerInterface {
  public boolean requestControl(final ControlRequesterInterface requester);

  public boolean returnControl(final ControlRequesterInterface requster);
}
