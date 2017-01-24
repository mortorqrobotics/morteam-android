package org.team1515.morteam.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.team1515.morteam.R;

//Pretty much here just to exist
public class DriveFileAdapter extends RecyclerView.Adapter<DriveFileAdapter.ViewHolder> {
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ViewHolder(CardView layout) {
            super(layout);
        }

        @Override
        public void onClick(View view) {

        }
    }

    public void setDrive() {

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView view = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}