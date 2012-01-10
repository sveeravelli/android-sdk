package com.ooyala.android;

import android.test.AndroidTestCase;

public class OoyalaAdSpotTest extends AndroidTestCase
{
  public OoyalaAdSpotTest()
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
   * Test create.  Also tests update.
   */
  public void testInitializers()
  {
    OoyalaAdSpot adSpot = new OoyalaAdSpot(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_AD_OOYALA), null);
    assertNotNull(adSpot);
    assertEquals(OoyalaAdSpot.class, adSpot.getClass());
    assertEquals("JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B", adSpot.getEmbedCode());
    assertEquals(0, adSpot.getTime());
    assertEquals("http://www.bhangraempire.com", adSpot.getClickURL().toString());
    assertEquals(1, adSpot.getTrackingURLs().size());
    assertEquals("http://www.ooyala.com/track", adSpot.getTrackingURLs().get(0).toString());
  }

  /**
   * Test embedCodesToAuthorize
   */
  public void testEmbedCodesToAuthorize()
  {
    OoyalaAdSpot adSpot = new OoyalaAdSpot(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_AD_OOYALA), null);
    assertEquals(1, adSpot.embedCodesToAuthorize().size());
    assertEquals(adSpot.getEmbedCode(), adSpot.embedCodesToAuthorize().get(0));
  }

  /**
   * Test fetchPlaybackInfo and getStream
   */
  public void testStreamsInfo()
  {
    PlayerAPIClient api = new PlayerAPIClient(new OoyalaAPIHelper(TestConstants.TEST_API_KEY, TestConstants.TEST_SECRET), TestConstants.TEST_PCODE, "www.ooyala.com");
    OoyalaAdSpot adSpot = new OoyalaAdSpot(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_AD_OOYALA), api);
    assertTrue(adSpot.fetchPlaybackInfo());
    assertEquals("http://player.ooyala.com/player/iphone/JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B.m3u8", adSpot.getStream().decodedURL().toString());
  }
}