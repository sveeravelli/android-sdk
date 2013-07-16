package com.ooyala.android.sampleapp;

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
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer.VideoAdPlayerCallback;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayoutController;
import com.ooyala.android.sampleapp.OoyalaPlayerIMAWrapper.CompleteCallback;


public class OoyalaIMAManager implements AdErrorListener, AdsLoadedListener, AdEventListener, CompleteCallback {
  protected AdsLoader adsLoader;
  protected AdsManager adsManager;
  protected AdDisplayContainer container;
  protected ImaSdkFactory sdkFactory;
  protected ImaSdkSettings sdkSettings;
  protected OoyalaPlayerLayoutController layoutController;
  protected OoyalaPlayerIMAWrapper ooyalaPlayerWrapper;

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

  public OoyalaIMAManager(Context c, String url, OoyalaPlayerLayoutController layoutController) {
    this.layoutController = layoutController;
    ooyalaPlayerWrapper = new OoyalaPlayerIMAWrapper(layoutController.getPlayer(), this);
    sdkFactory = ImaSdkFactory.getInstance();
    //createAdsLoader
    adsLoader = sdkFactory.createAdsLoader(c, getImaSdkSettings());
    adsLoader.addAdErrorListener(this);
    adsLoader.addAdsLoadedListener(this);

    //buildAdsRequest
    container = sdkFactory.createAdDisplayContainer();
    container.setPlayer(ooyalaPlayerWrapper);
    container.setAdContainer(layoutController.getLayout());
    Log.d(TAG, "Requesting ads");
    AdsRequest request = sdkFactory.createAdsRequest();
    request.setAdTagUrl(url);

//    ArrayList<CompanionAdSlot> companionAdSlots = new ArrayList<CompanionAdSlot>();

    //CompanionAdSlot companionAdSlot = sdkFactory.createCompanionAdSlot();
    //companionAdSlot.setContainer(companionView);
    //companionAdSlot.setSize(300, 50);
    //companionAdSlots.add(companionAdSlot);

//    if (leaderboardCompanionView != null) {
//      CompanionAdSlot leaderboardCompanionAdSlot = sdkFactory.createCompanionAdSlot();
//      leaderboardCompanionAdSlot.setContainer(leaderboardCompanionView);
//      leaderboardCompanionAdSlot.setSize(728, 90);
//      companionAdSlots.add(leaderboardCompanionAdSlot);
//    }

//    container.setCompanionSlots(companionAdSlots);
    request.setAdDisplayContainer(container);

    adsLoader.requestAds(request);
  }

  @Override
  public void onAdError(AdErrorEvent event) {
    Log.e(TAG, event.getError().getMessage() + "\n");
  }

  @Override
  public void onAdsManagerLoaded(AdsManagerLoadedEvent event) {
    Log.d(TAG, "Ads loaded!");
    adsManager = event.getAdsManager();
    adsManager.addAdErrorListener(this);
    adsManager.addAdEventListener(this);
    Log.d(TAG, "Calling init.");
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
  //        if (contentStarted) {
  //          videoPlayer.pauseContent();
  //        }
    //    layoutController.getPlayer().suspend();
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

}
