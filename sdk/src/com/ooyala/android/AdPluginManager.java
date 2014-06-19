package com.ooyala.android;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.ooyala.android.plugin.AdPluginInterface;

class AdPluginManager implements AdPluginInterface, AdPluginManagerInterface {
  private enum AdMode {
    None, ContentChanged, InitialPlay, Playhead, CuePoint, ContentFinished, ContentError
  };
  private static final String TAG = AdPluginManager.class.getName();
  private WeakReference<ActiveSwitchInterface> _player;
  private List<AdPluginInterface> _plugins = new ArrayList<AdPluginInterface>();
  private AdPluginInterface _activePlugin = null;
  private AdMode _admode = AdMode.None;
  private int _parameter = 0;
  private String _token = null;

  protected AdPluginManager(ActiveSwitchInterface player) {
    _player = new WeakReference<ActiveSwitchInterface>(player);
  }

  public static AdPluginManager createInstance(ActiveSwitchInterface player) {
    AdPluginManager manager = new AdPluginManager(player);
    return manager;
  }
  
  public boolean registerPlugin(final AdPluginInterface plugin) {
    if (_plugins.contains(plugin)) {
      return false;
    }
    _plugins.add(plugin);
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
    } while (_activePlugin != null && pluginRequestAdMode() == null);
    
    if (_activePlugin == null) {
      _player.get().exitActive();
    } else {
      _activePlugin.onAdModeEntered(_token);
    }
    return true;
  }

  // boolean Manager.insertPluginView()
  // boolean Manager.removePluginView()

  @Override
  public void pause() {
    if (_activePlugin != null) {
      _activePlugin.pause();
    }
  }

  @Override
  public void play() {
    if (_activePlugin != null) {
      _activePlugin.play();
    }
  }

  @Override
  public void stop() {
    if (_activePlugin != null) {
      _activePlugin.stop();
    }
  }

  @Override
  public void seekToTime(int timeInMillis) {
    if (_activePlugin != null) {
      _activePlugin.seekToTime(timeInMillis);
    }
  }

  @Override
  public int currentTime() {
    if (_activePlugin != null) {
      return _activePlugin.currentTime();
    }
    return 0;
  }

  @Override
  public int duration() {
    if (_activePlugin != null) {
      return _activePlugin.duration();
    }
    return 0;
  }

  @Override
  public int buffer() {
    if (_activePlugin != null) {
      return _activePlugin.buffer();
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
    if (_activePlugin != null) {
      return _activePlugin.seekable();
    }
    return false;
  }

  @Override
  public String onContentChanged() {
    return onAdMode(AdMode.ContentChanged);
  }

  @Override
  public String onInitialPlay() {
    return onAdMode(AdMode.ContentChanged);
  }

  @Override
  public String onPlayheadUpdate(int playhead) {
    _parameter = playhead;
    return onAdMode(AdMode.Playhead);
  }

  @Override
  public String onContentFinished() {
    return onAdMode(AdMode.ContentFinished);
  }

  @Override
  public String onCuePoint(int cuePointIndex) {
    _parameter = cuePointIndex;
    return onAdMode(AdMode.CuePoint);
  }

  @Override
  public String onContentError(int errorCode) {
    _parameter = errorCode;
    return onAdMode(AdMode.ContentError);
  }

  @Override
  public void onAdModeEntered(String token) {
    if (_activePlugin == null) {
      DebugMode.assertFail(TAG,
          "control is granted while active plugin is null");
      return;
    }
    _activePlugin.onAdModeEntered(_token);
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
  private String onAdMode(AdMode mode) {
    _admode = mode;
    _token = null;
    if (_plugins.size() <= 0) {
      return null;
    }

    do {
      _activePlugin = getNextPlugin(_activePlugin);
    } while (_activePlugin != null && pluginRequestAdMode() == null);

    return _token;
  }

  /**
   * ad manager queries plugin whether it requires control
   */
  private String pluginRequestAdMode() {
    if (_activePlugin == null) {
      DebugMode.assertFail(TAG,
          "plugin method is called when active plugin is null");
      return null;
    }
    switch (_admode) {
    case ContentChanged:
      _token = _activePlugin.onContentChanged();
      break;
    case InitialPlay:
      _token = _activePlugin.onInitialPlay();
      break;
    case Playhead:
      _token = _activePlugin.onPlayheadUpdate(_parameter);
      break;
    case CuePoint:
      _token = _activePlugin.onCuePoint(_parameter);
      break;
    case ContentFinished:
      _token = _activePlugin.onContentFinished();
      break;
    case ContentError:
      _token = _activePlugin.onContentError(_parameter);
      break;
    default:
      DebugMode.assertFail(TAG, "request admode when admode is not defined");
      break;
    }
    return _token;
  }
}
