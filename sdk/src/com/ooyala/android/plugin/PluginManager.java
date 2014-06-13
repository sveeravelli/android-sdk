package com.ooyala.android.plugin;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.ooyala.android.DebugMode;
import com.ooyala.android.plugin.AdPluginInterface.AdMode;

public class PluginManager implements ControlRequesterInterface {
  private static final String TAG = PluginManager.class.getName();
  private WeakReference<ControlManagerInterface> _player;
  private List<AdPluginInterface> _plugins = new ArrayList<AdPluginInterface>();
  private List<AdPluginInterface> _controlRequesters = new ArrayList<AdPluginInterface>();
  private AdMode _admode = AdMode.None;

  protected PluginManager(ControlManagerInterface player) {
    _player = new WeakReference<ControlManagerInterface>(player);
  }

  public static PluginManager createInstance(ControlManagerInterface player) {
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
    if (!_controlRequesters.contains(plugin)) {
      DebugMode.assertFail(TAG, plugin.toString()
          + " exit admode when it is not in the adMode");
    }
    _controlRequesters.remove(plugin);
    if (_controlRequesters.size() == 0) {
      _player.get().returnControl(this);
    } else {
      _controlRequesters.get(0).onAdModeEntered(_admode);
    }
    return true;
  }

  public boolean requestAdMode(final AdPluginInterface plugin) {
    DebugMode.assertCondition(_plugins.contains(plugin), TAG, plugin.toString()
        + " no registered before request ad mode");

    if (_controlRequesters.contains(plugin)) {
      DebugMode.assertFail(TAG, plugin.toString()
          + " already requested control");
      return false;
    }

    if (_controlRequesters.size() == 0) {
      boolean success = _player.get().requestControl(this);
      if (!success) {
        DebugMode.assertFail(TAG, "request control failed");
        return false;
      }
    }
    
    _controlRequesters.add(plugin);
    return true;
  }

  @Override
  public void onControlGranted() {
    if (_controlRequesters.size() == 0) {
      DebugMode.assertFail(TAG, "control is granted but no one want it");
    } else {
      _controlRequesters.get(0).onAdModeEntered(_admode);
    }
  }

  @Override
  public void onSuspend() {
    // TODO Auto-generated method stub

  }

  @Override
  public void onResume() {
    // TODO Auto-generated method stub

  }

  @Override
  public void onDestroy() {
    _player = null;
  }

  // boolean Manager.insertPluginView()
  // boolean Manager.removePluginView()
}
