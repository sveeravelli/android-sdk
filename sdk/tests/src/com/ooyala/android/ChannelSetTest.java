package com.ooyala.android;

import java.util.Iterator;
import java.util.LinkedHashMap;

import android.test.AndroidTestCase;

public class ChannelSetTest extends AndroidTestCase
{
  public ChannelSetTest()
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
   * Test the ChannelSet constructor.
   */
  public void testConstructor()
  {
    ChannelSet channelSet = new ChannelSet(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL_SET), TestConstants.TEST_CHANNEL_SET, null);
    assertNotNull(channelSet);
    assertEquals(TestConstants.TEST_CHANNEL_SET, channelSet.getEmbedCode());
    assertEquals("All My Channels", channelSet.getTitle());
    assertEquals("1-N1ZmszMzoWGX7wpenTZoWEpfjV5RMQQc", channelSet.getContentToken());
    assertEquals("Bhangra Empire\nFunny", channelSet.getDescription());
    assertEquals(ChannelSet.class, channelSet.getClass());

    LinkedHashMap<String,Channel> channels = channelSet.getChannels();
    assertEquals(2, channels.size());
    Channel channel = channels.get(TestConstants.TEST_CHANNEL);
    assertNotNull(channel);
    assertEquals(TestConstants.TEST_CHANNEL, channel.getEmbedCode());

    LinkedHashMap<String,Video> videos = channel.getVideos();
    assertEquals(5, videos.size());
    assertNotNull(videos.get("JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B"));
    assertEquals("JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B", videos.get("JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B").getEmbedCode());
    assertNotNull(videos.get("lrdnAxMzoTIzfqnDk8m5_T6eGupTsWf6"));
    assertEquals("lrdnAxMzoTIzfqnDk8m5_T6eGupTsWf6", videos.get("lrdnAxMzoTIzfqnDk8m5_T6eGupTsWf6").getEmbedCode());
    assertNotNull(videos.get("g3N2wxMzqxoB84c3dan5xyXTxdrhX1km"));
    assertEquals("g3N2wxMzqxoB84c3dan5xyXTxdrhX1km", videos.get("g3N2wxMzqxoB84c3dan5xyXTxdrhX1km").getEmbedCode());

    assertNotNull(channels.get("NueXAxMzqnfCtqVrgaEoD4-N8sFrt-nt"));
    assertEquals("NueXAxMzqnfCtqVrgaEoD4-N8sFrt-nt", channels.get("NueXAxMzqnfCtqVrgaEoD4-N8sFrt-nt").getEmbedCode());
  }

  /**
   * Test embedCodesToAuthorize.
   */
  public void testEmbedCodesToAuthorize()
  {
    ChannelSet channelSet = new ChannelSet(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL_SET), TestConstants.TEST_CHANNEL_SET, null);
    assertEquals(1, channelSet.embedCodesToAuthorize().size());
    assertEquals(channelSet.getEmbedCode(), channelSet.embedCodesToAuthorize().get(0));
  }

  /**
   * Test firstVideo
   */
  public void testFirstVideo()
  {
    ChannelSet channelSet = new ChannelSet(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL_SET), TestConstants.TEST_CHANNEL_SET, null);
    Video video = channelSet.getChannels().values().iterator().next().getVideos().values().iterator().next();
    assertEquals(video, channelSet.firstVideo());
  }

  /**
   * Test nextVideo.
   */
  public void testNextVideo()
  {
    ChannelSet channelSet = new ChannelSet(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL_SET), TestConstants.TEST_CHANNEL_SET, null);
    Iterator<Channel> iter = channelSet.getChannels().values().iterator();
    Channel channel = iter.next();
    Channel channel2 = iter.next();
    Video expectedNext = channel2.getVideos().values().iterator().next();
    Video next = channelSet.nextVideo(channel);
    assertEquals(expectedNext, next);
    next = channelSet.nextVideo(channel2);
    assertNull(next);
  }

  /**
   * Test previousVideo.
   */
  public void testPreviousVideo()
  {
    ChannelSet channelSet = new ChannelSet(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL_SET), TestConstants.TEST_CHANNEL_SET, null);
    Iterator<Channel> iter = channelSet.getChannels().values().iterator();
    Channel channel = iter.next();
    Channel channel2 = iter.next();
    Iterator<Video> videoIter = channel.getVideos().values().iterator();
    videoIter.next(); // 0
    videoIter.next(); // 1
    videoIter.next(); // 2
    videoIter.next(); // 3
    Video expectedPrevious = videoIter.next();
    Video previous = channelSet.previousVideo(channel2);
    assertEquals(expectedPrevious, previous);
    previous = channelSet.previousVideo(channel);
    assertNull(previous);
  }
}
