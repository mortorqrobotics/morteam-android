package org.team1515.morteam.entities;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.text.format.DateFormat;

import com.android.volley.RequestQueue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Announcement {
    private User user;
    private String content;
    private Date date;
    private String id;

    public Announcement(User user, String content, String date, String id) {
        this.user = user;
        this.content = content;
        try {
            this.date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).parse(date.replace("Z", "+0000"));
        } catch (ParseException e) {
            e.printStackTrace();
            date = null;
        }
        this.id = id;
    }

    public String getUserName() {
        return user.getFullName();
    }

    public void requestProfPic(SharedPreferences preferences, RequestQueue queue, PictureCallBack callBack) {
        user.requestProfPic(preferences, queue, callBack);
    }

    public Bitmap getProfPic() {
        return user.getProfPic();
    }

    public String getContent() {
        return  content;
    }

    public Date getRawDate() {
        return date;
    }

    public String getDate() {
        return DateFormat.format("h:mm a - MMMM d, yyyy", date).toString();
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return user.getId();
    }
}
