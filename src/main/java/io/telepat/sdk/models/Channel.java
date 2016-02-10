package io.telepat.sdk.models;

import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.telepat.sdk.Telepat;
import io.telepat.sdk.data.TelepatInternalDB;
import io.telepat.sdk.networking.CrudOperationsCallback;
import io.telepat.sdk.networking.OctopusApi;
import io.telepat.sdk.networking.responses.GenericApiResponse;
import io.telepat.sdk.networking.responses.TelepatCountCallback;
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
	private String mUserId;
	private String mParentModelName;
	private String mParentId;
	private String mSingleObjectId;
	private HashMap<String, Object> mFilters;
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

	public enum AggregationType {
		AverageAggregation,
		SumAggregation,
		None
	}

	/**
	 * Builder pattern implementation for the Channel class
	 */
	public static class Builder {
		private String mModelName, mUserId, mParentModelName, mParentId, mSingleObjectId;
		private HashMap<String, Object> mFilters;
		private TelepatContext mTelepatContext;
		private OnChannelEventListener mChannelEventListener;
		private Class objectType;

		public Builder setModelName(String modelName) { this.mModelName = modelName; return this; }
		public Builder setContext(TelepatContext context) { this.mTelepatContext = context; return this;}
		public Builder setUserFilter(String userId) { this.mUserId = userId; return this;}
		public Builder setParentFilter(String parentModelName, String parentId) { this.mParentModelName = parentModelName; this.mParentId = parentId; return this;}
		public Builder setSingleObjectIdFilter(String singleObjectId) {this.mSingleObjectId = singleObjectId; return this; }
		public Builder setFilters(HashMap<String, Object> filters) { this.mFilters = filters; return this;}
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
		if(Telepat.getInstance().getContexts()==null) return;
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
		this.mFilters = builder.mFilters;
		this.mUserId = builder.mUserId;
		this.mParentModelName = builder.mParentModelName;
		this.mParentId = builder.mParentId;
		this.mSingleObjectId = builder.mSingleObjectId;
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
		apiInstance.subscribe(
				getSubscribingRequestBody(),
				new Callback<HashMap<String, JsonElement>>() {
					@Override
					public void success(
							HashMap<String, JsonElement> responseHashMap,
							Response response) {

						Integer status = Integer.parseInt(responseHashMap.get("status").toString());
						JsonElement message = responseHashMap.get("content");

						if (status == 200) {
							Telepat.getInstance().registerSubscription(Channel.this);

							for (JsonElement entry
									: message.getAsJsonArray()) {
								processNotification(new TransportNotification(entry));
							}

							if(Channel.this.mChannelEventListener != null) {
								mChannelEventListener.onSubscribeComplete();
							}

						} else {
							if (Channel.this.mChannelEventListener != null)
								Channel.this.mChannelEventListener.onError(status, message.toString());
						}
					}

					@Override
					public void failure(RetrofitError error) {
						if (error.getMessage().startsWith("409")) {
							TelepatLogger.log("There is an already active subscription for this channel.");
						} else if (error.getMessage().startsWith("401")) {
							TelepatLogger.log("Not logged in.");
						} else {
							TelepatLogger.log("Error subscribing: " + error.getMessage());
						}
					}
				});
	}

	public void count(final TelepatCountCallback callback, AggregationType aggregationType, String aggregationField) {
		HashMap<String, Object> countRequestBody = getSubscribingRequestBody();
		if(aggregationType != null && aggregationField != null) {
			HashMap<String, Object> aggregationHash = new HashMap<>();
			HashMap<String, String> aggregationFieldHash = new HashMap<>();
			aggregationFieldHash.put("field", aggregationField);
			switch (aggregationType) {
				case AverageAggregation:
					aggregationHash.put("avg", aggregationFieldHash);
					countRequestBody.put("aggregation", aggregationHash);
					break;
				case SumAggregation:
					aggregationHash.put("sum", aggregationFieldHash);
					countRequestBody.put("aggregation", aggregationHash);
					break;
				case None:
				default:
					break;
			}
		}
		apiInstance.count(
				countRequestBody,
				new Callback<GenericApiResponse>() {
					@Override
					public void success(GenericApiResponse genericApiResponse, Response response) {
						int countValue = ((Double) genericApiResponse.content.get("count")).intValue();
						Double aggregationValue = ((Double) genericApiResponse.content.get("aggregation"));
						callback.onSuccess(countValue, aggregationValue);
					}

					@Override
					public void failure(RetrofitError error) {
						callback.onFailure(error);
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
		if(mParentModelName == null)
			channel.put("context", mTelepatContext.getId());
		channel.put("model", mModelName);
		if(mSingleObjectId != null) channel.put("id", mSingleObjectId);
		if(mParentId!=null && mParentModelName!=null) {
			HashMap<String, String> parent = new HashMap<>();
			parent.put("id", mParentId);
			parent.put("model", mParentModelName);
			channel.put("parent", parent);
		}
		if(mUserId!=null) channel.put("user", mUserId);
		requestBody.put("channel", channel);
		if(mFilters != null) requestBody.put("filters", mFilters);
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
								TelepatLogger.log("Unsubscribe failed: " + error.getMessage());
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
    4:  "blg:{appId}:{model}",                                 //channel
    used for built-in models (users, contexts)
    5:  "blg:{appId}:context:{context}:{model}",
    //the Channel of all objects from a context
    7:  "blg:{appId}:context:{context}:users:{user_id}:{model}",
    //the Channel of all objects from a context from an user
    12: "blg:{appId}:{parent_model}:{parent_id}:{model}",            //the
    Channel of all objects belong to a parent
    14: "blg:{appId}:users:{user_id}:{parent_model}:{parent_id}:{model}",//the
    Channel of all comments from event 1 from user 16
    20: "blg:{appId}:{model}:{id}",                            //the
    Channel of one item
    var key = 'blg:'+API.appId;
    if (!options.channel.id && options.channel.context) {
      key += ':context:'+options.channel.context;
    }
    if (options.channel.user) {
      key += ':users:'+options.channel.user;
    }
    key += ':'+options.channel.model;
    if (options.channel.id) {
      key += ':'+options.channel.id;
    }
    return key;
    */

		if(mModelName == null) return null;

		String identifier = "blg:"+Telepat.getInstance().getAppId();
		if((mSingleObjectId == null && mParentId == null) && mTelepatContext!=null) {
			identifier += ":context:"+mTelepatContext.getId();
		}

		if( mUserId != null) {
			identifier+= ":users:"+mUserId;
		}

		if( mParentModelName!=null && mParentId!=null) {
			identifier+=":"+mParentModelName+":"+mParentId;
		}

		identifier += ":"+mModelName;

		if(mSingleObjectId != null) {
			identifier += ":"+mSingleObjectId;
		}

		if(mFilters != null) {
			Gson gson = new Gson();
			String jsonFilters = gson.toJson(mFilters);
			byte[] data = new byte[0];
			try {
				data = jsonFilters.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			String base64Filters = Base64.encodeToString(data, Base64.NO_WRAP);
			identifier += ":filter:"+base64Filters;
		}
//		return "blg:"+Telepat.getInstance().getAppId()+":context:"+mTelepatContext.getId()+':'+mModelName;
		return identifier;
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

	public List<TelepatBaseModel> getChannelObjects() {
		return dbInstance.getChannelObjects(getSubscriptionIdentifier(), this.objectType);
	}

	public TelepatBaseModel getObject(String id) {
		return dbInstance.getObject(getSubscriptionIdentifier(), id, this.objectType);
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
		if(notification==null || notification.getNotificationType()==null) return;

		switch (notification.getNotificationType()) {
			case ObjectAdded:
				if(notification.getNotificationValue()==null) return;
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
				if(notification.getNotificationValue()==null || notification.getNotificationPath()==null) return;
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
					String propertyValue = null;

					if(notification.getNotificationValue().isJsonPrimitive()) {
						Class propertyType = updatedObject.getPropertyType(propertyName);
						if(propertyType == Long.class)
							updatedObject.setProperty(propertyName, notification.getNotificationValue().getAsLong());
						else if(propertyType == String.class)
							updatedObject.setProperty(propertyName, notification.getNotificationValue().getAsString());
						else if(propertyType == Double.class)
							updatedObject.setProperty(propertyName, notification.getNotificationValue().getAsDouble());
						else if(propertyType == Float.class)
							updatedObject.setProperty(propertyName, notification.getNotificationValue().getAsFloat());
						else {
							TelepatLogger.log("Unsupported property type");
						}
					} else if(notification.getNotificationValue().isJsonObject()) {
						TelepatLogger.log(propertyName + " is a json object. Please update your object accordingly in the listener call. ");
						propertyValue = notification.getNotificationValue().getAsJsonObject().toString();
					} else if(notification.getNotificationValue().isJsonArray()) {
						TelepatLogger.log(propertyName+" is a json array. Please update your object accordingly in the listener call.");
						propertyValue = notification.getNotificationValue().getAsJsonArray().toString();
					}

					TelepatLogger.log("Pushing changed value to listeners: "+propertyValue);
					if(mChannelEventListener != null) {
						mChannelEventListener.onObjectModified(updatedObject,
								propertyName,
								propertyValue);
					}
					dbInstance.persistObject(getSubscriptionIdentifier(), updatedObject);
				}
				break;
			case ObjectDeleted:
				if(notification.getNotificationPath()==null) return;
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
