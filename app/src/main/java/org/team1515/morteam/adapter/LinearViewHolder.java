package org.team1515.morteam.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

public class LinearViewHolder extends RecyclerView.ViewHolder {
    LinearLayout layout;

    public LinearViewHolder(View itemView) {
        super(itemView);
        layout = (LinearLayout) itemView;
    }
}
