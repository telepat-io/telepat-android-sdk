package io.telepat.sdk.models;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.telepat.sdk.Telepat;
import io.telepat.sdk.data.TelepatInternalDB;
import io.telepat.sdk.networking.CrudOperationsCallback;
import io.telepat.sdk.networking.OctopusApi;
import io.telepat.sdk.utilities.TelepatLogger;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Andrei Marinescu, catalinivan on 09/03/15.
 * Telepat Channel model
 */

public class Channel implements PropertyChangeListener {
	private String mModelName;
	private OnChannelEventListener mChannelEventListener;
	private TelepatContext mTelepatContext;
	private Class objectType;
	private Gson gson = new Gson();
	private HashMap<String, TelepatBaseModel> waitingForCreation = new HashMap<>();
	private TelepatInternalDB dbInstance;
	private OctopusApi apiInstance;

	public enum NotificationType {
		ObjectAdded,
		ObjectUpdated,
		ObjectDeleted
	}

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

	@SuppressWarnings("unused")
	public Channel(String identifier) {
		String[] identifierSegments = identifier.split("/:/");
		Integer contextId = Integer.parseInt(identifierSegments[1]);
		this.mTelepatContext = Telepat.getInstance().getContexts().get(contextId);
		this.mModelName = identifierSegments[2];
		this.objectType = Object.class;
		linkExternalDependencies();
	}

	public Channel(Builder builder) {
		this.mModelName = builder.mModelName;
		this.mChannelEventListener = builder.mChannelEventListener;
		this.mTelepatContext = builder.mTelepatContext;
		this.objectType = builder.objectType;
		this.dbInstance = Telepat.getInstance().getDBInstance();
		linkExternalDependencies();
//		this.notifyStoredObjects();
	}

	private void linkExternalDependencies() {
		this.dbInstance = Telepat.getInstance().getDBInstance();
		this.apiInstance = Telepat.getInstance().getAPIInstance();
	}

