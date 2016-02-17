package com.ooyala.android.player.exoplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.os.Handler;
import android.util.Log;

import com.google.android.exoplayer.DummyTrackRenderer;
import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.chunk.ChunkSampleSource;
import com.google.android.exoplayer.chunk.ChunkSource;
import com.google.android.exoplayer.chunk.FormatEvaluator;
import com.google.android.exoplayer.dash.DashChunkSource;
import com.google.android.exoplayer.dash.DefaultDashTrackSelector;
import com.google.android.exoplayer.dash.mpd.AdaptationSet;
import com.google.android.exoplayer.dash.mpd.MediaPresentationDescription;
import com.google.android.exoplayer.dash.mpd.MediaPresentationDescriptionParser;
import com.google.android.exoplayer.dash.mpd.Period;
import com.google.android.exoplayer.dash.mpd.UtcTimingElement;
import com.google.android.exoplayer.dash.mpd.UtcTimingElementResolver;
import com.google.android.exoplayer.drm.StreamingDrmSessionManager;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.text.TextTrackRenderer;
import com.google.android.exoplayer.upstream.BandwidthMeter;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.upstream.UriLoadable;

import java.io.IOException;

/**
 * Created by zchen on 2/10/16.
 */
public class DashRendererBuilder extends RendererBuilderBase<MediaPresentationDescription>
    implements UtcTimingElementResolver.UtcTimingCallback {

  private static final String TAG = DashRendererBuilder.class.getSimpleName();

  private static final int VIDEO_BUFFER_SEGMENTS = 200;
  private static final int AUDIO_BUFFER_SEGMENTS = 54;
  private static final int TEXT_BUFFER_SEGMENTS = 2;
  private static final int LIVE_EDGE_LATENCY_MS = 30000;

  private static final int SECURITY_LEVEL_UNKNOWN = -1;
  private static final int SECURITY_LEVEL_1 = 1;
  private static final int SECURITY_LEVEL_3 = 3;

  private MediaPresentationDescription manifest;
  private long elapsedRealtimeOffset;

//  private final MediaDrmCallback drmCallback;

  public DashRendererBuilder(Context context, String userAgent, String url, RendererBuilderCallback player) {
    super(context, userAgent, url, player);
  }

  @Override
  protected UriLoadable.Parser<MediaPresentationDescription> createParser() {
    return new MediaPresentationDescriptionParser();
  }

  @Override
  protected void processManifest(MediaPresentationDescription manifest) {
    this.manifest = manifest;
    if (manifest.dynamic && manifest.utcTiming != null) {
      UtcTimingElementResolver.resolveTimingElement(manifestDataSource, manifest.utcTiming,
              manifestFetcher.getManifestLoadCompleteTimestamp(), this);
    } else {
      assembleRenderers();
    }
  }

  @Override
  public void onTimestampResolved(UtcTimingElement utcTiming, long elapsedRealtimeOffset) {
    if (isCanceled()) {
      return;
    }

    this.elapsedRealtimeOffset = elapsedRealtimeOffset;
    assembleRenderers();
  }

  @Override
  public void onTimestampError(UtcTimingElement utcTiming, IOException e) {
    if (isCanceled()) {
      return;
    }

    Log.e(TAG, "Failed to resolve UtcTiming element [" + utcTiming + "]", e);
        // Be optimistic and continue in the hope that the device clock is correct.
    assembleRenderers();
  }

  private void assembleRenderers() {

    Handler mainHandler = player.getMainHandler();
    LoadControl loadControl = player.getLoadControl();
    BandwidthMeter bandwidthMeter = player.getBandwidthMeter();

    boolean hasContentProtection = this.contentProtected();

    // Check drm support if necessary.
    boolean filterHdContent = false;
    StreamingDrmSessionManager drmSessionManager = null;
    if (hasContentProtection) {
//      if (Util.SDK_INT < 18) {
        player.onRenderersError(
            new UnsupportedDrmException(UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME));
        return;
//      }

//      try {
//        drmSessionManager = StreamingDrmSessionManager.newWidevineInstance(
//            player.getPlaybackLooper(), drmCallback, null, player.getMainHandler(), player);
//        filterHdContent = getWidevineSecurityLevel(drmSessionManager) != SECURITY_LEVEL_1;
//      } catch (UnsupportedDrmException e) {
//        player.onRenderersError(e);
//        return;
//      }
    }

    // Build the video renderer.
    int bufferSegmentSize = player.getBufferSegmentSize();
    DataSource videoDataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);
    ChunkSource videoChunkSource = new DashChunkSource(manifestFetcher,
            DefaultDashTrackSelector.newVideoInstance(context, true, filterHdContent),
            videoDataSource, new FormatEvaluator.AdaptiveEvaluator(bandwidthMeter),
            LIVE_EDGE_LATENCY_MS,
            elapsedRealtimeOffset, mainHandler, player, ExoStreamPlayer.TYPE_VIDEO);
    ChunkSampleSource videoSampleSource = new ChunkSampleSource(videoChunkSource, loadControl,
        VIDEO_BUFFER_SEGMENTS * bufferSegmentSize, mainHandler, player, ExoStreamPlayer.TYPE_VIDEO);
        TrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(context, videoSampleSource,
            MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 5000,
            drmSessionManager, true, mainHandler, player, 50);

        // Build the audio renderer.
    DataSource audioDataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);
    ChunkSource audioChunkSource = new DashChunkSource(manifestFetcher,
        DefaultDashTrackSelector.newAudioInstance(), audioDataSource, null, LIVE_EDGE_LATENCY_MS,
        elapsedRealtimeOffset, mainHandler, player, ExoStreamPlayer.TYPE_AUDIO);
    ChunkSampleSource audioSampleSource = new ChunkSampleSource(audioChunkSource, loadControl,
        AUDIO_BUFFER_SEGMENTS * bufferSegmentSize, mainHandler, player,
        ExoStreamPlayer.TYPE_AUDIO);
    TrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(audioSampleSource,
        MediaCodecSelector.DEFAULT, drmSessionManager, true, mainHandler, player,
        AudioCapabilities.getCapabilities(context), AudioManager.STREAM_MUSIC);

    // Build the text renderer.
    DataSource textDataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);
    ChunkSource textChunkSource = new DashChunkSource(manifestFetcher,
        DefaultDashTrackSelector.newTextInstance(), textDataSource, null, LIVE_EDGE_LATENCY_MS,
        elapsedRealtimeOffset, mainHandler, player, ExoStreamPlayer.TYPE_TEXT);
    ChunkSampleSource textSampleSource = new ChunkSampleSource(textChunkSource, loadControl,
        TEXT_BUFFER_SEGMENTS * bufferSegmentSize, mainHandler, player,
        ExoStreamPlayer.TYPE_TEXT);
    TrackRenderer textRenderer = new TextTrackRenderer(textSampleSource, player,
        mainHandler.getLooper());

    // Invoke the callback.
    TrackRenderer[] renderers = new TrackRenderer[ExoStreamPlayer.RENDERER_COUNT];
    renderers[ExoStreamPlayer.TYPE_VIDEO] = videoRenderer;
    renderers[ExoStreamPlayer.TYPE_AUDIO] = audioRenderer;
    renderers[ExoStreamPlayer.TYPE_TEXT] = textRenderer;
    renderers[ExoStreamPlayer.TYPE_METADATA] = new DummyTrackRenderer();
    player.onRenderers(renderers, bandwidthMeter);
  }

  private boolean contentProtected() {
    Period period = manifest.getPeriod(0);
    boolean contentProtected = false;
    for (int i = 0; i < period.adaptationSets.size(); i++) {
      AdaptationSet adaptationSet = period.adaptationSets.get(i);
      if (adaptationSet.type != AdaptationSet.TYPE_UNKNOWN) {
        contentProtected |= adaptationSet.hasContentProtection();
      }
    }
    return contentProtected;
  }

  private static int getWidevineSecurityLevel(StreamingDrmSessionManager sessionManager) {
    String securityLevelProperty = sessionManager.getPropertyString("securityLevel");
    return securityLevelProperty.equals("L1") ? SECURITY_LEVEL_1 :
        securityLevelProperty.equals("L3") ? SECURITY_LEVEL_3 : SECURITY_LEVEL_UNKNOWN;
  }
}



