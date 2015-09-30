package io.telepat.sdk;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import io.telepat.sdk.data.TelepatInternalDB;
import io.telepat.sdk.data.TelepatSnappyDb;
import io.telepat.sdk.models.Channel;
import io.telepat.sdk.models.OnChannelEventListener;
import io.telepat.sdk.models.TelepatContext;
import io.telepat.sdk.networking.OctopusApi;
import io.telepat.sdk.networking.OctopusRequestInterceptor;
import io.telepat.sdk.networking.requests.RegisterDeviceRequest;
import io.telepat.sdk.networking.requests.RegisterUserRequest;
import io.telepat.sdk.networking.responses.ContextsApiResponse;
import io.telepat.sdk.networking.responses.GenericApiResponse;
import io.telepat.sdk.networking.transports.gcm.GcmRegistrar;
import io.telepat.sdk.utilities.TelepatConstants;
import io.telepat.sdk.utilities.TelepatLogger;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidLog;
import retrofit.client.Response;

/**
 * Created by Andrei Marinescu, catalinivan on 10/03/15.
 *
 * Telepat Main Orchestrator
 */
public final class Telepat
{
	/**
	 * Telepat singleton instance
	 */
	private static Telepat mInstance;
	/**
	 * Reference to the application context
	 */
	private        Context                        mContext;
	/**
	 * References to the currently available Telepat contexts
	 */
	private        HashMap<String, TelepatContext> mServerContexts;
	/**
	 * Reference to a Telepat Sync API client
	 */
	private OctopusApi apiClient;
	/**
	 * Retrofit RequestInterceptor implementation for injecting the proper authentication headers
	 */
	private OctopusRequestInterceptor requestInterceptor;
	/**
	 * Internal storage reference
	 */
	private TelepatInternalDB internalDB;
	/**
	 * Locally registered Channel instances
	 */
	private HashMap<String, Channel> subscriptions = new HashMap<>();
	/**
	 * Unique device identifier
	 */
	private String localUdid;

	private Telepat() {	}

	/**
	 *
	 * @return Returns a reference to the singleton instance
	 */
	public static Telepat getInstance()
	{
		if (mInstance == null)	{
			mInstance = new Telepat();
		}

		return mInstance;
	}

	/**
	 * Get access to an instance controlling the internal storage DB
	 * @return An instance of a class implementing <code>TelepatInternalDB</code>
	 */
	public TelepatInternalDB getDBInstance() {
		return internalDB;
	}

    /**
     * Get access to an Retrofit instance that is able to communicate with the Telepat Sync API
     * @return An <code>OctopusApi</code> instance
     */
	public OctopusApi getAPIInstance() { return apiClient; }

	public void initialize(Context context,
						   final String telepatEndpoint,
						   final String clientApiKey,
						   final String clientAppId,
						   String senderId) {
		mContext = context.getApplicationContext();
		internalDB = new TelepatSnappyDb(context);
		TelepatConstants.GCM_SENDER_ID = senderId;
		initHTTPClient(telepatEndpoint, clientApiKey, clientAppId);
		new GcmRegistrar(mContext).initGcmRegistration();
		updateContexts();
	}

    /**
     * Close the current Telepat instance. You should reinitialize the Telepat SDK before doing
     * additional work.
     */
	public void destroy() {
		internalDB.close();
	}

    /**
     * Configures the OctopusApi instance with relevant credentials
     * @param clientApiKey A string containing a Telepat client API key
     * @param clientAppId A string containing the corresponding Telepat application ID
     */
	private void initHTTPClient(String telepatEndpoint, String clientApiKey, final String clientAppId) {
		requestInterceptor = new OctopusRequestInterceptor(clientApiKey, clientAppId);

		RestAdapter.Builder rBuilder = new RestAdapter.Builder()
				.setEndpoint(telepatEndpoint)
				.setRequestInterceptor(requestInterceptor);
		if(TelepatConstants.RETROFIT_DEBUG_ENABLED)
			rBuilder.setLogLevel(RestAdapter.LogLevel.FULL)
					.setLog(new AndroidLog(TelepatConstants.TAG));

		RestAdapter restAdapter = rBuilder.build();
		apiClient = restAdapter.create(OctopusApi.class);
	}

