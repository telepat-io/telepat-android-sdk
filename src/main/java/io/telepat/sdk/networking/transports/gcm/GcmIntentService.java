package io.telepat.sdk.networking.transports.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;

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
			if(newObjects != null) notifyChannel(newObjects, Channel.NotificationType.ObjectAdded);
			if(updatedObjects != null) notifyChannel(updatedObjects, Channel.NotificationType.ObjectUpdated);
			if(deletedObjects != null) notifyChannel(deletedObjects, Channel.NotificationType.ObjectDeleted);
		}
	}
	private void notifyChannel(JsonArray objects, Channel.NotificationType notificationType) {
		for(JsonElement notificationObject : objects) {
			if(notificationObject.isJsonObject()) {
				TransportNotification notification = new TransportNotification((JsonObject)notificationObject, notificationType);
				String channelIdentifier = ((JsonObject)notificationObject).get("subscription").getAsString();
				Channel channel = Telepat.getInstance().getSubscribedChannel(channelIdentifier);
				if(channel != null) channel.processNotification(notification);
				else {
					//TODO - do persistance anyway
					TelepatLogger.error("Discarding notification due to no local channel instance available");
				}
			}
		}
	}
}
