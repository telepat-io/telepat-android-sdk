package io.telepat.sdk.networking.responses;

/**
 * Created by Andrei Marinescu on 03.06.2015.
 * Response PoJo class for device registering
 */
public class RegisterDeviceResponse {
    /**
     * The API status code
     */
    public int status;

    /**
     * The Telepat device identifier
     */
    public String identifier;
}
