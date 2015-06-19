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
import io.telepat.sdk.utilities.TelepatLogger;

/**
 * Created by catalinivan, Andrei Marinescu on 09/03/15.
 * GCM message handler
 */
public class GcmIntentService extends IntentService
{
	private static final String SERVICE_NAME = "io.telepat.sdk.networking.transports.gcm.GcmIntentService";

//	private final ObjectMapper mMapper = new ObjectMapper();

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
//			TelepatLogger.log(jsonObject.get("new").toString());
//			TelepatLogger.log(data);
		}
//		String patch = "{dummy:\"json\"}";
//		JsonNode nodeToPatch = null; // get the node somehow (not implemented yet)
//		JsonPatch jsonPatch = null;
//
//		try
//		{
//			jsonPatch = mMapper.readValue(patch, JsonPatch.class);
//			nodeToPatch = jsonPatch.apply(nodeToPatch);
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//		catch (JsonPatchException e)
//		{
//			e.printStackTrace();
//		}
	}
	private void notifyChannel(JsonArray objects, Channel.NotificationType notificationType) {
		for(JsonElement newObject : objects) {
			if(newObject.isJsonObject()) {
				String channelIdentifier = ((JsonObject)newObject).get("subscription").getAsString();
				Channel channel = Telepat.getInstance().getSubscribedChannel(channelIdentifier);
				channel.processNotification(((JsonObject)newObject).get("value"), notificationType);
			}
		}
	}
}
