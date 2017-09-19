package org.team1515.morteam.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.team1515.morteam.MorTeam;
import org.team1515.morteam.R;
import org.team1515.morteam.adapter.DriveFolderAdapter;
import org.team1515.morteam.entity.Folder;
import org.team1515.morteam.entity.User;
import org.team1515.morteam.network.CookieRequest;

import java.util.ArrayList;
import java.util.List;

public class DriveFragment extends Fragment {
    private RecyclerView driveList;
    private DriveFolderAdapter driveAdapter;
    private LinearLayoutManager driveLayoutManager;

    private SwipeRefreshLayout refreshLayout;

    private ProgressBar progress;
    private TextView errorView;

    private List<Folder> drive;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drive, container, false);

        drive = new ArrayList<>();

        driveList = (RecyclerView) view.findViewById(R.id.drive_folders);
        driveLayoutManager = new LinearLayoutManager(getContext());
        driveAdapter = new DriveFolderAdapter(this, getContext(), drive);
        driveList.setLayoutManager(driveLayoutManager);
        driveList.setAdapter(driveAdapter);

        getFolders();

        return view;
    }

    public void getFolders() {
        CookieRequest folderRequests = new CookieRequest(
                Request.Method.GET,
                "/folders",
                true,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                Log.i("THING", jsonObject.toString());


                                Folder folder;
                                try {
                                    folder = new Folder(jsonObject.getString("_id"), jsonObject.getString("created_at"), jsonObject.getString("updated_at"), jsonObject.getString("name"), jsonObject.getBoolean("defaultFolder"), new User(jsonObject.getString("creator")));
                                } catch (JSONException e) {
                                    Log.e("THING", "", e);
                                    folder = new Folder(jsonObject.getString("_id"), jsonObject.getString("created_at"), jsonObject.getString("updated_at"), jsonObject.getString("name"), jsonObject.getBoolean("defaultFolder"), null);
                                }

                                //TODO: Audience stuffs?

                                drive.add(folder);
                            }

                            driveAdapter.setDrive(drive);
                            driveAdapter.notifyDataSetChanged();
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
        MorTeam.queue.add(folderRequests);
    }
}