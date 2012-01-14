package com.ooyala.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.ooyala.android.AuthorizableItem.AuthCode;
import com.ooyala.android.player.MoviePlayer;
import com.ooyala.android.player.OoyalaAdPlayer;
import com.ooyala.android.player.Player;
import com.ooyala.android.player.VASTAdPlayer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class OoyalaPlayer extends Observable implements Observer {
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
    SUSPENDED,
    ERROR
  }

  public static final String TIME_CHANGED_NOTIFICATION = "timeChanged";
  public static final String STATE_CHANGED_NOTIFICATION = "stateChanged";
  public static final String BUFFER_CHANGED_NOTIFICATION = "bufferChanged";
  public static final String ERROR_NOTIFICATION = "error";
  public static final String PLAY_STARTED_NOTIFICATION = "playStarted";
  public static final String PLAY_COMPLETED_NOTIFICATION = "playCompleted";
  public static final String CURRENT_ITEM_CHANGED_NOTIFICATION = "currentItemChanged";
  public static final String AD_STARTED_NOTIFICATION = "adStarted";
  public static final String AD_COMPLETED_NOTIFICATION = "adCompleted";
  public static final String AD_ERROR_NOTIFICATION = "adError";

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
  private OoyalaPlayerLayout _layout = null;

  public OoyalaPlayer(String apiKey, String secret, String pcode, String domain) {
    _playerAPIClient = new PlayerAPIClient(new OoyalaAPIHelper(apiKey, secret), pcode, domain);
    _actionAtEnd = OoyalaPlayerActionAtEnd.CONTINUE;
  }

  public void setLayout(OoyalaPlayerLayout layout) {
    _layout = layout;
    if (_layout == null) {
      return;
    }
    if (_adPlayer != null) {
      _adPlayer.setParent(this);
    }
    if (_player != null) {
      _player.setParent(this);
    }
  }

  public OoyalaPlayerLayout getLayout() {
    return _layout;
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
    setState(OoyalaPlayerState.LOADING);
    cleanupPlayers();
    _playedAds.clear();
    _lastPlayedTime = 0;
    _currentItem = video;
    sendNotification(CURRENT_ITEM_CHANGED_NOTIFICATION);
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

    _player = initializePlayer(MoviePlayer.class, _currentItem.getStream());
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
    p.init(this, param);
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
      p.deleteObserver(this);
      p.destroy();
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
    if (currentPlayer() != null) {
      currentPlayer().pause();
    }
  }

  /**
   * Play the current video
   */
  public void play() {
    Log.d(this.getClass().getName(), "TEST - play");
    if (currentPlayer() != null) {
      if(playAdsBeforeTime(_lastPlayedTime)) {
        return;
      }
      currentPlayer().play();
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
    if (currentPlayer() == null) {
      return -1;
    }
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
    if (currentPlayer() == null) {
      return false;
    }
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

  private boolean playAdsBeforeTime(int time) {
    Log.d(this.getClass().getName(), "TEST - playAdsBeforeTime: "+time);
    this._lastPlayedTime = time;
    for (AdSpot ad : _currentItem.getAds()) {
      if (ad.getTime() <= time && !this._playedAds.contains(ad)) {
        _playedAds.add(ad);
        if (playAd(ad)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean playAd(AdSpot ad) {
    Log.d(this.getClass().getName(), "TEST - playAd: "+ad.getTime());

    Class<? extends Player> adPlayerClass = null;
    if (ad instanceof OoyalaAdSpot) {
      adPlayerClass = OoyalaAdPlayer.class;
    } else if (ad instanceof VASTAdSpot) {
      adPlayerClass = VASTAdPlayer.class;
    }

    if (adPlayerClass == null) {
      Log.d(this.getClass().getName(), "TEST - playAd fail class null");
      return false;
    }

    _adPlayer = initializePlayer(adPlayerClass, ad);

    if (_adPlayer == null) {
      Log.d(this.getClass().getName(), "TEST - playAd fail player null");
      return false;
    }

    _player.suspend();
    sendNotification(AD_STARTED_NOTIFICATION);
    _adPlayer.play();
    return true;
  }

  private Player currentPlayer() {
    return _adPlayer != null ? _adPlayer : _player;
  }

  private boolean fetchMoreChildren(PaginatedItemListener listener) {
    Channel parent = _currentItem.getParent();
    if (parent != null) {
      ChannelSet parentOfParent = parent.getParent();
      if (parent.hasMoreChildren()) {
        return parent.fetchMoreChildren(listener);
      } else if (parentOfParent != null && parentOfParent.hasMoreChildren()) {
        return parentOfParent.fetchMoreChildren(listener);
      }
    }
    return false;
  }

  //This is required because android enjoys making things difficult. talk to jigish if you got issues.
  private static final int FETCH_MORE_CHILDREN_PLAY = 0;
  private static final int FETCH_MORE_CHILDREN_PAUSE = 1;
  private final Handler _fetchMoreChildrenHandler = new Handler() {
    public void handleMessage(Message msg) {
      changeCurrentVideo(_currentItem.nextVideo());
      if (msg.what == FETCH_MORE_CHILDREN_PLAY) {
        play();
      } else if(msg.what == FETCH_MORE_CHILDREN_PAUSE) {
        pause();
      }
    }
  };

  @Override
  public void update(Observable arg0, Object arg1) {
    Log.d(this.getClass().getName(), "TEST - Notification: "+arg1.toString());
    if (arg0 == this._player) {
      if (arg1.equals(STATE_CHANGED_NOTIFICATION)) {
        switch(((Player)arg0).getState()) {
          case COMPLETED:
            if (!playAdsBeforeTime(Integer.MAX_VALUE)) {
              switch (_actionAtEnd) {
                case CONTINUE:
                  if (_currentItem.nextVideo() != null) {
                    changeCurrentVideo(_currentItem.nextVideo());
                    play();
                    break;
                  } else if (fetchMoreChildren(new PaginatedItemListener() {
                          @Override
                          public void onItemsFetched(int firstIndex, int count, OoyalaException error) {
                            _fetchMoreChildrenHandler.sendEmptyMessage(FETCH_MORE_CHILDREN_PLAY);
                          }
                        })) {
                    break;
                  }
                case PAUSE:
                  if (_currentItem.nextVideo() != null) {
                    changeCurrentVideo(_currentItem.nextVideo());
                    pause();
                    break;
                  } else if (fetchMoreChildren(new PaginatedItemListener() {
                          @Override
                          public void onItemsFetched(int firstIndex, int count, OoyalaException error) {
                            _fetchMoreChildrenHandler.sendEmptyMessage(FETCH_MORE_CHILDREN_PAUSE);
                          }
                        })) {
                    break;
                  }
                case STOP:
                  cleanupPlayers();
                  setState(OoyalaPlayerState.COMPLETED);
                  sendNotification(PLAY_COMPLETED_NOTIFICATION);
                  break;
              }
            }
            break;
          case ERROR:
            cleanupPlayers();
            _error = new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
            setState(OoyalaPlayerState.ERROR);
            sendNotification(ERROR_NOTIFICATION);
            break;
          case PLAYING:
            if (_lastPlayedTime == 0) {
              sendNotification(PLAY_STARTED_NOTIFICATION);
            }
          default:
            setState(((Player)arg0).getState());
            break;
        }
      } else if (arg1.equals(TIME_CHANGED_NOTIFICATION)) {
        if (this._player.getState() == OoyalaPlayerState.PLAYING) {
          sendNotification(TIME_CHANGED_NOTIFICATION);
          this._lastPlayedTime = this._player.currentTime();
          playAdsBeforeTime(this._lastPlayedTime);
        }
      }
    } else if (arg0 == this._adPlayer && arg1.equals(STATE_CHANGED_NOTIFICATION)) {
      switch(((Player)arg0).getState()) {
        case COMPLETED:
          sendNotification(AD_COMPLETED_NOTIFICATION);
        case ERROR:
          sendNotification(AD_ERROR_NOTIFICATION);
          cleanupPlayer(_adPlayer);
          _adPlayer = null;
          if (!playAdsBeforeTime(this._lastPlayedTime)) {
            _player.resume();
          }
      }
    }
  }

  public OoyalaPlayerActionAtEnd getActionAtEnd() {
    return _actionAtEnd;
  }

  public void setActionAtEnd(OoyalaPlayerActionAtEnd actionAtEnd) {
    this._actionAtEnd = actionAtEnd;
  }

  private void setState(OoyalaPlayerState state) {
    this._state = state;
    sendNotification(STATE_CHANGED_NOTIFICATION);
  }

  private void sendNotification(String obj) {
    setChanged();
    notifyObservers(obj);
  }

  //Closed Captions
  //@todo
//  public List<String> getCurrentItemClosedCaptionsLanguages();
//  public void setClosedCaptionsLanguage(String language);
//  public void getTimedText(float startTime, float endTime);

}
