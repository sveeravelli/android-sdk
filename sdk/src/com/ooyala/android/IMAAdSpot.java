package com.ooyala.android;

import java.util.HashSet;
import java.util.Set;

import android.util.Log;


public class IMAAdSpot extends AdSpot {

  private Stream stream;
  static String TAG="IMAAdSpot";
  public IMAAdSpot(String url) {
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
