package com.ooyala.android;

import java.util.Observer;

public interface OoyalaPlayerControls extends Observer {
  /**
   * Set the parent layout that these controls should show on top of. This method should add the controls to
   * this layout in the correct positions (but they should not actually be visible)
   * @param layout the layout that these controls should show on top of
   */
  public void setParentLayout(OoyalaPlayerLayout layout);

  /**
   * Set the OoyalaPlayer that these controls will control.
   * @param player the player to control
   */
  public void setOoyalaPlayer(OoyalaPlayer player);

  /**
   * Show these controls (make all controls visible)
   */
  public void show();

  /**
   * Hide these controls (make all controls invisible/transparent)
   */
  public void hide();

  /**
   * @return true if the controls are currently showing, false otherwise
   */
  public boolean isShowing();

  /**
   * @return absolute pixel of the bottom bar's top distance to the bottom of the device. used by
   *         ClosedCaption to determine offset to the bottom.
   */
  public int bottomBarOffset();

}