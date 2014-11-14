package com.ooyala.test;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Application;
import android.os.StrictMode;

import com.ooyala.test.cases.CustomEmbedInternalTestAppActivity;
import com.ooyala.test.cases.FreewheelInternalTestAppActivity;
import com.ooyala.test.cases.IMAInternalTestAppActivity;
import com.ooyala.test.cases.OoyalaAdsInternalTestAppActivity;
import com.ooyala.test.cases.PlaybackInternalTestAppActivity;

/**
 * A global scope of the application.  This allows all activities to use the same list of information.
 * Using this, we can add more test cases by doing the following:
 *
 * 1. Create the test activity in com.ooyala.test.cases
 * 2. Create the string for the activity's name in res/strings
 * 3. Add that activity to these maps, with proper information
 *
 */
public class InternalTestApplication extends Application {

  private static Map<String, Class<? extends Activity>> activityMap;
  private static Map<String, String> condensedActivityNameMap;

  public void onCreate() {

    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
    .detectDiskReads()
    .detectDiskWrites()
    .detectNetwork()   // or .detectAll() for all detectable problems
    .penaltyLog()
    .build());

    super.onCreate();
    activityMap = new HashMap<String, Class<? extends Activity>>();
    condensedActivityNameMap = new HashMap<String, String>();

    activityMap.put(getString(R.string.coreItemName), PlaybackInternalTestAppActivity.class);
    condensedActivityNameMap.put(getString(R.string.coreItemName), "Core");

    activityMap.put(getString(R.string.imaItemName), IMAInternalTestAppActivity.class);
    condensedActivityNameMap.put(getString(R.string.imaItemName), "IMA");

    activityMap.put(getString(R.string.ooyalaItemName), OoyalaAdsInternalTestAppActivity.class);
    condensedActivityNameMap.put(getString(R.string.ooyalaItemName), "OO Ads");

    activityMap.put(getString(R.string.freewheelItemName), FreewheelInternalTestAppActivity.class);
    condensedActivityNameMap.put(getString(R.string.freewheelItemName), "Freewheel");

    activityMap.put(getString(R.string.customItemName), CustomEmbedInternalTestAppActivity.class);
    condensedActivityNameMap.put(getString(R.string.customItemName), "Custom Embed");
  }

  public static Map<String, Class<? extends Activity>> getActivityMap() {
    return activityMap;
  }

  public static Map<String, String> getCondensedActivityNameMap() {
    return condensedActivityNameMap;
  }
}
