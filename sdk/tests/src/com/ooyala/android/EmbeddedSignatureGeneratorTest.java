package com.ooyala.android;

import android.test.AndroidTestCase;

public class EmbeddedSignatureGeneratorTest extends AndroidTestCase {
  public EmbeddedSignatureGenerator sigGen;

  public EmbeddedSignatureGeneratorTest() {
    super();
    sigGen = new EmbeddedSignatureGenerator(TestConstants.TEST_SECRET);
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }

  public void testSign() {
    String testString = "oogabooga";
    String expectedSignature = "Sd7Y1rr2Jx7bONdTFCke2ykX4dP7JVSQC/N4TrcAFbk";
    String signature = sigGen.sign(testString);
    assertEquals(signature, expectedSignature);
  }
}
