package io.telepat.sdk.models;

/**
 * Created by catalinivan, Andrei Marinescu on 16/03/15.
 * Interface of object modification callbacks
 */
public interface OnChannelEventListener
{
	void onObjectAdded(Object toAdd);

	void onObjectRemoved(Object toRemove);

	void onObjectModified(Object target, Object newValue);

	void onError(Integer statusCode, String message);
}
