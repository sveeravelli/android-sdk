package com.ooyala.android;

import android.test.AndroidTestCase;

public class EmbeddedSignatureGeneratorTest extends AndroidTestCase {
  public EmbeddedSignatureGenerator sigGen;

  public EmbeddedSignatureGeneratorTest() {
    super();
  }

  protected void setUp() {
    sigGen = new EmbeddedSignatureGenerator(TestConstants.TEST_SECRET);
  }

  protected void tearDown() {}

  public void testSign() {
    String testString = "oogabooga";
    String expectedSignature = "kxplw81748bBk41gvI17cfxHolbu3JtYNYvQHTp+9aM";
    String signature = sigGen.sign(testString);
    assertEquals(signature, expectedSignature);
  }
}
