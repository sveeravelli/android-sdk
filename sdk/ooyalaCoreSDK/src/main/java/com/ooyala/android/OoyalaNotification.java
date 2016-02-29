package com.ooyala.android;

/**
 * Created by ukumar on 2/4/16.
 */
public class OoyalaNotification {

  public static final String OLD_STATE_KEY = "oldState";
  public static final String NEW_STATE_KEY = "newState";

  private final String name;
  private final Object data;

  /**
   * @param notificationName non-null string, one of the OoyalaPlayer.*_NOTIFICATION_NAMEs.
   */
  public OoyalaNotification(String notificationName) {
    this( notificationName, null );
  }

  /**
   * @param notificationName non-null string, one of the OoyalaPlayer.*_NOTIFICATION_NAMEs.
   * @param data possibly null notification-specific data.
   */
  public OoyalaNotification(String notificationName, Object data) {
    this.name = notificationName;
    this.data = data;
  }

  public String getName() {
    return name;
  }

  /**
   * @return possibly null.
   */
  public Object getData() {
    return data;
  }

  public String toString() {
    return "[" + getClass().getSimpleName() + "@" + hashCode() + ":n=" + name + ";d=" + (data==null?"<null>":data) + "]";
  }
}
