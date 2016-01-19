package io.telepat.sdk.models;

import retrofit.RetrofitError;

/**
 * Created by andrei on 11/8/15.
 */
public interface UserCreateListener {
    public void onUserCreateSuccess();
    public void onUserCreateFailure(RetrofitError error);
}
