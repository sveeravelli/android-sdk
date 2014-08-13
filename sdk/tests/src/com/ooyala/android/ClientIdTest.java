package com.ooyala.android;

import java.util.UUID;

import android.test.AndroidTestCase;

public class ClientIdTest extends AndroidTestCase {
  public ClientIdTest() {
    super();
  }

  @Override
  protected void setUp() {

  }

  @Override
  protected void tearDown() {

  }
  
  public void testGetId() {
    String uuid = ClientId.getId(getContext());
    assertTrue(uuid != null);
    String uuid2 = ClientId.getId(getContext());
    assertTrue(uuid == uuid2);
  }

  public void testSetId() {
    String expectedId = UUID.randomUUID().toString();
    ClientId.setId(expectedId);
    assertEquals(expectedId, ClientId.getId(getContext()));
  }
  
  public void testResetId() {
    String expectedId = ClientId.getId(getContext());
    ClientId.resetId(getContext());
    assertFalse(expectedId.equals(ClientId.getId(getContext())));
  }

}
