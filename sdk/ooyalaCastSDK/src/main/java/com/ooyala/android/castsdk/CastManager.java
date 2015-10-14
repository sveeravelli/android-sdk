package com.ooyala.android.castsdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.view.View;
import android.view.ViewGroup;

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
import java.util.Map;

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

    @Override
    public void onRemoteMediaPlayerStatusUpdated() {
      int playerStatus = castManager.getVideoCastManager().getPlaybackStatus();
      if (castPlayer != null) {
        castPlayer.onPlayerStatusChanged(playerStatus);
      }
    }
  }

  private static final String TAG = CastManager.class.getSimpleName();

  public static final String ACTION_PLAY = "OOCastPlay";
  public static final String ACTION_STOP = "OOCastStop";
  private static CastManager castManager;

  private static int notificationMiniControllerResourceId;
  private static int notificationImageResourceId;

  private Bitmap miniControllerDefaultImageBitmap;

  private  Context context;
  private View castView;
  private WeakReference<OoyalaPlayer> ooyalaPlayer;
  private CastPlayer castPlayer;
  private Map<String, String> additionalInitParams;
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
  public static CastManager initialize(Context context, String applicationId, Class<?> targetActivity, String namespace) throws CastManagerInitializationException {
    DebugMode.assertCondition( castManager == null, TAG, "Cannot re-initialize" );
    if( castManager == null ) {
//      notificationMiniControllerResourceId = R.layout.oo_default_notification;
//      notificationImageResourceId = R.drawable.ic_ooyala;
      DebugMode.logD(TAG, "Init new CastManager with appId = " + applicationId + ", namespace = " + namespace);
      requireGooglePlayServices(context);
      try {
        DebugMode.logD(TAG, "Initialize VideoCastManager");
        VideoCastManager.initialize(context, applicationId, targetActivity, namespace).enableFeatures(
            VideoCastManager.FEATURE_LOCKSCREEN |
                VideoCastManager.FEATURE_WIFI_RECONNECT |
//                new CCL option, comment it out for now
                VideoCastManager.FEATURE_NOTIFICATION |
                VideoCastManager.FEATURE_AUTO_RECONNECT |
                VideoCastManager.FEATURE_CAPTIONS_PREFERENCE |
                VideoCastManager.FEATURE_DEBUGGING);
        // this is the default behavior but is mentioned to make it clear that it is configurable.
        VideoCastManager.getInstance().setNextPreviousVisibilityPolicy(
            VideoCastController.NEXT_PREV_VISIBILITY_POLICY_DISABLED);

        // this is to set the launch options, the following values are the default values
//        VideoCastManager.getInstance().setLaunchOptions(false, Locale.getDefault());

        // this is the default behavior but is mentioned to make it clear that it is configurable.
        VideoCastManager.getInstance().setCastControllerImmersive(true);

        castManager = new CastManager(context, namespace);
      }
      catch( Exception e ) {
        throw new CastManagerInitializationException( e );
      }
    }
    return castManager;
  }

  public static VideoCastManager getVideoCastManager() {
    return VideoCastManager.getInstance();
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
   * @see #initialize(android.content.Context, String, Class, String)
   */
  public static CastManager getCastManager() {
    return castManager;
  }
  
  private CastManager(Context c, String namespace ) {
    this.context = c;
    this.videoCastManager = VideoCastManager.getInstance();
    this.namespace = namespace; // there's no accessor for namespaces on DataCastManager.
    this.videoCastListener = new VideoCastListener();
    this.videoCastManager.addVideoCastConsumer(this.videoCastListener);
  }

  /*============================================================================================*/
  /*========== CastManager App Setting API   ===================================================*/
  /*============================================================================================*/
  
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
  public void onResume() {
    DebugMode.logD(TAG, "onResume()");
    this.videoCastManager.incrementUiCounter();
  }

  public void onPause() {
    DebugMode.logD(TAG, "onPause");
    this.videoCastManager.decrementUiCounter();
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
  }

  /*package private on purpose*/ void syncVolume() {
    if (videoCastManager != null) {
      AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
      double volume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
      volume /= audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
      DebugMode.logD(TAG, "set device volume to cast, volume:" + volume);
      try {
        castManager.getVideoCastManager().setVolume(volume);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}