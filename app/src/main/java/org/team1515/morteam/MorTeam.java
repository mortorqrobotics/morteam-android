package org.team1515.morteam;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import org.team1515.morteam.network.CookieImageLoader;

public class MorTeam extends Application {
    public static SharedPreferences preferences;

    public static RequestQueue queue;
    public static CookieImageLoader imageLoader;

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = getSharedPreferences("org.team1515.morteam", MODE_PRIVATE);

        queue = Volley.newRequestQueue(this);
        imageLoader = new CookieImageLoader(queue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> cache
                    = new LruCache<>(50);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });
    }

    public static void setNetworkImage(String url, NetworkImageView view) {
        imageLoader.get(url, imageLoader.getImageListener(view,
                R.drawable.ic_user, R.drawable.ic_user));
        view.setImageUrl(url, imageLoader);
    }
}
