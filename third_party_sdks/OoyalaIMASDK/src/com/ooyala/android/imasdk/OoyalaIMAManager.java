package com.ooyala.android.imasdk;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.ooyala.android.Video;

/**
 * The OoyalaIMAManager will play back all IMA ads affiliated with any playing Ooyala asset. This will
 * automatically be configured, as long as the VAST URL is properly configured in Third Module Metadata.
 *
 * The OoyalaIMAManager works most completely with an OptimizedOoyalaPlayerLayoutController.  If you do not
 * use this layout controller, you will not see IMA's "Learn More" button when in fullscreen mode.
 * @author michael.len
 *
 */
public class OoyalaIMAManager implements Observer {
  private static String TAG = "OoyalaIMAManager";

  protected AdsLoader _adsLoader;
  protected AdsManager _adsManager;
  protected AdDisplayContainer _container;
  protected ImaSdkFactory _sdkFactory;
  protected ImaSdkSettings _sdkSettings;

  protected OoyalaPlayerIMAWrapper _ooyalaPlayerWrapper;
  protected List<CompanionAdSlot> _companionAdSlots;
  protected Map<String,String> _adTagParameters;
  protected OoyalaPlayer _player;
  private String _adUrlOverride;
  private boolean _queueAdsManagerInit = false;
  protected boolean _adsManagerInited;

  /**
   * Initialize the Ooyala IMA Manager, which will play back all IMA ads affiliated with any playing Ooyala
   * asset. This will automatically be configured, as long as the VAST URL is properly configured in Third
   * Module Metadata.
   * @param context The context of the activity, which will be used to redirect end users to the browser
   * @param layoutController The Ooyala layout controller you initialized
   */
  public OoyalaIMAManager(OoyalaPlayer ooyalaPlayer) {
    _player = ooyalaPlayer;
    _companionAdSlots = new ArrayList<CompanionAdSlot>();

    //Initialize OoyalaPlayer-IMA Bridge
    _ooyalaPlayerWrapper = new OoyalaPlayerIMAWrapper(_player, this);
    _player.registerAdPlayer(IMAAdSpot.class, IMAAdPlayer.class);
    _player.registerAdPlayer(IMAEmptyAdSpot.class, IMAAdPlayer.class);
    _player.addObserver(this);

    //Initialize IMA classes
    _sdkFactory = ImaSdkFactory.getInstance();
    _adsLoader = _sdkFactory.createAdsLoader(_player.getLayout().getContext(), _sdkFactory.createImaSdkSettings());

    //Create the listeners for the adsLoader and adsManager
    _adsLoader.addAdErrorListener(new AdErrorListener() {
      @Override
      public void onAdError(AdErrorEvent event) {
        Log.e(TAG, "IMA AdsLoader Error: " + event.getError().getMessage() + "\n");
        Log.e(TAG, "IMA AdsLoader Error: doing adPlayerCompleted()" );
        _player.adPlayerCompleted();
      }
    } );
    
    _adsLoader.addAdsLoadedListener(new AdsLoadedListener() {
      
      @Override
      public void onAdsManagerLoaded(AdsManagerLoadedEvent event) {
        Log.d(TAG, "IMA AdsManager loaded");
        _adsManager = event.getAdsManager();
        _adsManagerInited = false;
 
        _adsManager.addAdErrorListener(new AdErrorListener() {
          @Override
          public void onAdError(AdErrorEvent event) {
            Log.e(TAG, "IMA AdsManager Error: " + event.getError().getMessage() + "\n");
            Log.e(TAG, "IMA AdsLoader Error: doing adPlayerCompleted()" );
            _player.adPlayerCompleted();
          }
        } );
        
        _adsManager.addAdEventListener(new AdEventListener() {

          @Override
          public void onAdEvent(AdEvent event) {

            Log.d(TAG,"IMA AdsManager Event: " + event.getType());

            switch (event.getType()) {
              case LOADED:
                Log.d(TAG,"IMA Ad Manager: Starting ad");
                if( _adsManager != null ) {
                  _adsManager.start();
                }
                break;
              case CONTENT_PAUSE_REQUESTED:
                _ooyalaPlayerWrapper.pauseContent();
                break;
              case CONTENT_RESUME_REQUESTED:
                _ooyalaPlayerWrapper.playContent();
                break;
              case STARTED:
                break;
              case COMPLETED:
                break;
              case PAUSED:
                break;
              case RESUMED:
                break;
              default:
                break;
            }
          }
        });

        //Sometimes the ads manager will be created late, after PLAY_STARTED_NOTIFICATION
        // We still need to init the manager in this case
        // todo: make sure this works still.
       if(_queueAdsManagerInit && !_adsManagerInited && _adsManager != null) {
         _adsManager.init();
         _adsManagerInited = true;
         _queueAdsManagerInit = false;
       }
      }
    });
  }

  /**
   * Specify a list of views that the IMA Manager can use to show companion ads.
   * @param _companionAdSlots
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
      Log.d(TAG, "IMA Managaer: The customer is loading ads a second time!");
    }

    if (_adTagParameters != null) {
      for(String key : _adTagParameters.keySet()) {
        url += (url.contains("?") ? "&" : "?") + key + "=" + _adTagParameters.get(key);
      }
    }
    _container = _sdkFactory.createAdDisplayContainer();
    _container.setPlayer(_ooyalaPlayerWrapper);
    _container.setAdContainer(_player.getLayout());
    Log.d(TAG, "IMA Managaer: Requesting ads: " + url);
    AdsRequest request = _sdkFactory.createAdsRequest();
    request.setAdTagUrl(url);

    if (_companionAdSlots != null) {
      _container.setCompanionSlots(_companionAdSlots);
    }

    request.setAdDisplayContainer(_container);
    _adsLoader.requestAds(request);
  }
  
  private void addPreRollAdSpotToItem( Video item ) {
    item.insertAd( new IMAEmptyAdSpot( this ) );
  }
  
  @Override
  public void update(Observable observable, Object data) {
    if (data.toString().equals(OoyalaPlayer.CURRENT_ITEM_CHANGED_NOTIFICATION)) {
      if (_adsManager != null) {
        _adsManager.destroy();
        _adsLoader.contentComplete();
      }
      _adsManager = null;

      Video currentItem = _player.getCurrentItem();

      final boolean isBacklotIMA = currentItem.getModuleData() != null &&
          currentItem.getModuleData().get("google-ima-ads-manager") != null &&
          currentItem.getModuleData().get("google-ima-ads-manager").getMetadata() != null;
      final boolean isOverrideIMA = _adUrlOverride != null;
      if ( isBacklotIMA || isOverrideIMA ) {
        String url = _adUrlOverride != null ? _adUrlOverride : currentItem.getModuleData().get("google-ima-ads-manager").getMetadata().get("adTagUrl");
        if(url != null) {
          addPreRollAdSpotToItem( currentItem );
          loadAds(url);
        }
      }
    }
    else if (data.toString().equals(OoyalaPlayer.PLAY_STARTED_NOTIFICATION)) {
      if (!_adsManagerInited) {
        if (_adsManager != null) {
          _adsManagerInited = true;
          _adsManager.init();
        } else {
          _queueAdsManagerInit = true;
        }
      }
    }
    else if (data.toString().equals(OoyalaPlayer.PLAY_COMPLETED_NOTIFICATION)) {
      Log.d(TAG, "IMA Ad Update: Player Content Complete");
      _adsLoader.contentComplete();
    }
  }

}
