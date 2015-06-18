package io.telepat.sdk;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import io.telepat.sdk.data.TelepatInternalDB;
import io.telepat.sdk.data.TelepatSnappyDb;
import io.telepat.sdk.models.Channel;
import io.telepat.sdk.models.TelepatContext;
import io.telepat.sdk.models.OnChannelEventListener;
import io.telepat.sdk.networking.OctopusApi;
import io.telepat.sdk.networking.OctopusRequestInterceptor;
import io.telepat.sdk.networking.requests.RegisterDeviceRequest;
import io.telepat.sdk.networking.requests.RegisterUserRequest;
import io.telepat.sdk.networking.responses.RegisterDeviceResponse;
import io.telepat.sdk.networking.responses.UserLoginResponse;
import io.telepat.sdk.networking.transports.gcm.GcmRegistrar;
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
	private        HashMap<Integer, TelepatContext> mServerContexts;
	private OctopusApi apiClient;
	private OctopusRequestInterceptor requestInterceptor;
	private TelepatInternalDB internalDB;
	private HashMap<String, Channel> subscriptions = new HashMap<>();
	private String localUdid;

	private Telepat() {	}

	public static Telepat getInstance()
	{
		if (mInstance == null)	{
			mInstance = new Telepat();
		}

		return mInstance;
	}

	public TelepatInternalDB getDBInstance() {
		return internalDB;
	}

	public OctopusApi getAPIInstance() { return apiClient; }

	public void initialize(Context context,
						   final String clientApiKey,
						   final String clientAppId,
						   String senderId) {
		mContext = context.getApplicationContext();
		internalDB = new TelepatSnappyDb(context);
		TelepatConstants.GCM_SENDER_ID = senderId;
		initHTTPClient(clientApiKey, clientAppId);
		new GcmRegistrar(mContext).initGcmRegistration();
		updateContexts();
	}

	public void destroy() {
		internalDB.close();
	}

	private void initHTTPClient(String clientApiKey, final String clientAppId) {
		requestInterceptor = new OctopusRequestInterceptor(clientApiKey, clientAppId);

		RestAdapter.Builder rBuilder = new RestAdapter.Builder()
				.setEndpoint(TelepatConstants.TELEPAT_ENDPOINT)
				.setRequestInterceptor(requestInterceptor);
		if(TelepatConstants.RETROFIT_DEBUG_ENABLED)
			rBuilder.setLogLevel(RestAdapter.LogLevel.FULL)
					.setLog(new AndroidLog(TelepatConstants.TAG));

		RestAdapter restAdapter = rBuilder.build();
		apiClient = restAdapter.create(OctopusApi.class);
	}

	public void registerDevice(String regId, boolean shouldUpdateBackend)
	{
		String udid = (String) internalDB.getOperationsData(TelepatConstants.UDID_KEY,
															"",
															String.class);
		if(!udid.isEmpty() && !shouldUpdateBackend) return;

		if(udid.isEmpty()) {
			RegisterDeviceRequest request = new RegisterDeviceRequest(regId);
			apiClient.registerDevice(request.getParams(), new Callback<RegisterDeviceResponse>() {
				@Override
				public void success(RegisterDeviceResponse octopusResponse,
									retrofit.client.Response response) {
					TelepatLogger.log("Register device success");
					if (octopusResponse.status == 200 && octopusResponse.identifier != null) {
						requestInterceptor.setUdid(octopusResponse.identifier);
						internalDB.setOperationsData(TelepatConstants.UDID_KEY,
								octopusResponse.identifier);
						TelepatLogger.log("Received Telepat UDID: " + octopusResponse.identifier);
					}
				}

				@Override
				public void failure(RetrofitError error) {
					TelepatLogger.log("Register device failure.");

				}
			});
		} //else {
			//TODO send update
		//}
	}

	private void updateContexts()
	{
		apiClient.updateContexts(new Callback<Map<Integer, TelepatContext>>() {
			@Override
			public void success(Map<Integer, TelepatContext> contextMap,
								retrofit.client.Response response) {
				if(contextMap == null) return;
				TelepatLogger.log("Retrieved "+contextMap.keySet().size()+" contexts");
				if (mServerContexts == null) mServerContexts = new HashMap<>();
				for(Integer ctxId : contextMap.keySet())
					mServerContexts.put(ctxId, contextMap.get(ctxId));
			}

			@Override
			public void failure(RetrofitError error) {
				TelepatLogger.log("Failed to get contexts"+ error.getMessage());
			}
		});
	}

	public void login(final String fbToken)
	{
		internalDB.setOperationsData(TelepatConstants.FB_TOKEN_KEY, fbToken);
		apiClient.loginAsync(new RegisterUserRequest(fbToken).getParams(), new Callback<UserLoginResponse>() {
			@Override
			public void success(UserLoginResponse userLoginResponse, retrofit.client.Response response) {
				TelepatLogger.log("Received JWT token");
				internalDB.setOperationsData(TelepatConstants.JWT_KEY, userLoginResponse.token);
				internalDB.setOperationsData(TelepatConstants.JWT_TIMESTAMP_KEY, System.currentTimeMillis());
				requestInterceptor.setAuthorizationToken(userLoginResponse.token);
			}

			@Override
			public void failure(RetrofitError error) {
				TelepatLogger.error("user login failed");
			}
		});
	}

	public void logout()
	{
		apiClient.logout(new HashMap<String, String>(), new Callback<HashMap<String, Object>>() {
			@Override
			public void success(HashMap<String, Object> userLogoutResponse, retrofit.client.Response response) {
				TelepatLogger.log("Logout successful");
				requestInterceptor.setAuthorizationToken(null);
			}

			@Override
			public void failure(RetrofitError error) {
				TelepatLogger.error("user logout failed - "+error.getMessage());
			}
		});
	}

	public Channel subscribe(TelepatContext context, String modelName, OnChannelEventListener listener, Class type) {
		Channel channel = new Channel.Builder().
                setContext(context).
                setModelName(modelName).
                setChannelEventListener(listener).
                setObjectType(type).
                build();
//		subscriptions.put(channel.getSubscriptionIdentifier(), channel);
		channel.subscribe();
		return channel;
	}

	public Map<Integer, TelepatContext> getContexts() { return mServerContexts; }

    @SuppressWarnings("unused")
	public void removeSubscription(Channel mChannel) {
        mChannel.unsubscribe();
        subscriptions.remove(mChannel.getSubscriptionIdentifier());
    }

    public void registerSubscription(Channel mChannel) {
        subscriptions.put(mChannel.getSubscriptionIdentifier(), mChannel);
    }

	public Channel getSubscribedChannel(String channelIdentifier) {
		return subscriptions.get(channelIdentifier);
	}

	public String getDeviceLocalIdentifier() {
		if(localUdid!=null) return localUdid;
		String androidId = android.provider.Settings.System.getString(mContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		localUdid = (String) internalDB.getOperationsData(TelepatConstants.LOCAL_UDID_KEY, androidId, String.class);
		return localUdid;
	}

	public void setDeviceLocalIdentifier(String udid) {
		internalDB.setOperationsData(TelepatConstants.LOCAL_UDID_KEY, udid);
	}
}
