package org.team1515.morteam.network;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import com.android.volley.toolbox.StringRequest;

import java.util.Map;


public class CookieRequest extends StringRequest {
    static String TAG = "CookieRequest";

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
    protected Map<String, String> getParams() {
        return params;
    }
}
