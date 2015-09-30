package io.telepat.sdk.models;

/**
 * Created by catalinivan, Andrei Marinescu on 16/03/15.
 * Interface of object modification callbacks
 */
public interface OnChannelEventListener
{
	/**
	 * Fired when an object was added to the channel (by a 3rd party device, locally initiated adds
	 * are fired through <code>onObjectCreateSuccess</code>
	 * @param toAdd
	 */
	void onObjectAdded(TelepatBaseModel toAdd);

	/**
	 * Fired when an object was created successfully. The same object as the one submitted for
	 * creation is returned in this callback. The object ID is transparently added, even if this
	 * event is not handled.
	 * @param toAdd
	 */
	void onObjectCreateSuccess(TelepatBaseModel toAdd);

	/**
	 * Fired when an object was deleted
	 * @param toRemove the deleted object. This value is null if the object isn't stored locally
	 * @param objectId the ID of the deleted object
	 */
	void onObjectRemoved(TelepatBaseModel toRemove, String objectId);

	/**
	 * Fired when an object was updated
	 * @param target the updated object
	 * @param propertyName the updated property name
	 * @param newValue the new value of the updated property
	 */
	void onObjectModified(TelepatBaseModel target, String propertyName, String newValue);

	/**
	 * Fired when an error was detected by the SDK
	 * @param statusCode the code of the error
	 * @param message a message associated with the error
	 */
	void onError(Integer statusCode, String message);
}
