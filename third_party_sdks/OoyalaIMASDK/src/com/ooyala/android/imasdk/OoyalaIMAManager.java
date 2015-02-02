package com.ooyala.android.imasdk;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.view.ViewGroup;

import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent.AdErrorListener;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventListener;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsLoader.AdsLoadedListener;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.CompanionAdSlot;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.ooyala.android.DebugMode;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.item.Video;
import com.ooyala.android.player.PlayerInterface;
import com.ooyala.android.plugin.AdPluginInterface;

/**
 * The OoyalaIMAManager will play back all IMA ads affiliated with any playing Ooyala asset. This will
 * automatically be configured, as long as the VAST URL is properly configured in Third Module Metadata.
 *
 * The OoyalaIMAManager works most completely with an OptimizedOoyalaPlayerLayoutController.  If you do not
 * use this layout controller, you will not see IMA's "Learn More" button when in fullscreen mode.
 *
 */
public class OoyalaIMAManager implements AdPluginInterface {
  private static String TAG = "OoyalaIMAManager";

  public boolean _onAdError;

  protected AdsLoader _adsLoader;
  protected AdsManager _adsManager;
  protected AdDisplayContainer _container;
  protected ImaSdkFactory _sdkFactory;
  protected ImaSdkSettings _sdkSettings;

  protected OoyalaPlayerIMAWrapper _ooyalaPlayerWrapper;
  protected List<CompanionAdSlot> _companionAdSlots;
  protected Set<Integer> _cuePoints;
  protected Map<String,String> _adTagParameters;
  protected OoyalaPlayer _player;
  private String _adUrlOverride;
  
  protected IMAAdPlayer _adPlayer = null;
  private boolean _browserOpened = false;

  /**
   * Initialize the Ooyala IMA Manager, which will play back all IMA ads affiliated with any playing Ooyala
   * asset. This will automatically be configured, as long as the VAST URL is properly configured in Third
   * Module Metadata.
   * @param ooyalaPlayer
   */
  public OoyalaIMAManager(OoyalaPlayer ooyalaPlayer) {
    _player = ooyalaPlayer;
    _adPlayer = new IMAAdPlayer();
    _adPlayer.setIMAManager(this);
    _companionAdSlots = new ArrayList<CompanionAdSlot>();

    //Initialize OoyalaPlayer-IMA Bridge
    _ooyalaPlayerWrapper = new OoyalaPlayerIMAWrapper(_player, this);
    _player.registerPlugin(this);

    //Initialize IMA classes
    _sdkFactory = ImaSdkFactory.getInstance();
    _adsLoader = _sdkFactory.createAdsLoader(_player.getLayout().getContext(), _sdkFactory.createImaSdkSettings());

    //Create the listeners for the adsLoader and adsManager
    _adsLoader.addAdErrorListener(new AdErrorListener() {
      @Override
      public void onAdError(AdErrorEvent event) {
        DebugMode.logE(TAG, "IMA AdsLoader Error: " + event.getError().getMessage() + "\n");
        DebugMode.logE(TAG, "IMA AdsLoader Error: doing adPlayerCompleted()" );
        _onAdError = true;
        _ooyalaPlayerWrapper.onAdError();
      }
    });

    _adsLoader.addAdsLoadedListener(new AdsLoadedListener() {

      @Override
      public void onAdsManagerLoaded(AdsManagerLoadedEvent event) {
        DebugMode.logD(TAG, "IMA AdsManager: Ads loaded");
        _adsManager = event.getAdsManager();
        _player.exitAdMode(_adPlayer.getIMAManager());
        _adsManager.addAdErrorListener(new AdErrorListener() {

          @Override
          public void onAdError(AdErrorEvent event) {
            DebugMode.logE(TAG, "IMA AdsManager Error: " + event.getError().getMessage() + "\n");
            DebugMode.logE(TAG, "IMA AdsLoader Error: doing adPlayerCompleted()" );
            _onAdError = true;
            _ooyalaPlayerWrapper.onAdError();
          }
        });

        _adsManager.addAdEventListener(new AdEventListener() {

          @Override
          public void onAdEvent(AdEvent event) {
            DebugMode.logD(TAG,"IMA AdsManager Event: " + event.getType());
            switch (event.getType()) {
            case LOADED:
              DebugMode.logD(TAG,"IMA Ad Manager: Ads Loaded");
              break;
            case CONTENT_PAUSE_REQUESTED:
              int currentContentPlayheadTime = _player.getPlayheadTime(); // have to be before _ooyalaPlayerWrapper.pauseContent() since "currentPlayer" will become adPlayer after pause;
              _ooyalaPlayerWrapper.pauseContent();
              Set<Integer> newCuePoints = new HashSet<Integer>();
              if (_cuePoints != null && _cuePoints.size() > 1) {
                for (Integer cuePoint : _cuePoints) {
                  // When pausing content for post-roll the current playhead is ZERO instead of content duration
                  if (cuePoint != 0 &&
                      cuePoint >= currentContentPlayheadTime && 
                      !(_cuePoints.size() == 1 && cuePoint == _player.getCurrentItem().getDuration())) {
                    newCuePoints.add(cuePoint);
                  }
                }
              }
              _cuePoints = newCuePoints;
              break;
            case CONTENT_RESUME_REQUESTED:
              _ooyalaPlayerWrapper.playContent();
              break;
            case STARTED:
              // This is the right moment to update the State
              _adPlayer.setState(State.PLAYING);
              break;
            case ALL_ADS_COMPLETED:
              break;
            case COMPLETED:
              break;
            case PAUSED:
              _adPlayer.setState(State.PAUSED);
              break;
            case RESUMED:
              if (_browserOpened) {
                _adPlayer.play();
                _browserOpened = false;
              }
              break;
            case CLICKED:
              _adPlayer.pause();
              _browserOpened = true;
              break;
            default:
              break;
            }
          }
        });
      }
    });
  }

