package com.ooyala.android.player.exoplayer;

import android.content.Context;

import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.upstream.UriDataSource;
import com.google.android.exoplayer.upstream.UriLoadable;
import com.google.android.exoplayer.util.ManifestFetcher;

import java.io.IOException;

/**
 * Created by zchen on 2/10/16.
 *
 * package private on purpose
 */
abstract class RendererBuilderBase<T> implements RendererBuilderInterface, ManifestFetcher.ManifestCallback<T> {
  protected final Context context;
  protected final String userAgent;
  protected final String url;
  protected final RendererBuilderCallback player;

  protected UriDataSource manifestDataSource;
  protected ManifestFetcher<T> manifestFetcher;

  private boolean canceled;

  public RendererBuilderBase(Context context, String userAgent, String url, RendererBuilderCallback player) {
    this.context = context;
    this.userAgent = userAgent;
    this.url = url;
    this.player = player;
  }

  public void buildRenderers() {
    UriLoadable.Parser<T> parser = createParser();
    manifestDataSource = new DefaultUriDataSource(context, userAgent);
    manifestFetcher = new ManifestFetcher<T>(url, manifestDataSource, parser);
    manifestFetcher.singleLoad(player.getMainHandler().getLooper(), this);
  }
  /**
   * Cancels the current build operation, if there is one. Else does nothing.
   */
  public void cancel() {
    canceled = true;
  }

  public boolean isCanceled() {
    return canceled;
  }

  /*
   * get called when manifest is downloaded successfully.
   */
  abstract protected void processManifest(T manifest);

  /*
   * subclass must return a manifest parser.
   */
  abstract protected UriLoadable.Parser<T> createParser();

  @Override
  public void onSingleManifestError(IOException e) {
    if (canceled) {
      return;
    }

    player.onRenderersError(e);
  }

  @Override
  public void onSingleManifest(T manifest) {
    if (canceled) {
      return;
    }
    processManifest(manifest);
  }
}
