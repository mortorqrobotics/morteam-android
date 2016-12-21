package org.team1515.morteam.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.bumptech.glide.Glide;

import org.team1515.morteam.MorTeam;
import org.team1515.morteam.R;
import org.team1515.morteam.activity.ProfileActivity;
import org.team1515.morteam.entity.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Context context;
        ImageView imageView;
        TextView nameView;

        ViewHolder(LinearLayout layout) {
            super(layout);

            context = layout.getContext();
            imageView = (ImageView) layout.findViewById(R.id.userlist_icon);
            nameView = (TextView) layout.findViewById(R.id.userlist_name);

            layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                User user = users.get(position);

                Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                intent.putExtra("_id", user.getId());
                if (user.getId().equals(MorTeam.preferences.getString("_id", ""))) {
                    intent.putExtra("isCurrentUser", true);
                } else {
                    intent.putExtra("isCurrentUser", false);
                }
                v.getContext().startActivity(intent);
            }
        }
    }

    private List<User> users;

    public UserAdapter() {
        this.users = new ArrayList<>();
    }

    public void setUsers(List<User> users, Activity activity) {
        this.users = users;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_user, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final User user = users.get(position);

        Glide
                .with(holder.context)
                .load(user.getProfPicPath())
                .centerCrop()
                .crossFade()
                .into(holder.imageView);

        holder.nameView.setText(user.getFullName());
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


}