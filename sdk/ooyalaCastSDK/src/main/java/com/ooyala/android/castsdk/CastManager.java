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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.cast.player.VideoCastController;
import com.ooyala.android.CastManagerInterface;
import com.ooyala.android.CastModeOptions;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.util.DebugMode;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CastManager implements CastManagerInterface {

  public static final class CastManagerInitializationException extends Exception {
      public CastManagerInitializationException( String message ) {
        super( message );
      }
    public CastManagerInitializationException( Throwable cause ) {
      super( cause );
    }
  }

  private class VideoCastListener extends VideoCastConsumerImpl {
    @Override
    public void onApplicationConnected(ApplicationMetadata appMetadata,
                                       String sessionId, boolean wasLaunched) {
      DebugMode.logD(TAG, "onApplicationConnected sessionId"+ sessionId);
      castPlayer = createNewCastPlayer();
      if (ooyalaPlayer != null && ooyalaPlayer.get().getCurrentItem() != null) {
        ooyalaPlayer.get().switchToCastMode(ooyalaPlayer.get().getEmbedCode());
      }
    }

    @Override
    public void onApplicationDisconnected(int errorCode) {
      DebugMode.logD( TAG, "onApplicationDisconnected called" );
      if (isActivelyCastingContent()) {
        cleanupAfterReceiverDisconnect();
      }
    }

    @Override
    public void onDataMessageReceived(String message) {
      DebugMode.logD(TAG, "onDataMessageReceived: " + message);
      if (castPlayer != null) {
        castPlayer.receivedMessage(message);
      }
    }

    @Override
    public void onDisconnected() {
      DebugMode.logD(TAG, "onDisconnected called");
      if (isInCastMode()) {
        cleanupAfterReceiverDisconnect();
      }
    }

    @Override
    public void onMediaLoadResult(int statusCode) {
      DebugMode.logD(TAG, "onMediaLoadResults:"+statusCode);
      try {
        castManager.getVideoCastManager().play();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private static final String TAG = CastManager.class.getSimpleName();

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
  private boolean isPlayerSeekable = true;
  private boolean isInCastMode;

  private final VideoCastManager videoCastManager;
  private final String namespace;
  private VideoCastListener videoCastListener;

  /**
   * Set up the CastManager singleton.
   *
   * @param context       an Android context. Non-null.
   * @param applicationId the unique ID for your application
   * @param namespace     is the single namespace to be set up for this class.
   * @return the CastManager singleton.
   * @throws CastManagerInitializationException if initialization fails.
   */
  public static CastManager initialize(Context context, String applicationId, String namespace) throws CastManagerInitializationException {
    DebugMode.assertCondition( castManager == null, TAG, "Cannot re-initialize" );
    if( castManager == null ) {
      notificationMiniControllerResourceId = R.layout.oo_default_notification;
      notificationImageResourceId = R.drawable.ic_ooyala;
      DebugMode.logD(TAG, "Init new CastManager with appId = " + applicationId + ", namespace = " + namespace);
      requireGooglePlayServices(context);
      try {
        DebugMode.logD(TAG, "Initialize VideoCastManager");
        VideoCastManager.initialize(context, applicationId, null, namespace).enableFeatures(
            VideoCastManager.FEATURE_LOCKSCREEN |
                VideoCastManager.FEATURE_WIFI_RECONNECT |
                VideoCastManager.FEATURE_AUTO_RECONNECT |
                VideoCastManager.FEATURE_CAPTIONS_PREFERENCE |
                VideoCastManager.FEATURE_DEBUGGING);
        // this is the default behavior but is mentioned to make it clear that it is configurable.
        VideoCastManager.getInstance().setNextPreviousVisibilityPolicy(
            VideoCastController.NEXT_PREV_VISIBILITY_POLICY_DISABLED);

        // this is to set the launch options, the following values are the default values
        VideoCastManager.getInstance().setLaunchOptions(false, Locale.getDefault());

        // this is the default behavior but is mentioned to make it clear that it is configurable.
        VideoCastManager.getInstance().setCastControllerImmersive(true);

        castManager = new CastManager( namespace);
      }
      catch( Exception e ) {
        throw new CastManagerInitializationException( e );
      }
    }
    return castManager;
  }

  private static void requireGooglePlayServices( Context context ) throws CastManagerInitializationException {
    final int gpsAvailableCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
    if( ConnectionResult.SUCCESS != gpsAvailableCode ) {
      String msg = "Couldn't find the appropriate version of Google Play Services (code " + gpsAvailableCode + ")";
      DebugMode.logE( TAG, msg );
      throw new CastManagerInitializationException( msg );
    }
  }

  /**
   * @return the CastManager singleton. Possibly null.
   * @see #initialize(android.content.Context, String, String)
   */
  public static CastManager getCastManager() {
    return castManager;
  }
  
  private CastManager( String namespace ) {
    this.videoCastManager = VideoCastManager.getInstance();
    this.namespace = namespace; // there's no accessor for namespaces on DataCastManager.
    this.videoCastListener = new VideoCastListener();
    this.videoCastManager.addVideoCastConsumer(this.videoCastListener);
  }

  /**
   * Get the DataCastManager, only to be used for calling methods that we have not already wrapped in CastManager.
   * @return the DataCastManager being wrapped.
   */
  public VideoCastManager getVideoCastManager() {
    return this.videoCastManager;
  }

  /*============================================================================================*/
  /*========== CastManager App Setting API   ===================================================*/
  /*============================================================================================*/

  /**
   * Add UI for casting options into a pre-existing menu.
   *
   * @param activity third party application's Activity desiring a cast button.
   * @param menu     into which to add the cast button. A selection of available
   *                 receivers will pop up after clicking on the cast button.
   */
  public void addCastButton(Activity activity, Menu menu) {
    DebugMode.logD(TAG, "Add Cast Button");
    this.setCurrentContext(activity);
    activity.getMenuInflater().inflate(R.menu.main, menu);
    MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
    MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider)
            MenuItemCompat.getActionProvider(mediaRouteMenuItem);
    mediaRouteActionProvider.setRouteSelector(videoCastManager.getMediaRouteSelector());
    mediaRouteActionProvider.setDialogFactory(new CastMediaRouteDialogFactory());
  }
  
  /**
   * * @param view to display in the video area while casting. Possibly null.
   *
   * @see #getCastView()
   */
  public void setCastView(View view) {
    DebugMode.assertCondition( view != null, TAG, "cannot set castView to null" );
    DebugMode.logD( TAG, "Set cast view to " + view );
    castView = view;
  }

  /**
   * @return the currently associated View. Possibly null.
   * @see #setCastView(android.view.View)
   */
  public View getCastView() {
    return castView;
  }

  /**
   * Icon to use for Android Notifications.
   *
   * @param resourceId for looking up the notification small icon image.
   */
  public void setNotificationImageResourceId(int resourceId) {
    DebugMode.logD(TAG, "Set notification image resource id = " + resourceId);
    notificationImageResourceId = resourceId;
  }

  /**
   * What to show in the mini controller when there's no thumbnail for the video asset.
   *
   * @param imageBitmap to show in the mini controller. Should not be null.
   * @see #getDefaultMiniControllerImageBitmap()
   */
  public void setDefaultMiniControllerImageBitmap(Bitmap imageBitmap) {
    DebugMode.logD(TAG, "Set mini controller image bitmap = " + imageBitmap);
    miniControllerDefaultImageBitmap = imageBitmap;
  }

  /**
   * @param resourceId for looking up the notification mini controller view layout.
   */
  public void setNotificationMiniControllerLayout(int resourceId) {
    DebugMode.logD(TAG, "Set notification mini controller layout = " + resourceId);
    notificationMiniControllerResourceId = resourceId;
  }

  /**
   * @param isSeekable true allows seek operations, false denies them.
   */
  public void setCastPlayerSeekable(boolean isSeekable) {
    isPlayerSeekable = isSeekable;
  }

  /**
   * Connect the CastManager to the OoyalaPlayer instance.
   * Must be called before setting the embed code of the video asset on the OoyalaPlayer instance.
   *
   * @param ooyalaPlayer must not be null.
   * @see #deregisterFromOoyalaPlayer()
   */
  public void registerWithOoyalaPlayer(OoyalaPlayer ooyalaPlayer) {
    DebugMode.assertCondition( ooyalaPlayer != null, TAG, "OoyalaPlayer must be non-null" );
    DebugMode.logD(TAG, "Connect to OoyalaPlayer " + ooyalaPlayer);
    this.ooyalaPlayer = new WeakReference<OoyalaPlayer>(ooyalaPlayer);
    ooyalaPlayer.registerCastManager(this);
  }

  /**
   * Disconnect the currently registered OoyalaPlayer from the OOCastManager.
   * The Android Activity making use of Ooyala Casting should call this
   * appropriately e.g. during its own tear-down.
   *
   * @see #registerWithOoyalaPlayer(com.ooyala.android.OoyalaPlayer)
   */
  public void deregisterFromOoyalaPlayer() {
    DebugMode.logD( TAG, "Disconnect from ooyalaPlayer " + ooyalaPlayer );
    this.ooyalaPlayer = null;
    if( isActivelyCastingContent() ) {
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

  /**
   * @return any previously registered bitmap, otherwise null.
   * @see #setDefaultMiniControllerImageBitmap(android.graphics.Bitmap)
   */
  public Bitmap getDefaultMiniControllerImageBitmap() {
    return miniControllerDefaultImageBitmap;
  }

  /**
   * For interacting with the cast playback, even when there is no OoyalaPlayer.
   *
   * @return the current CastPlayer. Possibly null.
   * @return the current CastPlayer. Possibly null.
   * @return the current CastPlayer. Possibly null.
   */
  public CastPlayer getCastPlayer() {
    return this.castPlayer;
  }

  /**
   * @return true if the CastManager is connected to a receiver app (e.g. on a networked Chromecast box),
   * even if casting has not actually begun.
   */
  public boolean isConnectedToReceiverApp() {
    return this.videoCastManager.isConnected();
  }
  
  /**
   * @return true if the CastManager is casting content to a receiver.
   */
  public boolean isActivelyCastingContent() {
    return isInCastMode;
  }

  /**
   * The Android Activity making use of Ooyala Casting must call this
   * in its own Application.onResume().
   */
  public void onResume( Context context ) {
    DebugMode.logD(TAG, "onResume()");
    setCurrentContext( context );
    updateMiniControllers();
  }

  /**
   * Removes castView from the OoyalaPlayer, for use after disconnecting from the receiver.
   */
  private void hideCastView() {
    DebugMode.logD(TAG, "Hide cast view");
    if( ooyalaPlayer != null && ooyalaPlayer.get().getLayout().getChildCount() != 0 && castView != null ) {
      ooyalaPlayer.get().getLayout().removeView(castView);
    }
  }

  /**
   * Called after we establish casting to a receiver.
   */
  private void displayCastView() {
    DebugMode.logD(TAG, "CastView = " + castView);
    if (ooyalaPlayer != null && castView != null) {
      if (castView.getParent() != null) {
        ((ViewGroup) castView.getParent()).removeView(castView);
      }
      ooyalaPlayer.get().getLayout().addView(castView);
    }
  }

  /**
   * Called when exiting cast mode.
   */
  private void destroyCastPlayer() {
    DebugMode.logD(TAG, "Destroy current CastPlayer");
    castPlayer.destroy();
    castPlayer = null;
  }

  private CastPlayer createNewCastPlayer() {
    DebugMode.logD(TAG, "Create new CastPlayer");
    return new CastPlayer(this);
  }

  /*============================================================================================*/
  /*========== Consumer callbacks =================================================================*/
  /*============================================================================================*/

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

  /*package private on purpose*/ void initCastPlayer(CastModeOptions options, String embedToken) {
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

  private void cleanupAfterReceiverDisconnect() {
    DebugMode.logD(TAG, "Exit Cast Mode");
    isInCastMode = false;
    hideCastView();
    DebugMode.assertCondition(castPlayer != null, TAG, "castPlayer cannot be null when exit cast mode");
    if (ooyalaPlayer != null) {
      ooyalaPlayer.get().exitCastMode(castPlayer.currentTime(), castPlayer.getState() == State.PLAYING, castPlayer.getEmbedCode());
    }
    destroyCastPlayer();
    dismissMiniControllers();
    removeAllMiniControllers();
  }
  
  /*============================================================================================*/
  /*========== MiniController ==================================================================*/
  /*============================================================================================*/

  /**
   * @param miniController to associate with this CastManager.
   */
  public void addMiniController(CastMiniController miniController) {
    DebugMode.logD(TAG, "Add mini controller " + miniController);
    if (miniControllers == null) {
      miniControllers = new HashSet<CastMiniController>();
    }
    if (!miniControllers.contains(miniController)) {
      miniControllers.add(miniController);
    }
  }

  /**
   * Tell all associated CastMiniControllers to update.
   */
  public void updateMiniControllers() {
    DebugMode.logD(TAG, "Update mini controllers state");
    if( miniControllers != null && isActivelyCastingContent() ) {
      if (castPlayer.getState() == State.COMPLETED) {
        dismissMiniControllers();
      } else {
        for (CastMiniController miniController : miniControllers) {
          miniController.updatePlayPauseButtonImage(castPlayer.getState() == State.PLAYING);
        }
      }
    }
  }
  
  /**
   * @param miniController to be disassociated from this CastManager.
   */
  public void removeMiniController(CastMiniController miniController) {
    DebugMode.logD(TAG, "Remove mini controller " + miniController);
    if (miniControllers != null) {
      miniControllers.remove(miniController);
    }
  }
  
  /**
   * Disassociate all previously associated CastMiniControllers.
   *
   * @see #removeMiniController(CastMiniController)
   */
  private void removeAllMiniControllers() {
    DebugMode.logD(TAG, "Remove all mini controllers");
    if (miniControllers != null) {
      miniControllers.clear();
    }
  }
  
  /**
   * Tell all associated CastMiniControllers to dismiss.
   */
  private void dismissMiniControllers() {
    DebugMode.logD(TAG, "dismiss mini controllers");
    if (miniControllers != null) {
      for (CastMiniController miniController : miniControllers) {
        miniController.dismiss();
      }
    }
  }
  
  /**
   * Should be called by the Activity in onCreate().
   *
   * @param targetActivity to be resumed (probably the Activity calling this method)
   *                       when the CastMiniController container is clicked. Should not be null.
   */
  public void setTargetActivity(Class<?> targetActivity) {
    this.targetActivity = targetActivity;
  }
  
  /**
   * @return previously registered target activity. Possibly null.
   */
  public Class<?> getTargetActivity() {
    return targetActivity;
  }

  private void setCurrentContext(Context context) {
    currentActivity = context.getClass();
    currentContext = context;
  }

  /*package private on purpose*/ Class<?> getCurrentActivity() {
    return currentActivity;
  }
  
  
  /*============================================================================================*/
  /*==========  Notification Service ============================================================*/
  /*============================================================================================*/
  
  /**
   * Enables reacting to mini controllers in the Notification area of the Android UI.
   * The app Activity should call this to add the Notification mini controller support e.g. in onSuspend().
   * Requires setTargetActivity to have previously been called with a valid target.
   *
   * @param context an Android context. Non-null.
   * @see #setTargetActivity(Class)
   */
  public  void createNotificationService(Context context) {
    DebugMode.logD(TAG, "Create notification service");
    if( isActivelyCastingContent() ) {
      notificationServiceIsActivated = true;
      if (castPlayer.getState() == State.PLAYING) {
        buildNotificationService(context, false);
      } else {
        buildNotificationService(context, true);
      }
      registerBroadcastReceiver(context);
    }
  }
  
  /**
   * The app Actvity should call this to remove the Notification mini
   * controller support e.g. in onResume().
   * @param context an Android context. Non-null.
   */
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
         DebugMode.logD(TAG, "Play/Pause button is clicked in notification service");
          if( isActivelyCastingContent() ) {
            String action = intent.getAction();
            if (action.equals(ACTION_STOP)) {
              cleanupAfterReceiverDisconnect();
              videoCastManager.disconnect();
              destroyNotificationService(context);
            } else if (action.equals(ACTION_PLAY)) {
              if (castPlayer.getState() == State.PLAYING) {
                castPlayer.pause();
              } else if (castPlayer.getState() == State.PAUSED || castPlayer.getState() == State.READY || castPlayer.getState() == State.LOADING || castPlayer.getState() == State.COMPLETED) {
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
    notificationView.setTextViewText(R.id.OOSubtitleView, videoCastManager.getDeviceName());
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
  
  /**
   * Set up controls to appear on the lock screen.
   * The application should call this to add the lock screen mini
   * controller support e.g. in onResume().
   *
   * @param context an Android context. Non-null.
   */
  public void registerLockScreenControls(Context context) {
    if( isActivelyCastingContent() ) {
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
      .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, videoCastManager.getDeviceName())
      .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, castPlayer.getCastItemTitle())
      .putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, castPlayer.getCastImageBitmap())
      .apply();
    }
  }
  
  /**
   * Remove any casting controls from the lock screen.
   */
  public void unregisterLockScreenControls() {
    DebugMode.logD(TAG, "Unregister lock screen controls");
    if (audioManager != null) {
      audioManager.unregisterRemoteControlClient(remoteControlClient);
      audioManager = null;
    }
    remoteControlClient = null;
  }
  
  /*package private on purpose*/ void updateNotificationAndLockScreenPlayPauseButton() {
    DebugMode.logD( TAG, "Update Lock Screen mini controller play/pause button status" );
    if (isActivelyCastingContent() && notificationServiceIsActivated) {
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
}
