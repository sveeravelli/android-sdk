package com.ooyala.android;

import java.net.MalformedURLException;
import java.net.URL;

public class PlayerDomain {
  private static final String[] schemes = {"http://","https://"};
  private URL _domainUrl = null;

  public static boolean isValid(final String domain) {
     // DEFAULT schemes = "http", "https", "ftp"
    for (String scheme : PlayerDomain.schemes) {
      if (domain.startsWith(scheme)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Construct a PlayerDomain
   * @param domainString a valid domain string starts with http: or https:
   */
  public PlayerDomain(String domainString) throws Exception {
    if (!PlayerDomain.isValid(domainString)) {
      throw new Exception("Invalid Domain String: " + domainString);
    }
    
    try {
      _domainUrl = new URL(domainString);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    
  }
  
  public URL url() {
    return _domainUrl;
  }

  public String toString() {
    return _domainUrl.toString();
  }
}
