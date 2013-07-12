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

public class OoyalaPlayerIMAWrapper implements VideoAdPlayer, Observer {
  static String TAG = "OoyalaPlayerIMAWrapper";

  OoyalaPlayer player;
  private AdSpot adSpot;
  private final List<VideoAdPlayerCallback> adCallbacks = new ArrayList<VideoAdPlayerCallback>(1);

  public OoyalaPlayerIMAWrapper(OoyalaPlayer p){
    player = p;
    Log.d(TAG, "Creating IMA Wrapper");
    player.addObserver(this);
  }
  // Methods implementing VideoAdPlayer interface.

  @Override
  public void playAd() {
    Log.d(TAG, "Playing Ad");
    for (VideoAdPlayerCallback callback : adCallbacks) {
      callback.onPlay();
    }

    player.playAd(adSpot);
    //stopAd();
    //video.start();
  }

  @Override
  public void stopAd() {
    Log.d(TAG, "StoppingAd");
    for (VideoAdPlayerCallback callback : adCallbacks) {
      callback.onEnded();
    }
//    video.stopPlayback();
  }

  @Override
  public void loadAd(String url) {
    Log.d(TAG, "LoadingAd");
    adSpot = new IMAAdSpot(url);
//    video.setVideoPath(url);
  }

  @Override
  public void pauseAd() {
    Log.d(TAG, "Pausign Ad");
    for (VideoAdPlayerCallback callback : adCallbacks) {
      callback.onPause();
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
    Log.d(TAG, "AddCallback");
    adCallbacks.add(callback);
//    video.addCallback(callback);
  }

  @Override
  public void removeCallback(VideoAdPlayerCallback callback) {
    Log.d(TAG, "RemovingCallback");
    adCallbacks.remove(callback);
//    video.removeCallback(callback);
  }

  @Override
  public VideoProgressUpdate getProgress() {
//    int durationMs =  video.getDuration();

    Log.d(TAG, "GetProgress");
    return new VideoProgressUpdate(player.getPlayheadTime(), player.getDuration());
  }


  @Override
  public void update(Observable arg0, Object arg) {
    OoyalaPlayer player = (OoyalaPlayer) arg0;
    String notification = arg.toString();

    if(notification.equals(OoyalaPlayer.AD_COMPLETED_NOTIFICATION)) {
      for (VideoAdPlayerCallback callback : adCallbacks) {
     //   callback.onEnded();
      }
    }
  }

}
