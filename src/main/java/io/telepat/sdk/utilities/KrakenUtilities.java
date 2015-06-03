package io.telepat.sdk.utilities;

import org.apache.http.protocol.HTTP;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SignatureException;
import java.util.Random;

/**
 * Created by catalinivan on 24/03/15.
 */
public class KrakenUtilities
{
	private static Random mRandom = new Random();

	public static String sha256(final String toHash)
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(toHash.getBytes(HTTP.UTF_8));
			StringBuilder hexString = new StringBuilder();

			for (int i = 0; i < hash.length; i++)
			{
//				Log.d("SHA-256", Integer.toBinaryString(hash[i]));
//				Log.d("SHA-256", Integer.toBinaryString(0xff & hash[i]));
//				Log.d("SHA-256", String.format("hash[i] = %d   0xff & hash[i] = %d", hash[i], 0xff & hash[i]));
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1)
				{
					hexString.append('0');
				}
				hexString.append(hex);
			}

			return hexString.toString();
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public static Random getRandomInstance()
	{
		return mRandom;
	}

	/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

	private static final int SECOND_MILLIS = 1000;
	private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
	private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
	private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


	public static String getTimeAgo(long time) {
		if (time < 1000000000000L) {
			// if timestamp given in seconds, convert to millis
			time *= 1000;
		}

		long now = System.currentTimeMillis();
		if (time > now || time <= 0) {
			return null;
		}

		// TODO: localize
		final long diff = now - time;
		if (diff < MINUTE_MILLIS) {
			return "just now";
		} else if (diff < 2 * MINUTE_MILLIS) {
			return "a minute ago";
		} else if (diff < 50 * MINUTE_MILLIS) {
			return diff / MINUTE_MILLIS + "m ago";
		} else if (diff < 90 * MINUTE_MILLIS) {
			return "an hour ago";
		} else if (diff < 24 * HOUR_MILLIS) {
			return diff / HOUR_MILLIS + "h ago";
		} else if (diff < 48 * HOUR_MILLIS) {
			return "yesterday";
		} else {
			return diff / DAY_MILLIS + "d ago";
		}
	}
	/* END Google I/O code */

	public String sha256Digest (String data) throws SignatureException {
		return getDigest("SHA-256", data, true);
	}

	private String getDigest(String algorithm, String data, boolean toLower)
			throws SignatureException {
		try {
			MessageDigest mac = MessageDigest.getInstance(algorithm);
			mac.update(data.getBytes("UTF-8"));
			return toLower ?
					toHex(mac.digest()).toLowerCase() : toHex(mac.digest());
		} catch (Exception e) {
			throw new SignatureException(e);
		}
	}

	private String toHex(byte[] bytes) {
		BigInteger bi = new BigInteger(1, bytes);
		return String.format("%0" + (bytes.length << 1) + "X", bi);
	}
}
