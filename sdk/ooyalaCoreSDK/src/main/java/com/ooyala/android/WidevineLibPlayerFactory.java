package com.ooyala.android;

import com.ooyala.android.item.Stream;
import com.ooyala.android.player.MoviePlayer;
import com.ooyala.android.player.PlayerFactory;
import com.ooyala.android.player.WidevineLibPlayer;

import java.util.Set;

/**
 * Created by zchen on 1/26/16.
 * package private
 */
class WidevineLibPlayerFactory implements PlayerFactory {
  public WidevineLibPlayerFactory() {}

  @Override
  public boolean canPlayVideo(Set<Stream> streams) {
    if (streams == null) {
      return false;
    }

    if (Stream.streamSetContainsDeliveryType(streams, Stream.DELIVERY_TYPE_WV_MP4)) {
      return true;
    }
    return false;
  }

  @Override
  public MoviePlayer createPlayer() throws OoyalaException {
    MoviePlayer player = null;
    try {
      player = new WidevineLibPlayer();
    } catch (Exception e) {
      throw new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_PLAYBACK_FAILED,
          "Could not initialize Widevine Lib Player");
    }

    return player;
  }

  public int priority() {
    return 90;
  }
}
