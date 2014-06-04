package com.ooyala.android;

import java.util.List;

public interface EmbedTokenGenerator {
  public void getTokenForEmbedCodes(List<String> embedCodes, EmbedTokenGeneratorCallback callback);
}
