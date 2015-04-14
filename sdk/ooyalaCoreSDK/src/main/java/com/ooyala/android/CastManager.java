package com.ooyala.android;

/**
 * Created by liusha.huang on 3/26/15.
 */
public interface CastManager {

  /**
   * Returns <code>true</code> only if application is connected to the Cast service.
   */
  public boolean isConnectedToChromecast();

  /**
   * Create CastPlayer with given embedcode
   * @param embedCode the embedcode of the content to be cast
   * @return new created CastPlayer
   */
  public CastPlayer createNewCastPlayer(String embedCode);

  public void setCastPlayer(CastPlayer castPlayer);

  public void registerWithOoyalaPlayer(OoyalaPlayer ooyalaPlayer);
}
