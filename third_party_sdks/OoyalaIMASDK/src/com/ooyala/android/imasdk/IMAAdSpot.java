package com.ooyala.android.imasdk;


import java.util.HashSet;
import java.util.Set;

import com.ooyala.android.AdSpot;
import com.ooyala.android.Stream;

/**
 * The ad spot that holds the single video stream for an IMA ad spot
 * @author michael.len
 *
 */
class IMAAdSpot extends AdSpot {

  private Stream _stream;

  /**
   * Initialize an IMA Ad Spot
   * @param url the URL for the video stream provided by the IMA Ad Manager
   */
  public IMAAdSpot(String url) {
    super();
    _stream = new IMAStream(url);
  }

  @Override
  public boolean fetchPlaybackInfo() {
    return true;
  }

  /**
   * Returns the Set of streams for the IMA Ad Spot
   * @return A set that contains the single IMA stream
   */
  public Set<Stream> getStreams() {
    Set<Stream> retVal = new HashSet<Stream>();
    retVal.add(_stream);
    return retVal;
  }

}
