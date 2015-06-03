package io.telepat.sdk.networking.transports.gcm;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.Random;

import io.telepat.sdk.Telepat;
import io.telepat.sdk.utilities.TelepatConstants;
import io.telepat.sdk.utilities.TelepatUtilities;

/**
 * Created by Andrei Marinescu on 03.06.2015.
 * GCM Token Registration
 */
public class GcmRegistrator {
    public static final  String PROPERTY_REG_ID      = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String SENDER_ID            = "117236164056";
    private static final int    MAX_ATTEMPTS         = 3;
    private Context mContext;

    public GcmRegistrator(Context mContext) {
        this.mContext = mContext;
    }

    private void gcmRegisterAsync()
    {
        new AsyncTask<Object, Object, Object>()
        {
            @Override
            protected Object doInBackground(Object[] params)
            {
                long backoff = 2000 + new Random().nextInt(1000);
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(mContext);
                for (int i = 0; i < MAX_ATTEMPTS; i++)
                {
                    try
                    {
                        String regId = gcm.register(SENDER_ID);
                        Log.d("GCM", regId);

                        storeRegistrationId(regId);
                        Telepat.getInstance().registerDevice(regId);

                        return regId;
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        Log.e("GCM", e.getMessage());

                        if (i == MAX_ATTEMPTS - 1)
                        {
                            break;
                        }

                        try
                        {
                            Log.d("GCM", "Sleeping for " + backoff + " ms before retry");
                            Thread.sleep(backoff);
                        }
                        catch (InterruptedException e1)
                        {
                            // Activity finished before we complete - exit.
                            Log.d("GCM", "Thread interrupted: abort remaining retries!");
                            Thread.currentThread().interrupt();
                            return "";
                        }
                        // increase backoff exponentially
                        backoff *= 2;
                    }
                }

                return null;
            }
        }.execute(null, null, null);
    }

    private String getRegistrationId() {
        String registrationId = (String) Telepat.getInstance().getDBInstance().getOperationsData(PROPERTY_REG_ID, "", String.class);
        if (registrationId.isEmpty()) {
            Log.i(TelepatConstants.TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = (int) Telepat.getInstance().getDBInstance().getOperationsData(PROPERTY_APP_VERSION, Integer.MIN_VALUE, Integer.class);
        int currentVersion = TelepatUtilities.getAppVersion(mContext);
        if (registeredVersion != currentVersion) {
            Log.i(TelepatConstants.TAG, "App version changed.");
            return "";
        }

        Log.d(TelepatConstants.TAG, "Registration id is: " + registrationId);
        return registrationId;
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     *
     * @param regId registration ID
     */
    private void storeRegistrationId(String regId)
    {
        int appVersion = TelepatUtilities.getAppVersion(mContext);
        Log.i(TelepatConstants.TAG, "Saving regId on app version " + appVersion);
        Telepat.getInstance().getDBInstance().setOperationsData(PROPERTY_REG_ID, regId);
        Telepat.getInstance().getDBInstance().setOperationsData(PROPERTY_APP_VERSION, appVersion);
    }

    public void initGcmRegistration()
    {
        if (checkPlayServices())
        {
            String regId = getRegistrationId();

            if (TextUtils.isEmpty(regId))
            {
                gcmRegisterAsync();
            }
            else
            {
                Telepat.getInstance().registerDevice(regId);
            }
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext.getApplicationContext());
        return resultCode == ConnectionResult.SUCCESS;
    }
}
