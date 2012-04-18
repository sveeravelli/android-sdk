package com.ooyala.android;

import java.util.List;

public interface EmbedTokenGenerator {
  public String getTokenForEmbedCodes(List<String> embedCodes);
}
