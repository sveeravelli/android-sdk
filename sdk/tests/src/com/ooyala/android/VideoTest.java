package com.ooyala.android;

import android.test.AndroidTestCase;

public class VideoTest extends AndroidTestCase {
  public VideoTest() {
    super();
  }

  protected void setUp() {

  }

  protected void tearDown() {

  }

  /**
   * Test the Video constructor.
   */
  public void testConstructor() {
    Video video = new Video(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_VIDEO),
        TestConstants.TEST_VIDEO, null);
    assertNotNull(video);
    assertEquals(TestConstants.TEST_VIDEO, video.getEmbedCode());
    assertEquals("Bhangra Empire - Boston Bhangra 2011 [Top View]", video.getTitle());
    assertEquals(TestConstants.TEST_CONTENT_ID, video.getContentToken());
    assertEquals("dancing", video.getDescription());
    assertEquals(Video.class, video.getClass());
  }

  /**
   * Test embedCodesToAuthorize.
   */
  public void testEmbedCodesToAuthorize() {
    Video video = new Video(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_VIDEO),
        TestConstants.TEST_VIDEO, null);
    assertEquals(1, video.embedCodesToAuthorize().size());
    assertEquals(video.getEmbedCode(), video.embedCodesToAuthorize().get(0));
  }

  /**
   * Test update
   */
  public void testUpdate() {
    Video video = new Video(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_VIDEO),
        TestConstants.TEST_VIDEO, null);
    assertNotNull(video);
    assertEquals(TestConstants.TEST_VIDEO, video.getEmbedCode());
    assertEquals("Bhangra Empire - Boston Bhangra 2011 [Top View]", video.getTitle());
    assertEquals(TestConstants.TEST_CONTENT_ID, video.getContentToken());
    assertEquals("dancing", video.getDescription());
    assertEquals(Video.class, video.getClass());
    video.update(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_VIDEO_AUTH_HLS));
    assertNotNull(video);
    // assertEquals("http://player.ooyala.com/player/iphone/UwN2wxMzpU1Nl_qojlX8iLlKEHfl4HLM.m3u8",
    // video.getStream().decodedURL().toString());
    assertNull(video.getStream()); // SDK doesn't support HLS yet
  }

  /**
   * Test getStream.
   */
  public void testGetStream() {
    Video video = new Video(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_VIDEO),
        TestConstants.TEST_VIDEO, null);
    assertNotNull(video);
    assertEquals(TestConstants.TEST_VIDEO, video.getEmbedCode());
    assertEquals("Bhangra Empire - Boston Bhangra 2011 [Top View]", video.getTitle());
    assertEquals(TestConstants.TEST_CONTENT_ID, video.getContentToken());
    assertEquals("dancing", video.getDescription());
    assertEquals(Video.class, video.getClass());
    assertFalse(video.hasAds());
    video.update(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_VIDEO_AUTH_MP4));
    assertNotNull(video);
    assertEquals("http://ak.c.ooyala.com/UwN2wxMzpU1Nl_qojlX8iLlKEHfl4HLM/DOcJ-FxaFrRg4gtGEwOmk2OjBrO5dC5F",
        video.getStream().decodedURL().toString());

    video = new Video(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_VIDEO),
        TestConstants.TEST_VIDEO, null);
    assertNotNull(video);
    assertEquals(TestConstants.TEST_VIDEO, video.getEmbedCode());
    assertEquals("Bhangra Empire - Boston Bhangra 2011 [Top View]", video.getTitle());
    assertEquals(TestConstants.TEST_CONTENT_ID, video.getContentToken());
    assertEquals("dancing", video.getDescription());
    assertEquals(Video.class, video.getClass());
    assertFalse(video.hasAds());
    video.update(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_VIDEO_AUTH_HLS));
    assertNotNull(video);
    // assertEquals("http://player.ooyala.com/player/iphone/UwN2wxMzpU1Nl_qojlX8iLlKEHfl4HLM.m3u8",
    // video.getStream().decodedURL().toString());
    assertNull(video.getStream()); // SDK doesn't support HLS yet

    video.update(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_VIDEO_AUTH_MP4));
    assertNotNull(video);
    assertEquals("http://ak.c.ooyala.com/UwN2wxMzpU1Nl_qojlX8iLlKEHfl4HLM/DOcJ-FxaFrRg4gtGEwOmk2OjBrO5dC5F",
        video.getStream().decodedURL().toString());
  }

  /**
   * Test ads.
   */
  public void testAds() {
    Video video = new Video(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_VIDEO_WITH_AD_OOYALA),
        TestConstants.TEST_VIDEO_WITH_AD_OOYALA, null);
    assertNotNull(video);
    assertEquals(TestConstants.TEST_VIDEO_WITH_AD_OOYALA, video.getEmbedCode());
    assertEquals("Bhangra Empire - Boston Bhangra 2011 [Side View]", video.getTitle());
    assertEquals("dancing", video.getDescription());
    assertEquals(Video.class, video.getClass());
    assertTrue(video.hasAds());
    assertEquals(1, video.getAds().size());
    assertEquals(OoyalaAdSpot.class, video.getAds().get(0).getClass());
    assertEquals(TestConstants.TEST_AD_OOYALA, ((OoyalaAdSpot) video.getAds().get(0)).getEmbedCode());
    assertEquals(0, video.getAds().get(0).getTime());
    assertEquals("http://www.bhangraempire.com", video.getAds().get(0).getClickURL().toString());
    assertEquals(1, video.getAds().get(0).getTrackingURLs().size());
    assertEquals("http://www.ooyala.com/track", video.getAds().get(0).getTrackingURLs().get(0).toString());
    video.insertAd(new OoyalaAdSpot(1000, null, null, "hello"));
    assertEquals(1000, video.getAds().get(1).getTime());
  }

  /**
   * Test firstVideo
   */
  public void testFirstVideo() {
    Video video = new Video(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_VIDEO_WITH_AD_OOYALA),
        TestConstants.TEST_VIDEO_WITH_AD_OOYALA, null);
    assertEquals(video, video.firstVideo());
  }

  /**
   * Test nextVideo. Also tests ChannelSet.nextVideo, Channel.nextVideo, Channel.firstVideo, and
   * Channel.lastVideo
   */
  public void testNextVideo() {
    ChannelSet channelSet = new ChannelSet(
        ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL_SET),
        TestConstants.TEST_CHANNEL_SET, null);
    Video video = channelSet.firstVideo();
    Video expectedNext = channelSet.getChannels().get(0).getVideos().get(1);
    Video next = video.nextVideo();
    assertEquals(expectedNext, next);

    video = channelSet.getChannels().get(0).lastVideo();
    expectedNext = channelSet.getChannels().get(1).getVideos().get(0);
    next = video.nextVideo();
    assertEquals(expectedNext, next);
    video = next;
    next = video.nextVideo();
    video = next;
    next = video.nextVideo();
    assertNull(next);
  }

  /**
   * Test previousVideo. Also tests ChannelSet.previousVideo, Channel.previousVideo, Channel.firstVideo, and
   * Channel.lastVideo
   */
  public void testPreviousVideo() {
    ChannelSet channelSet = new ChannelSet(
        ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL_SET),
        TestConstants.TEST_CHANNEL_SET, null);
    Video video = channelSet.getChannels().get(0).getVideos().get(1);
    Video expectedPrevious = channelSet.getChannels().get(0).getVideos().get(0);
    Video previous = video.previousVideo();
    assertEquals(expectedPrevious, previous);
    video = previous;
    previous = video.previousVideo();
    assertNull(previous);
    video = channelSet.getChannels().get(1).getVideos().get(0);
    expectedPrevious = channelSet.getChannels().get(0).getVideos()
        .get(channelSet.getChannels().get(0).getVideos().size() - 1);
    previous = video.previousVideo();
    assertEquals(expectedPrevious, previous);
  }
}
