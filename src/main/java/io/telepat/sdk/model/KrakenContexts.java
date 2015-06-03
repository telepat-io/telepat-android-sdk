package io.telepat.sdk.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

/**
 * Created by catalinivan on 17/03/15.
 */
public class KrakenContexts
{
	@SerializedName("message")
	private HashMap<String, KrakenContext> contexts;

	public HashMap<String, KrakenContext> getContexts()
	{
		return contexts;
	}

	public void setContexts(HashMap<String, KrakenContext> contexts)
	{
		this.contexts = contexts;
	}
}
