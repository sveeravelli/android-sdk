package com.ooyala.android;

import java.io.IOException;
import java.net.URL;

public class NetUtils {
  public void ping(URL url) {
    try {
      url.openConnection();
    } catch (IOException e) {
      // who cares. we're only pinging.
    }
  }
}
