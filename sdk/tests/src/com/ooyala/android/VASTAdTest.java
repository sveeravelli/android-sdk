package com.ooyala.android;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.test.AndroidTestCase;

public class VASTAdTest extends AndroidTestCase {
  public VASTAdTest() {
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
      VASTAd ad = new VASTAd(adXML);
      assertTrue(ad != null);
      assertEquals(ad.getTitle(), "93084_scrambled_eggs_10_v2_audio_17_03_11_MP4_360p_4x3.mp4");
      assertEquals(ad.getDescription(), "");
      assertEquals(ad.getSurveyURLs().size(), 1);
      assertEquals(ad.getSystem(), "VINDICO");
      assertEquals(ad.getSystemVersion(), "2.0");
      assertEquals(ad.getErrorURLs().size(), 1);
      assertTrue(ad.getExtensions() != null);
      assertEquals(ad.getImpressionURLs().size(), 4);
      assertEquals(ad.getImpressionURLs().get(0),
          "http://afe2.specificclick.net/dserve/t=d;l=20517;c=173705;b=1554660;ta=2596220;cr=3961100210");
      assertEquals(
          ad.getImpressionURLs().get(1),
          "http://trk.vindicosuite.com/Tracking/V3/Instream/Impression/?0|2213|69973|66796|8759|18959|undefined|1297|3281|546|BBEEND|&iari=129381&ximpid=20517&cb=%timestamp%&internalRedirect=false");
      assertEquals(
          ad.getImpressionURLs().get(2),
          "http://b.scorecardresearch.com/p?c1=1&c2=3000027&c3=&c4=&c5=01&cA1=3&cA2=2101&cA3=173705&cA4=1554660&cA5=&cA6=1&rn=%timestamp%");
      assertEquals(
          ad.getImpressionURLs().get(3),
          "http://trk.vindicosuite.com/Tracking/V3/Instream/Impression/?adimp|2213|69973|66796|8759|18959|undefined|1297|3281|546|BBEEND|&iari=129381&cb=%timestamp%&internalRedirect=false");
      assertEquals(ad.getSequence().size(), 1);
      VASTSequenceItem item = ad.getSequence().get(0);
      assertTrue(item.getNonLinears() == null);
      assertTrue(item.getCompanions() != null);
      assertEquals(
          item.getLinear().getStream().decodedURL().toString(),
          "http://vindicoasset.edgesuite.net/Repository/CampaignCreative/Campaign_8759/INSTREAMAD/93084_scrambled_eggs_10_v2_audio_17_03_11_MP4_360p_4x3.mp4");
    } catch (Exception e) {
      System.err.println("Exception: " + e.getMessage());
      e.printStackTrace();
      fail();
    }
  }
}
