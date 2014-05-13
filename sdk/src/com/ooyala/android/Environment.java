package com.ooyala.android;

public class Environment {
  /**
   * For internal use only
   */
  public static enum EnvironmentType {
    PRODUCTION, STAGING, LOCAL
  };

  static String JS_ANALYTICS_HOST = "http://player.ooyala.com";
  static String AUTHORIZE_HOST = "http://player.ooyala.com";
  static String CONTENT_TREE_HOST = "http://player.ooyala.com";
  static String DRM_HOST = "http://player.ooyala.com";
  static String BACKLOT_HOST = "http://cdn.api.ooyala.com";
  static String METADATA_HOST = "http://player.ooyala.com";

  static void setEnvironment(EnvironmentType e) {
    if (e == EnvironmentType.PRODUCTION) {
      JS_ANALYTICS_HOST = "http://player.ooyala.com";
      AUTHORIZE_HOST = "http://player.ooyala.com";
      CONTENT_TREE_HOST = "http://player.ooyala.com";
      DRM_HOST = "http://player.ooyala.com";
      BACKLOT_HOST = "http://cdn.api.ooyala.com";
      METADATA_HOST = "http://player.ooyala.com";
    } else if (e == EnvironmentType.STAGING) {
      JS_ANALYTICS_HOST = "http://player-staging.ooyala.com";
      AUTHORIZE_HOST = "http://player-staging.ooyala.com";
      CONTENT_TREE_HOST = "http://player-staging.ooyala.com";
      DRM_HOST = "http://player-staging.ooyala.com";
      BACKLOT_HOST = "http://api-staging.ooyala.com";
      METADATA_HOST = "http://player-staging.ooyala.com";
    } else if (e == EnvironmentType.LOCAL) {
      JS_ANALYTICS_HOST = "http://dev.corp.ooyala.com:3000";
      AUTHORIZE_HOST = "http://dev.corp.ooyala.com:4567";
      CONTENT_TREE_HOST = "http://dev.corp.ooyala.com:3000";
      DRM_HOST = "http://dev.corp.ooyala.com:4567";
      BACKLOT_HOST = "http://api-staging.ooyala.com";
      METADATA_HOST = "http://dev.corp.ooyala.com:3000";
    } else {
      JS_ANALYTICS_HOST = "http://player.ooyala.com";
      AUTHORIZE_HOST = "http://player.ooyala.com";
      CONTENT_TREE_HOST = "http://player.ooyala.com";
      DRM_HOST = "http://player.ooyala.com";
      BACKLOT_HOST = "http://cdn.api.ooyala.com";
      METADATA_HOST = "http://player.ooyala.com";
    }
  }

}