package com.ooyala.android.player.exoplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.net.Uri;

import com.google.android.exoplayer.DummyTrackRenderer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.text.TextTrackRenderer;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.BandwidthMeter;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;

/**
 * Created by zchen on 2/22/16.
 * renderer for MP4/MP3/AAC/TS/FLV
 * no need to download manifest for these formats
 */
public class ExtractorRendererBuilder implements RendererBuilderInterface {
  private final Context context;
  private final String userAgent;
  private final Uri uri;
  private final RendererBuilderCallback player;

  private static final int BUFFER_SEGMENT_COUNT = 256;

  public ExtractorRendererBuilder(Context context, String userAgent, Uri uri, RendererBuilderCallback player) {
    this.context = context;
    this.userAgent = userAgent;
    this.uri = uri;
    this.player = player;
  }

  @Override
  public void buildRenderers() {
    int bufferSegmentSize = player.getBufferSegmentSize();
    BandwidthMeter bandwithMeter = player.getBandwidthMeter();
    Allocator allocator = new DefaultAllocator(bufferSegmentSize);
    DataSource dataSource = new DefaultUriDataSource(context, bandwithMeter, userAgent);
    ExtractorSampleSource sampleSource = new ExtractorSampleSource(uri, dataSource, allocator,
        BUFFER_SEGMENT_COUNT * bufferSegmentSize);
    MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(context,
        sampleSource, MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 5000,
        player.getMainHandler(), player, 50);
    MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource,
        MediaCodecSelector.DEFAULT, null, true, player.getMainHandler(), player,
        AudioCapabilities.getCapabilities(context), AudioManager.STREAM_MUSIC);
    TrackRenderer textRenderer = new TextTrackRenderer(sampleSource, player,
        player.getMainHandler().getLooper());

    // Invoke the callback.
    TrackRenderer[] renderers = new TrackRenderer[ExoStreamPlayer.RENDERER_COUNT];
    renderers[ExoStreamPlayer.TYPE_VIDEO] = videoRenderer;
    renderers[ExoStreamPlayer.TYPE_AUDIO] = audioRenderer;
    renderers[ExoStreamPlayer.TYPE_TEXT] = textRenderer;
    renderers[ExoStreamPlayer.TYPE_METADATA] = new DummyTrackRenderer();
    player.onRenderers(renderers, bandwithMeter);
  }

  @Override
  public void cancel() {
    // Do nothing.
  }

  @Override
  public boolean isCanceled() {
    return false;
  }
}
