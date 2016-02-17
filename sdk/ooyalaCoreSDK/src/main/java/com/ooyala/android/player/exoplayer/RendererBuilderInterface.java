package com.ooyala.android.player.exoplayer;

/**
 * Created by zchen on 1/28/16.
 */
public interface RendererBuilderInterface {
  /**
   * Builds renderers for playback.
   */
  void buildRenderers();

  /**
   * Cancels the current build operation, if there is one. Else does nothing.
   */
  void cancel();

  /**
   * @return true if builder is canceled, false otherwise.
   */
  boolean isCanceled();
}
