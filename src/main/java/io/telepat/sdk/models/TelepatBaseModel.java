package io.telepat.sdk.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import io.telepat.sdk.utilities.TelepatLogger;

/**
 * Created by Andrei on 17.06.2015.
 */
public class TelepatBaseModel implements PropertyChangeListener, Serializable {
    protected final transient PropertyChangeSupport telepatChangeMonitor = new PropertyChangeSupport(
            this);
    protected int id;
    protected String uuid;

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        telepatChangeMonitor.firePropertyChange("id", this.id, id);
        this.id = id;
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
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if(event.getPropertyName().equals("id")) return;
        if(event.getOldValue() == null) { TelepatLogger.log("Property changed from null. Discarding"); }
        else {
            TelepatLogger.log("Property changed: "+event.getPropertyName()+". New value: "+event.getNewValue());
        }

    }

    public TelepatBaseModel() {
        this.addPropertyChangeListener(this);
    }


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
