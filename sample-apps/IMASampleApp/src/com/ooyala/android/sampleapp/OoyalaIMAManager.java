package com.ooyala.android.sampleapp;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.util.Log;

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
import com.ooyala.android.OoyalaPlayerLayoutController;
import com.ooyala.android.Video;
import com.ooyala.android.sampleapp.OoyalaPlayerIMAWrapper.CompleteCallback;

/**
 * The OoyalaIMAManager will play back all IMA ads affiliated with any playing Ooyala asset. This will
 * automatically be configured, as long as the VAST URL is properly configured in Third Module Metadata.
 * @author michael.len
 *
 */
public class OoyalaIMAManager implements AdErrorListener, AdsLoadedListener, AdEventListener, CompleteCallback, Observer {
  private static String TAG = "OoyalaIMAManager";

  protected AdsLoader adsLoader;
  protected AdsManager adsManager;
  protected AdDisplayContainer container;
  protected ImaSdkFactory sdkFactory;
  protected ImaSdkSettings sdkSettings;

  protected OoyalaPlayerLayoutController layoutController;
  protected OoyalaPlayerIMAWrapper ooyalaPlayerWrapper;
  protected OoyalaPlayer player;

  protected ImaSdkSettings getImaSdkSettings() {
    if (sdkSettings == null) {
      sdkSettings = sdkFactory.createImaSdkSettings();
    }
    return sdkSettings;
  }

  /**
   * Initialize the Ooyala IMA Manager, which will play back all IMA ads affiliated with any playing Ooyala
   * asset. This will automatically be configured, as long as the VAST URL is properly configured in Third
   * Module Metadata.
   * @param context The context of the activity, which will be used to redirect end users to the browser
   * @param layoutController The Ooyala layout controller you initialized
   */
  public OoyalaIMAManager(Context context, OoyalaPlayerLayoutController layoutController) {
    this.layoutController = layoutController;
    player = layoutController.getPlayer();

    //Initialize OoyalaPlayer-IMA Bridge
    ooyalaPlayerWrapper = new OoyalaPlayerIMAWrapper(layoutController.getPlayer(), this);
    layoutController.getPlayer().registerAdPlayer(IMAAdSpot.class, IMAAdPlayer.class);
    layoutController.getPlayer().addObserver(this);

    //Initialize IMA classes
    sdkFactory = ImaSdkFactory.getInstance();
    adsLoader = sdkFactory.createAdsLoader(context, getImaSdkSettings());
    adsLoader.addAdErrorListener(this);
    adsLoader.addAdsLoadedListener(this);
  }

  /**
   * Manually load an IMA Vast URL to initialize the IMA Manager.
   * You do not need to do this if a VAST URL is properly configured in Third Party Module Metadata
   * @param url VAST url for IMA
   */
  public void loadAds(String url) {
    loadAds(url, null);
  }

  /**
   * Manually load an IMA Vast URL to initialize the IMA Manager
   * @param url VAST url for IMA
   * @param companionAdSlots Companion Ad Slots that can be given banner ads.
   */
  public void loadAds(String url, List<CompanionAdSlot> companionAdSlots) {
    if(container != null) {
      Log.d(TAG, "The customer is loading ads a second time!");
    }
    container = sdkFactory.createAdDisplayContainer();
    container.setPlayer(ooyalaPlayerWrapper);
    container.setAdContainer(layoutController.getLayout());
    Log.d(TAG, "Requesting ads");
    AdsRequest request = sdkFactory.createAdsRequest();
    request.setAdTagUrl(url);

    if (companionAdSlots != null) {
      container.setCompanionSlots(companionAdSlots);
    }

    request.setAdDisplayContainer(container);
    adsLoader.requestAds(request);
  }

  @Override
  public void onAdError(AdErrorEvent event) {
    Log.e(TAG, event.getError().getMessage() + "\n");
  }

  @Override
  public void onAdsManagerLoaded(AdsManagerLoadedEvent event) {
    Log.d(TAG, "IMA Ad manager loaded");
    adsManager = event.getAdsManager();
    adsManager.addAdErrorListener(this);
    adsManager.addAdEventListener(this);
    adsManager.init();
  }

  @Override
  public void onAdEvent(AdEvent event) {
    Log.d(TAG,"IMA Ad Event:" + event.getType());

    switch (event.getType()) {
      case LOADED:
        Log.d(TAG,"Calling start.");
        adsManager.start();
        break;
      case CONTENT_PAUSE_REQUESTED:
        ooyalaPlayerWrapper.pauseContent();
        break;
      case CONTENT_RESUME_REQUESTED:
        ooyalaPlayerWrapper.playContent();
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

  @Override
  public void onComplete() {
      adsLoader.contentComplete();
  }

  @Override
  public void update(Observable observable, Object data) {
    if(data.toString().equals(OoyalaPlayer.METADATA_READY_NOTIFICATION)) {
    //  String url="http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=%2F15018773%2Feverything2&ciu_szs=300x250%2C468x60%2C728x90&impl=s&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=[referrer_url]&correlator=[timestamp]&cmsid=133&vid=10XWSh7W4so&ad_rule=1";
      Video currentItem = player.getCurrentItem();
      String url = currentItem.getModuleData().get("google-ima-ads-manager").getMetadata().get("ad_tag_url");

      if(url != null) {
        loadAds(url);
      }
    }
  }

}
