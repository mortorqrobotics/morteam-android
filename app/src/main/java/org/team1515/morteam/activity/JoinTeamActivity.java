package org.team1515.morteam.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

public class JoinTeamActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jointeam);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    }

    public void joinTeamClicked(View view) {
        EditText teamID = (EditText)findViewById(R.id.joinTeam_id);

        CookieJsonRequest joinTeamRequest = new CookieJsonRequest(Request.Method.POST,
                "/teams/code/" + teamID.getText() + "/join",
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Intent intent = new Intent();
                        try {
                            MorTeam.preferences.edit()
                                    .putBoolean("isOnTeam", true)
                                    .putString("team_id", response.getString("_id"))
                                    .putString("teamNumber", response.getString("number"))
                                    .apply();

                            intent.setClass(JoinTeamActivity.this, MainActivity.class);
                        } catch (JSONException e) {
                            e.printStackTrace();

                            intent.setClass(JoinTeamActivity.this, LoginActivity.class);
                        } finally {
                            startActivity(intent);
                            finish();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                Map<String, Pair<String, String>> errors = new HashMap<>();
                errors.put("You already have a team",
                        new Pair<>("You have already joined a team",
                                "Try closing MorTeam and logging in again."));
                errors.put("Team does not exist",
                        new Pair<>("Team does not exist",
                                "Please make sure you have entered a valid team code."));

                NetworkUtils.catchNetworkError(JoinTeamActivity.this, error.networkResponse, errors);
            }
        });
        MorTeam.queue.add(joinTeamRequest);
    }
}
