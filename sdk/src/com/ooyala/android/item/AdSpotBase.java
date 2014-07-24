package com.ooyala.android.item;

public abstract class AdSpotBase implements Comparable<AdSpotBase> {
  /**
   * Fetch the time at which this AdSpot should play.
   * @return The time at which this AdSpot should play in milliseconds.
   */
  public abstract int getTime();

  public int compareTo(AdSpotBase ad) {
    return this.getTime() - ad.getTime();
  }

}
