package com.appscend.library;

import android.app.IntentService;
import android.content.Intent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

import java.io.IOException;

/**
 * Created by catalinivan on 09/03/15.
 */
public class GcmIntentService extends IntentService
{
	private static final String SERVICE_NAME = "com.appscend.library.GcmIntentService";

	private final ObjectMapper mMapper = new ObjectMapper();

	public GcmIntentService()
	{
		super(SERVICE_NAME);
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		String patch = "{dummy:\"json\"}";
		JsonNode nodeToPatch = null; // get the node somehow (not implemented yet)
		JsonPatch jsonPatch = null;

		try
		{
			jsonPatch = mMapper.readValue(patch, JsonPatch.class);
			nodeToPatch = jsonPatch.apply(nodeToPatch);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (JsonPatchException e)
		{
			e.printStackTrace();
		}
	}
}
