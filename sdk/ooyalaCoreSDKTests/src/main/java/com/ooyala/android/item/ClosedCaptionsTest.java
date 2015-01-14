package com.ooyala.android.item;

import android.test.AndroidTestCase;

import com.ooyala.android.TestConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ClosedCaptionsTest extends AndroidTestCase {
  public ClosedCaptionsTest() {
    super();
  }

  @Override
  protected void setUp() {

  }

  @Override
  protected void tearDown() {

  }

  /**
   * Test the constructor. Also tests update.
   */
  public void testInitializers() {
    ClosedCaptions cc = new ClosedCaptions(
        TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_CLOSED_CAPTIONS));
    assertEquals("http://ak.c.ooyala.com/1ndnAxMzpxA4MFMw8G-F7frGiDYD_15p/cc/148094784", cc.getURL()
        .toString());
    assertEquals("", cc.getDefaultLanguage());
    assertEquals(2, cc.getLanguages().size());
  }

  /**
   * Test fetchClosedCaptionsInfo, closedCaptionsForLanguage, and getCaption. Also tests the constructor.
   */
  public void testFetchAndGet() {
    ClosedCaptions cc = new ClosedCaptions(
        TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_CLOSED_CAPTIONS));
    assertEquals("http://ak.c.ooyala.com/1ndnAxMzpxA4MFMw8G-F7frGiDYD_15p/cc/148094784", cc.getURL()
        .toString());
    assertEquals("", cc.getDefaultLanguage());
    assertEquals(2, cc.getLanguages().size());
    cc.fetchClosedCaptionsInfo();
    assertEquals(10, cc.closedCaptionsForLanguage("en").size());
    assertEquals(10, cc.closedCaptionsForLanguage("fr").size());
    Caption caption = cc.getCaption("en", 0.889);
    assertEquals(
        "two bytes meet. the first byte asks \"are you ill?\"\nthe second byte replies: \"no, just feeling a bit off.\"",
        caption.getText());
    caption = cc.getCaption("en", 19.900);
    assertEquals("alskdasdkja", caption.getText());
    caption = cc.getCaption("en", 40.300);
    assertEquals("The second string says: \"Please excuse my friend, he isnt null-terminated.\"",
        caption.getText());
    caption = cc.getCaption("fr", 0.889);
    assertEquals(
        "FR: two bytes meet. the first byte asks \"are you ill?\"\nthe second byte replies: \"no, just feeling a bit off.\"",
        caption.getText());
    caption = cc.getCaption("fr", 19.900);
    assertEquals("FR: alskdasdkja", caption.getText());
    caption = cc.getCaption("fr", 40.300);
    assertEquals("FR: The second string says: \"Please excuse my friend, he isnt null-terminated.\"",
        caption.getText());
    caption = cc.getCaption("fr", 0.800);
    assertNull(caption);
    caption = cc.getCaption("fr", 5.800);
    assertNull(caption);
    caption = cc.getCaption("fr", 32.800);
    assertNull(caption);
    caption = cc.getCaption("fr", 38.700);
    assertNull(caption);
    caption = cc.getCaption("fr", 42.00);
    assertNull(caption);
    caption = cc.getCaption("en", 0.800);
    assertNull(caption);
    caption = cc.getCaption("en", 5.800);
    assertNull(caption);
    caption = cc.getCaption("en", 32.800);
    assertNull(caption);
    caption = cc.getCaption("en", 38.700);
    assertNull(caption);
    caption = cc.getCaption("en", 42.00);
    assertNull(caption);
    caption = cc.getCaption("en", 15.084);
  }

  public void testOverlapBeginTime() {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = null;
    try {
      db = dbf.newDocumentBuilder();
    } catch (ParserConfigurationException e1) {
      e1.printStackTrace();
      assertNull(e1);
    }

    Document doc = null;
    InputStream is = TestConstants
        .getTestResourceAsStream(TestConstants.TEST_XML_CLOSED_CAPTIONS);
    try {
      doc = db.parse(is);
    } catch (SAXException e) {
      e.printStackTrace();
      assertNull(e);
    } catch (IOException e) {
      e.printStackTrace();
      assertNull(e);
    }

    Element root = doc.getDocumentElement();
    assertNotNull(root);
    String tag = root.getTagName();
    assertTrue(tag.equals(ClosedCaptions.ELEMENT_TT));
    ClosedCaptions cc = new ClosedCaptions();
    assertTrue(cc.testUpdate("en", root));
    List<Caption> cclist = cc.closedCaptionsForLanguage("en");
    assertNotNull(cclist);
    assertTrue(cclist.size() == 4);
    Caption c = cclist.get(0);
    assertTrue(c.getText().endsWith("This is the second."));
    c = cclist.get(1);
    assertTrue(c.getEnd() == ItemUtils.secondsFromTimeString("00:00:18:16"));
  }
}