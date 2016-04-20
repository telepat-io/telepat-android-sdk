package io.telepat.sdk.networking.requests;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by catalinivan, Andrei Marinescu on 18/03/15.
 * Request formatting class for user/register
 */
public class RegisterFacebookUserRequest
{
	public static final String KEY_USER_FB_TOKEN = "access_token";
	public static final String KEY_USER_USERNAME = "username";
	/**
	 * The logged in user's Facebook OAUTH token
	 */
	private String mFBToken;
	private String mUsername;

	public RegisterFacebookUserRequest(String fbToken) { mFBToken = fbToken; }
	public RegisterFacebookUserRequest(String fbToken, String username) {
		mFBToken = fbToken;
		mUsername = username;
	}

	/**
	 *
	 * @return a Map representation of the required POST fields for a register request
	 */
	public Map<String, String> getParams() {
		HashMap<String, String> params = new HashMap<>(1);

		params.put(KEY_USER_FB_TOKEN, mFBToken);
		if (mUsername != null && !mUsername.isEmpty()) {
			params.put(KEY_USER_USERNAME, mUsername);
		}

		return params;
	}
}
