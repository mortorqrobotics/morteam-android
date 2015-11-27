package net.team1515.morteam.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import net.team1515.morteam.R;
import net.team1515.morteam.network.CookieRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(null, 0);

        String sessionId = preferences.getString(CookieRequest.SESSION_COOKIE, "");
        boolean isOnTeam = preferences.getBoolean("isOnTeam", false);
        if (!sessionId.isEmpty()) {
            if(isOnTeam) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();

            } else {
                Intent intent = new Intent(LoginActivity.this, JoinTeamActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            setContentView(R.layout.activity_login);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        }
    }

    public void loginPressed(View view) {
        //Make sure the user cannot press the button twice
        final Button loginButton = (Button) findViewById(R.id.login_loginbutton);
        loginButton.setClickable(false);

        EditText userBox = (EditText) findViewById(R.id.login_username);
        EditText passBox = (EditText) findViewById(R.id.login_password);
        final String user = userBox.getText().toString();
        final String pass = passBox.getText().toString();

        if (user.isEmpty() || pass.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Please enter a valid username/password");
            builder.setPositiveButton("Okay", null);
            builder.create().show();

            loginButton.setClickable(true);
        } else {
            RequestQueue queue = Volley.newRequestQueue(this);

            Map<String, String> params = new HashMap<>();
            params.put("username", user);
            params.put("password", pass);
            CookieRequest stringRequest = new CookieRequest(Request.Method.POST, "/f/login", params, preferences, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject json = new JSONObject(response);
                        System.out.println(json);
                        JSONArray teams = json.getJSONArray("teams");
                        preferences.edit()
                                .putString("_id", json.getString("_id"))
                                .putString("username", json.getString("username"))
                                .putString("firstname", json.getString("firstname"))
                                .putString("lastname", json.getString("lastname"))
                                .putString("email", json.getString("email"))
                                .putString("profpicpath", json.getString("profpicpath"))
                                .putString("position", json.getJSONObject("current_team").getString("position"))
                                .apply();
                        if(teams.length() <= 0) {
                            preferences.edit().putBoolean("isOnTeam", false).apply();
                            Intent intent = new Intent(LoginActivity.this, JoinTeamActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            preferences.edit().putBoolean("isOnTeam", true).apply();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } catch (JSONException e) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setTitle("Incorrect username or password");
                        builder.setPositiveButton("Okay", null);
                        builder.create().show();

                        loginButton.setClickable(true);
                    }
                }
            }, new Response.ErrorListener() {
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
            queue.add(stringRequest);
        }
    }

    public void registerPressed(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
