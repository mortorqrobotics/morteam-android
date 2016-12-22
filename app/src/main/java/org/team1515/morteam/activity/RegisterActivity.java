package org.team1515.morteam.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void register(View view) {
        final Button registerButton = (Button) findViewById(R.id.register_registerButton);

        //Add all user info to post parameters
        JSONObject params = new JSONObject();
        try {
            EditText passwordBox = (EditText) findViewById(R.id.register_password);
            params.put("password", passwordBox.getText().toString());

            EditText confirmPasswordBox = (EditText) findViewById(R.id.register_passwordConfirm);
            params.put("password_confirm", confirmPasswordBox.getText().toString());

            // Make sure passwords match before proceeding
            if (!passwordBox.getText().toString().equals(confirmPasswordBox.getText().toString())) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("Failed to log in");
                dialog.setMessage("Please make sure your passwords match.");
                dialog.setPositiveButton("Okay", null);
                dialog.show();
                return;
            }

            EditText firstNameBox = (EditText) findViewById(R.id.register_firstName);
            params.put("firstname", firstNameBox.getText().toString());

            EditText lastNameBox = (EditText) findViewById(R.id.register_lastName);
            params.put("lastname", lastNameBox.getText().toString());

            EditText usernameBox = (EditText) findViewById(R.id.register_username);
            params.put("username", usernameBox.getText().toString());

            EditText emailBox = (EditText) findViewById(R.id.register_email);
            params.put("email", emailBox.getText().toString());

            EditText phoneNumberBox = (EditText) findViewById(R.id.register_phone);
            params.put("phone", phoneNumberBox.getText().toString());
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
                error.printStackTrace();

                Map<String, Pair<String, String>> errors = new HashMap<>();
                errors.put("Username is taken",
                        new Pair<>("Failed to register",
                                "The username you entered is already taken."));
                errors.put("Email is taken",
                        new Pair<>("Failed to register",
                                "The email you entered is already taken."));
                errors.put("Phone number is taken",
                        new Pair<>("Failed to register",
                                "The phone number you entered is already taken."));
                errors.put("Invalid user info",
                        new Pair<>("Failed to register",
                                "Please make sure you have entered a valid name, username, and password."));
                errors.put("Invalid email",
                        new Pair<>("Failed to register",
                                "Please make sure you have entered a valid email address."));
                errors.put("Invalid phone number",
                        new Pair<>("Failed to register",
                                "Please make sure you have entered a valid phone number."));

                NetworkUtils.catchNetworkError(RegisterActivity.this, error.networkResponse, errors);

                registerButton.setClickable(true);
            }
        });
        MorTeam.queue.add(registerRequest);
    }
}
