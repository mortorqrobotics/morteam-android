package org.team1515.morteam.entity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.text.format.DateFormat;

import com.android.volley.RequestQueue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Message {
    private User user;
    private String content;
    private Date date;
    private String id;
    public boolean isMyMessage;

    public Message(User user, String content, String date, String id, boolean isMyMessage) {
        this.user = user;
        this.content = content;
        try {
            this.date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).parse(date.replace("Z", "+0000"));
        } catch (ParseException e) {
            e.printStackTrace();
            date = null;
        }
        this.id = id;
        this.isMyMessage = isMyMessage;
    }

    public String getFirstName() {
        return user.getFirstName();
    }

    public String getContent() {
        return content;
    }

    public Date getRawDate() {
        return date;
    }

    public String getDate() {
        return DateFormat.format("MMM d, h:mm a", date).toString();
    }

    public String getId() {
        return id;
    }

    public Bitmap getProfPic() {
        return user.getProfPic();
    }

    public void requestProfPic(SharedPreferences preferences, RequestQueue queue, final PictureCallBack callBack) {
        user.requestProfPic(preferences, queue, callBack);
    }
}
