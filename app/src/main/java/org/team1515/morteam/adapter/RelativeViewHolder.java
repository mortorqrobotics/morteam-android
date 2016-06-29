package org.team1515.morteam.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

public class RelativeViewHolder extends RecyclerView.ViewHolder {
    public RelativeLayout layout;

    public RelativeViewHolder(View itemView) {
        super(itemView);
        layout = (RelativeLayout) itemView;
    }
}
