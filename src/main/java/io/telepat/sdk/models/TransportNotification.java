package io.telepat.sdk.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Created by Andrei on 19.06.2015.
 * Container for transmitting notifications received from different transport types to
 * the corresponding channel.
 */
public class TransportNotification {
    /**
     * Notification type based on the <code>Channel.NotificationType</code> enum
     */
    Channel.NotificationType notificationType;

    /**
     * The value the notification adds or changes
     */
    JsonElement notificationValue;

    /**
     * The object path the notification affects
     */
    JsonElement notificationPath;

    public  TransportNotification(JsonObject notificationObject, Channel.NotificationType notificationType) {
        if(notificationObject.get("value") != null)
            notificationValue = notificationObject.get("value");
        else if(notificationObject.get("object")!=null) {
            notificationValue = notificationObject.get("object");
        } else if(notificationObject.has("patch") && notificationObject.get("patch").isJsonObject()) {
            JsonObject updatePatch = notificationObject.getAsJsonObject("patch");
            notificationValue = updatePatch.get("value");
        }
        if(notificationObject.get("path") != null)
            notificationPath = notificationObject.get("path");
        else if(notificationObject.has("patch") && notificationObject.get("patch").isJsonObject()) {
            JsonObject updatePatch = notificationObject.getAsJsonObject("patch");
            notificationPath = updatePatch.get("path");
        }
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

    /**
     *
     * @return Returns true if the notificationValue field is not null
     */
    public boolean hasValue() {
        return notificationValue != null;
    }

    public JsonElement getNotificationPath() {
        return notificationPath;
    }

    public void setNotificationPath(JsonElement notificationPath) {
        this.notificationPath = notificationPath;
    }

    /**
     *
     * @return returns ture if the notificationPath field is not null
     */
    public boolean hasPath() {
        return notificationPath != null;
    }
}
