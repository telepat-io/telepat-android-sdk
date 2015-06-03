package io.telepat.sdk.networking;

import io.telepat.sdk.models.KrakenContext;
import io.telepat.sdk.networking.responses.RegisterDeviceResponse;

import java.util.HashMap;
import java.util.Map;

import io.telepat.sdk.networking.responses.UserLoginResponse;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;

/**
 * Created by Andrei Marinescu on 02.06.2015.
 * Octopus API Retrofit Interface
 */
public interface OctopusApi {
    @POST("/device/register")
    void registerDevice(@Body Map<String, Object> body, Callback<RegisterDeviceResponse> cb);

    @GET("/context/all")
    void updateContexts(Callback<Map<Integer,KrakenContext>> cb);

    @POST("/user/login")
    void loginAsync(@Body Map<String, String> body, Callback<UserLoginResponse> cb);

    @POST("/user/login")
    UserLoginResponse login(@Body Map<String, String> body);

    @POST("/user/logout")
    void logout(@Body Map<String, String> body, Callback<HashMap<String,Object>> cb);
}
