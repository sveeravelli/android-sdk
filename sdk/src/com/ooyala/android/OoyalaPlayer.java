package com.ooyala.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.ooyala.android.AuthorizableItem.AuthCode;
import com.ooyala.android.player.MoviePlayer;
import com.ooyala.android.player.Player;

import android.util.Log;

public class OoyalaPlayer implements Observer {
  public static enum OoyalaPlayerActionAtEnd {
    CONTINUE,
    PAUSE,
    STOP
  };

  public static enum OoyalaPlayerState {
    INIT,
    LOADING,
    READY,
    PLAYING,
    PAUSED,
    COMPLETED,
    ERROR,
    SUSPENDED,
    RESUMED // This is used by setState in Player to reset the state to the old state
  }

  private Video _currentItem = null;
  private ContentItem _rootItem = null;
  private OoyalaException _error = null;
  private OoyalaPlayerActionAtEnd _actionAtEnd;
  private Player _player = null;
  private Player _adPlayer = null;
  private PlayerAPIClient _playerAPIClient = null;
  private OoyalaPlayerState _state = OoyalaPlayerState.INIT;
  private List<AdSpot> _playedAds = new ArrayList<AdSpot>();
  private int _lastPlayedTime = 0;
  private boolean _playQueued = false;
  private OoyalaPlayerLayout _layout = null;

  public OoyalaPlayer(String apiKey, String secret, String pcode, String domain) {
    _playerAPIClient = new PlayerAPIClient(new OoyalaAPIHelper(apiKey, secret), pcode, domain);
  }

  public void setLayout(OoyalaPlayerLayout layout) {
    _layout = layout;
    if (_adPlayer != null) {
      _adPlayer.setParent(_layout);
    }
    if (_player != null) {
      _player.setParent(_layout);
    }
  }

  /**
   * Reinitializes the player with a new embedCode.
   * If embedCode is null, this method has no effect and just returns.
   * @param embedCode
   */
  public boolean setEmbedCode(String embedCode) {
    List<String> embeds = new ArrayList<String>();
    embeds.add(embedCode);
    return setEmbedCodes(embeds);
  }

  public boolean setEmbedCodes(List<String> embedCodes) {
    try {
      ContentItem contentTree = _playerAPIClient.contentTree(embedCodes);
      return reinitialize(contentTree);
    } catch (OoyalaException e) {
      Log.d(this.getClass().getName(), "Exception in setEmbedCodes!", e);
      this._error = e;
      return false;
    }
  }

  public boolean setExternalId(String externalId) {
    List<String> ids = new ArrayList<String>();
    ids.add(externalId);
    return setExternalIds(ids);
  }

  public boolean setExternalIds(List<String> externalIds) {
    try {
      ContentItem contentTree = _playerAPIClient.contentTreeByExternalIds(externalIds);
      return reinitialize(contentTree);
    } catch (OoyalaException e) {
      Log.d(this.getClass().getName(), "Exception in setExternalIds!", e);
      this._error = e;
      return false;
    }
  }

  /**
   * Set the current video in a channel if the video is present. Returns true if accepted, false if not.
   * @param embedCode
   * @return accepted
   */
  public boolean changeCurrentItem(String embedCode) {
    return changeCurrentVideo(_rootItem.videoFromEmbedCode(embedCode, _currentItem));
  }

  private boolean reinitialize(ContentItem tree) {
    _rootItem = tree;
    try {
      _playerAPIClient.authorize(_rootItem);
    } catch (OoyalaException e) {
      Log.d(this.getClass().getName(), "Exception in reinitialize!", e);
      this._error = e;
      return false;
    }
    return changeCurrentVideo(_rootItem.firstVideo());
  }

  public boolean changeCurrentVideo(Video video) {
    if (video == null) {
      cleanupPlayers();
      return false;
    }
    _state = OoyalaPlayerState.LOADING;
    cleanupPlayers();
    _playedAds.clear();
    _lastPlayedTime = 0;
    _currentItem = video;
    if (_currentItem.getAuthCode() == AuthCode.NOT_REQUESTED) {
      try {
        _playerAPIClient.authorize(_currentItem);
      } catch (OoyalaException e) {
        Log.d(this.getClass().getName(), "Exception in changeCurrentVideo!", e);
        this._error = e;
        return false;
      }
    }

    if (!_currentItem.isAuthorized()) {
      this._error = new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED);
      return false;
    }

