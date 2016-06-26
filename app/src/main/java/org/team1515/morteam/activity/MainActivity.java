package org.team1515.morteam.activity;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import net.team1515.morteam.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.team1515.morteam.entity.Subdivision;
import org.team1515.morteam.entity.User;
import org.team1515.morteam.fragment.CalendarFragment;
import org.team1515.morteam.fragment.ChatFragment;
import org.team1515.morteam.fragment.AnnouncementFragment;
import org.team1515.morteam.network.CookieRequest;
import org.team1515.morteam.network.ImageCookieRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
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


    //New announcement alert
    public AlertDialog.Builder announcementBuilder;
    public View newAnnouncementView;
    private Spinner choiceSpinner;
    private String currentPostGroup;
    private List<String> choices = new ArrayList<>();

    public static final List<User> teamUsers = new ArrayList<>();
    public static List<Subdivision> yourSubs = new ArrayList<>();
    public static List<Subdivision> publicSubs = new ArrayList<>();

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
        ImageCookieRequest profilePicRequest = new ImageCookieRequest("http://www.morteam.com" + preferences.getString("profpicpath", "") + "-60",
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
                                        "/logout",
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
                        intent.putExtra("_id", preferences.getString("_id", ""));
                        intent.putExtra("isCurrentUser", true);
                        startActivity(intent);
                        return true;
                    default:
                        return false;
                }
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        sectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.main_viewpager);
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


        //Create new announcement dialog
        announcementBuilder = new AlertDialog.Builder(this);
        announcementBuilder.setPositiveButton("Post", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                postAnnouncement();
            }
        });
        announcementBuilder.setNegativeButton("Cancel", null);

        getSubdivisions();
    }

    public void onResume() {
        super.onResume();
        getSubdivisions();
    }

    public void getSubdivisions() {
        //Get users and subdivisions
        CookieRequest usersRequest = new CookieRequest(
                Request.Method.GET,
                "/teams/current/users",
                preferences,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray userArray = new JSONArray(response);
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
                        System.out.println(error);
                    }
                }
        );

        CookieRequest yourSubsRequest = new CookieRequest(Request.Method.GET,
                "/subdivisions/joined",
                preferences,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray subdivisionArray = new JSONArray(response);
                            yourSubs = new ArrayList<>();
                            for (int i = 0; i < subdivisionArray.length(); i++) {
                                JSONObject subdivisionObject = subdivisionArray.getJSONObject(i);
                                yourSubs.add(new Subdivision(subdivisionObject.getString("name"),
                                                subdivisionObject.getString("_id"))
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

        CookieRequest publicSubsRequest = new CookieRequest(Request.Method.GET,
                "/subdivisions/public",
                preferences,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray subdivisionArray = new JSONArray(response);
                            publicSubs = new ArrayList<>();
                            for (int i = 0; i < subdivisionArray.length(); i++) {
                                JSONObject subdivisionObject = subdivisionArray.getJSONObject(i);
                                publicSubs.add(new Subdivision(subdivisionObject.getString("name"),
                                                subdivisionObject.getString("_id"))
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
                        error.printStackTrace();
                    }
                }
        );
        queue.add(yourSubsRequest);
        queue.add(publicSubsRequest);
        queue.add(usersRequest);
    }

    public void reloadData(View view) {
        sectionPagerAdapter.announcementFragment.getAnnouncements();
        sectionPagerAdapter.chatFragment.getChats();
        getSubdivisions();
    }

    public void showAnnouncementDialog() {

        //Create new announcement dialog
        newAnnouncementView = getLayoutInflater().inflate(R.layout.dialog_newannouncement, null);

        //Populate choice spinner
        choiceSpinner = (Spinner) newAnnouncementView.findViewById(R.id.announcement_choicespinner);
        choiceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                if (item.equals("Everyone")) {
                    currentPostGroup = "everyone";
                } else if (!item.equals("Custom")) {
                    for (Subdivision subdivision : yourSubs) {
                        if (subdivision.getName().equals(item)) {
                            currentPostGroup = subdivision.getId();
                        }
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Choose an audience");
                    final List<String> subdivisionIds = new ArrayList<>();
                    final List<String> userIds = new ArrayList<>();
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
                    builder.setPositiveButton("Choose", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            currentPostGroup = "{\"subdivisionMembers\":[";
                            for (String id : subdivisionIds) {
                                currentPostGroup += "\"" + id + "\",";
                            }
                            if (!subdivisionIds.isEmpty()) {
                                currentPostGroup = currentPostGroup.substring(0, currentPostGroup.length() - 1);
                            }
                            currentPostGroup += "],\"userMembers\":[\"" + preferences.getString("_id", "") + "\",";
                            for (String id : userIds) {
                                currentPostGroup += "\"" + id + "\",";
                            }
                            currentPostGroup = currentPostGroup.substring(0, currentPostGroup.length() - 1);
                            currentPostGroup += "]}";
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    builder.create().show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        populateChoiceSpinner();

        announcementBuilder.setView(newAnnouncementView);
        announcementBuilder.create().show();
    }

    private void populateChoiceSpinner() {
        choices = new ArrayList<>();
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

    public String getCurrentPostGroup() {
        return currentPostGroup;
    }

    public void profilePictureClick(View view) {
        popupMenu.show();
    }

    public void newAnnouncement(View view) {
        showAnnouncementDialog();
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

                final Map<String, String> params = new HashMap<>();
                if (subdivisionIds.isEmpty() && userIds.size() == 1) { //If is not a group chat
                    params.put("user2", userIds.get(0));
                    params.put("type", "private");

                    createNewChat(params);
                } else {
                    params.put("type", "group");

                    JSONArray subdivisionsArray = new JSONArray();
                    for (String id : subdivisionIds) {
                        subdivisionsArray.put(id);
                    }
                    params.put("subdivisionMembers", subdivisionsArray.toString());

                    JSONArray userArray = new JSONArray();
                    for (String id : userIds) {
                        userArray.put(id);
                    }
                    userArray.put(preferences.getString("_id", ""));
                    params.put("userMembers", userArray.toString());

                    AlertDialog.Builder nameBuilder = new AlertDialog.Builder(MainActivity.this);
                    nameBuilder.setView(getLayoutInflater().inflate(R.layout.dialog_chatname, null));
                    nameBuilder.setTitle("Type a name for your group chat");
                    nameBuilder.setNegativeButton("Cancel", null);
                    nameBuilder.setPositiveButton("Okay", null);
                    final AlertDialog nameDialog = nameBuilder.create();
                    nameDialog.show();
                    Button positiveButton = nameDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    positiveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText nameView = (EditText) ((AlertDialog) nameDialog).findViewById(R.id.chatname_name);
                            String name = nameView.getText().toString();
                            if (!name.isEmpty()) {
                                params.put("name", name);
                                nameDialog.dismiss();
                                createNewChat(params);
                            } else {
                                nameView.setHint("Please enter a name");
                                nameView.setHintTextColor(getResources().getColor(R.color.red));
                            }
                        }
                    });

                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    public void createNewChat(Map<String, String> params) {
        System.out.println(params.get("userMembers"));
        CookieRequest newChatRequest = new CookieRequest(Request.Method.POST,
                "/f/createChat",
                params,
                preferences,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                        if (!response.equals("fail")) {
                            sectionPagerAdapter.chatFragment.getChats();
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
        queue.add(newChatRequest);
    }

    public void postAnnouncement() {
        //Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        //Get content from edittext
        EditText messageBox = (EditText) newAnnouncementView.findViewById(R.id.announcement_message);
        String message = messageBox.getText().toString();

        if (!message.isEmpty()) {
            Map<String, String> params = new HashMap<>();
            params.put("content", message);
            params.put("audience", getCurrentPostGroup());
            CookieRequest request = new CookieRequest(Request.Method.POST, "/announcement", params, preferences, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    sectionPagerAdapter.announcementFragment.getAnnouncements();
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

    public void teamClick(View view) {
        Intent intent = new Intent(this, SubdivisionActivity.class);
        intent.putExtra("isTeam", true);
        intent.putExtra("name", "Team");
        startActivity(intent);
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

    private class SectionPagerAdapter extends FragmentPagerAdapter {

        public AnnouncementFragment announcementFragment;
        public ChatFragment chatFragment;
        public CalendarFragment calendarFragment;

        public SectionPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);

            announcementFragment = new AnnouncementFragment();
            chatFragment = new ChatFragment();
            calendarFragment = new CalendarFragment();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return announcementFragment;
                case 1:
                    return chatFragment;
                case 2:
                    return calendarFragment;
                default:
                    return new Fragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Home";
                case 1:
                    return "Chat";
                case 2:
                    return "Calendar";
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

        public void setSubdivisions(List<Subdivision> subdivisions) {
            this.subdivisions = subdivisions;
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
            LinearLayout layout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_subdivision, parent, false);
            ViewHolder viewHolder = new ViewHolder(layout);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final Subdivision currentSubdivision = subdivisions.get(position);

            ImageView icon = (ImageView) holder.layout.findViewById(R.id.subdivisionlist_icon);
            //TODO: set subdivision icon

            TextView name = (TextView) holder.layout.findViewById(R.id.subdivisionlist_name);
            name.setText(currentSubdivision.getName());

            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, SubdivisionActivity.class);
                    intent.putExtra("name", currentSubdivision.getName());
                    intent.putExtra("id", currentSubdivision.getId());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return subdivisions.size();
        }
    }
}
