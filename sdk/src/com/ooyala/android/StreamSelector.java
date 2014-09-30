package com.ooyala.android;

import java.util.Set;

import com.ooyala.android.item.Stream;

public interface StreamSelector {
  /**
   * The method used to select the correct Stream to play.
   * @param streams the array of streams to select from
   * @return the Stream to play
   */
  public Stream bestStream(Set<Stream> streams);
}
