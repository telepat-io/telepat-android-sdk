package io.telepat.sdk.networking;

import io.telepat.sdk.utilities.TelepatUtilities;

import java.security.MessageDigest;

import retrofit.RequestInterceptor;

/**
 * Created by Andrei Marinescu on 02.06.2015.
 * Retrofit RequestInterceptor implementation for adding required HTTP headers
 */
public class OctopusRequestInterceptor implements RequestInterceptor{
    private String appId;
    private String apiKey;
    private String apiKeyHash;

    private String udid;
    private String authorizationToken;

    public OctopusRequestInterceptor(String apiKey, String appId) {
        this.appId = appId;
        this.apiKey = apiKey;
        MessageDigest digest;
        if(apiKey != null) {
            this.apiKeyHash = TelepatUtilities.sha256(apiKey);
        }
    }

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("Content-Type", "application/json");
        if(apiKeyHash!=null) { request.addHeader("X-BLGREQ-SIGN", apiKeyHash); }
        if(appId!=null) { request.addHeader("X-BLGREQ-APPID", appId); }
        if(udid!=null) { request.addHeader("X-BLGREQ-UDID", udid); }
        else { request.addHeader("X-BLGREQ-UDID",""); }
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getUdid() {
        return udid;
    }

    public void setUdid(String udid) {
        this.udid = udid;
    }
}
