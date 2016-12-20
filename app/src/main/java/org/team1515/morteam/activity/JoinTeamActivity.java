package org.team1515.morteam.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

public class JoinTeamActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_jointeam);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void joinTeamClicked(View view) {
        EditText teamID = (EditText)findViewById(R.id.jointeam_id);

        CookieJsonRequest joinTeamRequest = new CookieJsonRequest(Request.Method.POST,
                "/teams/code/" + teamID.getText() + "/join",
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            MorTeam.preferences.edit()
                                    .putBoolean("isOnTeam", true)
                                    .putString("team_id", response.getString("_id"))
                                    .putString("teamNumber", response.getString("number"))
                                    .apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Intent intent = new Intent(JoinTeamActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle team joining errors
                AlertDialog.Builder builder = new AlertDialog.Builder(JoinTeamActivity.this);
                builder.setPositiveButton("Okay", null);

                NetworkResponse response = error.networkResponse;
                if (response != null) {
                    if (response.statusCode == 400) {
                        String message = new String(response.data);
                        System.out.println(message);
                        if (message.equals("You already have a team")) {
                            builder.setTitle("You have already joined a team");
                            builder.setMessage("Try closing MorTeam and logging in again.");
                        }
                    } else {
                        builder.setTitle("Team does not exist");
                        builder.setMessage("Please make sure you have entered a valid team code.");
                    }
                } else {
                    builder.setTitle("Cannot connect to server");
                    builder.setMessage("Please make sure you have a stable internet connection.");
                }

                builder.create().show();
            }
        });
        MorTeam.queue.add(joinTeamRequest);
    }
}
