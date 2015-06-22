package io.telepat.sdk.models;

/**
 * Created by catalinivan, Andrei Marinescu on 16/03/15.
 * Interface of object modification callbacks
 */
public interface OnChannelEventListener
{
	void onObjectAdded(TelepatBaseModel toAdd);

	void onObjectCreateSuccess(TelepatBaseModel toAdd);

	void onObjectRemoved(TelepatBaseModel toRemove, int objectId);

	void onObjectModified(TelepatBaseModel target, String propertyName, String newValue);

	void onError(Integer statusCode, String message);
}
