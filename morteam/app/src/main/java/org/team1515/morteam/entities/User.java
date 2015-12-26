package org.team1515.morteam.entities;

import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.team1515.morteam.network.ImageCookieRequest;

public class User {
    private String firstName;
    private String lastName;
    private String id;
    private String profPicPath;
    private Bitmap profPic;
    private String email;
    private String phone;

    public User(String firstName, String lastName, String id, String profPicPath, String email, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.profPicPath = profPicPath;
        profPic = null;
        this.email = email;
        this.phone = phone;
    }

    public User(String firstName, String lastName, String id, String profPicPath) {
        this(firstName, lastName, id, profPicPath, "", "");
    }

    public User(String firstName, String lastName, String profPicPath) {
        this(firstName, lastName, "", profPicPath);
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getId() {
        return id;
    }

    public Bitmap getProfPic() {
        return profPic;
    }

    public void requestProfPic(RequestQueue queue, SharedPreferences preferences, final PictureCallBack callBack) {
        ImageCookieRequest messagePicRequest = new ImageCookieRequest(
                "http://www.morteam.com" + profPicPath,
                preferences,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        profPic = response;
                        try {
                            callBack.onComplete();
                        } catch (NullPointerException e) {

                        }
                    }
                }, 0, 0, null, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });
        queue.add(messagePicRequest);
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String formatPhoneNumber(String number) {
        return "(" + number.substring(0, 3) + ") " + number.substring(3, 6) + "-" + number.substring(6, number.length());
    }

    public String getPhoneFormatted() {
        return formatPhoneNumber(phone);
    }
}
