package com.ooyala.android.freewheelsdk;

import java.util.ArrayList;
import java.util.Arrays;
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
import android.util.Log;

import com.ooyala.android.OptimizedOoyalaPlayerLayoutController;
import com.ooyala.android.OoyalaPlayer;

/**
 * The OoyalaFreewheelManager will play back all Freewheel ads affiliated with any playing Ooyala asset. It will
 * need some parameters to be properly configured in Third Party Module Metadata or passed in through overrideFreewheelParameters().
 *
 * The OoyalaFreewheelManager works best with an OptimizedOoyalaPlayerLayoutController. If you do not
 * use this layout controller, you will not get media controllers in fullscreen mode.
 */
public class OoyalaFreewheelManager implements Observer {

  private static final String TAG = "OoyalaFreewheelManager";

  protected Activity _parent;
  protected OoyalaPlayer _player;
  protected OptimizedOoyalaPlayerLayoutController _layoutController;
  protected Map<String,String> _fwParameters = null;

  private boolean _playQueued = false;

  //List of all ads
  protected List<ISlot> _prerolls = null;
  protected List<ISlot> _midrolls = null;
  protected List<ISlot> _postrolls = null;
  protected List<ISlot> _overlays = null;

  //Freewheel ad request parameters
  protected int _fwNetworkId = -1;
  protected String _fwAdServer = null;
  protected String _fwProfile = null;
  protected String _fwSiteSectionId = null;
  protected String _fwVideoAssetId = null;

  //Freewheel Ad Manager
  protected IAdContext _fwContext = null;
  protected IConstants _fwConstants = null;

  /**
   * Initialize OoyalaFreewheelManager
   * @param parent Activity to be used to set into IAdContext
   * @param playerLayoutController OoyalaPlayerLayoutController to get the player and playerLayout
   */
  public OoyalaFreewheelManager(Activity parent, OptimizedOoyalaPlayerLayoutController playerLayoutController) {
    _parent = parent;
    _layoutController = playerLayoutController;
    _player = playerLayoutController.getPlayer();
    _player.addObserver(this);
    _player.registerAdPlayer(FWAdSpot.class, FWAdPlayer.class);
  }

