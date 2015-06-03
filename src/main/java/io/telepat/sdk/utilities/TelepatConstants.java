package io.telepat.sdk.utilities;

/**
 * Created by catalinivan on 17/03/15.
 * Various constants used throughout the project
 */
public final class TelepatConstants
{
	public static final String SERVER_URL               = "http://blg-node-front.cloudapp.net:3000";
	public static final String ENDPOINT_SUBSCRIBE_EVENT = SERVER_URL + "/subscribe/event";
	public static final String ENDPOINT_REGISTER_USER   = SERVER_URL + "/user/login";
	public static final boolean RETROFIT_DEBUG_ENABLED  = true;
	public static final  String TAG                  = "TelepatSDK";

	public static final String UDID_KEY = "udid";
	public static final String JWT_KEY = "authentication-token";
	public static final String JWT_TIMESTAMP_KEY = "authentication-token-timestamp";
	public static final String FB_TOKEN_KEY = "fb-token";
	public static final int JWT_MAX_AGE = 60*60*1000;
	public static String SENDER_ID            = "361851333269";

	private TelepatConstants()
	{
	}
}
