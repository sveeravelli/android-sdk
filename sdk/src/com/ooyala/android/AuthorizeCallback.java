package com.ooyala.android;

interface AuthorizeCallback {
  public void callback(boolean result, OoyalaException error);
}
