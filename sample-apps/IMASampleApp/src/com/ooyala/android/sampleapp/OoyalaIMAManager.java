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
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer.VideoAdPlayerCallback;
import com.ooyala.android.ModuleData;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayoutController;
import com.ooyala.android.Video;
import com.ooyala.android.sampleapp.OoyalaPlayerIMAWrapper.CompleteCallback;


public class OoyalaIMAManager implements AdErrorListener, AdsLoadedListener, AdEventListener, CompleteCallback, Observer {
  protected AdsLoader adsLoader;
  protected AdsManager adsManager;
  protected AdDisplayContainer container;
  protected ImaSdkFactory sdkFactory;
  protected ImaSdkSettings sdkSettings;

  protected OoyalaPlayerLayoutController layoutController;
  protected OoyalaPlayerIMAWrapper ooyalaPlayerWrapper;
  protected OoyalaPlayer player;

  static String TAG = "OoyalaIMAManager";
  protected ImaSdkSettings getImaSdkSettings() {
    if (sdkSettings == null) {
      sdkSettings = sdkFactory.createImaSdkSettings();
    }
    return sdkSettings;
  }

  @Override
  public void onComplete() {
      adsLoader.contentComplete();
  }

  public OoyalaIMAManager(Context c, OoyalaPlayerLayoutController layoutController) {
    this.layoutController = layoutController;
    player = layoutController.getPlayer();

    //Initialize OoyalaPlayer-IMA Bridge
    ooyalaPlayerWrapper = new OoyalaPlayerIMAWrapper(layoutController.getPlayer(), this);
    layoutController.getPlayer().registerAdPlayer(IMAAdSpot.class, IMAAdPlayer.class);
    layoutController.getPlayer().addObserver(this);

    //Initialize IMA classes
    sdkFactory = ImaSdkFactory.getInstance();
    adsLoader = sdkFactory.createAdsLoader(c, getImaSdkSettings());
    adsLoader.addAdErrorListener(this);
    adsLoader.addAdsLoadedListener(this);


  }

  public void loadAds(String url) {
    loadAds(url, null);
  }
  public void loadAds(String url, List<CompanionAdSlot> companionAdSlots) {
    //buildAdsRequest
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
