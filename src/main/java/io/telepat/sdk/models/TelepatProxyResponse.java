package io.telepat.sdk.models;

import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Header;

/**
 * Created by andrei on 3/21/16.
 *
 */
public interface TelepatProxyResponse {
    void onRequestFinished(String responseBody, List<Header> responseHeaders);
    void onTelepatError(RetrofitError error);
}
