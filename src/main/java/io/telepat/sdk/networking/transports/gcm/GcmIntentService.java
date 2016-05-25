package io.telepat.sdk.networking.transports.gcm;

import android.app.IntentService;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.telepat.sdk.Telepat;
import io.telepat.sdk.models.Channel;
import io.telepat.sdk.models.TransportNotification;
import io.telepat.sdk.utilities.TelepatLogger;

/**
 * Created by catalinivan, Andrei Marinescu on 09/03/15.
 * GCM message handler
 */
public class GcmIntentService extends IntentService
{
	private static final String SERVICE_NAME = "io.telepat.sdk.networking.transports.gcm.GcmIntentService";

	/**
	 * Reference to a Gson instance, used for notification Json decoding
	 */
	private final Gson jsonParser = new Gson();

	public GcmIntentService()
	{
		super(SERVICE_NAME);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if(intent.getStringExtra("data") != null) {
			TelepatLogger.log("we have data");
			String data = intent.getStringExtra("data");
			JsonObject jsonObject = jsonParser.fromJson(data, JsonObject.class);
			JsonArray newObjects = (JsonArray) jsonObject.get("new");
			JsonArray updatedObjects = (JsonArray) jsonObject.get("updated");
			JsonArray deletedObjects = (JsonArray) jsonObject.get("deleted");
			if(newObjects != null) prepareChannelNotification(newObjects, Channel.NotificationType.ObjectAdded);
			if(updatedObjects != null) prepareChannelNotification(updatedObjects, Channel.NotificationType.ObjectUpdated);
			if(deletedObjects != null) prepareChannelNotification(deletedObjects, Channel.NotificationType.ObjectDeleted);
		}
	}

    /**
     * Builds TransportNotification objects and relays it to the relevant channels
     * @param objects A JsonArray of notifications
     * @param notificationType The type of notifications (added/updated/deleted)
     */
	private void prepareChannelNotification(JsonArray objects, Channel.NotificationType notificationType) {
		for(JsonElement notificationElement : objects) {
			if(notificationElement.isJsonObject()) {
				JsonObject notificationObject = (JsonObject) notificationElement;
				TransportNotification notification = new TransportNotification(notificationObject, notificationType);
				if(notificationObject.has("subscription")) {
					String channelIdentifier = notificationObject.get("subscription").getAsString();
					notifyChannel(channelIdentifier, notification);
				} else {
					TelepatLogger.log("V2 notification format detected");
					if(notificationObject.has("subscriptions")) {
						JsonArray affectedChannels = notificationObject.getAsJsonArray("subscriptions");
						for(JsonElement element : affectedChannels) {
							if(element.isJsonPrimitive()) {
								String channelIdentifier = element.getAsString();
								notifyChannel(channelIdentifier, notification);
							}
						}
					}
				}
			}
		}
	}

	private void notifyChannel(String channelIdentifier, TransportNotification notification) {
		if (channelIdentifier.endsWith(":context")) {
			Telepat.getInstance().fireContextUpdate(notification);
		} else {
			Channel channel = Telepat.getInstance().getSubscribedChannel(channelIdentifier);
			if (channel != null) channel.processNotification(notification);
			else {
				TelepatLogger.error("No local channel instance available");
				channel = new Channel(channelIdentifier);
				channel.processNotification(notification);
			}
		}
	}

}
