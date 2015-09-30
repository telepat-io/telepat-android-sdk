package io.telepat.sdk.networking.requests;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by catalinivan, Andrei Marinescu on 18/03/15.
 * Request formatting class for user/register
 */
public class RegisterUserRequest
{
	public static final String KEY_USER_FB_TOKEN = "access_token";
	/**
	 * The logged in user's Facebook OAUTH token
	 */
	private String mFBToken;

	public RegisterUserRequest(String fbToken) { mFBToken = fbToken; }

	/**
	 *
	 * @return a Map representation of the required POST fields for a register request
	 */
	public Map<String, String> getParams() {
		HashMap<String, String> params = new HashMap<>(1);

		params.put(KEY_USER_FB_TOKEN, mFBToken);

		return params;
	}
}
