package org.team1515.morteam.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.team1515.morteam.MorTeam;
import org.team1515.morteam.R;
import org.team1515.morteam.entity.User;
import org.team1515.morteam.network.CookieRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubdivisionActivity extends AppCompatActivity {

    SharedPreferences preferences;
    RequestQueue queue;

    String name;
    String id;

    RecyclerView userList;
    UserListAdapter userAdapter;
    List<User> users;

    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subdivision);


        preferences = getSharedPreferences(null, 0);
        queue = Volley.newRequestQueue(this);

        progress = (ProgressBar) findViewById(R.id.subdivision_loading);
        progress.getIndeterminateDrawable().setColorFilter(Color.rgb(255, 197, 71), android.graphics.PorterDuff.Mode.MULTIPLY);



        userList = (RecyclerView) findViewById(R.id.subdivision_userlist);
        LinearLayoutManager userLayoutManager = new LinearLayoutManager(this);
        userList.setLayoutManager(userLayoutManager);
        userAdapter = new UserListAdapter();
        userList.setAdapter(userAdapter);
        users = new ArrayList<>();

        Intent intent = getIntent();
        name = intent.getStringExtra("name");

        //Set up action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(name);
        }


        //Get users in subdivision/team
        Map<String, String> params = new HashMap<>();
        String path;
        if (intent.getBooleanExtra("isTeam", false)) {
            path = "/teams/current/users";
        } else {
            id = intent.getStringExtra("id");
            path = "/groups/normal/id/" + id + "/users";

        }

        CookieRequest userRequest = new CookieRequest(
                Request.Method.GET,
                path,
                params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        users = new ArrayList<>();
                        try {
                            JSONArray userArray = new JSONArray(response);
                            for (int i = 0; i < userArray.length(); i++) {
                                JSONObject userObject = userArray.getJSONObject(i);
                                String profPicPath = userObject.getString("profpicpath") + "-60";
                                profPicPath = profPicPath.replace(" ", "+");
                                final User user = new User(userObject.getString("firstname"),
                                        userObject.getString("lastname"),
                                        userObject.getString("_id"),
                                        profPicPath);

                                users.add(user);

                                progress.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error);
                    }
                }
        );
        queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
            @Override
            public void onRequestFinished(Request<Object> request) {
                userAdapter.setUsers(users);
            }
        });
        queue.add(userRequest);
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {
        private List<User> users;

        public UserListAdapter() {
            this.users = new ArrayList<>();
        }

        public void setUsers(List<User> users) {
            this.users = users;
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout layout;

            public ViewHolder(LinearLayout layout) {
                super(layout);
                this.layout = layout;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout layout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_user, parent, false);
            ViewHolder viewHolder = new ViewHolder(layout);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final User currentUser = users.get(position);

            NetworkImageView icon = (NetworkImageView) holder.layout.findViewById(R.id.userlist_icon);
            MorTeam.setNetworkImage(currentUser.getProfPicPath(), icon);

            TextView name = (TextView) holder.layout.findViewById(R.id.userlist_name);
            name.setText(currentUser.getFullName());

            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SubdivisionActivity.this, ProfileActivity.class);
                    intent.putExtra("_id", currentUser.getId());
                    if (currentUser.getId().equals(preferences.getString("_id", ""))) {
                        intent.putExtra("isCurrentUser", true);
                    } else {
                        intent.putExtra("isCurrentUser", false);
                    }
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return users.size();
        }


    }
}
