package net.team1515.morteam.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import net.team1515.morteam.R;
import net.team1515.morteam.fragment.ChatFragment;
import net.team1515.morteam.fragment.HomeFragment;
import net.team1515.morteam.network.CookieRequest;
import net.team1515.morteam.network.ImageCookieRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String BLANK_PIC_PATH = "/images/user.jpg";

    SectionPagerAdapter sectionPagerAdapter;

    SharedPreferences preferences;
    RequestQueue queue;

    PopupMenu popupMenu;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;
    RecyclerView yourSubList;
    SubdivisionListAdapter yourSubAdapter;
    RecyclerView publicSubList;
    SubdivisionListAdapter publicSubAdapter;

    public static final Map<String, String> teamUsers = new HashMap<>();
    public static final Map<String, String> yourSubs = new HashMap<>();
    public static final Map<String, String> publicSubs = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        preferences = getSharedPreferences(null, 0);
        queue = Volley.newRequestQueue(this);


        //Set up action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Set up action bar profile picture
        final ImageButton profilePic = (ImageButton) toolbar.findViewById(R.id.actionbar_pic);
        profilePic.setClickable(true);
        profilePic.setVisibility(View.VISIBLE);
        String profPicPath = preferences.getString("profpicpath", "");
        if (profPicPath.isEmpty()) {
            profPicPath = BLANK_PIC_PATH;
        } else {
            profPicPath += "-60";
        }
        ImageCookieRequest profilePicRequest = new ImageCookieRequest("http://www.morteam.com" + profPicPath,
                preferences, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                profilePic.setImageBitmap(response);
            }
        }, 0, 0, null, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });
        queue.add(profilePicRequest);

        //Menu
        popupMenu = new PopupMenu(this, profilePic);
        popupMenu.inflate(R.menu.menu_main);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.logout:
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Are you sure you want to logout?");
                        builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CookieRequest logoutRequest = new CookieRequest(Request.Method.POST,
                                        "/f/logout",
                                        preferences,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                preferences.edit().clear().apply();
                                                finish();
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                preferences.edit().clear().apply();
                                                finish();
                                            }
                                        });
                                queue.add(logoutRequest);
                            }
                        });
                        builder.setNegativeButton("Cancel", null);
                        builder.create().show();
                        return true;
                    case R.id.view_profile:
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        startActivity(intent);
                        return true;
                    default:
                        return false;
                }
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        sectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(sectionPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawerlayout);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                supportInvalidateOptionsMenu();
            }
            public void onDrawerOpened(View view) {
                super.onDrawerOpened(view);
                supportInvalidateOptionsMenu();
            }
        };
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(drawerToggle);


        yourSubAdapter = new SubdivisionListAdapter();
        LinearLayoutManager yourSubLayoutManager = new org.solovyev.android.views.llm.LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        yourSubList = (RecyclerView) findViewById(R.id.main_yoursub_list);
        yourSubList.setLayoutManager(yourSubLayoutManager);
        yourSubList.setAdapter(yourSubAdapter);

        publicSubList = (RecyclerView) findViewById(R.id.main_publicsub_list);
        LinearLayoutManager publicSubLayoutManager = new org.solovyev.android.views.llm.LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        publicSubAdapter = new SubdivisionListAdapter();
        publicSubList.setLayoutManager(publicSubLayoutManager);
        publicSubList.setAdapter(publicSubAdapter);


        //Get users and subdivisions
        CookieRequest usersRequest = new CookieRequest(
                Request.Method.POST,
                "/f/getUsersInTeam",
                preferences,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray userArray = new JSONArray(response);
                            for (int i = 0; i < userArray.length(); i++) {
                                JSONObject userObject = userArray.getJSONObject(i);
                                MainActivity.teamUsers.put(
                                        userObject.getString("firstname") + " " + userObject.getString("lastname"),
                                        userObject.getString("_id")
                                );
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

        CookieRequest yourSubsRequest = new CookieRequest(Request.Method.POST,
                "/f/getAllSubdivisionsForUserInTeam",
                preferences,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray subdivisionArray = new JSONArray(response);
                            for (int i = 0; i < subdivisionArray.length(); i++) {
                                JSONObject subdivisionObject = subdivisionArray.getJSONObject(i);
                                MainActivity.yourSubs.put(
                                        subdivisionObject.getString("name"),
                                        subdivisionObject.getString("_id")
                                );
                            }
                            yourSubAdapter.setSubdivisions(yourSubs);
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

        CookieRequest publicSubsRequest = new CookieRequest(Request.Method.POST,
                "/f/getPublicSubdivisions",
                preferences,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray subdivisionArray = new JSONArray(response);
                            for (int i = 0; i < subdivisionArray.length(); i++) {
                                JSONObject subdivisionObject = subdivisionArray.getJSONObject(i);
                                MainActivity.publicSubs.put(
                                        subdivisionObject.getString("name"),
                                        subdivisionObject.getString("_id")
                                );
                            }
                            publicSubAdapter.setSubdivisions(publicSubs);
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
        queue.add(yourSubsRequest);
        queue.add(publicSubsRequest);
        queue.add(usersRequest);
    }

    public void profilePictureClick(View view) {
        popupMenu.show();
    }

    public void newAnnouncement(View view) {
        sectionPagerAdapter.homeFragment.openNewAnnouncement();
    }

    public void collapseNewAnnouncement(View view) {
        sectionPagerAdapter.homeFragment.collapseNewAnnouncement();
    }

    public void postAnnouncement(View view) {
        //Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        //Get message from edittext
        EditText messageBox = (EditText) sectionPagerAdapter.homeFragment.getView().findViewById(R.id.new_message);
        String message = messageBox.getText().toString();

        if (!message.isEmpty()) {
            Map<String, String> params = new HashMap<>();
            params.put("content", message);
            params.put("audience", sectionPagerAdapter.homeFragment.getCurrentPostGroup());
            System.out.println(sectionPagerAdapter.homeFragment.getCurrentPostGroup());
            CookieRequest request = new CookieRequest(Request.Method.POST, "/f/postAnnouncement", params, preferences, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    sectionPagerAdapter.homeFragment.requestAnnouncements();
                    sectionPagerAdapter.homeFragment.collapseNewAnnouncement();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Erorr posting announcement");
                    builder.setMessage("Please try again later");
                    builder.setPositiveButton("Okay", null);
                    builder.create().show();
                }
            });

            queue.add(request);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Please enter a valid message");
            builder.setPositiveButton("Okay", null);
            builder.create().show();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (sectionPagerAdapter.homeFragment.getNewAnnouncementStatus() != SlidingUpPanelLayout.PanelState.COLLAPSED) {
            sectionPagerAdapter.homeFragment.collapseNewAnnouncement();
        } else {
            super.onBackPressed();
        }
    }

    private class SectionPagerAdapter extends FragmentPagerAdapter {

        public HomeFragment homeFragment;
        public ChatFragment chatFragment;

        public SectionPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);

            homeFragment = new HomeFragment();
            chatFragment = new ChatFragment();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return homeFragment;
                case 1:
                    return chatFragment;
                default:
                    return new Fragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Home";
                case 1:
                    return "Chat";
                default:
                    return "Home";
            }
        }
    }

    public class SubdivisionListAdapter extends RecyclerView.Adapter<SubdivisionListAdapter.ViewHolder> {

        private List<Subdivision> subdivisions;

        public SubdivisionListAdapter() {
            this.subdivisions = new ArrayList<>();
        }

        public void setSubdivisions(Map<String, String> subdivisions) {
            for(Map.Entry<String, String> subdivision : subdivisions.entrySet()) {
                this.subdivisions.add(new Subdivision(subdivision.getKey(), subdivision.getValue()));
            }
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
            LinearLayout layout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.subdivisionlist_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(layout);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final Subdivision currentSubdivision = subdivisions.get(position);

            ImageView icon = (ImageView) holder.layout.findViewById(R.id.subdivisionlist_icon);
            //TODO: set subdivision icon

            TextView name = (TextView) holder.layout.findViewById(R.id.subdivisionlist_name);
            name.setText(currentSubdivision.name);

            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, SubdivisionActivity.class);
                    intent.putExtra("name", currentSubdivision.name);
                    intent.putExtra("id", currentSubdivision.id);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return subdivisions.size();
        }

        private class Subdivision {
            public final String name;
            public final String id;

            public Subdivision(String name, String id) {
                this.name = name;
                this.id = id;
            }
        }
    }
}
