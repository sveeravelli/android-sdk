package com.ooyala.android.castsdk;

public interface CastMiniController {
  

  /**
   * Update the play/pause button image based on the given state.
   * For example if the given state is State.PLAYING the mini controller should display pauese button image otherwise the image should be play image
   * This method will only be called by castManager only when the playback state is changed for some reasons.
   * For example, the playback state is changed through other controls or the playback state becomes "buffering" because of the Internet issue
   * @param isPlaying true if the content is playing right now
   */
  public void updatePlayPauseButtonImage(boolean isPlaying);

  public void show();

  public void dismiss();
}
