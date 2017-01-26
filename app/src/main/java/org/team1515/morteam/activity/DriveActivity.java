package org.team1515.morteam.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.team1515.morteam.MorTeam;
import org.team1515.morteam.R;
import org.team1515.morteam.adapter.DriveFileAdapter;
import org.team1515.morteam.entity.File;
import org.team1515.morteam.network.CookieRequest;

import java.util.ArrayList;
import java.util.List;

public class DriveActivity extends AppCompatActivity {
    private String folderName;
    private String folderId;

    private RecyclerView fileList;
    private DriveFileAdapter fileAdapter;
    private LinearLayoutManager fileLayoutManager;

    private List<File> files;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);

        files = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        Intent intent = getIntent();
        folderName = intent.getStringExtra("name");
        folderId = intent.getStringExtra("_id");

        fileList = (RecyclerView) findViewById(R.id.drive_files);
        fileAdapter = new DriveFileAdapter();
        fileLayoutManager = new LinearLayoutManager(this);
        fileList.setLayoutManager(fileLayoutManager);
        fileList.setAdapter(fileAdapter);

        getFiles();
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void getFiles() {
        CookieRequest fileRequest = new CookieRequest(
                Request.Method.GET,
                "/folders/id/" + folderId + "/files",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                File file = new File(jsonObject.getString("_id"), jsonObject.getString("created_at"), jsonObject.getString("updated_at"), jsonObject.getString("name"), jsonObject.getString("originalName"), jsonObject.getInt("size"), jsonObject.getString("type"), jsonObject.getString("mimetype"), jsonObject.getString("creator"));

                                files.add(file);
                            }

                            fileAdapter.setFiles(files);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );
        MorTeam.queue.add(fileRequest);
    }
}
