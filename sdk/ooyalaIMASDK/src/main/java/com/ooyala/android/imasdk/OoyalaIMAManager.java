package com.ooyala.android.imasdk;


import android.os.Handler;
import android.os.Looper;
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
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.item.Video;
import com.ooyala.android.player.PlayerInterface;
import com.ooyala.android.plugin.AdPluginInterface;
import com.ooyala.android.util.DebugMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The OoyalaIMAManager works with OoyalaPlayer to manage IMA ads playback. It also provides methods configure IMA ads
 * playback, including companion ad slot and custom ad tag url.
 *
 *
 * The OoyalaIMAManager will play back all IMA ads affiliated with any playing Ooyala asset. This will
 * automatically be configured, as long as the VAST URL is properly configured in Third Module Metadata.
 *
 * The OoyalaIMAManager works most completely with an OptimizedOoyalaPlayerLayoutController.  If you do not
 * use this layout controller, you will not see IMA's "Learn More" button when in fullscreen mode.
 *
 */
public class OoyalaIMAManager implements AdPluginInterface {
  private static String TAG = "OoyalaIMAManager";
  private static final int TIMEOUT = 5000;

  /**
   * True if encounter error during ad playback
   */
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
  private boolean _allAdsCompleted = false;
  private Thread timeoutThread;

  private ViewGroup layout;

