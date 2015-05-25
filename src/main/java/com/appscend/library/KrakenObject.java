package com.appscend.library;

import java.security.SecureRandom;

/**
 * Created by catalinivan on 16/03/15.
 */
public class KrakenObject extends Object
{
	private SecureRandom sr = new SecureRandom();
	private String property;

	public String getId()
	{
		return String.valueOf(sr.nextInt());
	}

	public String getProperty()
	{
		return property;
	}

	public void setProperty(String property)
	{
		this.property = property;
	}
}
