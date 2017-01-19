package org.team1515.morteam.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.team1515.morteam.fragment.AnnouncementFragment;
import org.team1515.morteam.fragment.CalendarFragment;
import org.team1515.morteam.fragment.ChatFragment;
import org.team1515.morteam.fragment.DriveFragment;

public class MainTabAdapter extends FragmentPagerAdapter {
    public AnnouncementFragment announcementFragment;
    public ChatFragment chatFragment;
    public CalendarFragment calendarFragment;
    public DriveFragment driveFragment;

    public MainTabAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);

        announcementFragment = new AnnouncementFragment();
        chatFragment = new ChatFragment();
        calendarFragment = new CalendarFragment();
        driveFragment = new DriveFragment();
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
            case 3:
                return driveFragment;
            default:
                return new Fragment();
        }
    }

    @Override
    public int getCount() {
<<<<<<< HEAD
        return 4;
=======
        return 3;
>>>>>>> 1117939d1629ebded774c4c412c10000b8be809e
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
            case 3:
                return "Drive";
            case 4:
                return "MorMap";
            default:
                return "Home";
        }
    }
}
