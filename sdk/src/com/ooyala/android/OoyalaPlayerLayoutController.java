package com.ooyala.android;

import android.R;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.MediaController;

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
  private int _touchCount = 0;

  private class DefaultOoyalaPlayerControls extends MediaController implements OoyalaPlayerControls {
    public DefaultOoyalaPlayerControls(Context c, boolean showFFandRW) {
      super(c, showFFandRW);
    }

    @Override
    public void setOoyalaPlayer(OoyalaPlayer player) {
      super.setMediaPlayer(player);
    }

    @Override
    public void setParentLayout(OoyalaPlayerLayout layout) {
      super.setAnchorView(layout);
    }
  }

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
      setInlineControls(createDefaultControls(_layout));
    }
    _player.setLayoutController(this);
    _layout.setLayoutController(this);
  }

  private OoyalaPlayerControls createDefaultControls(View v) {
    DefaultOoyalaPlayerControls controls = new DefaultOoyalaPlayerControls(v.getContext(), false);
    controls.setOoyalaPlayer(_player);
    controls.setAnchorView(v);
    Log.d(this.getClass().getName(), "TEST - TOUCH CONTROLS PARENT: "+controls.getParent().getClass().getName());
    controls.setPrevNextListeners(new View.OnClickListener() {
      @Override
      public void onClick(View v) { // next
        _player.nextVideo(OoyalaPlayer.DO_PLAY);
      }
    }, new View.OnClickListener() {
      @Override
      public void onClick(View v) { // previous
        _player.previousVideo(OoyalaPlayer.DO_PLAY);
      }
    });
    return controls;
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
    if (isFullscreen() && !fullscreen) { // Fullscreen -> Not Fullscreen
      _fullscreenDialog.dismiss();
      _fullscreenDialog = null;
      _fullscreenControls = null;
      _fullscreenLayout = null;
    } else if (!isFullscreen() && fullscreen) { // Not Fullscreen -> Fullscreen
      _fullscreenDialog = new Dialog(_layout.getContext(), R.style.Theme_Black_NoTitleBar_Fullscreen);
      _fullscreenLayout = new OoyalaPlayerLayout(_fullscreenDialog.getContext());
      _fullscreenLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT, Gravity.FILL));
      _fullscreenLayout.setLayoutController(this);
      _fullscreenDialog.setContentView(_fullscreenLayout);
      _fullscreenDialog.show();
      setFullscreenControls(createDefaultControls(_fullscreenLayout));
    }
    if (_inlineOverlay != null && _layout != null) {
      _inlineOverlay.setParentLayout(_layout);
    }
    if (_fullscreenOverlay != null && _fullscreenLayout != null) {
      _fullscreenOverlay.setParentLayout(_fullscreenLayout);
    }
    _player.resume();
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
          _touchCount++;
          if (_touchCount % 2 == 0) {
            _touchCount = 0;
            setFullscreen(!isFullscreen());
          } else {
            if (getControls() != null) { Log.d(this.getClass().getName(), "TEST - TOUCH("+(source == _fullscreenLayout)+") - SHOWING CONTROLS"); getControls().show(); }
            if (getOverlay() != null) { Log.d(this.getClass().getName(), "TEST - TOUCH("+(source == _fullscreenLayout)+") - SHOWING OVERLAY"); getOverlay().show(); }
          }
          return false;
      }
    }
    return false;
  }
}
