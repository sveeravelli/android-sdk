package com.ooyala.android.freewheelsdk;

import android.app.Activity;
import android.widget.FrameLayout;

import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.StateNotifier;
import com.ooyala.android.StateNotifierListener;
import com.ooyala.android.item.Stream;
import com.ooyala.android.item.Video;
import com.ooyala.android.player.PlayerInterface;
import com.ooyala.android.plugin.AdPluginInterface;
import com.ooyala.android.plugin.ManagedAdsPlugin;
import com.ooyala.android.ui.AbstractOoyalaPlayerLayoutController;
import com.ooyala.android.ui.OoyalaPlayerControls;
import com.ooyala.android.util.DebugMode;

import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import tv.freewheel.ad.AdManager;
import tv.freewheel.ad.interfaces.IAdContext;
import tv.freewheel.ad.interfaces.IAdManager;
import tv.freewheel.ad.interfaces.IConstants;
import tv.freewheel.ad.interfaces.IEvent;
import tv.freewheel.ad.interfaces.IEventListener;
import tv.freewheel.ad.interfaces.ISlot;

/**
 * The OoyalaFreewheelManager will play back all Freewheel ads affiliated with any playing Ooyala asset. It will
 * need some parameters to be properly configured in Third Party Module Metadata or passed in through overrideFreewheelParameters().
 *
 * The OoyalaFreewheelManager works best with an OptimizedOoyalaPlayerLayoutController. If you do not
 * use this layout controller, you will not get media controllers in fullscreen mode.
 */
