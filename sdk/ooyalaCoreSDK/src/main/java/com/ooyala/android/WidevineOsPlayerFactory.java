package com.ooyala.android;

import com.ooyala.android.item.Stream;
import com.ooyala.android.item.Video;
import com.ooyala.android.player.MoviePlayer;
import com.ooyala.android.player.PlayerFactory;
import com.ooyala.android.player.WidevineOsPlayer;

import java.util.Set;

/**
 * Created by zchen on 1/26/16.
 * packate private
 */
class WidevineOsPlayerFactory implements PlayerFactory {
  WidevineOsPlayerFactory() {};

  @Override
  public boolean canPlayVideo(Video video) {
    Set<Stream> streams = video.getStreams();

    if (streams == null) {
      return false;
    }

    if (Stream.streamSetContainsDeliveryType(streams, Stream.DELIVERY_TYPE_WV_HLS) ||
        Stream.streamSetContainsDeliveryType(streams, Stream.DELIVERY_TYPE_WV_WVM)) {
      return true;
    }
    return false;
  }

  @Override
  public MoviePlayer createPlayer() throws OoyalaException {
    MoviePlayer player = null;
    try {
      player = new WidevineOsPlayer();
    } catch (Exception e) {
      throw new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_PLAYBACK_FAILED,
          "Could not initialize Widevine OS Player");
    } finally {
      return player;
    }
  }

  @Override
  public int priority() {
    return 100;
  }
}
