package org.team1515.morteam.network;

import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;
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
        String TAG = "Thing";

        try {
            JSONObject jsonResponse = new JSONObject(HttpHeaderParser.parseCharset(response.headers));
        } catch (JSONException e) {
            Log.e("JSONResponse Error", "Wow this is really bad practice right here", e);
        }

//        Log.d()

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

//            HttpHeaderParser
        }

        return headers;
    }
}
