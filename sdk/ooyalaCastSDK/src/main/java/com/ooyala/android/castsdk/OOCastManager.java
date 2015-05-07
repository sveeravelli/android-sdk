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
import android.graphics.Color;
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
import java.util.HashSet;
import java.util.Set;

import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGE;

public class OOCastManager extends DataCastManager implements CastManager {
  private static final String TAG = "CastManager";
  
  private static OOCastManager castManager;
  private static Class<?> targetActivity;
  private static Class<?> currentActivity;
  private static Context currentContext;
  public static final String ACTION_PLAY = "OOCastPlay";
  public static final String ACTION_STOP = "OOCastStop";
  private static NotificationCompat.Builder notificationBuilder;

  private static BroadcastReceiver receiver;
  private static AudioManager audioManager;
  private static RemoteControlClient remoteControlClient;
  private final int notificationReceiverID = 001;

  
  private String namespace;
  private View castView;
  private int notificationImageResourceId = -1;
  private Bitmap miniControllerImageBitmap;
  private OoyalaPlayer ooyalaPlayer;
  private OOCastPlayer castPlayer;
  private Set<OOMiniController> miniControllers;
  private int notificationMiniControllerResourceId = -1;
  private boolean notificationServiceIsActivated;
  private boolean isConnectedToReceiverApp;
  private boolean isPlayerSeekable = true;
  
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
    OOCastManager.currentActivity = context.getClass();
    currentContext = context;
    return castManager;
  }
  
  public static OOCastManager getCastManager() {
    return castManager;
  }
  
  protected OOCastManager(Context context, String applicationId, String[] namespaces) {
    super(context, applicationId, namespaces);
    namespace = namespaces[0];
  }

  public void destroy(Context context) {
    DebugMode.logD(TAG, "destroy OOCastManager");
    clearCastView();
    destroyCastPlayer();
    destroyNotificationService(context);
    unregisterLockScreenControls();
    unregisterBroadcastReceiver(context);
    destroyAllFeilds();
  }

  private void destroyAllFeilds() {
    castView = null;
    ooyalaPlayer = null;
    miniControllerImageBitmap = null;
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

  public void setNotificationImageResourceId(int resourceId) {
    this.notificationImageResourceId = resourceId;
  }
  
  public void setDefaultMiniControllerImageBitmap(Bitmap imageBitmap) {
    miniControllerImageBitmap = imageBitmap;
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
    this.ooyalaPlayer = ooyalaPlayer;
    ooyalaPlayer.registerCastManager(this);
  }

  /**
   * Disconnect the current OoyalaPlayer from the OOCastManager
   * Called from App level when right before the connected OoyalaPlayer is destroyed
   */
  public void deregisterOoyalaPlayer() {
    DebugMode.logD(TAG, "Disconnect from ooyalaPlayer " + ooyalaPlayer);
    if (castPlayer != null) {
      castPlayer.disconnectFromCurrentOoyalaPlayer();
    }
    ooyalaPlayer = null;
  }

  /*============================================================================================*/
  /*========== Access CastManager Status Or Fields =============================================*/
  /*============================================================================================*/

  public Bitmap getDefaultMiniControllerImageBitmap() {
    return miniControllerImageBitmap;
  }

  public OOCastPlayer getCastPlayer() {
    return this.castPlayer;
  }

  public boolean isConnectedToReceiverApp() {
    return this.isConnectedToReceiverApp;
  }

  /**
   * Update the mini controllers when activity with mini controllers resumes
   */
  public void onResume() {
    DebugMode.logD(TAG, "onResume()");
    updateMiniControllersState();
  }

  private void clearCastView() {
    DebugMode.logD(TAG, "Clear cast view");
    if (castPlayer != null) {
      castPlayer.clearCastView();
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
    if (ooyalaPlayer != null && ooyalaPlayer.getCurrentItem() != null) {
      ooyalaPlayer.switchToCastMode(ooyalaPlayer.getEmbedCode());
    }
  }

  private OOCastPlayer createNewCastPlayer() {
    DebugMode.logD(TAG, "Create new CastPlayer");
    DebugMode.assertCondition(ooyalaPlayer != null, TAG, "ooyalaPlayer should be not null while entering cast mode");
    return new OOCastPlayer(this, ooyalaPlayer);
  }
  
  @Override
  public void onApplicationDisconnected(int errorCode) {
    DebugMode.logD(TAG, "onApplicationDisconnected called");
    super.onApplicationDisconnected(errorCode);
    this.isConnectedToReceiverApp = false;
    if (castPlayer != null) {
      exitCastMode();
    }
  }
  
  @Override
  public void disconnectDevice(boolean stopAppOnExit, boolean clearPersistedConnectionData,
      boolean setDefaultRoute) {
    DebugMode.logD(TAG, "disconnectDevice called");
    super.disconnectDevice(stopAppOnExit, clearPersistedConnectionData, setDefaultRoute);
    if (castPlayer != null) {
      exitCastMode();
    }
  }

  public boolean isInCastMode() {
    return (castPlayer != null);
  }


  public void enterCastMode(String embedCode, int playheadTimeInMillis, boolean isPlaying) {
    this.castPlayer = createNewCastPlayer();
    castPlayer.setCastView(castView);
    castPlayer.setSeekable(isPlayerSeekable);
    castPlayer.enterCastMode(embedCode, playheadTimeInMillis, isPlaying);
  }
  
  private void exitCastMode() {
    DebugMode.logD(TAG, "Exit Cast Mode");
    clearCastView();
    DebugMode.assertCondition(castPlayer != null, TAG, "castPlayer cannot be null");
    if (ooyalaPlayer != null) {
      ooyalaPlayer.exitCastMode(castPlayer.currentTime(), castPlayer.getState() == State.PLAYING, castPlayer.getEmbedCode());
    }
    dismissMiniControllers();
    destroyCastPlayer();
    removeAllMiniControllers();
  }
  
  public void sendDataMessage(String message) throws IllegalArgumentException, IllegalStateException, IOException,
      TransientNetworkDisconnectionException, NoConnectionException {
   super.sendDataMessage(message, namespace);
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
  
  public void setNotificationMiniControllerLayout(int recourceId) {
    notificationMiniControllerResourceId = recourceId;
  }
  
  public void addMiniController(OOMiniController miniController) {
    DebugMode.logD(TAG, "Add mini controller " + miniController);
    if (miniControllers == null) {
      miniControllers = new HashSet<OOMiniController>();
    }
    miniControllers.add(miniController);
    miniController.setCastManager(castManager);
  }
  
  public void removeMiniController(OOMiniController miniController) {
    DebugMode.logD(TAG, "Remove mini controller " + miniController);
    miniControllers.remove(miniController);
  }
  
  public void removeAllMiniControllers() {
    DebugMode.logD(TAG, "Remove all mini controllers");
    miniControllers.clear();
  }
  
  public void updateMiniControllersState() {
    DebugMode.logD(TAG, "Update mini controllers state");
    if (miniControllers != null &&  castPlayer != null) {
      for (OOMiniController miniController : miniControllers) {
        miniController.updatePlayPauseState(castPlayer.getState());
      }
    }
  }
  
  public void dismissMiniControllers() {
    DebugMode.logD(TAG, "dismiss mini controllers");
    if (miniControllers != null) {
      for (OOMiniController miniController : miniControllers) {
        miniController.dismiss();
      }
    }
  }
  
  public void setTargetActivity(Class<?> targetActivity) {
    OOCastManager.targetActivity = targetActivity;
  }
  
  public Class<?> getTargetActivity() {
    return OOCastManager.targetActivity;
  }
  
  public Class<?> getCurrentActivity() {
    return OOCastManager.currentActivity;
  }
  
  
  /*============================================================================================*/
  /*==========  Notification Service ============================================================*/
  /*============================================================================================*/
  
  public  void createNotificationService(Context context, Class<?> targetActivity) {
    DebugMode.logD(TAG, "Create notification service");
    notificationServiceIsActivated = true;
    if (castPlayer != null) {
      OOCastManager.targetActivity = targetActivity;
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
    NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    mNotifyMgr.cancel(notificationReceiverID);
    unregisterBroadcastReceiver(context);
    notificationServiceIsActivated = false;
    notificationBuilder = null;
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
          if (castPlayer != null) {
            String action = intent.getAction();
            if (action.equals(ACTION_STOP)) {
              disconnect();
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
    if (this.notificationMiniControllerResourceId == -1) {
      return;
    }

    // Create notification View
    RemoteViews notificationView = new RemoteViews(context.getPackageName(), this.notificationMiniControllerResourceId);
    notificationView.setTextViewText(R.id.titleView, getCastPlayer().getCastItemTitle());
    notificationView.setTextViewText(R.id.subTitleView, getDeviceName());
    notificationView.setTextColor(R.id.titleView, Color.WHITE);
    notificationView.setTextColor(R.id.subTitleView, Color.WHITE);
    notificationView.setImageViewBitmap(R.id.iconView, getCastPlayer().getCastImageBitmap());
    notificationView.setImageViewBitmap(R.id.removeView, OOCastUtils.getChromecastNotificationCloseButton());

    if (shouldDisplayPlayButton) {
      notificationView.setImageViewBitmap(R.id.playPauseView, OOCastUtils.getDarkChromecastPlayButton());
    } else {
      notificationView.setImageViewBitmap(R.id.playPauseView, OOCastUtils.getDarkChromecastPauseButton());
    }

    // Set the result intent so the user can navigate to the target activity by clicking the notification view
    Intent resultIntent = new Intent(context, targetActivity);
//    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    resultIntent.putExtra("embedcode", castPlayer.getEmbedCode());
    resultIntent.putExtra("castState", "casting");

    // Build the stack for PendingIntent to make sure the user can navigate back to the parent acitvity
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    stackBuilder.addParentStack(targetActivity);
    stackBuilder.addNextIntent(resultIntent);

    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    if (castManager.notificationBuilder == null) {
      castManager.notificationBuilder = new NotificationCompat.Builder(context).
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
    notificationView.setOnClickPendingIntent(R.id.playPauseView, pendingSwitchIntent);

    Intent stopIntent = new Intent(ACTION_STOP);
    PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 100, stopIntent, 0);
    notificationView.setOnClickPendingIntent(R.id.removeView, stopPendingIntent);
    
    
    NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notifyMgr.notify(notificationReceiverID, notification);
  }
  
  
  /*============================================================================================*/
  /*========== Lock Screen Controller ==========================================================*/
  /*============================================================================================*/
  
  public void registerLockScreenControls(Context context) {
    if (castPlayer != null) {
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
    if (castPlayer != null && notificationServiceIsActivated) {
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


































