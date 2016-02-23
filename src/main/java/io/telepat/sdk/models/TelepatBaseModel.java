package io.telepat.sdk.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.lang.reflect.Field;

import io.telepat.sdk.utilities.TelepatLogger;

/**
 * Created by Andrei Marinescu on 17.06.2015.
 * Parent for all model classes used with the Telepat SDK.
 */
public class TelepatBaseModel implements PropertyChangeListener, Serializable {
    /**
     * Monitors the object for changes to be notified to the Telepat cloud
     */
    protected transient PropertyChangeSupport telepatChangeMonitor = new PropertyChangeSupport(
            this);

    /**
     * The Telepat object ID
     */
    protected String id;

    /**
     * Object creation UUID
     */
    protected String uuid;

    public TelepatBaseModel() {
//        this.addPropertyChangeListener(this);
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        telepatChangeMonitor.firePropertyChange("id", this.id, id);
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if(telepatChangeMonitor == null) {
            telepatChangeMonitor = new PropertyChangeSupport(this);
        }
        telepatChangeMonitor.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        telepatChangeMonitor.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName,
                                          PropertyChangeListener listener) {
        telepatChangeMonitor.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName,
                                             PropertyChangeListener listener) {
        telepatChangeMonitor.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Set a value of an object's field
     * @param propertyName the field name
     * @param propertyValue the desired value
     */
    public void setProperty(String propertyName, Object propertyValue) {
        Field property;
        try {
            property = this.getClass().getDeclaredField(propertyName);
            property.setAccessible(true);
            property.set(this, propertyValue);
        } catch (NoSuchFieldException ignore) { }
        catch (IllegalAccessException ignore) { }
    }

    /**
     * Retrieves the value of an object's field
     * @param propertyName the name of the field
     * @return the value of the specified field
     */
    public Object getProperty(String propertyName) {
        Field property;
        try {
            property = this.getClass().getDeclaredField(propertyName);
            property.setAccessible(true);
            return property.get(this);
        } catch (NoSuchFieldException ignore) { }
        catch (IllegalAccessException ignore) { }
        return null;
    }

    public Class getPropertyType(String propertyName) {
        Field property;
        try {
            property = this.getClass().getDeclaredField(propertyName);
            return property.getType();
        } catch (NoSuchFieldException ignore) { }
        return null;
    }

    /**
     * Listener for object field value changes
     * @param event
     */
    @SuppressWarnings("JavaDoc")
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if(event.getPropertyName().equals("id")) return;
        if(event.getOldValue() == null) { TelepatLogger.log("Property changed from null. Discarding"); }
        else {
            TelepatLogger.log("Property changed: "+event.getPropertyName()+". New value: "+event.getNewValue());
        }

    }

}