	public void subscribe() {
		Telepat.getInstance()
				.getAPIInstance()
				.subscribe(
						getSubscribingRequestBody(),
						new Callback<HashMap<String, JsonElement>>() {
							@Override
							public void success(
									HashMap<String, JsonElement> responseHashMap,
									Response response) {

								Integer status = Integer.parseInt(responseHashMap.get("status").toString());
								JsonElement message = responseHashMap.get("message");

								if(status == 200) {
									Telepat.getInstance().registerSubscription(Channel.this);

									for (Map.Entry<String, JsonElement> entry
											: message.getAsJsonObject().entrySet()) {
										processNotification(new TransportNotification(entry.getValue()));
									}

								} else {
									if(Channel.this.mChannelEventListener!=null)
										Channel.this.mChannelEventListener.onError(status, message.toString());
								}
							}

							@Override
							public void failure(RetrofitError error) {
								if(error.getMessage().startsWith("409")) {
									TelepatLogger.log("There is an already active subscription for this channel.");
								} else if(error.getMessage().startsWith("401")) {
									TelepatLogger.log("Not logged in.");
								} else {
									TelepatLogger.log("Error subscribing: " + error.getMessage());
								}
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

	public HashMap<String, Object> getCreateRequestBody(TelepatBaseModel object) {
		HashMap<String, Object> requestBody = new HashMap<>();
		requestBody.put("context", mTelepatContext.getId());
		requestBody.put("model", mModelName);
		requestBody.put("content", object);
		return requestBody;
	}

	public HashMap<String, Object> getUpdateRequestBody(PendingPatch pendingPatch) {
		HashMap<String, Object> body = new HashMap<>();
		body.put("model", this.mModelName);
		body.put("context", this.mTelepatContext.getId());
		body.put("id", pendingPatch.getObjectId());
		List<Map<String, Object>> pendingPatches = new ArrayList<>();
		pendingPatches.add(pendingPatch.toMap());

		body.put("patch", pendingPatches);
		return body;
	}

	public HashMap<String, Object> getDeleteRequestBody(TelepatBaseModel object) {
		HashMap<String, Object> body = new HashMap<>();
		body.put("model", this.mModelName);
		body.put("context", this.mTelepatContext.getId());
		body.put("id", object.getId());
		return body;
	}

	public void unsubscribe() {
		Telepat.getInstance()
				.getAPIInstance()
				.unsubscribe(getSubscribingRequestBody(),
						new Callback<HashMap<Integer, String>>() {
							@Override
							public void success(HashMap<Integer, String> integerStringHashMap, Response response) {
								TelepatLogger.log("Unsubscribed");
								dbInstance.
										deleteChannelObjects(Channel.this.getSubscriptionIdentifier());
							}

							@Override
							public void failure(RetrofitError error) {
								TelepatLogger.log("Unsubscribe failed: "+error.getMessage());
							}
						});
	}

	public String add(TelepatBaseModel object) {
		//TODO add a proper uuid
		object.setUuid(""+System.currentTimeMillis());
		waitingForCreation.put(object.getUuid(), object);
		apiInstance.create(getCreateRequestBody(object), new CrudOperationsCallback("Create"));
		return object.getUuid();
	}
	
	public void remove(TelepatBaseModel object) {
		apiInstance.delete(getDeleteRequestBody(object), new CrudOperationsCallback("Delete"));
	}

	@SuppressWarnings("unused")
	public void setOnChannelEventListener(OnChannelEventListener listener) {
		mChannelEventListener = listener;
	}

	public String getSubscriptionIdentifier() {
		/*
		 var key = 'blg:'+channel.context+':'+Application.loadedAppModels[appId][channel.model].namespace;

 if (channel.id) {
  key += ':'+channel.id;

  return key;
 }

 if (channel.user)
  key += ':users:'+user;
 if (channel.parent)
  key += ':'+channel.parent.model+':'+channel.parent.id;

 if (extraFilters)
  key += ':filters:'+(new Buffer(JSON.stringify(utils.parseQueryObject(extraFilters)))).toString('base64');

 return key;
}

		 */
		if(mTelepatContext == null || mModelName == null) return null;
		return "blg:"+mTelepatContext.getId()+':'+mModelName;
		//TODO add support for more channel params
	}

	public void notifyStoredObjects() {
		if(mChannelEventListener == null) return;
		for(TelepatBaseModel dataObject : Telepat.getInstance().
				getDBInstance().getChannelObjects(getSubscriptionIdentifier(), this.objectType)) {
			dataObject.addPropertyChangeListener(this);
			mChannelEventListener.onObjectAdded(dataObject);
		}
	}

	private void persistObject(TelepatBaseModel object) {
		dbInstance.
				persistObject(this.getSubscriptionIdentifier(),
						object
				);
	}

	public void processNotification(TransportNotification notification) {
		String[] pathSegments;
		String modelName;
		int objectId;

		switch (notification.getNotificationType()) {
			case ObjectAdded:
				TelepatBaseModel dataObject = (TelepatBaseModel) gson.fromJson(notification.getNotificationValue(), this.objectType);
				if(waitingForCreation.containsKey(dataObject.getUuid())) {
					waitingForCreation.get(dataObject.getUuid()).setId(dataObject.getId());
					waitingForCreation.get(dataObject.getUuid()).addPropertyChangeListener(this);
					if(mChannelEventListener != null) {
						mChannelEventListener.onObjectCreateSuccess(waitingForCreation.get(dataObject.getUuid()));
						waitingForCreation.remove(dataObject.getUuid());
					}
					persistObject(dataObject);
					return;
				}
				if(dbInstance.objectExists(getSubscriptionIdentifier(), dataObject.getId())) {
					return;
				}
				if(mChannelEventListener != null) {
					mChannelEventListener.onObjectAdded(dataObject);
				}
				persistObject(dataObject);
				break;
			case ObjectUpdated:
				TelepatLogger.log("Object updated: " +
						notification.getNotificationValue().toString() +
						" with path: " + notification.getNotificationPath().toString());

				if(!notification.hasValue()) {
					TelepatLogger.log("Notification object has no associated field value");
					return;
				}

				pathSegments = notification.getNotificationPath().getAsString().split("/");
				modelName = pathSegments[0];
				if(!modelName.equals(this.mModelName)) return;

				objectId = Integer.parseInt(pathSegments[1]);
				String propertyName = pathSegments[2];

				if(dbInstance.objectExists(this.getSubscriptionIdentifier(), objectId)) {
					TelepatBaseModel updatedObject = dbInstance.getObject(getSubscriptionIdentifier(),
																		  objectId,
																		  objectType);
					updatedObject.setProperty(propertyName,
											  notification.getNotificationValue().getAsString());

					if(mChannelEventListener != null) {
						mChannelEventListener.onObjectModified(updatedObject,
															   propertyName,
															   notification.getNotificationValue().getAsString());
					}
					dbInstance.persistObject(getSubscriptionIdentifier(), updatedObject);
				}
				break;
			case ObjectDeleted:
				TelepatLogger.log("Object deleted "+
						" with path: " + notification.getNotificationPath().toString());
				pathSegments = notification.getNotificationPath().getAsString().split("/");
				modelName = pathSegments[0];
				if(!modelName.equals(this.mModelName)) return;

				objectId = Integer.parseInt(pathSegments[1]);
				TelepatBaseModel deletedObject = null;
				if(dbInstance.objectExists(this.getSubscriptionIdentifier(), objectId)) {
					deletedObject = dbInstance.getObject(getSubscriptionIdentifier(),
														 objectId,
														 objectType);
					dbInstance.deleteObject(getSubscriptionIdentifier(), deletedObject);
				}
				if(mChannelEventListener != null) {
					mChannelEventListener.onObjectRemoved(deletedObject, objectId);
				}
				break;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		TelepatBaseModel obj = (TelepatBaseModel) event.getSource();
		TelepatLogger.log("Channel "+getSubscriptionIdentifier()+": Object with ID "+obj.getId()+" changed");
		PendingPatch patch = new PendingPatch(PendingPatch.PatchType.replace,
				this.mModelName+"/"+obj.getId()+"/"+event.getPropertyName(),
				event.getNewValue(),
				obj.getId());
		apiInstance.update(getUpdateRequestBody(patch), new CrudOperationsCallback("Update"));
	}

}
