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
	private String mRegId;
	public RegisterDeviceRequest(String regId)
	{
		mRegId = regId;
	}

	public Map<String, Object> getParams()
	{
		HashMap<String, Object> params = new HashMap<>();
		HashMap<String, String> tokenData = new HashMap<>();
		HashMap<String, String> deviceInfo = new HashMap<>();

		tokenData.put("type", "android");
		tokenData.put("token", mRegId);

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
