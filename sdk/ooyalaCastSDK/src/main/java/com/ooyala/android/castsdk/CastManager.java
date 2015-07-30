package com.ooyala.android.castsdk;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Status;
import com.google.sample.castcompanionlibrary.cast.DataCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.IDataCastConsumer;
import com.google.sample.castcompanionlibrary.cast.exceptions.CastException;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.ooyala.android.CastManagerInterface;
import com.ooyala.android.CastModeOptions;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.util.DebugMode;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CastManager implements CastManagerInterface, IDataCastConsumer {
  private static final String TAG = "CastManager";

  public static final String ACTION_PLAY = "OOCastPlay";
  public static final String ACTION_STOP = "OOCastStop";
  private static CastManager castManager;
  private static Class<?> targetActivity;
  private static Class<?> currentActivity;
  private static Context currentContext;
  private static int notificationMiniControllerResourceId;
  private static int notificationImageResourceId;

  private NotificationCompat.Builder notificationBuilder;
  private android.content.BroadcastReceiver receiver;
  private AudioManager audioManager;
  private RemoteControlClient remoteControlClient;
  private int notificationID = 001;
  private Bitmap miniControllerDefaultImageBitmap;

  private View castView;
  private WeakReference<OoyalaPlayer> ooyalaPlayer;
  private CastPlayer castPlayer;
  private Map<String, String> additionalInitParams;
  private Set<CastMiniController> miniControllers;
  private boolean notificationServiceIsActivated;
  private boolean isConnectedToReceiverApp;
  private boolean isPlayerSeekable = true;
  private boolean isInCastMode;

  private final DataCastManager dataCastManager;
  private final String namespace;

  public static CastManager initialize(Context context, String applicationId, String namespace) {
    if (null == castManager) {
      notificationMiniControllerResourceId = R.layout.oo_default_notification;
      notificationImageResourceId = R.drawable.ic_ooyala;
      DebugMode.logD(TAG, "Init OOCastManager with appId = " + applicationId + ", namespace = " + namespace);
      DebugMode.logD(TAG, "Create a new OOCastManager");
      if (ConnectionResult.SUCCESS != GooglePlayServicesUtil.isGooglePlayServicesAvailable(context)) {
        String msg = "Couldn't find the appropriate version of Google Play Services";
        DebugMode.logE(TAG, msg);
        throw new RuntimeException(msg);
      }
      try {
        DataCastManager.initialize( context, applicationId, new String[]{namespace} );
        castManager = new CastManager( DataCastManager.getInstance(), namespace );
      }
      catch( CastException ce ) {
        throw new RuntimeException( ce );
      }
    } else {
      DebugMode.logI(TAG, "Calling initialize a second time. Not an error, but any newer Application ID will not be respected");
    }
    return castManager;
  }
  
  public static CastManager getCastManager() {
    return castManager;
  }
  
  private CastManager( DataCastManager dataCastManager, String namespace ) {
    this.dataCastManager = dataCastManager;
    this.namespace = namespace;
    this.dataCastManager.addDataCastConsumer( this );
  }

  public DataCastManager getDataCastManager() {
    return this.dataCastManager;
  }

  /*============================================================================================*/
  /*========== CastManager App Setting API   ===================================================*/
  /*============================================================================================*/

  /**
   * Create options menu for cast button
   * @param activity: activity of third party application that implements cast button
   * @param menu: menu that pops up after clicking on the cast button
   */
  public void addCastButton(Activity activity, Menu menu) {
    DebugMode.logD(TAG, "Add Cast Button");
    this.setCurrentContext(activity);
    activity.getMenuInflater().inflate(R.menu.main, menu);
    MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
    MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider)
            MenuItemCompat.getActionProvider(mediaRouteMenuItem);
    mediaRouteActionProvider.setRouteSelector(dataCastManager.getMediaRouteSelector());
    mediaRouteActionProvider.setDialogFactory(new CastMediaRouteDialogFactory());
  }

  public void setCastView(View view) {
    DebugMode.assertCondition( view != null, TAG, "cannot set castView to null" );
    DebugMode.logD( TAG, "Set cast view to " + view );
    castView = view;
  }

  public View getCastView() {
    return castView;
  }

  public void setNotificationImageResourceId(int resourceId) {
    DebugMode.logD(TAG, "Set notification image resource id = " + resourceId);
    notificationImageResourceId = resourceId;
  }
  
  public void setDefaultMiniControllerImageBitmap(Bitmap imageBitmap) {
    DebugMode.logD(TAG, "Set mini controller image bitmap = " + imageBitmap);
    miniControllerDefaultImageBitmap = imageBitmap;
  }

  public void setNotificationMiniControllerLayout(int resourceId) {
    DebugMode.logD(TAG, "Set notification mini controller layout = " + resourceId);
    notificationMiniControllerResourceId = resourceId;
  }

  public void setCastPlayerSeekable(boolean isSeekable) {
    isPlayerSeekable = isSeekable;
  }

  /**
   * Init this.ooyalaPlayer with given ooyalaPlayer
   * Register the OOCastManager of the given ooyalaPlayer with "this"
   * @param ooyalaPlayer
   */
  public void registerWithOoyalaPlayer(OoyalaPlayer ooyalaPlayer) {
    DebugMode.logD(TAG, "Connect to OoyalaPlayer " + ooyalaPlayer);
    this.ooyalaPlayer = new WeakReference<OoyalaPlayer>(ooyalaPlayer);
    ooyalaPlayer.registerCastManager(this);
  }

  /**
   * Disconnect the current OoyalaPlayer from the OOCastManager
   * Called from App level when right before the connected OoyalaPlayer is destroyed
   */
  public void deregisterOoyalaPlayer() {
    DebugMode.logD( TAG, "Disconnect from ooyalaPlayer " + ooyalaPlayer );
    this.ooyalaPlayer = null;
    if (isInCastMode()) {
      castPlayer.disconnectFromCurrentOoyalaPlayer();
    }
  }

  /**
   * Provide key-value pairs that will be passed to the Receiver upon Cast Playback. Anything
   * added to this will overwrite anything set by default in the init.
   */
  public void setAdditionalInitParams(Map<String, String> params) {
    additionalInitParams = params;
  }

  /*============================================================================================*/
  /*========== Access CastManager Status Or Fields =============================================*/
  /*============================================================================================*/

  public Bitmap getDefaultMiniControllerImageBitmap() {
    return miniControllerDefaultImageBitmap;
  }

  public CastPlayer getCastPlayer() {
    return this.castPlayer;
  }

  public boolean isConnectedToReceiverApp() {
    return dataCastManager.isConnected() && this.isConnectedToReceiverApp;
  }

  /**
   * Update the mini controllers when activity with mini controllers resumes
   */
  public void onResume() {
    DebugMode.logD(TAG, "onResume()");
    updateMiniControllersState();
  }

  private void hideCastView() {
    DebugMode.logD(TAG, "Hide cast view");
    if (ooyalaPlayer != null && ooyalaPlayer.get().getLayout().getChildCount() != 0) {
      ooyalaPlayer.get().getLayout().removeView(castView);
    }
  }

  private void displayCastView() {
    DebugMode.logD(TAG, "CastView = " + castView);
    if (ooyalaPlayer != null && castView != null) {
      if (castView.getParent() != null) {
        ((ViewGroup) castView.getParent()).removeView(castView);
      }
      ooyalaPlayer.get().getLayout().addView(castView);
    }
  }

  private void destroyCastPlayer() {
    DebugMode.logD(TAG, "Destroy current CastPlayer");
    castPlayer.destroy();
    castPlayer = null;
  }

  /*============================================================================================*/
  /*========== Consumer callbacks =================================================================*/
  /*============================================================================================*/

  @Override
  public void onApplicationConnected(ApplicationMetadata appMetadata, String applicationStatus,
          String sessionId, boolean wasLaunched) {
    DebugMode.logD(TAG, "onApplicationConnected called");
    this.isConnectedToReceiverApp = true;
    this.castPlayer = createNewCastPlayer();
    if (ooyalaPlayer != null && ooyalaPlayer.get().getCurrentItem() != null) {
      ooyalaPlayer.get().switchToCastMode(ooyalaPlayer.get().getEmbedCode());
    }
  }

  private CastPlayer createNewCastPlayer() {
    DebugMode.logD(TAG, "Create new CastPlayer");
    return new CastPlayer(this);
  }
  
  @Override
  public void onApplicationDisconnected(int errorCode) {
    DebugMode.logD( TAG, "onApplicationDisconnected called" );
    this.isConnectedToReceiverApp = false;
    if (isInCastMode()) {
      exitCastMode();
    }
  }

  @Override
  public void onApplicationStopFailed( int errorCode ) {
    DebugMode.logD( TAG, "onApplicationStopFailed: " + errorCode );

  }

  @Override
  public boolean onApplicationConnectionFailed( int errorCode ) {
    return false; // TODO: what do we want here?
  }

  @Override
  public void onApplicationStatusChanged( String appStatus ) {
    DebugMode.logD( TAG, "onApplicationStatusChanged: " + appStatus );

  }

  @Override
  public void onVolumeChanged( double value, boolean isMute ) {
    DebugMode.logD( TAG, "onVolumeChanged: " + value + ", " + isMute );

  }

  @Override
  public void onConnected() {
    DebugMode.logD( TAG, "onConnected" );

  }

  @Override
  public void onConnectionSuspended( int cause ) {
    DebugMode.logD( TAG, "onConnectionSuspended: " + cause );
  }

  @Override
  public void onDisconnected() {
  DebugMode.logD(TAG, "onDisconnected called");
    this.isConnectedToReceiverApp = false;
    if (isInCastMode()) {
      exitCastMode();
    }
  }

  @Override
  public boolean onConnectionFailed( ConnectionResult result ) {
    return false; // TODO: what do we want here?
  }

  @Override
  public void onCastDeviceDetected( MediaRouter.RouteInfo info ) {
    DebugMode.logD( TAG, "onCastDeviceDetected: " + info );
  }

  @Override
  public void onCastAvailabilityChanged( boolean castPresent ) {
    DebugMode.logD( TAG, "onCastAvailabilityChanged: " + castPresent );
  }

  @Override
  public void onConnectivityRecovered() {
    DebugMode.logD( TAG, "onConnectivityRecovered" );
  }

  @Override
  public void onUiVisibilityChanged( boolean visible ) {
    DebugMode.logD( TAG, "onUiVisibilityChanged: " + visible );
  }

  @Override
  public void onReconnectionStatusChanged(int status) {
    DebugMode.logD( TAG, "onReconnectionStatusChanged: " + status );
  }

  @Override
  public void onDeviceSelected( CastDevice device ) {
    DebugMode.logD( TAG, "onDeviceSelected: " + device );
  }

  @Override
  public void onRemoved(CastDevice castDevice, String namespace) {
    DebugMode.logD( TAG, "onRemoved: " + castDevice + ", " + namespace );
  }

  public boolean isInCastMode() {
    return isInCastMode;
  }

  public void enterCastMode(CastModeOptions options) {
    DebugMode.logD(TAG, "enterCastMode with embedCode = " + options.getEmbedCode() + ", playhead = " + options.getPlayheadTimeInMillis() + " isPlaying = " + options.isPlaying());
    DebugMode.assertCondition( ooyalaPlayer != null, TAG, "ooyalaPlayer should be not null while entering cast mode" );
    DebugMode.assertCondition(castPlayer != null, TAG, "castPlayer should be not null while entering cast mode");
    new CastManagerInitCastPlayerAsyncTask(this, options).execute();
    displayCastView();
    isInCastMode = true;
  }

  void initCastPlayer(CastModeOptions options, String embedToken) {
    DebugMode.logD(TAG, "initCastPlayer with embedCode = " + options.getEmbedCode() + ", playhead = " + options.getPlayheadTimeInMillis() + " isPlaying = " + options.isPlaying());
    if (ooyalaPlayer != null) {
      castPlayer.setSeekable(isPlayerSeekable);
      castPlayer.setOoyalaPlayer(ooyalaPlayer.get());
      castPlayer.updateMetadataFromOoyalaPlayer(ooyalaPlayer.get());
      castPlayer.enterCastMode(options, embedToken, additionalInitParams);
    } else {
      DebugMode.logE(TAG, "Attempted to initCastPlayer while ooyalaPlayer is null");
    }
  }

  private void exitCastMode() {
    DebugMode.logD(TAG, "Exit Cast Mode");
    hideCastView();
    DebugMode.assertCondition(castPlayer != null, TAG, "castPlayer cannot be null when exit cast mode");
    if (ooyalaPlayer != null) {
      ooyalaPlayer.get().exitCastMode(castPlayer.currentTime(), castPlayer.getState() == State.PLAYING, castPlayer.getEmbedCode());
    }
    dismissMiniControllers();
    destroyCastPlayer();
    removeAllMiniControllers();
    isInCastMode = false;
  }

  public void sendDataMessage(String message) throws IllegalArgumentException, IllegalStateException, IOException,
      TransientNetworkDisconnectionException, NoConnectionException {
   dataCastManager.sendDataMessage(message, namespace);
  }

  @Override
  public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
    DebugMode.assertCondition( castPlayer != null, TAG, "castPlayer cannot be null" );
    if (castPlayer != null) {
      castPlayer.receivedMessage(message);
    }
  }

  @Override
  public void onMessageSendFailed( Status status ) {

  }
  
  /*============================================================================================*/
  /*========== MiniController ==================================================================*/
  /*============================================================================================*/

  public void addMiniController(CastMiniController miniController) {
    DebugMode.logD(TAG, "Add mini controller " + miniController);
    if (miniControllers == null) {
      miniControllers = new HashSet<CastMiniController>();
    }
    if (!miniControllers.contains(miniController)) {
      miniControllers.add(miniController);
    }
  }

  public void updateMiniControllersState() {
    DebugMode.logD(TAG, "Update mini controllers state");
    if (miniControllers != null &&  isInCastMode()) {
      if (castPlayer.getState() == State.COMPLETED) {
        dismissMiniControllers();
      } else {
        for (CastMiniController miniController : miniControllers) {
          miniController.updatePlayPauseButtonImage(castPlayer.getState() == State.PLAYING);
        }
      }
    }
  }
  
  public void removeMiniController(CastMiniController miniController) {
    DebugMode.logD(TAG, "Remove mini controller " + miniController);
    if (miniControllers != null) {
      miniControllers.remove(miniController);
    }
  }
  
  private void removeAllMiniControllers() {
    DebugMode.logD(TAG, "Remove all mini controllers");
    if (miniControllers != null) {
      miniControllers.clear();
    }
  }
  
  private void dismissMiniControllers() {
    DebugMode.logD(TAG, "dismiss mini controllers");
    if (miniControllers != null) {
      for (CastMiniController miniController : miniControllers) {
        miniController.dismiss();
      }
    }
  }
  
  public void setTargetActivity(Class<?> targetActivity) {
    this.targetActivity = targetActivity;
  }
  
  public Class<?> getTargetActivity() {
    return targetActivity;
  }

  public void setCurrentContext(Context context) {
    currentActivity = context.getClass();
    currentContext = context;
  }

  public Class<?> getCurrentActivity() {
    return currentActivity;
  }
  
  
  /*============================================================================================*/
  /*==========  Notification Service ============================================================*/
  /*============================================================================================*/
  
  public  void createNotificationService(Context context) {
    DebugMode.logD(TAG, "Create notification service");
    if (isInCastMode()) {
      notificationServiceIsActivated = true;
      if (castPlayer.getState() == State.PLAYING) {
        buildNotificationService(context, false);
      } else {
        buildNotificationService(context, true);
      }
      registerBroadcastReceiver(context);
    }
  }
  
  public void destroyNotificationService(Context context) {
    DebugMode.logD(TAG, "Destroy notification service");
    if (notificationServiceIsActivated) {
      NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      mNotifyMgr.cancel(notificationID);
      unregisterBroadcastReceiver(context);
      notificationServiceIsActivated = false;
      notificationBuilder = null;
    }
  }
  
  private void registerBroadcastReceiver(Context context) {
    DebugMode.logD(TAG, "Register notification broadcast receiver");
    IntentFilter filter = new IntentFilter();
    filter.addAction(ACTION_PLAY);
    filter.addAction(ACTION_STOP);
    
    if (receiver == null) {
      receiver = new android.content.BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
         DebugMode.logD("TAG", "Play/Pause button is clicked in notification service");
          if (isInCastMode()) {
            String action = intent.getAction();
            if (action.equals(ACTION_STOP)) {
              exitCastMode();
              dataCastManager.disconnect();
              destroyNotificationService(context);
            } else if (action.equals(ACTION_PLAY)) {
              if (castPlayer.getState() == State.PLAYING) {
                castPlayer.pause();
              } else if (castPlayer.getState() == State.PAUSED || castPlayer.getState() == State.READY) {
                castPlayer.play();
              }
            }
          }
        }
      };
    }
    context.registerReceiver(receiver, filter);
  }
  
  private void unregisterBroadcastReceiver(Context context) {
    DebugMode.logD(TAG, "Unregister broadcast receiver");
    if (receiver != null) {
      try {
        context.unregisterReceiver(receiver);
      } catch (IllegalArgumentException e) {
        DebugMode.logD(TAG,"epicReciver is already unregistered");
      }
      receiver = null;
    }
  }
  
  private void buildNotificationService(Context context, boolean shouldDisplayPlayButton) {
    // Create notification View
    RemoteViews notificationView = new RemoteViews(context.getPackageName(), CastManager.notificationMiniControllerResourceId);
    notificationView.setTextViewText(R.id.OOTitleView, getCastPlayer().getCastItemTitle());
    notificationView.setTextViewText(R.id.OOSubtitleView, dataCastManager.getDeviceName());
    notificationView.setImageViewBitmap(R.id.OOIconView, getCastPlayer().getCastImageBitmap());

    if (shouldDisplayPlayButton) {
      notificationView.setImageViewBitmap(R.id.OOPlayPauseView, CastUtils.getLightChromecastPlayButton());
    } else {
      notificationView.setImageViewBitmap(R.id.OOPlayPauseView, CastUtils.getLightChromecastPauseButton());
    }

    // Set the result intent so the user can navigate to the target activity by clicking the notification view
    Intent resultIntent = new Intent(context, targetActivity);
    resultIntent.putExtra("embedcode", castPlayer.getEmbedCode());

    // Build the stack for PendingIntent to make sure the user can navigate back to the parent acitvity
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    stackBuilder.addParentStack(targetActivity);
    stackBuilder.addNextIntent(resultIntent);

    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    if (notificationBuilder == null) {
      notificationBuilder = new NotificationCompat.Builder(context).
          setSmallIcon(notificationImageResourceId).
          setOnlyAlertOnce(true).
          setAutoCancel(false).
          setOngoing(false);
    }
    
    notificationBuilder.setContentIntent(resultPendingIntent);
    
    Notification notification = notificationBuilder.build();
    notification.contentView = notificationView;
    
    Intent switchIntent = new Intent(ACTION_PLAY);
    PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(context, 100, switchIntent, 0);
    notificationView.setOnClickPendingIntent(R.id.OOPlayPauseView, pendingSwitchIntent);

    Intent stopIntent = new Intent(ACTION_STOP);
    PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 100, stopIntent, 0);
    notificationView.setOnClickPendingIntent(R.id.OORemoveView, stopPendingIntent);
    
    
    NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notifyMgr.notify(notificationID, notification);
  }
  
  
  /*============================================================================================*/
  /*========== Lock Screen Controller ==========================================================*/
  /*============================================================================================*/
  
  public void registerLockScreenControls(Context context) {
    if (isInCastMode()) {
      DebugMode.logD(TAG, "Register Lock Screen Mini controller");
      audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
      audioManager.requestAudioFocus(
          new OnAudioFocusChangeListener() {
              public void onAudioFocusChange(int focusChange) {}
          },
          // Use the music stream.
          AudioManager.STREAM_MUSIC,
          // Request permanent focus.
          AudioManager.AUDIOFOCUS_GAIN);

      ComponentName myEventReceiver = new ComponentName(context, CastBroadcastReceiver.class);
      audioManager.registerMediaButtonEventReceiver(myEventReceiver);
      if (remoteControlClient == null) {
          Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
          intent.setComponent(myEventReceiver);
          remoteControlClient = new RemoteControlClient(PendingIntent.getBroadcast(context, 0, intent, 0)); // grant the remoteControlClient the right to perform broadcast
          audioManager.registerRemoteControlClient(remoteControlClient);
      }
      remoteControlClient.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PAUSE);
      remoteControlClient.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY);
      if (castPlayer.getState() == State.PLAYING) {
        remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
      } else {
        remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
      }
      remoteControlClient
      .editMetadata(true)
      .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, dataCastManager.getDeviceName())
      .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, castPlayer.getCastItemTitle())
      .putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, castPlayer.getCastImageBitmap())
      .apply();
    }
  }
  
  public void unregisterLockScreenControls() {
    DebugMode.logD(TAG, "Unregister lock screen controls");
    if (audioManager != null) {
      audioManager.unregisterRemoteControlClient(remoteControlClient);
      audioManager = null;
    }
    remoteControlClient = null;
  }
  
  public void updateNotificationAndLockScreenPlayPauseButton() {
    DebugMode.logD( TAG, "Update Lock Screen mini controller play/pause button status" );
    if (isInCastMode() && notificationServiceIsActivated) {
      if (castPlayer.getState() == State.PLAYING) {
        if (remoteControlClient != null) {
          remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        }
        buildNotificationService(currentContext, false);
      } else {
        if (remoteControlClient != null) { 
          remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
        }
        buildNotificationService(currentContext, true);
      }
    }
  }

  @Override
  public void onFailed( int resourceId, int statusCode ) {

  }
}


