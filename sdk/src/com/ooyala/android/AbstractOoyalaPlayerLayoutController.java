package com.ooyala.android;

import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.Toast;

public abstract class AbstractOoyalaPlayerLayoutController implements LayoutController {
  public static enum DefaultControlStyle {
    NONE, AUTO
  };

  protected OoyalaPlayerLayout _layout = null;
  protected Dialog _fullscreenDialog = null;
  protected OoyalaPlayerLayout _fullscreenLayout = null;
  protected OoyalaPlayerControls _inlineControls = null;
  protected OoyalaPlayerControls _fullscreenControls = null;
  protected OoyalaPlayerControls _inlineOverlay = null;
  protected OoyalaPlayerControls _fullscreenOverlay = null;
  protected OoyalaPlayer _player = null;

  /**
   * Instantiate an AbstractOoyalaPlayerLayoutController
   *
   * @param l the layout to use
   * @param apiKey the API Key to use
   * @param secret the secret to use
   * @param pcode the provider code to use
   * @param domain the embed domain to use
   */
  public AbstractOoyalaPlayerLayoutController(OoyalaPlayerLayout l, String apiKey, String secret,
      String pcode, String domain) {
    this(l, apiKey, secret, pcode, domain, DefaultControlStyle.AUTO);
  }

  /**
   * Instantiate an AbstractOoyalaPlayerLayoutController
   *
   * @param l the layout to use
   * @param apiKey the API Key to use
   * @param secret the secret to use
   * @param pcode the provider code to use
   * @param domain the embed domain to use
   * @param generator An embedTokenGenerator used to sign SAS requests
   */
  public AbstractOoyalaPlayerLayoutController(OoyalaPlayerLayout l, String apiKey, String secret,
      String pcode, String domain, EmbedTokenGenerator generator) {
    this(l, apiKey, secret, pcode, domain, DefaultControlStyle.AUTO, generator);
  }

  /**
   * Instantiate an AbstractOoyalaPlayerLayoutController
   *
   * @param l the layout to use
   * @param p the instantiated player to use
   */
  public AbstractOoyalaPlayerLayoutController(OoyalaPlayerLayout l, OoyalaPlayer p) {
    this(l, p, DefaultControlStyle.AUTO);
  }

  /**
   * Instantiate an AbstractOoyalaPlayerLayoutController
   *
   * @param l the layout to use
   * @param apiKey the API Key to use
   * @param secret the secret to use
   * @param pcode the provider code to use
   * @param domain the embed domain to use
   * @param dcs the DefaultControlStyle to use (AUTO is default controls, NONE has no controls)
   */
  public AbstractOoyalaPlayerLayoutController(OoyalaPlayerLayout l, String apiKey, String secret,
      String pcode, String domain, DefaultControlStyle dcs) {
    this(l, new OoyalaPlayer(apiKey, secret, pcode, domain), dcs);
  }

  /**
   * Instantiate an AbstractOoyalaPlayerLayoutController
   *
   * @param l the layout to use
   * @param apiKey the API Key to use
   * @param secret the secret to use
   * @param pcode the provider code to use
   * @param domain the embed domain to use
   * @param dcs the DefaultControlStyle to use (AUTO is default controls, NONE has no controls)
   * @param generator An embedTokenGenerator used to sign SAS requests
   */
  public AbstractOoyalaPlayerLayoutController(OoyalaPlayerLayout l, String apiKey, String secret,
      String pcode, String domain, DefaultControlStyle dcs, EmbedTokenGenerator generator) {
    this(l, new OoyalaPlayer(apiKey, secret, pcode, domain, generator), dcs);
  }

  /**
   * Instantiate an AbstractOoyalaPlayerLayoutController
   *
   * @param l the layout to use
   * @param p the instantiated player to use
   * @param dcs the DefaultControlStyle to use (AUTO is default controls, NONE has no controls)
   */
  public AbstractOoyalaPlayerLayoutController(OoyalaPlayerLayout l, OoyalaPlayer p, DefaultControlStyle dcs) {
    _player = p;
    _layout = l;
    _player.setLayoutController(this);
    _layout.setLayoutController(this);
    if (dcs == DefaultControlStyle.AUTO) {
      setInlineControls(createDefaultControls(_layout, false));
      _inlineControls.hide();
      _player.addObserver(_inlineControls);
    }
  }

  public void setInlineOverlay(OoyalaPlayerControls controlsOverlay) {
    _inlineOverlay = controlsOverlay;
    _inlineOverlay.setOoyalaPlayer(_player);
  }

  public void setFullscreenOverlay(OoyalaPlayerControls controlsOverlay) {
    _fullscreenOverlay = controlsOverlay;
    _fullscreenOverlay.setOoyalaPlayer(_player);
  }

  /**
   * Get the OoyalaPlayer associated with this Controller
   *
   * @return the OoyalaPlayer
   */
  public OoyalaPlayer getPlayer() {
    return _player;
  }

  /**
   * Get the current active layout
   *
   * @return the current active layout
   */
  public FrameLayout getLayout() {
    return isFullscreen() ? _fullscreenLayout.getPlayerFrame() : _layout.getPlayerFrame();
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
    // the MediaController will hide after 3 seconds - tap the screen to make it appear again
    if (_player != null) {
      switch (_player.getState()) {
        case INIT:
        case LOADING:
        case ERROR:
          return false;
        default:
          if (getControls() != null) {
            if (getControls().isShowing()) {
              getControls().hide();
            } else {
              getControls().show();
            }
          }
          if (getOverlay() != null) {
            if (getOverlay().isShowing()) {
              getOverlay().hide();
            } else {
              getOverlay().show();
            }
          }
          return false;
      }
    }
    return false;
  }

  @Override
  public void setFullscreen(boolean fullscreen) {}

  @Override
  public boolean isFullscreen() {
    return false;
  }

  public OoyalaPlayerControls createDefaultControls(OoyalaPlayerLayout layout, boolean fullscreen) {
    if (fullscreen) {
      return new DefaultOoyalaPlayerFullscreenControls(_player, layout);
    } else {
      return new DefaultOoyalaPlayerInlineControls(_player, layout);
    }
  }

  /**
   * Create and display the list of available languages.
   */
  public void showClosedCaptionsMenu() {
    AlertDialog dialog;
    Set<String> languageSet = _player.getAvailableClosedCaptionsLanguages();
    languageSet.add("None");

    final String[] items = languageSet.toArray(new String[0]);
    AlertDialog.Builder builder = new AlertDialog.Builder(_layout.getContext());
    builder.setTitle("Subtitles/Closed Captions");

    builder.setItems(items, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int pos) {
        String toastText = "Selected Language: "+items[pos];
        String language = items[pos];

        // Special case the None language
        if(language.equals("None")) {
          language = null;
          toastText = "Closed captions disabled";
        }

        Toast.makeText(_layout.getContext(),toastText,Toast.LENGTH_SHORT).show();
        _player.setClosedCaptionsLanguage(language);
      }
    });
    dialog = builder.create();
    dialog.show();
  }
}
