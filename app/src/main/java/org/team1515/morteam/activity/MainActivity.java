package org.team1515.morteam.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.team1515.morteam.R;
import org.team1515.morteam.adapter.MainTabAdapter;
import org.team1515.morteam.adapter.SubdivisionAdapter;
import org.team1515.morteam.entity.Subdivision;
import org.team1515.morteam.entity.User;
import org.team1515.morteam.network.CookieJsonRequest;
import org.team1515.morteam.network.CookieRequest;
import org.team1515.morteam.network.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import static org.team1515.morteam.MorTeam.preferences;
import static org.team1515.morteam.MorTeam.queue;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";

    MainTabAdapter tabAdapter;

    PopupMenu profileMenu;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;

    RecyclerView yourSubList;
    SubdivisionAdapter yourSubAdapter;
    RecyclerView publicSubList;
    SubdivisionAdapter publicSubAdapter;


    //New announcement alert
    private AlertDialog.Builder newAnnouncementBuilder;
    private AlertDialog.Builder newFolderBuilder;
    private View newAnnouncementView;
    private View newFolderView;
    private Spinner choiceSpinner;
    private JSONObject currentPostGroup = new JSONObject();

    public static final List<User> teamUsers = new ArrayList<>();
    public static List<Subdivision> yourSubs = new ArrayList<>();
    public static List<Subdivision> publicSubs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set up action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Set up action bar profile picture
        final ImageView profilePic = (ImageView) toolbar.findViewById(R.id.actionbar_pic);
        profilePic.setClickable(true);
        profilePic.setVisibility(View.VISIBLE);
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileMenu.show();
            }
        });
        Glide
                .with(this)
                .load(NetworkUtils.makePictureURL(preferences.getString("profpicpath", ""), "-60"))
                .crossFade()
                .centerCrop()
                .into(profilePic);

        //Menu
        profileMenu = new PopupMenu(this, profilePic);
        profileMenu.inflate(R.menu.menu_profile);
        profileMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.logout:
                        logout();
                        return true;
                    case R.id.view_profile:
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        intent.putExtra("_id", preferences.getString("_id", ""));
                        intent.putExtra("isCurrentUser", true); //TODO: Fix this?
                        startActivity(intent);
                        return true;
                    default:
                        return false;
                }
            }
        });


        // Set up tabs
        ViewPager viewPager = (ViewPager) findViewById(R.id.main_viewpager);
        tabAdapter = new MainTabAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        // Set up navigation drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawerLayout);
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
        drawerLayout.addDrawerListener(drawerToggle);


        yourSubAdapter = new SubdivisionAdapter();
        LinearLayoutManager yourSubLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        yourSubList = (RecyclerView) findViewById(R.id.main_yourSub_list);
        yourSubList.setLayoutManager(yourSubLayoutManager);
        yourSubList.setAdapter(yourSubAdapter);

        publicSubList = (RecyclerView) findViewById(R.id.main_publicsub_list);
        LinearLayoutManager publicSubLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        publicSubAdapter = new SubdivisionAdapter();
        publicSubList.setLayoutManager(publicSubLayoutManager);
        publicSubList.setAdapter(publicSubAdapter);

        //Create new announcement dialog
        newAnnouncementView = getLayoutInflater().inflate(R.layout.dialog_newannouncement, (ViewGroup) getWindow().getDecorView().getRootView(), false);
        newAnnouncementBuilder = new AlertDialog.Builder(this);
        newAnnouncementBuilder.setPositiveButton("Post", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                postAnnouncement();
            }
        });
        newAnnouncementBuilder.setNegativeButton("Cancel", null);
        newAnnouncementBuilder.setView(newAnnouncementView);

        //Create new folder dialog
        newFolderView = getLayoutInflater().inflate(R.layout.dialog_newfolder, (ViewGroup) getWindow().getDecorView().getRootView(), false);
        newFolderBuilder = new AlertDialog.Builder(this);
        newFolderBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addFolder();
            }
        });
        newFolderBuilder.setNegativeButton("Cancel", null);
        newFolderBuilder.setView(newFolderView);

        getSubdivisions();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onResume() {
        super.onResume();
        getSubdivisions();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    public void getSubdivisions() {
        //Get users and subdivisions
        CookieRequest usersRequest = new CookieRequest(
                Request.Method.GET,
                "/teams/current/users",
                true,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray userArray = new JSONArray(response);
                            teamUsers.clear();
                            for (int i = 0; i < userArray.length(); i++) {
                                JSONObject userObject = userArray.getJSONObject(i);
                                teamUsers.add(new User(userObject.getString("firstname"),
                                                userObject.getString("lastname"),
                                                userObject.getString("_id"),
                                                userObject.getString("profpicpath"))
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
                        error.printStackTrace();
                    }
                }
        );

        CookieRequest yourSubsRequest = new CookieRequest(Request.Method.GET,
                "/groups",
                true,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray subdivisionArray = new JSONArray(response);
                            yourSubs = new ArrayList<>();
                            for (int i = 0; i < subdivisionArray.length(); i++) {
                                JSONObject subdivisionObject = subdivisionArray.getJSONObject(i);
                                if (subdivisionObject.getString("__t").equals("NormalGroup")) {
                                    yourSubs.add(new Subdivision(subdivisionObject.getString("name"),
                                                    subdivisionObject.getString("_id"))
                                    );
                                }
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
                        error.printStackTrace();
                    }
                }
        );

        CookieRequest publicSubsRequest = new CookieRequest(Request.Method.GET,
                "/groups/other",
                true,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray subdivisionArray = new JSONArray(response);
                            publicSubs = new ArrayList<>();
                            for (int i = 0; i < subdivisionArray.length(); i++) {
                                JSONObject subdivisionObject = subdivisionArray.getJSONObject(i);
                                if (subdivisionObject.getString("__t").equals("NormalGroup")) {
                                    publicSubs.add(new Subdivision(subdivisionObject.getString("name"),
                                                    subdivisionObject.getString("_id"))
                                    );
                                }
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
                        error.printStackTrace();
                    }
                }
        );
        queue.add(yourSubsRequest);
        queue.add(publicSubsRequest);
        queue.add(usersRequest);
    }

    public void reloadData(View view) {
        tabAdapter.announcementFragment.getAnnouncements();
        tabAdapter.chatFragment.getChats();
        getSubdivisions();
    }

    public void showAnnouncementDialog() {
        //Populate choice spinner
        choiceSpinner = (Spinner) newAnnouncementView.findViewById(R.id.newAnnouncement_choiceSpinner);
        choiceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final List<String> users = new ArrayList<>();
                final List<String> groups = new ArrayList<>();

                String item = parent.getItemAtPosition(position).toString();
                if (item.equals("Everyone")) {
                    groups.add(preferences.getString("team_id", ""));
                } else if (!item.equals("Custom")) {
                    for (Subdivision subdivision : yourSubs) {
                        if (subdivision.getName().equals(item)) {
                            groups.add(subdivision.getId());
                        }
                    }
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Choose an audience");
                    final List<CharSequence> audiences = new ArrayList<>();
                    for (Subdivision subdivision : yourSubs) {
                        audiences.add(subdivision.getName());
                    }
                    for (User user : teamUsers) {
                        audiences.add(user.getFullName());
                    }
                    builder.setMultiChoiceItems(audiences.toArray(new CharSequence[audiences.size()]), null, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            String name = audiences.get(which).toString();
                            if (isChecked) {
                                boolean foundSub = false;
                                for (Subdivision subdivision : yourSubs) {
                                    if (subdivision.getName().equals(name)) {
                                        groups.add(subdivision.getId());
                                        foundSub = true;
                                        break;
                                    }
                                }
                                if (!foundSub) {
                                    for (User user : teamUsers) {
                                        if (user.getFullName().equals(name)) {
                                            users.add(user.getId());
                                            break;
                                        }
                                    }
                                }
                            } else {
                                boolean foundSub = false;
                                for (Subdivision subdivision : yourSubs) {
                                    if (subdivision.getName().equals(name)) {
                                        groups.remove(subdivision.getId());
                                        foundSub = true;
                                        break;
                                    }
                                }
                                if (!foundSub) {
                                    for (User user : teamUsers) {
                                        if (user.getFullName().equals(name)) {
                                            users.remove(user.getId());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    });
                    builder.setPositiveButton("Choose", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                            JSONArray userArray = new JSONArray();
                            for (String user : users) {
                                userArray.put(user);
                            }

                            JSONArray groupArray = new JSONArray();
                            for (String group : groups) {
                                groupArray.put(group);
                            }

                            currentPostGroup = new JSONObject();
                            try {
                                currentPostGroup.put("users", userArray);
                                currentPostGroup.put("groups", groupArray);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    builder.create().show();
                }

                JSONArray userArray = new JSONArray();
                for (String user : users) {
                    userArray.put(user);
                }

                JSONArray groupArray = new JSONArray();
                for (String group : groups) {
                    groupArray.put(group);
                }

                currentPostGroup = new JSONObject();
                try {
                    currentPostGroup.put("users", userArray);
                    currentPostGroup.put("groups", groupArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        populateChoiceSpinner();

        // Free dialog view from any previous dialogs
        ViewParent viewParent = newAnnouncementView.getParent();
        if (viewParent != null) {
            ((ViewGroup) viewParent).removeView(newAnnouncementView);
        }
        AlertDialog newAnnouncementDialog = newAnnouncementBuilder.create();
        newAnnouncementDialog.setView(newAnnouncementView);
        newAnnouncementDialog.show();
    }

    public void showFolderDialog() {
        ViewParent viewParent = newFolderView.getParent();
        if (viewParent != null) {
            ((ViewGroup) viewParent).removeView(newFolderView);
        }

        AlertDialog newFolderDialog = newFolderBuilder.create();
        newFolderDialog.setView(newFolderView);
        newFolderDialog.show();
    }

    private void populateChoiceSpinner() {
        List<String> choices = new ArrayList<>();
        choices.add("Everyone");
        for (Subdivision subdivision : yourSubs) {
            choices.add(subdivision.getName());
        }
        choices.add("Custom");
        choiceSpinner.setAdapter(
                new ArrayAdapter<>(
                        this,
                        R.layout.support_simple_spinner_dropdown_item,
                        choices)
        );
    }

    public JSONObject getCurrentPostGroup() {
        return currentPostGroup;
    }

    public void newAnnouncement(View view) {
        showAnnouncementDialog();
    }

    public void newFolder(View view) {
        showFolderDialog();
    }

    public void newChat(View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Choose chat members");
        final List<String> subdivisionIds = new ArrayList<>();
        final List<String> userIds = new ArrayList<>();
        final List<String> audiences = new ArrayList<>();
        for (Subdivision subdivision : yourSubs) {
            audiences.add(subdivision.getName());
        }
        for (User user : teamUsers) {
            if (!user.getFullName().equals(preferences.getString("firstname", "") + " " + preferences.getString("lastname", ""))) {
                audiences.add(user.getFullName());
            }
        }
        builder.setMultiChoiceItems(audiences.toArray(new CharSequence[audiences.size()]), null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                String name = audiences.get(which);
                if (isChecked) {
                    boolean foundSub = false;
                    for (Subdivision subdivision : yourSubs) {
                        if (subdivision.getName().equals(name)) {
                            subdivisionIds.add(subdivision.getId());
                            foundSub = true;
                            break;
                        }
                    }
                    if (!foundSub) {
                        for (User user : teamUsers) {
                            if (user.getFullName().equals(name)) {
                                userIds.add(user.getId());
                                break;
                            }
                        }
                    }
                } else {
                    boolean foundSub = false;
                    for (Subdivision subdivision : yourSubs) {
                        if (subdivision.getName().equals(name)) {
                            subdivisionIds.remove(subdivision.getId());
                            foundSub = true;
                            break;
                        }
                    }
                    if (!foundSub) {
                        for (User user : teamUsers) {
                            if (user.getFullName().equals(name)) {
                                userIds.remove(user.getId());
                                break;
                            }
                        }
                    }
                }
            }
        });
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (subdivisionIds.isEmpty() && userIds.isEmpty()) {
                    return;
                }

                final JSONObject params = new JSONObject();
                if (subdivisionIds.isEmpty() && userIds.size() == 1) { //If is not a group chat
                    try {
                        params.put("isTwoPeople", true);
                        params.put("otherUser", userIds.get(0));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    createNewChat(params);
                } else {
                    try {
                        params.put("isTwoPeople", false);

                        JSONObject audience = new JSONObject();

                        JSONArray usersArray = new JSONArray();
                        for (String user : userIds) {
                            usersArray.put(user);
                        }
                        audience.put("users", usersArray);

                        JSONArray groupsArray = new JSONArray();
                        for (String subdivision : subdivisionIds) {
                            groupsArray.put(subdivision);
                        }
                        audience.put("groups", groupsArray);

                        params.put("audience", audience);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    AlertDialog.Builder nameBuilder = new AlertDialog.Builder(MainActivity.this);
                    nameBuilder.setView(getLayoutInflater().inflate(R.layout.dialog_chatname, (ViewGroup) getWindow().getDecorView().getRootView(), false));
                    nameBuilder.setTitle("Type a name for your group chat");
                    nameBuilder.setNegativeButton("Cancel", null);
                    nameBuilder.setPositiveButton("Okay", null);
                    final AlertDialog nameDialog = nameBuilder.create();
                    nameDialog.show();
                    Button positiveButton = nameDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    positiveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText nameView = (EditText) nameDialog.findViewById(R.id.chatName_name);
                            String name = nameView.getText().toString();
                            if (!name.isEmpty()) {
                                try {
                                    params.put("name", name);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                nameDialog.dismiss();
                                createNewChat(params);
                            } else {
                                nameView.setHint("Please enter a name");
                                nameView.setHintTextColor(ContextCompat.getColor(MainActivity.this, R.color.red));
                            }
                        }
                    });

                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    public void createNewChat(JSONObject params) {
        CookieJsonRequest newChatRequest = new CookieJsonRequest(Request.Method.POST,
                "/chats",
                params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        tabAdapter.chatFragment.getChats();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();


                        // Handle chat errors
                        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
                        builder.setPositiveButton("Okay", null);

                        NetworkResponse response = error.networkResponse;
                        if (response != null) {
                            if (response.statusCode == 400) {
                                String message = new String(response.data);
                                System.out.println(message);
                                if (message.equals("Invalid request")) {
                                    builder.setTitle("Server error");
                                    builder.setMessage("Please try again later.");
                                }
                            } else {
                                builder.setTitle("Cannot connect to server");
                                builder.setMessage("Please make sure you have a stable internet connection.");
                            }
                        } else {
                            builder.setTitle("Cannot connect to server");
                            builder.setMessage("Please make sure you have a stable internet connection.");
                        }

                        builder.create().show();
                    }
                }
        );
        queue.add(newChatRequest);
    }

    public void postAnnouncement() {
        //Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View focus = getCurrentFocus();
        if (focus != null) {
            imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }

        //Get content from edit text
        final EditText messageBox = (EditText) newAnnouncementView.findViewById(R.id.newAnnouncement_message);
        String message = messageBox.getText().toString();

        if (!message.isEmpty()) {
            JSONObject params = new JSONObject();
            try {
                params.put("content", message);
                params.put("audience", getCurrentPostGroup());
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            System.out.println(params.toString());

            CookieJsonRequest request = new CookieJsonRequest(Request.Method.POST,
                    "/announcements",
                    params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            messageBox.setText("");
                            tabAdapter.announcementFragment.getAnnouncements();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Error posting announcement");
                            builder.setMessage("Please try again later");
                            builder.setPositiveButton("Okay", null);
                            builder.create().show();
                        }
                    }
            );
            queue.add(request);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Please enter a valid message");
            builder.setPositiveButton("Okay", null);
            builder.create().show();
        }
    }

    public void addFolder() {
        EditText addedFolderView = (EditText) newFolderView.findViewById(R.id.newFolder_name);
        String folderName = addedFolderView.getText().toString();

        System.out.println(folderName);
    }

    public void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Are you sure you want to logout?");
        builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CookieRequest logoutRequest = new CookieRequest(Request.Method.POST,
                        "/logout",
                        true,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.v(TAG, "logging out");
                                preferences.edit().clear().apply();
                                finish();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.v(TAG, "Logging out");
                                preferences.edit().clear().apply();
                                finish();
                            }
                        });
                queue.add(logoutRequest);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    public void teamClick(View view) {
        Intent intent = new Intent(this, SubdivisionActivity.class);
        intent.putExtra("isTeam", true);
        intent.putExtra("name", "Team");
        startActivity(intent);
    }

    public void onMapSearch(View view) {
        EditText searchMap = (EditText) findViewById(R.id.map_search_text);
        String searchText = searchMap.getText().toString();

        if (!searchText.equals("")) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            View focus = getCurrentFocus();
            if (focus != null) {
                imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }

            tabAdapter.mapFragment.searchLocations(searchText);
            searchMap.setText("");
        }
    }
}
