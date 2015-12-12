package com.ooyala.android;

import android.test.AndroidTestCase;

import com.ooyala.android.discovery.DiscoveryManager;
import com.ooyala.android.discovery.DiscoveryOptions;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by zchen on 12/11/15.
 */
public class DiscoveryTest  extends AndroidTestCase {
  private String deviceId;
  private String embedCode;
  private String pcode;
  private String bucketInfo;

  public DiscoveryTest() {
    super();
  }

  @Override
  protected void setUp() {
    deviceId = ClientId.getId(getContext());
    embedCode = "Y1ZHB1ZDqfhCPjYYRbCEOz0GR8IsVRm1";
    pcode = "c0cTkxOqALQviQIGAHWY5hP0q9gU";
    bucketInfo = "1{\"encoded\":\"eNpNj9EOgyAMRf+lz2QBBMf4GUOUOBIFAp2Jcf776uayvfWe3ra3G2RXfMRu\\nyV0YwAJ/amWEAgZ+OTiu2YMVDLIvPQE3HvLCGbhpTCXgfe6imwkC0qKhwn+H\\nlru5gt1gLC4+Jkd4JWtDLgzvKapqn4o/LwGmTIgGMWBIsR6WnUHxfTeFiiSF\\n1q2QwnySWsmN1Ko9g1uhubry5ieVvAnzfWt/AUq0Sc4=\",\"position\":0}";
  }

  @Override
  protected void tearDown() {

  }

  public void testGetDiscoveryResultsMomentum() {
    final CountDownLatch signal = new CountDownLatch(1);
    DiscoveryManager.Callback callback = new DiscoveryManager.Callback() {
      @Override
      public void callback(JSONObject results, OoyalaException error) {
        assertTrue(results != null);
        signal.countDown();
      }
    };

    DiscoveryOptions options =
        new DiscoveryOptions.Builder().setType(DiscoveryOptions.Type.Momentum).build();
    DiscoveryManager.getResults(options, embedCode, pcode, deviceId, null, callback);
    try {
      signal.await(20, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      assertTrue("timed out", false);
    }
  }

  public void testGetDiscoveryResultsSimilarAsset() {
    final CountDownLatch signal = new CountDownLatch(1);
    DiscoveryManager.Callback callback = new DiscoveryManager.Callback() {
      @Override
      public void callback(JSONObject results, OoyalaException error) {
        assertTrue(results != null);
        signal.countDown();
      }
    };

    DiscoveryOptions options =
        new DiscoveryOptions.Builder().setType(DiscoveryOptions.Type.Popular).build();
    DiscoveryManager.getResults(options, embedCode, pcode, deviceId, null, callback);
    try {
      signal.await(20, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      assertTrue("timed out", false);
    }
  }

  public void testGetDiscoveryResultsPopular() {
    final CountDownLatch signal = new CountDownLatch(1);
    DiscoveryManager.Callback callback = new DiscoveryManager.Callback() {
      @Override
      public void callback(JSONObject results, OoyalaException error) {
        assertTrue(results != null);
        signal.countDown();
      }
    };

    DiscoveryOptions options =
        new DiscoveryOptions.Builder().setType(DiscoveryOptions.Type.SimilarAssets).build();
    DiscoveryManager.getResults(options, embedCode, pcode, deviceId, null, callback);
    try {
      signal.await(20, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      assertTrue("timed out", false);
    }
  }

  public void testGetDiscoveryResultsPostImpression() {
    final CountDownLatch signal = new CountDownLatch(1);
    DiscoveryManager.Callback callback = new DiscoveryManager.Callback() {
      @Override
      public void callback(JSONObject results, OoyalaException error) {
        assertNotNull(results);
        signal.countDown();
      }
    };

    DiscoveryOptions options =
        new DiscoveryOptions.Builder().setType(DiscoveryOptions.Type.SimilarAssets).build();
    DiscoveryManager.sendImpression(options, bucketInfo, pcode, deviceId, null, callback);
    try {
      signal.await(20, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      assertTrue("timed out", false);
    }
  }

  public void testGetDiscoveryResultsPostClick() {
    final CountDownLatch signal = new CountDownLatch(1);
    DiscoveryManager.Callback callback = new DiscoveryManager.Callback() {
      @Override
      public void callback(JSONObject results, OoyalaException error) {
        assertTrue(results != null);
        signal.countDown();
      }
    };

    DiscoveryOptions options =
        new DiscoveryOptions.Builder().setType(DiscoveryOptions.Type.SimilarAssets).build();
    DiscoveryManager.sendClick(options, bucketInfo, pcode, deviceId, null, callback);
    try {
      signal.await(20, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      assertTrue("timed out", false);
    }
  }
}
