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
import org.team1515.morteam.network.CookieImageRequest;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String firstName;
    private String lastName;
    private String id;
    private String profPicPath;
    private String email;
    private String parentEmail;
    private String phone;

    public User(String firstName, String lastName, String id, String profPicPath, String email, String parentEmail, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        if (profPicPath.substring(0, 3).equals("/pp")) {
            this.profPicPath = "http://profilepics.morteam.com.s3.amazonaws.com" + profPicPath.substring(3);
        } else {
            this.profPicPath = "http://www.morteam.com:8080" + profPicPath;
        }
        this.email = email;
        this.parentEmail = parentEmail;
        this.phone = phone;
    }

    public User(String firstName, String lastName, String id, String profPicPath) {
        this(firstName, lastName, id, profPicPath, "", "", "");
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

    public String getProfPicPath() {
        return profPicPath;
    }

    public String getEmail() {
        return email;
    }

    public String getParentEmail() {
        return parentEmail;
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
