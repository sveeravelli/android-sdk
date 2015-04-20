package com.ooyala.android.item;

import android.test.AndroidTestCase;

import com.ooyala.android.StreamSelector;
import com.ooyala.android.TestConstants;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

public class StreamTest extends AndroidTestCase {
  public StreamTest() {
    super();
  }

  @Override
  protected void setUp() {

  }

  @Override
  protected void tearDown() {

  }

  /**
   * Tests Stream constructor, update, and combinedBitrate
   */
  public void testInitializers() {
    Stream stream = new Stream(TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_STREAM_HLS));
    assertEquals(Stream.DELIVERY_TYPE_HLS, stream.getDeliveryType());
    assertNull(stream.getVideoCodec());
    assertEquals(Stream.STREAM_URL_FORMAT_B64, stream.getUrlFormat());
    assertNull(stream.getFramerate());
    assertEquals("http://player.ooyala.com/player/iphone/UwN2wxMzpU1Nl_qojlX8iLlKEHfl4HLM.m3u8", stream
        .decodedURL().toString());

    stream = new Stream(TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_STREAM_MP4));
    assertEquals(Stream.DELIVERY_TYPE_MP4, stream.getDeliveryType());
    assertEquals("h264", stream.getVideoCodec());
    assertEquals("text", stream.getUrlFormat());
    assertEquals("30.0", stream.getFramerate());
    assertEquals("http://ak.c.ooyala.com/UwN2wxMzpU1Nl_qojlX8iLlKEHfl4HLM/DOcJ-FxaFrRg4gtGEwOjEzYzowODE7Z_",
        stream.decodedURL().toString());
    assertEquals(1200, stream.getVideoBitrate());
    assertEquals(128, stream.getAudioBitrate());
    assertEquals(1328, stream.getCombinedBitrate());
    assertEquals(720, stream.getHeight());
    assertEquals(1280, stream.getWidth());
  }

  /**
   * Tests bestStream and decodeURL, as well as the constructor as a side effect.
   */
  public void testBestStreamFromArray() {
    JSONArray streamsData = TestConstants.getTestJSONArray(getContext(), TestConstants.TEST_DICTIONARY_STREAMS_MP4);
    Set<Stream> mp4s = new HashSet<Stream>();
    if (streamsData.length() > 0) {
      for (int i = 0; i < streamsData.length(); i++) {
        try {
          Stream stream = new Stream(streamsData.getJSONObject(i));
          if (stream != null) {
            mp4s.add(stream);
          }
        } catch (JSONException e) {
          fail("JSONException: " + e);
        }
      }
    }

    assertEquals("http://ak.c.ooyala.com/UwN2wxMzpU1Nl_qojlX8iLlKEHfl4HLM/DOcJ-FxaFrRg4gtGEwOmk2OjBrO5dC5F",
        Stream.bestStream(mp4s, false).decodedURL().toString());
    mp4s.remove(Stream.bestStream(mp4s, false));
    assertEquals("http://ak.c.ooyala.com/UwN2wxMzpU1Nl_qojlX8iLlKEHfl4HLM/DOcJ-FxaFrRg4gtGEwOmk2OjA4MTvK-J",
        Stream.bestStream(mp4s, false).decodedURL().toString());

    Stream hlsStream = new Stream(TestConstants.getTestJSON(getContext(), TestConstants.TEST_DICTIONARY_STREAM_HLS));
    Set<Stream> hls = new HashSet<Stream>();
    hls.add(hlsStream);
    assertEquals(Stream.bestStream(hls, false), hlsStream);

    Stream.setStreamSelector(new StreamSelector() {
      @Override
      public Stream bestStream(Set<Stream> streams, boolean isWifiEnabled) {
        return null;
      }
    });
    assertNull(Stream.bestStream(mp4s, false));
    Stream.resetStreamSelector();
  }
}
