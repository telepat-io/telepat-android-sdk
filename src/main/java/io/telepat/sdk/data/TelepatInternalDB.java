package io.telepat.sdk.data;

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
    TelepatBaseModel getObject(String channelIdentifier, int id, Class type);
    void persistObject(String channelIdentifier, TelepatBaseModel object);
    void persistObjects(String channelIdentifier, Object[] value);
    List<TelepatBaseModel> getChannelObjects(String channelIdentifier, Class type);
    void deleteChannelObjects(String channelIdentifier);
    void deleteObject(String channelIdentifier, TelepatBaseModel object);
    void empty();
    void close();
}
