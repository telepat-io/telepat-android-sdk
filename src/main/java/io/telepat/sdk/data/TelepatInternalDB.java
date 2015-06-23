package io.telepat.sdk.data;

import java.util.List;

import io.telepat.sdk.models.TelepatBaseModel;

/**
 * Created by Andrei Marinescu on 03.06.2015.
 * Interface for DB providers
 */
public interface TelepatInternalDB {
    /**
     * Telepat internal metadata - sets a value on a specific key
     * @param key - the metadata key
     * @param value - the metadata value
     */
    void                    setOperationsData(String key, Object value);

    /**
     * Telepat internal metadata - get a stored value
     * @param key the key the value is stored on
     * @param defaultValue the default value to return if the key does not exist
     * @param type the type the return value should be casted to
     * @return the metadata value
     */
    Object                  getOperationsData(String key, Object defaultValue, Class type);

    /**
     * Checks if an object exists in the internal DB
     * @param channelIdentifier the identifier of the channel the object is stored in
     * @param id the Telepat object ID
     * @return true if the object exists, false otherwise
     */
    boolean                 objectExists(String channelIdentifier, int id);

    /**
     * Retrieve a stored object
     * @param channelIdentifier the identifier of the channel the object is stored in
     * @param id the Telepat object ID
     * @param type the class the object should be casted to
     * @return the stored object
     */
    TelepatBaseModel        getObject(String channelIdentifier, int id, Class type);

    /**
     * Retrieve a list of all stored objects for a channel
     * @param channelIdentifier the identifier of the channel the object is stored in
     * @param type the class the objects should be casted to
     * @return
     */
    List<TelepatBaseModel>  getChannelObjects(String channelIdentifier, Class type);

    /**
     * Save an objects to the internal DB
     * @param channelIdentifier the identifier of the channel the object is stored in
     * @param object the object to store
     */
    void                    persistObject(String channelIdentifier, TelepatBaseModel object);

    /**
     * Save an array of objects to the internal DB
     * @param channelIdentifier the identifier of the channel the object is stored in
     * @param objects an array of objects to store
     */
    void                    persistObjects(String channelIdentifier, TelepatBaseModel[] objects);

    /**
     * Delete an object from the internal DB
     * @param channelIdentifier the identifier of the channel the object is stored in
     * @param object the object to delete
     */
    void                    deleteObject(String channelIdentifier, TelepatBaseModel object);

    /**
     * Delete all objects stored in a specific channel
     * @param channelIdentifier the identifier of the channel the object is stored in
     */
    void                    deleteChannelObjects(String channelIdentifier);

    /**
     * Empty the internal DB
     */
    void                    empty();

    /**
     * Close the connection with the internal DB
     */
    void                    close();
}
