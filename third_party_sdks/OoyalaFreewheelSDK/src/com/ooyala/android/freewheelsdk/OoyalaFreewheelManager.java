package com.ooyala.android.freewheelsdk;

import java.lang.ref.WeakReference;
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
import android.app.Activity;

import com.ooyala.android.DebugMode;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.StateNotifier;
import com.ooyala.android.StateNotifierListener;
import com.ooyala.android.item.Stream;
import com.ooyala.android.item.Video;
import com.ooyala.android.player.PlayerInterface;
import com.ooyala.android.plugin.AdPluginInterface;
import com.ooyala.android.plugin.ManagedAdsPlugin;
import com.ooyala.android.ui.OptimizedOoyalaPlayerLayoutController;

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
  protected WeakReference<OoyalaPlayer> _player;
  protected OptimizedOoyalaPlayerLayoutController _layoutController;
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
  public OoyalaFreewheelManager(Activity parent, OptimizedOoyalaPlayerLayoutController playerLayoutController) {
    _parent = parent;
    _layoutController = playerLayoutController;
    _player = new WeakReference<OoyalaPlayer>(
        playerLayoutController.getPlayer());
    _player.get().addObserver(this);
    _player.get().registerPlugin(this);
    _notifier = _player.get().createStateNotifier();
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
    _fwContext.registerVideoDisplayBase(_layoutController.getLayout());
    _layoutController.getControls().setVisible(false); //disable our controllers
  }

  @Override
  public void update(Observable arg0, Object arg1) {
    if (arg1 == OoyalaPlayer.STATE_CHANGED_NOTIFICATION) {
      // Listen to state changed notification to set correct video states
      State state = _player.get().getState();
      boolean isShowingAd = _player.get().isShowingAd();
      DebugMode.logD(TAG, "State changed to: " + state.toString());
      switch (state) {
      case PLAYING:
        if (_fwContext != null && isShowingAd) {
          _fwContext.setVideoState(_fwConstants.VIDEO_STATE_PLAYING());
        }
        break;
      case PAUSED:
      case SUSPENDED:
        if (_fwContext != null && isShowingAd) {
          _fwContext.setVideoState(_fwConstants.VIDEO_STATE_PAUSED());
        }
        break;
      default:
        break;
      }
    } else if (arg1 == OoyalaPlayer.PLAY_COMPLETED_NOTIFICATION) {
      if (_fwContext != null) {
        _fwContext.setVideoState(_fwConstants.VIDEO_STATE_COMPLETED());
      }
    }
  }

  private boolean currentItemChanged(Video item) {
    //Set overlay ads to null since the ad manager stays alive even though content may change
    _overlays = null;
    _layoutController.getControls().setVisible(true); //enable our controllers when starting fresh
    _adSpotManager.clear();
    if (Stream.streamSetContainsDeliveryType(_player.get().getCurrentItem()
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
        value = _player.get().getCurrentItem().getModuleData()
            .get("freewheel-ads-manager").getMetadata().get(overrideKey);

        //If value is null, try using the backlotKey
        if (value == null) {
          DebugMode.logI(TAG, "Tried to get " + overrideKey + " but received a null value. Trying Backlot key: " + backlotKey);
          value = _player.get().getCurrentItem().getModuleData()
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
    _fwContext.setVideoAsset(_fwVideoAssetId, _player.get().getCurrentItem()
        .getDuration() / 1000.0, null,
        _fwConstants.VIDEO_ASSET_AUTO_PLAY_TYPE_ATTENDED(), random(), 0,
        _fwConstants.ID_TYPE_CUSTOM(), 0, _fwConstants.VIDEO_ASSET_DURATION_TYPE_EXACT());
    _fwContext.setActivity(_parent);

    //parse FRMSegment to put into the context
    if (_fwFRMSegment != "" && _fwFRMSegment != null)
    {
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
    _layoutController.getControls().setVisible(true);
    _player.get().exitAdMode(this);
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
      e.printStackTrace();
    }
  }

  /**
   * Check to play overlays. If the playheadTime passes the ad's time to play, play only the
   * first item on the list since other items may have different times to be played at.
   */
  private void checkPlayableAds(int time) {
    double playheadTime = time / 1000.0;
    if (_overlays != null && _overlays.size() > 0 && playheadTime > _overlays.get(0).getTimePosition()) {
      _fwContext.registerVideoDisplayBase(_layoutController.getLayout());
      _overlays.remove(0).play();
    }
  }

  private int random() {
    return (int)Math.floor(Math.random() * Integer.MAX_VALUE);
  }

  @Override
  public void reset() {
    resetAds();
  }

  @Override
  public void suspend() {
    if (_adPlayer != null) {
      _adPlayer.suspend();
    }
  }

  @Override
  public void resume() {
    if (_adPlayer != null) {
      _adPlayer.resume();
    }
  }

  @Override
  public void resume(int timeInMilliSecond, State stateToResume) {
    if (_adPlayer != null) {
      _adPlayer.resume(timeInMilliSecond, stateToResume);
    }
  }

  @Override
  public void destroy() {
    _player.get().deleteObserver(this);
    _player.get().deregisterPlugin(this);
    _player.clear();

    if (_adPlayer != null) {
      _adPlayer.destroy();
    }
  }

  @Override
  public boolean onContentChanged() {
    super.onContentChanged();
    return currentItemChanged(_player.get().getCurrentItem());
  }

  @Override
  public boolean onPlayheadUpdate(int playhead) {
    checkPlayableAds(playhead);
    return super.onPlayheadUpdate(playhead);
  }

  @Override
  public boolean onCuePoint(int cuePointIndex) {
    return false;
  }

  @Override
  public boolean onContentError(int errorCode) {
    this.cleanupOnError();
    return false;
  }

  @Override
  public void onAdModeEntered() {
    if (getLastAdModeTime() < 0) {
      submitAdRequest();
    } else {
      super.onAdModeEntered();
    }
  }

  @Override
  public PlayerInterface getPlayerInterface() {
    return _adPlayer;
  }

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
    _adPlayer.init(this, _player.get(), adToPlay, _notifier);
    _adPlayer.play();
    return true;
  }

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
}