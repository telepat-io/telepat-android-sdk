package io.telepat.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.android.volley.Response;
import io.android.volley.VolleyError;
import io.telepat.sdk.model.KrakenContext;
import io.telepat.sdk.model.KrakenUser;
import io.telepat.sdk.networking.OctopusApi;
import io.telepat.sdk.networking.OctopusRequestInterceptor;
import io.telepat.sdk.networking.VolleyWrapper;
import io.telepat.sdk.networking.requests.RegisterDeviceRequest;
import io.telepat.sdk.networking.requests.RegisterUserRequest;
import io.telepat.sdk.networking.responses.RegisterDeviceResponse;
import io.telepat.sdk.utilities.KrakenConstants;
import io.telepat.sdk.utilities.TelepatLogger;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidLog;

/**
 * Created by catalinivan on 10/03/15.
 *
 * Telepat Main Orchestrator
 */
public final class Telepat
{
	public static final  String PROPERTY_REG_ID      = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final String SENDER_ID            = "117236164056";
	private static final int    MAX_ATTEMPTS         = 3;
//	private static final int    PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	private static Telepat mInstance;
	private        Context                        mContext;
	private        HashMap<Integer, KrakenContext> mServerContexts;
	private        KrakenUser                     mCurrentUser;
	private OctopusApi apiClient;
	private OctopusRequestInterceptor requestInterceptor;

	private Telepat() {	}

	public static Telepat getInstance()
	{
		if (mInstance == null)
		{
			mInstance = new Telepat();
		}

		return mInstance;
	}

	public void initialize(Context context, final String clientApiKey, final String clientAppId)
	{
		mContext = context.getApplicationContext();
		requestInterceptor = new OctopusRequestInterceptor(clientApiKey, clientAppId);

		RestAdapter restAdapter = new RestAdapter.Builder()
				.setEndpoint(KrakenConstants.SERVER_URL)
				.setRequestInterceptor(requestInterceptor)
				.setLogLevel(RestAdapter.LogLevel.FULL).setLog(new AndroidLog(KrakenConstants.TAG))
				.build();
		apiClient = restAdapter.create(OctopusApi.class);

		initGcmRegistration();

		getServerContexts();
	}

	private void registerDevice(String regId)
	{
		RegisterDeviceRequest request = new RegisterDeviceRequest(regId);
		apiClient.registerDevice(request.getParams(), new Callback<RegisterDeviceResponse>() {
			@Override
			public void success(RegisterDeviceResponse octopusResponse, retrofit.client.Response response) {
				TelepatLogger.log("Register device success");
				if(octopusResponse.status == 200 && octopusResponse.identifier!=null) {
					requestInterceptor.setUdid(octopusResponse.identifier);
					TelepatLogger.log("Received Telepat UDID: "+octopusResponse.identifier);
				}
			}

			@Override
			public void failure(RetrofitError error) {
				TelepatLogger.log("Register device failure.");

			}
		});
	}

	private void getServerContexts()
	{
		apiClient.updateContexts(new Callback<Map<Integer, KrakenContext>>() {
			@Override
			public void success(Map<Integer, KrakenContext> contextMap, retrofit.client.Response response) {
				TelepatLogger.log("Retrieved contexts");
				if (mServerContexts == null) mServerContexts = new HashMap<>();
				for(Integer ctxId : contextMap.keySet())
					mServerContexts.put(ctxId, contextMap.get(ctxId));
			}

			@Override
			public void failure(RetrofitError error) {
				TelepatLogger.log("Failed req"+ error.getMessage());
			}
		});
	}

	private void initGcmRegistration()
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
				registerDevice(regId);
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

	private String getRegistrationId() {
		final SharedPreferences prefs = getGCMPreferences();
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(KrakenConstants.TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing registration ID is not guaranteed to work with
		// the new app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion();
		if (registeredVersion != currentVersion) {
			Log.i(KrakenConstants.TAG, "App version changed.");
			return "";
		}

		Log.d(KrakenConstants.TAG, "Registration id is: " + registrationId);
		return registrationId;
	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param regId registration ID
	 */
	private void storeRegistrationId(String regId)
	{
		final SharedPreferences prefs = getGCMPreferences();
		int appVersion = getAppVersion();
		Log.i(KrakenConstants.TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	private SharedPreferences getGCMPreferences()
	{
		return mContext.getSharedPreferences(Telepat.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private int getAppVersion()
	{
		try
		{
			PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
			return packageInfo.versionCode;
		}
		catch (NameNotFoundException e)
		{
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
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
						registerDevice(regId);

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

	public void registerUser(final String fbToken)
	{
		RegisterUserRequest request = new RegisterUserRequest(fbToken, new Response.Listener<KrakenUser>()
		{
			@Override
			public void onResponse(KrakenUser response)
			{
				//TODO: should we notify the client?
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				Log.e(KrakenConstants.TAG, error.getMessage());
			}
		});

		VolleyWrapper.getInstance(mContext).addRequest(request);
	}

	public void queuePatch()
	{

	}

	public class PatchDispatcher
	{
		private static final int DISPATCH_TIME_CAP  = 100;
		private static final int DISPATCH_COUNT_CAP = 10;

		private long mMostRecentTimestamp;
		private int  mPatchCounter;

		public void queuePatch()
		{
			long now = System.nanoTime();

			if (now - mMostRecentTimestamp > DISPATCH_TIME_CAP || mPatchCounter >= DISPATCH_COUNT_CAP)
			{
				//TODO: send the batch to the backend and reset the counter
				mPatchCounter = 0;
			}
			else
			{
				mMostRecentTimestamp = now;
				mPatchCounter++;
			}
		}
	}
}
