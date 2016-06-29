package org.team1515.morteam.entity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.team1515.morteam.network.CookieImageRequest;

public class Chat {
    private String name;
    private String id;
    private String picPath;
    public boolean isGroup;

    public Chat(String name, String id, String picPath, boolean isGroup) {
        this.name = name;
        this.id = id;
        this.picPath = picPath;
        this.isGroup = isGroup;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getPicPath() {
        return "http://morteam.com/" + picPath;
    }
}
