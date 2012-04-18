package com.ooyala.android;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.ooyala.android.AuthorizableItem.AuthCode;

import android.test.AndroidTestCase;

public class PlayerAPIClientTest extends AndroidTestCase {
  public PlayerAPIClient api;

  public PlayerAPIClientTest() {
    super();
  }

  protected void setUp() {
    api = new PlayerAPIClient(new OoyalaAPIHelper(TestConstants.TEST_API_KEY, TestConstants.TEST_SECRET),
        TestConstants.TEST_PCODE, "www.ooyala.com", null);
  }

  protected void tearDown() {}

  public void testAuthorizeVideo() throws OoyalaException {
    ContentItem video = ContentItem.create(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_VIDEO),
        TestConstants.TEST_VIDEO, api);
    assertTrue(api.authorize(video));
    assertTrue(video.isAuthorized());
    assertEquals(video.getAuthCode(), AuthCode.AUTHORIZED);
    assertTrue(video instanceof Video);
    assertTrue(((Video) video).getStream().decodedURL().toString()
        .equals("http://ak.c.ooyala.com/UwN2wxMzpU1Nl_qojlX8iLlKEHfl4HLM/DOcJ-FxaFrRg4gtGMwOjRpOmc3OzS3Gm")
        || ((Video) video)
            .getStream()
            .decodedURL()
            .toString()
            .equals(
                "http://ak.c.ooyala.com/UwN2wxMzpU1Nl_qojlX8iLlKEHfl4HLM/DOcJ-FxaFrRg4gtGIwOjRpOmc3OxgEkc"));

