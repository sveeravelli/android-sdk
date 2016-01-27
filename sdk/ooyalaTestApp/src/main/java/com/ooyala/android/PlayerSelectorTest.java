package com.ooyala.android;

import android.test.AndroidTestCase;

import com.ooyala.android.item.StandaloneVideo;
import com.ooyala.android.item.Stream;
import com.ooyala.android.item.Video;
import com.ooyala.android.player.MoviePlayer;
import com.ooyala.android.player.VisualOnMoviePlayer;
import com.ooyala.android.player.WidevineLibPlayer;
import com.ooyala.android.player.WidevineOsPlayer;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zchen on 1/26/16.
 */
public class PlayerSelectorTest extends AndroidTestCase {
  private MoviePlayerSelector selector;

  public PlayerSelectorTest() { super();}

  @Override
  protected void setUp() {
    selector = new MoviePlayerSelector();
    selector.registerPlayerFactory(new WidevineLibPlayerFactory());
    selector.registerPlayerFactory(new WidevineOsPlayerFactory());
    selector.registerPlayerFactory(new VisualOnPlayerFactory());
  }

  @Override
  protected void tearDown() {
    OoyalaPlayer.enableCustomHLSPlayer = false;
  }

  public void testNullVideo() {
    Video v = null;
    try {
      MoviePlayer player = selector.selectMoviePlayer(v);
    } catch (OoyalaException e) {
      assertTrue("select movie player should fail", e != null);
    }
  }

  public void testHls() {
    Set<Stream> streams = new HashSet<Stream>();
    Stream stream =  new Stream(TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_STREAM_HLS));
    streams.add(stream);
    StandaloneVideo v = new StandaloneVideo(streams);
    try {
      MoviePlayer player = selector.selectMoviePlayer(v);
      assertTrue(player instanceof MoviePlayer);
    } catch (OoyalaException e) {
      assertTrue(e.getMessage(), false);
    }
  }

  public void testWidevineHls() {
    Set<Stream> streams = new HashSet<Stream>();
    Stream stream =  new Stream(TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_STREAM_WIDEVINE_HLS));
    streams.add(stream);
    StandaloneVideo v = new StandaloneVideo(streams);
    try {
      MoviePlayer player = selector.selectMoviePlayer(v);
      assertTrue("expected type widevineOs player actual type" + player.getClass().getName(), player instanceof WidevineOsPlayer);
    } catch (OoyalaException e) {
      assertTrue(e.getMessage(), false);
    }
  }

  public void testWidevineMp4() {
    Set<Stream> streams = new HashSet<Stream>();
    Stream stream =  new Stream(TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_STREAM_WIDEVINE_MP4));
    streams.add(stream);
    StandaloneVideo v = new StandaloneVideo(streams);
    try {
      MoviePlayer player = selector.selectMoviePlayer(v);
      assertTrue("expected type widevineLib player actual type" + player.getClass().getName(), player instanceof WidevineLibPlayer);
    } catch (OoyalaException e) {
      assertTrue(e.getMessage(), false);
    }
  }

  public void testWidevineWvm() {
    Set<Stream> streams = new HashSet<Stream>();
    Stream stream =  new Stream(TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_STREAM_WIDEVINE_WVM));
    streams.add(stream);
    StandaloneVideo v = new StandaloneVideo(streams);
    try {
      MoviePlayer player = selector.selectMoviePlayer(v);
      assertTrue("expected type widevineOs player actual type" + player.getClass().getName(), player instanceof WidevineOsPlayer);
    } catch (OoyalaException e) {
      assertTrue(e.getMessage(), false);
    }
  }

  public void testVisualonHls() {
    Set<Stream> streams = new HashSet<Stream>();
    Stream stream =  new Stream(TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_STREAM_HLS));
    streams.add(stream);
    StandaloneVideo v = new StandaloneVideo(streams);
    OoyalaPlayer.enableCustomHLSPlayer = true;
    try {
      MoviePlayer player = selector.selectMoviePlayer(v);
      assertTrue("expected type visualon player actual type" + player.getClass().getName(), player instanceof VisualOnMoviePlayer);
    } catch (OoyalaException e) {
      assertTrue(e.getMessage(), false);
    }
  }
}
