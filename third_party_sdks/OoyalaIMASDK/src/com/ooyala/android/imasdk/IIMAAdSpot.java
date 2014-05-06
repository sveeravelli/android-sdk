package com.ooyala.android.imasdk;

import java.util.Set;

import com.ooyala.android.item.Stream;

interface IIMAAdSpot {

  /**
   * Returns the Set of streams for the IMA Ad Spot
   * @return A set that contains the single IMA stream
   */
  public Set<Stream> getStreams();

  public OoyalaIMAManager getImaManager();

}