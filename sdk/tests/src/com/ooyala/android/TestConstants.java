package com.ooyala.android;

import java.io.InputStream;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class TestConstants {
  public static final String TEST_API_KEY = "l1am06xhbSxa0OtyZsBTshW2DMtp.6Le2A";
  public static final String TEST_SECRET = "M1kKrxsGenjOJ--O2dDh8_XQ5C4nB3NeX6aFS4U4";
  public static final String TEST_PCODE = "l1am06xhbSxa0OtyZsBTshW2DMtp";
  public static final String TEST_VIDEO = "UwN2wxMzpU1Nl_qojlX8iLlKEHfl4HLM";
  public static final String TEST_VIDEO_EXTERNAL_ID = "jigish:4570720";
  public static final String TEST_VIDEO_WITH_AD_OOYALA = "g3N2wxMzqxoB84c3dan5xyXTxdrhX1km";
  public static final String TEST_VIDEO_WITH_AD_OOYALA_EXTERNAL_ID = "jigish:4570727";
  public static final String TEST_VIDEO_WITH_AD_VAST = "w2cXAxMzqpwY5HwqSbHMzYgu92Lj6Fer";
  public static final String TEST_VIDEO_WITH_CC = "1ndnAxMzpxA4MFMw8G-F7frGiDYD_15p";
  public static final String TEST_REMOTE_ASSET = "h5bWszMzq-3eGIYojdbUW11AXSyRDNhl";
  public static final String TEST_LIVE_STREAM = "A4cGszMzqimHtUUUfh3wNaJng_qG1gBr";
  public static final String TEST_CHANNEL = "B0eHAxMzqsbVRm0ZJROXw1Yaj73roQu6";
  public static final String TEST_CHANNEL_SET = "N1ZmszMzoWGX7wpenTZoWEpfjV5RMQQc";
  public static final String TEST_CONTENT_ID = "1-UwN2wxMzpU1Nl_qojlX8iLlKEHfl4HLM";
  public static final String TEST_DOMAIN = "http://www.ooyala.com";
  public static final String TEST_AD_OOYALA = "JzdHAxMzoJXCByNhz6UQrL5GjIiUrr_B";

  public static final String TEST_DICTIONARY_VIDEO = "resources/test_video.json";
  public static final String TEST_DICTIONARY_VIDEO_WITH_AD_OOYALA = "resources/test_video_ooyala_ad.json";
  public static final String TEST_DICTIONARY_VIDEO_AUTH_HLS = "resources/test_video_auth_hls.json";
  public static final String TEST_DICTIONARY_VIDEO_AUTH_MP4 = "resources/test_video_auth_mp4.json";
  public static final String TEST_DICTIONARY_VIDEO_AUTH_HLS_MP4 = "resources/test_video_auth_hls+mp4.json";
  public static final String TEST_DICTIONARY_CHANNEL = "resources/test_channel.json";
  public static final String TEST_DICTIONARY_CHANNEL_SET = "resources/test_channel_set.json";
  public static final String TEST_DICTIONARY_DYNAMIC_CHANNEL = "resources/test_dynamic_channel.json";
  public static final String TEST_DICTIONARY_AD_OOYALA = "resources/test_ooyala_ad.json";
  public static final String TEST_DICTIONARY_AD_VAST = "resources/test_vast_ad.json";
  public static final String TEST_DICTIONARY_AD_VAST_LR_DEVICEID = "resources/test_vast_ad_lr_deviceid.json";
  public static final String TEST_DICTIONARY_AD_VAST_TIMESTAMP = "resources/test_vast_ad_timestamp.json";
  public static final String TEST_DICTIONARY_STREAM_HLS = "resources/test_stream_hls.json";
  public static final String TEST_DICTIONARY_STREAM_MP4 = "resources/test_stream_mp4.json";
  public static final String TEST_DICTIONARY_STREAMS_MP4 = "resources/test_streams_mp4.json";
  public static final String TEST_DICTIONARY_CLOSED_CAPTIONS = "resources/test_closed_captions.json";

  public static final String TEST_ANALYTICS_HTML = "resources/test_analytics.html";
  public static final String TEST_ANALYTICS_HTML_FAIL = "resources/test_analytics_fail.html";

  public static final String TEST_XML_VAST_AD = "resources/test_vast_ad.xml";

  public static InputStream getTestResourceAsStream(String res) {
    return TestConstants.class.getResourceAsStream(res);
  }

  public static JSONObject getTestJSON(String file) {
    InputStream inputStream = getTestResourceAsStream(file);
    String json = new Scanner(inputStream).useDelimiter("\\A").next();
    try {
      return (JSONObject) new JSONTokener(json).nextValue();
    } catch (JSONException exception) {
      System.out.println("JSONException: " + exception);
      return null;
    }
  }

  public static JSONArray getTestJSONArray(String file) {
    InputStream inputStream = getTestResourceAsStream(file);
    String json = new Scanner(inputStream).useDelimiter("\\A").next();
    try {
      return new JSONArray(json);
    } catch (JSONException exception) {
      System.out.println("JSONException: " + exception);
      return null;
    }
  }
}
