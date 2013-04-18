package com.ooyala.android.sampleapp;

import java.util.Hashtable;

import android.app.Activity;

import com.adobe.adms.measurement.ADMS_Measurement;

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
	

}