public class OoyalaFreewheelManager extends ManagedAdsPlugin<FWAdSpot>
    implements AdPluginInterface, StateNotifierListener, Observer {

  private static final String TAG = "OoyalaFreewheelManager";
  private static final double FWPLAYER_AD_REQUEST_TIMEOUT = 5.0;

  protected Activity _parent;
  protected OoyalaPlayer _player;
  protected OoyalaPlayerControls _controls;
  protected FrameLayout _layout;

  protected Map<String,String> _fwParameters = null;
  protected List<ISlot> _overlays = null;
  protected boolean haveDataToUpdate;
  protected boolean didUpdateRollsAndDelegate;

  //Freewheel ad request parameters
  protected int _fwNetworkId = -1;
  protected String _fwAdServer = null;
  protected String _fwProfile = null;
  protected String _fwSiteSectionId = null;
  protected String _fwVideoAssetId = null;
  protected String _fwFRMSegment = null;

  //Freewheel Ad Manager
  protected IAdContext _fwContext = null;
  protected IConstants _fwConstants = null;

  private FWAdPlayer _adPlayer = null;
  private StateNotifier _notifier = null;

  /**
   * Initialize OoyalaFreewheelManager
   * @param parent Activity to be used to set into IAdContext
   * @param playerLayoutController OoyalaPlayerLayoutController to get the player and playerLayout
   */
  public OoyalaFreewheelManager(Activity parent,
    AbstractOoyalaPlayerLayoutController playerLayoutController) {
    this(parent, playerLayoutController.getLayout(), playerLayoutController.getPlayer());
    _controls = playerLayoutController.getControls();
  }

  public OoyalaFreewheelManager(Activity parent, FrameLayout layout, OoyalaPlayer player) {
    _parent = parent;
    _layout = layout;
    _player = player;
    _player.addObserver(this);
    _player.registerPlugin(this);
    _notifier = _player.createStateNotifier();
    _notifier.addListener(this);
  }

  /**
   * Sets the Freewheel ad parameters to override values from Backlot or Ooyala Internals.
   * <pre><b> Key                            Example Value</b>
   * "fw_android_mrm_network_id"    "42015"
   * "fw_android_ad_server"         "http://demo.v.fwmrm.net/"
   * "fw_android_player_profile"    "fw_tutorial_android"
   * "fw_android_site_section_id"   "fw_tutorial_android"
   * "fw_android_video_asset_id"    "fw_simple_tutorial_asset"</pre>
   * @param fwParameters Dictionary with the above defined string keys and values
   */
  public void overrideFreewheelParameters(Map<String, String> fwParameters) {
    _fwParameters = fwParameters;
  }

  /**
   * Gets the Freewheel context
   * @return the Freewheel context
   */
  public IAdContext getFreewheelContext() {
    return _fwContext;
  }

  /**
   * To be called only by the FWAdPlayer. Let the manager know that ads are playing.
   */
  public void adsPlaying() {
    _fwContext.setVideoState(_fwConstants.VIDEO_STATE_PAUSED()); //let the ad manager know content paused to let the ads play
    _fwContext.registerVideoDisplayBase(_layout);
    if(_controls != null) {
      _controls.setVisible(false); //disable our controllers
    }
  }

  /**
   * Implements the interface method, should only be called by ad plugin managed
   * @param arg0
   * @param arg1 notification passed by OoyalaPlayer
   */
  @Override
  public void update(Observable arg0, Object arg1) {
    if (arg1 == OoyalaPlayer.STATE_CHANGED_NOTIFICATION_NAME && !_player.isShowingAd() && _fwContext != null) {
      State state = _player.getState();
      DebugMode.logD(TAG, "update: State changed to: " + state.toString());
      switch (state) {
      case PLAYING:
        DebugMode.logD(TAG, "update: PLAYING");
        _fwContext.setVideoState(_fwConstants.VIDEO_STATE_PLAYING());
        break;
      case PAUSED:
        DebugMode.logD(TAG, "update: PAUSED");
        _fwContext.setVideoState(_fwConstants.VIDEO_STATE_PAUSED());
        break;
      case SUSPENDED:
        DebugMode.logD(TAG, "update: SUSPENDED");
        _fwContext.setVideoState(_fwConstants.VIDEO_STATE_STOPPED());
        break;
      default:
        break;
      }
    } else if (arg1 == OoyalaPlayer.PLAY_COMPLETED_NOTIFICATION_NAME) {
      if (_fwContext != null) {
        _fwContext.setVideoState(_fwConstants.VIDEO_STATE_COMPLETED());
      }
    }
  }

  private boolean currentItemChanged(Video item) {
    //Set overlay ads to null since the ad manager stays alive even though content may change
    _overlays = null;
    if(_controls != null) {
      _controls.setVisible(true); //enable our controllers when starting fresh
    }

    _adSpotManager.clear();
    if (Stream.streamSetContainsDeliveryType(_player.getCurrentItem()
        .getStreams(), Stream.DELIVERY_TYPE_HLS)) {
      _adSpotManager.setAlignment(10000);
    } else {
      _adSpotManager.setAlignment(0);
    }

    return (item.getModuleData() != null
        && item.getModuleData().get("freewheel-ads-manager") != null && setupAdManager());
  }

  /**
   * Get the metadata and parse it to get all Freewheel ad parameters.
   * Call submitAdRequest() only when all metadata were properly fetched.
   * @return true if we fetched all metadata, false otherwise
   */
  private boolean setupAdManager() {
    //Set the Freewheel Network Id. It can be only set once per application.
    String fwNetworkIdStr = getParameter("fw_android_mrm_network_id", "fw_mrm_network_id");
    int networkId = (fwNetworkIdStr != null) ? Integer.parseInt(fwNetworkIdStr) : -1;
    if (_fwNetworkId > 0 && _fwNetworkId != networkId) {
      DebugMode.logE(TAG, "The Freewheel network id can be set only once. Overriding it will not have any effect!");
    } else {
      _fwNetworkId = networkId;
    }

    //Set other Freewheel parameters
    _fwAdServer = getParameter("fw_android_ad_server", "adServer");
    _fwProfile = getParameter("fw_android_player_profile", "fw_player_profile");
    _fwSiteSectionId = getParameter("fw_android_site_section_id", "fw_site_section_id");
    _fwVideoAssetId = getParameter("fw_android_video_asset_id", "fw_video_asset_network_id");
    _fwFRMSegment = getParameter("FRMSegment", "FRMSegment");

    if (_fwNetworkId > 0 && _fwAdServer != null && _fwProfile != null && _fwSiteSectionId != null && _fwVideoAssetId != null) {
      return true;
    } else {
      DebugMode.logE(TAG, "Could not fetch all metadata for the Freewheel ad");
      return false;
    }
  }

  /**
   * Passes in the override key and the Backlot key and returns the correct value from
   *   1.) The app level's overriden parameters from _fwParameters,
   *   2.) Backdoor Third Party Module Metadata's Android specific values, or
   *   3.) Backlot's cross platform values
   * in the above order of priority. A null value is returned if key is not found.
   * @param overrideKey the name of the parameter to get from app level or from Backdoor
   * @param backlotKey the name of the parameter to get from Backlot
   * @return value of the parameter with key name
   */
  private String getParameter(String overrideKey, String backlotKey) {
    String value = null;

    if (_fwParameters != null && _fwParameters.containsKey(overrideKey)) {
      return _fwParameters.get(overrideKey); //get parameter from app level
    } else {
      try {
        //First try getting the Android specific value from Backdoor
        value = _player.getCurrentItem().getModuleData()
            .get("freewheel-ads-manager").getMetadata().get(overrideKey);

        //If value is null, try using the backlotKey
        if (value == null) {
          DebugMode.logI(TAG, "Tried to get " + overrideKey + " but received a null value. Trying Backlot key: " + backlotKey);
          value = _player.getCurrentItem().getModuleData()
              .get("freewheel-ads-manager").getMetadata().get(backlotKey);
        }
      } catch (Exception e) {
        DebugMode.logE(TAG, e + " exception in parsing Freewheel metadata for key " + overrideKey);
        return value; //short circuit when there's an error
      }
    }
    if (value == null) {
      DebugMode.logE(TAG, "Was not able to fetch value using Backlot key: " + backlotKey + "!");
    }
    return value;
  }

  /**
   * Sets up the ad manager and submits the ad request
   * If the ad request was successful, it will call updateRollsAndDelegate()
   */
  private void submitAdRequest() {
    IAdManager fwAdManager = AdManager.getInstance(_parent.getApplicationContext());
    fwAdManager.setServer(_fwAdServer);
    fwAdManager.setNetwork(_fwNetworkId);
    _fwContext = fwAdManager.newContext();
    _fwConstants = _fwContext.getConstants();

    //Set up profile, site section, and video asset info
    _fwContext.setProfile(_fwProfile, null, null, null);
    _fwContext.setSiteSection(_fwSiteSectionId, random(), 0, _fwConstants.ID_TYPE_CUSTOM(), 0);
    _fwContext.setVideoAsset(_fwVideoAssetId, _player.getCurrentItem()
        .getDuration() / 1000.0, null,
        _fwConstants.VIDEO_ASSET_AUTO_PLAY_TYPE_ATTENDED(), random(), 0,
        _fwConstants.ID_TYPE_CUSTOM(), 0, _fwConstants.VIDEO_ASSET_DURATION_TYPE_EXACT());
    _fwContext.setActivity(_parent);

    // Set parameters to use control panels
    if (_player.getOptions().getShowAdsControls()) {
      _fwContext.setParameter(_fwConstants.PARAMETER_CLICK_DETECTION(),
          "false",
        _fwConstants.PARAMETER_LEVEL_OVERRIDE());
      // TODO: use USE_CONTROL_PANEL from fwConstants when it is made public
      _fwContext.setParameter("renderer.video.useControlPanel", "true",
        _fwConstants.PARAMETER_LEVEL_OVERRIDE());
    }

    //parse FRMSegment to put into the context
    if (_fwFRMSegment != "" && _fwFRMSegment != null) {
      String[] keyValues = _fwFRMSegment.split(";");
      for (String keyValue : keyValues)
      {
        String[] splitKeyValue = keyValue.split("=");
        if (splitKeyValue.length > 1)
        {
          _fwContext.addKeyValue(splitKeyValue[0], splitKeyValue[1]);
        }
      }
    }

    //Listen for the request complete event
    _fwContext.addEventListener(_fwConstants.EVENT_REQUEST_COMPLETE(), new IEventListener() {
      @Override
      public void run(IEvent e) {
        String eType = e.getType();
        String eSuccess = e.getData().get(_fwConstants.INFO_KEY_SUCCESS()).toString();

        if (_fwConstants != null) {
          if (_fwConstants.EVENT_REQUEST_COMPLETE().equals(eType) && Boolean.valueOf(eSuccess)) {
            DebugMode.logD(TAG, "Request completed successfully");
            haveDataToUpdate = true;
            didUpdateRollsAndDelegate = false;
            updateRollsAndDelegate();
          } else {
            DebugMode.logE(TAG, "Request failed");
            cleanupOnError();
          }
        }
            exitAdMode();
      }
    });
    //Listen for any errors that may happen
    _fwContext.addEventListener(_fwConstants.EVENT_ERROR(), new IEventListener() {
      @Override
      public void run(IEvent e) {
        DebugMode.logE(TAG, "There was an error in the Freewheel Ad Manager!");
        //Set overlay ads to null so they don't affect playback
        _overlays = null;
        cleanupOnError();
            exitAdMode();
      }
    });
    //Submit request with 3s timeout
    _fwContext.submitRequest(FWPLAYER_AD_REQUEST_TIMEOUT);
  }

  private void exitAdMode() {
    if (_adPlayer != null) {
      _adPlayer.destroy();
      _adPlayer = null;
    }
    if(_controls != null) {
      _controls.setVisible(true);
    }

    _player.exitAdMode(this);
  }

  private void cleanupOnError() {
    if (_adPlayer != null) {
      _adPlayer.onError();
    }
  }

  /**
   * Gets the pre, mid, and post-rolls when ad request is complete
   * Play pre-rolls if _player.play() was called.
   * If no listener has been set yet, this does nothing; we'll be called again when the listener is set to a non-null value.
   */
  private void updateRollsAndDelegate() {
    if (haveDataToUpdate && !didUpdateRollsAndDelegate) {
      updatePreMidPost();
      haveDataToUpdate = false;
      didUpdateRollsAndDelegate = true;
    }
  }

  /**
   * Call this only via updateRollsAndDelegate() to ensure proper state.
   */
  private void updatePreMidPost() {
    DebugMode.logD(TAG, "updatePreMidPost()");
    _overlays = _fwContext.getSlotsByTimePositionClass(_fwConstants.TIME_POSITION_CLASS_OVERLAY());
    if (_overlays != null) {
      DebugMode.logD(TAG, "overlay count: " + String.valueOf(_overlays.size()));
    }
    try {
      //Add the rest of pre-rolls, mid-rolls, and post-rolls to the list and insert them to the current item to be played by the OoyalaPlayer
      insertAds(_fwContext.getSlotsByTimePositionClass(_fwConstants
          .TIME_POSITION_CLASS_PREROLL()),
          _fwConstants.TIME_POSITION_CLASS_PREROLL());
      insertAds(_fwContext.getSlotsByTimePositionClass(_fwConstants
          .TIME_POSITION_CLASS_MIDROLL()),
          _fwConstants.TIME_POSITION_CLASS_MIDROLL());
      insertAds(_fwContext.getSlotsByTimePositionClass(_fwConstants
          .TIME_POSITION_CLASS_POSTROLL()),
          _fwConstants.TIME_POSITION_CLASS_POSTROLL());
    } catch (Exception e) {
      DebugMode.logE(TAG,
          "Error in adding ad slots to the list of ads to play", e);
      DebugMode.logE(TAG, "Caught!", e);
    }
  }

  /**
   * Check to play overlays. If the playheadTime passes the ad's time to play, play only the
   * first item on the list since other items may have different times to be played at.
   */
  private void checkPlayableAds(int time) {
    double playheadTime = time / 1000.0;
    if (_overlays != null && _overlays.size() > 0 && playheadTime > _overlays.get(0).getTimePosition()) {
      _fwContext.registerVideoDisplayBase(_layout);
      _overlays.remove(0).play();
    }
  }

  private int random() {
    return (int)Math.floor(Math.random() * Integer.MAX_VALUE);
  }

  /**
   * Implements the interface method, should only be called by ad plugin managed
   */
  @Override
  public void reset() {
    resetAds();
  }

  /**
   *  Implements the interface method, should only be called by ad plugin managed
   */
  @Override
  public void suspend() {
    if (_adPlayer != null) {
      _adPlayer.suspend();
    }
  }

  /**
   * Implements the interface method, should only be called by ad plugin managed
   */
  @Override
  public void resume() {
    if (_adPlayer != null) {
      _adPlayer.resume();
    }
  }

  /**
   * Implements the interface method, should only be called by ad plugin managed
   * @param timeInMilliSecond time in millisecond
   * @param stateToResume state to resume
   */
  @Override
  public void resume(int timeInMilliSecond, State stateToResume) {
    if (_adPlayer != null) {
      _adPlayer.resume(timeInMilliSecond, stateToResume);
    }
  }

  /**
   * destroy the current adPlayer
   */
  @Override
  public void destroy() {

    if (_adPlayer != null) {
      _adPlayer.destroy();
    }
  }

  /**
   * Implements the interface method, should only be called by ad plugin managed
   * @return true if current content changes, otherwise false
   */
  @Override
  public boolean onContentChanged() {
    super.onContentChanged();
    return currentItemChanged(_player.getCurrentItem());
  }

  /**
   * Implements the interface method, should only be called by ad plugin managed
   * @param playhead
   *          the current content playhead
   * @return true if plugin needs to play midroll ads, false otherwise
   */
  @Override
  public boolean onPlayheadUpdate(int playhead) {
    checkPlayableAds(playhead);
    return super.onPlayheadUpdate(playhead);
  }

  /**
   * Implements the interface method, should only be called by ad plugin managed
   * @param cuePointIndex the index of cuePoint
   * @return false
   */
  @Override
  public boolean onCuePoint(int cuePointIndex) {
    return false;
  }

  /**
   * Implements the interface method, should only be called by ad plugin managed
   * @param errorCode the code number of the error
   * @return false
   */
  @Override
  public boolean onContentError(int errorCode) {
    this.cleanupOnError();
    return false;
  }

  /**
   * Implements the interface method, should only be called by ad plugin manager
   */
  @Override
  public void onAdModeEntered() {
    if (getLastAdModeTime() < 0) {
      submitAdRequest();
    } else {
      super.onAdModeEntered();
    }
  }

  /**
   * Implements the interface method, should only be called by ad plugin manager
   * @return the adPlayer
   */
  @Override
  public PlayerInterface getPlayerInterface() {
    return _adPlayer;
  }

  /**
   * Implements the interface method, should only be called by ad plugin managed
   */
  @Override
  public void resetAds() {
    _adSpotManager.resetAds();
  }

  private void insertAds(List<ISlot> slotList, int adType) {
    if (slotList == null) {
      return;
    }

    DebugMode.logD(TAG,
        "Freewheel insertAds: " + String.valueOf(slotList.size())
            + adTypeString(adType) + " ads");
    for (ISlot slot : slotList) {
      FWAdSpot adSpot = FWAdSpot.create(slot,
          adType == _fwConstants.TIME_POSITION_CLASS_POSTROLL());
      _adSpotManager.insertAd(adSpot);
    }
  }

  private String adTypeString(int adType) {
    if (adType == _fwConstants.TIME_POSITION_CLASS_PREROLL()) {
      return "PREROLL";
    } else if (adType == _fwConstants.TIME_POSITION_CLASS_MIDROLL()) {
      return "MIDROLL";
    } else if (adType == _fwConstants.TIME_POSITION_CLASS_POSTROLL()) {
      return "POSTROLL";
    } else if (adType == _fwConstants.TIME_POSITION_CLASS_PAUSE_MIDROLL()) {
      return "PAUSE_MIDROLL";
    } else if (adType == _fwConstants.TIME_POSITION_CLASS_OVERLAY()) {
      return "OVERLAY";
    } else if (adType == _fwConstants.TIME_POSITION_CLASS_DISPLAY()) {
      return "DISPLAY";
    }
    return "UNKNOWN_TYPE";
  }

  @Override
  protected boolean playAd(FWAdSpot adToPlay) {
    if (_adPlayer != null) {
      _adPlayer.destroy();
    }
    _adPlayer = new FWAdPlayer();
    _adPlayer.init(this, _player, adToPlay, _notifier);
    _adPlayer.play();
    return true;
  }

  /**
   * Implements the interface method, should only be called by ad plugin manager
   * @param notifier state notifier
   */
  @Override
  public void onStateChange(StateNotifier notifier) {
    if (_adPlayer == null || _adPlayer.getNotifier() != notifier) {
      return;
    }

    switch (notifier.getState()) {
    case COMPLETED:
      if (!playAdsBeforeTime()) {
        exitAdMode();
      }
      break;
    case ERROR:
      exitAdMode();
      break;
    default:
      break;
    }
  }

  @Override
  public void processClickThrough() {
    if (_adPlayer != null) {
      _adPlayer.processClickThrough();
    }
  }
}