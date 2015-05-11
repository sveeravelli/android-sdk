package com.ooyala.android.castsdk;

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
   * @param isPlaying true if the content is playing right now
   */
  public void updatePlayPauseButtonImage(boolean isPlaying);
  
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

  public void show();

  public void dismiss();
}
