package com.ooyala.android.ads.vast;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ooyala.android.TestConstants;

import android.test.AndroidTestCase;

public class VASTLinearAdTest extends AndroidTestCase {
  public VASTLinearAdTest() {
    super();
  }

  protected void setUp() {}

  protected void tearDown() {}

  public void testInitializers() {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputStream is = this.getClass().getResourceAsStream(TestConstants.TEST_XML_VAST_AD);
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
      e.printStackTrace();
      fail();
    }
  }
}
