package net.team1515.morteam.network;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ImageCookieRequest extends ImageRequest {

    SharedPreferences preferences;

    public ImageCookieRequest(String url, SharedPreferences preferences, Response.Listener<Bitmap> listener, int maxWidth, int maxHeight, ImageView.ScaleType scaleType, Bitmap.Config decodeConfig, Response.ErrorListener errorListener) {
        super(url, listener, maxWidth, maxHeight, scaleType, decodeConfig, errorListener);
        this.preferences = preferences;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();
        if(headers == null || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<>();
        }

        //Insert session-id cookie into header
        String sessionId = preferences.getString(CookieRequest.SESSION_COOKIE, "");
        if(sessionId.length() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(CookieRequest.SESSION_COOKIE);
            builder.append("=");
            builder.append(sessionId);
            if(headers.containsKey(CookieRequest.COOKIE_KEY)) {
                builder.append("; ");
                builder.append(headers.get(CookieRequest.COOKIE_KEY));
            }
            headers.put(CookieRequest.COOKIE_KEY, builder.toString());
        }

        return headers;
    }
}