  /**
   * Initialize the Ooyala IMA Manager, which will play back all IMA ads affiliated with any playing Ooyala
   * asset. This will automatically be configured, as long as the VAST URL is properly configured in Third
   * Module Metadata.
   * @param ooyalaPlayer current OoyalaPlayer
   */
  public OoyalaIMAManager(OoyalaPlayer ooyalaPlayer, ViewGroup l) {
    this.layout = l;
    _player = ooyalaPlayer;

    if(this.layout == null) {
      this.layout = ooyalaPlayer.getLayout();
    }

    _adPlayer = new IMAAdPlayer();
    _adPlayer.setIMAManager(this);
    _companionAdSlots = new ArrayList<CompanionAdSlot>();

    //Initialize OoyalaPlayer-IMA Bridge
    _ooyalaPlayerWrapper = new OoyalaPlayerIMAWrapper(_player, this);
    _player.registerPlugin(this);

    //Initialize IMA classes
    _sdkFactory = ImaSdkFactory.getInstance();
    _adsLoader = _sdkFactory.createAdsLoader(this.layout.getContext(), _sdkFactory.createImaSdkSettings());

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
        if(timeoutThread != null && timeoutThread.isAlive()) {
          timeoutThread.interrupt();
        }
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
                    if (cuePoint != 0 && cuePoint >= currentContentPlayheadTime) {
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
                break;
              case ALL_ADS_COMPLETED:
                _allAdsCompleted = true;
                break;
              case COMPLETED:
                break;
              case PAUSED:
                break;
              case SKIPPED:
                skipAd();
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

  public OoyalaIMAManager(OoyalaPlayer ooyalaPlayer) {
    this(ooyalaPlayer, null);
  }

  /**
   * Specify a list of views that the IMA Manager can use to show companion ads.
   * @param companionAdView The AdView to hold the companion ad slot
   * @param width the width of companion ad view
   * @param height the height of companion ad view
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
    _container.setAdContainer(layout);
    DebugMode.logD(TAG, "IMA Managaer: Requesting ads: " + url);
    AdsRequest request = _sdkFactory.createAdsRequest();
    request.setAdTagUrl(url);
    request.setContentProgressProvider(_ooyalaPlayerWrapper);

    if (_companionAdSlots != null) {
      _container.setCompanionSlots(_companionAdSlots);
    }

    request.setAdDisplayContainer(_container);
    _adsLoader.requestAds(request);

    timeoutThread = new Thread() {
          @Override
          public void run() {
            try {
              Thread.sleep(TIMEOUT);
              new Handler( Looper.getMainLooper() ).post( new Runnable() {
                @Override
                public void run() {
                  timeout();
                }
              } );
            } catch (InterruptedException e) {
              DebugMode.logD(TAG, "InterruptedException " + e + "while waiting for IMA ads request response");
            }
          }
      };
      timeoutThread.start();
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

  /**
   * @return true if no pre-roll, false otherwise
   */
  @Override
  public boolean onInitialPlay() {
    DebugMode.logD(TAG, "IMA Ads Manager: onInitialPlay");
    if (_adsManager != null) {
      _adsManager.init();
      fetchCuePoint();
      _adsManager.start();
      if (_cuePoints.size() == 0) {
        // Non-ad-rules ima ads are always for pre-roll
        return true;
      }
    }
    return (_cuePoints != null && _cuePoints.contains(0));
  }

  /**
   * Return true if there is a URL to load
   * @return true if an adTagURL exists
   */
  @Override
  public boolean onContentChanged() {
    DebugMode.logD(TAG, "IMA Ads Manager: onContentChanged");
    destroy();
    resetFields();
    if (getAdTagFromCurrentItemOrUrlOverride(_player.getCurrentItem(), _adUrlOverride) != null) {
      return true;  //True if you want to block, false otherwise
    }

    DebugMode.logE(TAG, "No Ad URL Available, even though IMA is loaded");
    return false;
  }

  /**
   * Always return false since we only play ads when IMA SDK sends notification
   * @param playhead current playhead time
   * @return false
   */
  @Override
  public boolean onPlayheadUpdate(int playhead) {
    // We do not know when to play ads until the IMAAdManager send the notification
    // so we always return false;
    return false;
  }

  /**
   * @return true if has post-roll, flase otherwise
   */
  @Override
  public boolean onContentFinished() {
    // This is the time we need to check should we play post-roll
    DebugMode.logD(TAG, "IMA Ads Manager: onContentFinished");
    _adsLoader.contentComplete();
    if (_allAdsCompleted) {
      return false;
    }
    return true;
  }

  /**
   * Always return false for IMA Ads since we only play ads when IMA SDK sends notification
   * @param cuePointIndex (never used)
   * @return false
   */
  @Override
  public boolean onCuePoint(int cuePointIndex) {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * Fire a IMAAdErrorCallback to IMA SDK
   * @param errorCode (never used)
   * @return false
   */
  @Override
  public boolean onContentError(int errorCode) {
    // Handled by call back
    DebugMode.logD(TAG, "IMA Ads Manager: onContentError");
    _ooyalaPlayerWrapper.fireIMAAdErrorCallback();
    return false;
  }

  /**
   * Load current IMA ad if no ad loaded and a IMA ad tag url is available
   */
  @Override
  public void onAdModeEntered() {
    // nothing need to be done here for Google IMA since we fire the AdModeEnter request first
    DebugMode.logD(TAG, "IMA Ads Manager: onAdModeEntered");
    if (_adsManager == null) {

      String url = getAdTagFromCurrentItemOrUrlOverride(_player.getCurrentItem(), _adUrlOverride);
      if(url != null) {
        DebugMode.logD(TAG, "Start Loading ads after CURRENT_ITEM_CHANGED_NOTIFICATION");
        loadAds(url);
      }
      else {
        DebugMode.assertFail(TAG, "Ad Mode Entered, but there is no Ad URL");
        _player.exitAdMode(this);
      }
    }
  }

  private String getAdTagFromCurrentItemOrUrlOverride(Video currentItem, String adUrlOverride) {
    final boolean hasBacklotAdMetadata = currentItem.getModuleData() != null &&
            currentItem.getModuleData().get("google-ima-ads-manager") != null &&
            currentItem.getModuleData().get("google-ima-ads-manager").getMetadata() != null;

    if (adUrlOverride != null) {
      return adUrlOverride;
    } else if (hasBacklotAdMetadata){
      return _adUrlOverride != null ? _adUrlOverride : currentItem.getModuleData().get("google-ima-ads-manager").getMetadata().get("adTagUrl");
    } else {
      return null;
    }
  }
  /**
   * Suspend ads playback
   */
  @Override
  public void suspend() {
    // TODO Auto-generated method stub
    DebugMode.logD(TAG, "IMA Ads Manager: suspend");
    _adPlayer.suspend();
    _ooyalaPlayerWrapper.fireVideoSuspendCallback();
  }

  /**
   * Resume suspended ads playback
   */
  @Override
  public void resume() {
    // TODO Auto-generated method stub
    DebugMode.logD(TAG, "IMA Ads Manager: resume");
    if (_adPlayer != null) {
      _adPlayer.resume();
    }
    _ooyalaPlayerWrapper.fireIMAAdResumeCallback();
  }

  /**
   * Resume from given time with given state, but does not work for IMA Ads Playback
   * @param timeInMilliSecond (never used)
   * @param stateToResume (never used)
   */
  @Override
  public void resume(int timeInMilliSecond, State stateToResume) {
    // TODO Auto-generated method stub
    // Can we resume in this way for Google IMA??
    // Currently we just do the normal resume
    resume();
  }

  /**
   * Destroy IMAAdsManager and all related fields.
   */
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

  /**
   * @return current IMAAdPlayer
   */
  @Override
  public PlayerInterface getPlayerInterface() {
    // TODO Auto-generated method stub
    return _adPlayer;
  }

  /**
   * Reset all IMA Ads Playback related fields and restart ads Playback
   */
  @Override
  public void resetAds() {
    DebugMode.logD(TAG, "IMA Ads Manager: reset");
    // TODO Auto-generated method stub
    resetFields();
    onInitialPlay();
  }

  /**
   * Skip current ad playback
   */
  @Override
  public void skipAd() {
    DebugMode.logD(TAG, "IMA Ads Manager: skipAd");
    // TODO Auto-generated method stub
    _adPlayer.notifyAdSkipped();
    _adsManager.skip();
  }

  private void resetFields() {
    DebugMode.logD(TAG, "IMA Ads Manager: resetFields");
    _cuePoints = null;
    _allAdsCompleted = false;
  }

  /**
   * Reset all IMA Ads Playback related fields and restart ads Playback
   */
  @Override
  public void reset() {
    // TODO Auto-generated method stub
    resetAds();
  }

  /**
   * Fetch cue points for current ad tag url
   */
  @Override
  public Set<Integer> getCuePointsInMilliSeconds() {
    if (_cuePoints != null) {
      return new HashSet<Integer>(_cuePoints);
    }
    return new HashSet<Integer>();
  }

  private void timeout() {
    DebugMode.logD(TAG, "Requesting ads timeout");
    _player.exitAdMode(this);
  }

  @Override
  public void processClickThrough() {
    // do nothing. the click through UI and event is handled by google IMA.
  }
}
