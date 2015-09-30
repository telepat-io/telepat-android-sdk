package io.telepat.sdk.data;

import android.content.Context;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.telepat.sdk.Telepat;
import io.telepat.sdk.models.TelepatBaseModel;
import io.telepat.sdk.utilities.TelepatLogger;

/**
 * Created by Andrei Marinescu on 03.06.2015.
 * Internal DB provider over Shared Preferences
 */
public class TelepatSnappyDb implements TelepatInternalDB {

    /**
     * SnappyDB database name
     */
    private static  String      DB_NAME = Telepat.class.getSimpleName()+"_OPERATIONS";
    /**
     * Prefix for Telepat internal metadata
     */
    private static  String      OPERATIONS_PREFIX = "TP_OPERATIONS_";
    /**
     * Prefix for stored objects
     */
    private static  String      OBJECTS_PREFIX = "TP_OBJECTS_";

    /**
     * SnappyDB connection reference
     */
    private         DB          snappyDb;
    /**
     * Reference to the Application context (useful for opening / closing the DB)
     */
    private         Context     mContext;

    public TelepatSnappyDb(Context mContext) {
        this.mContext = mContext;
        try {
            snappyDb = DBFactory.open(mContext, DB_NAME);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    /**
     * Telepat internal metadata - sets a value on a specific key
     * @param key - the metadata key
     * @param value - the metadata value
     */
    @Override
    public void setOperationsData(String key, Object value) {
        setData(OPERATIONS_PREFIX+key, value);
    }

    /**
     * Telepat internal metadata - get a stored value
     * @param key the key the value is stored on
     * @param defaultValue the default value to return if the key does not exist
     * @param type the type the return value should be casted to
     * @return the metadata value
     */
    @Override
    public Object getOperationsData(String key, Object defaultValue, Class type) {
        return getData(OPERATIONS_PREFIX+key, defaultValue, type);
    }

    /**
     * Checks if an object exists in the internal DB
     * @param channelIdentifier the identifier of the channel the object is stored in
     * @param id the Telepat object ID
     * @return true if the object exists, false otherwise
     */
    @Override
    public boolean objectExists(String channelIdentifier, String id) {
        try {
            return snappyDb.exists(getObjectKey(channelIdentifier, id));
        } catch (SnappydbException e) {
            return false;
        }
    }

    /**
     * Retrieve a stored object
     * @param channelIdentifier the identifier of the channel the object is stored in
     * @param id the Telepat object ID
     * @param type the class the object should be casted to
     * @return the stored object
     */
    @Override
    public TelepatBaseModel getObject(String channelIdentifier, String id, Class type) {
        return (TelepatBaseModel)getData(getObjectKey(channelIdentifier, id), null, type);
    }

    /**
     * Save an objects to the internal DB
     * @param channelIdentifier the identifier of the channel the object is stored in
     * @param object the object to store
     */
    @Override
    public void persistObject(String channelIdentifier, TelepatBaseModel object) {
        String objectKey = getObjectKey(channelIdentifier, object.getId());
        setData(objectKey, object);
    }

    /**
     * Save an array of objects to the internal DB
     * @param channelIdentifier the identifier of the channel the object is stored in
     * @param objects an array of objects to store
     */
    @Override
    public void persistObjects(String channelIdentifier, TelepatBaseModel[] objects) {
        for(TelepatBaseModel object : objects) {
            persistObject(channelIdentifier, object);
        }
    }

    /**
     * Retrieve a list of all stored objects for a channel
     * @param channelIdentifier the identifier of the channel the object is stored in
     * @param type the class the objects should be casted to
     * @return
     */
    @Override
    public List<TelepatBaseModel> getChannelObjects(String channelIdentifier, Class type) {
        String[] keys = channelKeys(channelIdentifier);
        ArrayList<TelepatBaseModel> objects = new ArrayList<>();
        for(String key : keys) {
            try {
                objects.add((TelepatBaseModel)snappyDb.get(key, type));
            } catch (SnappydbException ignored) { }
        }
        Collections.sort(objects, new Comparator<TelepatBaseModel>() {
            @Override
            public int compare(TelepatBaseModel lhs, TelepatBaseModel rhs) {
                return (lhs.getId()).compareTo(rhs.getId());
            }
        });
        TelepatLogger.log("Retrieved "+channelIdentifier+ " objects. Size: "+objects.size());
        return objects;
    }

    /**
     * Get all the keys stored for a specific channel
     * @param channelIdentifier the identifier of the channel the object is stored in
     * @return An array of keys
     */
    public String[] channelKeys(String channelIdentifier) {
        try {
            return snappyDb.findKeys(getChannelPrefix(channelIdentifier));
        } catch (SnappydbException e) {
            return new String[0];
        }
    }

    /**
     * Delete all objects stored in a specific channel
     * @param channelIdentifier the identifier of the channel the object is stored in
     */
    @Override
    public void deleteChannelObjects(String channelIdentifier) {
        String[] keys = channelKeys(channelIdentifier);
        for(String key : keys) {
            try {
                snappyDb.del(key);
            } catch (SnappydbException ignore) {  }
        }
    }

    /**
     * Delete an object from the internal DB
     * @param channelIdentifier the identifier of the channel the object is stored in
     * @param object the object to delete
     */
    @Override
    public void deleteObject(String channelIdentifier, TelepatBaseModel object) {
        try {
            snappyDb.del(getObjectKey(channelIdentifier, object.getId()));
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    /**
     * Empty the internal DB
     */
    @Override
    public void empty() {
        try {
            snappyDb.destroy();
            snappyDb = DBFactory.open(mContext, DB_NAME);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save data to internal DB
     * @param key the key to store under
     * @param value the data to store
     */
    private void setData(String key, Object value) {
        try {
            snappyDb.put(key, value);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieve data from the internal DB
     * @param key the key the data is stored under
     * @param defaultValue the default value to return if the key does not exist
     * @param type the class the return value should be casted to
     * @return the stored data
     */
    private Object getData(String key, Object defaultValue, Class type) {
        try {
            Object obj = snappyDb.getObject(key, type);
            if(obj == null) return defaultValue;
            return obj;
        } catch (SnappydbException e) {
            if(!e.getMessage().startsWith("Failed to get a byte array: NotFound:"))
                e.printStackTrace();
            else
                TelepatLogger.log("Internal DB object with key "+key+" not found");
        }
        return defaultValue;
    }

    /**
     * Retrieve an array of objects from the internal DB
     * @param key the key the data is stored under
     * @param defaultValue the default value to return if they key does not exist
     * @param type the class the return value should be casted to
     * @return the array of stored objects
     */
    @SuppressWarnings("unused")
    private List<Object> getObjectArray(String key, ArrayList<Object> defaultValue, Class type) {
        //TODO refactor
        try {
            Object[] obj = snappyDb.getObjectArray(OBJECTS_PREFIX + key, type);
            if(obj == null) {
                return defaultValue;
            }
            return new ArrayList<>(Arrays.asList(obj));
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    /**
     * Close the connection with the internal DB
     */
    @Override
    public void close() {
        try {
            if(snappyDb != null) snappyDb.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    /**
     * Form the prefix for channel data
     * @param channelIdentifier the channel identifier
     * @return the prefix
     */
    private String getChannelPrefix(String channelIdentifier) {
        return OBJECTS_PREFIX + channelIdentifier;
    }

    /**
     * Form an object key
     * @param channelIdentifier the channel identifier
     * @param id the object ID
     * @return the object key
     */
    private String getObjectKey(String channelIdentifier, String id) {
        return getChannelPrefix(channelIdentifier)+":"+id;
    }
}
