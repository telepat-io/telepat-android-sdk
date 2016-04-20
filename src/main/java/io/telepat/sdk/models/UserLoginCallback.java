package io.telepat.sdk.models;

import java.util.HashMap;

import io.telepat.sdk.data.TelepatInternalDB;
import io.telepat.sdk.networking.OctopusRequestInterceptor;
import io.telepat.sdk.networking.responses.GenericApiResponse;
import io.telepat.sdk.utilities.TelepatConstants;
import io.telepat.sdk.utilities.TelepatLogger;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Andrei on 11.02.2016.
 * Retrofit callback implementation for user login requests.
 */
public class UserLoginCallback implements Callback<GenericApiResponse>  {

    private OctopusRequestInterceptor interceptor;
    private TelepatRequestListener loginListener;
    private TelepatInternalDB internalDB;

    public UserLoginCallback(OctopusRequestInterceptor interceptor,
                             TelepatInternalDB internalDB,
                             TelepatRequestListener loginListener) {
        this.interceptor = interceptor;
        this.internalDB = internalDB;
        this.loginListener = loginListener;
    }

    @Override
    public void success(GenericApiResponse genericApiResponse, Response response) {
        persistLoginData(genericApiResponse.content);
        TelepatLogger.log("User logged in");
        if(loginListener != null)
            loginListener.onSuccess();
    }

    @Override
    public void failure(RetrofitError error) {
        if(error!=null && error.getResponse()!=null && error.getResponse().getStatus()==409) {
            TelepatLogger.log("A facebook user with that fid already exists.");
        } else {
            TelepatLogger.log("User login failed.");
        }
        if(loginListener != null)
            loginListener.onError(error);
    }


    private void persistLoginData(HashMap<String, Object> loginData) {
        if(loginData.containsKey("token")) {
            internalDB.setOperationsData(TelepatConstants.JWT_KEY, loginData.get("token"));
            internalDB.setOperationsData(TelepatConstants.JWT_TIMESTAMP_KEY, System.currentTimeMillis());
            interceptor.setAuthorizationToken((String) loginData.get("token"));
        }

        if(loginData.containsKey("user")) {
            internalDB.setOperationsData(TelepatConstants.CURRENT_USER_DATA, loginData.get("user"));
        }
    }
}


