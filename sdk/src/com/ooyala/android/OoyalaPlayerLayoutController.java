package com.ooyala.android;

import android.R;
import android.app.Dialog;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * This LayoutController is a generic LayoutController that will work in most cases (regardless of the
 * containing Layout type). It uses basic controls and allows additional overlays to be added. Fullscreening
 * is done by opening a full screen Dialog and filling it with a dynamically created OoyalaPlayerLayout.
 * Because of this, playback will be suspended and subsequently resumed during this process. As a result,
 * fullscreening is slower than if the OoyalaPlayerLayout is embeded directly in the Activity's base layout,
 * that base layout is a FrameLayout, and the LayoutController used is FastOoyalaPlayerLayoutController.
 * @author jigish
 */
public class OoyalaPlayerLayoutController extends AbstractOoyalaPlayerLayoutController {

  /**
   * Instantiate an OoyalaPlayerLayoutController
   * @param l the layout to use
   * @param apiKey the API Key to use
   * @param secret the secret to use
   * @param pcode the provider code to use
   * @param domain the embed domain to use
   */
  public OoyalaPlayerLayoutController(OoyalaPlayerLayout l, String apiKey, String secret, String pcode,
      String domain) {
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
  public OoyalaPlayerLayoutController(OoyalaPlayerLayout l, String apiKey, String secret, String pcode,
      String domain, DefaultControlStyle dcs) {
    this(l, new OoyalaPlayer(apiKey, secret, pcode, domain), dcs);
  }

  /**
   * Instantiate an OoyalaPlayerLayoutController
   * @param l the layout to use
   * @param p the instantiated player to use
   * @param dcs the DefaultControlStyle to use (AUTO is default controls, NONE has no controls)
   */
  public OoyalaPlayerLayoutController(OoyalaPlayerLayout l, OoyalaPlayer p, DefaultControlStyle dcs) {
    super(l, p, dcs);
  }

  /**
   * @return true if currently in fullscreen, false if not
   */
  @Override
  public boolean isFullscreen() {
    return _fullscreenLayout != null;
  }

  /**
   * Sets the fullscreen state to this layout controller.
   * @param fullscreen
   */
  @Override
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
      _fullscreenLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT, Gravity.FILL));
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
    if (controlsToShow != null) {
      controlsToShow.show();
    }
    if (overlayToShow != null) {
      overlayToShow.show();
    }
  }
}
