package com.ooyala.android.freewheelsdk;

import tv.freewheel.ad.interfaces.ISlot;

import com.ooyala.android.item.AdSpot;

/**
 * The ad spot that holds a list of ISlots (ads) and the Freewheel context
 */
public class FWAdSpot extends AdSpot {

  private ISlot _ad;
  private OoyalaFreewheelManager _adManager;

  /**
   * Initialize a Freewheel Ad Spot. Note that this AdSpot does not actually have a stream like other AdSpots
   * @param ad the ISlot to play
   * @param adManager the Freewheel ad manager
   */
  public FWAdSpot(ISlot ad, OoyalaFreewheelManager adManager) {
    _ad = ad;
    _adManager = adManager;
  }

  public ISlot getAd() {
    return _ad;
  }

  public OoyalaFreewheelManager getAdManager() {
    return _adManager;
  }

  @Override
  public boolean fetchPlaybackInfo() {
    return true;
  }

  /**
   * Fetch the time at which this AdSpot should play.
   * @return The time at which this AdSpot should play in milliseconds.
   */
  public int getTime() {
    //Ad may be null if pre-rolls have not been fetched yet
    if (_ad != null) {
      return (int) (_ad.getTimePosition() * 1000);
    } else {
      return 0;
    }
  }
}