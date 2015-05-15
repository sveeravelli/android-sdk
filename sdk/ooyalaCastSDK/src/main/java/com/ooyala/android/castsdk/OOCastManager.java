package com.ooyala.android.castsdk;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
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
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.sample.castcompanionlibrary.cast.DataCastManager;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.ooyala.android.CastManager;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.util.DebugMode;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGE;

public class OOCastManager extends DataCastManager implements CastManager {
  private static final String TAG = "CastManager";

  public static final String ACTION_PLAY = "OOCastPlay";
  public static final String ACTION_STOP = "OOCastStop";
  private static OOCastManager castManager;
  private static Class<?> targetActivity;
  private static Class<?> currentActivity;
  private static Context currentContext;
  private static String namespace;
  private static int notificationMiniControllerResourceId;
  private static int notificationImageResourceId;

  private NotificationCompat.Builder notificationBuilder;
  private BroadcastReceiver receiver;
  private AudioManager audioManager;
  private RemoteControlClient remoteControlClient;
  private int notificationID = 001;
  private Bitmap miniControllerDefaultImageBitmap;

  private View castView;
  private WeakReference<OoyalaPlayer> ooyalaPlayer;
  private OOCastPlayer castPlayer;
  private Set<OOMiniController> miniControllers;
  private boolean notificationServiceIsActivated;
  private boolean isConnectedToReceiverApp;
  private boolean isPlayerSeekable = true;
  private boolean isInCastMode;

  public static OOCastManager initialize(Context context, String applicationId, String namespace) {
    String[] namespaces = {namespace};
    notificationMiniControllerResourceId = R.layout.oo_default_notification;
    notificationImageResourceId = R.drawable.ic_ooyala;
    return OOCastManager.initialize(context, applicationId, namespaces);
  }
  
  public static OOCastManager initialize(Context context, String applicationId, String... namespaces) {
    DebugMode.logD(TAG, "Init OOCastManager with appId = " + applicationId + ", namespace = " + namespaces);
    if (null == castManager) {
        DebugMode.logD(TAG, "Create a new OOCastManager");
        if (ConnectionResult.SUCCESS != GooglePlayServicesUtil.isGooglePlayServicesAvailable(context)) {
            String msg = "Couldn't find the appropriate version of Google Play Services";
            LOGE(TAG, msg);
            throw new RuntimeException(msg);
        }
        castManager = new OOCastManager(context, applicationId, namespaces);
        mCastManager = castManager; // mCastManager is used when BaseCastManarger.getCastManager() called
    }
    return castManager;
  }
  
  public static OOCastManager getCastManager() {
    return castManager;
  }
  
  protected OOCastManager(Context context, String applicationId, String[] namespaces) {
    super(context, applicationId, namespaces);
    OOCastManager.namespace = namespaces[0];
  }

  public void destroy(Context context) {
    DebugMode.logD(TAG, "destroy OOCastManager");
    hideCastView();
    destroyCastPlayer();
    destroyNotificationService(context);
    unregisterLockScreenControls();
    unregisterBroadcastReceiver(context);
    destroyAllFeilds();
  }

