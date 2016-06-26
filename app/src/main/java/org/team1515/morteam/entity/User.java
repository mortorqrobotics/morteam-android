package org.team1515.morteam.entity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;
import org.team1515.morteam.network.CookieRequest;
import org.team1515.morteam.network.ImageCookieRequest;

import java.util.HashMap;
import java.util.Map;

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

    public User(String id) {
        this.id = id;
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

    public void requestProfPic(SharedPreferences preferences, RequestQueue queue, final PictureCallBack callBack) {
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

    public void populate(final SharedPreferences preferences, final RequestQueue queue, final boolean getProfPic) {
        Map<String, String> params = new HashMap<>();
        params.put("_id", getId());
        CookieRequest userRequest = new CookieRequest(Request.Method.GET,
                "/users",
                params,
                preferences,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject userObject = new JSONObject(response);
                            firstName = userObject.getString("firstname");
                            lastName = userObject.getString("lastname");
                            profPicPath = userObject.getString("profpicpath");
                            email = userObject.getString("email");
                            phone = userObject.getString("phone");
                            if(getProfPic) {
                                requestProfPic(preferences, queue, null);
                            }

                        } catch(JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );
        queue.add(userRequest);
    }
}