  /**
   * Sets the Freewheel ad parameters to override values from Backlot and Backdoor.
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
   * This function gets called when METADATA_READY notification was fired.Get the metadata and parse it to
   * get all Freewheel ad parameters. Call submitAdRequest() only when all metadata were properly fetched.
   */
  private void setupAdManager() {
    //Set the Freewheel Network Id. It can be only set once per application.
    String fwNetworkIdStr = getParameter("fw_android_mrm_network_id", "fw_mrm_network_id");
    int networkId = (fwNetworkIdStr != null) ? Integer.parseInt(fwNetworkIdStr) : -1;
    if (_fwNetworkId > 0 && _fwNetworkId != networkId) {
      Log.e(TAG, "The Freewheel network id can be set only once. Overriding it will not have any effect!!");
    } else {
      _fwNetworkId = networkId;
    }

    //Set other Freewheel parameters
    _fwAdServer = getParameter("fw_android_ad_server", "adServer");
    _fwProfile = getParameter("fw_android_player_profile", "fw_player_profile");
    _fwSiteSectionId = getParameter("fw_android_site_section_id", "fw_site_section_id");
    _fwVideoAssetId = getParameter("fw_android_video_asset_id", "fw_video_asset_network_id");

    if (_fwNetworkId > 0 && _fwAdServer != null && _fwProfile != null && _fwSiteSectionId != null && _fwVideoAssetId != null) {
      submitAdRequest();
    } else {
      Log.e(TAG, "Could not fetch all metadata for the Freewheel ad");
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
        value = _player.getCurrentItem().getModuleData().get("freewheel-ads-manager").getMetadata().get(overrideKey);

        //If value is null, try using the backlotKey
        if (value == null) {
          Log.i(TAG, "Tried to get " + overrideKey + " but received a null value. Trying Backlot key: " + backlotKey);
          value = _player.getCurrentItem().getModuleData().get("freewheel-ads-manager").getMetadata().get(backlotKey);
        }
      } catch (Exception e) {
        Log.e(TAG, e + " exception in parsing Freewheel metadata for key " + overrideKey);
        return value; //short circuit when there's an error
      }
    }
    if (value == null) {
      Log.e(TAG, "Was not able to fetch value using Backlot key: " + backlotKey + "!!");
    }
    return value;
  }

  /**
   * Sets up the ad manager and submits the ad request
   * If the ad request was successful, it will call handleAdManagerRequestComplete()
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
    _fwContext.setVideoAsset(_fwVideoAssetId, _player.getDuration() / 1000, null, _fwConstants.VIDEO_ASSET_AUTO_PLAY_TYPE_ATTENDED(), random(), 0,
        _fwConstants.ID_TYPE_CUSTOM(), 0, _fwConstants.VIDEO_ASSET_DURATION_TYPE_EXACT());
    _fwContext.setActivity(_parent);

    //Listen for the request complete event
    _fwContext.addEventListener(_fwConstants.EVENT_REQUEST_COMPLETE(), new IEventListener() {
      public void run(IEvent e) {
        String eType = e.getType();
        String eSuccess = e.getData().get(_fwConstants.INFO_KEY_SUCCESS()).toString();

        if (_fwConstants != null) {
          if (_fwConstants.EVENT_REQUEST_COMPLETE().equals(eType) && Boolean.valueOf(eSuccess)) {
            Log.d(TAG, "Request completed successfully");
            handleAdManagerRequestComplete();
          } else {
            Log.e(TAG, "Request failed");
          }
        }
      }
    });
    //Listen for any errors that may happen
    _fwContext.addEventListener(_fwConstants.EVENT_ERROR(), new IEventListener() {
      public void run(IEvent e) {
        Log.e(TAG, "There was an error in the Freewheel Ad Manager!!!");
        //Set all ads to null so they don't affect playback
        _prerolls = null;
        _midrolls = null;
        _postrolls = null;
        _overlays = null;
      }
    });
    //Submit request with 3s timeout
    _fwContext.submitRequest(3.0);
  }

  /**
   * Gets the pre, mid, and post-rolls when ad request is complete
   * Play pre-rolls if _player.play() was called
   */
  private void handleAdManagerRequestComplete() {
    _prerolls = _fwContext.getSlotsByTimePositionClass(_fwConstants.TIME_POSITION_CLASS_PREROLL());
    _midrolls = _fwContext.getSlotsByTimePositionClass(_fwConstants.TIME_POSITION_CLASS_MIDROLL());
    _postrolls = _fwContext.getSlotsByTimePositionClass(_fwConstants.TIME_POSITION_CLASS_POSTROLL());
    _overlays = _fwContext.getSlotsByTimePositionClass(_fwConstants.TIME_POSITION_CLASS_OVERLAY());

    //We should only play ads if _player.play() has been called first
    if (_playQueued && _prerolls != null && _prerolls.size() > 0) {
      _fwContext.setVideoState(_fwConstants.VIDEO_STATE_PAUSED()); //let the ad manager know content paused to let the ads play
      playAds(_prerolls);
    }
  }

  /**
   * Plays the passed in list of ads
   * @param ads the list of ISlots to be played
   */
  private void playAds(List<ISlot> ads) {
    _fwContext.registerVideoDisplayBase(_layoutController.getLayout());
    _layoutController.getControls().setVisible(false); //disable our controllers
    _player.playAd(new FWAdSpot(ads, _fwContext));
  }

  /**
   * Check to play mid-rolls or overlays. If the playheadTime passes the ad's time to play,
   * play only the first item on the list since other items may have different times to be played at.
   */
  private void checkPlayableAds() {
    double playheadTime = _player.getPlayheadTime() / 1000;

    if (_midrolls != null && _midrolls.size() > 0 && playheadTime > _midrolls.get(0).getTimePosition()) {
      _fwContext.setVideoState(_fwConstants.VIDEO_STATE_PAUSED()); //let the ad manager know content paused to let the ads play
      playAds(new ArrayList<ISlot>(Arrays.asList(_midrolls.remove(0))));
    }
    else if (_overlays != null && _overlays.size() > 0 && playheadTime > _overlays.get(0).getTimePosition()) {
      _fwContext.registerVideoDisplayBase(_layoutController.getLayout());
      _overlays.remove(0).play();
    }
  }

  private int random() {
    return (int)Math.floor(Math.random() * Integer.MAX_VALUE);
  }

  @Override
  public void update(Observable arg0, Object arg1) {
    if (arg1 == OoyalaPlayer.TIME_CHANGED_NOTIFICATION) {
      //Check to play mid-rolls or overlay ads when content is playing
      if (_fwContext != null && !_player.isShowingAd()) {
        checkPlayableAds();
      }
    }
    else if (arg1 == OoyalaPlayer.METADATA_READY_NOTIFICATION) {
      //Get the metadata for fwContext when metadata is ready
      setupAdManager();
    }
    else if (arg1 == OoyalaPlayer.PLAY_COMPLETED_NOTIFICATION) {
      //When content has finished playing, play post-rolls
      if (_fwContext != null) {
        _fwContext.setVideoState(_fwConstants.VIDEO_STATE_COMPLETED());
        if (_postrolls != null && _postrolls.size() > 0) {
          playAds(_postrolls);
        }
      }
    }
    else if (arg1 == OoyalaPlayer.STATE_CHANGED_NOTIFICATION) {
      //Listen to state changed notification to set correct video states
      Log.d(TAG, "State changed to: " + _player.getState());

      switch(_player.getState()) {
        case PLAYING:
          //This boolean is needed in case fwPrerollSlots are not received before content starts playing
          _playQueued = true;

          //If pre-rolls haven't played yet and we're trying to play content, play pre-rolls first
          if (_prerolls != null && _prerolls.size() > 0) {
            _fwContext.setVideoState(_fwConstants.VIDEO_STATE_PAUSED()); //let the ad manager know content paused to let the ads play
            playAds(_prerolls);
          }
          else if (_fwContext != null && !_player.isShowingAd()) {
            _fwContext.setVideoState(_fwConstants.VIDEO_STATE_PLAYING());
          }
          break;
        case PAUSED:
        case SUSPENDED:
          if(_fwContext != null && !_player.isShowingAd()) {
            _fwContext.setVideoState(_fwConstants.VIDEO_STATE_PAUSED());
          }
          break;
        default:
          break;
      }
    }
    else if (arg1 == OoyalaPlayer.AD_COMPLETED_NOTIFICATION) {
      //When ads are completed, re-enable the controls
      //TODO: this fires when any ad is played, not just Freewheel ads.
      _layoutController.getControls().setVisible(true);
    }
  }
}