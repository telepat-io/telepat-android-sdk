package io.telepat.sdk.models;

import retrofit.RetrofitError;

/**
 * Created by andrei on 11/8/15.
 */
public interface TelepatRequestListener {
    public void onSuccess();
    public void onError(RetrofitError error);
}
