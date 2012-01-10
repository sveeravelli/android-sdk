package com.ooyala.android;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.ooyala.android.AuthorizableItem.AuthCode;
import com.ooyala.android.player.MoviePlayer;
import com.ooyala.android.player.Player;

public class OoyalaPlayer extends RelativeLayout
{
  public static enum OoyalaPlayerActionAtEnd {
    OoyalaPlayerActionAtEndContinue,
    OoyalaPlayerActionAtEndPause,
    OoyalaPlayerActionAtEndStop
  };

  public static enum OoyalaPlayerState {
    OoyalaPlayerStateInit,
    OoyalaPlayerStateLoading,
    OoyalaPlayerStateReadyToPlay,
    OoyalaPlayerStatePlaying,
    OoyalaPlayerStatePaused,
    OoyalaPlayerStateCompleted,
    OoyalaPlayerStateError
  }

  private ContentItem _rootItem = null;
  private Video _currentItem = null;
  private OoyalaException _currentError = null;
  private Player _player = null;
  private Player _adPlayer = null;
  private PlayerAPIClient _playerAPIClient = null;
  private OoyalaPlayerState _state = OoyalaPlayerState.OoyalaPlayerStateInit;
  private List<AdSpot> _playedAds = new ArrayList<AdSpot>();

  public OoyalaPlayer(Context context, String pcode, String apiKey, String secret, String domain) {
    super(context);
    if (pcode != null && apiKey != null && secret != null && domain != null) {
      _playerAPIClient = new PlayerAPIClient(new OoyalaAPIHelper(apiKey, secret), pcode, domain);
    } else {
      throw new IllegalStateException("pcode, apiKey, secret, and domain must be non-null");
    }
  }

  public OoyalaPlayer(Context context)
  {
    super(context);
    createAPI(context);
  }

  public OoyalaPlayer(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    createAPI(context);
  }

