package io.telepat.sdk.networking.transports.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by catalinivan on 12/03/15.
 */
public class BootReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		//TODO: boot complete so maybe the OS id has changed which means we need to trigger gcm registration
	}
}
