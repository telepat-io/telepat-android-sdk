package com.appscend.library;

import org.apache.http.protocol.HTTP;

import java.security.MessageDigest;
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
}
