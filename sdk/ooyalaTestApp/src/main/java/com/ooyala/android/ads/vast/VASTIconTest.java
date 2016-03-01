package com.ooyala.android.ads.vast;

import android.test.AndroidTestCase;

import com.ooyala.android.TestConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by zchen on 2/26/16.
 */
public class VASTIconTest extends AndroidTestCase {
  public void testIconAttributes() {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputStream is = TestConstants.getTestAssetAsStream(getContext(), TestConstants.TEST_VAST3_ICONS);
      Document doc = db.parse(is);
      Element adXML = doc.getDocumentElement();
      VASTAdSpot adSpot = new VASTAdSpot(adXML);
      assertEquals(1, adSpot.getAds().size());
      VASTAd ad = adSpot.getAds().get(0);
      assertNotNull(ad);
      assertEquals(1, ad.getSequence().size());
      VASTLinearAd linear = ad.getSequence().get(0).getLinear();
      assertNotNull(linear);
      assertEquals(1, linear.getIcons().size());
      VASTIcon icon = linear.getIcons().get(0);
      assertNotNull(icon);

      assertEquals("ad10", icon.getProgram());
      assertEquals(100, icon.getWidth());
      assertEquals(100, icon.getHeight());
      assertEquals(0, icon.getXPosition());
      assertEquals(Integer.MAX_VALUE, icon.getYPosition());
      assertEquals(10.0, icon.getDuration());
      assertEquals(1.0, icon.getOffset());
      assertEquals("VPAID", icon.getApiFramework());
      assertEquals("image/png", icon.getCreativeType());
      assertEquals(icon.getResourceType(), VASTIcon.ResourceType.Static);
      assertEquals("http://player.ooyala.com/static/v4/testAssets/creatives/vast_icon.png", icon.getResourceUrl());
      assertEquals("http://www.ooyala.com/", icon.getClickThrough());
      assertEquals(1, icon.getClickTrackings().size());
      assertEquals("http://example-ad-server.ooyala.com/trk?uid=fb94f0c8e8c20963db4a9806a1906c0a&action=iconClickThrough&value=532c20fb6c132923bb000005", icon.getClickTrackings().get(0));
      assertEquals(1, icon.getViewTrackings().size());
      assertEquals("http://example-ad-server.ooyala.com/trk?uid=fb94f0c8e8c20963db4a9806a1906c0a&action=iconClientSideImpression&value=532c20fb6c132923bb000005", icon.getViewTrackings().get(0));


    } catch (Exception e) {
      System.err.println("Exception: " + e.getMessage());
      fail();
    }
  }
}
