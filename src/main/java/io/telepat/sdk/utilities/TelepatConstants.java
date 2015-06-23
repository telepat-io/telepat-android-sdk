package io.telepat.sdk.utilities;

/**
 * Created by catalinivan on 17/03/15.
 * Various constants used throughout the project
 */
public final class TelepatConstants
{
	public static final String TELEPAT_ENDPOINT = "http://blg-octopus-api.cloudapp.net:3000";
	public static final String TAG = "TelepatSDK";
	public static final boolean RETROFIT_DEBUG_ENABLED  = true;

	public static final String UDID_KEY = "udid";
	public static final String JWT_KEY = "authentication-token";
	public static final String JWT_TIMESTAMP_KEY = "authentication-token-timestamp";
	public static final String FB_TOKEN_KEY = "fb-token";
	public static final String LOCAL_UDID_KEY = "local-udid";
	public static final int JWT_MAX_AGE = 60*60*1000;

	public static String GCM_SENDER_ID = "361851333269";

	private TelepatConstants() {
	}
}
