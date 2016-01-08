package io.telepat.sdk.networking.responses;

import retrofit.RetrofitError;

/**
 * Created by Andrei Marinescu on 11/29/15.
 *
 */
public interface TelepatCountCallback {
    void onSuccess(int number);
    void onFailure(RetrofitError error);
}