    /**
     * Send the Telepat Sync API a device registration request
     * @param regId A GCM token for the current device
     * @param shouldUpdateBackend If true, an update should be sent to the Telepat cloud instance
     *                            regardless of the state of the token (new/already sent)
     */
	public void registerDevice(String regId, boolean shouldUpdateBackend)
	{
		String udid = (String) internalDB.getOperationsData(TelepatConstants.UDID_KEY,
															"",
															String.class);
		if(!udid.isEmpty() && !shouldUpdateBackend) return;

		if(udid.isEmpty()) {
			RegisterDeviceRequest request = new RegisterDeviceRequest(regId);
			apiClient.registerDevice(request.getParams(), new Callback<GenericApiResponse>() {
				@Override
				public void success(GenericApiResponse octopusResponse,
									retrofit.client.Response response) {
					TelepatLogger.log("Register device success");
					if (octopusResponse.status == 200 && octopusResponse.content.get("identifier") != null) {
						requestInterceptor.setUdid((String)octopusResponse.content.get("identifier"));
						internalDB.setOperationsData(TelepatConstants.UDID_KEY,
								octopusResponse.content.get("identifier"));
						TelepatLogger.log("Received Telepat UDID: " + octopusResponse.content.get("identifier"));
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

    /**
     * Retrieve the currently active contexts for the current Telepat application
     */
	private void updateContexts()
	{
		apiClient.updateContexts(new Callback<ContextsApiResponse>() {
			@Override
			public void success(ContextsApiResponse contextMap,
								retrofit.client.Response response) {
				if (contextMap == null) return;
				if (mServerContexts == null) mServerContexts = new HashMap<>();
				for (TelepatContext ctx : contextMap.content)
					mServerContexts.put(ctx.getId(), ctx);
				TelepatLogger.log("Retrieved " + contextMap.content.size() + " contexts");
			}

			@Override
			public void failure(RetrofitError error) {
				TelepatLogger.log("Failed to get contexts" + error.getMessage());
			}
		});
	}

    /**
     * Send a Telepat Sync API call for logging in a user
     * @param fbToken A Facebook OAUTH token
     */
	public void register(final String fbToken)
	{
		internalDB.setOperationsData(TelepatConstants.FB_TOKEN_KEY, fbToken);
		apiClient.registerAsync(new RegisterUserRequest(fbToken).getParams(), new Callback<Map<String, String>>() {
			@Override
			public void success(Map<String, String> userRegisterResponse, retrofit.client.Response response) {
				TelepatLogger.log("User registered");
				apiClient.loginAsync(new RegisterUserRequest(fbToken).getParams(), new Callback<GenericApiResponse>() {
					@Override
					public void success(GenericApiResponse genericApiResponse, Response response) {
						internalDB.setOperationsData(TelepatConstants.JWT_KEY, genericApiResponse.content.get("token"));
						internalDB.setOperationsData(TelepatConstants.JWT_TIMESTAMP_KEY, System.currentTimeMillis());
						requestInterceptor.setAuthorizationToken((String) genericApiResponse.content.get("token"));
						TelepatLogger.log("User logged in");
					}

					@Override
					public void failure(RetrofitError error) {
						TelepatLogger.log("User login failed.");
					}
				});

			}

			@Override
			public void failure(RetrofitError error) {
				if(error.getResponse().getStatus()==409) {
					apiClient.loginAsync(new RegisterUserRequest(fbToken).getParams(), new Callback<GenericApiResponse>() {
						@Override
						public void success(GenericApiResponse genericApiResponse, Response response) {
							internalDB.setOperationsData(TelepatConstants.JWT_KEY, genericApiResponse.content.get("token"));
							internalDB.setOperationsData(TelepatConstants.JWT_TIMESTAMP_KEY, System.currentTimeMillis());
							requestInterceptor.setAuthorizationToken((String) genericApiResponse.content.get("token"));
							TelepatLogger.log("User logged in -- existing user");
						}

						@Override
						public void failure(RetrofitError error) {
							TelepatLogger.log("User login failed - "+error.getMessage());
						}
					});
				} else {
					TelepatLogger.error("user register failed");
				}
			}
		});
	}

    /**
     * Send a Telepat Sync API call for logging out the current user.
     */
	public void logout()
	{
		apiClient.logout(new Callback<HashMap<String, Object>>() {
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

    /**
     * Create a new subscription to a Telepat channel
     * @param context The context ID where the desired objects live in
     * @param modelName The model name of the desired objects
     * @param listener An object implementing OnChannelEventListener. All channel events will be sent
     *                 to this object.
     * @param type The desired Java class of the objects that will be emitted in this channel (should
     *             extend the TelepatBaseModel class)
     * @return a <code>Channel</code> object with the specified characteristics
     */
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

    /**
     * Get a Map of all curently active contexts for the Telepat Application
     * @return A Map instance containing TelepatContext objects stored by their ID
     */
	public Map<String, TelepatContext> getContexts() { return mServerContexts; }

    /**
     * Remove a locally registered subscription of a Telepat Channel object (this does not send any
     * notifications to the Telepat Sync API
     * @param mChannel The channel instance
     */
    @SuppressWarnings("unused")
	public void removeSubscription(Channel mChannel) {
        mChannel.unsubscribe();
        subscriptions.remove(mChannel.getSubscriptionIdentifier());
    }

    /**
     * Locally register an active subscription to a Telepat Channel with the Telepat SDK instance
     * (new channel objects register themselves automatically)
     * @param mChannel The channel object to be registered
     */
    public void registerSubscription(Channel mChannel) {
        subscriptions.put(mChannel.getSubscriptionIdentifier(), mChannel);
    }

    /**
     * Get the <code>Channel</code> instance of a locally registered channel.
     * @param channelIdentifier A properly formatted string of the channel identifier.
     * @return the <code>Channel</code> instance
     */
	public Channel getSubscribedChannel(String channelIdentifier) {
		return subscriptions.get(channelIdentifier);
	}

    /**
     * Get a unique device identifier. Used internally for detecting already registered devices
     * @return A String containing the UDID
     */
	public String getDeviceLocalIdentifier() {
		if(localUdid!=null) return localUdid;
		String androidId = android.provider.Settings.
                                    System.getString(mContext.getContentResolver(),
                                                     android.provider.Settings.Secure.ANDROID_ID);

		localUdid = (String) internalDB.getOperationsData(TelepatConstants.LOCAL_UDID_KEY,
                                                          androidId,
                                                          String.class);

		return localUdid;
	}

    /**
     * Set the unique device identifier sent to the Telepat cloud. This method should be used as
     * early as possible, before registering the device with the Sync API.
     * @param udid the desired UDID
     */
	public void setDeviceLocalIdentifier(String udid) {
		internalDB.setOperationsData(TelepatConstants.LOCAL_UDID_KEY, udid);
	}
}
