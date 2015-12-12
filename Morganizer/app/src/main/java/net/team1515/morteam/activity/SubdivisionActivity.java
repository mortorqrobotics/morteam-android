package net.team1515.morteam.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import net.team1515.morteam.R;
import net.team1515.morteam.network.CookieRequest;
import net.team1515.morteam.network.ImageCookieRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subdivision);


        preferences = getSharedPreferences(null, 0);
        queue = Volley.newRequestQueue(this);


        //Set up action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        userList = (RecyclerView) findViewById(R.id.subdivision_userlist);
        LinearLayoutManager userLayoutManager = new LinearLayoutManager(this);
        userList.setLayoutManager(userLayoutManager);
        userAdapter = new UserListAdapter();
        userList.setAdapter(userAdapter);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        id = intent.getStringExtra("id");

        //Get users in subdivision
        Map<String, String> params = new HashMap<>();
        params.put("subdivision_id", id);
        CookieRequest userRequest = new CookieRequest(Request.Method.POST,
                "/f/getUsersInSubdivision",
                params,
                preferences,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                users = new ArrayList<>();
                try {
                    JSONArray userArray = new JSONArray(response);
                    for(int i = 0; i < userArray.length(); i++) {
                        JSONObject userObject = userArray.getJSONObject(i);
                        String profPicPath = userObject.getString("profpicpath") + "-60";
                        profPicPath = profPicPath.replace(" ", "%20");
                        final User user = new User(userObject.getString("firstname") + " " + userObject.getString("lastname"),
                                userObject.getString("_id"),
                                profPicPath);

                        ImageCookieRequest profPicRequest = new ImageCookieRequest("http://www.morteam.com" + profPicPath,
                                preferences,
                                new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap response) {
                                        user.setProfPic(response);
                                        users.add(user);
                                    }
                                }, 0, 0, null, Bitmap.Config.RGB_565,
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        users.add(user);
                                        System.out.println(user.profPicPath + " does not work correctly. Will be fixed soon...hopefully.");
                                        System.out.println(error);
                                    }
                                }
                        );
                        queue.add(profPicRequest);
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
        });
        queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
            @Override
            public void onRequestFinished(Request<Object> request) {
                userAdapter.setUsers(users);
            }
        });
        queue.add(userRequest);

        TextView subName = (TextView) findViewById(R.id.subdivision_name);
        subName.setText(name);
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
            LinearLayout layout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.userlist_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(layout);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final User currentUser = users.get(position);

            final ImageView icon = (ImageView) holder.layout.findViewById(R.id.userlist_icon);
            icon.setImageBitmap(currentUser.profPic);

            TextView name = (TextView) holder.layout.findViewById(R.id.userlist_name);
            name.setText(currentUser.name);

            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: implement user activity
                    /*
                    Intent intent = new Intent(SubdivisionActivity.this, UserActivity.class);
                    intent.putExtra("name", currentUser.name);
                    intent.putExtra("id", currentUser.id);
                    startActivity(intent);
                    */
                }
            });
        }

        @Override
        public int getItemCount() {
            return users.size();
        }


    }

    public class User {
        public String name;
        public String id;
        public String profPicPath;
        public Bitmap profPic;

        public User(String name, String id, String profPicPath) {
            this.name = name;
            this.id = id;
            this.profPicPath = profPicPath;
            profPic = null;
        }

        public void setProfPic(Bitmap profPic) {
            this.profPic = profPic;
        }
    }
}