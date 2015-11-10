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

	/**
	 * Possible notification message types arriving from the Telepat cloud
	 */
	public enum NotificationType {
		ObjectAdded,
		ObjectUpdated,
		ObjectDeleted
	}

	/**
	 * Builder pattern implementation for the Channel class
	 */
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

	public Channel(String identifier) {
		String[] identifierSegments = identifier.split(":");
		String contextId = identifierSegments[1];
		this.mTelepatContext = Telepat.getInstance().getContexts().get(contextId);
		this.mModelName = identifierSegments[2];
		this.objectType = TelepatBaseModel.class;
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

	/**
	 * Sets the internal DB and OctopusAPI references so that Channel objects are able to
	 * persist data in the internal DB as well as send out Sync API requests.
	 */
	private void linkExternalDependencies() {
		this.dbInstance = Telepat.getInstance().getDBInstance();
		this.apiInstance = Telepat.getInstance().getAPIInstance();
	}

	/**
	 * Create a new subscription with the Telepat Cloud instance.
	 * If the device is already registered, the stored objects will be notified again.
	 */
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
								JsonElement message = responseHashMap.get("content");

								if(status == 200) {
									Telepat.getInstance().registerSubscription(Channel.this);

									for (JsonElement entry
											: message.getAsJsonArray()) {
										processNotification(new TransportNotification(entry));
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

	/**
	 *
	 * @return Get a HashMap containing the relevant POST field parameters for a
	 * Subscribe Sync API request
	 */
	public HashMap<String, Object> getSubscribingRequestBody() {
		HashMap<String, Object> requestBody = new HashMap<>();
		HashMap<String, Object> channel = new HashMap<>();
		channel.put("context", mTelepatContext.getId());
		channel.put("model", mModelName);
		requestBody.put("channel", channel);
		return requestBody;
	}

	/**
	 *
	 * @return Get a HashMap containing the relevant POST field parameters for an
	 * item creating request
	 */
	public HashMap<String, Object> getCreateRequestBody(TelepatBaseModel object) {
		HashMap<String, Object> requestBody = new HashMap<>();
		requestBody.put("context", mTelepatContext.getId());
		requestBody.put("model", mModelName);
		requestBody.put("content", object);
		return requestBody;
	}

	/**
	 *
	 * @return Get a HashMap containing the relevant POST field parameters for an
	 * item update request
	 */
	public HashMap<String, Object> getUpdateRequestBody(PendingPatch pendingPatch) {
		HashMap<String, Object> body = new HashMap<>();
		body.put("model", this.mModelName);
		body.put("context", this.mTelepatContext.getId());
		body.put("id", pendingPatch.getObjectId());
		List<Map<String, Object>> pendingPatches = new ArrayList<>();
		pendingPatches.add(pendingPatch.toMap());

		body.put("patches", pendingPatches);
		return body;
	}

	/**
	 *
	 * @return Get a HashMap containing the relevant POST field parameters for an
	 * item delete request
	 */
	public HashMap<String, Object> getDeleteRequestBody(TelepatBaseModel object) {
		HashMap<String, Object> body = new HashMap<>();
		body.put("model", this.mModelName);
		body.put("context", this.mTelepatContext.getId());
		body.put("id", object.getId());
		return body;
	}

	/**
	 * Unsubscribes this device from receiving further updates from this channel
	 */
	public void unsubscribe() {
		Telepat.getInstance()
				.getAPIInstance()
				.unsubscribe(getSubscribingRequestBody(),
						new Callback<HashMap<String, String>>() {
							@Override
							public void success(HashMap<String, String> integerStringHashMap, Response response) {
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

	/**
	 * Create a new object on this Telepat channel
	 * @param object an object of a class extending <code>TelepatBaseModel</code>
	 * @return An UUID used for detecting the creation success
	 */
	public String add(TelepatBaseModel object) {
		//TODO add a proper uuid
		object.setUuid(""+System.currentTimeMillis());
		waitingForCreation.put(object.getUuid(), object);
		apiInstance.create(getCreateRequestBody(object), new CrudOperationsCallback("Create"));
		return object.getUuid();
	}

	/**
	 * Deletes an object from this Telepat channel
	 * @param object an object of a class extending <code>TelepatBaseModel</code>
	 */
	public void remove(TelepatBaseModel object) {
		apiInstance.delete(getDeleteRequestBody(object), new CrudOperationsCallback("Delete"));
	}

	/**
	 * Set the listener object where events of this channel will be notified
	 * @param listener an object of a class implementing <code>OnChannelEventListener</code>
	 */
	@SuppressWarnings("unused")
	public void setOnChannelEventListener(OnChannelEventListener listener) {
		mChannelEventListener = listener;
	}

	/**
	 * Get a string representation of this channels characteristics
	 * @return
	 */
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
		return "blg:"+Telepat.getInstance().getAppId()+":context:"+mTelepatContext.getId()+':'+mModelName;
		//TODO add support for more channel params
	}

	/**
	 * Send created notifications for currently locally stored objects in this channel.
	 */
	public void notifyStoredObjects() {
		if(mChannelEventListener == null) return;
		for(TelepatBaseModel dataObject : Telepat.getInstance().
				getDBInstance().getChannelObjects(getSubscriptionIdentifier(), this.objectType)) {
			dataObject.addPropertyChangeListener(this);
			mChannelEventListener.onObjectAdded(dataObject);
		}
	}

	/**
	 * Save an object to the internal DB
	 * @param object An object of a class extending TelepatBaseModel
	 */
	private void persistObject(TelepatBaseModel object) {
		dbInstance.
				persistObject(this.getSubscriptionIdentifier(),
						object
				);
	}

	/**
	 * Process an incoming notification from a network transport
	 * @param notification the received notification
	 */
	public void processNotification(TransportNotification notification) {
		String[] pathSegments;
		String modelName;
		String objectId;

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

				objectId = pathSegments[1];
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

				objectId = pathSegments[1];
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

	/**
	 * Listener for modified objects (previously emitted by this channel)
	 * @param event the property change event
	 */
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
