package com.ooyala.android;

import android.test.AndroidTestCase;

import com.ooyala.android.item.AuthorizableItem.AuthCode;
import com.ooyala.android.item.Channel;
import com.ooyala.android.item.ChannelSet;
import com.ooyala.android.item.ContentItem;
import com.ooyala.android.item.DynamicChannel;
import com.ooyala.android.item.ModuleData;
import com.ooyala.android.item.Video;
import com.ooyala.android.util.DebugMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class PlayerAPIClientTest extends AndroidTestCase {
  private static final String TAG = PlayerAPIClientTest.class.getSimpleName();
  public PlayerAPIClient api;

  public PlayerAPIClientTest() {
    super();
  }

  @Override
  protected void setUp() {
    PlayerDomain domain = null;
    try {
      domain = new PlayerDomain("http://www.ooyala.com");
    } catch (Exception e) {
      // TODO Auto-generated catch block
      DebugMode.logE( TAG, "Caught!", e );
    }
    api = new PlayerAPIClient(TestConstants.TEST_PCODE, domain, null);
  }

  @Override
  protected void tearDown() {}

  public void testAuthorizeVideo() throws OoyalaException {
    ContentItem video = ContentItem.create(TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_VIDEO),
        TestConstants.TEST_VIDEO, new OoyalaAPIClient(api));
    assertTrue(api.authorize(video));
    assertTrue(video.isAuthorized());
    assertEquals(video.getAuthCode(), AuthCode.AUTHORIZED);
    assertTrue(video instanceof Video);
    String url = ((Video) video).getStream().decodedURL().toString();
    assertTrue(url.startsWith("http://"));

    OoyalaPlayer.enableHLS = true;
    ContentItem videoHLS = ContentItem.create(
        TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_VIDEO), TestConstants.TEST_VIDEO, new OoyalaAPIClient(api));
    assertTrue(api.authorize(videoHLS));
    assertTrue(videoHLS.isAuthorized());
    assertEquals(videoHLS.getAuthCode(), AuthCode.AUTHORIZED);
    assertTrue(videoHLS instanceof Video);
    url = ((Video) videoHLS).getStream().decodedURL().toString();
    assertTrue(url.startsWith("http://"));

    OoyalaPlayer.enableHLS = false;
    OoyalaPlayer.enableHighResHLS = true;
    videoHLS = ContentItem.create(TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_VIDEO),
        TestConstants.TEST_VIDEO, new OoyalaAPIClient(api));
    assertTrue(api.authorize(videoHLS));
    assertTrue(videoHLS.isAuthorized());
    assertEquals(videoHLS.getAuthCode(), AuthCode.AUTHORIZED);
    assertTrue(videoHLS instanceof Video);
    url = ((Video) videoHLS).getStream().decodedURL().toString();
    assertTrue(url.startsWith("http://"));
    OoyalaPlayer.enableHighResHLS = false;

    video = ContentItem.create(
        TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_VIDEO_WITH_AD_OOYALA),
        TestConstants.TEST_VIDEO_WITH_AD_OOYALA, new OoyalaAPIClient(api));
    assertTrue(api.authorize(video));
    assertTrue(video.isAuthorized());
    assertEquals(video.getAuthCode(), AuthCode.AUTHORIZED);
    assertTrue(video instanceof Video);
    url = ((Video) video).getStream().decodedURL().toString();
    assertTrue(url.startsWith("http://"));
    assertTrue(((Video) video).fetchPlaybackInfo());
    url = ((OoyalaAdSpot) ((Video) video).getAds().get(0)).getStream()
        .decodedURL().toString();
    assertTrue(url.startsWith("http://"));

    List<String> embeds = new ArrayList<String>();
    embeds.add(TestConstants.TEST_REMOTE_ASSET);
    video = api.contentTree(embeds);
    assertTrue(api.authorize(video));
    assertTrue(video.isAuthorized());
    assertEquals(video.getAuthCode(), AuthCode.AUTHORIZED);
    assertTrue(video instanceof Video);
    url = ((Video) video).getStream().decodedURL().toString();
    assertTrue(url.startsWith("http://"));

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
        TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_CHANNEL), TestConstants.TEST_CHANNEL, new OoyalaAPIClient(api));
    assertTrue(api.authorize(channel));
    assertTrue(channel.isAuthorized());
    assertEquals(channel.getAuthCode(), AuthCode.AUTHORIZED);
    assertTrue(channel instanceof Channel);
    String decodeUrl = ((Channel) channel).firstVideo().getStream()
        .decodedURL().toString();
    assertTrue(decodeUrl.startsWith("http://"));
  }

  public void testAuthorizeChannelSet() throws OoyalaException {
    ContentItem channelSet = ContentItem.create(
        TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_CHANNEL_SET),
        TestConstants.TEST_CHANNEL_SET, new OoyalaAPIClient(api));
    assertTrue(api.authorize(channelSet));
    assertTrue(channelSet.isAuthorized());
    assertEquals(channelSet.getAuthCode(), AuthCode.AUTHORIZED);
    assertTrue(channelSet instanceof ChannelSet);
    String url = ((ChannelSet) channelSet).firstVideo().getStream()
        .decodedURL().toString();
    assertTrue(url.startsWith("http://"));
  }

  public void testAuthorizeDynamicChannel() throws OoyalaException {
    List<String> embeds = new ArrayList<String>();
    embeds.add(TestConstants.TEST_VIDEO);
    embeds.add(TestConstants.TEST_VIDEO_WITH_AD_OOYALA);
    ContentItem dynamicChannel = ContentItem.create(
        TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_DYNAMIC_CHANNEL), embeds, new OoyalaAPIClient(api));
    assertTrue(api.authorize(dynamicChannel));
    assertTrue(dynamicChannel.isAuthorized());
    assertEquals(dynamicChannel.getAuthCode(), AuthCode.AUTHORIZED);
    assertTrue(dynamicChannel instanceof DynamicChannel);
    Iterator<Entry<String, Video>> i = ((DynamicChannel) dynamicChannel).getVideos().entrySet().iterator();
    Video firstVideo = i.next().getValue();
    Video secondVideo = i.next().getValue();
    String url = firstVideo.getStream().decodedURL().toString();
    assertTrue(url.startsWith("http://"));
    url = secondVideo.getStream().decodedURL().toString();
    assertTrue(url.startsWith("http://"));
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
    assertNotNull(firstVideo.getEmbedCode());
    assertNotNull(secondVideo.getEmbedCode());
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
    assertNotNull(firstVideo.getEmbedCode());
    assertNotNull(secondVideo.getEmbedCode());
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
    assertTrue(api.fetchMoreChildrenForPaginatedParentItem(second, listener));
    assertFalse(api.fetchMoreChildrenForPaginatedParentItem(second, listener));
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

  public void testFetchMetadata() throws OoyalaException {
    ContentItem video = ContentItem.create(TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_VIDEO),
        TestConstants.TEST_VIDEO, new OoyalaAPIClient(api));
    assertTrue(api.fetchMetadata(video));

    //Check Metadata
    assertEquals(video.getMetadata().get("location"), "boston");
    assertEquals(video.getMetadata().get("year"), "2011");

    //Check moduleData
    ModuleData module = video.getModuleData().get("v3-playlists");
    assertNotNull(module);
    assertEquals(module.getName(), "v3-playlists");
    assertEquals(module.getType(), "v3-playlists");
    assertEquals(module.getMetadata().get("testparam"), "true");
  }
}
