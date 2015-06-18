package io.telepat.sdk.networking.transports.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import io.telepat.sdk.utilities.TelepatConstants;

/**
 * Created by catalinivan, Andrei Marinescu on 09/03/15.
 * GCM Receiver class.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		GoogleCloudMessaging gcm  = GoogleCloudMessaging.getInstance(context);
		Log.d(TelepatConstants.TAG, "Message received with message type = " + gcm.getMessageType(intent) + " and content = " + intent.getExtras().toString());
		// Explicitly specify that GcmIntentService will handle the intent.
		ComponentName comp = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());
		// Start the service, keeping the device awake while it is launching.
		startWakefulService(context, (intent.setComponent(comp)));
		setResultCode(Activity.RESULT_OK);
	}
}
