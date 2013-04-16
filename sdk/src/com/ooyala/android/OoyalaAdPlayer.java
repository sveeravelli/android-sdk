package com.ooyala.android;

import java.net.URL;

import com.ooyala.android.OoyalaPlayer.State;

class OoyalaAdPlayer extends AdMoviePlayer {
  private OoyalaAdSpot _ad;
  private Object _fetchTask;

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
    _seekable = false;
    _ad = (OoyalaAdSpot) ad;

    //If this ad tried to authorize and failed
    if(!_ad.isAuthorized() && _ad.getAuthCode() > 0) {
      this._error = "This ad was unauthorized to play";
      this._state = State.ERROR;
      return;
    }
    if (_ad.getStream() == null || getBasePlayer() != null) {
      if (_fetchTask != null) {
        this._parent.getPlayerAPIClient().cancel(_fetchTask);
      }
      PlayerInfo info = getBasePlayer() != null ? getBasePlayer().getPlayerInfo() : StreamPlayer.defaultPlayerInfo;

      _fetchTask = _ad._api.authorize(_ad, info, new AuthorizeCallback() {

        @Override
        public void callback(boolean result, OoyalaException error) {
          if (error != null || !_ad.isAuthorized()) {
            _error = "Error fetching VAST XML";
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

    // TODO[jigish] setup clickthrough

    if (_ad.getTrackingURLs() != null) {
      for (URL url : _ad.getTrackingURLs()) {
        NetUtils.ping(url);
      }
    }
  }

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

    _ad._api.authorize(_ad, info, new AuthorizeCallback() {

      @Override
      public void callback(boolean result, OoyalaException error) {
        if (error != null || !_ad.isAuthorized()) {
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

  @Override
  public void destroy() {
    if (_fetchTask != null) this._parent.getPlayerAPIClient().cancel(_fetchTask);
    super.destroy();
  }
}
