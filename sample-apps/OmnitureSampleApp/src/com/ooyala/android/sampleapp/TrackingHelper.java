package com.ooyala.android.sampleapp;

import java.util.Hashtable;

import android.app.Activity;

import com.adobe.adms.measurement.ADMS_Measurement;
import com.adobe.adms.mediameasurement.ADMS_MediaMeasurement;

public class TrackingHelper {

  //private static final String TRACKING_RSID = "YOUR_REPORTSUITEID";
  //private static final String TRACKING_SERVER = "YOUR_TRACKINGSERVER";
  private static final String TRACKING_RSID = "123";
  private static final String TRACKING_SERVER = "172.16.100.241:57365";

  public static void startActivity(Activity activity) {
    ADMS_Measurement measurement = ADMS_Measurement.sharedInstance(activity);
    measurement.startActivity(activity);
  }

  public static void stopActivity() {
    ADMS_Measurement measurement = ADMS_Measurement.sharedInstance();
    measurement.stopActivity();
  }

  public static void configureAppMeasurement(Activity activity) {
    ADMS_Measurement measurement = ADMS_Measurement.sharedInstance(activity);
    measurement.configureMeasurement(TRACKING_RSID, TRACKING_SERVER);

    measurement.setSSL(false);
    measurement.setDebugLogging(true);
  }

  // Examples of Custom Event and AppState Tracking
  public static void trackCustomEvents (String events) {
    Hashtable<String, Object> contextData = new Hashtable<String, Object>();
    contextData.put("contextKey", "value");

    ADMS_Measurement measurement = ADMS_Measurement.sharedInstance();
    measurement.trackEvents(events, contextData);
  }

  public static void trackCustomAppState (String appState) {
    Hashtable<String, Object> contextData = new Hashtable<String, Object>();
    contextData.put("contextKey", "value");

    ADMS_Measurement measurement = ADMS_Measurement.sharedInstance();
    measurement.trackAppState(appState, contextData);
  }

  public static void trackLogonEvent (String username_value) {
    Hashtable<String, Object> contextData = new Hashtable<String, Object>();
    contextData.put("username", username_value);

    ADMS_Measurement measurement = ADMS_Measurement.sharedInstance();
    measurement.trackEvents("event1", contextData);
  }

  public static void configureMediaMeasurement() {
    ADMS_MediaMeasurement mediaMeasurement = ADMS_MediaMeasurement.sharedInstance();
    Hashtable<String, Object> contextDataMapping = new Hashtable<String, Object>();
    contextDataMapping.put("a.media.name", "eVar2,prop2");
    contextDataMapping.put("a.media.segment", "eVar3");
    contextDataMapping.put("a.contentType", "eVar1"); //note that this is not in the .media namespace
    contextDataMapping.put("a.media.timePlayed", "event3");
    contextDataMapping.put("a.media.view", "event1");
    contextDataMapping.put("a.media.segmentView", "event2");
    contextDataMapping.put("a.media.complete", "event7");

    mediaMeasurement.contextDataMapping = contextDataMapping;
  }

  public static void open(String mediaName, double mediaLength, String playerName) {
    ADMS_MediaMeasurement mediaMeasurement = ADMS_MediaMeasurement.sharedInstance();
    mediaMeasurement.open(mediaName, mediaLength, playerName);
  }

  public static void play(String mediaName, double offset) {
    ADMS_MediaMeasurement mediaMeasurement = ADMS_MediaMeasurement.sharedInstance();
    mediaMeasurement.play(mediaName, offset);
  }

  public static void stop(String mediaName, double offset) {
    ADMS_MediaMeasurement mediaMeasurement = ADMS_MediaMeasurement.sharedInstance();
    mediaMeasurement.stop(mediaName, offset);
  }

  public static void close(String mediaName) {
    ADMS_MediaMeasurement mediaMeasurement = ADMS_MediaMeasurement.sharedInstance();
    mediaMeasurement.close(mediaName);
  }
}
