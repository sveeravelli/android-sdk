package com.ooyala.android.ads.vast;

import android.test.AndroidTestCase;

import com.ooyala.android.TestConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by zchen on 3/10/16.
 */
public class VMAPTest extends AndroidTestCase {
  public void testPreMidPost() {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputStream is = TestConstants.getTestAssetAsStream(getContext(), TestConstants.TEST_VMAP_PREMIDPOST);
      Document doc = db.parse(is);
      Element adXML = doc.getDocumentElement();
      VASTAdSpot adSpot = new VASTAdSpot(adXML);
      assertNotNull(adSpot.getVMAPAdSpots());
      assertEquals(adSpot.getVMAPAdSpots().size(), 3);
    } catch (Exception e) {
      System.err.println("Exception: " + e.getMessage());
      fail();
    }
  }
}
