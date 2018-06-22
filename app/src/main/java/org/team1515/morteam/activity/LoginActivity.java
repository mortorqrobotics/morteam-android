package org.team1515.morteam.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;
import org.team1515.morteam.MorTeam;
import org.team1515.morteam.R;
import org.team1515.morteam.network.CookieJsonRequest;
import org.team1515.morteam.network.NetworkUtils;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    static final String TAG = "LoginActivity";

    //TODO: Fix this (Fix what?)
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

            Log.i(TAG, data + " : " + MorTeam.preferences.getString(data, "DEFAULT"));
        }

        if (!MorTeam.cookieStore.foundCookies()) {
            MorTeam.preferences.edit().clear().apply();
            setContentView(R.layout.activity_login);
            return;
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

        CheckBox rememberMe = (CheckBox) findViewById(R.id.login_rememberMe);

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
            params.put("rememberMe", rememberMe.isChecked());
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

                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();

                            AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
                            dialog.setTitle("Error retrieving data from server");
                            dialog.setMessage("Please try again later or contact the developers for assistance.");
                            dialog.setPositiveButton("Okay", null);
                            dialog.show();
                        } finally {
                            loginButton.setClickable(true);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();

                        Map<String, Pair<String, String>> errors = new HashMap<>();
                        errors.put("Invalid login credentials",
                                new Pair<>("Invalid login",
                                        "Please make sure you entered the correct username and password"));
                        NetworkUtils.catchNetworkError(LoginActivity.this, error.networkResponse, errors);

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