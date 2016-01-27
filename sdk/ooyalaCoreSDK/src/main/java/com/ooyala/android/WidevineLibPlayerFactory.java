package com.ooyala.android;

import com.ooyala.android.item.Stream;
import com.ooyala.android.item.Video;
import com.ooyala.android.player.MoviePlayer;
import com.ooyala.android.player.PlayerFactory;

import java.util.Set;

/**
 * Created by zchen on 1/26/16.
 * package private
 */
class WidevineLibPlayerFactory implements PlayerFactory {
  static final String WIDEVINE_LIB_PLAYER = "com.ooyala.android.WidevineLibPlayer";

  public WidevineLibPlayerFactory() {}

  @Override
  public boolean canPlayVideo(Video video) {
    Set<Stream> streams = video.getStreams();

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
      player = (MoviePlayer) getClass().getClassLoader()
          .loadClass(WIDEVINE_LIB_PLAYER).newInstance();
    } catch (Exception e) {
      throw new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_PLAYBACK_FAILED,
          "Could not initialize Widevine Lib Player");
    } finally {
      return player;
    }
  }

  public int priority() {
    return 99;
  }
}
