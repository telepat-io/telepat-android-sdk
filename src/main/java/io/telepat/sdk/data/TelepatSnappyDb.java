package io.telepat.sdk.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.io.Serializable;

import io.telepat.sdk.Telepat;
import io.telepat.sdk.utilities.TelepatConstants;
import io.telepat.sdk.utilities.TelepatUtilities;

/**
 * Created by Andrei Marinescu on 03.06.2015.
 * Internal DB provider over Shared Preferences
 */
public class TelepatSnappyDb implements TelepatInternalDB {
    private static String OPERATIONS_DB = Telepat.class.getSimpleName()+"_OPERATIONS";
    private static String OBJECTS_DB = Telepat.class.getSimpleName()+"_OBJECTS";
    private DB operations;
    private DB objects;
    private Context mContext;

    public TelepatSnappyDb(Context mContext) {
        this.mContext = mContext;
        try {
            operations = DBFactory.open(mContext, OPERATIONS_DB);
            objects = DBFactory.open(mContext, OBJECTS_DB);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setOperationsData(String key, Object value) {
        try {
            operations.put(key, value);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getOperationsData(String key, Object defaultValue, Class type) {
        try {
            Object obj = operations.getObject(key, type);
            if(obj == null) return defaultValue;
            return obj;
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    @Override
    public void close() {
        try {
            if(operations != null) operations.close();
            if(objects != null) objects.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }
}
