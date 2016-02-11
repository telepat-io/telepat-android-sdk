package io.telepat.sdk.networking.requests;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Andrei Marinescu on 18/03/15.
 * Request formatting class for user/register
 */
public class RegisterTwitterUserRequest
{
	public static final String KEY_USER_OAUTH_TOKEN = "oauth_token";
	public static final String KEY_USER_SECRET_TOKEN = "oauth_token_secret";
	/**
	 * The logged in user's Twitter OAUTH token
	 */
	private String oauthToken;
	private String oauthTokenSecret;

	public RegisterTwitterUserRequest(String oauthToken, String oauthTokenSecret) {
		this.oauthToken = oauthToken;
		this.oauthTokenSecret = oauthTokenSecret;
	}

	/**
	 *
	 * @return a Map representation of the required POST fields for a register request
	 */
	public Map<String, String> getParams() {
		HashMap<String, String> params = new HashMap<>(1);

		params.put(KEY_USER_OAUTH_TOKEN, oauthToken);
		params.put(KEY_USER_SECRET_TOKEN, oauthTokenSecret);

		return params;
	}
}