  /**
   * Specify a list of views that the IMA Manager can use to show companion ads.
   * @param companionAdView The AdView to hold the companion ad slot
   * @param width
   * @param height
   */
  public void addCompanionSlot(ViewGroup companionAdView, int width, int height) {
    CompanionAdSlot adSlot = _sdkFactory.createCompanionAdSlot();
    adSlot.setContainer(companionAdView);
    adSlot.setSize(width, height);
    _companionAdSlots.add(adSlot);
  }

  /**
   * Specify a map of Ad Tag parameters that will be appended to the ad tag
   * This will not override already set parameters.  This will not query-string encode parameters.
   * If you call this method twice, you override the parameters originally sent
   * @param adTagParameters the keys and values for ad tag parameters to be appended
   */
  public void setAdTagParameters(Map<String, String> adTagParameters) {
    _adTagParameters = adTagParameters;
  }

  /**
   * Manually load an IMA Vast URL to initialize the IMA Manager.
   * You do not need to do this if a VAST URL is properly configured in Third Party Module Metadata.
   * It is not advised usage to manually load an IMA VAST URL while any IMA URL is configured in Third Party
   * Module Metadata.
   * @param url VAST url for IMA
   */
  public void setAdUrlOverride( String url ) {
    _adUrlOverride = url;
  }

  private void loadAds(String url) {
    if (_container != null) {
      DebugMode.logD(TAG, "IMA Managaer: The customer is loading ads a second time!");
    }

    if (_adTagParameters != null) {
      for(String key : _adTagParameters.keySet()) {
        url += (url.contains("?") ? "&" : "?") + key + "=" + _adTagParameters.get(key);
      }
    }
    _container = _sdkFactory.createAdDisplayContainer();
    _container.setPlayer(_ooyalaPlayerWrapper);
    _container.setAdContainer(_player.getLayout());
    DebugMode.logD(TAG, "IMA Managaer: Requesting ads: " + url);
    AdsRequest request = _sdkFactory.createAdsRequest();
    request.setAdTagUrl(url);
    request.setContentProgressProvider(_ooyalaPlayerWrapper);

    if (_companionAdSlots != null) {
      _container.setCompanionSlots(_companionAdSlots);
    }

    request.setAdDisplayContainer(_container);
    _adsLoader.requestAds(request);
  }
  
  private void fetchCuePoint() {
    if (_adsManager != null && _cuePoints == null) {
      _cuePoints = new HashSet<Integer>();
      List<Float> cuePointsFloat = _adsManager.getAdCuePoints();
      for (Float cuePoint : cuePointsFloat) {
        if (cuePoint < 0) {
          _cuePoints.add(_player.getCurrentItem().getDuration());
        } else {
          _cuePoints.add(cuePoint.intValue()  * 1000);
        }
      }
      DebugMode.logD(TAG, "Cue Point List = " + _cuePoints);
    }
  }

  // Implement AdPluginInterface

