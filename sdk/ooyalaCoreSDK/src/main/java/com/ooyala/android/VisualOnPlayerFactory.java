package com.ooyala.android;

import com.ooyala.android.item.Stream;
import com.ooyala.android.player.MoviePlayer;
import com.ooyala.android.player.PlayerFactory;
import com.ooyala.android.player.VisualOnMoviePlayer;
import com.ooyala.android.util.DebugMode;

import java.util.Set;

/**
 * Created by zchen on 1/26/16.
 */
public class VisualOnPlayerFactory implements PlayerFactory {
  private static final String TAG = "VisualOnPlayerFactory";
  public VisualOnPlayerFactory() {}

  @Override
  public boolean canPlayVideo(Set<Stream> streams) {
    if (streams == null) {
      return false;
    }

    // If custom HLS Player is enabled, and one of the following:
    //   1.) Delivery type is HLS
    //   2.) Delivery type is Remote Asset, and the url contains .m3u8
    //   3.) Delivery type is Smooth streaming
    // use VisualOn
    boolean isHls = Stream.streamSetContainsDeliveryType(streams, Stream.DELIVERY_TYPE_HLS);
    boolean isRemoteHls = Stream.streamSetContainsDeliveryType(streams, Stream.DELIVERY_TYPE_REMOTE_ASSET) &&
        Stream.getStreamWithDeliveryType(streams, Stream.DELIVERY_TYPE_REMOTE_ASSET).decodedURL().toString().contains("m3u8");
    boolean isSmooth = Stream.streamSetContainsDeliveryType(streams, Stream.DELIVERY_TYPE_SMOOTH);
    boolean isRemoteSmooth = Stream.streamSetContainsDeliveryType(streams, Stream.DELIVERY_TYPE_REMOTE_ASSET) &&
        Stream.getStreamWithDeliveryType(streams, Stream.DELIVERY_TYPE_REMOTE_ASSET).decodedURL().toString().contains(".ism");

    boolean isVisualOnHLSEnabled = OoyalaPlayer.enableCustomHLSPlayer && (isHls || isRemoteHls);
    boolean isPlayreadyEnabled = OoyalaPlayer.enableCustomPlayreadyPlayer && (isSmooth || isRemoteSmooth || isHls || isRemoteHls);

    if (isVisualOnHLSEnabled || isPlayreadyEnabled) {
      if (isSmooth || isRemoteSmooth) {
        DebugMode.assertFail(TAG, "A Smooth stream is about to load on the base stream player.  Did you mean to set enableCustomSmoothPlayer?");
        return false;
      }
      return true;
    }
    return false;
  }

  @Override
  public MoviePlayer createPlayer() throws OoyalaException {
    MoviePlayer player = null;
    try {
      player = new VisualOnMoviePlayer();
    } catch (Exception e) {
      throw new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_PLAYBACK_FAILED,
          "Could not initialize visualon Player");
    }

    return player;
  }

  public int priority() {
    return 110;
  }

}
