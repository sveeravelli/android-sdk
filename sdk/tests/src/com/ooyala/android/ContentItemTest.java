package com.ooyala.android;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.*;

import android.test.AndroidTestCase;

public class ContentItemTest extends AndroidTestCase
{
  public ContentItemTest()
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
   * Test ContentItem.create (Video)
   * This will also test init and update, as well as Video constructor, init, and update.
   */
  public void testCreateVideo()
  {
    List<String> embeds = new ArrayList<String>();
    embeds.add(TestConstants.TEST_VIDEO);
    ContentItem video = ContentItem.create(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_VIDEO), embeds, null);
    assertNotNull(video);
    assertEquals(TestConstants.TEST_VIDEO, video.getEmbedCode());
    assertEquals("Bhangra Empire - Boston Bhangra 2011 [Top View]", video.getTitle());
    assertEquals(TestConstants.TEST_CONTENT_ID, video.getContentToken());
    assertEquals("dancing", video.getDescription());
    assertEquals(Video.class, video.getClass());
  }

  /**
   * Test ContentItem.create (Channel)
   * This will also test init and update, as well as Channel constructor, init, and update.
   */
  public void testCreateChannel()
  {
    List<String> embeds = new ArrayList<String>();
    embeds.add("B0eHAxMzqsbVRm0ZJROXw1Yaj73roQu6");
    ContentItem channel = ContentItem.create(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL), embeds, null);
    assertNotNull(channel);
    assertEquals(TestConstants.TEST_CHANNEL, channel.getEmbedCode());
    assertEquals("Bhangra Empire", channel.getTitle());
    assertEquals("1-B0eHAxMzqsbVRm0ZJROXw1Yaj73roQu6", channel.getContentToken());
    assertEquals("best. team. ever.", channel.getDescription());
    assertEquals(Channel.class, channel.getClass());

    OrderedMap<String,Video> videos = ((Channel)channel).getVideos();
    assertEquals(5, videos.size());
    assertNotNull(videos.get("JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B"));
    assertEquals("JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B", videos.get("JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B").getEmbedCode());
    assertNotNull(videos.get("lrdnAxMzoTIzfqnDk8m5_T6eGupTsWf6"));
    assertEquals("lrdnAxMzoTIzfqnDk8m5_T6eGupTsWf6", videos.get("lrdnAxMzoTIzfqnDk8m5_T6eGupTsWf6").getEmbedCode());
    assertNotNull(videos.get("g3N2wxMzqxoB84c3dan5xyXTxdrhX1km"));
    assertEquals("g3N2wxMzqxoB84c3dan5xyXTxdrhX1km", videos.get("g3N2wxMzqxoB84c3dan5xyXTxdrhX1km").getEmbedCode());
  }

  /**
   * Test ContentItem.create (ChannelSet)
   * This will also test init and update, as well as ChannelSet constructor, init, and update.
   */
  public void testCreateChannelSet()
  {
    List<String> embeds = new ArrayList<String>();
    embeds.add(TestConstants.TEST_CHANNEL_SET);
    ContentItem channelSet = ContentItem.create(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL_SET), embeds, null);
    assertNotNull(channelSet);
    assertEquals(TestConstants.TEST_CHANNEL_SET, channelSet.getEmbedCode());
    assertEquals("All My Channels", channelSet.getTitle());
    assertEquals("1-N1ZmszMzoWGX7wpenTZoWEpfjV5RMQQc", channelSet.getContentToken());
    assertEquals("Bhangra Empire\nFunny", channelSet.getDescription());
    assertEquals(ChannelSet.class, channelSet.getClass());

    OrderedMap<String,Channel> channels = ((ChannelSet)channelSet).getChannels();
    assertEquals(2, channels.size());

    Channel channel = channels.get(TestConstants.TEST_CHANNEL);
    assertNotNull(channel);
    assertEquals(TestConstants.TEST_CHANNEL, channel.getEmbedCode());

    OrderedMap<String,Video> videos = channel.getVideos();
    assertEquals(5, videos.size());
    assertNotNull(videos.get("JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B"));
    assertEquals("JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B", videos.get("JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B").getEmbedCode());
    assertNotNull(videos.get("lrdnAxMzoTIzfqnDk8m5_T6eGupTsWf6"));
    assertEquals("lrdnAxMzoTIzfqnDk8m5_T6eGupTsWf6", videos.get("lrdnAxMzoTIzfqnDk8m5_T6eGupTsWf6").getEmbedCode());
    assertNotNull(videos.get("g3N2wxMzqxoB84c3dan5xyXTxdrhX1km"));
    assertEquals("g3N2wxMzqxoB84c3dan5xyXTxdrhX1km", videos.get("g3N2wxMzqxoB84c3dan5xyXTxdrhX1km").getEmbedCode());

    Channel channel2 = channels.get("NueXAxMzqnfCtqVrgaEoD4-N8sFrt-nt");
    assertNotNull(channel2);
    assertEquals("NueXAxMzqnfCtqVrgaEoD4-N8sFrt-nt", channel2.getEmbedCode());
  }

  /**
   * Test ContentItem.create (DynamicChannel)
   * This will also test init and update, as well as DynamicChannel constructor, init, and update.
   */
  public void testCreateDynamicChannel()
  {
    List<String> embeds = new ArrayList<String>();
    embeds.add(TestConstants.TEST_VIDEO);
    embeds.add(TestConstants.TEST_VIDEO_WITH_AD_OOYALA);
    ContentItem dynamicChannel = ContentItem.create(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_DYNAMIC_CHANNEL), embeds, null);
    assertNotNull(dynamicChannel);
    assertNull(dynamicChannel.getEmbedCode());
    assertNull(dynamicChannel.getTitle());
    assertNull(dynamicChannel.getContentToken());
    assertNull(dynamicChannel.getDescription());
    assertEquals(DynamicChannel.class, dynamicChannel.getClass());

    assertEquals(2, ((DynamicChannel)dynamicChannel).getVideos().size());
    assertEquals(TestConstants.TEST_VIDEO, ((DynamicChannel)dynamicChannel).getVideos().get(0).getEmbedCode());
    assertEquals(TestConstants.TEST_VIDEO_WITH_AD_OOYALA, ((DynamicChannel)dynamicChannel).getVideos().get(1).getEmbedCode());
  }

  public static JSONObject getTestJSON(String file)
  {
    InputStream inputStream = ContentItemTest.class.getResourceAsStream(file);
    String json = new Scanner(inputStream).useDelimiter("\\A").next();
    try
    {
      return (JSONObject) new JSONTokener(json).nextValue();
    }
    catch (JSONException exception)
    {
      System.out.println("JSONException: " + exception);
      return null;
    }
  }

  public static JSONArray getTestJSONArray(String file)
  {
    InputStream inputStream = ContentItemTest.class.getResourceAsStream(file);
    String json = new Scanner(inputStream).useDelimiter("\\A").next();
    try
    {
      return (JSONArray) new JSONArray(json);
    }
    catch (JSONException exception)
    {
      System.out.println("JSONException: " + exception);
      return null;
    }
  }
}
