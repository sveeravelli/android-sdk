package com.ooyala.android.plugin;

import java.util.Observable;

import com.ooyala.android.OoyalaPlayer;

public class DefaultChangeNotifier extends Observable implements ChangeNotifierInterface {

  @Override
  public void notifyTimeChange() {
    sendNotification(OoyalaPlayer.TIME_CHANGED_NOTIFICATION);
  }

  @Override
  public void notifyStateChange() {
    sendNotification(OoyalaPlayer.STATE_CHANGED_NOTIFICATION);
  }

  @Override
  public void notifyBufferChange() {
    sendNotification(OoyalaPlayer.BUFFER_CHANGED_NOTIFICATION);
  }

  private void sendNotification(String notification) {
    setChanged();
    notifyObservers(notification);
  }
}
