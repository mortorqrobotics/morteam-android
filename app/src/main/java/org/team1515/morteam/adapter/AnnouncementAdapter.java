package org.team1515.morteam.adapter;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import org.team1515.morteam.MorTeam;
import org.team1515.morteam.R;
import org.team1515.morteam.activity.ProfileActivity;
import org.team1515.morteam.entity.Announcement;
import org.team1515.morteam.fragment.AnnouncementFragment;

import java.util.ArrayList;
import java.util.List;

import static org.team1515.morteam.MorTeam.preferences;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {
        NetworkImageView pictureView;
        TextView authorView;
        TextView dateView;
        TextView messageView;
        ImageButton deleteButton;

        ViewHolder(CardView layout) {
            super(layout);

            pictureView = (NetworkImageView) layout.findViewById(R.id.announcement_pic);
            authorView = (TextView) layout.findViewById(R.id.author);
            dateView = (TextView) layout.findViewById(R.id.date);
            messageView = (TextView) layout.findViewById(R.id.message);
            deleteButton = (ImageButton) layout.findViewById(R.id.delete_button);
        }
    }


    private List<Announcement> announcements;
    private AnnouncementFragment fragment;

    public AnnouncementAdapter(AnnouncementFragment fragment) {
        this.fragment = fragment;
        announcements = new ArrayList<>();
    }



    public void setAnnouncements(List<Announcement> announcements) {
        this.announcements = announcements;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView view = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_announcement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Announcement currentAnnouncement = announcements.get(position);

        View.OnClickListener profileListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(fragment.getContext(), ProfileActivity.class);
                intent.putExtra("_id", currentAnnouncement.getUserId());
                if (currentAnnouncement.getUserId().equals(preferences.getString("_id", ""))) {
                    intent.putExtra("isCurrentUser", true);
                } else {
                    intent.putExtra("isCurrentUser", false);
                }
                fragment.getContext().startActivity(intent);
            }
        };

        MorTeam.setNetworkImage(currentAnnouncement.getProfPicPath(), holder.pictureView);
        holder.pictureView.setOnClickListener(profileListener);

        holder.authorView.setText(currentAnnouncement.getUserName());
        holder.authorView.setOnClickListener(profileListener);

        holder.dateView.setText(currentAnnouncement.getDate());

        holder.messageView.setMovementMethod(LinkMovementMethod.getInstance());
        holder.messageView.setText(Html.fromHtml(currentAnnouncement.getContent()));

        //Don't show delete announcement buttons if not admin
        String teamPosition = preferences.getString("position", "");
        if (!teamPosition.equals("leader")) {
            holder.deleteButton.setClickable(false);
            holder.deleteButton.setVisibility(View.INVISIBLE);
        } else {
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fragment.deleteAnnouncement(currentAnnouncement.getId(), holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return announcements.size();
    }
}