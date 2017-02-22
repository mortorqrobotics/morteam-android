package org.team1515.morteam.network;

import android.content.SharedPreferences.Editor;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;

import org.team1515.morteam.MorTeam;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class CookieRequest extends StringRequest {
    private final Map<String, String> params;

    public CookieRequest(int method, String path, Map<String, String> params, Listener<String> listener, ErrorListener errorListener) {
        super(method, NetworkUtils.makeURL(path, true), listener, errorListener);
        this.params = params;
    }

    public CookieRequest(int method, String path, boolean isAPI, Listener<String> listener, ErrorListener errorListener) {
        super(method, NetworkUtils.makeURL(path, isAPI), listener, errorListener);
        params = null;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        //Store session-id cookie in storage
        if(response.headers.containsKey(NetworkUtils.SET_COOKIE_KEY) && response.headers.get(NetworkUtils.SET_COOKIE_KEY).startsWith(NetworkUtils.SESSION_COOKIE)) {
            String cookie = response.headers.get(NetworkUtils.SET_COOKIE_KEY);
            if (cookie.length() > 0) {
                String[] splitCookie = cookie.split(";");
                String[] splitSessionId = splitCookie[0].split("=");
                cookie = splitSessionId[1];
                Editor editor = MorTeam.preferences.edit();
                editor.putString(NetworkUtils.SESSION_COOKIE, cookie);
                editor.apply();
            }
        }
        return super.parseNetworkResponse(response);
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();
        if(headers == null || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<>();
        }

        //Insert session-id cookie into header
        String sessionId = MorTeam.preferences.getString(NetworkUtils.SESSION_COOKIE, "");
        if(sessionId.length() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(NetworkUtils.SESSION_COOKIE);
            builder.append("=");
            builder.append(sessionId);
            if(headers.containsKey(NetworkUtils.COOKIE_KEY)) {
                builder.append("; ");
                builder.append(headers.get(NetworkUtils.COOKIE_KEY));
            }
            headers.put(NetworkUtils.COOKIE_KEY, builder.toString());
        }

        return headers;
    }
}
