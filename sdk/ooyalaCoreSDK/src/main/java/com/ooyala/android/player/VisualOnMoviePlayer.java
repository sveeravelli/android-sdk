package com.ooyala.android.player;

import com.ooyala.android.util.DebugMode;

/**
 * Created by zchen on 1/26/16.
 */
public class VisualOnMoviePlayer extends MoviePlayer {
  private static final String TAG = "VisualOnMoviePlayer";
  private static final String VISUALON_PLAYER = "com.ooyala.android.visualon.VisualOnStreamPlayer";

  @Override
  protected StreamPlayer getStreamPlayer() {
    StreamPlayer player = null;
    try {
      player = (StreamPlayer)getClass().getClassLoader().loadClass(VISUALON_PLAYER).newInstance();
    } catch(Exception e) {
      DebugMode.logE(TAG, "Tried to load VisualOn Player but failed");
      player = new BaseStreamPlayer();
    } finally {
      return player;
    }
  }
}
