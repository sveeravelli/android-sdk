package com.ooyala.android;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.os.Handler;

import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.player.PlayerInterface;
import com.ooyala.android.plugin.AdPluginInterface;
import com.ooyala.android.plugin.StateNotifier;
import com.ooyala.android.plugin.VastPlugin;

class AdPluginManager extends StateNotifier implements Observer,
    AdPluginInterface,
    AdPluginManagerInterface, PlayerInterface {
  enum AdMode {
    None, ContentChanged, InitialPlay, Playhead, CuePoint, ContentFinished, ContentError
  };
  private static final String TAG = AdPluginManager.class.getName();
  private WeakReference<OoyalaPlayer> _player;
  private List<AdPluginInterface> _plugins = new ArrayList<AdPluginInterface>();
  private AdPluginInterface _activePlugin = null;
  private AdMode _admode = AdMode.None;
  private int _parameter = 0;
  private Handler _handler = null;

  protected AdPluginManager(OoyalaPlayer player) {
    _player = new WeakReference<OoyalaPlayer>(player);
    _handler = new Handler();
  }

  public static AdPluginManager createInstance(OoyalaPlayer player) {
    AdPluginManager manager = new AdPluginManager(player);
    return manager;
  }
  
  @Override
  public boolean registerPlugin(final AdPluginInterface plugin) {
    if (_plugins.contains(plugin)) {
      return false;
    }

    StateNotifier notifier = getNotifierFromPlugin(plugin);
    if (notifier != null) {
      DebugMode.logD(TAG, "add observer to " + plugin.toString());
      notifier.addObserver(this);
    }

    _plugins.add(plugin);
    return true;
  }

  @Override
  public boolean deregisterPlugin(final AdPluginInterface plugin) {
    if (!_plugins.contains(plugin)) {
      return false;
    }

    StateNotifier notifier = getNotifierFromPlugin(plugin);
    if (notifier != null) {
      DebugMode.logD(TAG, "delete observer from " + plugin.toString());
      notifier.deleteObserver(this);
    }

    _plugins.remove(plugin);

    return true;
  }

  public boolean exitAdMode(final AdPluginInterface plugin) {
    if (plugin == null) {
      DebugMode.assertFail(TAG, "exitAdModed.plugin is null");
      return false;
    }

    if (!_plugins.contains(plugin)) {
      DebugMode.assertFail(TAG, plugin.toString()
          + " exit admode before it register");
      return false;
    }

    if (_activePlugin != plugin) {
      DebugMode.assertFail(TAG, plugin.toString()
          + " exit admode but active plugin is " + _activePlugin.toString());
      return false;
    }

    do {
      _activePlugin = getNextPlugin(_activePlugin);
    } while (_activePlugin != null && !pluginRequestAdMode(_admode));
    
    if (_activePlugin == null) {
      final AdPluginInterface self = this;
      _handler.post(new Runnable() {
        @Override
        public void run() {
          OoyalaPlayer p = _player.get();
          if (p != null) {
            _player.get().exitAdMode(self);
          } else {
            DebugMode.assertFail(TAG, "player is null when exiting admode");
          }
          _admode = AdMode.None;
        }
      });
    } else {
      _activePlugin.onAdModeEntered();
    }
    return true;
  }

  // boolean Manager.insertPluginView()
  // boolean Manager.removePluginView()

  @Override
  public void pause() {
    if (_activePlugin instanceof PlayerInterface) {
      ((PlayerInterface) _activePlugin).pause();
    }
  }

  @Override
  public void play() {
    if (_activePlugin instanceof PlayerInterface) {
      ((PlayerInterface) _activePlugin).play();
    }
  }

  @Override
  public void stop() {
    if (_activePlugin instanceof PlayerInterface) {
      ((PlayerInterface) _activePlugin).stop();
    }
  }

  @Override
  public void seekToTime(int timeInMillis) {
    if (_activePlugin instanceof PlayerInterface) {
      ((PlayerInterface) _activePlugin).seekToTime(timeInMillis);
    }
  }

  @Override
  public int currentTime() {
    if (_activePlugin instanceof PlayerInterface) {
      return ((PlayerInterface) _activePlugin).currentTime();
    }
    return 0;
  }

  @Override
  public int duration() {
    if (_activePlugin instanceof PlayerInterface) {
      return ((PlayerInterface) _activePlugin).duration();
    }
    return 0;
  }

  @Override
  public int buffer() {
    if (_activePlugin instanceof PlayerInterface) {
      return ((PlayerInterface) _activePlugin).buffer();
    }
    return 0;
  }

  @Override
  public void reset() {
    if (_activePlugin != null) {
      _activePlugin.reset();
    }
  }

  @Override
  public void suspend() {
    if (_activePlugin != null) {
      _activePlugin.suspend();
    }
  }

  @Override
  public void resume() {
    if (_activePlugin != null) {
      _activePlugin.resume();
    }
  }

  @Override
  public void destroy() {
    if (_activePlugin != null) {
      _activePlugin.destroy();
    }
  }

  @Override
  public boolean seekable() {
    if (_activePlugin instanceof PlayerInterface) {
      return ((PlayerInterface) _activePlugin).seekable();
    }
    return false;
  }

  @Override
  public boolean onContentChanged() {
    return onAdMode(AdMode.ContentChanged);
  }

  @Override
  public boolean onInitialPlay() {
    return onAdMode(AdMode.InitialPlay);
  }

  @Override
  public boolean onPlayheadUpdate(int playhead) {
    _parameter = playhead;
    return onAdMode(AdMode.Playhead);
  }

  @Override
  public boolean onContentFinished() {
    return onAdMode(AdMode.ContentFinished);
  }

  @Override
  public boolean onCuePoint(int cuePointIndex) {
    _parameter = cuePointIndex;
    return onAdMode(AdMode.CuePoint);
  }

  @Override
  public boolean onContentError(int errorCode) {
    _parameter = errorCode;
    return onAdMode(AdMode.ContentError);
  }

  @Override
  public void onAdModeEntered() {
    if (_activePlugin == null) {
      DebugMode.assertFail(TAG, "enter ad mode when active plugin is null");
      return;
    }
    _activePlugin.onAdModeEntered();
  }

  // helper functions
  private AdPluginInterface getNextPlugin(final AdPluginInterface plugin) {
    if (_plugins.size() == 0) {
      return null;
    }

    if (plugin == null) {
      return _plugins.get(0);
    }

    int index = _plugins.indexOf(plugin);
    if (index < 0 || index >= _plugins.size() - 1) {
      return null;
    }
    return _plugins.get(index + 1);
  }

  /**
   * AdPluginManager is notified by OoyalaPlayer for ad events query plugins
   * whether they need control
   * 
   * @return a token when any of the plugins wants control or null if no plugin
   *         needs control
   * 
   */
  private boolean onAdMode(AdMode mode) {
    if (_plugins.size() <= 0) {
      return false;
    }

    do {
      _activePlugin = getNextPlugin(_activePlugin);
    } while (_activePlugin != null && !pluginRequestAdMode(mode));

    if (_activePlugin != null) {
      _admode = mode;
      return true;
    }

    return false;
  }

  /**
   * ad manager queries plugin whether it requires control
   */
  private boolean pluginRequestAdMode(AdMode mode) {
    if (_activePlugin == null) {
      DebugMode.assertFail(TAG,
          "plugin method is called when active plugin is null");
      return false;
    }

    boolean result = false;
    switch (mode) {
    case ContentChanged:
      result = _activePlugin.onContentChanged();
      break;
    case InitialPlay:
      result = _activePlugin.onInitialPlay();
      break;
    case Playhead:
      result = _activePlugin.onPlayheadUpdate(_parameter);
      break;
    case CuePoint:
      result = _activePlugin.onCuePoint(_parameter);
      break;
    case ContentFinished:
      result = _activePlugin.onContentFinished();
      break;
    case ContentError:
      result = _activePlugin.onContentError(_parameter);
      break;
    default:
      DebugMode.assertFail(TAG, "request admode when admode is not defined");
      break;
    }
    if (result) {
      _admode = mode;
    }
    return result;
  }

  @Override
  public StateNotifier getStateNotifier() {
    return this;
  }

  public AdMode adMode() {
    return _admode;
  }

  public boolean inAdMode() {
    return _admode != AdMode.None;
  }

  @Override
  public void resetAds() {
    if (_activePlugin instanceof VastPlugin) {
      ((VastPlugin) _activePlugin).resetAds();
    }
  }

  @Override
  public void skipAd() {
    if (_activePlugin instanceof VastPlugin) {
      ((VastPlugin) _activePlugin).skipAd();
    }
  }

  @Override
  public void update(Observable observable, Object data) {
    StateNotifier notifier = (StateNotifier) observable;
    if (notifier != getNotifierFromPlugin(_activePlugin)) {
      return;
    }

    String notification = data.toString();
    processNotification(notifier, notification);
  }

  private void processNotification(StateNotifier notifier, String notification) {
    DebugMode.logD(TAG, "receive notification " + notification + " from "
        + notifier.toString());
    if (notification.equals(OoyalaPlayer.STATE_CHANGED_NOTIFICATION)) {
      State state = notifier.getState();
      setState(state);
    }
  }

  private StateNotifier getNotifierFromPlugin(final AdPluginInterface plugin) {
    if (!(plugin instanceof PlayerInterface)) {
      return null;
    }
    return ((PlayerInterface) plugin).getStateNotifier();
  }

}
