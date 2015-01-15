package com.ooyala.android.item;

import android.test.AndroidTestCase;

import com.ooyala.android.TestConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DynamicChannelTest extends AndroidTestCase {
  public DynamicChannelTest() {
    super();
  }

  @Override
  protected void setUp() {

  }

  @Override
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
        TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_DYNAMIC_CHANNEL), embeds, null);
    assertNotNull(dynamicChannel);
    assertNull(dynamicChannel.getEmbedCode());
    assertNull(dynamicChannel.getTitle());
    assertNull(dynamicChannel.getContentToken());
    assertNull(dynamicChannel.getDescription());
    assertEquals(DynamicChannel.class, dynamicChannel.getClass());

    assertEquals(2, dynamicChannel.getVideos().size());
    Iterator<Video> iter = dynamicChannel.getVideos().values().iterator();
    assertNotNull(iter.next().getEmbedCode());
    assertNotNull(iter.next().getEmbedCode());
  }

  /**
   * Test embedCodesToAuthorize.
   */
  public void testEmbedCodesToAuthorize() {
    List<String> embeds = new ArrayList<String>();
    embeds.add(TestConstants.TEST_VIDEO);
    embeds.add(TestConstants.TEST_VIDEO_WITH_AD_OOYALA);
    DynamicChannel dynamicChannel = new DynamicChannel(
        TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_DYNAMIC_CHANNEL), embeds, null);
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
        TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_DYNAMIC_CHANNEL), embeds, null);
    Video video = dynamicChannel.getVideos().values().iterator().next();
    assertNotNull(dynamicChannel.firstVideo());
  }

  /**
   * Test lastVideo
   */
  public void testLastVideo() {
    List<String> embeds = new ArrayList<String>();
    embeds.add(TestConstants.TEST_VIDEO);
    embeds.add(TestConstants.TEST_VIDEO_WITH_AD_OOYALA);
    DynamicChannel dynamicChannel = new DynamicChannel(
        TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_DYNAMIC_CHANNEL), embeds, null);
    Iterator<Video> iter = dynamicChannel.getVideos().values().iterator();
    iter.next(); // skip 1st video
    Video video = iter.next();
    assertNotNull(dynamicChannel.lastVideo());
  }

  /**
   * Test nextVideo.
   */
  public void testNextVideo() {
    List<String> embeds = new ArrayList<String>();
    embeds.add(TestConstants.TEST_VIDEO);
    embeds.add(TestConstants.TEST_VIDEO_WITH_AD_OOYALA);
    DynamicChannel dynamicChannel = new DynamicChannel(
        TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_DYNAMIC_CHANNEL), embeds, null);
    Video video = dynamicChannel.firstVideo();
    Video expectedNext = dynamicChannel.lastVideo();
    Video next = dynamicChannel.nextVideo(video);
    assertEquals(expectedNext, next);
    next = dynamicChannel.nextVideo(next);
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
        TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_DYNAMIC_CHANNEL), embeds, null);
    Video video = dynamicChannel.lastVideo();
    Video expectedPrevious = dynamicChannel.firstVideo();
    Video previous = dynamicChannel.previousVideo(video);
    assertEquals(expectedPrevious, previous);
    previous = dynamicChannel.previousVideo(previous);
    assertNull(previous);
  }
}
