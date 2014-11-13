package com.ooyala.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.nielsen.app.sdk.AppSdk;
import com.ooyala.android.ID3TagNotifier.ID3TagNotifierListener;

// general ugliness in here is forced upon us by the design of Nielsen's SDK.

public class NielsenAnalytics implements ID3TagNotifierListener {
  private static final String TAG = "NielsenAnalytics";

  /**
   * This should only be called once e.g. during app startup.
   * Should be called before NielsenAnalytics constructor.
   * @param context Android Context.
   * @param appName per Nielsen SDK docs.
   * @param appVersion per Nielsen SDK docs.
   * @param sfCode per Nielsen SDK docs.
   * @param appID per Nielsen SDK docs.
   * @see #NielsenAnalytics(String)
   */
  private static Boolean s_configured = Boolean.FALSE;
  private static AppSdk s_instance;
  public synchronized static final void s_setNielsenConfiguration( Context context, String appName, String appVersion, String sfCode, String appID ) {
    DebugMode.logV( TAG, "getMeterVersion=" + AppSdk.getMeterVersion() );
    synchronized( s_configured ) {
      if( DebugMode.assertCondition( s_configured.equals(Boolean.FALSE), TAG, "setInstanceConfiguration(): was already previously set! Ignoring call." ) ) {
        try {
          JSONObject configJson = new JSONObject();
          configJson.put( "appName", appName );
          configJson.put( "appVersion", appVersion );
          configJson.put( "sfcode", sfCode );
          configJson.put( "appId", appID );
          s_instance = AppSdk.getInstance( context, configJson.toString() );
          DebugMode.logV( TAG, "s_instance=" + s_instance + " valid=" + AppSdk.isValid() );
          s_configured = Boolean.TRUE;
        } catch (JSONException e) {
          DebugMode.logE( TAG, e.toString() );
        }
      }
    }
  }

  private int lastPlayheadMsec;
  private final AppSdk nielsenApp; // can be null, always check.
  private final String channelNameJson;

  /**
   * Should be called after s_setNielsenConfiguration().
   * @param channelName
   * @see #s_setNielsenConfiguration(Context, String, String, String, String)
   */
  public NielsenAnalytics( String channelName ) {
    this.nielsenApp = NielsenAnalytics.s_instance;
    this.channelNameJson = "{\"channelName\":\"" + channelName + "\"}";
    ID3TagNotifier.s_getInstance().addWeakListener( this );
  }

  /**
   * Effectively, a wrapper around Nielsen's static AppSdk.isValid().
   * @return
   */
  public synchronized boolean isValid() {
    return nielsenApp != null && AppSdk.isValid();
  }

  public synchronized void onMetadata( String json ) {
    if( isValid() ) {
      nielsenApp.loadMetadata( json );
    }
  }

  @Override
  public synchronized void onTag( byte[] tag ) {
    if( isValid() ) {
      final String tagStr = new String(tag);
      DebugMode.logV( TAG, "onTag(): tagStr=" + tagStr );
      if( tagStr.contains("www.nielsen.com") ) {
        final String nielsenStr = tagStr.replaceFirst( ".*www.nielsen.com", "www.nielsen.com" );
        DebugMode.logV( TAG, "onTag(): nielsenStr=" + nielsenStr );
      }
    }
  }

  public synchronized void onPlay() {
    DebugMode.logV( TAG, "onPlay()" );
    if( isValid() ) {
      nielsenApp.play( channelNameJson );
    }
  }

  public synchronized void onStop() {
    DebugMode.logV( TAG, "onStop()" );
    if( isValid() ) {
      nielsenApp.stop();
    }
  }

  public synchronized void onPlayheadUpdate( int playheadMsec ) {
    DebugMode.logV( TAG, "onPlayheadUpdate(): playheadMsec=" + playheadMsec );
    if( playheadMsec > 0 && playheadMsec - lastPlayheadMsec > 2000 ) {
      lastPlayheadMsec = playheadMsec;
      DebugMode.logV( TAG, "onPlayheadUpdate(): updating" );
      if( isValid() ) {
        nielsenApp.setPlayheadPosition( playheadMsec/1000 );
      }
    }
  }
}
