package io.telepat.sdk;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import io.android.volley.Response;
import io.android.volley.VolleyError;
import io.telepat.sdk.models.KrakenContext;
import io.telepat.sdk.models.KrakenUser;
import io.telepat.sdk.networking.OctopusApi;
import io.telepat.sdk.networking.OctopusRequestInterceptor;
import io.telepat.sdk.networking.VolleyWrapper;
import io.telepat.sdk.networking.requests.RegisterDeviceRequest;
import io.telepat.sdk.networking.requests.RegisterUserRequest;
import io.telepat.sdk.networking.responses.RegisterDeviceResponse;
import io.telepat.sdk.networking.transports.gcm.GcmRegistrator;
import io.telepat.sdk.utilities.TelepatConstants;
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
		initHTTPClient(clientApiKey, clientAppId);
		new GcmRegistrator(mContext).initGcmRegistration();
		getServerContexts();
	}

	private void initHTTPClient(String clientApiKey, final String clientAppId) {
		requestInterceptor = new OctopusRequestInterceptor(clientApiKey, clientAppId);

		RestAdapter.Builder rBuilder = new RestAdapter.Builder()
				.setEndpoint(TelepatConstants.SERVER_URL)
				.setRequestInterceptor(requestInterceptor);
		if(TelepatConstants.RETROFIT_DEBUG_ENABLED)
			rBuilder.setLogLevel(RestAdapter.LogLevel.FULL).setLog(new AndroidLog(TelepatConstants.TAG));

		RestAdapter restAdapter = rBuilder.build();
		apiClient = restAdapter.create(OctopusApi.class);
	}

	public void registerDevice(String regId)
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
				if(contextMap == null) return;
				TelepatLogger.log("Retrieved"+contextMap.keySet().size()+"contexts");
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
				TelepatLogger.error(error.getMessage());
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
