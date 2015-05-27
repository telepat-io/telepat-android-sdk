package com.appscend.library.networking;

import android.os.Build;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.appscend.library.KrakenConstants;
import com.appscend.library.model.RegisterDeviceResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by catalinivan on 27/05/15.
 */
public class RegisterDeviceRequest extends GsonRequest
{
	private String mRegId;
	public RegisterDeviceRequest(String regId, Response.Listener successListener, Response.ErrorListener errorListener)
	{
		super(Method.POST, KrakenConstants.ENDPOINT_REGISTER_DEVICE, RegisterDeviceResponse.class, null, successListener, errorListener);

		mRegId = regId;
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError
	{
		HashMap<String, String> headers = new HashMap<>(1);
		headers.put("Content-Type", "application/json");
		headers.put("X-BLGREQ-UDID", "");

		return headers;
	}

	@Override
	protected Map<String, Object> getParams() throws AuthFailureError
	{
		HashMap<String, Object> params = new HashMap<>();
		HashMap<String, String> innerObject = new HashMap<>();

		innerObject.put("type", "android");
		innerObject.put("token", mRegId);

		params.put("os", Build.VERSION.SDK_INT + Build.VERSION.RELEASE);
		params.put("persistent", innerObject);

		return params;
	}
}
