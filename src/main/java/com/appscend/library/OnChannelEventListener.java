package com.appscend.library;

/**
 * Created by catalinivan on 16/03/15.
 */
public interface OnChannelEventListener
{
	void onObjectAdded(Object toAdd);

	void onObjectRemoved(Object toRemove);

	void onObjectModified(Object target, Object newValue);
}
