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

        preferences = getSharedPreferences(null, 0);
        queue = Volley.newRequestQueue(this);

        for (Object data : preferences.getAll().keySet()) {
            System.out.println(data + "\t" + preferences.getAll().get(data));
        }

        for (String data : userData) {
            if (!preferences.contains(data)) {
                System.out.println(data);
                // If not logged in, bring to login page and clear data
                preferences.edit().clear().apply();
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
                            System.out.println(response);
                            JSONObject userObject = new JSONObject(response);

                            SharedPreferences.Editor editor = preferences.edit();
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
