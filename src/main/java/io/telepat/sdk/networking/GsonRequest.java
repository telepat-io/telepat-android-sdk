package io.telepat.sdk.networking;

import android.util.Log;

import io.android.volley.AuthFailureError;
import io.android.volley.NetworkResponse;
import io.android.volley.ParseError;
import io.android.volley.Request;
import io.android.volley.Response;
import io.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by catalinivan on 09/03/15.
 */
public class GsonRequest<T> extends Request<T>
{
	private final Gson gson = new Gson();
	private final Class<T>            clazz;
	private final Map<String, String> headers;
	private final Response.Listener<T> listener;

	/**
	 * Make a GET request and return a parsed object from JSON.
	 *
	 * @param method  HTTP method type as specified by the {@link Method} interface
	 * @param url     URL of the request to make
	 * @param clazz   Relevant class object, for Gson's reflection
	 * @param headers Map of request headers
	 */
	public GsonRequest(int method, String url, Class<T> clazz, Map<String, String> headers,
					   Response.Listener<T> listener, Response.ErrorListener errorListener)
	{
		super(method, url, errorListener);
		this.clazz = clazz;
		this.headers = headers;
		this.listener = listener;
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError
	{
		return headers != null ? headers : super.getHeaders();
	}

	@Override
	protected void deliverResponse(T response)
	{
		listener.onResponse(response);
	}

	@Override
	protected Response<T> parseNetworkResponse(NetworkResponse response)
	{
		try
		{
			String json = new String(
					response.data,
					HttpHeaderParser.parseCharset(response.headers));
			Log.d("Volley", json);

			return Response.success(
					gson.fromJson(json, clazz),
					HttpHeaderParser.parseCacheHeaders(response));
		}
		catch (UnsupportedEncodingException e)
		{
			return Response.error(new ParseError(e));
		}
		catch (JsonSyntaxException e)
		{
			return Response.error(new ParseError(e));
		}
	}
}
