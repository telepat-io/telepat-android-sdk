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
    protected final transient PropertyChangeSupport telepatChangeMonitor = new PropertyChangeSupport(
            this);
    protected int id;
    protected String uuid;

    public TelepatBaseModel() {
//        this.addPropertyChangeListener(this);
    }

    public int getId() {
        return id;
    }

    protected void setId(int id) {
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

    public void setProperty(String propertyName, Object propertyValue) {
        Field property;
        try {
            property = this.getClass().getDeclaredField(propertyName);
            property.setAccessible(true);
            property.set(this, propertyValue);
        } catch (NoSuchFieldException ignore) { }
        catch (IllegalAccessException ignore) { }
    }

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

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if(event.getPropertyName().equals("id")) return;
        if(event.getOldValue() == null) { TelepatLogger.log("Property changed from null. Discarding"); }
        else {
            TelepatLogger.log("Property changed: "+event.getPropertyName()+". New value: "+event.getNewValue());
        }

    }

}
