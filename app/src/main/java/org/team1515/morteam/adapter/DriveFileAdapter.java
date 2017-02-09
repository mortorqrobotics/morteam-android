package org.team1515.morteam.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.team1515.morteam.R;
import org.team1515.morteam.entity.MorFile;

import java.util.ArrayList;
import java.util.List;

public class DriveFileAdapter extends RecyclerView.Adapter<DriveFileAdapter.ViewHolder> {
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView filePreview;
        TextView fileName;
        ImageButton deleteButton;

        private final Context context;

        ViewHolder(CardView layout) {
            super(layout);

            context = layout.getContext();

            filePreview = (ImageView) layout.findViewById(R.id.file_preview);
            fileName = (TextView) layout.findViewById(R.id.file_name);
            deleteButton = (ImageButton) layout.findViewById(R.id.file_delete);

            layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                final MorFile file = files.get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Download '" + file.getName() + "'?");
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.morteam.com/api/files/id/" + file.getId()));
                        context.startActivity(browserIntent);
                    }
                });

                builder.setNegativeButton("Cancel", null);
                builder.create().show();
            }
        }
    }

    private List<MorFile> files;

    public DriveFileAdapter() {
        files = new ArrayList<>();
    }

    public void setFiles(List<MorFile> files) {
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
        final MorFile currentFile = files.get(position);

        if (currentFile.getFolderName().equalsIgnoreCase("personal files")) {
            holder.deleteButton.setVisibility(View.VISIBLE);
        }

        if (currentFile.getType().equals("word")) {
            holder.filePreview.setImageResource(R.drawable.doc);
        } else if (currentFile.getType().equals("pdf")) {
            holder.filePreview.setImageResource(R.drawable.pdf);
        } else if (currentFile.getMimeType().contains("jpeg") || currentFile.getType().equals("image")) {
            holder.filePreview.setImageResource(R.drawable.jpg);
        } else if (currentFile.getMimeType().contains("png")) {
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