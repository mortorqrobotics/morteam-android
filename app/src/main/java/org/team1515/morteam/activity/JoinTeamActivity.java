package org.team1515.morteam.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import net.team1515.morteam.R;
import org.team1515.morteam.network.CookieRequest;

import java.util.HashMap;
import java.util.Map;

public class JoinTeamActivity extends AppCompatActivity {
    SharedPreferences preferences;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(null, 0);
        queue = Volley.newRequestQueue(this);

        setContentView(R.layout.activity_jointeam);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void joinTeamClicked(View view) {
        EditText teamID = (EditText)findViewById(R.id.jointeam_id);
        Map<String, String> params = new HashMap<>();
        params.put("team_id", teamID.getText().toString());
        CookieRequest joinTeamRequest = new CookieRequest(Request.Method.POST,
                "/f/joinTeam",
                params,
                preferences,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(response.equals("success")) {
                    preferences.edit().
                            putBoolean("isOnTeam", true)
                            .apply();
                    Intent intent = new Intent(JoinTeamActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else if(response.equals("no such team")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(JoinTeamActivity.this);
                    builder.setTitle("The team does not exists");
                    builder.setMessage("Make sure you inputted the correct team id.");
                    builder.setPositiveButton("Okay", null);
                    builder.create().show();
                } else if(response.equals("banned")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(JoinTeamActivity.this);
                    builder.setTitle("You have been banned from this team");
                    builder.setMessage("Contact the team owner if this is an error.");
                    builder.setPositiveButton("Okay", null);
                    builder.create().show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                AlertDialog.Builder builder = new AlertDialog.Builder(JoinTeamActivity.this);
                builder.setTitle("Error contacting server");
                builder.setPositiveButton("Okay", null);
                builder.create().show();
            }
        });
        queue.add(joinTeamRequest);
    }
}
