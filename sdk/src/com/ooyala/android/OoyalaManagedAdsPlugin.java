package com.ooyala.android;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;

import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.item.OoyalaManagedAdSpot;
import com.ooyala.android.item.Stream;
import com.ooyala.android.player.AdMoviePlayer;
import com.ooyala.android.player.Player;
import com.ooyala.android.player.PlayerInterface;
import com.ooyala.android.plugin.AdPluginInterface;
import com.ooyala.android.plugin.DefaultAdsPlugin;

/**
 * Ooyala managed ads plugin manages ooyala and vast ads.
 */
public class OoyalaManagedAdsPlugin extends
    DefaultAdsPlugin<OoyalaManagedAdSpot> implements Observer,
    AdPluginInterface {
  private static final String TAG = OoyalaManagedAdsPlugin.class.getName();
  private AdMoviePlayer _adPlayer;
  private boolean _seekable = false;
  protected WeakReference<OoyalaPlayer> _player;

  /**
   * Ooyala managed ads plugin manages ooyala and vast ads.
   */
  public OoyalaManagedAdsPlugin(OoyalaPlayer player) {
    super();
    _player = new WeakReference<OoyalaPlayer>(player);
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
    super.onContentChanged();
    _adSpotManager.insertAds(_player.get().getCurrentItem().getAds());
    if (Stream.streamSetContainsDeliveryType(_player.get().getCurrentItem()
        .getStreams(), Stream.DELIVERY_TYPE_HLS)) {
      _adSpotManager.setAlignment(10000);
    }
    return false;
  }

  private boolean initializeAdPlayer(AdMoviePlayer p, OoyalaManagedAdSpot ad) {
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

  private boolean initializeAd(OoyalaManagedAdSpot ad) {
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
        if (!playAdsBeforeTime()) {
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

  @Override
  protected boolean playAd(OoyalaManagedAdSpot ad) {
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
