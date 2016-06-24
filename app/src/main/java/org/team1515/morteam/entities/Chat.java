package org.team1515.morteam.entities;

import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.team1515.morteam.network.ImageCookieRequest;

public class Chat {
    private String name;
    private String id;
    private String picPath;
    private Bitmap pic;
    public boolean isGroup;

    public Chat(String name, String id, String picPath, boolean isGroup) {
        this.name = name;
        this.id = id;
        this.picPath = picPath;
        pic = null;
        this.isGroup = isGroup;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public Bitmap getPic() {
        return pic;
    }

    public void requestProfPic(RequestQueue queue, SharedPreferences preferences, final PictureCallBack callBack) {
        ImageCookieRequest messagePicRequest = new ImageCookieRequest(
                "http://www.morteam.com" + picPath,
                preferences,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        pic = response;
                        callBack.onComplete();
                    }
                }, 0, 0, null, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });
        queue.add(messagePicRequest);
    }
}
