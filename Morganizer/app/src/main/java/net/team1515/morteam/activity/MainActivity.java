package net.team1515.morteam.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    SectionPagerAdapter sectionPagerAdapter;

    SharedPreferences preferences;
    RequestQueue queue;
    PopupMenu popupMenu;

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
}
