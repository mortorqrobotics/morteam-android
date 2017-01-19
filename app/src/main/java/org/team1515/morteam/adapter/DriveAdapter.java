package org.team1515.morteam.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.team1515.morteam.R;
import org.team1515.morteam.activity.DriveActivity;
import org.team1515.morteam.entity.Drive;
import org.team1515.morteam.fragment.DriveFragment;

import java.util.List;

public class DriveAdapter extends RecyclerView.Adapter<DriveAdapter.ViewHolder> {
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ViewHolder(LinearLayout layout) {
            super(layout);

            layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Drive drive = driveList.get(position);

                Intent intent = new Intent(context, DriveActivity.class);
                intent.putExtra("_id", drive.getId());
                context.startActivity(intent);
            }
        }
    }

    private Context context;
    private List<Drive> driveList;
    private DriveFragment fragment;

    public DriveAdapter(DriveFragment fragment, Context context, List<Drive> driveList) {
        this.fragment = fragment;
        this.context = context;
        this.driveList = driveList;
    }

    public void setDrive(List<Drive> driveList) {
        this.driveList = driveList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout view = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Drive currentDrive = driveList.get(position);
    }

    @Override
    public int getItemCount() {
        return driveList.size();
    }
}