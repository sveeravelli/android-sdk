package com.ooyala.android.player.exoplayer;

/**
 * Created by zchen on 1/28/16.
 */
public interface RendererBuilder {
  /**
   * Builds renderers for playback.
   *
   * @param player The player for which renderers are being built.
   */
  void buildRenderers(RendererBuilderListener player);
  /**
   * Cancels the current build operation, if there is one. Else does nothing.
   */
  void cancel();
}
