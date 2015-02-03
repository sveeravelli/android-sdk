package com.ooyala.android.plugin;

import java.util.Set;

import com.ooyala.android.util.DebugMode;
import com.ooyala.android.item.AdSpot;
import com.ooyala.android.item.AdSpotManager;

public abstract class ManagedAdsPlugin<T extends AdSpot> implements
    AdPluginInterface {

  protected static final int PLUGIN_INIT = -2;
  protected static final int CONTENT_CHANGED = -1;

  private static final String TAG = ManagedAdsPlugin.class.getName();

  protected AdSpotManager<T> _adSpotManager;
  private int _lastAdModeTime;
  
  public ManagedAdsPlugin() {
    _adSpotManager = new AdSpotManager<T>();
    _lastAdModeTime = PLUGIN_INIT;
  }

  @Override
  public boolean onContentChanged() {
    _lastAdModeTime = CONTENT_CHANGED;
    _adSpotManager.clear();
    return false;
  }

  /**
   * called before content play starts
   * 
   * @return true if plugin needs to play preroll ads, false otherwise
   */
  @Override
  public boolean onInitialPlay() {
    DebugMode.logD(TAG, "onInitialPlay");
    _lastAdModeTime = 0;
    return _adSpotManager.adBeforeTime(_lastAdModeTime) != null;
  }

  /**
   * called during content play
   * 
   * @param playhead
   *          the current content playhead
   * @return true if plugin needs to play midroll ads, false otherwise
   */
  @Override
  public boolean onPlayheadUpdate(int playhead) {
    _lastAdModeTime = playhead;
    return _adSpotManager.adBeforeTime(_lastAdModeTime) != null;
  }

  /**
   * called after content finish
   * 
   * @return true if plugin needs to play postroll ads, false otherwise
   */
  @Override
  public boolean onContentFinished() {
    _lastAdModeTime = Integer.MAX_VALUE;
    return _adSpotManager.adBeforeTime(_lastAdModeTime) != null;
  }

  /**
   * called on cue points
   * 
   * @return true if plugin needs to play midroll ads, false otherwise
   */
  @Override
  public boolean onCuePoint(int cuePointIndex) {
    return false;
  }

  /**
   * called when content playback error happens
   * 
   * @return true if plugin needs to handle error, false otherwise
   */
  @Override
  public boolean onContentError(int errorCode) {
    return false;
  }

  /**
   * called on content
   * 
   * @return true if plugin needs to play postroll ads, false otherwise
   */
  @Override
  public void onAdModeEntered() {
    this.playAdsBeforeTime();
  }

  @Override
  public void resetAds() {
    _adSpotManager.resetAds();
  }

  @Override
  public void skipAd() {
    playAdsBeforeTime();
  }

  protected boolean playAdsBeforeTime() {
    T adToPlay = _adSpotManager.adBeforeTime(_lastAdModeTime);
    if (adToPlay == null) {
      return false;
    }
    _adSpotManager.markAsPlayed(adToPlay);
    return playAd(adToPlay);
  }

  protected abstract boolean playAd(T ad);

  /**
   * called after content finish
   * 
   * @return CONTENT_CHANGED after onContentChanged, non-negative value after
   *         content play starts
   */
  protected int getLastAdModeTime() {
    return _lastAdModeTime;
  }

  @Override
  public Set<Integer> getCuePointsInMilliSeconds() {
    return _adSpotManager.getCuePointsInMilliSeconds();
  }
}
