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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.team1515.morteam.MorTeam;

import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.Collections;
import java.util.HashMap;
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
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        //Store session-id cookie in storage
        JSONArray jsonResponse = null;
        String stringResponse = null;
        Map<String, String> headers = response.headers;

        try {
            stringResponse = new String(response.data, HttpHeaderParser.parseCharset(headers));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Something went wrong, encoding-wise", e);
        }

        Log.i(TAG, "asdfkla;sdjf;lkj");
        Log.i(TAG, stringResponse);
        Log.i(TAG, Boolean.toString(headers.get("Set-Cookie") == null));

        Cookie

        try {
            jsonResponse = new JSONArray(stringResponse);
        } catch (JSONException e) {
            Log.e(TAG, "Something went wrong, json-wise", e);
        }

        Log.i(TAG, jsonResponse.toString());

        return super.parseNetworkResponse(response);
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }
}
