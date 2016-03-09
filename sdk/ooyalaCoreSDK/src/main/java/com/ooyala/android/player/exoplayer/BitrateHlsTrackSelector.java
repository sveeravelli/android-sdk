package com.ooyala.android.player.exoplayer;

import android.text.TextUtils;

import com.google.android.exoplayer.hls.HlsMasterPlaylist;
import com.google.android.exoplayer.hls.HlsTrackSelector;
import com.google.android.exoplayer.hls.Variant;
import com.ooyala.android.util.DebugMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zchen on 2/18/16.
 */
public class BitrateHlsTrackSelector implements  HlsTrackSelector {
  private static final String TAG = BitrateHlsTrackSelector.class.getSimpleName();
  private long lowerBitrateThreshold;
  private long upperBitrateThreshold;

  public BitrateHlsTrackSelector(long upperBitrateThreshold, long lowerBitrateThreshold) {
    this.upperBitrateThreshold = upperBitrateThreshold;
    this.lowerBitrateThreshold = lowerBitrateThreshold;
  }

  @Override
  public void selectTracks(HlsMasterPlaylist playlist, HlsTrackSelector.Output output) throws IOException {
    ArrayList<Variant> enabledVariantList = new ArrayList<Variant>(playlist.variants);
    ArrayList<Variant> videoVariants = new ArrayList<Variant>();
    ArrayList<Variant> audioOnlyVariants = new ArrayList<Variant>();
    for (int i = 0; i < playlist.variants.size(); i++) {
      Variant variant = playlist.variants.get(i);
      if (variant.format.height > 0 || variantHasExplicitCodecWithPrefix(variant, "avc")) {
        videoVariants.add(variant);

      } else if (variantHasExplicitCodecWithPrefix(variant, "mp4a")) {
        audioOnlyVariants.add(variant);
      }
    }

    if (!videoVariants.isEmpty()) {
      // We've identified some variants as definitely containing video. Assume variants within the
      // master playlist are marked consistently, and hence that we have the full set. Filter out
      // any other variants, which are likely to be audio only.
      enabledVariantList = videoVariants;
    } else if (audioOnlyVariants.size() < enabledVariantList.size()) {
      // We've identified some variants, but not all, as being audio only. Filter them out to leave
      // the remaining variants, which are likely to contain video.
      enabledVariantList.removeAll(audioOnlyVariants);
    }

    filterTracksWithBitrate(enabledVariantList);
    if (enabledVariantList.size() <= 0) {
      DebugMode.logE(TAG, "no track available between " + lowerBitrateThreshold + " and " + upperBitrateThreshold);
    }
    if (enabledVariantList.size() > 1) {
      Variant[] enabledVariants = new Variant[enabledVariantList.size()];
      enabledVariantList.toArray(enabledVariants);
      output.adaptiveTrack(playlist, enabledVariants);
    }
    for (int i = 0; i < enabledVariantList.size(); i++) {
      output.fixedTrack(playlist, enabledVariantList.get(i));
    }
  }

  private void filterTracksWithBitrate(List<Variant> tracks) {
    Iterator<Variant> it = tracks.iterator();
    while (it.hasNext()) {
      Variant variant = it.next();
      if (variant.format.bitrate < lowerBitrateThreshold || variant.format.bitrate > upperBitrateThreshold) {
        it.remove();
      }
    }
  }

  private static boolean variantHasExplicitCodecWithPrefix(Variant variant, String prefix) {
    String codecs = variant.format.codecs;
    if (TextUtils.isEmpty(codecs)) {
      return false;
    }
    String[] codecArray = codecs.split("(\\s*,\\s*)|(\\s*$)");
    for (int i = 0; i < codecArray.length; i++) {
      if (codecArray[i].startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }
}
