package io.telepat.sdk.utilities;

/**
 * Created by catalinivan on 17/03/15.
 */
public final class TelepatConstants
{
	public static final String SERVER_URL               = "http://blg-node-front.cloudapp.net:3000";
	public static final String ENDPOINT_SUBSCRIBE_EVENT = SERVER_URL + "/subscribe/event";
	public static final String ENDPOINT_REGISTER_USER   = SERVER_URL + "/user/login";
	public static final boolean RETROFIT_DEBUG_ENABLED  = false;
	public static final String KEY_USER_FB_TOKEN        = "access_token";
	public static final  String TAG                  = "TelepatSDK";

	public static final String UDID_KEY = "udid";
	public static String SENDER_ID            = "361851333269";

	private TelepatConstants()
	{
	}
}
