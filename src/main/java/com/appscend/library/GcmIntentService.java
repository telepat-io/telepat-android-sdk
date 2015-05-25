package com.appscend.library;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by catalinivan on 09/03/15.
 */
public class GcmIntentService extends IntentService
{
	private static final String SERVICE_NAME = "com.appscend.library.GcmIntentService";

	public GcmIntentService()
	{
		super(SERVICE_NAME);
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{

	}
}
