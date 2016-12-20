package org.team1515.morteam.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.team1515.morteam.fragment.AnnouncementFragment;
import org.team1515.morteam.fragment.CalendarFragment;
import org.team1515.morteam.fragment.ChatFragment;

public class MainTabAdapter extends FragmentPagerAdapter {
    public AnnouncementFragment announcementFragment;
    public ChatFragment chatFragment;
    public CalendarFragment calendarFragment;

    public MainTabAdapter(FragmentManager fragmentManager) {
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
        return 2;
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
