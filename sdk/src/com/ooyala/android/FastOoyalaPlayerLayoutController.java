package com.ooyala.android;

import android.view.Gravity;
import android.widget.FrameLayout;

/**
 * This LayoutController is a faster LayoutController that will work only on one specific case: The OoyalaPlayerLayout it controls
 * is a direct child of the Activity's base layout which is a FrameLayout. This LayoutController uses basic controls and allows additional
 * overlays to be added. Fullscreening is done by simply resizing the OoyalaPlayerLayout to fill the entire screen, which does not trigger
 * a player reload thus causing this to be much faster at Fullscreening than OoyalaPlayerLayoutController.
 * @author jigish
 */
public class FastOoyalaPlayerLayoutController extends AbstractOoyalaPlayerLayoutController {
  private boolean _fullscreen = false;
  private FrameLayout.LayoutParams _inlineLP = null;
  private FrameLayout.LayoutParams _fullscreenLP = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.FILL);

  /**
   * Instantiate a FastOoyalaPlayerLayoutController
   * @param l the layout to use
   * @param apiKey the API Key to use
   * @param secret the secret to use
   * @param pcode the provider code to use
   * @param domain the embed domain to use
   */
  public FastOoyalaPlayerLayoutController(OoyalaPlayerLayout l, String apiKey, String secret, String pcode, String domain) {
    this(l, apiKey, secret, pcode, domain, DefaultControlStyle.AUTO);
  }

  /**
   * Instantiate a FastOoyalaPlayerLayoutController
   * @param l the layout to use
   * @param p the instantiated player to use
   */
  public FastOoyalaPlayerLayoutController(OoyalaPlayerLayout l, OoyalaPlayer p) {
    this(l, p, DefaultControlStyle.AUTO);
  }

  /**
   * Instantiate a FastOoyalaPlayerLayoutController
   * @param l the layout to use
   * @param apiKey the API Key to use
   * @param secret the secret to use
   * @param pcode the provider code to use
   * @param domain the embed domain to use
   * @param dcs the DefaultControlStyle to use (AUTO is default controls, NONE has no controls)
   */
  public FastOoyalaPlayerLayoutController(OoyalaPlayerLayout l, String apiKey, String secret, String pcode, String domain, DefaultControlStyle dcs) {
    this(l, new OoyalaPlayer(apiKey, secret, pcode, domain), dcs);
  }

  /**
   * Instantiate a FastOoyalaPlayerLayoutController
   * @param l the layout to use
   * @param p the instantiated player to use
   * @param dcs the DefaultControlStyle to use (AUTO is default controls, NONE has no controls)
   */
  public FastOoyalaPlayerLayoutController(OoyalaPlayerLayout l, OoyalaPlayer p, DefaultControlStyle dcs) {
    super(l, p, dcs);
    if (dcs == DefaultControlStyle.AUTO) {
      setFullscreenControls(createDefaultControls(_layout, true));
      _fullscreenControls.hide();
    }
    _inlineLP = (FrameLayout.LayoutParams)_layout.getLayoutParams();
  }

  /**
   * @return true if currently in fullscreen, false if not
   */
  @Override
  public boolean isFullscreen() {
    return _fullscreen;
  }

  /**
   * Sets the fullscreen state to this layout controller.
   * @param fullscreen
   */
  @Override
  public void setFullscreen(boolean fullscreen) {
    if (isFullscreen() && !fullscreen) { // Fullscreen -> Not Fullscreen
      _fullscreen = fullscreen;
      _fullscreenControls.hide();
      if (_fullscreenOverlay != null) { _fullscreenOverlay.hide(); }
      _layout.setLayoutParams(_inlineLP);
      _inlineControls.show();
      if (_inlineOverlay != null) { _inlineOverlay.show(); }
    } else if (!isFullscreen() && fullscreen) { // Not Fullscreen -> Fullscreen
      _fullscreen = fullscreen;
      _inlineControls.hide();
      if (_inlineOverlay != null) { _inlineOverlay.hide(); }
      _layout.setLayoutParams(_fullscreenLP);
      _layout.bringToFront();
      _fullscreenControls.show();
      if (_fullscreenOverlay != null) { _fullscreenOverlay.show(); }
    }
  }
}
