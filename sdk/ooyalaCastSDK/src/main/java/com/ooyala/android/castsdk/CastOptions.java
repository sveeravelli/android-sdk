package com.ooyala.android.castsdk;

import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;

/**
 * Created by zchen on 10/13/15.
 */
public class CastOptions {
  /**
   * Supports a fluid syntax for configuration.
   */
  public static class Builder {

    private String applicationId;
    private String nameSpace;
    private Class<?> targetActivity;
    private boolean enableLockScreen;
    private boolean enableNotification;
    private boolean enableDebug;

    /**
     * The constructor
     * @param applicationId - The unique receiver application Id, cannot be null.
     * @param nameSpace - The namespace, cannot be null
     */
    public Builder(String applicationId, String nameSpace) {
      if (applicationId == null || nameSpace == null) {
        throw new IllegalArgumentException("applicationId and namespace cannot be null");
      }
      this.applicationId = applicationId;
      this.nameSpace = nameSpace;
      this.targetActivity = null;
      this.enableDebug = false;
      this.enableLockScreen = true;
      this.enableNotification = true;
    }

    /**
     * Set the target activity.
     * @param targetActivity - The target activity to be launched from notification.
     * The default value is VideoCast
     */
    public Builder setTargetActivity(Class<?> targetActivity) {
      this.targetActivity = targetActivity;
      return this;
    }

    /**
     * Set the target activity.
     * @param enable - true to enable, false to disable.
     * The default value is 0 (never times out).
     */
    public Builder setEnableLockScreen(boolean enable) {
      this.enableLockScreen = enable;
      return this;
    }

    /**
     * Set the target activity.
     * @param enable - true to enable, false to disable.
     * The default value is 0 (never times out).
     */
    public Builder setEnableNotification(boolean enable) {
      this.enableNotification = enable;
      return this;
    }

    /**
     * set enable debug.
     * @param enable - true to enable, false to disable.
     * The default value is 0 (never times out).
     */
    public Builder setEnableDebug(boolean enable) {
      this.enableDebug = enable;
      return this;
    }

    public CastOptions build() {
      return new CastOptions(applicationId, nameSpace, targetActivity, enableLockScreen, enableNotification, enableDebug);
    }
  }

  private final String applicationId;
  private String nameSpace;
  private final Class<?> targetActivity;
  private final Integer enabledFeatures;

  private CastOptions(
      String _appId,
      String _nameSpace,
      Class<?> _targetActivity,
      boolean enableLockScreen,
      boolean enableNotification,
      boolean enableDebug) {


    this.applicationId = _appId;
    this.nameSpace = _nameSpace;
    this.targetActivity = _targetActivity;
    Integer features =
        VideoCastManager.FEATURE_WIFI_RECONNECT |
        VideoCastManager.FEATURE_AUTO_RECONNECT |
        VideoCastManager.FEATURE_CAPTIONS_PREFERENCE;

    if (enableDebug) {
      features |= VideoCastManager.FEATURE_DEBUGGING;
    }

    if (enableLockScreen) {
      features |= VideoCastManager.FEATURE_LOCKSCREEN;
    }

    if (enableNotification) {
      features |= VideoCastManager.FEATURE_NOTIFICATION;
    }
    this.enabledFeatures = features;
  }

  public String getApplicationId() {
    return this.applicationId;
  }

  public String getNameSpace() {
    return this.nameSpace;
  }

  public Class<?> getTargetActivity() {
    return this.targetActivity;
  }

  public boolean isDebugEnabled() {
    return (this.enabledFeatures & VideoCastManager.FEATURE_DEBUGGING) != 0;
  }

  public boolean isNotificationEnabled() {
    return (this.enabledFeatures & VideoCastManager.FEATURE_NOTIFICATION) != 0;
  }

  public boolean isLockScreenEnabled() {
    return (this.enabledFeatures & VideoCastManager.FEATURE_LOCKSCREEN) != 0;
  }

  int enabledFeatures() {
    return this.enabledFeatures;
  }
}
