package org.team1515.morteam.entities;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.text.format.DateFormat;

import com.android.volley.RequestQueue;

import java.util.Date;

public class Message {
    private User user;
    private String content;
    private String id;
    public boolean isMyMessage;

    public Message(User user, String content, String id, boolean isMyMessage) {
        this.user = user;
        this.content = content;
        this.id = id;
        this.isMyMessage = isMyMessage;
    }

    public String getFirstName() {
        return user.getFirstName();
    }

    public String getContent() {
        return  content;
    }

    public String getId() {
        return id;
    }

    public Bitmap getProfPic() {
        return user.getProfPic();
    }

    public void requestProfPic(RequestQueue queue, SharedPreferences preferences, final PictureCallBack callBack) {
        user.requestProfPic(queue, preferences, callBack);
    }
}
