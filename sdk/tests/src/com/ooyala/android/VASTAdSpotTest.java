package com.ooyala.android;

import android.test.AndroidTestCase;

public class VASTAdSpotTest extends AndroidTestCase {
  public VASTAdSpotTest() {
    super();
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }

  /**
   * @test Test VASTAdSpot.initWithDictionary:api: and VASTAdSpot.updateWithDictionary:
   */
  public void testInitializer() {
    VASTAdSpot vast = new VASTAdSpot(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_AD_VAST), null);
    assertEquals(vast.getVASTURL().toString(), "http://ad3.liverail.com/?LR_PUBLISHER_ID=1331&LR_CAMPAIGN_ID=229&LR_SCHEMA=vast2");
  }

  /**
   * @test Test VASTAdSpot.fetchPlaybackInfo
   */
  public void testFetchPlaybackInfo() {
    VASTAdSpot vast = new VASTAdSpot(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_AD_VAST), null);
    assertTrue(vast.fetchPlaybackInfo());
    assertEquals(vast.getAds().size(), 1);
    VASTAd ad = vast.getAds().get(0);
    assertTrue(ad != null);
    assertTrue(ad.getTitle() != null);
    assertEquals(ad.getDescription(), "");
    assertEquals(ad.getSurveyURLs().size(), 0);
    assertEquals(ad.getSystem(), "LiveRail");
    assertEquals(ad.getSystemVersion(), "4.4.0-1");
    assertEquals(ad.getErrorURLs().size(), 0);
    assertEquals(ad.getExtensions(), null);
    assertEquals(ad.getImpressionURLs().size(), 3);
    assertTrue(ad.getImpressionURLs().get(0).contains("http://t4.liverail.com/?metric=impression"));
    assertTrue(ad.getImpressionURLs().get(1).contains("http://pixel.quantserve.com/pixel"));
    assertTrue(ad.getImpressionURLs().get(2).contains("http://t4.liverail.com/?metric=rsync"));
    assertEquals(ad.getSequence().size(), 1);
    VASTSequenceItem item = ad.getSequence().get(0);
    assertTrue(item.getNonLinears() == null);
    assertTrue(item.getCompanions() != null);
    assertTrue(item.getLinear().getStream() == null);
  }
}
