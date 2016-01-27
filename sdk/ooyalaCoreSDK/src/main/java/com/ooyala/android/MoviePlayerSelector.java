package com.ooyala.android;

import com.ooyala.android.item.Video;
import com.ooyala.android.player.MoviePlayer;
import com.ooyala.android.player.PlayerFactory;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Created by zchen on 1/26/16.
 */
class MoviePlayerSelector {
  private static final String TAG = "MoviePlayerSelector";

  private class PlayerFactoryComparator implements Comparator<PlayerFactory> {
    @Override
    public int compare(PlayerFactory f1, PlayerFactory f2) {
      return f1.priority() - f2.priority();
    }
  }

  private SortedSet<PlayerFactory> factories;


  public MoviePlayerSelector() {
    factories = new TreeSet<PlayerFactory>(new PlayerFactoryComparator());
  }

  public void registerPlayerFactory(PlayerFactory factory) {
    factories.add(factory);
  }

  public MoviePlayer selectMoviePlayer(Video video) throws OoyalaException {
    MoviePlayer player = null;
    if (video == null) {
      throw new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_PLAYBACK_FAILED, "the video is null");
    }

    Iterator it = factories.iterator();
    while (it.hasNext()) {
      PlayerFactory pf = (PlayerFactory)it.next();
      if (pf.canPlayVideo(video.getStreams())) {
        try {
          return pf.createPlayer();
        } catch (OoyalaException e) {
          throw e;
        }
      }
    }

    return new MoviePlayer();
  }
}
