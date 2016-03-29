package io.telepat.sdk.models;

import java.util.HashMap;

/**
 * Created by andrei on 3/21/16.
 *
 */
public class TelepatProxyRequest {
    public enum HttpMethod {
        POST,
        GET,
        PUT,
        DELETE
    }

    private HttpMethod method;
    private HashMap<String, String> headers;
    private String url;
    private String queryString;
    private String body;

    @SuppressWarnings("unused")
    public TelepatProxyRequest(String url, String queryString) {
        method = HttpMethod.GET;
        //TODO remove this once the Telepat API no longer requires headers
        HashMap<String, String> headers = new HashMap<>();
        headers.put("SomeHeader", "SomeValue");
        this.headers = headers;
        this.url = url;
        this.queryString = queryString;
    }

    @SuppressWarnings("unused")
    public TelepatProxyRequest(String url, HashMap<String, String> headers, String queryString) {
        method = HttpMethod.GET;
        this.url = url;
        this.queryString = queryString;
    }

    @SuppressWarnings("unused")
    public TelepatProxyRequest(String url, HttpMethod method, String body) {
        this.url = url;
        this.body = body;
        this.method = method;
    }

    @SuppressWarnings("unused")
    public TelepatProxyRequest(String url, HttpMethod method, HashMap<String, String> headers, String body) {
        this.url = url;
        this.method = method;
        this.headers = headers;
        this.body = body;
    }

    @SuppressWarnings("unused")
    public HttpMethod getMethod() {
        return method;
    }

    @SuppressWarnings("unused")
    public HashMap<String, String> getHeaders() {
        return headers;
    }

    @SuppressWarnings("unused")
    public String getQueryString() {
        return queryString;
    }

    @SuppressWarnings("unused")
    public String getBody() {
        return body;
    }

    @SuppressWarnings("unused")
    public String getUrl() {
        return url;
    }
}
