package com.ooyala.android.ads.vast;

import android.test.AndroidTestCase;

import com.ooyala.android.TestConstants;
import com.ooyala.android.util.DebugMode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class VASTLinearAdTest extends AndroidTestCase {
  private static final String TAG = VASTLinearAdTest.class.getSimpleName();

  public VASTLinearAdTest() {
    super();
  }

  @Override
  protected void setUp() {}

  @Override
  protected void tearDown() {}

  public void testInitializers() {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputStream is = TestConstants.getTestAssetAsStream(getContext(), TestConstants.TEST_XML_VAST_AD);
      Document doc = db.parse(is);
      Element adXML = doc.getDocumentElement();
      Element il = (Element) adXML.getElementsByTagName("InLine").item(0);
      Element creatives = (Element) il.getElementsByTagName("Creatives").item(0);
      Element lcreative = (Element) creatives.getElementsByTagName("Creative").item(0);
      Element linearXML = (Element) lcreative.getElementsByTagName("Linear").item(0);
      VASTLinearAd linear = new VASTLinearAd(linearXML);
      assertEquals(
          linear.getStream().decodedURL().toString(),
          "http://vindicoasset.edgesuite.net/Repository/CampaignCreative/Campaign_8759/INSTREAMAD/93084_scrambled_eggs_10_v2_audio_17_03_11_MP4_360p_4x3.mp4");
    } catch (Exception e) {
      System.err.println("Exception: " + e.getMessage());
      DebugMode.logE( TAG, "Caught!", e );
      fail();
    }
  }

  public void testSkipOffset() {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputStream is = TestConstants.getTestAssetAsStream(getContext(), TestConstants.TEST_VAST3_SKIPPABLE);
      Document doc = db.parse(is);
      Element adXML = doc.getDocumentElement();
      VASTAdSpot adSpot = new VASTAdSpot(adXML);
      assertEquals(2, adSpot.getAds().size());
      VASTAd ad = adSpot.getAds().get(0);
      assertEquals(1, ad.getSequence().size());
      VASTLinearAd linear = ad.getSequence().get(0).getLinear();
      assertNotNull(linear);
      assertTrue(linear.getSkippable());
      assertEquals(5.0, linear.getSkipOffset());

      ad = adSpot.getAds().get(1);
      assertEquals(1, ad.getSequence().size());
      linear = ad.getSequence().get(0).getLinear();
      assertNotNull(linear);
      assertTrue(linear.getSkippable());
      assertEquals(linear.getDuration() * 0.2, linear.getSkipOffset());

    } catch (Exception e) {
      System.err.println("Exception: " + e.getMessage());
      DebugMode.logE(TAG, "Caught!", e);
      fail();
    }
  }

  public void testSkipOffsetNegative() {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputStream is = TestConstants.getTestAssetAsStream(getContext(), TestConstants.TEST_VAST3_PODDED);
      Document doc = db.parse(is);
      Element adXML = doc.getDocumentElement();
      VASTAdSpot adSpot = new VASTAdSpot(adXML);
      assertEquals(3, adSpot.getAds().size());
      VASTAd ad = adSpot.getAds().get(0);
      assertEquals(1, ad.getSequence().size());
      VASTLinearAd linear = ad.getSequence().get(0).getLinear();
      assertNotNull(linear);
      assertFalse(linear.getSkippable());
    } catch (Exception e) {
      System.err.println("Exception: " + e.getMessage());
      DebugMode.logE(TAG, "Caught!", e);
      fail();
    }
  }

  public void testSkipOffsetPercentage() {
    String s = "-1%";
    VASTLinearAd.SkipOffset offset = VASTLinearAd.parseSkipOffset(s);
    assertEquals(VASTLinearAd.SkipOffsetType.Percentage, offset.getType());
    assertEquals(0.0, offset.getValue());

    s = "50%";
    offset = VASTLinearAd.parseSkipOffset(s);
    assertEquals(VASTLinearAd.SkipOffsetType.Percentage, offset.getType());
    assertEquals(0.5, offset.getValue());

    s = "12.6%";
    offset = VASTLinearAd.parseSkipOffset(s);
    assertEquals(VASTLinearAd.SkipOffsetType.Percentage, offset.getType());
    assertEquals(0.126, offset.getValue());

    s = "100%";
    offset = VASTLinearAd.parseSkipOffset(s);
    assertEquals(VASTLinearAd.SkipOffsetType.Percentage, offset.getType());
    assertEquals(1.0, offset.getValue());

    s = "126%";
    offset = VASTLinearAd.parseSkipOffset(s);
    assertEquals(VASTLinearAd.SkipOffsetType.Percentage, offset.getType());
    assertEquals(1.0, offset.getValue());
  }

  public void testSkipOffsetPercentageFail() {
    String s = "ABCD%";
    VASTLinearAd.SkipOffset offset = VASTLinearAd.parseSkipOffset(s);
    assertEquals(VASTLinearAd.SkipOffsetType.None, offset.getType());
  }

  public void testSkipOffsetSeconds() {
    String s = "03:30:00";
    VASTLinearAd.SkipOffset offset = VASTLinearAd.parseSkipOffset(s);
    assertEquals(VASTLinearAd.SkipOffsetType.Seconds, offset.getType());
    assertEquals(3*3600.0 + 30* 60, offset.getValue());

    s = "5.000";
    offset = VASTLinearAd.parseSkipOffset(s);
    assertEquals(VASTLinearAd.SkipOffsetType.Seconds, offset.getType());
    assertEquals(5.0, offset.getValue());

    s = "00:00:08.520";
    offset = VASTLinearAd.parseSkipOffset(s);
    assertEquals(VASTLinearAd.SkipOffsetType.Seconds, offset.getType());
    assertEquals(8.52, offset.getValue());
  }

  public void testSkipOffsetSecondsFail() {
    String s = "03:abc:00";
    VASTLinearAd.SkipOffset offset = VASTLinearAd.parseSkipOffset(s);
    assertEquals(VASTLinearAd.SkipOffsetType.None, offset.getType());

    s = "03::00";
    offset = VASTLinearAd.parseSkipOffset(s);
    assertEquals(VASTLinearAd.SkipOffsetType.None, offset.getType());

    s = "-1:00:00";
    offset = VASTLinearAd.parseSkipOffset(s);
    assertEquals(VASTLinearAd.SkipOffsetType.None, offset.getType());
  }
}
