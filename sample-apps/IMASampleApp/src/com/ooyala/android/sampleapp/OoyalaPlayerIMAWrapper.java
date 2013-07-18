package com.ooyala.android.sampleapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.util.Log;

import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.ooyala.android.AdSpot;
import com.ooyala.android.OoyalaPlayer;

public class OoyalaPlayerIMAWrapper implements VideoAdPlayer, Observer {
  static String TAG = "OoyalaPlayerIMAWrapper";

  OoyalaPlayer player;
  private AdSpot adSpot;
  private boolean isPlayingIMAAd;
  private final List<VideoAdPlayerCallback> adCallbacks = new ArrayList<VideoAdPlayerCallback>(1);
  private final CompleteCallback completeCallback;
  private int liveContentTimePlayed;

  public interface CompleteCallback {
    public void onComplete();
  }

  public OoyalaPlayerIMAWrapper(OoyalaPlayer p, CompleteCallback c){
    player = p;
    Log.d(TAG, "Creating IMA Wrapper");
    isPlayingIMAAd = false;
    completeCallback = c;
    liveContentTimePlayed = 0;
    player.addObserver(this);
  }


  // Methods implementing VideoAdPlayer interface.
  @Override
  public void playAd() {
    Log.d(TAG, "Playing Ad");
    isPlayingIMAAd = true;

    player.playAd(adSpot);
  }

  @Override
  public void stopAd() {
    Log.d(TAG, "Stopping Ad");
    if(isPlayingIMAAd && player.isShowingAd()) {
      player.suspend();
    }
    else {
      Log.i(TAG, "Stopping an ad when an IMA Ad isn't even playing!!");
    }
  }

  @Override
  public void loadAd(String url) {
    Log.d(TAG, "Loading Ad: " + url);
    adSpot = new IMAAdSpot(url);
  }

  @Override
  public void pauseAd() {
    Log.d(TAG, "Pausing Ad");
    if(isPlayingIMAAd && player.isShowingAd()) {
      player.pause();
    }
    else {
      Log.i(TAG, "Pausing an ad when an IMA Ad isn't even playing!!");
    }
  }

  @Override
  public void resumeAd() {
    Log.d(TAG, "Resuming Ad");
    if(isPlayingIMAAd && player.isShowingAd()) {
      player.resume();
    }
    else {
      Log.i(TAG, "Resuming an ad when an IMA Ad isn't even playing!!");
    }
  }

  @Override
  public void addCallback(VideoAdPlayerCallback callback) {
    Log.d(TAG, "Add Callback");
    adCallbacks.add(callback);
  }

  @Override
  public void removeCallback(VideoAdPlayerCallback callback) {
    Log.d(TAG, "Removing Callback");
    adCallbacks.remove(callback);
  }

  @Override
  public VideoProgressUpdate getProgress() {
   int durationMs =  player.getDuration();
   int playheadMs = player.getPlayheadTime();

   if(!isPlayingIMAAd) {
     playheadMs += liveContentTimePlayed;
   }

    if (durationMs == 0) durationMs = Integer.MAX_VALUE;
    Log.v(TAG, "GetProgress time: " + playheadMs + ", duration: " + durationMs);
    return new VideoProgressUpdate(playheadMs, durationMs);
  }

  public void pauseContent(){
    if(player.getCurrentItem().isLive()) {
      liveContentTimePlayed = liveContentTimePlayed + player.getPlayheadTime();
    }
  }

  public void playContent(){
    for (VideoAdPlayerCallback callback : adCallbacks) {
      callback.onPlay();
    }
    if(isPlayingIMAAd) {
      player.skipAd();
    }
    player.resume();
  }

  @Override
  public void update(Observable arg0, Object arg) {
    OoyalaPlayer player = (OoyalaPlayer) arg0;
    String notification = arg.toString();

    //If playing a non-IMA ad
    if(!isPlayingIMAAd && player.isShowingAd()) {

      //If starting a non-IMA Ad, we're pausing the content
      if (notification.equals(OoyalaPlayer.AD_STARTED_NOTIFICATION)) {
        Log.d(TAG, "Update: Non IMA ad playing");
        for (VideoAdPlayerCallback callback : adCallbacks) {
          callback.onPause();
        }
      }
      //If completing a non-IMA ad
      else if (notification.equals(OoyalaPlayer.AD_COMPLETED_NOTIFICATION)) {
        Log.d(TAG, "Update: Non IMA ad completed");
      }


    }
    else if (isPlayingIMAAd){
      //If an IMA ad is playing while state is being changed
      if(notification.equals(OoyalaPlayer.STATE_CHANGED_NOTIFICATION) && player.isShowingAd()) {
        switch (player.getState()) {
        case PLAYING:
          Log.d(TAG, "Update: Player  Ad start");
          for (VideoAdPlayerCallback callback : adCallbacks) {
            callback.onPlay();
          }
          break;
        case PAUSED:
          Log.d(TAG, "Update: Player  Ad Pause");
          for (VideoAdPlayerCallback callback : adCallbacks) {
            callback.onPause();
          }
        default:
          break;
        }
      }

      //If we get an AD_COMPLETE during an IMA ad, our ad has finished
      else if (notification.equals(OoyalaPlayer.AD_COMPLETED_NOTIFICATION)) {
        Log.d(TAG, "Update: Player  Ad Complete");
        isPlayingIMAAd = false;
        for (VideoAdPlayerCallback callback : adCallbacks) {
          callback.onEnded();
        }
      }
    }

    //Notifications from content playback
    else {
      if (notification.equals(OoyalaPlayer.STATE_CHANGED_NOTIFICATION)) {
        switch (player.getState()) {
        case PLAYING:
          Log.d(TAG, "Update: Player Content start");
          for (VideoAdPlayerCallback callback : adCallbacks) {
            callback.onPlay();
          }
          break;
        case PAUSED:
          Log.d(TAG, "Update: Player Content Pause");
          for (VideoAdPlayerCallback callback : adCallbacks) {
            callback.onPause();
          }
          break;
        default:
          break;
        }
      }
      else if (notification.equals(OoyalaPlayer.PLAY_COMPLETED_NOTIFICATION)) {
        Log.d(TAG, "Update: Player Content Complete");
        completeCallback.onComplete();
      }
    }
  }

}
