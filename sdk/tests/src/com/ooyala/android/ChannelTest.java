package com.ooyala.android;

import android.test.AndroidTestCase;

public class ChannelTest extends AndroidTestCase {
  public ChannelTest() {
    super();
  }

  protected void setUp() {

  }

  protected void tearDown() {

  }

  /**
   * Test the Channel constructor.
   */
  public void testConstructor() {
    Channel channel = new Channel(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL),
        TestConstants.TEST_CHANNEL, null);
    assertNotNull(channel);
    assertEquals(TestConstants.TEST_CHANNEL, channel.getEmbedCode());
    assertEquals("Bhangra Empire", channel.getTitle());
    assertEquals("1-B0eHAxMzqsbVRm0ZJROXw1Yaj73roQu6", channel.getContentToken());
    assertEquals("best. team. ever.", channel.getDescription());
    assertEquals(Channel.class, channel.getClass());

    OrderedMap<String, Video> videos = channel.getVideos();
    assertEquals(5, videos.size());
    assertNotNull(videos.get("JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B"));
    assertEquals("JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B", videos.get("JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B")
        .getEmbedCode());
    assertNotNull(videos.get("lrdnAxMzoTIzfqnDk8m5_T6eGupTsWf6"));
    assertEquals("lrdnAxMzoTIzfqnDk8m5_T6eGupTsWf6", videos.get("lrdnAxMzoTIzfqnDk8m5_T6eGupTsWf6")
        .getEmbedCode());
    assertNotNull(videos.get("g3N2wxMzqxoB84c3dan5xyXTxdrhX1km"));
    assertEquals("g3N2wxMzqxoB84c3dan5xyXTxdrhX1km", videos.get("g3N2wxMzqxoB84c3dan5xyXTxdrhX1km")
        .getEmbedCode());
  }

  /**
   * Test embedCodesToAuthorize.
   */
  public void testEmbedCodesToAuthorize() {
    Channel channel = new Channel(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL),
        TestConstants.TEST_CHANNEL, null);
    assertEquals(6, channel.embedCodesToAuthorize().size());
    assertEquals(channel.getEmbedCode(), channel.embedCodesToAuthorize().get(0));
  }

  /**
   * Test firstVideo
   */
  public void testFirstVideo() {
    Channel channel = new Channel(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL),
        TestConstants.TEST_CHANNEL, null);
    Video video = channel.getVideos().get(0);
    assertEquals(video, channel.firstVideo());
  }

  /**
   * Test lastVideo
   */
  public void testLastVideo() {
    Channel channel = new Channel(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL),
        TestConstants.TEST_CHANNEL, null);
    Video video = channel.getVideos().get(channel.getVideos().size() - 1);
    assertEquals(video, channel.lastVideo());
  }

  /**
   * Test nextVideo.
   */
  public void testNextVideo() {
    ChannelSet channelSet = new ChannelSet(
        ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL_SET),
        TestConstants.TEST_CHANNEL_SET, null);
    Channel channel = channelSet.getChannels().values().iterator().next();
    Video video = channelSet.firstVideo();
    Video expectedNext = channel.getVideos().get(1);
    Video next = channel.nextVideo(video);
    assertEquals(expectedNext, next);

    video = channelSet.getChannels().get(0).lastVideo();
    expectedNext = channelSet.getChannels().get(1).getVideos().get(0);
    next = channel.nextVideo(video);
    assertEquals(expectedNext, next);
    channel = channelSet.getChannels().get(1);
    next = channel.nextVideo(video);
    assertNull(next);
  }

  /**
   * Test previousVideo.
   */
  public void testPreviousVideo() {
    ChannelSet channelSet = new ChannelSet(
        ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL_SET),
        TestConstants.TEST_CHANNEL_SET, null);
    Channel channel = channelSet.getChannels().get(0);
    Video video = channel.getVideos().get(1);
    Video expectedPrevious = channel.getVideos().get(0);
    Video previous = channel.previousVideo(video);
    assertEquals(expectedPrevious, previous);

    video = previous;
    previous = channel.previousVideo(video);
    assertNull(previous);
    channel = channelSet.getChannels().get(1);
    video = channel.getVideos().get(0);
    expectedPrevious = channelSet.getChannels().get(0).getVideos()
        .get(channelSet.getChannels().get(0).getVideos().size() - 1);
    previous = channel.previousVideo(video);
    assertEquals(expectedPrevious, previous);
  }
}
