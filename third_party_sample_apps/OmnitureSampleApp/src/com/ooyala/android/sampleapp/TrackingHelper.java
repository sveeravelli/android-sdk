//
//  TrackingHelper.java
//  OmnitureSampleApp
//
//  A helper class for OmnitureSampleAppActivity
//

package com.ooyala.android.sampleapp;

import java.util.Hashtable;

import android.app.Activity;

import com.adobe.adms.measurement.ADMS_Measurement;
import com.adobe.adms.mediameasurement.ADMS_MediaMeasurement;

public class TrackingHelper {

  // Use your reportSuiteID and Tracking Server here
  private static final String TRACKING_RSID = "YOUR_REPORTSUITEID";
  private static final String TRACKING_SERVER = "YOUR_TRACKING_SERVER";

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

  public static void configureMediaMeasurement() {
    ADMS_MediaMeasurement mediaMeasurement = ADMS_MediaMeasurement.sharedInstance();
    Hashtable<String, Object> contextDataMapping = new Hashtable<String, Object>();

    // Put the Config variables that were set up in SiteCatalyst here. evar, prop and events
    contextDataMapping.put("a.media.name", "eVar29,prop29");
    contextDataMapping.put("a.media.segment", "eVar55");
    contextDataMapping.put("a.contentType", "eVar5"); //note that this is not in the .media namespace
    contextDataMapping.put("a.media.timePlayed", "event26");
    contextDataMapping.put("a.media.view", "event8");
    contextDataMapping.put("a.media.segmentView", "event25");
    contextDataMapping.put("a.media.complete", "event12");

    mediaMeasurement.contextDataMapping = contextDataMapping;

    // track Milestones & segmentByMilestones:
    mediaMeasurement.trackMilestones = "25,50,75";
    mediaMeasurement.segmentByMilestones = true;
  }

  public static void open(String mediaName, double mediaLength, String mediaPlayerName) {
    ADMS_MediaMeasurement mediaMeasurement = ADMS_MediaMeasurement.sharedInstance();
    mediaMeasurement.open(mediaName, mediaLength, mediaPlayerName);
  }

  public static void play(String mediaName, double mediaOffset) {
    ADMS_MediaMeasurement mediaMeasurement = ADMS_MediaMeasurement.sharedInstance();
    mediaMeasurement.play(mediaName, mediaOffset);
  }

  public static void stop(String mediaName, double mediaOffset) {
    ADMS_MediaMeasurement mediaMeasurement = ADMS_MediaMeasurement.sharedInstance();
    mediaMeasurement.stop(mediaName, mediaOffset);
  }

  public static void close(String mediaName) {
    ADMS_MediaMeasurement mediaMeasurement = ADMS_MediaMeasurement.sharedInstance();
    mediaMeasurement.close(mediaName);
  }
}
