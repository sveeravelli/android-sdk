package com.ooyala.android;

import android.test.AndroidTestCase;

public class OoyalaAdSpotTest extends AndroidTestCase {
  public OoyalaAdSpotTest() {
    super();
  }

  protected void setUp() {

  }

  protected void tearDown() {

  }

  /**
   * Test create. Also tests update.
   */
  public void testInitializers() {
    OoyalaAdSpot adSpot = new OoyalaAdSpot(
        ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_AD_OOYALA), null);
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
  public void testEmbedCodesToAuthorize() {
    OoyalaAdSpot adSpot = new OoyalaAdSpot(
        ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_AD_OOYALA), null);
    assertEquals(1, adSpot.embedCodesToAuthorize().size());
    assertEquals(adSpot.getEmbedCode(), adSpot.embedCodesToAuthorize().get(0));
  }

  /**
   * Test fetchPlaybackInfo and getStream
   */
  public void testStreamsInfo() {
    PlayerAPIClient api = new PlayerAPIClient(TestConstants.TEST_PCODE, "www.ooyala.com", null);
    OoyalaAdSpot adSpot = new OoyalaAdSpot(
        ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_AD_OOYALA), api);
    assertTrue(adSpot.fetchPlaybackInfo());
    // FIXME: This test asset has multiple streams with the same resolution and bitrate...
    String url = adSpot.getStream().decodedURL().toString();
    assertTrue(url
        .equals("http://ak.c.ooyala.com/JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B/DOcJ-FxaFrRg4gtGIwOjRpOmc3OxgEkc")
        || url
            .equals("http://ak.c.ooyala.com/JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B/DOcJ-FxaFrRg4gtGMwOjRpOmc3OzS3Gm"));
  }
}