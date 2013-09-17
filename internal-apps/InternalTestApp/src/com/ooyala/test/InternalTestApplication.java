package com.ooyala.test;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Application;

/**
 * A global scope of the application.  This allows all activities to use the same list of information.
 * Using this, we can add more test cases by doing the following:
 *
 * 1. Create the test activity
 * 2. Create the string for the activity's name
 * 3. Add that activity to these maps, with proper information
 * @author michael.len
 *
 */
public class InternalTestApplication extends Application {

  private static Map<String, Class<? extends Activity>> activityMap;
  private static Map<String, String> condensedActivityNameMap;

  public void onCreate() {
    super.onCreate();
    activityMap = new HashMap<String, Class<? extends Activity>>();
    condensedActivityNameMap = new HashMap<String, String>();

    activityMap.put(getString(R.string.coreItemName), PlaybackInternalTestAppActivity.class);
    condensedActivityNameMap.put(getString(R.string.coreItemName), "core");

    activityMap.put(getString(R.string.imaItemName), IMAInternalTestAppActivity.class);
    condensedActivityNameMap.put(getString(R.string.imaItemName), "IMA");

    activityMap.put(getString(R.string.ooyalaItemName), OoyalaAdsInternalTestAppActivity.class);
    condensedActivityNameMap.put(getString(R.string.ooyalaItemName), "OO Ads");
  }

  public static Map<String, Class<? extends Activity>> getActivityMap() {
    return activityMap;
  }

  public static Map<String, String> getCondensedActivityNameMap() {
    return condensedActivityNameMap;
  }
}
