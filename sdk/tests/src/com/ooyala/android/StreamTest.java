package com.ooyala.android;

import android.test.AndroidTestCase;

public class StreamTest extends AndroidTestCase
{
  public StreamTest()
  {
    super();
  }

  protected void setUp()
  {

  }

  protected void tearDown()
  {

  }

  public void testInitializers()
  {
    Stream stream = new Stream(ContentItemTest.getTestJSON(TestConstants.TEST_DICTIONARY_STREAM_HLS));
    assertEquals(stream.getDeliveryType(), Constants.DELIVERY_TYPE_HLS);
  }

  public void testBestStreamFromArray()
  {

  }
}
