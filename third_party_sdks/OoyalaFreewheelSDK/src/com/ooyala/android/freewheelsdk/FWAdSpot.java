package com.ooyala.android.freewheelsdk;

import tv.freewheel.ad.interfaces.ISlot;

import com.ooyala.android.DebugMode;
import com.ooyala.android.item.AdSpotBase;

/**
 * The ad spot that holds a list of ISlots (ads) and the Freewheel context
 */
public class FWAdSpot extends AdSpotBase {
  private static final String TAG = FWAdSpot.class.getName();
  private ISlot _ad;

  private FWAdSpot(ISlot ad) {
    _ad = ad;
  }

  /**
   * Initialize a Freewheel Ad Spot. Note that this AdSpot does not actually have a stream like other AdSpots
   * @param ad the ISlot to play
   * @param adManager the Freewheel ad manager
   */
  public static FWAdSpot create(ISlot ad) {
    if (ad == null) {
      DebugMode.assertFail(TAG, "FWAdSpot.create error, ad is null");
      return null;
    }
    return new FWAdSpot(ad);
  }

  public ISlot getAd() {
    return _ad;
  }

  /**
   * Fetch the time at which this AdSpot should play.
   * @return The time at which this AdSpot should play in milliseconds.
   */
  public int getTime() {
    //Ad may be null if pre-rolls have not been fetched yet
    return (int) (_ad.getTimePosition() * 1000);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof FWAdSpot)) {
      return false;
    }

    return ((FWAdSpot) obj).getAd() == _ad;
  }
}