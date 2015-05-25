package com.appscend.library.networking;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.appscend.library.KrakenConstants;
import com.appscend.library.KrakenUtilities;
import com.appscend.library.model.KrakenContexts;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by catalinivan on 17/03/15.
 */
public class GetAllContextsRequest extends GsonRequest
{
	/**
	 * Make a GET request and return a parsed object from JSON.
	 *
	 * @param listener
	 * @param errorListener
	 */
	public GetAllContextsRequest(Response.Listener<KrakenContexts> listener, Response.ErrorListener errorListener)
	{
		super(Method.POST, KrakenConstants.ENDPOINT_CONTEXTS, KrakenContexts.class, null, listener, errorListener);
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError
	{
		HashMap<String, String> headers = new HashMap<>(3);
		headers.put("Content-Type", "application/json");
		headers.put("X-BLGREQ-SIGN", KrakenUtilities.sha256("1bc29b36f623ba82aaf6724fd3b16718"));
		headers.put("X-BLGREQ-APPID", "1");

		return headers;
	}
}
