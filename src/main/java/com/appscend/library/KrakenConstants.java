package com.appscend.library;

/**
 * Created by catalinivan on 17/03/15.
 */
public final class KrakenConstants
{
	public static final String SERVER_URL               = "http://blg-node-front.cloudapp.net:3000";
	public static final String ENDPOINT_CONTEXTS        = SERVER_URL + "/get/contexts";
	public static final String ENDPOINT_SUBSCRIBE_EVENT = SERVER_URL + "/subscribe/event";
	public static final String ENDPOINT_REGISTER_USER   = SERVER_URL + "/user/login";
	public static final String ENDPOINT_REGISTER_DEVICE = SERVER_URL + "/device/register";
	public static final String KEY_USER_FB_TOKEN        = "access_token";

	private KrakenConstants()
	{
	}
}
