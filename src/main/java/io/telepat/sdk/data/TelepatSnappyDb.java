package io.telepat.sdk.data;

import android.content.Context;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.telepat.sdk.Telepat;
import io.telepat.sdk.models.TelepatBaseModel;
import io.telepat.sdk.utilities.TelepatLogger;

/**
 * Created by Andrei Marinescu on 03.06.2015.
 * Internal DB provider over Shared Preferences
 */
public class TelepatSnappyDb implements TelepatInternalDB {
    private static String DB_NAME = Telepat.class.getSimpleName()+"_OPERATIONS";
    private static String OPERATIONS_PREFIX = "TP_OPERATIONS_";
    private static String OBJECTS_PREFIX = "TP_OBJECTS_";
    private DB snappyDb;

    public TelepatSnappyDb(Context mContext) {
        try {
            snappyDb = DBFactory.open(mContext, DB_NAME);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setOperationsData(String key, Object value) {
        setData(OPERATIONS_PREFIX+key, value);
    }

    @Override
    public Object getOperationsData(String key, Object defaultValue, Class type) {
        return getData(OPERATIONS_PREFIX+key, defaultValue, type);
    }

    @Override
    public boolean objectExists(String channelIdentifier, int id) {
        try {
            return snappyDb.exists(getObjectKey(channelIdentifier, id));
        } catch (SnappydbException e) {
            return false;
        }
    }

    @Override
    public TelepatBaseModel getObject(String channelIdentifier, int id, Class type) {
        return (TelepatBaseModel)getData(getObjectKey(channelIdentifier, id), null, type);
    }

    @Override
    public void persistObject(String channelIdentifier, TelepatBaseModel object) {
        String objectKey = getObjectKey(channelIdentifier, object.getId());
        setData(objectKey, object);
    }

    @Override
    public void persistObjects(String channelIdentifier, Object[] value) {
        //setObjectData(channelIdentifier, value);
    }

    @Override
    public List<TelepatBaseModel> getChannelObjects(String channelIdentifier, Class type) {
        String[] keys = channelKeys(channelIdentifier);
        ArrayList<TelepatBaseModel> objects = new ArrayList<>();
        for(String key : keys) {
            try {
                objects.add((TelepatBaseModel)snappyDb.get(key, type));
            } catch (SnappydbException ignored) { }
        }
        TelepatLogger.log("Retrieved "+channelIdentifier+ " objects. Size: "+objects.size());
        return objects;
    }

    public String[] channelKeys(String channelIdentifier) {
        try {
            return snappyDb.findKeys(getChannelPrefix(channelIdentifier));
        } catch (SnappydbException e) {
            return new String[0];
        }
    }

    @Override
    public void deleteChannelObjects(String channelIdentifier) {
        String[] keys = channelKeys(channelIdentifier);
        for(String key : keys) {
            try {
                snappyDb.del(key);
            } catch (SnappydbException ignore) {  }
        }
    }

    @Override
    public void deleteObject(String channelIdentifier, TelepatBaseModel object) {
        try {
            snappyDb.del(getObjectKey(channelIdentifier, object.getId()));
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void empty() {
        try {
            snappyDb.destroy();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    private void setData(String key, Object value) {
        try {
            snappyDb.put(key, value);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    private Object getData(String key, Object defaultValue, Class type) {
        try {
            Object obj = snappyDb.getObject(key, type);
            if(obj == null) return defaultValue;
            return obj;
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

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

    @Override
    public void close() {
        try {
            if(snappyDb != null) snappyDb.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    private String getChannelPrefix(String channelIdentifier) {
        return OBJECTS_PREFIX + channelIdentifier;
    }

    private String getObjectKey(String channelIdentifier, int id) {
        return getChannelPrefix(channelIdentifier)+":"+id;
    }
}
