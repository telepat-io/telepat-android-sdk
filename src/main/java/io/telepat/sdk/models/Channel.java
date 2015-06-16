package io.telepat.sdk.models;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

import io.telepat.sdk.Telepat;
import io.telepat.sdk.utilities.TelepatLogger;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Andrei Marinescu, catalinivan on 09/03/15.
 * Telepat Channel model
 */
public class Channel {
//	private HashMap<String, KrakenObject> mObjects;
	private String mModelName;
//	private ArrayList<String>       mFilters;
	private OnChannelEventListener mChannelEventListener;
	private TelepatContext mTelepatContext;
	private Class objectType;
	private Gson gson = new Gson();

	public static class Builder {
		private String mModelName;
		private TelepatContext mTelepatContext;
		private OnChannelEventListener mChannelEventListener;
		private Class objectType;

		public Builder setModelName(String modelName) { this.mModelName = modelName; return this; }
		public Builder setContext(TelepatContext context) { this.mTelepatContext = context; return this;}
		public Builder setChannelEventListener(OnChannelEventListener listener) {
			this.mChannelEventListener = listener;
			return this;
		}
		public Builder setObjectType(Class objectType) {
			this.objectType = objectType;
			return this;
		}
		public Channel build() {
			return new Channel(this);
		}
	}

	public Channel(Builder builder) {
		this.mModelName = builder.mModelName;
		this.mChannelEventListener = builder.mChannelEventListener;
		this.mTelepatContext = builder.mTelepatContext;
		this.objectType = builder.objectType;
	}

	public void subscribe() {
		Telepat.getInstance()
				.getAPIInstance()
				.subscribe(
						getSubscribingRequestBody(),
						new Callback<HashMap<String, JsonElement>>() {
							@Override
							public void success(HashMap<String, JsonElement> responseHashMap, Response response) {
								Integer status = Integer.parseInt(responseHashMap.get("status").toString());
								JsonElement message = responseHashMap.get("message");
								if(status == 200) {
									for (Map.Entry<String, JsonElement> entry : message.getAsJsonObject().entrySet()) {
										Channel.this.mChannelEventListener.
												onObjectAdded(gson.fromJson(entry.getValue(),
														Channel.this.objectType));
									}
								} else {
									Channel.this.mChannelEventListener.onError(status, message.toString());
								}
							}

							@Override
							public void failure(RetrofitError error) {
								TelepatLogger.log("Error subscribing: "+error.getMessage());
							}
						});
	}

	public HashMap<String, Object> getSubscribingRequestBody() {
		HashMap<String, Object> requestBody = new HashMap<>();
		HashMap<String, Object> channel = new HashMap<>();
		channel.put("context", mTelepatContext.getId());
		channel.put("model", mModelName);
		requestBody.put("channel", channel);
		return requestBody;
	}

	public HashMap<String, Object> getCreateRequestBody(Object object) {
		HashMap<String, Object> requestBody = new HashMap<>();
		requestBody.put("context", mTelepatContext.getId());
		requestBody.put("model", mModelName);
		requestBody.put("content", object);
		return requestBody;
	}

	public void unsubscribe() {
		Telepat.getInstance()
				.getAPIInstance()
				.unsubscribe(getSubscribingRequestBody(),
						new Callback<HashMap<Integer, String>>() {
							@Override
							public void success(HashMap<Integer, String> integerStringHashMap, Response response) {
								TelepatLogger.log("Unsubscribed");
							}

							@Override
							public void failure(RetrofitError error) {
								TelepatLogger.log("Unsubscribe failed: "+error.getMessage());
							}
						});
	}

	public void add(Object object) {
		Telepat.getInstance().getAPIInstance().create(getCreateRequestBody(object), new Callback<String>() {
			@Override
			public void success(String s, Response response) {
				TelepatLogger.log("Create successful: "+s);
			}

			@Override
			public void failure(RetrofitError error) {
				TelepatLogger.log("Create failed: "+error.getMessage());
			}
		});
	}

	@SuppressWarnings("unused")
	public void setOnChannelEventListener(OnChannelEventListener listener) {
		mChannelEventListener = listener;
	}


}
