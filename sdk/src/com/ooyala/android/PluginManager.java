package com.ooyala.android;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.ooyala.android.plugin.AdPluginInterface;
import com.ooyala.android.plugin.AdPluginInterface.AdMode;

class PluginManager {
  private static final String TAG = PluginManager.class.getName();
  private WeakReference<ActiveSwitchInterface> _player;
  private List<AdPluginInterface> _plugins = new ArrayList<AdPluginInterface>();
  private AdPluginInterface _activePlugin = null;
  private AdMode _admode = AdMode.None;
  private int _playhead = 0;
  private int _cuePointIndex = 0;

  protected PluginManager(ActiveSwitchInterface player) {
    _player = new WeakReference<ActiveSwitchInterface>(player);
  }

  public static PluginManager createInstance(ActiveSwitchInterface player) {
    PluginManager manager = new PluginManager(player);
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

    switch (_admode) {
    case Preroll:
      do {
        _activePlugin = getNextPlugin(_activePlugin);
      } while (_activePlugin != null && !_activePlugin.onInitialPlay());
      break;
    case Midroll:
      do {
        _activePlugin = getNextPlugin(_activePlugin);
      } while (_activePlugin != null && !onMidrollEvents());
      break;
    case Postroll:
      do {
        _activePlugin = getNextPlugin(_activePlugin);
      } while (_activePlugin != null && !_activePlugin.onContentFinished());
      break;
    default:
      _activePlugin = null;
    }
    
    if (_activePlugin == null) {
      _player.get().exitActive();
    }
    return true;
  }

  // boolean Manager.insertPluginView()
  // boolean Manager.removePluginView()
  
  // helper functions 
  private AdPluginInterface getNextPlugin (final AdPluginInterface plugin) {
    if (_plugins.size() == 0) {
      return null;
    }

    int index = _plugins.indexOf(plugin);
    if (index < 0 || index >= _plugins.size() - 1) {
      return null;
    }
    return _plugins.get(index + 1);
  }

  private boolean onMidrollEvents() {
    if (_cuePointIndex <= 0) {
      return _activePlugin.onPlayheadUpdate(_playhead);
    } else {
      return _activePlugin.onCuePoint(_cuePointIndex);
    }
  }

  public void onSuspend() {
    for (AdPluginInterface plugin : _plugins) {
      plugin.suspend();
    }
  }
  
  public void onResume() {
    for (AdPluginInterface plugin : _plugins) {
      plugin.resume();
    }
  }

}
