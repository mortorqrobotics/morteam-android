package org.team1515.morteam.network;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import org.team1515.morteam.MorTeam;

public class CookieImageLoader extends ImageLoader {

    public CookieImageLoader(RequestQueue queue, ImageCache imageCache) {
        super(queue, imageCache);
    }

    @Override
    protected Request<Bitmap> makeImageRequest(String requestUrl, int maxWidth, int maxHeight, ImageView.ScaleType scaleType, final String cacheKey) {
        return new CookieImageRequest(
                requestUrl,
                MorTeam.preferences,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        onGetImageSuccess(cacheKey, response);
                    }
                },
                maxWidth,
                maxHeight,
                ImageView.ScaleType.CENTER,
                Bitmap.Config.RGB_565,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        onGetImageError(cacheKey, error);
                    }
                }
        );
    }
}
