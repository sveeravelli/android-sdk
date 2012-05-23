package com.ooyala.android;

import java.util.List;
import com.ooyala.android.EmbedTokenGeneratorCallback;

public interface EmbedTokenGenerator {
  public void getTokenForEmbedCodes(List<String> embedCodes, EmbedTokenGeneratorCallback callback);
}
