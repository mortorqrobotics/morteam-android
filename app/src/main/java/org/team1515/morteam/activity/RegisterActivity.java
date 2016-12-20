package org.team1515.morteam.activity;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;
import org.team1515.morteam.MorTeam;
import org.team1515.morteam.R;
import org.team1515.morteam.network.CookieJsonRequest;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void register(View view) {
        final Button registerButton = (Button) findViewById(R.id.register_registerButton);

        //Add all user info to post parameters
        JSONObject params = new JSONObject();

        try {
            EditText firstnameBox = (EditText) findViewById(R.id.register_firstName);
            params.put("firstname", firstnameBox.getText().toString());

            EditText lastnameBox = (EditText) findViewById(R.id.register_lastName);
            params.put("lastname", lastnameBox.getText().toString());

            EditText usernameBox = (EditText) findViewById(R.id.register_username);
            params.put("username", usernameBox.getText().toString());

            EditText passwordBox = (EditText) findViewById(R.id.register_password);
            params.put("password", passwordBox.getText().toString());

            EditText confirmPasswordBox = (EditText) findViewById(R.id.register_passwordConfirm);
            params.put("password_confirm", confirmPasswordBox.getText().toString());

            EditText emailBox = (EditText) findViewById(R.id.register_email);
            params.put("email", emailBox.getText().toString());

            EditText phonenumberBox = (EditText) findViewById(R.id.register_phone);
            params.put("phone", phonenumberBox.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        CookieJsonRequest registerRequest = new CookieJsonRequest(
                Request.Method.POST,
                "/users",
                params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                        builder.setTitle("Registered Successfully!");
                        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });
                        builder.show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle register errors
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                builder.setPositiveButton("Okay", null);


                NetworkResponse response = error.networkResponse;
                if (response != null) {
                    if (response.statusCode == 400) {
                        String message = new String(response.data);
                        if (message.equals("Username is taken")) {
                            builder.setTitle("Failed to register");
                            builder.setMessage("The username you entered is already taken.");
                        } else if (message.equals("Email is taken")) {
                            builder.setTitle("Failed to register");
                            builder.setMessage("The email you entered is already taken.");
                        } else if (message.equals("Phone number is taken")) {
                            builder.setTitle("Failed to register");
                            builder.setMessage("The phone number you entered is already taken.");
                        } else if (message.equals("Invalid email")) {
                            builder.setTitle("Failed to register");
                            builder.setMessage("Please make sure you have entered a valid email address.");
                        } else if (message.equals("Invalid phone number")) {
                            builder.setTitle("Failed to register");
                            builder.setMessage("Please make sure you have entered a valid phone number.");
                        } else if (message.equals("Invalid user info")) {
                            builder.setTitle("Failed to register");
                            builder.setMessage("Please make sure you have entered a valid name, username, and password.");
                        }
                    } else {
                        builder.setTitle("Cannot connect to server");
                        builder.setMessage("Please make sure you have a stable internet connection.");
                    }
                } else {
                    builder.setTitle("Cannot connect to server");
                    builder.setMessage("Please make sure you have a stable internet connection.");
                }

                builder.create().show();

                registerButton.setClickable(true);
            }
        });
        MorTeam.queue.add(registerRequest);
    }
}
