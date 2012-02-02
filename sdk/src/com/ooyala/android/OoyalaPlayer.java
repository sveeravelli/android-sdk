package com.ooyala.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import com.ooyala.android.AuthorizableItem.AuthCode;

import android.R.color;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class OoyalaPlayer extends Observable implements Observer {
  public static enum ActionAtEnd {
    CONTINUE, PAUSE, STOP, RESET
  };

  public static enum State {
    INIT, LOADING, READY, PLAYING, PAUSED, COMPLETED, SUSPENDED, ERROR
  };

  /**
   * Used by previousVideo and nextVideo. When passed to them, it will cause the video to be played after it
   * is set.
   */
  public static final int DO_PLAY = 0;
  /**
   * Used by previousVideo and nextVideo. When passed to them, it will cause the video to be paused after it
   * is set.
   */
  public static final int DO_PAUSE = 1;

  public static final String TIME_CHANGED_NOTIFICATION = "timeChanged";
  public static final String STATE_CHANGED_NOTIFICATION = "stateChanged";
  public static final String BUFFER_CHANGED_NOTIFICATION = "bufferChanged";
  public static final String CONTENT_TREE_READY_NOTIFICATION = "contentTreeReady";
  public static final String AUTHORIZATION_READY_NOTIFICATION = "authorizationReady";
  public static final String ERROR_NOTIFICATION = "error";
  public static final String PLAY_STARTED_NOTIFICATION = "playStarted";
  public static final String PLAY_COMPLETED_NOTIFICATION = "playCompleted";
  public static final String CURRENT_ITEM_CHANGED_NOTIFICATION = "currentItemChanged";
  public static final String AD_STARTED_NOTIFICATION = "adStarted";
  public static final String AD_COMPLETED_NOTIFICATION = "adCompleted";
  public static final String AD_SKIPPED_NOTIFICATION = "adSkipped";
  public static final String AD_ERROR_NOTIFICATION = "adError";

  private Video _currentItem = null;
  private ContentItem _rootItem = null;
  private OoyalaException _error = null;
  private ActionAtEnd _actionAtEnd;
  private Player _player = null;
  private Player _adPlayer = null;
  private PlayerAPIClient _playerAPIClient = null;
  private State _state = State.INIT;
  private List<AdSpot> _playedAds = new ArrayList<AdSpot>();
  private int _lastPlayedTime = 0;
  private LayoutController _layoutController = null;
  private ClosedCaptionsView _closedCaptionsView = null;
  private Analytics _analytics = null;
  private String _language = Locale.getDefault().getLanguage();
  private boolean _adsSeekable = false;
  private boolean _seekable = true;
  private boolean _playQueued = false;
  private ClosedCaptionsStyle _closedCaptionsStyle = new ClosedCaptionsStyle(color.white, color.black,
      Typeface.DEFAULT);
  private Map<String, Object> _openTasks = new HashMap<String, Object>();
  private CurrentItemChangedCallback _currentItemChangedCallback = null;

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param apiKey Your API Key
   * @param secret Your Secret
   * @param pcode Your Provider Code
   * @param domain Your Embed Domain
   */
  public OoyalaPlayer(String apiKey, String secret, String pcode, String domain) {
    this(new EmbeddedSecureURLGenerator(apiKey, secret), pcode, domain);
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param lc the LayoutController to use
   * @param apiKey Your API Key
   * @param secret Your Secret
   * @param pcode Your Provider Code
   * @param domain Your Embed Domain
   */
  public OoyalaPlayer(LayoutController lc, String apiKey, String secret, String pcode, String domain) {
    this(lc, new EmbeddedSecureURLGenerator(apiKey, secret), pcode, domain);
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param apiKey Your API Key
   * @param signatureGenerator the SignatureGenerator to use
   * @param pcode Your Provider Code
   * @param domain Your Embed Domain
   */
  public OoyalaPlayer(String apiKey, SignatureGenerator signatureGenerator, String pcode, String domain) {
    this(new EmbeddedSecureURLGenerator(apiKey, signatureGenerator), pcode, domain);
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param lc the LayoutController to use
   * @param apiKey Your API Key
   * @param signatureGenerator the SignatureGenerator to use
   * @param pcode Your Provider Code
   * @param domain Your Embed Domain
   */
  public OoyalaPlayer(LayoutController lc, String apiKey, SignatureGenerator signatureGenerator,
      String pcode, String domain) {
    this(lc, new EmbeddedSecureURLGenerator(apiKey, signatureGenerator), pcode, domain);
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param apiClient the initialized OoyalaAPIClient to use
   */
  public OoyalaPlayer(OoyalaAPIClient apiClient) {
    this(apiClient.getSecureURLGenerator(), apiClient.getPcode(), apiClient.getDomain());
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param lc the LayoutController to use
   * @param apiClient the initialized OoyalaAPIClient to use
   */
  public OoyalaPlayer(LayoutController lc, OoyalaAPIClient apiClient) {
    this(lc, apiClient.getSecureURLGenerator(), apiClient.getPcode(), apiClient.getDomain());
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param secureURLGenerator the SecureURLGenerator to use
   * @param pcode Your Provider Code
   * @param domain Your Embed Domain
   */
  public OoyalaPlayer(SecureURLGenerator secureURLGenerator, String pcode, String domain) {
    _playerAPIClient = new PlayerAPIClient(new OoyalaAPIHelper(secureURLGenerator), pcode, domain);
    _actionAtEnd = ActionAtEnd.CONTINUE;
  }

  /**
   * Initialize an OoyalaPlayer with the given parameters
   * @param lc the LayoutController to use
   * @param secureURLGenerator the SecureURLGenerator to use
   * @param pcode Your Provider Code
   * @param domain Your Embed Domain
   */
  public OoyalaPlayer(LayoutController lc, SecureURLGenerator secureURLGenerator, String pcode, String domain) {
    this(secureURLGenerator, pcode, domain);
    setLayoutController(lc);
  }

  /**
   * Set the layout controller from which the OoyalaPlayer should fetch the layout to display to.
   * @param lc the LayoutController to use.
   */
  public void setLayoutController(LayoutController lc) {
    _layoutController = lc;
    if (getLayout() == null) { return; }
    if (_adPlayer != null) {
      _adPlayer.setParent(this);
    }
    if (_player != null) {
      _player.setParent(this);
    }
    /*
     * NOTE(jigish): we have to do this here because we need the context from the layout. Theoretically all of
     * our customers should actually call setLayout right after initializing the player so this is ok.
     */
    _analytics = new Analytics(getLayout().getContext(), _playerAPIClient);
  }

  /**
   * Get the current OoyalaPlayerLayout
   * @return the current OoyalaPlayerLayout
   */
  public OoyalaPlayerLayout getLayout() {
    return _layoutController.getLayout();
  }

  /**
   * Reinitializes the player with a new embed code. If embedCode is null, this method has no effect and just
   * returns false.
   * @param embedCode
   * @return true if the embed code was successfully set, false if not.
   */
  public boolean setEmbedCode(String embedCode) {
    if (embedCode == null) { return false; }
    List<String> embeds = new ArrayList<String>();
    embeds.add(embedCode);
    return setEmbedCodes(embeds);
  }

  /**
   * Reinitializes the player with a new set of embed codes. If embedCodes is null, this method has no effect
   * and just returns false.
   * @param embedCodes
   * @return true if the embed codes were successfully set, false if not.
   */
  public boolean setEmbedCodes(List<String> embedCodes) {
    if (embedCodes == null || embedCodes.isEmpty()) { return false; }
    cancelOpenTasks();
    final String taskKey = "setEmbedCodes" + System.currentTimeMillis();
    taskStarted(taskKey, _playerAPIClient.contentTree(embedCodes, new ContentTreeCallback() {
      @Override
      public void callback(ContentItem item, OoyalaException error) {
        taskCompleted(taskKey);
        Log.d(this.getClass().getName(), "TEST - CALLBACK");
        if (error != null) {
          _error = error;
          Log.d(this.getClass().getName(), "Exception in setEmbedCodes!", error);
          setState(State.ERROR);
          sendNotification(ERROR_NOTIFICATION);
          return;
        }
        reinitialize(item);
      }
    }));
    return true;
  }

  /**
   * Reinitializes the player with a new external ID. If externalId is null, this method has no effect and
   * just returns false.
   * @param externalId
   * @return true if the external ID was successfully set, false if not.
   */
  public boolean setExternalId(String externalId) {
    if (externalId == null) { return false; }
    List<String> ids = new ArrayList<String>();
    ids.add(externalId);
    return setExternalIds(ids);
  }

  /**
   * Reinitializes the player with a new set of external IDs. If externalIds is null, this method has no
   * effect and just returns false.
   * @param externalIds
   * @return true if the external IDs were successfully set, false if not.
   */
  public boolean setExternalIds(List<String> externalIds) {
    if (externalIds == null || externalIds.isEmpty()) { return false; }
    cancelOpenTasks();
    final String taskKey = "setExternalIds" + System.currentTimeMillis();
    taskStarted(taskKey, _playerAPIClient.contentTreeByExternalIds(externalIds, new ContentTreeCallback() {
      @Override
      public void callback(ContentItem item, OoyalaException error) {
        taskCompleted(taskKey);
        Log.d(this.getClass().getName(), "TEST - CALLBACK");
        if (error != null) {
          _error = error;
          Log.d(this.getClass().getName(), "Exception in setExternalIds!", error);
          setState(State.ERROR);
          sendNotification(ERROR_NOTIFICATION);
          return;
        }
        reinitialize(item);
      }
    }));
    return true;
  }

  /**
   * Set the current video in a channel if the video is present.
   * @param embedCode
   * @return true if the change was successful, false if not
   */
  public boolean changeCurrentItem(String embedCode) {
    return changeCurrentItem(_rootItem.videoFromEmbedCode(embedCode, _currentItem));
  }

  /**
   * Set the current video in a channel if the video is present.
   * @param video
   * @return true if the change was successful, false if not
   */
  public boolean changeCurrentItem(Video video) {
    if (video == null) {
      cleanupPlayers();
      return false;
    }
    setState(State.LOADING);
    cleanupPlayers();
    _playedAds.clear();
    _lastPlayedTime = 0;
    _currentItem = video;
    if (_currentItemChangedCallback != null) {
      _currentItemChangedCallback.callback(_currentItem);
    }
    sendNotification(CURRENT_ITEM_CHANGED_NOTIFICATION);
    if (_currentItem.getAuthCode() == AuthCode.NOT_REQUESTED) {
      // Async authorize;
      cancelOpenTasks();
      final String taskKey = "changeCurrentVideo" + System.currentTimeMillis();
      taskStarted(taskKey, _playerAPIClient.authorize(_currentItem, new AuthorizeCallback() {
        @Override
        public void callback(boolean result, OoyalaException error) {
          taskCompleted(taskKey);
          if (error != null) {
            _error = error;
            Log.d(this.getClass().getName(), "Exception in changeCurrentVideo!", error);
            setState(State.ERROR);
            sendNotification(ERROR_NOTIFICATION);
            return;
          }
          sendNotification(AUTHORIZATION_READY_NOTIFICATION);
          changeCurrentItemAfterAuth();
        }
      }));
      return true;
    }

    return changeCurrentItemAfterAuth();
  }

  /**
   * This is a helper function ONLY to be used with changeCurrentItem.
   * @return
   */
  private boolean changeCurrentItemAfterAuth() {
    if (!_currentItem.isAuthorized()) {
      this._error = new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED);
      return false;
    }

    final String taskKey = "setEmbedCodes" + System.currentTimeMillis();
    taskStarted(taskKey, _currentItem.fetchPlaybackInfo(new FetchPlaybackInfoCallback() {
      @Override
      public void callback(boolean result) {
        taskCompleted(taskKey);
        if (!result) {
          _error = new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
          setState(State.ERROR);
          return;
        }
        changeCurrentItemAfterFetch();
      }
    }));
    return true;
  }

  /**
   * This is a helper function ONLY to be used with changeCurrentItem (in changeCurrentItemAfterAuth).
   * @return
   */
  private boolean changeCurrentItemAfterFetch() {
    _player = initializePlayer(MoviePlayer.class, _currentItem.getStream());
    if (_player == null) { return false; }
    _player.setSeekable(_seekable);

    addClosedCaptionsView();

    _analytics.initializeVideo(_currentItem.getEmbedCode(), _currentItem.getDuration());
    _analytics.reportPlayerLoad();

    dequeuePlay();
    return true;
  }

  private boolean reinitialize(ContentItem tree) {
    if (tree == null) {
      _rootItem = null;
      _currentItem = null;
      return false;
    }
    _rootItem = tree;
    _currentItem = tree.firstVideo();
    sendNotification(CONTENT_TREE_READY_NOTIFICATION);

    // Async Authorize
    final String taskKey = "setEmbedCodes" + System.currentTimeMillis();
    taskStarted(taskKey, _playerAPIClient.authorize(tree, new AuthorizeCallback() {
      @Override
      public void callback(boolean result, OoyalaException error) {
        taskCompleted(taskKey);
        if (error != null) {
          _error = error;
          Log.d(this.getClass().getName(), "Exception in reinitialize!", error);
          setState(State.ERROR);
          sendNotification(ERROR_NOTIFICATION);
          return;
        }
        sendNotification(AUTHORIZATION_READY_NOTIFICATION);
        changeCurrentItem(_rootItem.firstVideo());
      }
    }));
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
    removeClosedCaptionsView();
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
  public State getState() {
    return _state;
  }

  /**
   * Pause the current video
   */
  public void pause() {
    _playQueued = false;
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
      currentPlayer().play();
    } else {
      queuePlay();
    }
  }

  /**
   * Suspend the current video (can be resumed later by calling resume). This differs from pause in that it
   * completely deconstructs the view so the layout can be changed.
   */
  public void suspend() {
    _playQueued = false;
    if (currentPlayer() != null) {
      currentPlayer().suspend();
      removeClosedCaptionsView();
    }
  }

  /**
   * Resume the current video from a suspended state
   */
  public void resume() {
    if (currentPlayer() != null) {
      currentPlayer().resume();
      addClosedCaptionsView();
    }
  }

  /**
   * Returns true if in fullscreen mode, false if not. Fullscreen currently does not work due to limitations
   * in Android.
   * @return fullscreen mode
   */
  public boolean isFullscreen() {
    return _layoutController != null && _layoutController.isFullscreen();
  }

  /**
   * Set fullscreen mode (will only work if fullscreenLayout is set) This will call the setFullscreen method
   * on the associated LayoutController. If you are implementing your own LayoutController here are some
   * things to keep in mind: <li>If the setFullscreen method of your LayoutController creates a new
   * OoyalaPlayerLayout or switches to a different one, you *must* call OoyalaPlayer.suspend() before doing so
   * and call OoyalaPlayer.resume() after doing so. <li>If the setFullscreen method of your LayoutController
   * uses the same OoyalaPlayerLayout, you do not need to do any special handling.
   * @param fullscreen true to switch to fullscreen, false to switch out of fullscreen
   */
  public void setFullscreen(boolean fullscreen) {
    if (isFullscreen() == !fullscreen) { // this is so we don't add/remove cc view if we are not actually
                                         // changing state.
      removeClosedCaptionsView();
      _layoutController.setFullscreen(fullscreen);
      addClosedCaptionsView();
    }
  }

  /**
   * Find where the playhead is with millisecond accuracy
   * @return time in milliseconds
   */
  public int getPlayheadTime() {
    if (currentPlayer() == null) { return -1; }
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
    if (currentPlayer() == null) { return false; }
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
        _analytics.reportPlayheadUpdate(((double) timeInMillis) / 1000);
      }
    }
  }

  private void addClosedCaptionsView() {
    removeClosedCaptionsView();
    if (_currentItem != null && _currentItem.hasClosedCaptions()) {
      _closedCaptionsView = new ClosedCaptionsView(getLayout().getContext());
      _closedCaptionsView.setStyle(_closedCaptionsStyle);
      getLayout().addView(_closedCaptionsView);
    }
  }

  private void removeClosedCaptionsView() {
    if (_closedCaptionsView != null) {
      getLayout().removeView(_closedCaptionsView);
      _closedCaptionsView = null;
    }
  }

  private boolean playAdsBeforeTime(int time) {
    Log.d(this.getClass().getName(), "TEST - playAdsBeforeTime: " + time);
    this._lastPlayedTime = time;
    for (AdSpot ad : _currentItem.getAds()) {
      if (ad.getTime() <= time && !this._playedAds.contains(ad)) {
        _playedAds.add(ad);
        if (playAd(ad)) { return true; }
      }
    }
    return false;
  }

  private boolean playAd(AdSpot ad) {
    Log.d(this.getClass().getName(), "TEST - playAd: " + ad.getTime());

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
    _adPlayer.setSeekable(_adsSeekable);

    _player.suspend();
    removeClosedCaptionsView();
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
      } else if (parentOfParent != null && parentOfParent.hasMoreChildren()) { return parentOfParent
          .fetchMoreChildren(listener); }
    }
    return false;
  }

  /**
   * Change the current video to the previous video in the Channel or ChannelSet. If there is no previous
   * video, this will seek to the beginning of the video.
   * @param what OoyalaPlayerControl.DO_PLAY or OoyalaPlayerControl.DO_PAUSE depending on what to do after the
   *          video is set.
   * @return true if there was a previous video, false if not.
   */
  public boolean previousVideo(int what) {
    if (_currentItem.previousVideo() != null) {
      changeCurrentItem(_currentItem.previousVideo());
      if (what == DO_PLAY) {
        play();
      } else if (what == DO_PAUSE) {
        pause();
      }
      return true;
    }
    seek(0);
    return false;
  }

  /**
   * Change the current video to the next video in the Channel or ChannelSet. If there is no next video,
   * nothing will happen. Note that this will trigger a fetch of additional children if the Channel or
   * ChannelSet is paginated. If so, it may take some time before the video is actually set.
   * @param what OoyalaPlayerControl.DO_PLAY or OoyalaPlayerControl.DO_PAUSE depending on what to do after the
   *          video is set.
   * @return true if there was a next video, false if not.
   */
  public boolean nextVideo(int what) {
    // This is required because android enjoys making things difficult. talk to jigish if you got issues.
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
    })) { return true; }
    return false;
  }

  private final Handler _fetchMoreChildrenHandler = new Handler() {
    public void handleMessage(Message msg) {
      changeCurrentItem(_currentItem.nextVideo());
      if (msg.what == DO_PLAY) {
        play();
      } else if (msg.what == DO_PAUSE) {
        pause();
      }
    }
  };

  private void reset() {
    removeClosedCaptionsView();
    _playQueued = false;
    _player.reset();
    addClosedCaptionsView();
  }

  private void onComplete() {
    Log.d(this.getClass().getName(), "TEST - COMPLETED - onComplete");
    switch (_actionAtEnd) {
      case CONTINUE:
        if (nextVideo(DO_PLAY)) {} else {
          reset();
          sendNotification(PLAY_COMPLETED_NOTIFICATION);
        }
        break;
      case PAUSE:
        if (nextVideo(DO_PAUSE)) {} else {
          reset();
          sendNotification(PLAY_COMPLETED_NOTIFICATION);
        }
        break;
      case STOP:
        cleanupPlayers();
        setState(State.COMPLETED);
        sendNotification(PLAY_COMPLETED_NOTIFICATION);
        break;
      case RESET:
        reset();
        sendNotification(PLAY_COMPLETED_NOTIFICATION);
        break;
    }
  }

  @Override
  public void update(Observable arg0, Object arg1) {
    Log.d(this.getClass().getName(), "TEST - Notificationn: " + arg1.toString() + " " + _state);
    if (arg0 == this._player) {
      Log.d(this.getClass().getName(), "TEST - Note from player");
      if (arg1.equals(STATE_CHANGED_NOTIFICATION)) {
        switch (((Player) arg0).getState()) {
          case COMPLETED:
            if (!playAdsBeforeTime(Integer.MAX_VALUE)) {
              onComplete();
            }
            break;
          case ERROR:
            cleanupPlayers();
            _error = new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_PLAYBACK_FAILED);
            setState(State.ERROR);
            sendNotification(ERROR_NOTIFICATION);
            break;
          case PLAYING:
            if (_lastPlayedTime == 0) {
              _analytics.reportPlayStarted();
              playAdsBeforeTime(250);
              sendNotification(PLAY_STARTED_NOTIFICATION);
            }
            setState(State.PLAYING);
            break;
          case SUSPENDED: // suspended is an internal state. we don't want to pass it through
            break;
          default:
            setState(((Player) arg0).getState());
            break;
        }
      } else if (arg1.equals(TIME_CHANGED_NOTIFICATION) && _player.getState() == State.PLAYING) {
        sendNotification(TIME_CHANGED_NOTIFICATION);
        this._lastPlayedTime = this._player.currentTime();
        playAdsBeforeTime(this._lastPlayedTime);
        // closed captions
        if (_language != null && _currentItem.hasClosedCaptions()) {
          double currT = ((double) currentPlayer().currentTime()) / 1000d;
          if (_closedCaptionsView.getCaption() == null || currT > _closedCaptionsView.getCaption().getEnd()) {
            Caption caption = _currentItem.getClosedCaptions().getCaption(_language, currT);
            if (caption != null && caption.getBegin() <= currT && caption.getEnd() > currT) {
              _closedCaptionsView.setCaption(caption);
            } else {
              _closedCaptionsView.setCaption(null);
            }
          }
        }
      }
    } else if (arg0 == this._adPlayer) {
      if (arg1.equals(STATE_CHANGED_NOTIFICATION)) {
        Log.d(this.getClass().getName(), "TEST - Note from adPlayer");
        switch (((Player) arg0).getState()) {
          case ERROR:
            sendNotification(AD_ERROR_NOTIFICATION);
          case COMPLETED:
            cleanupPlayer(_adPlayer);
            _adPlayer = null;
            sendNotification(AD_COMPLETED_NOTIFICATION);
            if (!playAdsBeforeTime(this._lastPlayedTime)) {
              if (_player.getState() == State.COMPLETED) {
                onComplete();
              } else {
                _player.resume();
                addClosedCaptionsView();
              }
            }
            break;
          case PLAYING:
            if (_state != State.PLAYING) {
              setState(State.PLAYING);
            }
            break;
          case PAUSED:
            if (_state != State.PAUSED) {
              setState(State.PAUSED);
            }
          default:
            break;
        }
      } else if (arg1.equals(TIME_CHANGED_NOTIFICATION) && _adPlayer.getState() == State.PLAYING) {
        sendNotification(TIME_CHANGED_NOTIFICATION);
      }
    }
    Log.d(this.getClass().getName(), "TEST - Notificationn END: " + arg1.toString() + " " + _state);
  }

  /**
   * Get what the player will do at the end of playback.
   * @return the OoyalaPlayer.OoyalaPlayerActionAtEnd to use
   */
  public ActionAtEnd getActionAtEnd() {
    return _actionAtEnd;
  }

  /**
   * Set what the player should do at the end of playback.
   * @param actionAtEnd
   */
  public void setActionAtEnd(ActionAtEnd actionAtEnd) {
    this._actionAtEnd = actionAtEnd;
  }

  public OoyalaAPIClient getOoyalaAPIClient() {
    return new OoyalaAPIClient(_playerAPIClient);
  }

  private void setState(State state) {
    this._state = state;
    sendNotification(STATE_CHANGED_NOTIFICATION);
  }

  private void sendNotification(String obj) {
    setChanged();
    notifyObservers(obj);
    Log.d(this.getClass().getName(), "TEST - SENT NOTIFICATION " + obj);
  }

  /**
   * Set the displayed closed captions language
   * @param language 2 letter country code of the language to display or nil to hide closed captions
   */
  public void setClosedCaptionsLanguage(String language) {
    _language = language;
  }

  public double getBitrate() {
    if (android.os.Build.VERSION.SDK_INT >= 10) {
      // Query for bitrate
      MediaMetadataRetriever metadataRetreiver = new MediaMetadataRetriever();
      metadataRetreiver.setDataSource(getCurrentItem().getStream().getUrl());
      return Double.parseDouble(metadataRetreiver
          .extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
    } else {
      return (double) (getCurrentItem().getStream().getVideoBitrate() * 1000);
    }
  }

  /**
   * @return true if the current state is State.Playing, false otherwise
   */
  public boolean isPlaying() {
    return _state == State.PLAYING;
  }

  /**
   * Seek to the given percentage
   * @param percent percent (between 0 and 100) to seek to
   */
  public void seekToPercent(int percent) {
    if (percent < 0 || percent > 100) { return; }
    if (seekable()) {
      seek(percentToMillis(percent));
    }
  }

  /**
   * Get the current item's duration
   * @return the duration in milliseconds
   */
  public int getDuration() {
    if (currentPlayer() == null) { return 0; }
    return currentPlayer().duration();
  }

  /**
   * Get the current item's buffer percentage
   * @return the buffer percentage (between 0 and 100 inclusive)
   */
  public int getBufferPercentage() {
    if (currentPlayer() == null) { return 0; }
    return currentPlayer().buffer();
  }

  /**
   * Get the current item's playhead time as a percentage
   * @return the playhead time percentage (between 0 and 100 inclusive)
   */
  public int getPlayheadPercentage() {
    if (currentPlayer() == null) { return 0; }
    return millisToPercent(currentPlayer().currentTime());
  }

  /**
   * Set whether ads played by this OoyalaPlayer are seekable (default is false)
   * @param seekable true if seekable, false if not.
   */
  public void setAdsSeekable(boolean seekable) {
    _adsSeekable = seekable;
    if (_adPlayer != null) {
      _adPlayer.setSeekable(_adsSeekable);
    }
  }

  /**
   * Set whether videos played by this OoyalaPlayer are seekable (default is true)
   * @param seekable true if seekable, false if not.
   */
  public void setSeekable(boolean seekable) {
    _seekable = seekable;
    if (_player != null) {
      _player.setSeekable(_seekable);
    }
  }

  /**
   * This will reset the state of all the ads to "unplayed" causing any ad that has already played to play
   * again.
   */
  public void resetAds() {
    _playedAds.clear();
  }

  /**
   * Skip the currently playing ad. Do nothing if no ad is playing
   */
  public void skipAd() {
    if (isShowingAd()) {
      cleanupPlayer(_adPlayer);
      _adPlayer = null;
      sendNotification(AD_SKIPPED_NOTIFICATION);
      if (!playAdsBeforeTime(this._lastPlayedTime)) {
        if (_player.getState() == State.COMPLETED) {
          onComplete();
        } else {
          _player.resume();
          addClosedCaptionsView();
        }
      }
    }
  }

  public boolean isShowingAd() {
    return _adPlayer != null;
  }

  private int percentToMillis(int percent) {
    float fMillis = (((float) percent) / (100f)) * ((float) getDuration());
    return (int) fMillis;
  }

  private int millisToPercent(int millis) {
    float fPercent = (((float) millis) / ((float) getDuration())) * (100f);
    return (int) fPercent;
  }

  private void queuePlay() {
    Log.d(this.getClass().getName(), "TEST - queuePlayy");
    _playQueued = true;
  }

  private void dequeuePlay() {
    Log.d(this.getClass().getName(), "TEST - dequeuePlay");
    if (_playQueued && currentPlayer() != null) {
      Log.d(this.getClass().getName(), "TEST - dequeuePlay queued");
      _playQueued = false;
      play();
    }
  }

  private void taskStarted(String key, Object task) {
    if (task != null) _openTasks.put(key, task);
  }

  private void taskCompleted(String key) {
    _openTasks.remove(key);
  }

  private void cancelOpenTasks() {
    for (String key : _openTasks.keySet()) {
      this._playerAPIClient.cancel(_openTasks.get(key));
    }
    _openTasks.clear();
  }

  public ClosedCaptionsStyle getClosedCaptionsStyle() {
    return _closedCaptionsStyle;
  }

  public void setClosedCaptionsStyle(ClosedCaptionsStyle closedCaptionsStyle) {
    this._closedCaptionsStyle = closedCaptionsStyle;
    if (_closedCaptionsView != null) _closedCaptionsView.setStyle(closedCaptionsStyle);
  }

  PlayerAPIClient getPlayerAPIClient() {
    return this._playerAPIClient;
  }

  /**
   * Set the callback that will be called every time the current item changes
   * @param callback the CurrentItemChangedCallback that should be called every time the current item changes
   */
  public void setCurrentItemChangedCallback(CurrentItemChangedCallback callback) {
    _currentItemChangedCallback = callback;
  }
}
