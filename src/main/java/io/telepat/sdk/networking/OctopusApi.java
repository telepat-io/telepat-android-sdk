package io.telepat.sdk.networking;

import com.google.gson.JsonElement;

import io.telepat.sdk.models.TelepatContext;
import io.telepat.sdk.networking.responses.ContextsApiResponse;
import io.telepat.sdk.networking.responses.RegisterDeviceResponse;

import java.util.HashMap;
import java.util.Map;

import io.telepat.sdk.networking.responses.GenericApiResponse;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;

/**
 * Created by Andrei Marinescu on 02.06.2015.
 * Octopus API Retrofit Interface
 */
@SuppressWarnings("JavaDoc")
public interface OctopusApi {
    /**
     * Method for sending a device registration request
     * @param body
     * @param cb
     */
    @POST("/device/register")
    void registerDevice(@Body Map<String, Object> body, Callback<GenericApiResponse> cb);

    /**
     * Method for retrieving all active contexts
     * @param cb
     */
    @GET("/context/all")
    void updateContexts(Callback<ContextsApiResponse> cb);

    /**
     * Method for sending an async register request
     * @param body
     * @param cb
     */
    @POST("/user/register")
    void registerAsync(@Body Map<String, String> body, Callback<Map<String, String>> cb);

    /**
     * Method for sending a synchronous register request
     * @param body
     * @return
     */
    @POST("/user/register")
    Map<String, String> register(@Body Map<String, String> body);

    /**
     * Method for sending an async login request
     * @param body
     * @param cb
     */
    @POST("/user/login")
    void loginAsync(@Body Map<String, String> body, Callback<GenericApiResponse> cb);

    /**
     * Method for sending a logout request
     * @param cb
     */
    @GET("/user/logout")
    void logout(Callback<HashMap<String,Object>> cb);

    /**
     * Method for sending a subscribe request
     * @param body
     * @param cb
     */
    @POST("/object/subscribe")
    void subscribe(@Body Map<String, Object> body, Callback<HashMap<String, JsonElement>> cb);

    /**
     * Method for sending an unsubscribe request
     * @param body
     * @param cb
     */
    @POST("/object/unsubscribe")
    void unsubscribe(@Body Map<String, Object> body, Callback<HashMap<String, String>> cb);

    /**
     * Method for sending an object creation request
     * @param body
     * @param cb
     */
    @POST("/object/create")
    void create(@Body Map<String, Object> body, Callback<HashMap<String, String>> cb);

    /**
     * Method for sending an object update request
     * @param body
     * @param cb
     */
    @POST("/object/update")
    void update(@Body Map<String, Object> body, Callback<HashMap<String, String>> cb);

    /**
     * Method for sending an object delete request
     * @param body
     * @param cb
     */
    @POST("/object/delete")
    void delete(@Body Map<String, Object> body, Callback<HashMap<String, String>> cb);
}
