package org.team1515.morteam.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.team1515.morteam.R;
import org.team1515.morteam.activity.SubdivisionActivity;
import org.team1515.morteam.entity.Subdivision;

import java.util.ArrayList;
import java.util.List;

public class SubdivisionAdapter extends RecyclerView.Adapter<SubdivisionAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView iconView;
        TextView nameView;

        ViewHolder(LinearLayout layout) {
            super(layout);

            iconView = (ImageView) layout.findViewById(R.id.subdivisionlist_icon);
            nameView = (TextView) layout.findViewById(R.id.subdivisionlist_name);

            layout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Subdivision subdivision = subdivisions.get(position);

                Intent intent = new Intent(v.getContext(), SubdivisionActivity.class);
                intent.putExtra("name", subdivision.getName());
                intent.putExtra("id", subdivision.getId());
                v.getContext().startActivity(intent);
            }
        }
    }

    private List<Subdivision> subdivisions;

    public SubdivisionAdapter() {
        this.subdivisions = new ArrayList<>();
    }

    public void setSubdivisions(List<Subdivision> subdivisions) {
        this.subdivisions = subdivisions;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_subdivision, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Subdivision subdivision = subdivisions.get(position);

        //TODO: set subdivision icon

        holder.nameView.setText(subdivision.getName());
    }

    @Override
    public int getItemCount() {
        return subdivisions.size();
    }
}
