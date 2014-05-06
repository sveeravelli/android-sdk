package com.ooyala.android.imasdk;


import java.util.HashSet;
import java.util.Set;

import com.ooyala.android.item.AdSpot;
import com.ooyala.android.item.Stream;

/**
 * The ad spot that holds the single video stream for an IMA ad spot
 * @author michael.len
 *
 */
class IMAAdSpot extends AdSpot implements IIMAAdSpot {

  private final OoyalaIMAManager _imaManager;
  private final Stream _stream;

  /**
   * Initialize an IMA Ad Spot
   * @param url the URL for the video stream provided by the IMA Ad Manager
   */
  public IMAAdSpot( String url, OoyalaIMAManager imaManager ) {
    this( url, imaManager, AdSpot.REUSABLE ); 
  }
  
  public IMAAdSpot( String url, OoyalaIMAManager imaManager, boolean isOneTimeUse ) {
    super( isOneTimeUse );
    _imaManager = imaManager;
    _stream = new IMAStream(url);
  }

  @Override
  public boolean fetchPlaybackInfo() {
    return true;
  }

  @Override
  public Set<Stream> getStreams() {
    Set<Stream> retVal = new HashSet<Stream>();
    retVal.add(_stream);
    return retVal;
  }
  
  @Override
  public OoyalaIMAManager getImaManager() {
    return _imaManager;
  }

}
