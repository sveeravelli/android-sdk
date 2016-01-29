package com.ooyala.android.player.exoplayer;

import android.os.Handler;

import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.hls.HlsSampleSource;
import com.google.android.exoplayer.metadata.MetadataTrackRenderer;
import com.google.android.exoplayer.text.TextRenderer;
import com.google.android.exoplayer.upstream.BandwidthMeter;

import java.util.Map;

/**
 * Created by zchen on 1/28/16.
 */
public interface RendererBuilderListener extends
    HlsSampleSource.EventListener,
    MediaCodecVideoTrackRenderer.EventListener,
    MediaCodecAudioTrackRenderer.EventListener,
    TextRenderer,
    MetadataTrackRenderer.MetadataRenderer<Map<String, Object>>
{
  void onRenderers(TrackRenderer[] renderers, BandwidthMeter bandwidthMeter);

  void onRenderersError(Exception e);

  Handler getMainHandler();
}
