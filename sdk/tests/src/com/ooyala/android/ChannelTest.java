package com.ooyala.android;

import java.util.Iterator;
import java.util.LinkedHashMap;

import android.test.AndroidTestCase;

public class ChannelTest extends AndroidTestCase
{
  public ChannelTest()
  {
    super();
  }

  protected void setUp()
  {

  }

  protected void tearDown()
  {

  }

  /**
   * Test the Channel constructor.
   */
  public void testConstructor()
  {
    Channel channel = new Channel(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL), TestConstants.TEST_CHANNEL, null);
    assertNotNull(channel);
    assertEquals(TestConstants.TEST_CHANNEL, channel.getEmbedCode());
    assertEquals("Bhangra Empire", channel.getTitle());
    assertEquals("1-B0eHAxMzqsbVRm0ZJROXw1Yaj73roQu6", channel.getContentToken());
    assertEquals("best. team. ever.", channel.getDescription());
    assertEquals(Channel.class, channel.getClass());

    LinkedHashMap<String,Video> videos = channel.getVideos();
    assertEquals(5, videos.size());
    assertNotNull(videos.get("JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B"));
    assertEquals("JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B", videos.get("JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B").getEmbedCode());
    assertNotNull(videos.get("lrdnAxMzoTIzfqnDk8m5_T6eGupTsWf6"));
    assertEquals("lrdnAxMzoTIzfqnDk8m5_T6eGupTsWf6", videos.get("lrdnAxMzoTIzfqnDk8m5_T6eGupTsWf6").getEmbedCode());
    assertNotNull(videos.get("g3N2wxMzqxoB84c3dan5xyXTxdrhX1km"));
    assertEquals("g3N2wxMzqxoB84c3dan5xyXTxdrhX1km", videos.get("g3N2wxMzqxoB84c3dan5xyXTxdrhX1km").getEmbedCode());
  }

  /**
   * Test embedCodesToAuthorize.
   */
  public void testEmbedCodesToAuthorize()
  {
    Channel channel = new Channel(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL), TestConstants.TEST_CHANNEL, null);
    assertEquals(1, channel.embedCodesToAuthorize().size());
    assertEquals(channel.getEmbedCode(), channel.embedCodesToAuthorize().get(0));
  }

  /**
   * Test firstVideo
   */
  public void testFirstVideo()
  {
    Channel channel = new Channel(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL), TestConstants.TEST_CHANNEL, null);
    Video video = channel.getVideos().values().iterator().next();
    assertEquals(video, channel.firstVideo());
  }

  /**
   * Test lastVideo
   */
  public void testLastVideo()
  {
    Channel channel = new Channel(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL), TestConstants.TEST_CHANNEL, null);
    Iterator<Video> iter = channel.getVideos().values().iterator();
    iter.next(); // 0
    iter.next(); // 1
    iter.next(); // 2
    iter.next(); // 3
    Video video = iter.next(); // 4
    assertEquals(video, channel.lastVideo());
  }

  /**
   * Test nextVideo.
   */
  public void testNextVideo()
  {
    ChannelSet channelSet = new ChannelSet(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL_SET), TestConstants.TEST_CHANNEL_SET, null);
    Channel channel = channelSet.getChannels().values().iterator().next();
    Video video = channelSet.firstVideo();
    Iterator<Video> iter = channelSet.getChannels().values().iterator().next().getVideos().values().iterator();
    iter.next(); // skip 1st video
    Video expectedNext = iter.next();
    Video next = channel.nextVideo(video);
    assertEquals(expectedNext, next);

    video = channelSet.getChannels().values().iterator().next().lastVideo();
    Iterator<Channel> channelIter = channelSet.getChannels().values().iterator();
    channelIter.next(); // skip 1st channel
    expectedNext = channelIter.next().getVideos().values().iterator().next();
    next = channel.nextVideo(video);
    assertEquals(expectedNext, next);
    channelIter = channelSet.getChannels().values().iterator();
    channelIter.next(); // skip 1st channel
    channel = channelIter.next();
    next = channel.nextVideo(video);
    assertNull(next);
  }

  /**
   * Test previousVideo.
   */
  public void testPreviousVideo()
  {
    ChannelSet channelSet = new ChannelSet(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL_SET), TestConstants.TEST_CHANNEL_SET, null);
    Channel channel = channelSet.getChannels().values().iterator().next();
    Iterator<Video> iter = channelSet.getChannels().values().iterator().next().getVideos().values().iterator();
    iter.next(); // skip 1st video
    Video video = iter.next();
    Video expectedPrevious = channelSet.firstVideo();
    Video previous = channel.previousVideo(video);
    assertEquals(expectedPrevious, previous);

    video = previous;
    previous = channel.previousVideo(video);
    assertNull(previous);
    Iterator<Channel> channelIter = channelSet.getChannels().values().iterator();
    channelIter.next(); // skip 1st channel
    channel = channelIter.next();
    video = channel.getVideos().values().iterator().next();
    expectedPrevious = channelSet.getChannels().values().iterator().next().lastVideo();
    previous = channel.previousVideo(video);
    assertEquals(expectedPrevious, previous);
  }
}
