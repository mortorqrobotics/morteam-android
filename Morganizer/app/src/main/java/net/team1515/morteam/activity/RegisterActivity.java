package net.team1515.morteam.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    SharedPreferences preferences;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(null, 0);
        queue = Volley.newRequestQueue(this);

        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void registerPressed(View view){
        final Button registerButton = (Button)findViewById(R.id.register_button);

        //Add all user info to post parameters
        Map<String, String> params = new HashMap<>();

        EditText firstnameBox = (EditText)findViewById(R.id.register_firstname);
        params.put("firstname", firstnameBox.getText().toString());

        EditText lastnameBox = (EditText)findViewById(R.id.register_lastname);
        params.put("lastname", lastnameBox.getText().toString());

        EditText usernameBox = (EditText)findViewById(R.id.register_username);
        params.put("username", usernameBox.getText().toString());

        EditText passwordBox = (EditText)findViewById(R.id.register_password);
        params.put("password", passwordBox.getText().toString());

        EditText confirmPasswordBox = (EditText)findViewById(R.id.register_confirmpassword);
        params.put("password_confirm", confirmPasswordBox.getText().toString());

        EditText emailBox = (EditText)findViewById(R.id.register_email);
        params.put("email", emailBox.getText().toString());

        EditText phonenumberBox = (EditText)findViewById(R.id.register_phone);
        params.put("phone", phonenumberBox.getText().toString());

        CookieRequest registerRequest = new CookieRequest(Request.Method.POST, "/f/createUser",
                params,
                preferences,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equals("success")) {
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    try {
                        finalize();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setTitle("One or more of the inputted information is incorrect");
                    builder.setPositiveButton("Okay", null);
                    builder.create().show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                builder.setTitle("Please make sure you are connected to the internet");
                builder.setPositiveButton("Okay", null);
                builder.create().show();
            }
        });
        queue.add(registerRequest);
    }

}
