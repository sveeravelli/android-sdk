package com.ooyala.android;

import java.net.URL;
import java.util.Map;

public interface SecureURLGenerator
{
  public URL secureURL(String host, String uri, Map<String,String> params);
}
