package com.ooyala.android;

import java.util.List;

/**
 * Defines a class that will generate Ooyala Player Tokens for playback authentication
 * @author michael.len
 *
 */
public interface EmbedTokenGenerator {
  public void getTokenForEmbedCodes(List<String> embedCodes, EmbedTokenGeneratorCallback callback);
}
