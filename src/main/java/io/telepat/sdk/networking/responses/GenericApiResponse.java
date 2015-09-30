package io.telepat.sdk.networking.responses;

import java.util.HashMap;

/**
 * Created by Andrei Marinescu on 03.06.2015.
 * Response PoJo Class for User/Login
 */
public class GenericApiResponse {
    public int status;
    public HashMap<String, Object> content;
}