    video = ContentItem.create(
        ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_VIDEO_WITH_AD_OOYALA),
        TestConstants.TEST_VIDEO_WITH_AD_OOYALA, api);
    assertTrue(api.authorize(video));
    assertTrue(video.isAuthorized());
    assertEquals(video.getAuthCode(), AuthCode.AUTHORIZED);
    assertTrue(video instanceof Video);
    assertTrue(((Video) video).getStream().decodedURL().toString()
        .equals("http://ak.c.ooyala.com/g3N2wxMzqxoB84c3dan5xyXTxdrhX1km/DOcJ-FxaFrRg4gtGMwOjRpOmc3OzS3Gm")
        || ((Video) video)
            .getStream()
            .decodedURL()
            .toString()
            .equals(
                "http://ak.c.ooyala.com/g3N2wxMzqxoB84c3dan5xyXTxdrhX1km/DOcJ-FxaFrRg4gtGIwOjRpOmc3OxgEkc"));
    assertTrue(((Video) video).fetchPlaybackInfo());
    assertTrue(((OoyalaAdSpot) ((Video) video).getAds().get(0)).getStream().decodedURL().toString()
        .equals("http://ak.c.ooyala.com/JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B/DOcJ-FxaFrRg4gtGMwOjRpOmc3OzS3Gm")
        || ((OoyalaAdSpot) ((Video) video).getAds().get(0))
            .getStream()
            .decodedURL()
            .toString()
            .equals(
                "http://ak.c.ooyala.com/JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B/DOcJ-FxaFrRg4gtGIwOjRpOmc3OxgEkc"));

    List<String> embeds = new ArrayList<String>();
    embeds.add(TestConstants.TEST_REMOTE_ASSET);
    video = api.contentTree(embeds);
    assertTrue(api.authorize(video));
    assertTrue(video.isAuthorized());
    assertEquals(video.getAuthCode(), AuthCode.AUTHORIZED);
    assertTrue(video instanceof Video);
    assertEquals(((Video) video).getStream().decodedURL().toString(),
        "http://ak.c.ooyala.com/JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B/DOcJ-FxaFrRg4gtGEwOjFyazowazsvY7");

    embeds.clear();
    embeds.add(TestConstants.TEST_LIVE_STREAM);
    video = api.contentTree(embeds);
    assertTrue(api.authorize(video));
    assertTrue(video.isAuthorized());
    assertEquals(video.getAuthCode(), AuthCode.AUTHORIZED);
    assertTrue(video instanceof Video);
    assertTrue(((Video) video).isLive());
  }

  public void testAuthorizeChannel() throws OoyalaException {
    ContentItem channel = ContentItem.create(
        ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL), TestConstants.TEST_CHANNEL, api);
    assertTrue(api.authorize(channel));
    assertTrue(channel.isAuthorized());
    assertEquals(channel.getAuthCode(), AuthCode.AUTHORIZED);
    assertTrue(channel instanceof Channel);
    assertTrue(((Channel) channel).firstVideo().getStream().decodedURL().toString()
        .equals("http://ak.c.ooyala.com/JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B/DOcJ-FxaFrRg4gtGMwOjRpOmc3OzS3Gm")
        || ((Channel) channel)
            .firstVideo()
            .getStream()
            .decodedURL()
            .toString()
            .equals(
                "http://ak.c.ooyala.com/JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B/DOcJ-FxaFrRg4gtGIwOjRpOmc3OxgEkc"));
  }

  public void testAuthorizeChannelSet() throws OoyalaException {
    ContentItem channelSet = ContentItem.create(
        ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CHANNEL_SET),
        TestConstants.TEST_CHANNEL_SET, api);
    assertTrue(api.authorize(channelSet));
    assertTrue(channelSet.isAuthorized());
    assertEquals(channelSet.getAuthCode(), AuthCode.AUTHORIZED);
    assertTrue(channelSet instanceof ChannelSet);
    assertTrue(((ChannelSet) channelSet).firstVideo().getStream().decodedURL().toString()
        .equals("http://ak.c.ooyala.com/JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B/DOcJ-FxaFrRg4gtGMwOjRpOmc3OzS3Gm")
        || ((ChannelSet) channelSet)
            .firstVideo()
            .getStream()
            .decodedURL()
            .toString()
            .equals(
                "http://ak.c.ooyala.com/JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B/DOcJ-FxaFrRg4gtGIwOjRpOmc3OxgEkc"));
  }

  public void testAuthorizeDynamicChannel() throws OoyalaException {
    List<String> embeds = new ArrayList<String>();
    embeds.add(TestConstants.TEST_VIDEO);
    embeds.add(TestConstants.TEST_VIDEO_WITH_AD_OOYALA);
    ContentItem dynamicChannel = ContentItem.create(
        ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_DYNAMIC_CHANNEL), embeds, api);
    assertTrue(api.authorize(dynamicChannel));
    assertTrue(dynamicChannel.isAuthorized());
    assertEquals(dynamicChannel.getAuthCode(), AuthCode.AUTHORIZED);
    assertTrue(dynamicChannel instanceof DynamicChannel);
    Iterator<Entry<String, Video>> i = ((DynamicChannel) dynamicChannel).getVideos().entrySet().iterator();
    Video firstVideo = i.next().getValue();
    Video secondVideo = i.next().getValue();
    assertTrue(firstVideo.getStream().decodedURL().toString()
        .equals("http://ak.c.ooyala.com/UwN2wxMzpU1Nl_qojlX8iLlKEHfl4HLM/DOcJ-FxaFrRg4gtGMwOjRpOmc3OzS3Gm")
        || firstVideo
            .getStream()
            .decodedURL()
            .toString()
            .equals(
                "http://ak.c.ooyala.com/UwN2wxMzpU1Nl_qojlX8iLlKEHfl4HLM/DOcJ-FxaFrRg4gtGIwOjRpOmc3OxgEkc"));
    assertTrue(secondVideo.getStream().decodedURL().toString()
        .equals("http://ak.c.ooyala.com/g3N2wxMzqxoB84c3dan5xyXTxdrhX1km/DOcJ-FxaFrRg4gtGMwOjRpOmc3OzS3Gm")
        || secondVideo
            .getStream()
            .decodedURL()
            .toString()
            .equals(
                "http://ak.c.ooyala.com/g3N2wxMzqxoB84c3dan5xyXTxdrhX1km/DOcJ-FxaFrRg4gtGIwOjRpOmc3OxgEkc"));
  }

  public void testContentTreeVideo() throws OoyalaException {
    List<String> embeds = new ArrayList<String>();
    embeds.add(TestConstants.TEST_VIDEO);
    ContentItem rootItem = api.contentTree(embeds);
    assertTrue(rootItem != null);
    assertEquals(rootItem.getEmbedCode(), TestConstants.TEST_VIDEO);

    embeds.clear();
    embeds.add(TestConstants.TEST_REMOTE_ASSET);
    rootItem = api.contentTree(embeds);
    assertTrue(rootItem != null);
    assertEquals(rootItem.getEmbedCode(), TestConstants.TEST_REMOTE_ASSET);

    embeds.clear();
    embeds.add(TestConstants.TEST_LIVE_STREAM);
    rootItem = api.contentTree(embeds);
    assertTrue(rootItem != null);
    assertEquals(rootItem.getEmbedCode(), TestConstants.TEST_LIVE_STREAM);
    assertTrue(((Video) rootItem).isLive());
  }

  public void testContentTreeDynamicChannel() throws OoyalaException {
    List<String> embeds = new ArrayList<String>();
    embeds.add(TestConstants.TEST_VIDEO);
    embeds.add(TestConstants.TEST_VIDEO_WITH_AD_OOYALA);
    ContentItem rootItem = api.contentTree(embeds);
    assertTrue(rootItem != null);
    assertTrue(rootItem instanceof DynamicChannel);
    assertEquals(((DynamicChannel) rootItem).getVideos().size(), 2);
    Iterator<Entry<String, Video>> i = ((DynamicChannel) rootItem).getVideos().entrySet().iterator();
    Video firstVideo = i.next().getValue();
    Video secondVideo = i.next().getValue();
    assertEquals(firstVideo.getEmbedCode(), TestConstants.TEST_VIDEO);
    assertEquals(secondVideo.getEmbedCode(), TestConstants.TEST_VIDEO_WITH_AD_OOYALA);
  }

  public void testContentTreeByExternalIdsVideo() throws OoyalaException {
    List<String> externals = new ArrayList<String>();
    externals.add(TestConstants.TEST_VIDEO_EXTERNAL_ID);
    ContentItem rootItem = api.contentTreeByExternalIds(externals);
    assertTrue(rootItem != null);
    assertEquals(rootItem.getEmbedCode(), TestConstants.TEST_VIDEO);
  }

  public void testContentTreeByExternalIdsDynamicChannel() throws OoyalaException {
    List<String> externals = new ArrayList<String>();
    externals.add(TestConstants.TEST_VIDEO_EXTERNAL_ID);
    externals.add(TestConstants.TEST_VIDEO_WITH_AD_OOYALA_EXTERNAL_ID);
    ContentItem rootItem = api.contentTreeByExternalIds(externals);
    assertTrue(rootItem != null);
    assertTrue(rootItem instanceof DynamicChannel);
    assertEquals(((DynamicChannel) rootItem).getVideos().size(), 2);
    Iterator<Entry<String, Video>> i = ((DynamicChannel) rootItem).getVideos().entrySet().iterator();
    Video firstVideo = i.next().getValue();
    Video secondVideo = i.next().getValue();
    assertEquals(firstVideo.getEmbedCode(), TestConstants.TEST_VIDEO);
    assertEquals(secondVideo.getEmbedCode(), TestConstants.TEST_VIDEO_WITH_AD_OOYALA);
  }

  public void testContentTreeNext() throws OoyalaException {
    List<String> embeds = new ArrayList<String>();
    embeds.add(TestConstants.TEST_CHANNEL_SET);
    ContentItem rootItem = api.contentTree(embeds);
    assertTrue(rootItem != null);
    assertTrue(rootItem instanceof ChannelSet);
    assertEquals(((ChannelSet) rootItem).getChannels().size(), 2);
    Iterator<Entry<String, Channel>> i = ((ChannelSet) rootItem).getChannels().entrySet().iterator();
    i.next();
    Channel second = i.next().getValue();
    assertTrue(second.hasMoreChildren());
    class TestPaginatedItemListener implements PaginatedItemListener {
      public boolean fetched = false;
      public boolean error = false;
      public int idx = -2;
      public int cnt = -2;

      @Override
      public void onItemsFetched(int firstIndex, int count, OoyalaException error) {
        if (error != null) {
          this.error = true;
        } else {
          this.fetched = true;
          this.idx = firstIndex;
          this.cnt = count;
        }
      }
    };
    TestPaginatedItemListener listener = new TestPaginatedItemListener();
    assertTrue(second.fetchMoreChildren(listener));
    assertFalse(second.fetchMoreChildren(listener));
    while (!listener.fetched && !listener.error) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {}
    }
    assertFalse(second.hasMoreChildren());
    assertFalse(listener.error);
    assertTrue(listener.fetched);
    assertEquals(listener.idx, 0);
    assertEquals(listener.cnt, 2);
  }
}
