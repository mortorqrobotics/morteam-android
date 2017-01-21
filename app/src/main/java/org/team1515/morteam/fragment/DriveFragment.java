package org.team1515.morteam.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import org.team1515.morteam.R;
import org.team1515.morteam.adapter.DriveAdapter;
import org.team1515.morteam.entity.Folder;
import org.team1515.morteam.entity.User;
import org.team1515.morteam.network.CookieRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.team1515.morteam.MorTeam.preferences;
import static org.team1515.morteam.MorTeam.queue;

public class DriveFragment extends Fragment {
    private RecyclerView driveList;
    private DriveAdapter driveAdapter;
    private LinearLayoutManager driveLayoutManager;

    private SwipeRefreshLayout refreshLayout;

    private ProgressBar progress;
    private TextView errorView;

    private List<Folder> drive;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drive, container, false);

        drive = new ArrayList<>();

        driveList = (RecyclerView) view.findViewById(R.id.drive_list);
        driveLayoutManager = new LinearLayoutManager(getContext());
        driveAdapter = new DriveAdapter(this, getContext(), drive);
        driveList.setLayoutManager(driveLayoutManager);
        driveList.setAdapter(driveAdapter);

        getFolders();

        return view;
    }

    //Drive requests and what they do:
    //folders: /folders
    //press on folder: /folders/id/:folderID/files

    public void getFolders() {
        CookieRequest folderRequests = new CookieRequest(
                Request.Method.GET,
                "/folders",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                Folder folder = new Folder(jsonObject.getString("name"), jsonObject.getBoolean("defaultFolder"), new User(jsonObject.getString("creator")));

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
        queue.add(folderRequests);
    }
}