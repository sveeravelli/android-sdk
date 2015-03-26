package com.ooyala.android.castsdk;

import com.ooyala.android.OoyalaPlayer.State;

public interface OOMiniController {
  
  /**
   * Set the current castManager to the current mini controller.
   * This method should be called once you create a mini controller so that your mini controller
   * can control the cast playback through castManager.
   * This castManager is the interface between App and Chromecast SDK.
   * @param castManager
   */
  public void setCastManager(OOCastManager castManager);
  
  /**
   * Update the play/pause button image based on the given state.
   * For example if the given state is State.PLAYING the mini controller should display pauese button image otherwise the image should be play image
   * This method will only be called by castManager only when the playback state is changed for some reasons.
   * For example, the playback state is changed through other controls or the playback state becomes "buffering" because of the Internet issue
   * @param state
   */
  public void updatePlayPauseState(State state);
  
  /**
   * Only show the mini controller when the app is in cast mode, hide it otherwise.
   * This method should only be called by castManager because in the related activity we should only show the mini controller during casting and the cast
   * state can only be determined by castManager.
   * Check if the app is in cast mode by using castManager.isInCastMode()
   */
  public void updateVisibility();
  
  /**
   * Play the content when the play button in the mini controller is clicked by calling castManager.getCurrentCastPlayer().play() and
   * this call can be the only thing you need to do in this method;
   * This method should be called when the play button in your mini controller is clicked 
   */
  public void play();
  
  /**
   * Pause the content when the pause button in the mini controller is clicked by calling castManager.getCurrentCastPlayer().pause() and
   * this call can be the only thing you need to do in this method
   * This method should be called when the pause button in your mini controller is clicked 
   */
  public void pause();
}
