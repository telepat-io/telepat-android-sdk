package io.telepat.sdk.utilities;

import android.util.Log;

/**
 * Created by Andrei on 03.06.2015.
 */
public class TelepatLogger {
    public static void log(String message) {
        Log.d(KrakenConstants.TAG, message);
    }
}
