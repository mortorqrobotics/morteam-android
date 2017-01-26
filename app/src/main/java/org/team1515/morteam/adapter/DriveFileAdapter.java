package org.team1515.morteam.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.team1515.morteam.R;
import org.team1515.morteam.entity.File;

import java.util.ArrayList;
import java.util.List;

public class DriveFileAdapter extends RecyclerView.Adapter<DriveFileAdapter.ViewHolder> {
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView fileName;

        ViewHolder(CardView layout) {
            super(layout);

            fileName = (TextView) layout.findViewById(R.id.file_name);

            //layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }
    }

    private List<File> files;

    public DriveFileAdapter() {
        files = new ArrayList<>();
    }

    public void setFiles(List<File> files) {
        this.files = files;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView view = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final File currentFile = files.get(position);

        holder.fileName.setText(currentFile.getName());
    }

    @Override
    public int getItemCount() {
        return 0;
    }
}