package org.team1515.morteam.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

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

    private TextView folderNameView;

    private RecyclerView fileList;
    private DriveFileAdapter fileAdapter;
    private LinearLayoutManager fileLayoutManager;

    private List<File> files;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);

        files = new ArrayList<>();

        Intent intent = getIntent();
        folderName = intent.getStringExtra("name");
        folderId = intent.getStringExtra("_id");

        folderNameView = (TextView) findViewById(R.id.file_folder_name);
        folderNameView.setText(folderName);

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

    public void newFile(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("3");
        if (requestCode == 1) {
            System.out.println("2");
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)

                System.out.println("1");
            }
        }
    }
}
