package io.telepat.sdk.models;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;

import io.telepat.sdk.Telepat;
import io.telepat.sdk.utilities.TelepatLogger;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by catalinivan on 09/03/15.
 */
public class Channel
{
	private HashMap<String, KrakenObject> mObjects;
	private String                  mChannelName;
	private ArrayList<String>       mFilters;
	private OnChannelEventListener mChannelEventListener;
	private KrakenContext mKrakenContext;
	private Class objectType;

	public Channel(KrakenContext context,
				   String modelName,
				   OnChannelEventListener listener,
				   Class type) {
		mKrakenContext = context;
		mChannelName = modelName;
		mChannelEventListener = listener;
		objectType = type;
		Telepat.getInstance()
				.getAPIInstance()
				.subscribe(
						getSubscribeRequestBody(),
						new Callback<HashMap<Integer, String>>() {
							@Override
							public void success(HashMap<Integer, String> integerStringHashMap, Response response) {
								for(Integer id : integerStringHashMap.keySet()) {
									TelepatLogger.log("Got object with ID: "+id+" and body:"+integerStringHashMap.get(id));
								}

							}

							@Override
							public void failure(RetrofitError error) {
								TelepatLogger.log("Error subscribing: "+error.getMessage());
							}
						});
	}

	public HashMap<String, Object> getSubscribeRequestBody() {
		HashMap<String, Object> requestBody = new HashMap<>();
		HashMap<String, Object> channel = new HashMap<>();
		channel.put("context", mKrakenContext.getId());
		channel.put("model", mChannelName);
		requestBody.put("channel", channel);
		return requestBody;
	}

	public void unsubscribe() {
		Telepat.getInstance()
				.getAPIInstance()
				.unsubscribe(getSubscribeRequestBody(),
						new Callback<HashMap<Integer, String>>() {
							@Override
							public void success(HashMap<Integer, String> integerStringHashMap, Response response) {
								TelepatLogger.log("Unsubscribed");
							}

							@Override
							public void failure(RetrofitError error) {
								TelepatLogger.log("Unsubscribe failed: "+error.getMessage());
							}
						});
	}

/*
  api.call('object/subscribe',
    {
      'channel': {
        'context': context,
        'model': channel
      }
    },
    function (err, res) {
      if (err) {
        event.emit('error', error('Subscribe failed with error: ' + err));
      } else {
        lastObjects = JSON.parse(JSON.stringify(self.objects));
        event.emit('update');
        timer = setInterval(function () {
          var diff = jsondiffpatch.diff(lastObjects, self.objects);
          if (diff !== undefined) {
            var diffKeys = Object.keys(diff);
            for (var i=0; i<diffKeys.length; i++) {
              if (diff[diffKeys[i]].length == 1) {
                self.add(self.objects[diffKeys[i]]);
                delete self.objects[diffKeys[i]];
              } else if (diff[diffKeys[i]].length == 2) {
                // update
              } else if (diff[diffKeys[i]].length == 3) {
                // delete
              }
            }
          }
          lastObjects = JSON.parse(JSON.stringify(self.objects));
        }, 50);
      }
    });

  this.unsubscribe = function() {
    api.call('object/unsubscribe',
      {
        channel: {
          context: context,
          model: channel
        }
      },
      function (err, res) {
        if (err) {
          event.emit('error', error('Unsubscribe failed with error: ' + err));
        } else {
          self.objects = {};
          clearInterval(timer);
          event.emit('unsubscribe');
          event.emit('_unsubscribe');
        }
      });
  }

  this.add = function(object) {
    api.call('object/create',
      {
        model: channel,
        content: object
      },
      function (err, res) {
        if (err) {
          event.emit('error', error('Adding object failed with error: ' + err));
        } else {
          //event.emit('update');
        }
      });
  }

  this.on = function(name, callback) {
    return event.on(name, callback);
  }
}
 */
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
