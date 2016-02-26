package com.ooyala.android;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.util.Scanner;

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

  public static final String TEST_DICTIONARY_VIDEO = "test_video.json";
  public static final String TEST_DICTIONARY_VIDEO_WITH_AD_OOYALA = "test_video_ooyala_ad.json";
  public static final String TEST_DICTIONARY_VIDEO_AUTH_HLS = "test_video_auth_hls.json";
  public static final String TEST_DICTIONARY_VIDEO_AUTH_MP4 = "test_video_auth_mp4.json";
  public static final String TEST_DICTIONARY_VIDEO_AUTH_HLS_MP4 = "test_video_auth_hls+mp4.json";
  public static final String TEST_DICTIONARY_CHANNEL = "test_channel.json";
  public static final String TEST_DICTIONARY_CHANNEL_SET = "test_channel_set.json";
  public static final String TEST_DICTIONARY_DYNAMIC_CHANNEL = "test_dynamic_channel.json";
  public static final String TEST_DICTIONARY_AD_OOYALA = "test_ooyala_ad.json";
  public static final String TEST_DICTIONARY_AD_VAST = "test_vast_ad.json";
  public static final String TEST_DICTIONARY_AD_VAST_LR_DEVICEID = "test_vast_ad_lr_deviceid.json";
  public static final String TEST_DICTIONARY_AD_VAST_TIMESTAMP = "test_vast_ad_timestamp.json";
  public static final String TEST_DICTIONARY_STREAM_HLS = "test_stream_hls.json";
  public static final String TEST_DICTIONARY_STREAM_MP4 = "test_stream_mp4.json";
  public static final String TEST_DICTIONARY_STREAMS_MP4 = "test_streams_mp4.json";
  public static final String TEST_DICTIONARY_STREAM_WIDEVINE_MP4 = "test_stream_wv_mp4.json";
  public static final String TEST_DICTIONARY_STREAM_WIDEVINE_HLS = "test_stream_wv_hls.json";
  public static final String TEST_DICTIONARY_STREAM_WIDEVINE_WVM = "test_stream_wv_wvm.json";
  public static final String TEST_DICTIONARY_CLOSED_CAPTIONS = "test_closed_captions.json";
  public static final String TEST_XML_CLOSED_CAPTIONS = "test_closed_captions_timeline.xml";

  public static final String TEST_ANALYTICS_HTML = "test_analytics.html";
  public static final String TEST_ANALYTICS_HTML_FAIL = "test_analytics_fail.html";
  public static final String TEST_XML_VAST_AD = "test_vast_ad.xml";
  // vast3 tests
  public static final String TEST_VAST3_ICONS = "test_vast3_icons.xml";
  public static final String TEST_VAST3_PODDED = "test_vast3_podded.xml";
  public static final String TEST_VAST3_PODDED_STANDALONE = "test_vast3_podded_standalone.xml";
  public static final String TEST_VAST3_STANDALONE = "test_vast3_standalone.xml";
  public static final String TEST_VAST3_SKIPPABLE = "test_vast3_skippable.xml";

  public static InputStream getTestAssetAsStream(Context c, String name) {
      try {
          InputStream i = c.getAssets().open(name);
          return i;
      } catch (Exception e) {
          return null;
      }
  }

  public static JSONObject getTestJSON(Context c, String file) {
    InputStream inputStream = getTestAssetAsStream(c, file);
    String json = new Scanner(inputStream).useDelimiter("\\A").next();
    try {
      return (JSONObject) new JSONTokener(json).nextValue();
    } catch (JSONException exception) {
      System.out.println("JSONException: " + exception);
      return null;
    }
  }

  public static JSONArray getTestJSONArray(Context c, String file) {
        InputStream inputStream = getTestAssetAsStream(c, file);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        try {
            return new JSONArray(json);
        } catch (JSONException exception) {
            System.out.println("JSONException: " + exception);
            return null;
        }
    }
}
