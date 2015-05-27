package com.appscend.library.networking;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.appscend.library.KrakenConstants;
import com.appscend.library.model.KrakenUser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by catalinivan on 18/03/15.
 */
public class RegisterUserRequest extends GsonRequest<KrakenUser>
{
	private String mFBToken;
	/**
	 * Make a GET request and return a parsed object from JSON.
	 *
	 * @param listener
	 * @param errorListener
	 */
	public RegisterUserRequest(final String fbToken, Response.Listener listener, Response.ErrorListener errorListener)
	{
		super(Method.POST, KrakenConstants.ENDPOINT_REGISTER_USER, KrakenUser.class, null, listener, errorListener);

		mFBToken = fbToken;
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError
	{
		HashMap<String, String> params = new HashMap<>(1);

		params.put(KrakenConstants.KEY_USER_FB_TOKEN, mFBToken);

		return params;
	}
}
