package io.telepat.sdk.networking.requests;

import android.os.Build;

import java.util.HashMap;
import java.util.Map;

import io.telepat.sdk.Telepat;

/**
 * Created by Andrei Marinescu on 2/06/15.
 * Request generator for device registering
 */
public class RegisterDeviceRequest
{
	/**
	 * The GCM token of the device
	 */
	private String mRegId;
	public RegisterDeviceRequest(String regId)
	{
		mRegId = regId;
	}

	/**
	 * Returns a Map of the required POST fields of an device registration request
	 * @return
	 */
	public Map<String, Object> getParams()
	{
		HashMap<String, Object> params = new HashMap<>();
		HashMap<String, Object> tokenData = new HashMap<>();
		HashMap<String, String> deviceInfo = new HashMap<>();

		tokenData.put("type", "android");
		tokenData.put("token", mRegId);
		tokenData.put("active", 1);

		deviceInfo.put("os", "Android");
		deviceInfo.put("udid", Telepat.getInstance().getDeviceLocalIdentifier());
		deviceInfo.put("version", Build.VERSION.RELEASE);
		deviceInfo.put("sdk_level", String.valueOf(Build.VERSION.SDK_INT));
		deviceInfo.put("manufacturer", Build.MANUFACTURER);
		deviceInfo.put("model", Build.MODEL);

		params.put("persistent", tokenData);
		params.put("info", deviceInfo);

		return params;
	}
}
