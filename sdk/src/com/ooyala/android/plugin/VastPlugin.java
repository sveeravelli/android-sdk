package com.ooyala.android.plugin;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.ooyala.android.DebugMode;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.item.AdSpot;
import com.ooyala.android.item.Stream;
import com.ooyala.android.player.AdMoviePlayer;
import com.ooyala.android.player.Player;
import com.ooyala.android.player.PlayerInterface;

public class VastPlugin extends StateNotifier implements Observer,
    AdPluginInterface, PlayerInterface {
  private static final String TAG = VastPlugin.class.getName();
  private WeakReference<OoyalaPlayer> _player;
  private AdMoviePlayer _adPlayer;
  private boolean _seekable = false;
  private final List<AdSpot> _playedAds = new ArrayList<AdSpot>();
  private int _lastPlayedTime;

  public VastPlugin(OoyalaPlayer player) {
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
  public void destroy() {
    if (_adPlayer != null) {
      _adPlayer.destroy();
    }
  }

  @Override
  public void pause() {
    if (_adPlayer != null) {
      _adPlayer.resume();
    }
  }

  @Override
  public void play() {
    if (_adPlayer != null) {
      _adPlayer.play();
    }
  }

  @Override
  public void stop() {
    if (_adPlayer != null) {
      _adPlayer.stop();
    }
  }

  @Override
  public int currentTime() {
    if (_adPlayer != null) {
      return _adPlayer.currentTime();
    }
    return 0;
  }

  @Override
  public int duration() {
    if (_adPlayer != null) {
      return _adPlayer.duration();
    }
    return 0;
  }

  @Override
  public int buffer() {
    if (_adPlayer != null) {
      return _adPlayer.buffer();
    }
    return 0;
  }

  @Override
  public boolean seekable() {
    if (_adPlayer != null) {
      return _adPlayer.seekable();
    }
    return false;
  }

  @Override
  public void seekToTime(int timeInMillis) {
    if (_adPlayer != null) {
      _adPlayer.seekToTime(timeInMillis);
    }
  }

  @Override
  public StateNotifier getStateNotifier() {
    return this;
  }

  @Override
  public boolean onContentChanged() {
    resetAds();
    _lastPlayedTime = 0;
    return false;
  }

  @Override
  public boolean onInitialPlay() {
    DebugMode.logD(TAG, "onInitialPlay");
    return hasAdsBeforeTime(0);
  }

  @Override
  public boolean onPlayheadUpdate(int playhead) {
    DebugMode.logD(TAG, "onPlayheadUpdate");
    _lastPlayedTime = playhead;
    return hasAdsBeforeTime(_lastPlayedTime);
  }

  @Override
  public boolean onContentFinished() {
    _lastPlayedTime = Integer.MAX_VALUE;
    return hasAdsBeforeTime(_lastPlayedTime);
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
    playAdsBeforeTime(this._lastPlayedTime, this._lastPlayedTime > 0 ? true
        : false);
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
    if (p.getError() != null) {
      return false;
    }
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

    if (!initializeAdPlayer(adPlayer, ad)) {
      return false;
    }

    _adPlayer = adPlayer;
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

  public boolean hasAdsBeforeTime(int time) {
    for (AdSpot ad : _player.get().getCurrentItem().getAds()) {
      int adTime = ad.getTime();
      // Align ad times to 10 second (HLS chunk length) boundaries
      if (Stream.streamSetContainsDeliveryType(_player.get().getCurrentItem()
          .getStreams(), Stream.DELIVERY_TYPE_HLS)) {
        adTime = ((adTime + 5000) / 10000) * 10000;
      }
      if (adTime <= time && !this._playedAds.contains(ad)) {
        return true;
      }
    }
    return false;
  }

  public boolean playAdsBeforeTime(int time, boolean autoplay) {
    this._lastPlayedTime = time;
    for (AdSpot ad : _player.get().getCurrentItem().getAds()) {
      int adTime = ad.getTime();
      // Align ad times to 10 second (HLS chunk length) boundaries
      if (Stream.streamSetContainsDeliveryType(_player.get().getCurrentItem()
          .getStreams(),
          Stream.DELIVERY_TYPE_HLS)) {
        adTime = ((adTime + 5000) / 10000) * 10000;
      }
      if (adTime <= time && !this._playedAds.contains(ad)) {
        _playedAds.add(ad);
        if (!autoplay && initializeAd(ad)) {
          return true;
        } else if (autoplay && playAd(ad)) {
          return true;
        }
      }
    }
    return false;
  }

  public void resetAds() {
    _playedAds.clear();
  }

  /**
   * Skip the currently playing ad. Do nothing if no ad is playing
   */
  public void skipAd() {
    playAdsBeforeTime(_lastPlayedTime, true);
  }

  @Override
  public void update(Observable arg0, Object arg1) {
    AdMoviePlayer player = (AdMoviePlayer) arg0;
    String notification = arg1.toString();

    if (notification.equals(OoyalaPlayer.STATE_CHANGED_NOTIFICATION)) {
      switch (player.getState()) {
      case COMPLETED:
        if (!playAdsBeforeTime(_lastPlayedTime, true)) {
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
        setState(player.getState());
        break;
      }
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
    play();
    return true;
  }
}
