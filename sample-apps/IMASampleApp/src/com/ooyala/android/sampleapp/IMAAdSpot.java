package com.ooyala.android.sampleapp;

import java.util.HashSet;
import java.util.Set;

import com.ooyala.android.AdSpot;
import com.ooyala.android.Stream;

import android.util.Log;


public class IMAAdSpot extends AdSpot {

  private Stream stream;
  static String TAG="IMAAdSpot";
  public IMAAdSpot(String url) {
    super();
    stream = new IMAStream(url);
    Log.d(TAG, "Initing the IMA Ad Spot");
  }

  @Override
  public boolean fetchPlaybackInfo() {
    // TODO Auto-generated method stub
    return false;
  }

  public Set<Stream> getStreams() {
    Set<Stream> retVal = new HashSet<Stream>();
    retVal.add(stream);
    return retVal;
  }

}
