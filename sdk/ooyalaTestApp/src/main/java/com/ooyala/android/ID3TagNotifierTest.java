package com.ooyala.android;

import android.test.AndroidTestCase;

import com.google.android.exoplayer.metadata.GeobMetadata;
import com.google.android.exoplayer.metadata.PrivMetadata;
import com.google.android.exoplayer.metadata.TxxxMetadata;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zchen on 2/5/16.
 */
public class ID3TagNotifierTest extends AndroidTestCase {
  private final String testString = "testingId3tag";
  private final String testString2 = "testingId3tag2";
  private final String testString3 = "testingId3tag3";

  @Override
  protected void setUp() {
  }

  @Override
  protected void tearDown() {

  }

  public void testPrivateTag() {
    final AtomicBoolean seen = new AtomicBoolean();
    final PrivMetadata metadata = new PrivMetadata(testString, null);
    final ID3TagNotifier.ID3TagNotifierListener l = new ID3TagNotifier.ID3TagNotifierListener() {
      @Override
      public void onTag( byte[] tag ) {
        seen.set(false);
      }

      @Override
      public void onPrivateMetadata(final String owner, final byte[] privateMetadata) {
        assertEquals(owner, testString);
        seen.set(true);
      }

      @Override
      public void onTxxxMetadata(final String description, final String value) {
        seen.set(false);
      }

      @Override
      public void onGeobMetadata(final String mimeType, final String filename, final String description, final byte[] data) {
       seen.set(false);
      }

    };
    final ID3TagNotifier q = new ID3TagNotifier();
    q.addWeakListener( l );
    q.onPrivateMetadata(metadata.owner, metadata.privateData);
    assertTrue( seen.get() );
  }

  public void testTxxxTag() {
    final AtomicBoolean seen = new AtomicBoolean();
    final TxxxMetadata metadata = new TxxxMetadata(testString, testString2);
    final ID3TagNotifier.ID3TagNotifierListener l = new ID3TagNotifier.ID3TagNotifierListener() {
      @Override
      public void onTag( byte[] tag ) {
        seen.set(false);
      }

      @Override
      public void onPrivateMetadata(final String owner, final byte[] privateMetadata) {
        seen.set(false);
      }

      @Override
      public void onTxxxMetadata(final String description, final String value) {
        assertEquals(description, testString);
        assertEquals(value, testString2);
        seen.set(true);
      }

      @Override
      public void onGeobMetadata(final String mimeType, final String filename, final String description, final byte[] data) {
        seen.set(false);
      }

    };
    final ID3TagNotifier q = new ID3TagNotifier();
    q.addWeakListener( l );
    q.onTxxxMetadata(metadata.description, metadata.value);
    assertTrue(seen.get());
  }

  public void testPGeobTag() {
    final AtomicBoolean seen = new AtomicBoolean();
    final GeobMetadata metadata = new GeobMetadata(testString, testString2, testString3, null);
    final ID3TagNotifier.ID3TagNotifierListener l = new ID3TagNotifier.ID3TagNotifierListener() {
      @Override
      public void onTag( byte[] tag ) {
        seen.set(false);
      }

      @Override
      public void onPrivateMetadata(final String owner, final byte[] privateMetadata) {
        seen.set(false);
      }

      @Override
      public void onTxxxMetadata(final String description, final String value) {
        seen.set(false);
      }

      @Override
      public void onGeobMetadata(final String mimeType, final String filename, final String description, final byte[] data) {
        assertEquals(mimeType, testString);
        assertEquals(filename, testString2);
        assertEquals(description, testString3);
        seen.set(true);
      }

    };
    final ID3TagNotifier q = new ID3TagNotifier();
    q.addWeakListener( l );
    q.onGeobMetadata(metadata.mimeType, metadata.filename, metadata.description, null);
    assertTrue( seen.get() );
  }

  public void testDefaultTag() {
    final AtomicBoolean seen = new AtomicBoolean();
    byte[] bytes = testString.getBytes();
    final ID3TagNotifier.ID3TagNotifierListener l = new ID3TagNotifier.ID3TagNotifierListener() {
      @Override
      public void onTag( byte[] tag ) {
        String s = new String(tag);
        assertEquals(s, testString);
        seen.set(true);
      }

      @Override
      public void onPrivateMetadata(final String owner, final byte[] privateMetadata) {
        seen.set(false);
      }

      @Override
      public void onTxxxMetadata(final String description, final String value) {
        seen.set(false);
      }

      @Override
      public void onGeobMetadata(final String mimeType, final String filename, final String description, final byte[] data) {
        seen.set(false);
      }

    };
    final ID3TagNotifier q = new ID3TagNotifier();
    q.addWeakListener( l );
    q.onTag(bytes);
    assertTrue( seen.get() );
  }
}