  // Return true if has pre-roll
  @Override
  public boolean onInitialPlay() {
    DebugMode.logD(TAG, "IMA Ads Manager: onInitialPlay");
    if (_adsManager != null) {
      _adsManager.init();
      fetchCuePoint();
      _adsManager.start();
    }
    return (_cuePoints != null && _cuePoints.contains(0));
  }

  @Override
  public boolean onContentChanged() {
    DebugMode.logD(TAG, "IMA Ads Manager: onContentChanged");
    destroy();
    resetFields();
    
    return true;  //True if you want to block, false otheriwse
  }

  @Override
  public boolean onPlayheadUpdate(int playhead) {
    // We do not know when to play ads until the IMAAdManager send the notification
    // so we always return false;
    return false;
  }

  @Override
  public boolean onContentFinished() {
    // This is the time we need to check should we play post-roll
    DebugMode.logD(TAG, "IMA Ads Manager: onContentFinished");
    if (_cuePoints.size() != 0 && _cuePoints.contains(_player.getCurrentItem().getDuration())) {
      _adsLoader.contentComplete();
      return true;
    }
    return false;
  }

  @Override
  public boolean onCuePoint(int cuePointIndex) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean onContentError(int errorCode) {
    // Handled by call back
    DebugMode.logD(TAG, "IMA Ads Manager: onContentError");
    _ooyalaPlayerWrapper.fireIMAAdErrorCallback();
    return false;
  }

  @Override
  public void onAdModeEntered() {
    // nothing need to be done here for Google IMA since we fire the AdModeEnter request first
    DebugMode.logD(TAG, "IMA Ads Manager: onAdModeEntered");
    if (_adsManager == null) {
      Video currentItem = _player.getCurrentItem();

      final boolean isBacklotIMA = currentItem.getModuleData() != null &&
          currentItem.getModuleData().get("google-ima-ads-manager") != null &&
          currentItem.getModuleData().get("google-ima-ads-manager").getMetadata() != null;
      final boolean isOverrideIMA = _adUrlOverride != null;
      if ( isBacklotIMA || isOverrideIMA ) {
        String url = _adUrlOverride != null ? _adUrlOverride : currentItem.getModuleData().get("google-ima-ads-manager").getMetadata().get("adTagUrl");
        if(url != null) {
          DebugMode.logD(TAG, "Start Loading ads after CURRENT_ITEM_CHANGED_NOTIFICATION");
          loadAds(url);
        }
      }
    }
  }

  @Override
  public void suspend() {
    // TODO Auto-generated method stub
    DebugMode.logD(TAG, "IMA Ads Manager: suspend");
    _adPlayer.suspend();
    _ooyalaPlayerWrapper.fireVideoSuspendCallback();
  }

  @Override
  public void resume() {
    // TODO Auto-generated method stub
    DebugMode.logD(TAG, "IMA Ads Manager: resume");
    if (_adPlayer != null) {
      _adPlayer.resume();
    }
    _ooyalaPlayerWrapper.fireIMAAdResumeCallback();
  }

  @Override
  public void resume(int timeInMilliSecond, State stateToResume) {
    // TODO Auto-generated method stub
    // Can we resume in this way for Google IMA??
    // Currently we just do the normal resume
    resume();
  }

  @Override
  public void destroy() {
    DebugMode.logD(TAG, "IMA Ads Manager: destroy");
    if (_adsManager != null) {
      _adsManager.destroy();
      _ooyalaPlayerWrapper.stopAd();
      _onAdError = false;
      _cuePoints = null;
    }
    _adsManager = null;
  }

  @Override
  public PlayerInterface getPlayerInterface() {
    // TODO Auto-generated method stub
    return _adPlayer;
  }

  @Override
  public void resetAds() {
    DebugMode.logD(TAG, "IMA Ads Manager: reset");
    // TODO Auto-generated method stub
    resetFields();
    onInitialPlay();
  }

  @Override
  public void skipAd() {
    DebugMode.logD(TAG, "IMA Ads Manager: skipAd");
    // TODO Auto-generated method stub
    _adsManager.skip();
  }

  private void resetFields() {
    DebugMode.logD(TAG, "IMA Ads Manager: resetFields");
    _cuePoints = null;
  }


  @Override
  public void reset() {
    // TODO Auto-generated method stub
    resetAds();
  }

  @Override
  public Set<Integer> getCuePointsInMilliSeconds() {
    if (_cuePoints != null) {
      return new HashSet<Integer>(_cuePoints);
    }
    return new HashSet<Integer>();
  }
}
