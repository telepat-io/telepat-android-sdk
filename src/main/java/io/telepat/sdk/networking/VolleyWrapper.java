package io.telepat.sdk.networking;

import android.content.Context;

import io.android.volley.Request;
import io.android.volley.RequestQueue;
import io.android.volley.toolbox.Volley;

/**
 * Created by catalinivan on 09/03/15.
 */
public class VolleyWrapper
{
	private static VolleyWrapper mInstance;
	private        Context       mContext;
	private        RequestQueue  mRequestQueue;

	private VolleyWrapper(Context context)
	{
		// getApplicationContext() is key, it keeps you from leaking the
		// Activity or BroadcastReceiver if someone passes one in.
		mContext = context.getApplicationContext();
		mRequestQueue = getRequestQueue();
	}

	public static VolleyWrapper getInstance(Context context)
	{
		if (mInstance == null)
		{
			mInstance = new VolleyWrapper(context);
		}

		return mInstance;
	}

	public RequestQueue getRequestQueue()
	{
		if (mRequestQueue == null)
		{
			mRequestQueue = Volley.newRequestQueue(mContext);
		}

		return mRequestQueue;
	}

	public <T> void addRequest(Request<T> req)
	{
		getRequestQueue().add(req);
	}
}
