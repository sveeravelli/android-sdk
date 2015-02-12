package com.ooyala.android.nielsensdk;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.nielsen.app.sdk.AppSdk;
import com.nielsen.app.sdk.IAppNotifier;
import com.ooyala.android.util.DebugMode;
import com.ooyala.android.ID3TagNotifier;
import com.ooyala.android.ID3TagNotifier.ID3TagNotifierListener;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.Utils;
import com.ooyala.android.item.Video;

public class NielsenAnalytics implements ID3TagNotifierListener, IAppNotifier, Observer {
  private static final String TAG = NielsenAnalytics.class.getSimpleName();
  private static final String BACKLOT_NIELSEN_KEY_PREFIX = "nielsen_";
  private static final String CHANNEL_NAME_KEY = "channelName";

  private OoyalaPlayer player;
  private AppSdk nielsenApp;
  private final ID3TagNotifier id3TagNotifier;
  private String channelNameJson;
  private JSONObject metadataJson;
  private long lastReportedMsec;
  private final JSONObject customMetadata;

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
   * @param customConfig, optionally null, is any custom JSON you want added into the config for Nielsen's AppSdk. (Note that
   * Nielsen requires data to meet certain restrictions for valid strings.)
   * @param customMetadata, optionally null, is any custom JSON you want added into the metadata for Nielsen's loadMetadata. (Note that
   * Nielsen requires data to meet certain restrictions for valid strings.)
   * @see AppSdk
   * @see #destroy()
   */
  public NielsenAnalytics( Context context, OoyalaPlayer player, String appName, String appVersion, String sfCode, String appID, ID3TagNotifier id3TagNotifier, JSONObject customConfig, JSONObject customMetadata ) {
    this.metadataJson = new JSONObject();
    this.customMetadata = customMetadata;
    this.player = player;
    this.id3TagNotifier = id3TagNotifier;
    this.lastReportedMsec = Long.MIN_VALUE;
    JSONObject configJson = new JSONObject();
    try {
      configJson.put( "appname", appName );
      configJson.put( "appversion", appVersion );
      configJson.put( "sfcode", sfCode );
      configJson.put( "appid", appID );
      Utils.overwriteJSONObject( customConfig, configJson );
    } catch (JSONException e) {
      DebugMode.logE( TAG, e.toString() );
    }
    this.nielsenApp = AppSdk.getInstance( context, configJson.toString(), null );
    DebugMode.logV( TAG, "<init>(): isValid = " + AppSdk.isValid() );
    this.id3TagNotifier.addWeakListener( this );
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
    if( isValid() ) {
      nielsenApp.suspend();
    }
    if( nielsenApp != null ) {
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
        nielsenApp.sendID3( nielsenStr );
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
    DebugMode.logV( TAG, "reportPlayheadUpdate(): isLive=" + item.isLive() + ", playheadMsec=" + playheadMsec );
    long reportingMsec = item.isLive() ? System.currentTimeMillis() : playheadMsec;
    if( isValid() && reportingMsec > 0 && Math.abs(reportingMsec - lastReportedMsec) > 2000 ) {
      DebugMode.logV( TAG, "reportPlayheadUpdate(): updating" );
      nielsenApp.setPlayheadPosition( (int)(reportingMsec/1000) );
      lastReportedMsec = reportingMsec;
    }
  }

  private void reportMetadata() {
    updateContentTypeMetadata();
    DebugMode.logV( TAG, "reportMetadata(): " + metadataJson );
    if( isValid() ) {
      nielsenApp.loadMetadata( metadataJson.toString() );
    }
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
      case SUSPENDED:
      case COMPLETED:
      case ERROR:
        stop();
        break;
      default:
        break;
    }
  }

  private void itemChanged( Video item ) {
    setChannelNameJson( item );
    setMetadataJson( item );
  }

  private void setChannelNameJson( Video item ) {
    if( item.getMetadata().containsKey( BACKLOT_NIELSEN_KEY_PREFIX + CHANNEL_NAME_KEY ) ) {
      JSONObject json = new JSONObject();
      try {
        json.put( CHANNEL_NAME_KEY, item.getMetadata().get( BACKLOT_NIELSEN_KEY_PREFIX + CHANNEL_NAME_KEY ) );
        channelNameJson = json.toString();
      } catch( JSONException e ) {
        DebugMode.logE( TAG, e.toString() );
      }
    }
  }

  private void setMetadataJson( Video item ) {
    metadataJson = new JSONObject();
    try {
      metadataJson.put( "nielsen_length", NielsenJSONFilter.s_instance.filter( String.valueOf( item.getDuration() ) ) );
      copyBacklotNielsenMetadata( metadataJson, item );
      Utils.overwriteJSONObject( customMetadata, metadataJson );
    } catch (JSONException e) {
      DebugMode.logE( TAG, e.toString() );
    }
  }

  private void copyBacklotNielsenMetadata( JSONObject metadataJson, Video item ) {
    for( Map.Entry<String,String> kv : item.getMetadata().entrySet() ) {
      if( kv.getKey().startsWith( BACKLOT_NIELSEN_KEY_PREFIX ) ) {
        try {
          metadataJson.put( kv.getKey(), kv.getValue() );
        } catch (JSONException e) {
          DebugMode.logE( TAG, e.toString() );
        }
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
