package com.ooyala.android.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ooyala.android.DebugMode;

/**
 * A helper class help us to manage ad spots
 */
public class AdSpotManager<T extends AdSpot> {
  private static final String TAG = "AdSpotManager";
  private List<T> _ads;
  private Set<T> _playedAds;
  private int _timeAlignment;
  private Map<Integer, Set<T>> _cuePointMap;

  public AdSpotManager() {
    _ads = new ArrayList<T>();
    _playedAds = new HashSet<T>();
    _timeAlignment = 0;
    _cuePointMap = new HashMap<Integer, Set<T>>();
  }

  /**
   * Mark all adspots as unplayed
   */
  public void resetAds() {
    _playedAds.clear();
    refreshCuePoints();
  }

  /**
   * Clear all adspots
   */
  public void clear() {
    _playedAds.clear();
    _ads.clear();
    _timeAlignment = 0;
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
    updateCuePoints(ad);
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
    refreshCuePoints();
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
  public T adBeforeTime(int time) {
    for (T ad : _ads) {
      int adTime = ad.getTime();
      if (_timeAlignment > 0) {
        adTime = ((adTime + _timeAlignment / 2) / _timeAlignment)
            * _timeAlignment;
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
    updateCuePoints(ad);
  }

  /**
   * get the adspot list size
   * 
   * @returns size
   */
  public int size() {
    return _ads.size();
  }

  /**
   * set the time alignment
   * 
   * @param alignment
   *          in millisecond
   */
  public void setAlignment(int alignment) {
    _timeAlignment = alignment;
  }

  /**
   * get the time alignment
   * 
   * @return the alignment in millisecond
   */
  public int getAlignment() {
    return _timeAlignment;
  }

  public Set<Integer> cuePoints() {
    return _cuePointMap.keySet();
  }

  private void refreshCuePoints() {
    _cuePointMap.clear();
    for (T ad : _ads) {
      this.updateCuePoints(ad);
    }
  }

  private void updateCuePoints(T ad) {
    if (ad == null || ad.getTime() <= 0) {
      return;
    }

    Integer key = ad.getTime();
    Set<T> adset = _cuePointMap.get(key);
    if (!_playedAds.contains(ad)) {
      // The ad is not played yet, check if we need to insert a cue point.
      // it.
      if (adset == null) {
        adset = new HashSet<T>();
        _cuePointMap.put(key, adset);
      }
      adset.add(ad);
    } else {
      // the ad is marked as played. check if we need to remove the cue point.
      if (adset != null) {
        adset.remove(ad);
        if (adset.isEmpty()) {
          _cuePointMap.remove(key);
        }
      }
    }
  }
}
