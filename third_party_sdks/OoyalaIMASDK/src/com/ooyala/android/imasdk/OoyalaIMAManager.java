package com.ooyala.android.imasdk;


import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.util.Log;
import android.view.ViewGroup;

import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent.AdErrorListener;
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventListener;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsLoader.AdsLoadedListener;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
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
  protected OoyalaPlayer _player;

  private class IMAAdErrorListener implements AdErrorListener {

    @Override
    public void onAdError(AdErrorEvent event) {
      Log.e(TAG, "IMA Manager Error: " + event.getError().getMessage() + "\n");

    }
  }

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
    _ooyalaPlayerWrapper = new OoyalaPlayerIMAWrapper(_player);
    _player.registerAdPlayer(IMAAdSpot.class, IMAAdPlayer.class);
    _player.addObserver(this);

    //Initialize IMA classes
    _sdkFactory = ImaSdkFactory.getInstance();
    _adsLoader = _sdkFactory.createAdsLoader(_player.getLayout().getContext(), _sdkFactory.createImaSdkSettings());

    //Create the listeners for the adsLoader and adsManager
    _adsLoader.addAdErrorListener(new IMAAdErrorListener());
    _adsLoader.addAdsLoadedListener(new AdsLoadedListener() {
      @Override
      public void onAdsManagerLoaded(AdsManagerLoadedEvent event) {
        Log.d(TAG, "IMA Ad manager loaded");
        _adsManager = event.getAdsManager();
        _adsManager.addAdErrorListener(new IMAAdErrorListener());
        _adsManager.addAdEventListener(new AdEventListener() {

          @Override
          public void onAdEvent(AdEvent event) {

            Log.d(TAG,"IMA Ad Event: " + event.getType());

            switch (event.getType()) {
              case LOADED:
                Log.d(TAG,"IMA Ad Manager: Starting ad");
                _adsManager.start();
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

        _adsManager.init();
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
   * Manually load an IMA Vast URL to initialize the IMA Manager.
   * You do not need to do this if a VAST URL is properly configured in Third Party Module Metadata.
   * It is not advised usage to manually load an IMA VAST URL while any IMA URL is configured in Third Party
   * Module Metadata.
   * @param url VAST url for IMA
   */
  public void loadAds(String url) {
    if(_container != null) {
      Log.d(TAG, "IMA Managaer: The customer is loading ads a second time!");
    }

    _container = _sdkFactory.createAdDisplayContainer();
    _container.setPlayer(_ooyalaPlayerWrapper);
    _container.setAdContainer(_player.getLayout());
    Log.d(TAG, "IMA Managaer: Requesting ads");
    AdsRequest request = _sdkFactory.createAdsRequest();
    request.setAdTagUrl(url);

    if (_companionAdSlots != null) {
      _container.setCompanionSlots(_companionAdSlots);
    }

    request.setAdDisplayContainer(_container);
    _adsLoader.requestAds(request);
  }

  @Override
  public void update(Observable observable, Object data) {
    if(data.toString().equals(OoyalaPlayer.METADATA_READY_NOTIFICATION)) {
      Video currentItem = _player.getCurrentItem();

      if (currentItem.getModuleData() != null &&
          currentItem.getModuleData().get("google-ima-ads-manager") != null &&
          currentItem.getModuleData().get("google-ima-ads-manager").getMetadata() != null ){
        String url = currentItem.getModuleData().get("google-ima-ads-manager").getMetadata().get("adTagUrl");
        if(url != null) {
          loadAds(url);
        }
      }
    }
    else if (data.toString().equals(OoyalaPlayer.PLAY_COMPLETED_NOTIFICATION)) {
      Log.d(TAG, "IMA Ad Update: Player Content Complete");
      _adsLoader.contentComplete();
    }
  }

}
