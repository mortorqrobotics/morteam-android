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

    private List<Subdivision> subdivisions;

    public SubdivisionAdapter() {
        this.subdivisions = new ArrayList<>();
    }

    public void setSubdivisions(List<Subdivision> subdivisions) {
        this.subdivisions = subdivisions;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout layout;

        public ViewHolder(LinearLayout layout) {
            super(layout);
            this.layout = layout;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_subdivision, parent, false);
        ViewHolder viewHolder = new ViewHolder(layout);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Subdivision currentSubdivision = subdivisions.get(position);

        ImageView icon = (ImageView) holder.layout.findViewById(R.id.subdivisionlist_icon);
        //TODO: set subdivision icon

        TextView name = (TextView) holder.layout.findViewById(R.id.subdivisionlist_name);
        name.setText(currentSubdivision.getName());

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.layout.getContext(), SubdivisionActivity.class);
                intent.putExtra("name", currentSubdivision.getName());
                intent.putExtra("id", currentSubdivision.getId());
                holder.layout.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subdivisions.size();
    }
}