    _player = initializePlayer(MoviePlayer.class, _currentItem.getStream().decodedURL());
    if (_player == null) { return false; }

    /**
     * TODO closed captions
     */

    return true;
  }

  private Player initializePlayer(Class<? extends Player> playerClass, Object param) {
    Player p = null;
    try {
      p = playerClass.newInstance();
    } catch (Exception e) {
      _error = new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_INTERNAL_ANDROID, e);
      return null;
    }
    p.addObserver(this);
    p.init(_layout, param);
    return p;
  }

  private void cleanupPlayers() {
    cleanupPlayer(_adPlayer);
    _adPlayer = null;
    cleanupPlayer(_player);
    _player = null;
  }

  private void cleanupPlayer(Player p) {
    if (p != null) {
      p.stop();
      /**
       * TODO remove view!
       */
    }
  }

  /**
   * The current movie.
   * @return movie
   */
  public Video getCurrentItem() {
    return _currentItem;
  }

  /**
   * The embedded item (movie, channel, or channel set).
   * @return movie
   */
  public ContentItem getRootItem() {
    return _rootItem;
  }

  /**
   * Get the current error code, if one exists
   * @return error code
   */
  public OoyalaException getError() {
    return _error;
  }

  /**
   * Get the embedCode for the current player.
   * @return embedCode
   */
  public String getEmbedCode() {
    return _rootItem == null ? null : _rootItem.getEmbedCode();
  }

  /**
   * Get current player state. One of playing, paused, buffering, channel, or error
   * @return state
   */
  public OoyalaPlayerState getState() {
    return _state;
  }

  /**
   * Pause the current video
   */
  public void pause() {
    switch (_state) {
      case PLAYING:
        currentPlayer().pause();
      default:
        break;
    }
  }

  /**
   * Play the current video
   */
  public void play() {
    Log.d(this.getClass().getName(), "TEST - play");
    switch (_state) {
      case INIT:
      case LOADING:
        queuePlay();
        break;
      case PAUSED:
      case READY:
      case COMPLETED:
        currentPlayer().play();
      default:
        break;
    }
  }

  /**
   * Returns true if in fullscreen mode, false if not
   * @return fullscreen mode
   */
  public boolean getFullscreen() {
    return false;
  }

  /**
   * Find where the playhead is with millisecond accuracy
   * @return time in seconds
   */
  public int getPlayheadTime() {
    return currentPlayer().currentTime();
  }

  /**
   * Synonym for seek.
   * @param time in milliseconds
   */
  public void setPlayheadTime(int timeInMillis) {
    seek(timeInMillis);
  }

  public boolean seekable() {
    return currentPlayer().seekable();
  }

  /**
   * Move the playhead to a new location in seconds with millisecond accuracy
   * @param time in milliseconds
   */
  public void seek(int timeInMillis) {
    if (currentPlayer().seekable()) {
      currentPlayer().seekToTime(timeInMillis);
    }
  }

  private Player currentPlayer() {
    return _player;
  }

  private void queuePlay() {
    Log.d(this.getClass().getName(), "TEST - queuePlay");
    _playQueued = true;
  }

  private void dequeuePlay() {
    Log.d(this.getClass().getName(), "TEST - dequeuePlay");
    if (_playQueued) {
      switch (_state) {
        case PAUSED:
        case READY:
        case COMPLETED:
          _playQueued = false;
          currentPlayer().play();
        default:
          break;
      }
    }
  }

  @Override
  public void update(Observable arg0, Object arg1) {
    this._state = ((Player)arg0).getState();
    Log.d(this.getClass().getName(), "TEST - update: "+_state);
    dequeuePlay();
  }

  //Closed Captions
  //@todo
//  public List<String> getCurrentItemClosedCaptionsLanguages();
//  public void setClosedCaptionsLanguage(String language);
//  public void getTimedText(float startTime, float endTime);

}
