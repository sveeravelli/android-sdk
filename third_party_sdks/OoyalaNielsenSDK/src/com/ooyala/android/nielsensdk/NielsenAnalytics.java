package com.ooyala.android.nielsensdk;

import java.util.Observable;
import java.util.Observer;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.nielsen.app.sdk.AppSdk;
import com.nielsen.app.sdk.IAppNotifier;
import com.ooyala.android.DebugMode;
import com.ooyala.android.ID3TagNotifier;
import com.ooyala.android.ID3TagNotifier.ID3TagNotifierListener;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.item.Video;

public class NielsenAnalytics implements ID3TagNotifierListener, IAppNotifier, Observer {
  private static final String TAG = NielsenAnalytics.class.getSimpleName();
  private static final String UNKNOWN_CHANNEL_NAME = "unknown_not_yet_set_by_app";
  private static final String METADATA_PREFIX = "nielsen_";

  private OoyalaPlayer player;
  private AppSdk nielsenApp;
  private final NielsenJSONFilter jsonFilter;
  private final String clientID;
  private final String vcID;
  private final ID3TagNotifier id3TagNotifier;
  private String channelName;
  private String channelNameJson;
  private JSONObject metadataJson;
  private long lastReportedMsec;

  /**
   * Implementation of integration between Ooyala SDK and Nielsen AppSdk.
   * See the Nielsen SDK documentation around AppSdk.getInstance().
   * Note: When the client app goes into / resumes from the background, the app should destroy() this instance
   * and create a new one via the constructor. See the lifecycle diagram in the Nielsen Android Developer's Guide.
   * @param context Android Context. Not null.
   * @param player OoyalaPlayer. Not null.
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
   * @see AppSdk
   * @see #destroy()
   */
  public NielsenAnalytics( Context context, OoyalaPlayer player, String appName, String appVersion, String sfCode, String appID, String dma, String ccode, String longitude, String latitude, String clientID, String vcID, ID3TagNotifier id3TagNotifier ) {
    this.player = player;
    this.clientID = clientID;
    this.vcID = vcID;
    this.id3TagNotifier = id3TagNotifier;
    this.lastReportedMsec = Long.MIN_VALUE;
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
    } catch (JSONException e) {
      DebugMode.logE( TAG, e.toString() );
    }
    this.nielsenApp = AppSdk.getInstance( context, configJson.toString() );
    DebugMode.logV( TAG, "<init>(): isValid = " + AppSdk.isValid() );
    this.id3TagNotifier.addWeakListener( this );
    setChannelName( UNKNOWN_CHANNEL_NAME );
    this.player.addObserver( this );
  }

  /**
   * Provides the AppSdk reference we internally use: for use cases that aren't covered by this Class's interface.
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

  public void destroy() {
    DebugMode.logV( TAG, "destroy()" );
    player.deleteObserver( this );
    id3TagNotifier.removeWeakListener( this );
    setChannelName( UNKNOWN_CHANNEL_NAME );
    if( nielsenApp != null ) {
      nielsenApp.suspend();
      nielsenApp = null;
    }
  }

  public boolean isValid() {
    final boolean isValid = nielsenApp != null && AppSdk.isValid();
    DebugMode.logV( TAG, "isValid(): " + isValid );
    return isValid;
  }

  public void onTag( byte[] tag ) {
    if( isValid() ) {
      final String tagStr = new String(tag);
      DebugMode.logV( TAG, "onTag(): tagStr=" + tagStr );
      if( tagStr.contains("www.nielsen.com") ) {
        final String nielsenStr = tagStr.replaceFirst( ".*www.nielsen.com", "www.nielsen.com" );
        DebugMode.logV( TAG, "onTag(): nielsenStr=" + nielsenStr );
      }
    }
  }

  private void play() {
    DebugMode.logV( TAG, "play()" );
    if( isValid() ) {
      nielsenApp.play( channelNameJson );
      reportMetadata();
    }
  }

  private void stop() {
    DebugMode.logV( TAG, "stop()" );
    if( isValid() ) {
      nielsenApp.stop();
    }
  }

  private void reportPlayheadUpdate( Video item, int playheadMsec ) {
    reportPlayhead( item, playheadMsec );
    reportMetadata();
  }

  private void reportPlayhead( Video item, int playheadMsec ) {
    DebugMode.logV( TAG, "reportPlayhead(): isLive=" + item.isLive() + ", playheadMsec=" + playheadMsec );
    long reportingMsec = item.isLive() ? System.currentTimeMillis() : playheadMsec;
    if( reportingMsec > 0 && Math.abs(reportingMsec - lastReportedMsec) > 2000 && isValid() ) {
      DebugMode.logV( TAG, "reportPlayhead(): updating" );
      nielsenApp.setPlayheadPosition( (int)(reportingMsec/1000) );
      lastReportedMsec = reportingMsec;
    }
  }

  private void reportMetadata() {
    updateContentTypeMetadata();
    DebugMode.logV( TAG, "reportMetadata(): " + metadataJson );
    nielsenApp.loadMetadata( metadataJson.toString() );
  }

  private void updateContentTypeMetadata() {
    OoyalaPlayer.ContentOrAdType type = player.getPlayingType();
    String typeMetadata;
    switch( type ) {
    case MainContent: typeMetadata = "content"; break;
    case PreRollAd: typeMetadata = "preroll"; break;
    case MidRollAd: typeMetadata = "midroll"; break;
    case PostRollAd: typeMetadata = "postroll"; break;
    default: typeMetadata = "content"; break;
    }
    try {
      metadataJson.put( "type", typeMetadata );
    }
    catch( JSONException e ) {
      DebugMode.logE( TAG, e.toString() );
    }
  }

  private void stateUpdate( OoyalaPlayer.State state ) {
    switch( state ) {
    case PLAYING:
      play();
      break;
    case PAUSED:
    case COMPLETED:
    case ERROR:
      stop();
      break;
    default:
      break;
    }
  }

  private void itemChanged( Video item ) {
    setChannelName( item.getEmbedCode() ); // todo: what's really the best channel name source?
    setMetadataJson( item );
  }

  private void setChannelName( String channelName ) {
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

  private String getChannelName() {
    return channelName;
  }

  private void setMetadataJson( Video item ) {
    metadataJson = new JSONObject();
    try {
      metadataJson.put( "assetid", jsonFilter.filter(item.getEmbedCode()) );
      metadataJson.put( "length", jsonFilter.filter(String.valueOf(item.getDuration())) );
      metadataJson.put( "title", jsonFilter.filter(item.getTitle()) );
      // todo: type? has to dynamically change as we're in-and-out of advertisements.
      // todo: all the others.
      if( clientID != null ) { metadataJson.put( "clientid", jsonFilter.filter(clientID) ); }
      if( vcID != null ) { metadataJson.put( "vcid", jsonFilter.filter(vcID) ); }
      setMetadataHelper( metadataJson, item, "category" );
      setMetadataHelper( metadataJson, item, "censuscategory" );
      setMetadataHelper( metadataJson, item, "tv" );
      setMetadataHelper( metadataJson, item, "prod" );
      setMetadataHelper( metadataJson, item, "pd" );
      setMetadataHelper( metadataJson, item, "tfid" );
      setMetadataHelper( metadataJson, item, "sid" );
      // explicitly leaving out 'ocrtag' at the moment as unimplemented.
    } catch (JSONException e) {
      DebugMode.logE( TAG, e.toString() );
    }
  }

  private void setMetadataHelper( JSONObject json, Video item, String key ) {
    final String metadataKey = METADATA_PREFIX + key;
    if( item.getMetadata().containsKey( metadataKey ) ) {
      final String value = item.getMetadata().get( metadataKey );
      try {
        json.put( key, jsonFilter.filter(value) );
      }
      catch (JSONException e) {
        DebugMode.logE( TAG, "setMetadataHelper(): " + e.toString() );
      }
    }
  }

  public void update( Observable o, Object arg ) {
    DebugMode.assertEquals( o, player, TAG, "not our player?!" );
    if( arg == OoyalaPlayer.CURRENT_ITEM_CHANGED_NOTIFICATION ) {
      itemChanged( player.getCurrentItem() );
    }
    else if( arg == OoyalaPlayer.STATE_CHANGED_NOTIFICATION ) {
      stateUpdate( player.getState() );
    }
    else if( arg == OoyalaPlayer.TIME_CHANGED_NOTIFICATION ) {
      reportPlayheadUpdate( player.getCurrentItem(), player.getPlayheadTime() );
    }
  }

  public void onAppSdkEvent( long timestamp, int code, String description ) {
    DebugMode.logV( TAG, "onAppSdkEvent(): timestamp=" + timestamp + ", code=" + code + ", description=" + description );
  }
}
