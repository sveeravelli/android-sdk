package com.ooyala.android.player.exoplayer;

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.os.Handler;

import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecUtil;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.chunk.VideoFormatSelectorUtil;
import com.google.android.exoplayer.hls.DefaultHlsTrackSelector;
import com.google.android.exoplayer.hls.HlsChunkSource;
import com.google.android.exoplayer.hls.HlsMasterPlaylist;
import com.google.android.exoplayer.hls.HlsPlaylist;
import com.google.android.exoplayer.hls.HlsPlaylistParser;
import com.google.android.exoplayer.hls.HlsSampleSource;
import com.google.android.exoplayer.hls.PtsTimestampAdjusterProvider;
import com.google.android.exoplayer.metadata.Id3Parser;
import com.google.android.exoplayer.metadata.MetadataTrackRenderer;
import com.google.android.exoplayer.text.eia608.Eia608TrackRenderer;
import com.google.android.exoplayer.upstream.BandwidthMeter;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.upstream.UriLoadable;

import java.util.Map;

/**
 * A {@link RendererBuilderInterface} for HLS.
 */
public class HlsRendererBuilder extends RendererBuilderBase<HlsPlaylist> {
  private static final int MAIN_BUFFER_SEGMENTS = 256;

  public HlsRendererBuilder(Context context, String userAgent, String url, RendererBuilderCallback player) {
    super(context, userAgent, url, player);
  }


  @Override
  protected UriLoadable.Parser<HlsPlaylist> createParser() {
    return new HlsPlaylistParser();
  }

  @Override
  protected void processManifest(HlsPlaylist manifest) {
    Handler mainHandler = player.getMainHandler();
    BandwidthMeter bandwidthMeter = player.getBandwidthMeter();
    LoadControl loadControl = player.getLoadControl();

    PtsTimestampAdjusterProvider timestampAdjusterProvider = new PtsTimestampAdjusterProvider();

    int[] variantIndices = null;
    if (manifest instanceof HlsMasterPlaylist) {
      HlsMasterPlaylist masterPlaylist = (HlsMasterPlaylist) manifest;
      try {
        variantIndices = VideoFormatSelectorUtil.selectVideoFormatsForDefaultDisplay(
            context, masterPlaylist.variants, null, false);
      } catch (MediaCodecUtil.DecoderQueryException e) {
        player.onRenderersError(e);
        return;
      }
      if (variantIndices.length == 0) {
        player.onRenderersError(new IllegalStateException("No variants selected."));
        return;
      }
    }

    DataSource dataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);
    HlsChunkSource chunkSource =
        new HlsChunkSource(true, dataSource, url, manifest,
            DefaultHlsTrackSelector.newDefaultInstance(context), bandwidthMeter,
            timestampAdjusterProvider, HlsChunkSource.ADAPTIVE_MODE_SPLICE);
    HlsSampleSource sampleSource =
        new HlsSampleSource(
            chunkSource, loadControl, player.getBufferSegmentSize() * MAIN_BUFFER_SEGMENTS,
            mainHandler,
            player, ExoStreamPlayer.TYPE_VIDEO);
    MediaCodecVideoTrackRenderer videoRenderer =
        new MediaCodecVideoTrackRenderer(
            context,
            sampleSource, MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT,
            5000, mainHandler, player, 50);
    MediaCodecAudioTrackRenderer audioRenderer =
        new MediaCodecAudioTrackRenderer(sampleSource,
            MediaCodecSelector.DEFAULT, null, true, player.getMainHandler(), player,
            AudioCapabilities.getCapabilities(context), AudioManager.STREAM_MUSIC);
    MetadataTrackRenderer<Map<String, Object>> id3Renderer = new MetadataTrackRenderer<>(
        sampleSource, new Id3Parser(), player, mainHandler.getLooper());
    Eia608TrackRenderer closedCaptionRenderer = new Eia608TrackRenderer(sampleSource, player,
        mainHandler.getLooper());

    TrackRenderer[] renderers = new TrackRenderer[ExoStreamPlayer.RENDERER_COUNT];
    renderers[ExoStreamPlayer.TYPE_VIDEO] = videoRenderer;
    renderers[ExoStreamPlayer.TYPE_AUDIO] = audioRenderer;
    renderers[ExoStreamPlayer.TYPE_METADATA] = id3Renderer;
    renderers[ExoStreamPlayer.TYPE_TEXT] = closedCaptionRenderer;
    player.onRenderers(renderers, bandwidthMeter);
  }
}
