package com.ooyala.android.player;


import com.ooyala.android.OoyalaException;
import com.ooyala.android.item.Video;

/**
 * The interface that must be implemented by a movie player factory.
 * Movie player factory can register itself to ooyala player
 * when ooyala player needs to create movie player to playback a stream,
 * it queries all factories from priority high to low, if a factory canPlayVideo returns true
 * the factory will be used to create a player
 *
 */
public interface PlayerFactory {
  public boolean canPlayVideo(Video video);

  public MoviePlayer createPlayer() throws OoyalaException;

  public int priority();
}
