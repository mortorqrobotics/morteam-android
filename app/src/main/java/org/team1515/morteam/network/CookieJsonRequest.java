package org.team1515.morteam.network;

import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CookieJsonRequest extends JsonObjectRequest {
    public static final String SET_COOKIE_KEY = "set-cookie";
    public static final String COOKIE_KEY = "Cookie";
    public static final String SESSION_COOKIE = "connect.sid";

        private static final String host = "http://www.morteam.com:8080/api";
//    public static final String host = "http://192.168.1.100:8042";

    private SharedPreferences preferences;

    public CookieJsonRequest(int method, String path, SharedPreferences preferences, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        this(method, path, new JSONObject(), preferences, listener, errorListener);
    }

    public CookieJsonRequest(int method, String path, JSONObject jsonRequest, SharedPreferences preferences, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, host + path, jsonRequest, listener, errorListener);
        this.preferences = preferences;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        //Store seession-id cookie in storage
        if(response.headers.containsKey(SET_COOKIE_KEY) && response.headers.get(SET_COOKIE_KEY).startsWith(SESSION_COOKIE)) {
            String cookie = response.headers.get(SET_COOKIE_KEY);
            if (cookie.length() > 0) {
                String[] splitCookie = cookie.split(";");
                String[] splitSessionId = splitCookie[0].split("=");
                cookie = splitSessionId[1];
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(SESSION_COOKIE, cookie);
                editor.apply();
            }
        }
        return super.parseNetworkResponse(response);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();
        if(headers == null || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<>();
        }

        //Insert session-id cookie into header
        String sessionId = preferences.getString(SESSION_COOKIE, "");
        if(sessionId.length() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(SESSION_COOKIE);
            builder.append("=");
            builder.append(sessionId);
            if(headers.containsKey(COOKIE_KEY)) {
                builder.append("; ");
                builder.append(headers.get(COOKIE_KEY));
            }
            headers.put(COOKIE_KEY, builder.toString());
        }

        return headers;
    }
}
