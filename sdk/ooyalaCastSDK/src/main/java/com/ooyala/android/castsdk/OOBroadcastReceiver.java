package com.ooyala.android.cast;

import com.ooyala.android.OoyalaPlayer.State;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RemoteControlClient;

public class OOBroadcastReceiver extends BroadcastReceiver {
  
  // Same intent can be fired more than once and this is a device specific problem. 
  // So add the boolean variable to check if the current intent is the first intent
  private static boolean firstIntent = true;
  
  @Override
  public void onReceive(Context context, Intent intent) {
    final String action = intent.getAction();
    if (action != null && action.equals(Intent.ACTION_MEDIA_BUTTON)) {
      if (firstIntent == true) {
        OOCastManager castManager = OOCastManager.getCastManager();
        OOCastPlayer castPlayer = castManager.getCurrentCastPlayer();
        if (castPlayer != null) {
          if (castPlayer.getState() == State.PLAYING) {
            castPlayer.pause();
          } else if ((castPlayer.getState() == State.PAUSED || castPlayer.getState() == State.READY) || castPlayer.getState() == State.COMPLETED) {
            castPlayer.play();
          } 
        }
        firstIntent = false;
      } else {
        firstIntent = true;
      }
    }
  }

}
