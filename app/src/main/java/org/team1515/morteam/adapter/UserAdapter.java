package org.team1515.morteam.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import org.team1515.morteam.MorTeam;
import org.team1515.morteam.R;
import org.team1515.morteam.activity.ProfileActivity;
import org.team1515.morteam.entity.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;
        NetworkImageView imageView;
        TextView nameView;

        ViewHolder(LinearLayout layout) {
            super(layout);
            this.layout = layout;

            imageView = (NetworkImageView) layout.findViewById(R.id.userlist_icon);
            nameView = (TextView) layout.findViewById(R.id.userlist_name);
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
        ViewHolder viewHolder = new ViewHolder(layout);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final User currentUser = users.get(position);

        MorTeam.setNetworkImage(currentUser.getProfPicPath(), holder.imageView);
        holder.nameView.setText(currentUser.getFullName());

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ProfileActivity.class);
                intent.putExtra("_id", currentUser.getId());
                if (currentUser.getId().equals(MorTeam.preferences.getString("_id", ""))) {
                    intent.putExtra("isCurrentUser", true);
                } else {
                    intent.putExtra("isCurrentUser", false);
                }
                v.getContext().startActivity(intent);
            }
        });



    }

    @Override
    public int getItemCount() {
        return users.size();
    }


}