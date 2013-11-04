package com.ooyala.android.freewheelsdk;

import java.util.List;

import tv.freewheel.ad.interfaces.IAdContext;
import tv.freewheel.ad.interfaces.ISlot;

import com.ooyala.android.AdSpot;

/**
 * The ad spot that holds a list of ISlots (ads) and the Freewheel context
 */
public class FWAdSpot extends AdSpot {

  private List<ISlot> _ads;
  private IAdContext _fwContext;

  /**
   * Initialize a Freewheel Ad Spot. Note that this AdSpot does not actually have a stream like other AdSpots
   * @param ad an ISlot to be played
   */
  public FWAdSpot(List<ISlot> ads, IAdContext fwContext) {
    _ads = ads;
    _fwContext = fwContext;
  }

  public List<ISlot> getAdsList() {
    return _ads;
  }

  public IAdContext getContext() {
    return _fwContext;
  }

  @Override
  public boolean fetchPlaybackInfo() {
    return true;
  }
}