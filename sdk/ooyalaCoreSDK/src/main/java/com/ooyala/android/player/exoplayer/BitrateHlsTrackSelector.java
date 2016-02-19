package com.ooyala.android.player.exoplayer;

import android.content.Context;
import android.text.TextUtils;

import com.google.android.exoplayer.chunk.VideoFormatSelectorUtil;
import com.google.android.exoplayer.hls.HlsMasterPlaylist;
import com.google.android.exoplayer.hls.HlsTrackSelector;
import com.google.android.exoplayer.hls.Variant;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by zchen on 2/18/16.
 */
public class BitrateHlsTrackSelector implements  HlsTrackSelector {
  private Context context;
  private long lowerBitrateThreshold;
  private long upperBitrateThreshold;

  public BitrateHlsTrackSelector(Context context, long upperBitrateThreshold, long lowerBitrateThreshold) {
    this.upperBitrateThreshold = upperBitrateThreshold;
    this.lowerBitrateThreshold = lowerBitrateThreshold;
    this.context = context;
  }

  @Override
  public void selectTracks(HlsMasterPlaylist playlist, HlsTrackSelector.Output output) throws IOException {
    ArrayList<Variant> enabledVariantList = new ArrayList<>();
    int[] variantIndices = VideoFormatSelectorUtil.selectVideoFormatsForDefaultDisplay(
        context, playlist.variants, null, false);
    for (int i = 0; i < variantIndices.length; i++) {
      enabledVariantList.add(playlist.variants.get(variantIndices[i]));
    }

    ArrayList<Variant> definiteVideoVariants = new ArrayList<>();
    ArrayList<Variant> definiteAudioOnlyVariants = new ArrayList<>();
    for (int i = 0; i < enabledVariantList.size(); i++) {
      Variant variant = enabledVariantList.get(i);
      if (variant.format.height > 0 || variantHasExplicitCodecWithPrefix(variant, "avc")) {
        // video
        if (variant.format.bitrate >= lowerBitrateThreshold && variant.format.bitrate <= upperBitrateThreshold) {
          definiteVideoVariants.add(variant);
        }
      } else if (variantHasExplicitCodecWithPrefix(variant, "mp4a")) {
        definiteAudioOnlyVariants.add(variant);
      }
    }

    if (!definiteVideoVariants.isEmpty()) {
      // We've identified some variants as definitely containing video. Assume variants within the
      // master playlist are marked consistently, and hence that we have the full set. Filter out
      // any other variants, which are likely to be audio only.
      enabledVariantList = definiteVideoVariants;
    } else if (definiteAudioOnlyVariants.size() < enabledVariantList.size()) {
      // We've identified some variants, but not all, as being audio only. Filter them out to leave
      // the remaining variants, which are likely to contain video.
      enabledVariantList.removeAll(definiteAudioOnlyVariants);
    } else {
      // Leave the enabled variants unchanged. They're likely either all video or all audio.
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
