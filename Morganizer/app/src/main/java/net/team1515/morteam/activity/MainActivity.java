package net.team1515.morteam.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

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

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    SectionPagerAdapter sectionPagerAdapter;

    SharedPreferences preferences;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set up action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        sectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(sectionPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        preferences = getSharedPreferences(null, 0);
        queue = Volley.newRequestQueue(this);
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

        if(!message.isEmpty()) {
            Map<String, String> params = new HashMap<>();
            params.put("content", message);
            params.put("audience", "everyone");
            CookieRequest request = new CookieRequest(Request.Method.POST, "/f/postAnnouncement", params, preferences, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    sectionPagerAdapter.homeFragment.requestAnnouncements();
                    sectionPagerAdapter.homeFragment.collapseNewAnnouncement();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println(error);
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
