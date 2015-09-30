package io.telepat.sdk.utilities;

import android.util.Log;

/**
 * Created by Andrei Marinescu on 03.06.2015.
 * Logging facility
 */
public class TelepatLogger {
    public static void log(String message) {
        if(message == null) return;
        Log.d(TelepatConstants.TAG, message);
    }
    public static void error(String message) {
        if(message == null) return;
        Log.e(TelepatConstants.TAG, message);
    }
}
