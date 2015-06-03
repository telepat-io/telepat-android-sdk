package io.telepat.sdk.data;

import java.io.Serializable;

/**
 * Created by Andrei Marinescu on 03.06.2015.
 * Interface for DB providers
 */
public interface TelepatInternalDB {
    public void setOperationsData(String key, Object value);
    public Object getOperationsData(String key, Object defaultValue, Class type);
    public void close();
}
