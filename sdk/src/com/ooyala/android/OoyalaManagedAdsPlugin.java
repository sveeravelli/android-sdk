package com.ooyala.android;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;

import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.item.AdSpot;
import com.ooyala.android.item.AdSpotManager;
import com.ooyala.android.item.Stream;
import com.ooyala.android.player.AdMoviePlayer;
import com.ooyala.android.player.Player;
import com.ooyala.android.player.PlayerInterface;
import com.ooyala.android.plugin.AdPluginInterface;

/**
 * Ooyala managed ads plugin manages ooyala and vast ads.
 */
public class OoyalaManagedAdsPlugin implements Observer, AdPluginInterface {
  private static final String TAG = OoyalaManagedAdsPlugin.class.getName();
  private WeakReference<OoyalaPlayer> _player;
  private AdMoviePlayer _adPlayer;
  private boolean _seekable = false;
  private int _timeAlignment;
  private int _lastPlayedTime;
  private AdSpotManager<AdSpot> _adSpotManager;

  /**
   * Ooyala managed ads plugin manages ooyala and vast ads.
   */
  public OoyalaManagedAdsPlugin(OoyalaPlayer player) {
    _player = new WeakReference<OoyalaPlayer>(player);
    _adSpotManager = new AdSpotManager<AdSpot>();
  }

  /**
   * called when plugin should be reset
   */
  @Override
  public void reset() {
    // TODO Auto-generated method stub

  }

  /**
   * called when plugin should be suspended
   */
  @Override
  public void suspend() {
    if (_adPlayer != null) {
      _adPlayer.suspend();
    }
  }

  /**
   * called when plugin should be resumed
   */
  @Override
  public void resume() {
    if (_adPlayer != null) {
      _adPlayer.resume();
    }
  }

  /**
   * called when plugin should be resumed
   * 
   * @param timeInMilliSecond
   *          playhead time to seek after resume
   * @param stateToResume
   *          player state after resume
   */
  @Override
  public void resume(int timeInMilliSecond, State stateToResume) {
    if (_adPlayer != null) {
      _adPlayer.resume(timeInMilliSecond, stateToResume);
    }
  }

  /**
   * called when plugin should be destroyed
   */
  @Override
  public void destroy() {
    if (_adPlayer != null) {
      _adPlayer.destroy();
    }
  }

  /**
   * called when content is changed
   */
  @Override
  public boolean onContentChanged() {
    _adSpotManager.clear();
    _adSpotManager.insertAds(_player.get().getCurrentItem().getAds());
    _timeAlignment = Stream.streamSetContainsDeliveryType(_player.get()
        .getCurrentItem().getStreams(), Stream.DELIVERY_TYPE_HLS) ? 10000 : 0;
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
    _lastPlayedTime = 0;
    return _adSpotManager.adBeforeTime(_lastPlayedTime, _timeAlignment) != null;
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
    DebugMode.logD(TAG, "onPlayheadUpdate");
    _lastPlayedTime = playhead;
    return _adSpotManager.adBeforeTime(_lastPlayedTime, _timeAlignment) != null;
  }

  /**
   * called after content finish
   * 
   * @return true if plugin needs to play postroll ads, false otherwise
   */
  @Override
  public boolean onContentFinished() {
    _lastPlayedTime = Integer.MAX_VALUE;
    return _adSpotManager.adBeforeTime(_lastPlayedTime, _timeAlignment) != null;
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
   * called when admode is granted, plugin can start ad play now.
   * 
   */
  @Override
  public void onAdModeEntered() {
    playAdsBeforeTime(_lastPlayedTime);
  }

  private boolean initializeAdPlayer(AdMoviePlayer p, AdSpot ad) {
    if (p == null) {
      DebugMode.assertFail(TAG, "initializeAdPlayer when adPlayer is null");
      return false;
    }
    if (ad == null) {
      DebugMode.assertFail(TAG, "initializeAdPlayer when ad is null");
      return false;
    }
    p.addObserver(this);

    p.init(_player.get(), ad);
    // if (p.getError() != null) {
    // return false;
    // }
    p.setSeekable(_seekable);
    return true;
  }

  private boolean initializeAd(AdSpot ad) {
    DebugMode.logD(TAG, "Ooyala Player: Playing Ad");

    cleanupExistingAdPlayer();

    AdMoviePlayer adPlayer = null;
    try {
      Class<? extends AdMoviePlayer> adPlayerClass = _player.get()
          .getAdPlayerClass(ad);
      if (adPlayerClass != null) {
        adPlayer = adPlayerClass.newInstance();
      }
    } catch (InstantiationException e) {
      DebugMode.assertFail(TAG, e.toString());
    } catch (IllegalAccessException e) {
      DebugMode.assertFail(TAG, e.toString());
    }

    if (adPlayer == null) {
      return false;
    }

    _adPlayer = adPlayer;
    if (!initializeAdPlayer(adPlayer, ad)) {
      _adPlayer = null;
      return false;
    }

    return true;
  }

  private void cleanupExistingAdPlayer() {
    // If an ad is playing, take it out of playedAds, and save it for playback
    // later
    if (_adPlayer != null) {
      cleanupPlayer(_adPlayer);
      _adPlayer = null;
    }
  }

  private boolean playAdsBeforeTime(int time) {
    AdSpot adToPlay = _adSpotManager.adBeforeTime(time, _timeAlignment);
    if (adToPlay == null) {
      return false;
    }
    _adSpotManager.markAsPlayed(adToPlay);
    return playAd(adToPlay);
  }

  /**
   * called when ads need to be reset
   */
  public void resetAds() {
    _adSpotManager.resetAds();
  }

  /**
   * Skip the currently playing ad. Do nothing if no ad is playing
   */
  public void skipAd() {
    playAdsBeforeTime(_lastPlayedTime);
  }

  /**
   * event observer
   */
  @Override
  public void update(Observable arg0, Object arg1) {
    AdMoviePlayer player = (AdMoviePlayer) arg0;
    String notification = arg1.toString();

    if (notification.equals(OoyalaPlayer.STATE_CHANGED_NOTIFICATION)) {
      switch (player.getState()) {
      case COMPLETED:
        if (!playAdsBeforeTime(_lastPlayedTime)) {
          cleanupPlayer(_adPlayer);
          _player.get().exitAdMode(this);
        }
        break;
      case ERROR:
        DebugMode.logE(TAG, "Error recieved from Ad.  Cleaning up everything");
        cleanupPlayer(_adPlayer);
        _player.get().exitAdMode(this);
        break;
      default:
        break;
      }
      OoyalaPlayer.notifyStateChange(player);
    } else if (notification.equals(OoyalaPlayer.TIME_CHANGED_NOTIFICATION)) {
      OoyalaPlayer.notifyTimeChange(player);
    } else if (notification.equals(OoyalaPlayer.BUFFER_CHANGED_NOTIFICATION)) {
      OoyalaPlayer.notifyBufferChange(player);
    }
  }

  public void setSeekable(boolean s) {
    _seekable = s;
    if (_adPlayer != null) {
      _adPlayer.setSeekable(_seekable);
    }
  }

  private void cleanupPlayer(Player p) {
    if (p != null) {
      p.deleteObserver(this);
      p.destroy();
    }
  }

  private boolean playAd(AdSpot ad) {
    if (!initializeAd(ad)) {
      return false;
    }
    _adPlayer.play();
    return true;
  }

  /**
   * get the ad player, used to update UI controls
   * 
   * @return the ad player
   */
  @Override
  public PlayerInterface getPlayerInterface() {
    return _adPlayer;
  }
}
