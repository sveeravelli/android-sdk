package com.ooyala.android;

/**
 * Created by liusha.huang on 3/26/15.
 */
public interface CastManager {

  /**
   * Returns <code>true</code> only if application is connected to the Cast service.
   */
  public boolean isConnectedToReceiverApp();

  public CastPlayer getCastPlayer();

  public void registerWithOoyalaPlayer(OoyalaPlayer ooyalaPlayer);

  public boolean isInCastMode();

  public void enterCastMode(String embedCode, int playheadTimeInMillis, boolean isPlaying);
}
