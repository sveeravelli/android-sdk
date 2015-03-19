package com.ooyala.android.imasdk;


import com.ooyala.android.item.OoyalaManagedAdSpot;
import com.ooyala.android.item.Stream;

import java.util.HashSet;
import java.util.Set;

/**
 * The ad spot that holds the single video stream for an IMA ad spot
 *
 */
class IMAAdSpot extends OoyalaManagedAdSpot {

  private final OoyalaIMAManager _imaManager;
  private final Stream _stream;

  /**
   * Initialize an IMA Ad Spot
   * @param url the URL for the video stream provided by the IMA Ad Manager
   */
  public IMAAdSpot(String url, OoyalaIMAManager imaManager) {
    super();
    _imaManager = imaManager;
    _stream = new IMAStream(url);
  }

  @Override
  public boolean fetchPlaybackInfo() {
    return true;
  }

  /**
   *
   * @return current a set of current streams
   */
  public Set<Stream> getStreams() {
    Set<Stream> retVal = new HashSet<Stream>();
    retVal.add(_stream);
    return retVal;
  }

  /**
   *
   * @return current OoyalaIMAManager
   */
  public OoyalaIMAManager getImaManager() {
    return _imaManager;
  }

}
