package io.telepat.sdk.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Created by Andrei on 19.06.2015.
 * Container for transmitting notifications received from different transport types to
 * the corresponding channel.
 */
public class TransportNotification {
    Channel.NotificationType notificationType;
    JsonElement notificationValue;
    JsonElement notificationPath;

    public TransportNotification(JsonObject notificationObject, Channel.NotificationType notificationType) {
        if(notificationObject.get("value") != null)
            notificationValue = notificationObject.get("value");
        if(notificationObject.get("path") != null)
            notificationPath = notificationObject.get("path");
        this.notificationType = notificationType;
    }

    public TransportNotification(JsonElement createdObject) {
        this.notificationType = Channel.NotificationType.ObjectAdded;
        this.notificationValue = createdObject;
    }

    public Channel.NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(Channel.NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public JsonElement getNotificationValue() {
        return notificationValue;
    }

    public void setNotificationValue(JsonElement notificationValue) {
        this.notificationValue = notificationValue;
    }

    public boolean hasValue() {
        return notificationValue != null;
    }

    public JsonElement getNotificationPath() {
        return notificationPath;
    }

    public void setNotificationPath(JsonElement notificationPath) {
        this.notificationPath = notificationPath;
    }

    public boolean hasPath() {
        return notificationPath != null;
    }
}
