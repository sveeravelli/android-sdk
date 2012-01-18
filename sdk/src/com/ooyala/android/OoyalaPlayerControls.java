package com.ooyala.android;

public interface OoyalaPlayerControls {
  /**
   * Set the parent layout that these controls should show on top of.
   * This method should add the controls to this layout in the correct positions (but they should not actually be visible)
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
}