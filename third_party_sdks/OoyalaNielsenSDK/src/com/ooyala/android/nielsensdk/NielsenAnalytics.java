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
  private final NielsenJSONFilter jsonFilter;
  private final String clientID;
  private final String vcID;
  private final String pd;
  private final ID3TagNotifier id3TagNotifier;
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
   * @param longitude per Nielsen SDK docs. Optional, can be null to omit it.
   * @param latitude per Nielsen SDK docs. Optional, can be null to omit it.
   * @param clientID per Nielsen SDK docs. Not null.
   * @param vcID per Nielsen SDK docs. Not null.
   * @param pd per Nielsen SDK docs. Not null.
   * @see AppSdk
   */
  public NielsenAnalytics( Context context, String appName, String appVersion, String sfCode, String appID, String dma, String ccode, String longitude, String latitude, String clientID, String vcID, String pd, ID3TagNotifier id3TagNotifier ) {
    this.clientID = clientID;
    this.vcID = vcID;
    this.pd = pd;
    this.id3TagNotifier = id3TagNotifier;
    this.lastPlayheadMsec = Integer.MIN_VALUE;
    this.jsonFilter = new NielsenJSONFilter();
    JSONObject configJson = new JSONObject();
    try {
      configJson.put( "appname", jsonFilter.filter(appName) );
      configJson.put( "appversion", jsonFilter.filter(appVersion) );
      configJson.put( "sfcode", jsonFilter.filter(sfCode) );
      configJson.put( "appid", jsonFilter.filter(appID) );
      if( dma != null ) { configJson.put( "dma", jsonFilter.filter(dma) ); }
      if( ccode != null ) { configJson.put( "ccode", jsonFilter.filter(ccode) ); }
      if( longitude != null ) { configJson.put( "longitude", jsonFilter.filter(longitude) ); }
      if( latitude != null ) { configJson.put( "latitude", jsonFilter.filter(latitude) ); }
      this.nielsenApp = AppSdk.getInstance( context, configJson.toString() );
      id3TagNotifier.addWeakListener( this );
    } catch (JSONException e) {
      DebugMode.logE( TAG, e.toString() );
    }
    setChannelName( UNKNOWN_CHANNEL_NAME );
  }

  public String buildMetadataJson( String assetID, int lengthSec, String type, String category, String ocrTag, boolean tv, String prod, String tfID, String sID ) {
    JSONObject json = new JSONObject();
    try {
      json.put( "assetid", jsonFilter.filter(assetID) );
      json.put( "clientid", jsonFilter.filter(this.clientID) );
      json.put( "vcid", jsonFilter.filter(this.vcID) );
      json.put( "length", jsonFilter.filter(String.valueOf(lengthSec)) );
      json.put( "type", jsonFilter.filter(type) );
      json.put( "category", jsonFilter.filter(category) );
      json.put( "ocrtag", jsonFilter.filter(ocrTag) );
      json.put( "tv", tv );
      json.put( "prod", jsonFilter.filter(prod) );
      json.put( "pd", jsonFilter.filter(this.pd) );
      json.put( "tfid", jsonFilter.filter(tfID) );
      json.put( "sid", jsonFilter.filter(sID) );
    } catch (JSONException e) {
      DebugMode.logE( TAG, "buildMetadataJson(): " + e.toString() );
    }
    return json.toString();
  }

  /**
   * Provides the AppSdk reference for use cases that aren't covered by this Class's interface.
   * In particular, the 3rd party application must register themselves as a listener on the AppSdk
   * in order to wait for the EVENT_STARTUP event, after which the opt in/out URL will be available
   * from the AppSdk.
   * @return our cached AppSdk ref, originally obtained by calling AppSdk.getInstance() in our constructor.
   * @see AppSdk#getInstance(Context, String)
   * @see AppSdk#EVENT_STARTUP
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
      json.put( "channelName", jsonFilter.filter(channelName) );
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

  /**
   * Currently only intended for use internally, immediately after play().
   */
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
  public synchronized void play() {
    DebugMode.logV( TAG, "play()" );
    if( isValid() ) {
      nielsenApp.play( channelNameJson );
    }
  }

  /* (non-Javadoc)
   * @see com.ooyala.android.AnalyticsPluginInterface#onStop()
   */
  @Override
  public synchronized void stop() {
    DebugMode.logV( TAG, "stop()" );
    if( isValid() ) {
      nielsenApp.stop();
    }
  }

  /* (non-Javadoc)
   * @see com.ooyala.android.AnalyticsPluginInterface#onPlayheadUpdate(int)
   */
  @Override
  public synchronized void reportPlayheadUpdate( int playheadMsec ) {
    DebugMode.logV( TAG, "reportPlayheadUpdate(): playheadMsec=" + playheadMsec );
    if( playheadMsec > 0 && Math.abs(playheadMsec - lastPlayheadMsec) > 2000 ) {
      lastPlayheadMsec = playheadMsec;
      DebugMode.logV( TAG, "reportPlayheadUpdate(): updating" );
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
