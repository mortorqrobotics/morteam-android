package org.team1515.morteam.adapter;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import net.team1515.morteam.R;

import org.team1515.morteam.MorTeam;
import org.team1515.morteam.activity.ProfileActivity;
import org.team1515.morteam.entity.Announcement;
import org.team1515.morteam.fragment.AnnouncementFragment;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

    private List<Announcement> announcements;
    private AnnouncementFragment fragment;

    public AnnouncementAdapter(AnnouncementFragment fragment) {
        this.fragment = fragment;
        announcements = new ArrayList<>();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;

        public ViewHolder(CardView cardView) {
            super(cardView);
            this.cardView = cardView;
        }
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
                if (currentAnnouncement.getUserId().equals(fragment.preferences.getString("_id", ""))) {
                    intent.putExtra("isCurrentUser", true);
                } else {
                    intent.putExtra("isCurrentUser", false);
                }
                fragment.getContext().startActivity(intent);
            }
        };

        NetworkImageView profPic = (NetworkImageView) holder.cardView.findViewById(R.id.announcement_pic);
        MorTeam.setNetworkImage(currentAnnouncement.getProfPicPath(), profPic);
        profPic.setOnClickListener(profileListener);

        TextView author = (TextView) holder.cardView.findViewById(R.id.author);
        author.setText(currentAnnouncement.getUserName());
        author.setOnClickListener(profileListener);

        TextView date = (TextView) holder.cardView.findViewById(R.id.date);
        date.setText(currentAnnouncement.getDate());

        TextView message = (TextView) holder.cardView.findViewById(R.id.message);
        message.setMovementMethod(LinkMovementMethod.getInstance());
        message.setText(Html.fromHtml(currentAnnouncement.getContent()));

        ImageButton deleteButton = (ImageButton) holder.cardView.findViewById(R.id.delete_button);

        //Don't show delete announcement buttons if not admin
        String teamPosition = fragment.preferences.getString("position", "");
        if (!teamPosition.equals("leader")) {
            deleteButton.setClickable(false);
            deleteButton.setVisibility(View.INVISIBLE);
        } else {
            deleteButton.setOnClickListener(new View.OnClickListener() {
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