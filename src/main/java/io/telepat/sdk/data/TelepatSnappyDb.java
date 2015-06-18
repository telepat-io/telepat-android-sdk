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
//        this.mContext = mContext;
        try {
            snappyDb = DBFactory.open(mContext, DB_NAME);
//            objects = DBFactory.open(mContext, OBJECTS_DB);
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
            return snappyDb.exists(OBJECTS_PREFIX+channelIdentifier+":"+id);
        } catch (SnappydbException e) {
            return false;
        }
    }

    @Override
    public void persistObject(String channelIdentifier, int id, Object value) {
        String objectKey = OBJECTS_PREFIX + channelIdentifier+":"+id;
        setData(objectKey, value);
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
            return snappyDb.findKeys(OBJECTS_PREFIX+channelIdentifier);
        } catch (SnappydbException e) {
            return new String[0];
        }
    }

    @Override
    public void deleteChannelObjects(String channelIdentifier) {
        try {
            snappyDb.del(channelIdentifier);
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

    public void setData(String key, Object value) {
        try {
            snappyDb.put(key, value);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public Object getData(String key, Object defaultValue, Class type) {
        try {
            Object obj = snappyDb.getObject(key, type);
            if(obj == null) return defaultValue;
            return obj;
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    private List<Object> getObjectArray(String key, ArrayList<Object> defaultValue, Class type) {
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
}
