package io.telepat.sdk.networking.requests;

import android.os.Build;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by catalinivan on 27/05/15.
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
		deviceInfo.put("version", Build.VERSION.RELEASE);
		deviceInfo.put("sdk_level", String.valueOf(Build.VERSION.SDK_INT));
		deviceInfo.put("manufacturer", Build.MANUFACTURER);
		deviceInfo.put("model", Build.MODEL);

		params.put("persistent", tokenData);
		params.put("info", deviceInfo);

		return params;
	}
}
