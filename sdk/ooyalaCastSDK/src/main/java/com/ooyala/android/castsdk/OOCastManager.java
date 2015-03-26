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
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.util.DebugMode;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGE;

public class OOCastManager extends DataCastManager {
  private static final String TAG = "CastManager";
  
  private static OOCastManager castManager;
  private static Class<?> targetActivity;
  private static Class<?> currentActivity;
  private static Context currentContext;
  
  private static NotificationCompat.Builder notificationBuilder;
  private BroadcastReceiver receiver;
  private AudioManager audioManager;
  private RemoteControlClient remoteControlClient;
  private final int notificationReceiverID = 001;
  
  public static final String ACTION_PLAY = "OOCastPlay";
  public static final String ACTION_STOP = "OOCastStop";
  
  private String namespace;
  private View castView;
  private int notificationImageResourceId = -1;
  private Bitmap miniControllerImageBitmap;
  private OoyalaPlayer ooyalaPlayer;
  private com.ooyala.android.cast.OOCastPlayer castPlayer;
  private Set<com.ooyala.android.cast.OOMiniController> miniControllers;
  public boolean isShowingPlayButton;
  private int notificationMiniControllerResourceId = -1;
  
  public static OOCastManager initialize(Context context, String applicationId, String... namespaces) {
    DebugMode.logD(TAG, "Init OOCastManager with appId = " + applicationId + ", namespace = " + namespaces);
    if (null == castManager) {
        if (ConnectionResult.SUCCESS != GooglePlayServicesUtil.isGooglePlayServicesAvailable(context)) {
            String msg = "Couldn't find the appropriate version of Google Play Services";
            LOGE(TAG, msg);
            throw new RuntimeException(msg);
        }
        castManager = new OOCastManager(context, applicationId, namespaces);
        mCastManager = castManager;
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
  
  
  public void setCastView(View view) {
    DebugMode.logD(TAG, "Set cast view to " + view);
    castView = view;
  }
  
  public void clearCastView() {
    DebugMode.logD(TAG, "Clear cast view");
    castView = null;
    if (castPlayer != null) {
      castPlayer.clearCastView();
    }
  }
  
  public void setNotificationImageResourceId(int resourceId) {
    this.notificationImageResourceId = resourceId;
  }
  
  public void setDefaultMiniControllerImageBitmap(Bitmap imageBitmap) {
    miniControllerImageBitmap = imageBitmap;
  }
  
  public Bitmap getDefaultMiniControllerImageBitmap() {
    return miniControllerImageBitmap;
  }
  
  public void destroy(Context context) {
    DebugMode.logD(TAG, "destroy OOCastManager");
    ooyalaPlayer = null;
    clearCastView();
    destroyCurrentCastPlayer();
    destroyNotificationService(context);
    unregisterLockScreenControls();
    unregisterBroadcastReceiver(context);
  }
  
  public com.ooyala.android.cast.OOCastPlayer createNewCastPlayer(String embedCode) {
    DebugMode.logD(TAG, "Create new CastPlayer");
    if (castPlayer == null || (castPlayer.getEmbedCode() != null && !castPlayer.getEmbedCode().equals(embedCode))) {
      castPlayer = new com.ooyala.android.cast.OOCastPlayer(this, ooyalaPlayer);
    } 
    castPlayer.setCastView(castView);
    // This method can only be called from ooyalaPlayer
    // We do not need to show any mini controller for a activity with ooyalaPlayer
    removeAllMiniControllers();
    return castPlayer;
  }

  public com.ooyala.android.cast.OOCastPlayer getCurrentCastPlayer() {
    return castPlayer;
  }
  
  public void destroyCurrentCastPlayer() {
    DebugMode.logD(TAG, "Destroy current CastPlayer");
    castPlayer = null;
  }
  
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
    mediaRouteActionProvider.setDialogFactory(new com.ooyala.android.cast.OOMediaRouteDialogFactory(this, null));
  }
  
  public void connectOoyalaPlayer(OoyalaPlayer ooyalaPlayer) {
    DebugMode.logD(TAG, "Connect to OoyalaPlayer " + ooyalaPlayer);
    this.ooyalaPlayer = ooyalaPlayer;
    ooyalaPlayer.setCastManager(this);
  }
  
  public void disconnectOoyalaPlayer() {
    DebugMode.logD(TAG, "Disconnect from ooyalaPlayer " + ooyalaPlayer);
    ooyalaPlayer = null;
  }

  public void onResume() {
    DebugMode.logD(TAG, "onResume()");
    updateMiniControllersVisibility();
    updateMiniControllersState();
  }
  
  @Override
  public void onApplicationConnected(ApplicationMetadata appMetadata, String applicationStatus,
          String sessionId, boolean wasLaunched) {
    DebugMode.logD(TAG, "onApplicationConnected called");
    super.onApplicationConnected(appMetadata, applicationStatus, sessionId, wasLaunched);
    if (ooyalaPlayer != null && ooyalaPlayer.getCurrentItem() != null) {
      ooyalaPlayer.switchToCastMode(ooyalaPlayer.getEmbedCode());
    }
  }
  
  @Override
  public void onApplicationDisconnected(int errorCode) {
    DebugMode.logD(TAG, "onApplicationDisconnected called");
    super.onApplicationDisconnected(errorCode);
    exitCastMode();
  }
  
  @Override
  public void disconnectDevice(boolean stopAppOnExit, boolean clearPersistedConnectionData,
      boolean setDefaultRoute) {
    DebugMode.logD(TAG, "disconnectDevice called");
    super.disconnectDevice(stopAppOnExit, clearPersistedConnectionData, setDefaultRoute);
    exitCastMode();
  }
  
  public boolean isInCastMode() {
    return (getCurrentCastPlayer() != null);
  }
  
  public void exitCastMode() {
    DebugMode.logD(TAG, "Exit Cast Mode");
    clearCastView();
    if (ooyalaPlayer != null && castPlayer != null) {
      ooyalaPlayer.exitCastMode(castPlayer.currentTime(), castPlayer.getState(), castPlayer.getEmbedCode());
    }
    updateMiniControllersVisibility();
    removeAllMiniControllers();
  }
  
  public void sendDataMessage(String message) throws IllegalArgumentException, IllegalStateException, IOException,
      TransientNetworkDisconnectionException, NoConnectionException {
   super.sendDataMessage(message, namespace);
  }

  @Override
  public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
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
  
  public void addMiniController(com.ooyala.android.cast.OOMiniController miniController) {
    DebugMode.logD(TAG, "Add mini controller " + miniController);
    if (miniControllers == null) {
      miniControllers = new HashSet<com.ooyala.android.cast.OOMiniController>();
    }
    miniControllers.add(miniController);
    miniController.setCastManager(castManager);
  }
  
  public void removeMiniController(com.ooyala.android.cast.OOMiniController miniController) {
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
      for (com.ooyala.android.cast.OOMiniController miniController : miniControllers) {
        miniController.updatePlayPauseState(castPlayer.getState());
      }
    }
  }
  
