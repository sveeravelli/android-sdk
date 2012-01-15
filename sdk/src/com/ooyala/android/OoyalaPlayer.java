package com.ooyala.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
import android.widget.MediaController;

public class OoyalaPlayer extends Observable implements Observer,
                                                        MediaController.MediaPlayerControl {
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
  };

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
  private ClosedCaptionsView _closedCaptionsView = null;
  private Analytics _analytics = null;

  public OoyalaPlayer(String apiKey, String secret, String pcode, String domain) {
    _playerAPIClient = new PlayerAPIClient(new OoyalaAPIHelper(apiKey, secret), pcode, domain);
    _actionAtEnd = OoyalaPlayerActionAtEnd.CONTINUE;
  }

  /**
   * Set the layout that the OoyalaPlayer should display to
   * @param layout the OoyalaPlayerLayout to use
   */
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
    /*
     * NOTE(jigish): we have to do this here because we need the context from the layout. Theoretically all of our customers
     * should actually call setLayout right after initializing the player so this is ok.
     */
    _analytics = new Analytics(_layout.getContext(), _playerAPIClient);
  }

  /**
   * Set the layout that the OoyalaPlayer should display to
   * @param layout the OoyalaPlayerLayout to use
   * @param useDefaultControls true if the default Android media controls should be used. false if no controls are needed.
   */
  public void setLayout(OoyalaPlayerLayout layout, boolean useDefaultControls) {
    setLayout(layout);
    if (_layout != null && useDefaultControls) {
      _layout.useDefaultControls(this);
    }
  }

  /**
   * Get the current OoyalaPlayerLayout
   * @return the current OoyalaPlayerLayout
   */
  public OoyalaPlayerLayout getLayout() {
    return _layout;
  }

  /**
   * Reinitializes the player with a new embed code.
   * If embedCode is null, this method has no effect and just returns false.
   * @param embedCode
   * @return true if the embed code was successfully set, false if not.
   */
  public boolean setEmbedCode(String embedCode) {
    if (embedCode == null) {
      return false;
    }
    List<String> embeds = new ArrayList<String>();
    embeds.add(embedCode);
    return setEmbedCodes(embeds);
  }

  /**
   * Reinitializes the player with a new set of embed codes.
   * If embedCodes is null, this method has no effect and just returns false.
   * @param embedCodes
   * @return true if the embed codes were successfully set, false if not.
   */
  public boolean setEmbedCodes(List<String> embedCodes) {
    if (embedCodes == null || embedCodes.isEmpty()) {
      return false;
    }
    try {
      ContentItem contentTree = _playerAPIClient.contentTree(embedCodes);
      return reinitialize(contentTree);
    } catch (OoyalaException e) {
      Log.d(this.getClass().getName(), "Exception in setEmbedCodes!", e);
      this._error = e;
      return false;
    }
  }

  /**
   * Reinitializes the player with a new external ID.
   * If externalId is null, this method has no effect and just returns false.
   * @param externalId
   * @return true if the external ID was successfully set, false if not.
   */
  public boolean setExternalId(String externalId) {
    if (externalId == null) {
      return false;
    }
    List<String> ids = new ArrayList<String>();
    ids.add(externalId);
    return setExternalIds(ids);
  }

  /**
   * Reinitializes the player with a new set of external IDs.
   * If externalIds is null, this method has no effect and just returns false.
   * @param externalIds
   * @return true if the external IDs were successfully set, false if not.
   */
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
   * Set the current video in a channel if the video is present.
   * @param embedCode
   * @return true if the change was successful, false if not
   */
  public boolean changeCurrentItem(String embedCode) {
    return changeCurrentVideo(_rootItem.videoFromEmbedCode(embedCode, _currentItem));
  }

  /**
   * Set the current video in a channel if the video is present.
   * @param video
   * @return true if the change was successful, false if not
   */
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

    //closed captions
    if (_currentItem.hasClosedCaptions()) {
      _closedCaptionsView = new ClosedCaptionsView(_layout.getContext());
      _layout.addView(_closedCaptionsView);
    }

    _analytics.initializeVideo(_currentItem.getEmbedCode(), _currentItem.getDuration());
    _analytics.reportPlayerLoad();
    return true;
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
    if (_closedCaptionsView != null)
      _layout.removeView(_closedCaptionsView);
    _closedCaptionsView = null;
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
   * @param timeInMillis in milliseconds
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
   * @param timeInMillis in milliseconds
   */
  public void seek(int timeInMillis) {
    if (currentPlayer().seekable()) {
      currentPlayer().seekToTime(timeInMillis);
      if (currentPlayer() == _player) {
        _analytics.reportPlayheadUpdate(((double)timeInMillis)/1000);
      }
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
    if (_closedCaptionsView != null)
        _closedCaptionsView.setVisibility(ClosedCaptionsView.GONE);
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

  /**
   * Used by previousVideo and nextVideo. When passed to them, it will cause the video to be played after it is set.
   */
  public static final int DO_PLAY = 0;
  /**
   * Used by previousVideo and nextVideo. When passed to them, it will cause the video to be paused after it is set.
   */
  public static final int DO_PAUSE = 1;

  /**
   * Change the current video to the previous video in the Channel or ChannelSet. If there is no previous video,
   * this will seek to the beginning of the video.
   * @param what OoyalaPlayer.DO_PLAY or OoyalaPlayer.DO_PAUSE depending on what to do after the video is set.
   * @return true if there was a previous video, false if not.
   */
  public boolean previousVideo(int what) {
    if (_currentItem.previousVideo() != null) {
      changeCurrentVideo(_currentItem.previousVideo());
      if (what == DO_PLAY) {
        play();
      } else if(what == DO_PAUSE) {
        pause();
      }
      return true;
    }
    seek(0);
    return false;
  }

  /**
   * Change the current video to the next video in the Channel or ChannelSet. If there is no next video,
   * nothing will happen. Note that this will trigger a fetch of additional children if the Channel or ChannelSet
   * is paginated. If so, it may take some time before the video is actually set.
   * @param what OoyalaPlayer.DO_PLAY or OoyalaPlayer.DO_PAUSE depending on what to do after the video is set.
   * @return true if there was a next video, false if not.
   */
  public boolean nextVideo(int what) {
    //This is required because android enjoys making things difficult. talk to jigish if you got issues.
    if (_currentItem.nextVideo() != null) {
      _fetchMoreChildrenHandler.sendEmptyMessage(what);
      return true;
    } else if (what == DO_PLAY && fetchMoreChildren(new PaginatedItemListener() {
            @Override
            public void onItemsFetched(int firstIndex, int count, OoyalaException error) {
              _fetchMoreChildrenHandler.sendEmptyMessage(DO_PLAY);
            }
          })) {
      return true;
    } else if (what == DO_PAUSE && fetchMoreChildren(new PaginatedItemListener() {
            @Override
            public void onItemsFetched(int firstIndex, int count, OoyalaException error) {
              _fetchMoreChildrenHandler.sendEmptyMessage(DO_PAUSE);
            }
          })) {
      return true;
    }
    return false;
  }

  private final Handler _fetchMoreChildrenHandler = new Handler() {
    public void handleMessage(Message msg) {
      changeCurrentVideo(_currentItem.nextVideo());
      if (msg.what == DO_PLAY) {
        play();
      } else if(msg.what == DO_PAUSE) {
        pause();
      }
    }
  };

  @Override
  public void update(Observable arg0, Object arg1) {
    Log.d(this.getClass().getName(), "TEST - Notificationn: "+arg1.toString()+" "+_state);
    if (arg0 == this._player) {
      Log.d(this.getClass().getName(), "TEST - Note from player");
      if (arg1.equals(STATE_CHANGED_NOTIFICATION)) {
        switch(((Player)arg0).getState()) {
          case COMPLETED:
            if (!playAdsBeforeTime(Integer.MAX_VALUE)) {
              switch (_actionAtEnd) {
                case CONTINUE:
                  if(nextVideo(DO_PLAY)) {
                    break;
                  }
                case PAUSE:
                  if(nextVideo(DO_PAUSE)) {
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
              _analytics.reportPlayStarted();
              sendNotification(PLAY_STARTED_NOTIFICATION);
            }
            setState(OoyalaPlayerState.PLAYING);
            break;
          case SUSPENDED: // suspended is an internal state. we don't want to pass it through
            break;
          default:
            setState(((Player)arg0).getState());
            break;
        }
      } else if (arg1.equals(TIME_CHANGED_NOTIFICATION)) {
        if (this._player.getState() == OoyalaPlayerState.PLAYING) {
          sendNotification(TIME_CHANGED_NOTIFICATION);
          this._lastPlayedTime = this._player.currentTime();
          playAdsBeforeTime(this._lastPlayedTime);
          //closed captions
          if (_currentItem.hasClosedCaptions()) {
            if (_closedCaptionsView.getCaption() != null && currentPlayer().currentTime() > _closedCaptionsView.getCaption().getEnd()) {
            	Caption caption = _currentItem.getClosedCaptions().getCaption(Locale.getDefault().getCountry(), currentPlayer().currentTime());
            	if (caption.getBegin() <= currentPlayer().currentTime() && caption.getEnd() > currentPlayer().currentTime()) {
            	  _closedCaptionsView.setCaption(caption);
            	} else {
            	  _closedCaptionsView.setCaption(null);
            	}
            }
          }
        }
      }
    } else if (arg0 == this._adPlayer && arg1.equals(STATE_CHANGED_NOTIFICATION)) {
      Log.d(this.getClass().getName(), "TEST - Note from adPlayer");
      switch(((Player)arg0).getState()) {
        case ERROR:
          sendNotification(AD_ERROR_NOTIFICATION);
        case COMPLETED:
          sendNotification(AD_COMPLETED_NOTIFICATION);
          cleanupPlayer(_adPlayer);
          _adPlayer = null;
          if (!playAdsBeforeTime(this._lastPlayedTime)) {
            _player.resume();
            if (_closedCaptionsView != null) {
              _closedCaptionsView.setVisibility(ClosedCaptionsView.VISIBLE);
              _closedCaptionsView.bringToFront();
            }
          }
          break;
        case PLAYING:
          if (_state != OoyalaPlayerState.PLAYING) {
            setState(OoyalaPlayerState.PLAYING);
          }
          break;
        case PAUSED:
          if (_state != OoyalaPlayerState.PAUSED) {
            setState(OoyalaPlayerState.PAUSED);
          }
        default:
          break;
      }
    }
    Log.d(this.getClass().getName(), "TEST - Notificationn END: "+arg1.toString()+" "+_state);
  }

  /**
   * Get what the player will do at the end of playback.
   * @return the OoyalaPlayer.OoyalaPlayerActionAtEnd to use
   */
  public OoyalaPlayerActionAtEnd getActionAtEnd() {
    return _actionAtEnd;
  }

  /**
   * Set what the player should do at the end of playback.
   * @param actionAtEnd
   */
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

  // The following methods are required for MediaPlayerControl
  @Override
  public boolean canPause() {
    return currentPlayer() != null && currentPlayer().pauseable();
  }

  @Override
  public boolean canSeekBackward() {
    return currentPlayer() != null && currentPlayer().seekable();
  }

  @Override
  public boolean canSeekForward() {
    return currentPlayer() != null && currentPlayer().seekable();
  }

  @Override
  public int getBufferPercentage() {
    return currentPlayer() == null ? 0 : currentPlayer().getBufferPercentage();
  }

  @Override
  public int getCurrentPosition() {
    return currentPlayer() == null ? 0 : currentPlayer().currentTime();
  }

  @Override
  public int getDuration() {
    return currentPlayer() == null ? 0 : currentPlayer().duration();
  }

  @Override
  public boolean isPlaying() {
    return _state == OoyalaPlayerState.PLAYING;
  }

  @Override
  public void seekTo(int arg0) {
    seek(arg0);
  }

  @Override
  public void start() {
    play();
  }
}
