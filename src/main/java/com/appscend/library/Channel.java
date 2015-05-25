package com.appscend.library;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by catalinivan on 09/03/15.
 */
public class Channel
{
	private HashMap<String, KrakenObject> mObjects;
	private String                  mChannelName;
	private ArrayList<String>       mFilters;
	private OnChannelEventListener  mChannelEventListener;

	public Channel(String name)
	{
		mChannelName = name;
	}

	/**
	 *
	 */
	public void connect()
	{
		//TODO: register the caller as an observer on the channel and fetch channel data
	}

	public void disconnect()
	{
		//TODO: unregister the caller and mark channel data as invalid
	}

	/**
	 * Adds a filter to the list of active filters on the channel
	 *
	 * @param filterName: the name of the filter to add
	 * @throws java.lang.IllegalArgumentException if filterName is null or has 0 length.
	 */
	public void addFilter(String filterName)
	{
		if (TextUtils.isEmpty(filterName))
		{
			throw new IllegalArgumentException("Please try to add a meaningful filter! i.e. not null or 0 length");
		}

		if (mFilters == null)
		{
			mFilters = new ArrayList<>();
		}

		mFilters.add(filterName);
	}

	public void setOnChannelEventListener(OnChannelEventListener listener)
	{
		mChannelEventListener = listener;
	}

	protected void addObject(KrakenObject toAdd)
	{
		if (mObjects == null)
		{
			mObjects = new HashMap<>();
		}

		mObjects.put(toAdd.getId(), toAdd);

		if (mChannelEventListener != null)
		{
			mChannelEventListener.onObjectAdded(toAdd);
		}
	}

	protected void removeObject(KrakenObject toRemove)
	{
		mObjects.remove(toRemove.getId());

		if (mChannelEventListener != null)
		{
			mChannelEventListener.onObjectRemoved(toRemove);
		}
	}

	protected void modifyObject(KrakenObject target, KrakenObject source)
	{
		KrakenObject objToModify = mObjects.get(target.getId());

		//TODO: apply the patch somehow
	}

}
