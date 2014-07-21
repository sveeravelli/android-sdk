package com.ooyala.android;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.item.AdSpot;
import com.ooyala.android.item.Stream;
import com.ooyala.android.player.AdMoviePlayer;
import com.ooyala.android.player.Player;
import com.ooyala.android.player.PlayerInterface;
import com.ooyala.android.plugin.AdPluginInterface;

class OoyalaManagedAdsPlugin implements Observer, AdPluginInterface {
  private static final String TAG = OoyalaManagedAdsPlugin.class.getName();
  private WeakReference<OoyalaPlayer> _player;
  private AdMoviePlayer _adPlayer;
  private boolean _seekable = false;
  private final Set<AdSpot> _playedAds = new HashSet<AdSpot>();
  private int _timeAlignment;
  private int _lastPlayedTime;

  public OoyalaManagedAdsPlugin(OoyalaPlayer player) {
    _player = new WeakReference<OoyalaPlayer>(player);
  }

  @Override
  public void reset() {
    // TODO Auto-generated method stub

  }

  @Override
  public void suspend() {
    if (_adPlayer != null) {
      _adPlayer.suspend();
    }
  }

  @Override
  public void resume() {
    if (_adPlayer != null) {
      _adPlayer.resume();
    }
  }

  @Override
  public void resume(int timeInMilliSecond, State stateToResume) {
    if (_adPlayer != null) {
      _adPlayer.resume(timeInMilliSecond, stateToResume);
    }
  }

  @Override
  public void destroy() {
    if (_adPlayer != null) {
      _adPlayer.destroy();
    }
  }

  @Override
  public boolean onContentChanged() {
    resetAds();
    return false;
  }

  @Override
  public boolean onInitialPlay() {
    DebugMode.logD(TAG, "onInitialPlay");
    _lastPlayedTime = 0;
    return adBeforeTime(_player.get().getCurrentItem().getAds(),
        _playedAds, _lastPlayedTime, _timeAlignment) != null;
  }

  @Override
  public boolean onPlayheadUpdate(int playhead) {
    DebugMode.logD(TAG, "onPlayheadUpdate");
    _lastPlayedTime = playhead;
    return adBeforeTime(_player.get().getCurrentItem().getAds(),
        _playedAds, _lastPlayedTime, _timeAlignment) != null;
  }

  @Override
  public boolean onContentFinished() {
    _lastPlayedTime = Integer.MAX_VALUE;
    return adBeforeTime(_player.get().getCurrentItem().getAds(),
        _playedAds, _lastPlayedTime, _timeAlignment) != null;
  }

  @Override
  public boolean onCuePoint(int cuePointIndex) {
    return false;
  }

  @Override
  public boolean onContentError(int errorCode) {
    return false;
  }

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
      AdSpot oldAd = _adPlayer.getAd();
        if (oldAd.isReusable()) {
          _playedAds.remove(oldAd);
        }
      cleanupPlayer(_adPlayer);
      _adPlayer = null;
    }
  }

  public boolean playAdsBeforeTime(int time) {
    AdSpot adToPlay = adBeforeTime(_player.get().getCurrentItem()
        .getAds(), _playedAds, time, _timeAlignment);
    if (adToPlay == null) {
      return false;
    }
    _playedAds.add(adToPlay);
    return playAd(adToPlay);
  }

  public void resetAds() {
    _playedAds.clear();
    _timeAlignment = Stream.streamSetContainsDeliveryType(_player.get()
        .getCurrentItem().getStreams(), Stream.DELIVERY_TYPE_HLS) ? 10000 : 0;
  }

  /**
   * Skip the currently playing ad. Do nothing if no ad is playing
   */
  public void skipAd() {
    playAdsBeforeTime(_lastPlayedTime);
  }

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

  /**
   * Manually initiate playback of an AdSpot
   * 
   * @param ad
   * @return if the ad started playing
   */
  public boolean playAd(AdSpot ad) {
    if (!initializeAd(ad)) {
      return false;
    }
    _adPlayer.play();
    return true;
  }

  @Override
  public PlayerInterface getPlayerInterface() {
    return _adPlayer;
  }

  /*
   * helper function to identify the ad to be played before a certain time
   * 
   * @param adList the adspot list
   * 
   * @param filteredAdList the ads that should not be played
   * 
   * @param time the time stamp in millisecond
   * 
   * @param timeAlignment time alignment in millisecond
   * 
   * @return the ad spot to be played, null if no ad to be played
   */
  public static AdSpot adBeforeTime(List<AdSpot> adList,
      Set<AdSpot> filteredAds, int time, int timeAlignment) {
    if (adList == null) {
      return null;
    }

    for (AdSpot ad : adList) {
      int adTime = ad.getTime();
      // Align ad times to 10 second (HLS chunk length) boundaries
      if (timeAlignment > 0) {
        adTime = ((adTime + timeAlignment / 2) / timeAlignment) * timeAlignment;
      }
      if (adTime > time || (filteredAds != null && filteredAds.contains(ad))) {
        continue;
      }
      return ad;
    }
    return null;
  }
}
