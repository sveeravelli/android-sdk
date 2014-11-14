package com.ooyala.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.nielsen.app.sdk.AppSdk;
import com.ooyala.android.ID3TagNotifier.ID3TagNotifierListener;

// general ugliness in here is forced upon us by the design of Nielsen's SDK.

public class NielsenAnalytics implements ID3TagNotifierListener {
  private static final String TAG = "NielsenAnalytics";
  private static final String UNKNOWN_CHANNEL_NAME = "unknown_not_yet_set_by_app";
  private AppSdk nielsenApp;
  private String channelName;
  private String channelNameJson;
  private int lastPlayheadMsec;

  /**
   * Convenience wrapper around Nielsen AppSdk.
   * See the Nielsen SDK documentation around AppSdk.getInstance().
   * @param context Android Context.
   * @param appName per Nielsen SDK docs.
   * @param appVersion per Nielsen SDK docs.
   * @param sfCode per Nielsen SDK docs.
   * @param appID per Nielsen SDK docs.
   * @see AppSdk
   */
  public NielsenAnalytics( Context context, String appName, String appVersion, String sfCode, String appID ) {
    JSONObject configJson = new JSONObject();
    try {
      configJson.put( "appName", appName );
      configJson.put( "appVersion", appVersion );
      configJson.put( "sfcode", sfCode );
      configJson.put( "appId", appID );
      this.nielsenApp = AppSdk.getInstance( context, configJson.toString() );
      ID3TagNotifier.s_getInstance().addWeakListener( this );
    } catch (JSONException e) {
      DebugMode.logE( TAG, e.toString() );
    }
    setChannelName( UNKNOWN_CHANNEL_NAME );
  }

  /**
   * See the Nielsen SDK documentation around AppSdk.getInstance(),
   * in particular regarding the backgrounding of the app.
   */
  public synchronized void destroy() {
    DebugMode.logV( TAG, "destroy()" );
    if( isValid() ) {
      setChannelName( UNKNOWN_CHANNEL_NAME );
      nielsenApp.suspend();
      nielsenApp = null;
    }
  }

  public void setChannelName( String channelName ) {
    DebugMode.logV( TAG, "setChannelName(): channelName=" + channelName );
    JSONObject json = new JSONObject();
    try {
      json.put( "channelName", channelName );
      this.channelNameJson = json.toString();
      this.channelName = channelName;
    } catch (JSONException e) {
      DebugMode.logE( TAG, e.toString() );
    }
  }

  public String getChannelName() {
    return channelName;
  }

  /**
   * Effectively, a wrapper around Nielsen's static AppSdk.isValid().
   * @return
   */
  public synchronized boolean isValid() {
    return nielsenApp != null && AppSdk.isValid();
  }

  public synchronized void onMetadata( String json ) {
    DebugMode.logV( TAG, "onMetadata(): json=" + json );
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
