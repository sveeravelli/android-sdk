package com.ooyala.android.item;

import com.ooyala.android.TestConstants;

import android.test.AndroidTestCase;

public class ClosedCaptionsTest extends AndroidTestCase {
  public ClosedCaptionsTest() {
    super();
  }

  protected void setUp() {

  }

  protected void tearDown() {

  }

  /**
   * Test the constructor. Also tests update.
   */
  public void testInitializers() {
    ClosedCaptions cc = new ClosedCaptions(
        ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CLOSED_CAPTIONS));
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
        ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_CLOSED_CAPTIONS));
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
}