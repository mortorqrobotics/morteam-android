package org.team1515.morteam.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

public class LoginActivity extends AppCompatActivity {

    public static final String[] userData = {
            "_id",
            "username",
            "firstname",
            "lastname",
            "email",
            "phone",
            "profpicpath",
            "position",
            "team_id",
            "teamNumber",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for (String data : userData) {
            if (!MorTeam.preferences.contains(data)) {
                // If not logged in, bring to login page and clear data
                MorTeam.preferences.edit().clear().apply();
                setContentView(R.layout.activity_login);
                return;
            }
        }

        // If all values are present, proceed to main activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void login(View view) {
        //Make sure the user cannot press the button twice
        final Button loginButton = (Button) findViewById(R.id.login_loginButton);
        loginButton.setClickable(false);

        EditText usernameView = (EditText) findViewById(R.id.login_username);
        EditText passwordView = (EditText) findViewById(R.id.login_password);
        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        // Make sure text boxes are not blank
        boolean isEmpty = false;
        if (username.trim().isEmpty()) {
            usernameView.setText("");
            usernameView.setHintTextColor(Color.RED);
            isEmpty = true;
        }

        if (password.trim().isEmpty()) {
            passwordView.setText("");
            passwordView.setHintTextColor(Color.RED);
            isEmpty = true;
        }

        if (isEmpty) {
            loginButton.setClickable(true);
            return;
        }

        JSONObject params = new JSONObject();
        try {
            params.put("username", username);
            params.put("password", password);
            params.put("rememberMe", true);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        CookieJsonRequest loginRequest = new CookieJsonRequest(
                Request.Method.POST,
                "/login",
                params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject userObject) {
                        try {
                            SharedPreferences.Editor editor =  MorTeam.preferences.edit();
                            editor.putString("_id", userObject.getString("_id"))
                                    .putString("username", userObject.getString("username"))
                                    .putString("firstname", userObject.getString("firstname"))
                                    .putString("lastname", userObject.getString("lastname"))
                                    .putString("email", userObject.getString("email"))
                                    .putString("phone", userObject.getString("phone"))
                                    .putString("profpicpath", userObject.getString("profpicpath"));

                            String position = userObject.getString("position");
                            Intent intent = new Intent();

                            if (userObject.has("team")) {
                                JSONObject teamObject = userObject.getJSONObject("team");
                                editor.putString("team_id", teamObject.getString("_id"))
                                        .putString("teamNumber", teamObject.getString("number"))
                                        .putString("position", userObject.getString("position"));

                                intent.setClass(LoginActivity.this, MainActivity.class);
                            } else {
                                intent.setClass(LoginActivity.this, JoinTeamActivity.class);
                            }

                            editor.apply();

                            loginButton.setClickable(true);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setTitle("Incorrect username or password");
                            builder.setPositiveButton("Okay", null);
                            builder.create().show();

                            loginButton.setClickable(true);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();

                        // Handle login errors
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setPositiveButton("Okay", null);

                        NetworkResponse response = error.networkResponse;
                        if (response != null) {
                            if (response.statusCode == 400) {
                                String message = new String(response.data);
                                System.out.println(message);
                                if (message.equals("Invalid login credentials")) {
                                    builder.setTitle("Invalid login");
                                    builder.setMessage("Please make sure you entered the correct username and password.");
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

                        loginButton.setClickable(true);
                    }
                }
        );
        MorTeam.queue.add(loginRequest);
    }

    public void register(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}