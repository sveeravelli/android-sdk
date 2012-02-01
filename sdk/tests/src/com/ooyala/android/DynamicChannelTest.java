package com.ooyala.android;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.test.AndroidTestCase;

public class DynamicChannelTest extends AndroidTestCase {
  public DynamicChannelTest() {
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
    List<String> embeds = new ArrayList<String>();
    embeds.add(TestConstants.TEST_VIDEO);
    embeds.add(TestConstants.TEST_VIDEO_WITH_AD_OOYALA);
    DynamicChannel dynamicChannel = new DynamicChannel(
        ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_DYNAMIC_CHANNEL), embeds, null);
    assertNotNull(dynamicChannel);
    assertNull(dynamicChannel.getEmbedCode());
    assertNull(dynamicChannel.getTitle());
    assertNull(dynamicChannel.getContentToken());
    assertNull(dynamicChannel.getDescription());
    assertEquals(DynamicChannel.class, dynamicChannel.getClass());

    assertEquals(2, ((DynamicChannel) dynamicChannel).getVideos().size());
    Iterator<Video> iter = ((DynamicChannel) dynamicChannel).getVideos().values().iterator();
    assertEquals(TestConstants.TEST_VIDEO, iter.next().getEmbedCode());
    assertEquals(TestConstants.TEST_VIDEO_WITH_AD_OOYALA, iter.next().getEmbedCode());
  }

  /**
   * Test embedCodesToAuthorize.
   */
  public void testEmbedCodesToAuthorize() {
    List<String> embeds = new ArrayList<String>();
    embeds.add(TestConstants.TEST_VIDEO);
    embeds.add(TestConstants.TEST_VIDEO_WITH_AD_OOYALA);
    DynamicChannel dynamicChannel = new DynamicChannel(
        ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_DYNAMIC_CHANNEL), embeds, null);
    assertEquals(dynamicChannel.getEmbedCodes(), dynamicChannel.embedCodesToAuthorize());
  }

  /**
   * Test firstVideo
   */
  public void testFirstVideo() {
    List<String> embeds = new ArrayList<String>();
    embeds.add(TestConstants.TEST_VIDEO);
    embeds.add(TestConstants.TEST_VIDEO_WITH_AD_OOYALA);
    DynamicChannel dynamicChannel = new DynamicChannel(
        ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_DYNAMIC_CHANNEL), embeds, null);
    Video video = dynamicChannel.getVideos().values().iterator().next();
    assertEquals(video, dynamicChannel.firstVideo());
  }

  /**
   * Test lastVideo
   */
  public void testLastVideo() {
    List<String> embeds = new ArrayList<String>();
    embeds.add(TestConstants.TEST_VIDEO);
    embeds.add(TestConstants.TEST_VIDEO_WITH_AD_OOYALA);
    DynamicChannel dynamicChannel = new DynamicChannel(
        ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_DYNAMIC_CHANNEL), embeds, null);
    Iterator<Video> iter = dynamicChannel.getVideos().values().iterator();
    iter.next(); // skip 1st video
    Video video = iter.next();
    assertEquals(video, dynamicChannel.lastVideo());
  }

  /**
   * Test nextVideo.
   */
  public void testNextVideo() {
    List<String> embeds = new ArrayList<String>();
    embeds.add(TestConstants.TEST_VIDEO);
    embeds.add(TestConstants.TEST_VIDEO_WITH_AD_OOYALA);
    DynamicChannel dynamicChannel = new DynamicChannel(
        ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_DYNAMIC_CHANNEL), embeds, null);
    Video video = dynamicChannel.firstVideo();
    Iterator<Video> iter = dynamicChannel.getVideos().values().iterator();
    iter.next(); // skip 1st video
    Video expectedNext = iter.next();
    Video next = dynamicChannel.nextVideo(video);
    assertEquals(expectedNext, next);
    video = next;
    next = dynamicChannel.nextVideo(video);
    assertNull(next);
  }

  /**
   * Test previousVideo.
   */
  public void testPreviousVideo() {
    List<String> embeds = new ArrayList<String>();
    embeds.add(TestConstants.TEST_VIDEO);
    embeds.add(TestConstants.TEST_VIDEO_WITH_AD_OOYALA);
    DynamicChannel dynamicChannel = new DynamicChannel(
        ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_DYNAMIC_CHANNEL), embeds, null);
    Video video = dynamicChannel.lastVideo();
    Video expectedPrevious = dynamicChannel.getVideos().values().iterator().next();
    Video previous = dynamicChannel.previousVideo(video);
    assertEquals(expectedPrevious, previous);
    video = previous;
    previous = dynamicChannel.previousVideo(video);
    assertNull(previous);
  }
}
