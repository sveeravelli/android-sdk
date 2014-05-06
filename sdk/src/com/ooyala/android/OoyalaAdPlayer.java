package com.ooyala.android;

import java.net.URL;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.FrameLayout;

import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.item.AdSpot;
import com.ooyala.android.item.ContentItem;

class OoyalaAdPlayer extends AdMoviePlayer {
  private static String TAG = OoyalaAdPlayer.class.getName();

  private OoyalaAdSpot _ad;
  private Object _fetchTask;

  private int _topMargin;
  private FrameLayout _playerLayout;
  private AdsLearnMoreButton _learnMore;

  public OoyalaAdPlayer() {
    super();
  }

  @Override
  public void init(final OoyalaPlayer parent, AdSpot ad) {
    if (!(ad instanceof OoyalaAdSpot)) {
      this._error = "Invalid Ad";
      this._state = State.ERROR;
      return;
    }
    Log.d(TAG, "Ooyala Ad Player Loaded");

    _seekable = false;
    _ad = (OoyalaAdSpot) ad;

    //If this ad tried to authorize and failed
    if(!_ad.isAuthorized() && _ad.getAuthCode() > 0) {
      this._error = "This ad was unauthorized to play: " + ContentItem.getAuthError(_ad.getAuthCode());
      this._state = State.ERROR;
      return;
    }
    if (_ad.getStream() == null || getBasePlayer() != null) {
      if (_fetchTask != null) {
        this._parent.getPlayerAPIClient().cancel(_fetchTask);
      }
      PlayerInfo info = getBasePlayer() != null ? getBasePlayer().getPlayerInfo() : StreamPlayer.defaultPlayerInfo;

      _fetchTask = _ad.fetchPlaybackInfo(info, new FetchPlaybackInfoCallback() {

        @Override
        public void callback(boolean result) {
          if (!_ad.isAuthorized()) {
            _error = "Error fetching playback info on init";
            setState(State.ERROR);
            return;
          } else {
            initAfterFetch(parent);
          }
        }
      });
      return;
    }
    initAfterFetch(parent);
  }

  private void initAfterFetch(OoyalaPlayer parent) {
    super.init(parent, _ad.getStreams());

    //Add Learn More button if there is a click through URL
    if (_learnMore == null && _ad.getClickURL() != null) {
      _playerLayout = parent.getLayout();
      _topMargin = parent.getTopBarOffset();
      _learnMore = new AdsLearnMoreButton(_playerLayout.getContext(), this, _topMargin);
      _playerLayout.addView(_learnMore);
    }

    if (_ad.getTrackingURLs() != null) {
      for (URL url : _ad.getTrackingURLs()) {
        NetUtils.ping(url);
      }
    }
  }

  @Override
  public OoyalaAdSpot getAd() {
    return _ad;
  }

  @Override
  public void setBasePlayer(StreamPlayer basePlayer) {
    if (_ad == null) {
      setBasePlayer2(basePlayer);
      return;
    }

    PlayerInfo info = basePlayer != null ? basePlayer.getPlayerInfo() : StreamPlayer.defaultPlayerInfo;
    final StreamPlayer player = basePlayer;

    _fetchTask = _ad.fetchPlaybackInfo(info, new FetchPlaybackInfoCallback() {

      @Override
      public void callback(boolean result) {
        if (!_ad.isAuthorized()) {
          _error = "Error fetching playback info on setBasePlayer";
          setState(State.ERROR);
          return;
        } else {
          setBasePlayer2(player);
        }
      }
    });
  }

  // we can't access super in an anonymous class
  private void setBasePlayer2(StreamPlayer basePlayer) {
    super.setBasePlayer(basePlayer);
  }

  /**
   * Called by OoyalaPlayer when going in and out of fullscreen using OoyalaPlayerLayoutController
   * @param layout the new layout to add the Learn More button
   * @param topMargin the pixels to shift the Learn More button down
   */
  @Override
  public void updateLearnMoreButton(FrameLayout layout, int topMargin) {
    //If topMargin did not change, return
    if (_topMargin == topMargin) {
      return;
    }

    if (_learnMore != null) {
      //Remove the Learn More button from the old playerLayout and set the new playerLayout
      _playerLayout.removeView(_learnMore);
      _playerLayout = layout;

      //Set the new topMargin and add the Learn More button to new playerLayout
      _learnMore.setTopMargin(topMargin);
      _playerLayout.addView(_learnMore);
    }
  }

  /**
   * Called by the Learn More button's onClick event.
   * Opens browser to click through URL
   */
  @Override
  public void processClickThrough() {
    String url = _ad.getClickURL().toString();
    try {
      url = url.trim(); //strip leading and trailing whitespace
      Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      _playerLayout.getContext().startActivity(browserIntent);
      Log.d(TAG, "Opening brower to " + url);
    } catch (Exception e) {
      Log.e(TAG, "There was some exception on clickthrough!");
      e.printStackTrace();
    }
  }

  @Override
  public void resume() {
    super.resume();

    //Bring Learn More button to front when play resumes so it does not get hidden beneath the video view.
    if (_learnMore != null) {
      _playerLayout.bringChildToFront(_learnMore);
    }
  }

  @Override
  public void destroy() {
    //Remove Learn More button if it exists
    if (_learnMore != null) {
      _playerLayout.removeView(_learnMore);
      _learnMore.destroy();
      _learnMore = null;
    }

    if (_fetchTask != null) this._parent.getPlayerAPIClient().cancel(_fetchTask);
    super.destroy();
  }
}
