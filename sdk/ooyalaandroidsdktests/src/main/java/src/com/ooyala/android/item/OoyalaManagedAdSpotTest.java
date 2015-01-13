package com.ooyala.android.item;

import android.test.AndroidTestCase;

import com.ooyala.android.OoyalaAdSpot;
import com.ooyala.android.TestConstants;
import com.ooyala.android.ads.vast.VASTAdSpot;

public class OoyalaManagedAdSpotTest extends AndroidTestCase {
  public OoyalaManagedAdSpotTest() {
    super();
  }

  @Override
  protected void setUp() {

  }

  @Override
  protected void tearDown() {

  }

  /**
   * Test create. Also tests update. Cannot test the constructor since AdSpot is an abstract class.
   */
  public void testCreate() {
    OoyalaManagedAdSpot ad = OoyalaManagedAdSpot.create(
        TestConstants.getTestJSON(TestConstants.TEST_DICTIONARY_AD_OOYALA),
        null);
    assertNotNull(ad);
    assertEquals(OoyalaAdSpot.class, ad.getClass());
    OoyalaAdSpot adSpot = (OoyalaAdSpot) ad;
    assertEquals("JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B", adSpot.getEmbedCode());
    assertEquals(0, adSpot.getTime());
    assertEquals("http://www.bhangraempire.com", adSpot.getClickURL().toString());
    assertEquals(1, adSpot.getTrackingURLs().size());
    assertEquals("http://www.ooyala.com/track", adSpot.getTrackingURLs().get(0).toString());

    ad = OoyalaManagedAdSpot.create(
        TestConstants.getTestJSON(TestConstants.TEST_DICTIONARY_AD_VAST), null);
    assertEquals(VASTAdSpot.class, ad.getClass());
    assertEquals(
        "http://www.daveproxy.co.uk/browse.php/Oi8vYWZlMi5zcGVjaWZpY2NsaWNrLm5ldC9hZHNlcnZlLz9sPTIwNTE3JnQ9eCZybmQ9YkFvcnJxSyxiaGxhZnNvaGRsQXg_3D/b13/fnorefer/",
        ((VASTAdSpot) ad).getVASTURL().toString());
  }
}
