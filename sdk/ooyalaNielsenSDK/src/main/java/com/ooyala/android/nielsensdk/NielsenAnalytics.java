package com.ooyala.android.nielsensdk;

import android.content.Context;
import com.nielsen.app.sdk.AppSdk;
import com.nielsen.app.sdk.IAppNotifier;
import com.ooyala.android.ID3TagNotifier;
import com.ooyala.android.ID3TagNotifier.ID3TagNotifierListener;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.Utils;
import com.ooyala.android.item.ModuleData;
import com.ooyala.android.item.Video;
import com.ooyala.android.util.DebugMode;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class NielsenAnalytics implements ID3TagNotifierListener, IAppNotifier, Observer {
  private static final String TAG = NielsenAnalytics.class.getSimpleName();
  private static final String BACKLOT_NIELSEN_KEY_PREFIX = "nielsen_";
  private static final String NIELSEN_KEY_DATASRC = "dataSrc";
  private static final String NIELSEN_VALUE_DATASRC_CMS = "cms";
  private static final String NIELSEN_KEY_LENGTH = "length";
  private static final int NIELSEN_VALUE_LENGTH_LIVE = 86400;
  private static final String NIELSEN_KEY_TYPE = "type";
  private static final String NIELSEN_KEY_CHANNEL_NAME = "channelName";

  private OoyalaPlayer player;
  private AppSdk nielsenApp;
  private final NielsenJSONFilter jsonFilter;
  private final ID3TagNotifier id3TagNotifier;
  private JSONObject metadataJson;
  private JSONObject channelJson;
  private long lastReportedMsec;
  private final JSONObject customMetadata;

  /**
   * Implementation of integration between Ooyala SDK and Nielsen AppSdk.
   * See the Nielsen SDK documentation around AppSdk.getInstance().
   * Note: When the client app goes into / resumes from the background, the app should destroy() this instance
   * and create a new one via the constructor. See the lifecycle diagram in the Nielsen Android Developer's Guide.
   * @param context Android Context. Not null.
   * @param player OoyalaPlayer. Not null.
   * @param appID per Nielsen SDK docs. Not null.
   * @param appVersion per Nielsen SDK docs. Not null.
   * @param appName per Nielsen SDK docs. Not null.
   * @param sfCode per Nielsen SDK docs. Not null.
   * @param customConfig, optionally null, is any custom JSON you want added into the config for Nielsen's AppSdk, such as longitude. (Note that
   * Nielsen requires data to meet certain restrictions for valid strings.)
   * @param customMetadata, optionally null, is any custom JSON you want added into the metadata for Nielsen's loadMetadata(). (Note that
   * Nielsen requires data to meet certain restrictions for valid strings.)
   * @see AppSdk
   * @see #destroy()
   */
  public NielsenAnalytics( Context context, OoyalaPlayer player, String appID, String appVersion, String appName, String sfCode, ID3TagNotifier id3TagNotifier, JSONObject customConfig, JSONObject customMetadata ) {
    this.metadataJson = new JSONObject();
    this.customMetadata = customMetadata;
    this.player = player;
    this.id3TagNotifier = id3TagNotifier;
    this.lastReportedMsec = Long.MIN_VALUE;
    JSONObject configJson = new JSONObject();
    try {
      configJson.put( "appid", jsonFilter.filter(appID) );
      configJson.put( "appversion", jsonFilter.filter(appVersion) );
      configJson.put( "appname", jsonFilter.filter(appName) );
      configJson.put( "sfcode", jsonFilter.filter(sfCode) );
      Utils.overwriteJSONObject( customConfig, configJson );
    } catch (JSONException e) {
      DebugMode.logE( TAG, e.toString() );
    }
    DebugMode.logV( TAG, "<init>: json = " + configJson );
    this.nielsenApp = AppSdk.getInstance( context, configJson.toString(), null );
    DebugMode.logV( TAG, "<init>(): isValid = " + AppSdk.isValid() );
    this.id3TagNotifier.addWeakListener( this );
    this.player.addObserver( this );
  }

  /**
   * Provides the AppSdk reference we are using internally,
   * for use cases that aren't covered by this Class's interface.
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

  /**
   * This is mainly for internal use by NielsenAnalytics itself,
   * but can be used to gate the use of NielsenAnalytics by clients.
   * It implements a slightly more strict validation than the Nielsen AppSdk.isValid() method.
   * @return true if we are set up to report to Nielsen, false otherwise.
   */
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

  private void reportPlayheadUpdate( Video item, int playheadMsec ) {
    long reportingMsec = item.isLive() ? System.currentTimeMillis() : playheadMsec;
    if( isValid() && reportingMsec > 0 && Math.abs(reportingMsec - lastReportedMsec) > 2000 ) {
      nielsenApp.setPlayheadPosition( (int)(reportingMsec/1000) );
      lastReportedMsec = reportingMsec;
    }
  }

  public void update( Observable o, Object arg ) {
    if( o != player ) {
      DebugMode.logE( TAG, "not our player!" );
    }
    else {
      if( arg == OoyalaPlayer.CURRENT_ITEM_CHANGED_NOTIFICATION ) {
        itemChanged( player.getCurrentItem() );
      } else if( arg == OoyalaPlayer.STATE_CHANGED_NOTIFICATION ) {
        stateUpdate( player.getState() );
      } else if( arg == OoyalaPlayer.TIME_CHANGED_NOTIFICATION ) {
        reportPlayheadUpdate( player.getCurrentItem(), player.getPlayheadTime() );
      }
    }
  }

  private void stateUpdate( OoyalaPlayer.State state ) {
    switch( state ) {
    case PLAYING:
      sendPlay();
      break;
    case PAUSED:
    case SUSPENDED:
    case COMPLETED:
    case ERROR:
      sendStop();
      break;
    default:
      break;
    }
  }

  private void sendPlay() {
    DebugMode.logV( TAG, "sendPlay()" );
    if( isValid() ) {
      updateMetadata();
      nielsenApp.play( channelJson.toString() );
      nielsenApp.loadMetadata( metadataJson.toString() );
    }
  }

  private void sendStop() {
    DebugMode.logV( TAG, "sendStop()" );
    if( isValid() ) {
      nielsenApp.stop();
    }
  }

  private void itemChanged( Video item ) {
    metadataJson = null;
    channelJson = null;
  }

  private void updateMetadata() {
    final Video item = player.getCurrentItem();
    ensureInitializedMetadata( item );
    updateContentTypeMetadata( metadataJson, player.getPlayingType() );
  }

  private void ensureInitializedMetadata( Video item ) {
    if( metadataJson == null ) {
      metadataJson = initMetadata( item, customMetadata );
      extractChannelName();
    }
  }

  private void extractChannelName() {
    final String backlotKey = BACKLOT_NIELSEN_KEY_PREFIX + NIELSEN_KEY_CHANNEL_NAME;
    if( metadataJson.has( backlotKey ) ) {
      try {
        final String channelName = metadataJson.getString( backlotKey );
        channelJson = new JSONObject();
        channelJson.put( NIELSEN_KEY_CHANNEL_NAME, channelName );
      }
      catch( JSONException e ) {
        e.printStackTrace();
      }
    }
  }

  private static JSONObject initMetadata( Video item, JSONObject customMetadata ) {
    final JSONObject json = new JSONObject();
    copyBacklotNielsenMetadata( json, item.getMetadata() );
    copyModuleBacklotNielsenMetadata( json, item.getModuleData() );
    updateLength( json, item );
    try {
      Utils.overwriteJSONObject( customMetadata, json );
    }
    catch( JSONException e ) {
      DebugMode.logE( TAG, e.toString() );
    }
    return json;
  }

  private static void copyBacklotNielsenMetadata( JSONObject json, Map<String, String> data ) {
    for( Map.Entry<String, String> kv : data.entrySet() ) {
      if( kv.getKey().startsWith( BACKLOT_NIELSEN_KEY_PREFIX ) ) {
        try {
          json.put( kv.getKey(), kv.getValue() );
        } catch( JSONException e ) {
          DebugMode.logE( TAG, e.toString() );
        }
      }
    }
  }

  private static void copyModuleBacklotNielsenMetadata( JSONObject json, Map<String, ModuleData> data ) {
    for( ModuleData moduleData : data.values() ) {
      copyBacklotNielsenMetadata( json, moduleData.getMetadata() );
    }
  }

  private static boolean isCMS( JSONObject json ) {
    final Object cmsValue = Utils.getJSONValueOrElse( json, NIELSEN_KEY_DATASRC, null );
    final boolean isCMS = NIELSEN_VALUE_DATASRC_CMS.equals( cmsValue );
    return isCMS;
  }

  /**
   * Should be called after copy-from-Backlot methods.
   */
  private static void updateLength( JSONObject json, Video item ) {
    // it might have already been set into the json from static Backlot metadata.
    final boolean alreadySet = json.has( NIELSEN_KEY_LENGTH );
    // todo: check if length really comes in with ID3 for nonCMS case.
    if( isCMS( json ) && !alreadySet ) {
      int length = item.isLive() ? NIELSEN_VALUE_LENGTH_LIVE : item.getDuration();
      try {
        json.put( NIELSEN_KEY_LENGTH, String.valueOf( length ) );
      }
      catch( JSONException e ) {
        DebugMode.logE( TAG, e.toString() );
      }
    }
  }

  private static void updateContentTypeMetadata( JSONObject json, OoyalaPlayer.ContentOrAdType type ) {
    if( isCMS( json ) ) {
      String typeMetadata;
      switch( type ) {
        case MainContent:   typeMetadata = "content"; break;
        case PreRollAd:     typeMetadata = "preroll"; break;
        case MidRollAd:     typeMetadata = "midroll"; break;
        case PostRollAd:    typeMetadata = "postroll"; break;
        default:            typeMetadata = "content"; break;
      }
      try {
        json.put( NIELSEN_KEY_TYPE, typeMetadata );
      }
      catch( JSONException e ) {
        DebugMode.logE( TAG, e.toString() );
      }
    }
  }

  public void onAppSdkEvent( long timestamp, int code, String description ) {
    DebugMode.logV( TAG, "onAppSdkEvent(): timestamp=" + timestamp + ", code=" + code + ", description=" + description );
  }
}
