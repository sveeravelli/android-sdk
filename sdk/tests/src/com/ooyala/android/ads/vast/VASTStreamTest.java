package com.ooyala.android.ads.vast;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.test.AndroidTestCase;

import com.ooyala.android.TestConstants;
import com.ooyala.android.item.Stream;

public class VASTStreamTest extends AndroidTestCase {
  public VASTStreamTest() {
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
      InputStream is = TestConstants.getTestResourceAsStream(TestConstants.TEST_XML_VAST_AD);
      Document doc = db.parse(is);
      Element adXML = doc.getDocumentElement();
      Element il = (Element) adXML.getElementsByTagName("InLine").item(0);
      Element creatives = (Element) il.getElementsByTagName("Creatives").item(0);
      Element lcreative = (Element) creatives.getElementsByTagName("Creative").item(0);
      Element linear = (Element) lcreative.getElementsByTagName("Linear").item(0);
      Element mediaFiles = (Element) linear.getElementsByTagName("MediaFiles").item(0);
      Element mediaFile = (Element) mediaFiles.getElementsByTagName("MediaFile").item(0);
      VASTStream stream = new VASTStream(mediaFile);
      assertEquals(
          stream.decodedURL().toString(),
          "http://vindicoasset.edgesuite.net/Repository/CampaignCreative/Campaign_8759/INSTREAMAD/93084_scrambled_eggs_10_v2_audio_17_03_11_MP4_360p_4x3.mp4");
      assertEquals(stream.getVastDeliveryType(), "progressive");
      assertEquals(stream.getDeliveryType(), Stream.DELIVERY_TYPE_MP4);
      assertEquals(stream.getWidth(), 400);
      assertEquals(stream.getHeight(), 300);
      assertEquals(stream.getVideoBitrate(), 400);
    } catch (Exception e) {
      System.err.println("Exception: " + e.getMessage());
      e.printStackTrace();
      fail();
    }
  }
}
