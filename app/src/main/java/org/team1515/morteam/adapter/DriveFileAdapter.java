package org.team1515.morteam.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.team1515.morteam.R;
import org.team1515.morteam.entity.File;

import java.util.ArrayList;
import java.util.List;

public class DriveFileAdapter extends RecyclerView.Adapter<DriveFileAdapter.ViewHolder> {
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView filePreview;
        TextView fileName;

        private final Context context;

        ViewHolder(CardView layout) {
            super(layout);

            context = layout.getContext();

            filePreview = (ImageView) layout.findViewById(R.id.file_preview);
            fileName = (TextView) layout.findViewById(R.id.file_name);

            layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                File file = files.get(position);

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.morteam.com/api/files/id/" + file.getId()));
                context.startActivity(browserIntent);
            }
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

        if (currentFile.getType().equals("word")) {
            holder.filePreview.setImageResource(R.drawable.doc);
        } else if (currentFile.getType().equals("pdf")) {
            holder.filePreview.setImageResource(R.drawable.pdf);
        } else if (currentFile.getMimeType().substring(6).equals("pjpeg")) {
            holder.filePreview.setImageResource(R.drawable.jpg);
        } else if (currentFile.getMimeType().substring(6).equals("png")) {
            holder.filePreview.setImageResource(R.drawable.png);
        } else if (currentFile.getType().equals("audio")) {
            holder.filePreview.setImageResource(R.drawable.mp3);
        } else if (currentFile.getType().equals("video")) {
            holder.filePreview.setImageResource(R.drawable.mp4);
        } else {
            holder.filePreview.setImageResource(R.drawable.file);

            System.out.println("FILE TYPE: " + currentFile.getType());
            System.out.println("FILE MIME TYPE: " + currentFile.getMimeType());
        }

        holder.fileName.setText(currentFile.getName());
    }

    @Override
    public int getItemCount() {
        return files.size();
    }
}