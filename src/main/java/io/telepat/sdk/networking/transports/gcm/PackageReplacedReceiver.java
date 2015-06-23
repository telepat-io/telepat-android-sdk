package io.telepat.sdk.networking.transports.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by catalinivan on 12/03/15.
 * BroadcastReceiver implementation for receiving application update callbacks (GCM re-registration)
 */
public class PackageReplacedReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		//TODO: app updated so trigger gcm registration
	}
}
