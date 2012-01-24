package com.ooyala.android;

import java.util.Set;

public interface StreamSelector {
  public Stream bestStream(Set<Stream> streams);
}
