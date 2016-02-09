package com.ooyala.android;

/**
 * Created by ukumar on 2/4/16.
 */
public class OoyalaNotification {

    private String notificationName;
    private Object data;


    public OoyalaNotification(String notificationName, Object data) {
        this.notificationName=notificationName;
        this.data=data;


    }
    public String getNotificationName() {
        return notificationName;
    }
    public Object getData() {
        return data;
    }

}
