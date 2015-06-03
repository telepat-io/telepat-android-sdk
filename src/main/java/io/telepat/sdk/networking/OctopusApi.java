package io.telepat.sdk.networking;

import io.telepat.sdk.models.KrakenContext;
import io.telepat.sdk.networking.responses.RegisterDeviceResponse;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by Andrei Marinescu on 02.06.2015.
 * Octopus API Retrofit Interface
 */
public interface OctopusApi {
    @POST("/device/register")
    void registerDevice(@Body Map<String, Object> body, Callback<RegisterDeviceResponse> cb);

    @POST("/context/all")
    void updateContexts(Callback<Map<Integer,KrakenContext>> cb);

}
