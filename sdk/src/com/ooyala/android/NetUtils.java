package com.ooyala.android;

import java.io.IOException;
import java.net.URL;

class NetUtils {
  public void ping(URL url) {
    if (url == null) { return; }
    try {
      url.openConnection();
    } catch (IOException e) {
      // who cares. we're only pinging.
    }
  }
}
