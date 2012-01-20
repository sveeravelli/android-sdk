package com.ooyala.android;

import android.R;
import android.app.Dialog;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class OoyalaPlayerLayoutController implements LayoutController {
  public static enum DefaultControlStyle {
    NONE,
    AUTO
  };

  private OoyalaPlayerLayout _layout = null;
  private Dialog _fullscreenDialog = null;
  private OoyalaPlayerLayout _fullscreenLayout = null;
  private OoyalaPlayerControls _inlineControls = null;
  private OoyalaPlayerControls _fullscreenControls = null;
  private OoyalaPlayerControls _inlineOverlay = null;
  private OoyalaPlayerControls _fullscreenOverlay = null;
  private OoyalaPlayer _player = null;

  /**
   * Instantiate an OoyalaPlayerLayoutController
   * @param l the layout to use
   * @param apiKey the API Key to use
   * @param secret the secret to use
   * @param pcode the provider code to use
   * @param domain the embed domain to use
   */
  public OoyalaPlayerLayoutController(OoyalaPlayerLayout l, String apiKey, String secret, String pcode, String domain) {
    this(l, apiKey, secret, pcode, domain, DefaultControlStyle.AUTO);
  }

  /**
   * Instantiate an OoyalaPlayerLayoutController
   * @param l the layout to use
   * @param p the instantiated player to use
   */
  public OoyalaPlayerLayoutController(OoyalaPlayerLayout l, OoyalaPlayer p) {
    this(l, p, DefaultControlStyle.AUTO);
  }

  /**
   * Instantiate an OoyalaPlayerLayoutController
   * @param l the layout to use
   * @param apiKey the API Key to use
   * @param secret the secret to use
   * @param pcode the provider code to use
   * @param domain the embed domain to use
   * @param dcs the DefaultControlStyle to use (AUTO is default controls, NONE has no controls)
   */
  public OoyalaPlayerLayoutController(OoyalaPlayerLayout l, String apiKey, String secret, String pcode, String domain, DefaultControlStyle dcs) {
    this(l, new OoyalaPlayer(apiKey, secret, pcode, domain), dcs);
  }

  /**
   * Instantiate an OoyalaPlayerLayoutController
   * @param l the layout to use
   * @param p the instantiated player to use
   * @param dcs the DefaultControlStyle to use (AUTO is default controls, NONE has no controls)
   */
  public OoyalaPlayerLayoutController(OoyalaPlayerLayout l, OoyalaPlayer p, DefaultControlStyle dcs) {
    _player = p;
    _layout = l;
    if (dcs == DefaultControlStyle.AUTO) {
      setInlineControls(createDefaultControls(_layout, false));
    }
    _player.setLayoutController(this);
    _layout.setLayoutController(this);
  }

  private OoyalaPlayerControls createDefaultControls(OoyalaPlayerLayout layout, boolean fullscreen) {
    if (fullscreen) {
      return new DefaultOoyalaPlayerFullscreenControls(_player, layout);
    } else {
      return new DefaultOoyalaPlayerInlineControls(_player, layout);
    }
  }

  public void setInlineOverlay(OoyalaPlayerControls controlsOverlay) {
    _inlineOverlay = controlsOverlay;
    if (_layout != null)
      _inlineOverlay.setParentLayout(_layout);
    _inlineOverlay.setOoyalaPlayer(_player);
  }

  public void setFullscreenOverlay(OoyalaPlayerControls controlsOverlay) {
    _fullscreenOverlay = controlsOverlay;
    if (_fullscreenLayout != null)
      _fullscreenOverlay.setParentLayout(_fullscreenLayout);
    _fullscreenOverlay.setOoyalaPlayer(_player);
  }

  /**
   * Get the OoyalaPlayer associated with this Controller
   * @return the OoyalaPlayer
   */
  public OoyalaPlayer getPlayer() {
    return _player;
  }

  /**
   * Get the current active layout
   * @return the current active layout
   */
  public OoyalaPlayerLayout getLayout() {
    return isFullscreen() ? _fullscreenLayout : _layout;
  }

  /**
   * @return true if currently in fullscreen, false if not
   */
  public boolean isFullscreen() {
    return _fullscreenLayout != null;
  }

  /**
   * Sets the fullscreen state to this layout controller.
   * @param fullscreen
   */
  public void setFullscreen(boolean fullscreen) {
    _player.suspend();
    OoyalaPlayerControls controlsToShow = null;
    OoyalaPlayerControls overlayToShow = null;
    if (isFullscreen() && !fullscreen) { // Fullscreen -> Not Fullscreen
      _fullscreenDialog.dismiss();
      _fullscreenDialog = null;
      _fullscreenControls = null;
      _fullscreenLayout = null;
      controlsToShow = _inlineControls;
      if (_inlineOverlay != null) {
        _inlineOverlay.setParentLayout(_layout);
        overlayToShow = _inlineOverlay;
      }
    } else if (!isFullscreen() && fullscreen) { // Not Fullscreen -> Fullscreen
      _fullscreenDialog = new Dialog(_layout.getContext(), R.style.Theme_Black_NoTitleBar_Fullscreen);
      _fullscreenLayout = new OoyalaPlayerLayout(_fullscreenDialog.getContext());
      _fullscreenLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT, Gravity.FILL));
      _fullscreenLayout.setLayoutController(this);
      _fullscreenDialog.setContentView(_fullscreenLayout);
      _fullscreenDialog.show();
      setFullscreenControls(createDefaultControls(_fullscreenLayout, true));
      controlsToShow = _fullscreenControls;
      if (_fullscreenOverlay != null) {
        _fullscreenOverlay.setParentLayout(_fullscreenLayout);
        overlayToShow = _fullscreenOverlay;
      }
    }
    _player.resume();
    if (controlsToShow != null) { controlsToShow.show(); }
    if (overlayToShow != null) { overlayToShow.show(); }
  }

  public void setInlineControls(OoyalaPlayerControls controls) {
    _inlineControls = controls;
  }

  public void setFullscreenControls(OoyalaPlayerControls controls) {
    _fullscreenControls = controls;
  }

  public OoyalaPlayerControls getControls() {
    return isFullscreen() ? _fullscreenControls : _inlineControls;
  }

  public OoyalaPlayerControls getOverlay() {
    return isFullscreen() ? _fullscreenOverlay : _inlineOverlay;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event, OoyalaPlayerLayout source) {
    Log.d(this.getClass().getName(), "TEST - TOUCH("+(source == _fullscreenLayout)+")");
    //the MediaController will hide after 3 seconds - tap the screen to make it appear again
    if (_player != null) {
      switch(_player.getState()) {
        case INIT:
        case LOADING:
        case ERROR:
          return false;
        default:
          if (getControls() != null) {
            if (getControls().isShowing()) {
              Log.d(this.getClass().getName(), "TEST - TOUCH("+(source == _fullscreenLayout)+") - HIDING CONTROLS");
              getControls().hide();
            } else {
              Log.d(this.getClass().getName(), "TEST - TOUCH("+(source == _fullscreenLayout)+") - SHOWING CONTROLS");
              getControls().show();
            }
          }
          if (getOverlay() != null) {
            if (getOverlay().isShowing()) {
              Log.d(this.getClass().getName(), "TEST - TOUCH("+(source == _fullscreenLayout)+") - HIDING OVERLAY");
              getOverlay().hide();
            } else {
              Log.d(this.getClass().getName(), "TEST - TOUCH("+(source == _fullscreenLayout)+") - SHOWING OVERLAY");
              getOverlay().show();
            }
          }
          return false;
      }
    }
    return false;
  }
}
