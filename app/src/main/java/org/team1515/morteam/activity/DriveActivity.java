package org.team1515.morteam.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.team1515.morteam.MorTeam;
import org.team1515.morteam.PathHandler;
import org.team1515.morteam.R;
import org.team1515.morteam.adapter.DriveFileAdapter;
import org.team1515.morteam.entity.MorFile;
import org.team1515.morteam.network.CookieRequest;
import org.team1515.morteam.network.MultipartUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriveActivity extends AppCompatActivity {
    private String folderName;
    private String folderId;

    private TextView folderNameView;

    private RecyclerView fileList;
    private DriveFileAdapter fileAdapter;
    private LinearLayoutManager fileLayoutManager;

    private List<MorFile> files;

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

                                MorFile file = new MorFile(jsonObject.getString("_id"), jsonObject.getString("created_at"), jsonObject.getString("updated_at"), jsonObject.getString("name"), jsonObject.getString("originalName"), jsonObject.getInt("size"), jsonObject.getString("type"), jsonObject.getString("mimetype"), jsonObject.getString("creator"), folderName);

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
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String fileName;

                Uri uri = data.getData();
                String mimeType = getContentResolver().getType(uri);

                if (mimeType == null) {
                    String path = PathHandler.getPath(this, uri);
                    File file = new File(path);
                    fileName = file.getName();
                } else {
                    Uri returnUri = data.getData();
                    Cursor returnCursor = getContentResolver().query(returnUri, null, null, null, null);
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    fileName = returnCursor.getString(nameIndex);
                }

                File fileSave = getExternalFilesDir(null);
                String sourcePath = getExternalFilesDir(null).toString();

                try {
                    copyFileStream(new File(sourcePath + "/" + fileName), uri, this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void copyFileStream(File dest, Uri uri, Context context) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            is.close();
            os.close();
        }
    }

//    public void deleteFile(final String id, final int position) {
//
//    }
}
