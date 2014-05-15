package com.ooyala.android.ads.vast;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ooyala.android.TestConstants;

import android.test.AndroidTestCase;

public class VASTSequenceItemTest extends AndroidTestCase {
  public VASTSequenceItemTest() {
    super();
  }

  protected void setUp() {}

  protected void tearDown() {}

  public void testHasLinear() {
    VASTSequenceItem item = new VASTSequenceItem();
    assertFalse(item.hasLinear());
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
      item.setLinear(new VASTLinearAd(linearXML));
    } catch (Exception e) {
      System.err.println("Exception: " + e.getMessage());
      e.printStackTrace();
      fail();
    }
    assertTrue(item.hasLinear());
  }
}
