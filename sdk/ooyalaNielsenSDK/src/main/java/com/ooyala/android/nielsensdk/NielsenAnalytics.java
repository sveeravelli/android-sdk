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
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class NielsenAnalytics implements ID3TagNotifierListener, Observer {
  private static final String TAG = NielsenAnalytics.class.getSimpleName();
  private static final String BACKLOT_NIELSEN_KEY_PREFIX = "nielsen_";
  private static final String NIELSEN_KEY_DATASRC = "dataSrc";
  private static final String NIELSEN_VALUE_DATASRC_CMS = "cms";
  private static final String NIELSEN_KEY_LENGTH = "length";
  private static final int NIELSEN_VALUE_LENGTH_LIVE = 86400;
  private static final String NIELSEN_KEY_TYPE = "type";
  private static final String NIELSEN_KEY_CHANNEL_NAME = "channelName";
  private static final String NIELSEN_ASSET_ID_KEY = "assetid";
  private static String n2b( final String nkey ) { return BACKLOT_NIELSEN_KEY_PREFIX + nkey; }
  private static String b2n( final String bkey ) { return bkey.replaceFirst( BACKLOT_NIELSEN_KEY_PREFIX, "" ); }

  private OoyalaPlayer player;
  private AppSdk nielsenApp;
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
   * @param iappNotifier per Nielsen SDK docs, optionally null.
   * @param appID per Nielsen SDK docs. Not null.
   * @param appVersion per Nielsen SDK docs. Not null.
   * @param appName per Nielsen SDK docs. Not null.
   * @param sfCode per Nielsen SDK docs. Not null.
   * @param customConfig, optionally null, is any custom JSON you want added into the config for Nielsen's AppSdk, such as longitude. (Note that
   * Nielsen requires data to meet certain restrictions for valid strings.)
   * @param customMetadata, optionally null, is any custom JSON you want added into the metadata for Nielsen's loadMetadata(). (Note that
   * Nielsen requires data to meet certain restrictions for valid strings.)
   * see AppSdk
   * @see #destroy()
   */
  public NielsenAnalytics( Context context, OoyalaPlayer player, IAppNotifier iappNotifier, String appID, String appVersion, String appName, String sfCode, JSONObject customConfig, JSONObject customMetadata ) {
    this.metadataJson = new JSONObject();
    this.customMetadata = customMetadata;
    this.player = player;
    this.id3TagNotifier = player.getID3TagNotifier();
    this.lastReportedMsec = Long.MIN_VALUE;
    JSONObject configJson = new JSONObject();
    try {
      configJson.put( "appid", NielsenJSONFilter.s_instance.filter(appID) );
      configJson.put( "appversion", NielsenJSONFilter.s_instance.filter(appVersion) );
      configJson.put( "appname", NielsenJSONFilter.s_instance.filter(appName) );
      configJson.put( "sfcode", NielsenJSONFilter.s_instance.filter(sfCode) );
      Utils.overwriteJSONObject( customConfig, configJson );
    } catch (JSONException e) {
      DebugMode.logE( TAG, e.toString() );
    }
    DebugMode.logV( TAG, "<init>: json = " + configJson );
    this.nielsenApp = AppSdk.getInstance( context, configJson.toString(), iappNotifier );
    DebugMode.logV( TAG, "<init>(): isValid = " + AppSdk.isValid() );
    this.id3TagNotifier.addWeakListener( this );
    this.player.addObserver( this );
    lastReportedMsec = -1;
  }

  /**
   * Provides the AppSdk reference we are using internally,
   * for use cases that aren't covered by this Class's interface.
   * In particular, the 3rd party application must register themselves as a listener on the AppSdk
   * in order to wait for the EVENT_STARTUP event, after which the opt in/out URL will be available
   * from the AppSdk.
   * @return our cached AppSdk ref, originally obtained by calling AppSdk.getInstance() in our constructor.
   * see AppSdk#getInstance(Context, String)
   * see AppSdk#EVENT_STARTUP
   */
  public AppSdk getNielsenAppSdk() {
    return this.nielsenApp;
  }

  /**
   * Destroy the current NielsenAnalytics object.
   */
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
    return
      nielsenApp != null &&
      AppSdk.isValid();
  }

  /**
   * Handle the original tag and send Nielsen ID3 tag data
   * @param tag
   */
  public void onTag( byte[] tag ) {
    if( isValid() && isContent() ) {
      final String tagStr = new String(tag);
      DebugMode.logV( TAG, "onTag(): tagStr=" + tagStr );
      if( tagStr.contains("www.nielsen.com") ) {
        final String nielsenStr = tagStr.replaceFirst( ".*www.nielsen.com", "www.nielsen.com" );
        DebugMode.logV( TAG, "onTag(): nielsenStr=" + nielsenStr );
        nielsenApp.sendID3( nielsenStr );
      }
    }
  }

  /**
   * update the player according to notification
   * @param o the player
   * @param arg notification of the player
   */
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

  private void itemChanged( Video item ) {
    sendStop();
    metadataJson = null;
    channelJson = null;
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
      case LOADING:
        sendStop();
        break;
      default:
        break;
    }
  }

  private void reportPlayheadUpdate( Video item, int playheadMsec ) {
    long reportingMsec = item.isLive() ? System.currentTimeMillis() : playheadMsec;
    if( isValid() && isContent() ) {
      final boolean notYetReported = lastReportedMsec < 0;
      final boolean reportExpired = Math.abs(reportingMsec - lastReportedMsec) > 2000;
      if( notYetReported || reportExpired ) {
        int playheadPosition = (int) (reportingMsec / 1000);
//        DebugMode.logV(TAG, "set playhead position: " + playheadPosition);
        nielsenApp.setPlayheadPosition( playheadPosition );
        lastReportedMsec = reportingMsec;
      }
    }
  }

  private void sendPlay() {
    DebugMode.logV( TAG, "sendPlay(): valid=" + isValid() + ", content=" + isContent() );
    if( isValid() && isContent() ) {
      updateMetadata();
      DebugMode.logV( TAG, "sendPlay(): channelJson = " + channelJson );
      nielsenApp.play( channelJson.toString() );
      DebugMode.logV( TAG, "sendPlay(): metadataJson = " + metadataJson );
      nielsenApp.loadMetadata( metadataJson.toString() );
      lastReportedMsec = -1;
    }
  }

  private boolean isContent() {
    return OoyalaPlayer.ContentOrAdType.MainContent == player.getPlayingType();
  }

  private void sendStop() {
    DebugMode.logV( TAG, "sendStop(): valid=" + isValid() + ", content=" + isContent() );
    if( isValid() && isContent() ) {
      nielsenApp.stop();
    }
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
      logMetadataWarnings();
    }
  }

  // I wish Backlot had validation in the UI but noooooo.
  private void logMetadataWarnings() {
    String assetIdValue = (String)Utils.getJSONValueOrElse( metadataJson, NIELSEN_ASSET_ID_KEY, "" );
    if( assetIdValue.matches( "\\s+") ) {
      DebugMode.logE( TAG, "logMetadataWarnings(): whitespace not allowed in assetid, was '" + assetIdValue + "'" );
      assetIdValue = assetIdValue.replaceAll( "\\s+", "" );
    }
    final Iterator<String> keys = metadataJson.keys();
    while ( keys.hasNext() ) {
      final String k = keys.next();
      final String v = (String)Utils.getJSONValueOrElse( metadataJson, k, null );
      if( v != null && NielsenJSONFilter.s_instance.filter( v ) != v ) {
        DebugMode.logE( TAG, "logMetadataWarnings(): perhaps invalid format, was '" + v + "'" );
      }
    }
  }

  private void extractChannelName() {
    channelJson = new JSONObject();
    String channelName = "";

    final String backlotKey = n2b( NIELSEN_KEY_CHANNEL_NAME );
    if( metadataJson.has( backlotKey ) ) {
      channelName = (String)Utils.getJSONValueOrElse( metadataJson, backlotKey, "" );
    }

    try {
      channelJson.put( NIELSEN_KEY_CHANNEL_NAME, channelName );
    }
    catch( JSONException e ) {
      DebugMode.logE( TAG, e.toString() );
    }
  }

  private static JSONObject initMetadata( Video item, JSONObject customMetadata ) {
    final JSONObject json = new JSONObject();
    copyModuleBacklotNielsenMetadata( json, item.getModuleData() );
    copyBacklotNielsenMetadata( json, item.getMetadata() );
    updateLength( json, item );
    try {
      Utils.overwriteJSONObject( customMetadata, json );
    }
    catch( JSONException e ) {
      DebugMode.logE( TAG, e.toString() );
    }
    return json;
  }

  private static void copyModuleBacklotNielsenMetadata( JSONObject json, Map<String, ModuleData> data ) {
    for( ModuleData moduleData : data.values() ) {
      copyBacklotNielsenMetadata( json, moduleData.getMetadata() );
    }
  }

  private static void copyBacklotNielsenMetadata( JSONObject json, Map<String, String> data ) {
    for( Map.Entry<String, String> kv : data.entrySet() ) {
      if( kv.getKey().startsWith( BACKLOT_NIELSEN_KEY_PREFIX ) ) {
        try {
          final String nkey = b2n( kv.getKey() );
          json.put( nkey, kv.getValue() );
        } catch( JSONException e ) {
          DebugMode.logE( TAG, e.toString() );
        }
      }
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
    if( isCMS( json ) && !alreadySet ) {
      int itemDurationMsec = item.getDuration();
      int itemDurationSeconds = itemDurationMsec / 1000;
      int length = item.isLive() ? NIELSEN_VALUE_LENGTH_LIVE : itemDurationSeconds;
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
}
