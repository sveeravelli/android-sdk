package com.ooyala.android.sampleapp;

import java.util.HashSet;
import java.util.Set;

import com.ooyala.android.AdSpot;
import com.ooyala.android.Stream;

import android.util.Log;

/**
 * The ad spot that holds the single video stream for an IMA ad spot
 * @author michael.len
 *
 */
public class IMAAdSpot extends AdSpot {

  private Stream stream;
  static String TAG="IMAAdSpot";

  /**
   * Initialize an IMA Ad Spot
   * @param url the URL for the video stream provided by the IMA Ad Manager
   */
  public IMAAdSpot(String url) {
    super();
    stream = new IMAStream(url);
    Log.d(TAG, "Initing the IMA Ad Spot");
  }

  @Override
  public boolean fetchPlaybackInfo() {
    return false;
  }

  /**
   * Returns the Set of streams for the IMA Ad Spot
   * @return A set that contains the single IMA stream
   */
  public Set<Stream> getStreams() {
    Set<Stream> retVal = new HashSet<Stream>();
    retVal.add(stream);
    return retVal;
  }

}
