package org.team1515.morteam.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class DriveActivity extends AppCompatActivity {
    private String folderName;
    private String folderId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        folderName = intent.getStringExtra("name");
        folderId = intent.getStringExtra("_id");
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
