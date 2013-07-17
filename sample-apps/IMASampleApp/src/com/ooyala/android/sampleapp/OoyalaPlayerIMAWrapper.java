package com.ooyala.android.sampleapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.util.Log;

import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer.VideoAdPlayerCallback;
import com.ooyala.android.AdSpot;
import com.ooyala.android.IMAAdSpot;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;

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
    player.addObserver(this);
    isPlayingIMAAd = false;
    completeCallback = c;
    liveContentTimePlayed = 0;
  }
  // Methods implementing VideoAdPlayer interface.

  @Override
  public void playAd() {
    Log.d(TAG, "Playing Ad");
    isPlayingIMAAd = true;

    for (VideoAdPlayerCallback callback : adCallbacks) {
      callback.onPlay();
    }
    player.playAd(adSpot);
    //stopAd();
    //video.start();
  }

  @Override
  public void stopAd() {
    Log.d(TAG, "Stopping Ad");
//    video.stopPlayback();
  }

  @Override
  public void loadAd(String url) {
    Log.d(TAG, "Loading Ad: " + url);
    adSpot = new IMAAdSpot(url);
//    video.setVideoPath(url);
  }

  @Override
  public void pauseAd() {
    Log.d(TAG, "Pausing Ad");
    if(isPlayingIMAAd && player.isShowingAd()) {
      player.pause();
    }
    else {
      Log.e(TAG, "Pausing an ad when an IMA Ad isn't even playing!!");
    }
//    video.pause();
  }

  @Override
  public void resumeAd() {
    Log.d(TAG, "Resuming Ad");
//    video.start();
  }

  @Override
  public void addCallback(VideoAdPlayerCallback callback) {
    Log.d(TAG, "Add Callback");
    adCallbacks.add(callback);
//    video.addCallback(callback);
  }

  @Override
  public void removeCallback(VideoAdPlayerCallback callback) {
    Log.d(TAG, "Removing Callback");
    adCallbacks.remove(callback);
//    video.removeCallback(callback);
  }

  @Override
  public VideoProgressUpdate getProgress() {
   int durationMs =  player.getDuration();
   int playheadMs = player.getPlayheadTime();

   if(!isPlayingIMAAd) {
     playheadMs += liveContentTimePlayed;
     Log.d(TAG, "added livecontent:" + liveContentTimePlayed);
   }

//    if (durationMs <= 0) {
//      Log.d(TAG, "GetProgress Not Ready");
//      return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
//    }
    if (durationMs == 0) durationMs = Integer.MAX_VALUE;
    Log.d(TAG, "GetProgress time: " + playheadMs + ", duration: " + durationMs);
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
