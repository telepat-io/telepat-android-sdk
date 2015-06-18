package io.telepat.sdk.data;

import java.io.Serializable;
import java.util.List;

import io.telepat.sdk.models.TelepatBaseModel;

/**
 * Created by Andrei Marinescu on 03.06.2015.
 * Interface for DB providers
 */
public interface TelepatInternalDB {
    void setOperationsData(String key, Object value);
    Object getOperationsData(String key, Object defaultValue, Class type);
    boolean objectExists(String channelIdentifier, int id);
    void persistObject(String channelIdentifier, int id, Object value);
    void persistObjects(String channelIdentifier, Object[] value);
    List<TelepatBaseModel> getChannelObjects(String channelIdentifier, Class type);
    void deleteChannelObjects(String channelIdentifier);
    void empty();
    void close();
}
