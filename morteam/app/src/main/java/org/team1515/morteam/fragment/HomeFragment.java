package org.team1515.morteam.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import net.team1515.morteam.R;

import org.team1515.morteam.activity.ProfileActivity;
import org.team1515.morteam.entities.Announcement;
import org.team1515.morteam.entities.User;
import org.team1515.morteam.network.CookieRequest;
import org.team1515.morteam.network.ImageCookieRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private RequestQueue queue;
    private SharedPreferences preferences;
    private AnnouncementAdapter announcementAdapter;
    private SwipeRefreshLayout refreshLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        preferences = getActivity().getSharedPreferences(null, 0);
        queue = Volley.newRequestQueue(getContext());
        queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
            @Override
            public void onRequestFinished(Request<Object> request) {
                announcementAdapter.notifyDataSetChanged();
            }
        });

        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.announcement_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        announcementAdapter = new AnnouncementAdapter();
        recyclerView.setAdapter(announcementAdapter);

        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        refreshLayout.setColorSchemeResources(R.color.orange_theme);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestAnnouncements();
            }
        });

        return view;
    }


    public void requestAnnouncements() {
        announcementAdapter.requestAnnouncements();
    }

    public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

        private CookieRequest announcementsRequest;
        private List<Announcement> announcements;

        public AnnouncementAdapter() {
            announcements = new ArrayList<>();
            requestAnnouncements();
        }

        public void requestAnnouncements() {
            announcementsRequest = new CookieRequest(Request.Method.POST, "/f/getAnnouncementsForUser", preferences, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray array = new JSONArray(response);
                        announcements = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
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

                            announcement.requestProfPic(queue, preferences, null);

                            announcements.add(announcement);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        //Tell adapter to update once request is finished
                        //Do so whether it fails or succeeds
                        refreshLayout.setRefreshing(false);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    refreshLayout.setRefreshing(false);
                    System.out.println(error);
                }
            });
            queue.add(announcementsRequest);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public CardView cardView;

            public ViewHolder(CardView cardView) {
                super(cardView);
                this.cardView = cardView;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CardView view = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_announcement, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final Announcement currentAnnouncement = announcements.get(position);

            ImageView profPic = (ImageView) holder.cardView.findViewById(R.id.announcement_pic);
            profPic.setImageBitmap(currentAnnouncement.getProfPic());
            profPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeFragment.this.getContext(), ProfileActivity.class);
                    intent.putExtra("_id", currentAnnouncement.getUserId());
                    if(currentAnnouncement.getUserId().equals(preferences.getString("_id", ""))) {
                        intent.putExtra("isCurrentUser", true);
                    } else {
                        intent.putExtra("isCurrentUser", false);
                    }
                    startActivity(intent);

                }
            });

            TextView author = (TextView) holder.cardView.findViewById(R.id.author);
            author.setText(currentAnnouncement.getUserName());

            TextView date = (TextView) holder.cardView.findViewById(R.id.date);
            date.setText(currentAnnouncement.getDate());

            TextView message = (TextView) holder.cardView.findViewById(R.id.message);
            message.setMovementMethod(LinkMovementMethod.getInstance());
            message.setText(Html.fromHtml(currentAnnouncement.getContent()));

            ImageButton deleteButton = (ImageButton) holder.cardView.findViewById(R.id.delete_button);

            //Don't show delete announcement buttons if not admin
            if (!preferences.getString("position", "").equals("admin")) {
                deleteButton.setClickable(false);
                deleteButton.setVisibility(View.INVISIBLE);
            } else {
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Are you sure you want to delete?");
                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                Map<String, String> params = new HashMap<>();
                                params.put("_id", currentAnnouncement.getId());

                                CookieRequest request = new CookieRequest(Request.Method.POST, "/f/deleteAnnouncement", params, preferences, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        requestAnnouncements();
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
                });
            }
        }

        @Override
        public int getItemCount() {
            return announcements.size();
        }
    }
}
