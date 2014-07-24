package com.ooyala.android.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ooyala.android.DebugMode;

/**
 * A helper class help us to manage ad spots
 */
public class AdSpotManager<T extends AdSpotBase> {
  private static final String TAG = "AdSpotManager";
  List<T> _ads;
  Set<T> _playedAds;

  public AdSpotManager() {
    _ads = new ArrayList<T>();
    _playedAds = new HashSet<T>();
  }

  /**
   * Mark all adspots as unplayed
   */
  public void resetAds() {
    _playedAds.clear();
  }

  /**
   * Clear all adspots
   */
  public void clear() {
    _playedAds.clear();
    _ads.clear();
  }

  /**
   * Insert an adSpot
   * 
   * @param ad
   *          the adSpot to insert
   */
  public void insertAd(T ad) {
    if (ad == null) {
      DebugMode.assertFail(TAG, "try to insert a null ad");
      return;
    }

    if (_ads.contains(ad)) {
      DebugMode.assertFail(TAG, ad.toString() + " already exist");
      return;
    }

    _ads.add(ad);
    Collections.sort(_ads);
  }

  /**
   * Insert an adSpot
   * 
   * @param ad
   *          the adSpot to insert
   */
  public void insertAds(List<T> adSpots) {
    _ads.addAll(adSpots);
    Collections.sort(_ads);
  }

  /**
   * get the adspot before a certain time,
   * 
   * @param time
   *          in millisecond
   * @param timeAlignment
   *          time unit to round up time, 0 if no alignment
   * @returns the unplayed adspot before the specified time which, null if no
   *          such adspot
   */
  public T adBeforeTime(int time, int timeAlignment) {
    for (T ad : _ads) {
      int adTime = ad.getTime();
      if (timeAlignment > 0) {
        adTime = ((adTime + timeAlignment / 2) / timeAlignment) * timeAlignment;
      }
      if (adTime > time || _playedAds.contains(ad)) {
        continue;
      }
      return ad;
    }
    return null;
  }

  /**
   * mark an adspot as played
   * 
   * @param ad
   *          the adspot to be marked
   */
  public void markAsPlayed(T ad) {
    if (ad == null) {
      DebugMode.assertFail(TAG, "try to mark a null adspot");
      return;
    }

    _playedAds.add(ad);
  }

  /**
   * get the adspot list size
   * 
   * @returns size
   */
  public int size() {
    return _ads.size();
  }
}
