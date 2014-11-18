package com.ooyala.android.nielsensdk;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.nielsen.app.sdk.AppSdk;
import com.nielsen.app.sdk.IAppNotifier;
import com.ooyala.android.AnalyticsPluginInterface;
import com.ooyala.android.DebugMode;
import com.ooyala.android.ID3TagNotifier;
import com.ooyala.android.ID3TagNotifier.ID3TagNotifierListener;

// general ugliness in here is forced upon us by the design of Nielsen's SDK.

public class NielsenAnalytics implements ID3TagNotifierListener, AnalyticsPluginInterface, IAppNotifier {
  private static final String TAG = "NielsenAnalytics";
  private static final String UNKNOWN_CHANNEL_NAME = "unknown_not_yet_set_by_app";

  private AppSdk nielsenApp;
  private String clientID;
  private String vcID;
  private String pd;
  private ID3TagNotifier id3TagNotifier;
  private String channelName;
  private String channelNameJson;
  private int lastPlayheadMsec;

  /**
   * Convenience wrapper around Nielsen AppSdk.
   * See the Nielsen SDK documentation around AppSdk.getInstance().
   * @param context Android Context. Not null.
   * @param appName per Nielsen SDK docs. Not null.
   * @param appVersion per Nielsen SDK docs. Not null.
   * @param sfCode per Nielsen SDK docs. Not null.
   * @param appID per Nielsen SDK docs. Not null.
   * @param dma per Nielsen SDK docs. Optional, can be null to omit it.
   * @param ccode per Nielsen SDK docs. Optional, can be null to omit it.
   * @param clientID per Nielsen SDK docs. Not null.
   * @param vcID per Nielsen SDK docs. Not null.
   * @param pd per Nielsen SDK docs. Not null.
   * @see AppSdk
   */
  public NielsenAnalytics( Context context, String appName, String appVersion, String sfCode, String appID, String dma, String ccode, String clientID, String vcID, String pd, ID3TagNotifier id3TagNotifier ) {
    JSONObject configJson = new JSONObject();
    try {
      configJson.put( "appName", appName );
      configJson.put( "appVersion", appVersion );
      configJson.put( "sfcode", sfCode );
      configJson.put( "appId", appID );
      if( dma != null ) { configJson.put( "dma", dma ); }
      if( ccode != null ) { configJson.put( "ccode", ccode ); }
      this.clientID = clientID;
      this.vcID = vcID;
      this.pd = pd;
      this.nielsenApp = AppSdk.getInstance( context, configJson.toString() );
      this.id3TagNotifier = id3TagNotifier;
      this.lastPlayheadMsec = Integer.MIN_VALUE;
      id3TagNotifier.addWeakListener( this );
    } catch (JSONException e) {
      DebugMode.logE( TAG, e.toString() );
    }
    setChannelName( UNKNOWN_CHANNEL_NAME );
  }

  public String buildMetadataJson( String assetID, int lengthSec, String type, String category, String ocrTag, boolean tv, String prod, String tfID, String sID ) {
    JSONObject json = new JSONObject();
    try {
      json.put( "assetid", assetID );
      json.put( "clientid", this.clientID );
      json.put( "vcid", this.vcID );
      json.put( "length", String.valueOf(lengthSec) );
      json.put( "type", type );
      json.put( "category", category );
      json.put( "ocrtag", ocrTag );
      json.put( "tv", tv );
      json.put( "prod", prod );
      json.put( "pd", this.pd );
      json.put( "tfid", tfID );
      json.put( "sid", sID );
    } catch (JSONException e) {
      DebugMode.logE( TAG, "buildMetadataJson(): " + e.toString() );
    }
    return json.toString();
  }

  /**
   * Provides the AppSdk reference for use cases that aren't covered by this Class's interface.
   * @return our cached AppSdk ref, originally obtained by calling AppSdk.getInstance() in our constructor.
   * @see AppSdk#getInstance(Context, String)
   */
  public AppSdk getNielsenAppSdk() {
    return this.nielsenApp;
  }

  /* (non-Javadoc)
   * @see com.ooyala.android.AnalyticsPluginInterface#destroy()
   */
  @Override
  public synchronized void destroy() {
    DebugMode.logV( TAG, "destroy()" );
    if( isValid() ) {
      id3TagNotifier.removeWeakListener( this );
      setChannelName( UNKNOWN_CHANNEL_NAME );
      nielsenApp.suspend();
      nielsenApp = null;
    }
  }

  /* (non-Javadoc)
   * @see com.ooyala.android.AnalyticsPluginInterface#setChannelName(java.lang.String)
   */
  @Override
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
   */
  public synchronized boolean isValid() {
    return nielsenApp != null && AppSdk.isValid();
  }

  /* (non-Javadoc)
   * @see com.ooyala.android.AnalyticsPluginInterface#onMetadata(java.lang.String)
   */
  @Override
  public synchronized void onMetadata( String json ) {
    DebugMode.logV( TAG, "onMetadata(): json=" + json );
    if( isValid() ) {
      nielsenApp.loadMetadata( json );
    }
  }

  /* (non-Javadoc)
   * @see com.ooyala.android.AnalyticsPluginInterface#onTag(byte[])
   */
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

  /* (non-Javadoc)
   * @see com.ooyala.android.AnalyticsPluginInterface#onPlay()
   */
  @Override
  public synchronized void onPlay() {
    DebugMode.logV( TAG, "onPlay()" );
    if( isValid() ) {
      nielsenApp.play( channelNameJson );
    }
  }

  /* (non-Javadoc)
   * @see com.ooyala.android.AnalyticsPluginInterface#onStop()
   */
  @Override
  public synchronized void onStop() {
    DebugMode.logV( TAG, "onStop()" );
    if( isValid() ) {
      nielsenApp.stop();
    }
  }

  /* (non-Javadoc)
   * @see com.ooyala.android.AnalyticsPluginInterface#onPlayheadUpdate(int)
   */
  @Override
  public synchronized void onPlayheadUpdate( int playheadMsec ) {
    DebugMode.logV( TAG, "onPlayheadUpdate(): playheadMsec=" + playheadMsec );
    if( playheadMsec > 0 && Math.abs(playheadMsec - lastPlayheadMsec) > 2000 ) {
      lastPlayheadMsec = playheadMsec;
      DebugMode.logV( TAG, "onPlayheadUpdate(): updating" );
      if( isValid() ) {
        nielsenApp.setPlayheadPosition( playheadMsec/1000 );
      }
    }
  }

  @Override
  public void onAppSdkEvent( long timestamp, int code, String description ) {
    DebugMode.logV( TAG, "onAppSdkEvent(): timestamp=" + timestamp + ", code=" + code + ", description=" + description );
  }
}