  public void updateMiniControllersVisibility() {
    DebugMode.logD(TAG, "Update mini controllers visibility");
    if (miniControllers != null) {
      for (com.ooyala.android.cast.OOMiniController miniController : miniControllers) {
        miniController.updateVisibility();
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
    }
  }
  
  private void buildNotificationService(Context context, boolean shouldDisplayPlayButton) {
    if (this.notificationMiniControllerResourceId == -1) {
      return;
    }
    RemoteViews notificationView = new RemoteViews(context.getPackageName(), this.notificationMiniControllerResourceId);
    notificationView.setTextViewText(R.id.titleView, getCurrentCastPlayer().getCastItemTitle());
    notificationView.setTextViewText(R.id.subTitleView, getDeviceName());
    notificationView.setTextColor(R.id.titleView, Color.WHITE);
    notificationView.setTextColor(R.id.subTitleView, Color.WHITE);
    notificationView.setImageViewBitmap(R.id.iconView, getCurrentCastPlayer().getCastImageBitmap());
    notificationView.setImageViewBitmap(R.id.removeView, Images.getChromecastNotificationCloseButton());
  
    if (shouldDisplayPlayButton) {
      notificationView.setImageViewBitmap(R.id.playPauseView, Images.getDarkChromecastPlayButton());
    } else {
      notificationView.setImageViewBitmap(R.id.playPauseView, Images.getDarkChromecastPauseButton());
    }
    Intent resultIntent = new Intent(context, targetActivity);
    resultIntent.putExtra("embedcode", castPlayer.getEmbedCode());
    resultIntent.putExtra("castState", "casting");
    
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

      ComponentName myEventReceiver = new ComponentName(context, com.ooyala.android.cast.OOBroadcastReceiver.class);
      audioManager.registerMediaButtonEventReceiver(myEventReceiver);
      if (remoteControlClient == null) {
          Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
          intent.setComponent(myEventReceiver);
          remoteControlClient = new RemoteControlClient(PendingIntent.getBroadcast(context, 0, intent, 0));
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
    }
  }
  
  public void updateNotificationAndLockScreenPlayPauseButton() {
    DebugMode.logD(TAG, "Update Lock Screen mini controller play/pause button status");
    if (castPlayer != null) {
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


































