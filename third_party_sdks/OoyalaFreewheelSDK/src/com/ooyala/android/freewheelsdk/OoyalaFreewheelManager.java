package com.ooyala.android.freewheelsdk;

import java.util.ArrayList;
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
  protected FWAdPlayerListener _fwAdPlayerListener;
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

  @Override
  public void update(Observable arg0, Object arg1) {
    if (arg1 == OoyalaPlayer.TIME_CHANGED_NOTIFICATION) {
      //Check to play overlay ads when content is playing
      if (_fwContext != null && !_player.isShowingAd()) {
        checkPlayableAds();
      }
    }
    else if (arg1 == OoyalaPlayer.CURRENT_ITEM_CHANGED_NOTIFICATION) {
      currentItemChanged();
    }
    else if (arg1 == OoyalaPlayer.PLAY_COMPLETED_NOTIFICATION) {
      if (_fwContext != null) {
        _fwContext.setVideoState(_fwConstants.VIDEO_STATE_COMPLETED());
      }
    }
    else if (arg1 == OoyalaPlayer.STATE_CHANGED_NOTIFICATION) {
      //Listen to state changed notification to set correct video states
      Log.d(TAG, "State changed to: " + _player.getState());

      switch(_player.getState()) {
        case PLAYING:
          if (_fwContext != null && !_player.isShowingAd()) {
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

  private void currentItemChanged() {
    //Set overlay ads to null since the ad manager stays alive even though content may change
    _overlays = null;

    if (_player.getCurrentItem().getModuleData() != null &&
        _player.getCurrentItem().getModuleData().get("freewheel-ads-manager") != null &&
        setupAdManager()) {
      _player.getCurrentItem().insertAd(new FWAdSpot(null, this));
    }
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
      Log.e(TAG, "The Freewheel network id can be set only once. Overriding it will not have any effect!");
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
      return true;
    } else {
      Log.e(TAG, "Could not fetch all metadata for the Freewheel ad");
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
      Log.e(TAG, "Was not able to fetch value using Backlot key: " + backlotKey + "!");
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
            if (_fwAdPlayerListener != null) {
              _fwAdPlayerListener.onError();
            }
          }
        }
      }
    });
    //Listen for any errors that may happen
    _fwContext.addEventListener(_fwConstants.EVENT_ERROR(), new IEventListener() {
      public void run(IEvent e) {
        Log.e(TAG, "There was an error in the Freewheel Ad Manager!");
        //Set overlay ads to null so they don't affect playback
        _overlays = null;

        if (_fwAdPlayerListener != null) {
          _fwAdPlayerListener.onError();
        }
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
    List<ISlot> prerolls = _fwContext.getSlotsByTimePositionClass(_fwConstants.TIME_POSITION_CLASS_PREROLL());
    _overlays = _fwContext.getSlotsByTimePositionClass(_fwConstants.TIME_POSITION_CLASS_OVERLAY());

    if (_fwAdPlayerListener != null) {
      //If there are pre-rolls, pop the first pre-roll off and call adReady. The rest of the pre-rolls will be added below.
      //Else, make sure to pass nil to the listener so that FWAdPlayer can fire ad complete and resume content.
      if (prerolls != null && prerolls.size() > 0) {
        _fwAdPlayerListener.adReady(prerolls.remove(0));
      } else {
        _fwAdPlayerListener.adReady(null);
      }
    }

    try {
      //Add the rest of pre-rolls, mid-rolls, and post-rolls to the list and insert them to the current item to be played by the OoyalaPlayer
      List<ISlot> adsToPlay = new ArrayList<ISlot>();
      adsToPlay.addAll(prerolls);
      adsToPlay.addAll(_fwContext.getSlotsByTimePositionClass(_fwConstants.TIME_POSITION_CLASS_MIDROLL()));
      adsToPlay.addAll(_fwContext.getSlotsByTimePositionClass(_fwConstants.TIME_POSITION_CLASS_POSTROLL()));
      for (ISlot ad : adsToPlay) {
        _player.getCurrentItem().insertAd(new FWAdSpot(ad, this));
      }
    } catch (Exception e) {
      Log.e(TAG, "Error in adding ad slots to the list of ads to play");
      e.printStackTrace();
    }
  }

  public IAdContext getFreewheelContext() {
    return _fwContext;
  }

  public void setFWAdPlayerListener(FWAdPlayerListener adPlayer) {
    _fwAdPlayerListener = adPlayer;
  }

  public void adsPlaying() {
    _fwContext.setVideoState(_fwConstants.VIDEO_STATE_PAUSED()); //let the ad manager know content paused to let the ads play
    _fwContext.registerVideoDisplayBase(_layoutController.getLayout());
    _layoutController.getControls().setVisible(false); //disable our controllers
  }

  /**
   * Check to play overlays. If the playheadTime passes the ad's time to play, play only the
   * first item on the list since other items may have different times to be played at.
   */
  private void checkPlayableAds() {
    double playheadTime = _player.getPlayheadTime() / 1000;

    if (_overlays != null && _overlays.size() > 0 && playheadTime > _overlays.get(0).getTimePosition()) {
      _fwContext.registerVideoDisplayBase(_layoutController.getLayout());
      _overlays.remove(0).play();
    }
  }

  private int random() {
    return (int)Math.floor(Math.random() * Integer.MAX_VALUE);
  }
}