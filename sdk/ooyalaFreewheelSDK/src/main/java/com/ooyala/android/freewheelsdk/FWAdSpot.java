package com.ooyala.android.freewheelsdk;

import tv.freewheel.ad.interfaces.ISlot;

import com.ooyala.android.util.DebugMode;
import com.ooyala.android.item.AdSpot;

/**
 * The ad spot that holds a list of ISlots (ads) and the Freewheel context
 */
public class FWAdSpot extends AdSpot {

  private static final String TAG = FWAdSpot.class.getName();
  private ISlot _ad;
  private boolean _isPostRoll;

  private FWAdSpot(ISlot ad, boolean isPostRoll) {
    _ad = ad;
    _isPostRoll = isPostRoll;
  }

  /**
   * Initialize a Freewheel Ad Spot. Note that this AdSpot does not actually have a stream like other AdSpots
   * @param ad the ISlot to play
   * @param isPostRoll if this is a post roll spot
   */
  public static FWAdSpot create(ISlot ad, boolean isPostRoll) {
    if (ad == null) {
      DebugMode.assertFail(TAG, "FWAdSpot.create error, ad is null");
      return null;
    }
    return new FWAdSpot(ad, isPostRoll);
  }

  public ISlot getAd() {
  /**
   * Fetch the ISlot to play.
   * @return the ISlot to play
   */
   public ISlot getAd() {
    return _ad;
  }

  /**
   * Fetch the time at which this AdSpot should play.
   * @return The time at which this AdSpot should play in milliseconds.
   */
  public int getTime() {
    if (_isPostRoll) {
      return Integer.MAX_VALUE - 1;
    }
    return (int) (_ad.getTimePosition() * 1000);
  }

  /**
   * Overrides the equal method of the FWAdSpot class.
   * @param obj the object compared with current FWAdSpot
   * @return true if the object is the current FWAdSpot, otherwise false
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof FWAdSpot)) {
      return false;
    }

    return ((FWAdSpot) obj).getAd() == _ad;
  }
}