  private void destroyAllFeilds() {
    castView = null;
    ooyalaPlayer = null;
    miniControllerDefaultImageBitmap = null;
    miniControllers = null;
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
    activity.getMenuInflater().inflate(R.menu.main, menu);
    MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
    MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider)
            MenuItemCompat.getActionProvider(mediaRouteMenuItem);
    mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
    mediaRouteActionProvider.setDialogFactory(new OOMediaRouteDialogFactory(this));
  }

  public void setCastView(View view) {
    DebugMode.assertCondition(view != null, TAG, "cannot set castView to null");
    DebugMode.logD(TAG, "Set cast view to " + view);
    castView = view;
  }

  public View getCastView() {
    return castView;
  }

  public void setNotificationImageResourceId(int resourceId) {
    notificationImageResourceId = resourceId;
  }
  
  public void setDefaultMiniControllerImageBitmap(Bitmap imageBitmap) {
    miniControllerDefaultImageBitmap = imageBitmap;
  }

  public void setNotificationMiniControllerLayout(int recourceId) {
    notificationMiniControllerResourceId = recourceId;
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
    DebugMode.logD(TAG, "Disconnect from ooyalaPlayer " + ooyalaPlayer);
    if (isInCastMode()) {
      castPlayer.disconnectFromCurrentOoyalaPlayer();
    }
    ooyalaPlayer = null;
  }

  /*============================================================================================*/
  /*========== Access CastManager Status Or Fields =============================================*/
  /*============================================================================================*/

  public Bitmap getDefaultMiniControllerImageBitmap() {
    return miniControllerDefaultImageBitmap;
  }

  public OOCastPlayer getCastPlayer() {
    return this.castPlayer;
  }

  public boolean isConnectedToReceiverApp() {
    return this.isConnected() && this.isConnectedToReceiverApp;
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
  /*========== BaseCastManager =================================================================*/
  /*============================================================================================*/


  @Override
  public void onApplicationConnected(ApplicationMetadata appMetadata, String applicationStatus,
          String sessionId, boolean wasLaunched) {
    DebugMode.logD(TAG, "onApplicationConnected called");
    super.onApplicationConnected(appMetadata, applicationStatus, sessionId, wasLaunched);
    this.isConnectedToReceiverApp = true;
    this.castPlayer = createNewCastPlayer();
    if (ooyalaPlayer != null && ooyalaPlayer.get().getCurrentItem() != null) {
      ooyalaPlayer.get().switchToCastMode(ooyalaPlayer.get().getEmbedCode());
    }
  }

  private OOCastPlayer createNewCastPlayer() {
    DebugMode.logD(TAG, "Create new CastPlayer");
    return new OOCastPlayer(this);
  }
  
  @Override
  public void onApplicationDisconnected(int errorCode) {
    DebugMode.logD(TAG, "onApplicationDisconnected called");
    super.onApplicationDisconnected(errorCode);
    this.isConnectedToReceiverApp = false;
    if (isInCastMode()) {
      exitCastMode();
    }
  }
  
  @Override
  public void disconnectDevice(boolean stopAppOnExit, boolean clearPersistedConnectionData,
      boolean setDefaultRoute) {
    DebugMode.logD(TAG, "disconnectDevice called");
    super.disconnectDevice(stopAppOnExit, clearPersistedConnectionData, setDefaultRoute);
    if (isInCastMode()) {
      exitCastMode();
    }
  }

  public boolean isInCastMode() {
    return isInCastMode;
  }

  public void enterCastMode(String embedCode, int playheadTimeInMillis, boolean isPlaying) {
    DebugMode.assertCondition(ooyalaPlayer != null, TAG, "ooyalaPlayer should be not null while entering cast mode");
    DebugMode.assertCondition(castPlayer != null, TAG, "castPlayer should be not null while entering cast mode");
    initCastPlayer(embedCode, playheadTimeInMillis, isPlaying);
    displayCastView();
    isInCastMode = true;
  }

  private void initCastPlayer(String embedCode, int playheadTimeInMillis, boolean isPlaying) {
    castPlayer.setSeekable(isPlayerSeekable);
    castPlayer.setOoyalaPlayer(ooyalaPlayer.get());
    castPlayer.updateMetadataFromOoyalaPlayer(ooyalaPlayer.get());
    castPlayer.enterCastMode(embedCode, playheadTimeInMillis, isPlaying);
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
   super.sendDataMessage(message, OOCastManager.namespace);
  }

  @Override
  public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
    DebugMode.assertCondition(castPlayer != null, TAG, "castPlayer cannot be null");
    if (castPlayer != null) {
      castPlayer.receivedMessage(message);
    }
  }
  
  /*============================================================================================*/
  /*========== MiniController ==================================================================*/
  /*============================================================================================*/

  public void addMiniController(OOMiniController miniController) {
    DebugMode.logD(TAG, "Add mini controller " + miniController);
    if (miniControllers == null) {
      miniControllers = new HashSet<OOMiniController>();
    }
    miniControllers.add(miniController);
    miniController.setCastManager(castManager);
  }

  public void updateMiniControllersState() {
    DebugMode.logD(TAG, "Update mini controllers state");
    if (miniControllers != null &&  isInCastMode()) {
      if (castPlayer.getState() == State.COMPLETED) {
        dismissMiniControllers();
      } else {
        for (OOMiniController miniController : miniControllers) {
          miniController.updatePlayPauseButtonImage(castPlayer.getState() == State.PLAYING);
        }
      }
    }
  }
  
  public void removeMiniController(OOMiniController miniController) {
    DebugMode.logD(TAG, "Remove mini controller " + miniController);
    miniControllers.remove(miniController);
  }
  
  private void removeAllMiniControllers() {
    DebugMode.logD(TAG, "Remove all mini controllers");
    miniControllers.clear();
  }
  
  private void dismissMiniControllers() {
    DebugMode.logD(TAG, "dismiss mini controllers");
    if (miniControllers != null) {
      for (OOMiniController miniController : miniControllers) {
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
      receiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
         DebugMode.logD("TAG", "Play/Pause button is clicked in notification service");
          if (isInCastMode()) {
            String action = intent.getAction();
            if (action.equals(ACTION_STOP)) {
              disconnect();
              exitCastMode();
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
    RemoteViews notificationView = new RemoteViews(context.getPackageName(), OOCastManager.notificationMiniControllerResourceId);
    notificationView.setTextViewText(R.id.OOTitleView, getCastPlayer().getCastItemTitle());
    notificationView.setTextViewText(R.id.OOSubtitleView, getDeviceName());
    notificationView.setImageViewBitmap(R.id.OOIconView, getCastPlayer().getCastImageBitmap());

    if (shouldDisplayPlayButton) {
      notificationView.setImageViewBitmap(R.id.OOPlayPauseView, OOCastUtils.getLightChromecastPlayButton());
    } else {
      notificationView.setImageViewBitmap(R.id.OOPlayPauseView, OOCastUtils.getLightChromecastPauseButton());
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

      ComponentName myEventReceiver = new ComponentName(context, OOBroadcastReceiver.class);
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
      .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, getDeviceName())
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
    DebugMode.logD(TAG, "Update Lock Screen mini controller play/pause button status");
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
}

































