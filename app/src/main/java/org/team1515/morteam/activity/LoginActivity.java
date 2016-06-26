package org.team1515.morteam.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import net.team1515.morteam.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.team1515.morteam.network.CookieRequest;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    SharedPreferences preferences;
    RequestQueue queue;

    public static final String[] userData = {
            "_id",
            "username",
            "fistname",
            "lastname",
            "email",
            "phone",
            "profpicpath",
            "position",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(null, 0);
        queue = Volley.newRequestQueue(this);

        for (String data : userData) {
            if (!preferences.contains(data) || !preferences.getBoolean("isOnTeam", false)) {
                // If not logged in, bring to login page and clear data
                System.out.println(data);
                System.out.println(preferences.getString("_id", "nope"));
                System.out.println(preferences.getBoolean("isOnTeam", false));
//                preferences.edit().clear().apply();
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
        }

        if (password.trim().isEmpty()) {
            passwordView.setText("");
            passwordView.setHintTextColor(Color.RED);
            isEmpty = true;
        }

        if (isEmpty) {
            loginButton.setEnabled(true);
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        CookieRequest loginRequest = new CookieRequest(
                Request.Method.POST,
                "/login",
                params,
                preferences,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject userObject = new JSONObject(response);
                            JSONArray teamArray = userObject.getJSONArray("teams");

                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("_id", userObject.getString("_id"))
                                    .putString("username", userObject.getString("username"))
                                    .putString("firstname", userObject.getString("firstname"))
                                    .putString("lastname", userObject.getString("lastname"))
                                    .putString("email", userObject.getString("email"))
                                    .putString("phone", userObject.getString("phone"))
                                    .putString("profpicpath", userObject.getString("profpicpath"));

                            Intent intent = new Intent();

                            if (teamArray.length() <= 0) {
                                editor.putBoolean("isOnTeam", false).apply();
                                intent.setClass(LoginActivity.this, JoinTeamActivity.class);
                            } else {
                                editor.putBoolean("isOnTeam", true)
                                        .putString("position", userObject.getJSONObject("current_team").getString("position"))
                                        .apply();
                                intent.setClass(LoginActivity.this, MainActivity.class);
                            }

                            loginButton.setClickable(true);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setTitle("Cannot connect to server");
                        builder.setMessage("Please make sure you have a working internet connection.");
                        builder.setPositiveButton("Okay", null);
                        builder.create().show();

                        loginButton.setClickable(true);
                    }
                });
        queue.add(loginRequest);
    }

    public void register(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
