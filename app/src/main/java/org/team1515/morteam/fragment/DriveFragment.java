package org.team1515.morteam.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
<<<<<<< HEAD
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.team1515.morteam.R;
import org.team1515.morteam.adapter.ChatAdapter;
import org.team1515.morteam.adapter.DriveAdapter;
import org.team1515.morteam.entity.Chat;
import org.team1515.morteam.entity.Drive;

import java.util.ArrayList;
import java.util.List;

public class DriveFragment extends Fragment {
    private RecyclerView driveList;
    private DriveAdapter driveAdapter;
    private LinearLayoutManager driveLayoutManager;

    private SwipeRefreshLayout refreshLayout;

    private ProgressBar progress;
    private TextView errorView;

    private List<Drive> drive;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drive, container, false);

        drive = new ArrayList<>();

        driveList = (RecyclerView) view.findViewById(R.id.drive_list);
        driveLayoutManager = new LinearLayoutManager(getContext());
        driveAdapter = new DriveAdapter(this, getContext(), drive);
        driveList.setLayoutManager(driveLayoutManager);
        driveList.setAdapter(driveAdapter);

=======
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.team1515.morteam.R;

public class DriveFragment extends Fragment {
    //This exists for the sake of existing
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drive, container, false);

>>>>>>> 1117939d1629ebded774c4c412c10000b8be809e
        return view;
    }
}