  public OoyalaPlayer(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);
    createAPI(context);
  }

  private void createAPI(Context context) {
    TypedArray a = getContext().obtainStyledAttributes(R.styleable.OoyalaPlayer);
    String apiKey = a.getString(R.styleable.OoyalaPlayer_apiKey);
    String secret = a.getString(R.styleable.OoyalaPlayer_secret);
    String pcode = a.getString(R.styleable.OoyalaPlayer_pcode);
    String domain = a.getString(R.styleable.OoyalaPlayer_domain);
    if (pcode != null && apiKey != null && secret != null && domain != null) {
      _playerAPIClient = new PlayerAPIClient(new OoyalaAPIHelper(apiKey, secret), pcode, domain);
    } else {
      throw new IllegalStateException("pcode, apiKey, secret, and domain must be non-null");
    }
  }

  private boolean reinitialize(ContentItem contentTree) {
    _rootItem = contentTree;
    try {
      if (!_playerAPIClient.authorize(contentTree)) {
        _currentError = new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED);
        return false;
      }
    } catch (OoyalaException e) {
     _currentError = e;
     return false;
    }
    return changeCurrentVideo(_rootItem.firstVideo());
  }

  private boolean changeCurrentVideo(Video v) {
    if (v == null) {
      cleanupPlayers();
      return false;
    }
    _state = OoyalaPlayerState.OoyalaPlayerStateLoading;
    cleanupPlayers();
    _playedAds = new ArrayList<AdSpot>();
    _currentItem = v;
    if (_currentItem.getAuthCode() == AuthCode.NOT_REQUESTED) {
      try {
        if (!_playerAPIClient.authorize(_currentItem)) {
          _currentError = new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED);
          return false;
        }
      } catch (OoyalaException e) {
        _currentError = e;
        return false;
      }
    }

    if (!_currentItem.isAuthorized()) {
      _currentError = new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_AUTHORIZATION_FAILED);
      return false;
    }

    _player = initializePlayer(MoviePlayer.class, _currentItem.getStream().decodedURL());
    if (_player == null) {
      return false;
    }

    /**
     * TODO closed captions
     */

    return true;
  }

  private Player initializePlayer(Class<? extends Player> playerClass, Object param) {
    Player p;
    try {
      p = playerClass.newInstance();
    } catch (Exception e) {
      e.printStackTrace();
      _currentError = new OoyalaException(OoyalaException.OoyalaErrorCode.ERROR_INTERNAL_ANDROID, e);
      return null;
    }
    p.init(this.getContext(), param);

    this.addView(p.getView(), this.getWidth(), this.getHeight());

    /**
     * TODO add observers and views and shit
     */

    return p;
  }

  private void cleanupPlayers() {
    cleanupPlayer(_adPlayer);
    _adPlayer = null;
    cleanupPlayer(_player);
    _player = null;
  }

  private void cleanupPlayer(Player player) {
    /**
     * TODO actually clean up shit.
     */
  }

  /**
   * The current movie.
   * @return movie
   */
  public Video getCurrentItem()
  {
	  return _currentItem;
  }

  /**
   * The embedded item (movie, channel, or channel set).
   * @return movie
   */
  public ContentItem getRootItem()
  {
	  return _rootItem;
  }

  /**
   * Get the embedCode for the current player.
   * @return embedCode
   */
  public String getEmbedCode()
  {
	  return _rootItem == null ? null : _rootItem.getEmbedCode();
  }

  /**
   * Reinitializes the player with a new embedCode.
   * If embedCode is null, this method has no effect and just returns.
   * @param embedCode
   */
  public boolean setEmbedCode(String embedCode) {
    List<String> embedCodes = new ArrayList<String>();
    embedCodes.add(embedCode);
    return setEmbedCodes(embedCodes);
  }

  public boolean setEmbedCodes(List<String> embedCodes) {
    try {
      ContentItem contentTree = _playerAPIClient.contentTree(embedCodes);
      return reinitialize(contentTree);
    } catch (OoyalaException e) {
      _currentError = e;
      return false;
    }
  }

  public boolean setExternalId(String externalId) {
    List<String> externalIds = new ArrayList<String>();
    externalIds.add(externalId);
    return setExternalIds(externalIds);
  }

  public boolean setExternalIds(List<String> externalIds) {
    try {
      ContentItem contentTree = _playerAPIClient.contentTreeByExternalIds(externalIds);
      return reinitialize(contentTree);
    } catch (OoyalaException e) {
      _currentError = e;
      return false;
    }
  }

  /**
   * Set the current video in a channel if the video is present. Returns true if accepted, false if not.
   * @param embedCode
   * @return accepted
   */
  public boolean changeCurrentItem(String embedCode)
  {
    return changeCurrentVideo(_rootItem.videoFromEmbedCode(embedCode, _currentItem));
  }


  /**
   * Get the current error code, if one exists
   * @return error code
   */
  public OoyalaException getError()
  {
    return _currentError;
  }

  /**
   * Get current player state. One of playing, paused, buffering, channel, or error
   * @return state
   */
  public OoyalaPlayerState getState()
  {
    return _state;
  }

  /**
   * Pause the current video
   */
  public void pause()
  {

  }

  /**
   * Play the current video
   */
  public void play()
  {

  }

  /**
   * Returns true if in fullscreen mode, false if not
   * @return fullscreen mode
   */
  public boolean getFullscreen()
  {
    return false;
  }

  /**
   * Find where the playhead is with millisecond accuracy
   * @return time in seconds
   */
  public int getPlayheadTime()
  {
    return 0;
  }

  /**
   * Synonym for seek.
   * @param time in milliseconds
   */
  public void setPlayheadTime(int timeInMillis)
  {
    seek(timeInMillis);
  }

  /**
   * Move the playhead to a new location in seconds with millisecond accuracy
   * @param time in milliseconds
   */
  public void seek(int timeInMillis)
  {
  }

  //Closed Captions
  //@todo
//  public List<String> getCurrentItemClosedCaptionsLanguages();
//  public void setClosedCaptionsLanguage(String language);
//  public void getTimedText(float startTime, float endTime);

}
