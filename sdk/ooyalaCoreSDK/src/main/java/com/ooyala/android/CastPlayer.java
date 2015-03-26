package com.ooyala.android;

import com.ooyala.android.player.PlayerInterface;
import com.ooyala.android.plugin.LifeCycleInterface;

/**
 * Created by liusha.huang on 3/26/15.
 */
public interface CastPlayer extends PlayerInterface, LifeCycleInterface {

  public void setSeekable(boolean seekable);

  public void initReceiverPlayer(String embedCode, int playheadTimeInMillis, OoyalaPlayer.State currentState);
}
