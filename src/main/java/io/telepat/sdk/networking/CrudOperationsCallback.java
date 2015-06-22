package io.telepat.sdk.networking;

import java.util.HashMap;

import io.telepat.sdk.utilities.TelepatLogger;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Andrei Marinescu on 22.06.2015.
 * Common Callback implementation for CRUD operations with OctopusApi
 */
public class CrudOperationsCallback implements Callback<HashMap<String, String>> {
    private String opType;

    public CrudOperationsCallback(String opType) {
        this.opType = opType;
    }

    @Override
    public void success(HashMap<String, String> apiResponse, Response response) {
        if(apiResponse.get("status").equals("202"))
            TelepatLogger.log(opType+" successful: " + apiResponse.get("message"));
        else
            TelepatLogger.error(opType+" failed: "+apiResponse.get("message"));
    }

    @Override
    public void failure(RetrofitError error) {
        TelepatLogger.log(opType+" failed: " + error.getMessage());
    }
}
