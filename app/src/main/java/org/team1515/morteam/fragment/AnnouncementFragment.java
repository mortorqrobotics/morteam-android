package org.team1515.morteam.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import net.team1515.morteam.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.team1515.morteam.adapter.AnnouncementAdapter;
import org.team1515.morteam.entity.Announcement;
import org.team1515.morteam.entity.User;
import org.team1515.morteam.network.CookieRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnouncementFragment extends Fragment {

    private RequestQueue queue;
    public SharedPreferences preferences;

    private RecyclerView announcementView;
    private AnnouncementAdapter announcementAdapter;
    private LinearLayoutManager announcementLayoutManager;

    private SwipeRefreshLayout refreshLayout;
    private ProgressBar progress;
    private TextView errorView;

    private List<Announcement> announcements;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        preferences = getActivity().getSharedPreferences(null, 0);
        queue = Volley.newRequestQueue(getContext());

        announcementView = (RecyclerView) view.findViewById(R.id.announcement_view);
        announcementLayoutManager = new LinearLayoutManager(getActivity());
        announcementAdapter = new AnnouncementAdapter(this);
        announcementView.setLayoutManager(announcementLayoutManager);
        announcementView.setAdapter(announcementAdapter);

        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        refreshLayout.setColorSchemeResources(R.color.orange_theme);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAnnouncements();
            }
        });

        progress = (ProgressBar) view.findViewById(R.id.announcement_loading);
        progress.getIndeterminateDrawable().setColorFilter(Color.rgb(255, 197, 71), android.graphics.PorterDuff.Mode.MULTIPLY);
        errorView = (TextView) view.findViewById(R.id.announcement_error);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getAnnouncements();
    }

    public void getAnnouncements() {
        errorView.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        CookieRequest announcementRequest = new CookieRequest(
                Request.Method.GET,
                "/announcements",
                preferences,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray announcementArray = new JSONArray(response);

                            announcements = new ArrayList<>();

                            for (int i = 0; i < announcementArray.length(); i++) {
                                JSONObject object = announcementArray.getJSONObject(i);
                                Announcement announcement = new Announcement(
                                        new User(
                                                object.getJSONObject("author").getString("firstname"),
                                                object.getJSONObject("author").getString("lastname"),
                                                object.getJSONObject("author").getString("_id"),
                                                object.getJSONObject("author").getString("profpicpath") + "-60"
                                        ),
                                        object.getString("content"),
                                        object.getString("timestamp"),
                                        object.getString("_id")
                                );

                                announcements.add(announcement);
                            }

                            announcementAdapter.setAnnouncements(announcements);

                            progress.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progress.setVisibility(View.GONE);
                            if (announcements.isEmpty()) {
                                errorView.setVisibility(View.VISIBLE);
                            }
                        } finally {
                            //Tell adapter to update once request is finished
                            //Do so whether it fails or succeeds
                            refreshLayout.setRefreshing(false);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        refreshLayout.setRefreshing(false);
                        progress.setVisibility(View.GONE);

                        if (announcements.isEmpty()) {
                            errorView.setVisibility(View.VISIBLE);
                        }

                        Toast.makeText(getContext(), "Error connecting to the server. Try checking your internet connection and try again later.", Toast.LENGTH_SHORT).show();
                    }
                });
        queue.add(announcementRequest);
    }

    public void deleteAnnouncement(final String id, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Are you sure you want to delete?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                CookieRequest request = new CookieRequest(Request.Method.DELETE, "/announcements/id/" + id, preferences, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        announcements.remove(position);
                        announcementAdapter.notifyItemRemoved(position);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error);
                    }
                });

                queue.add(request);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }
}
