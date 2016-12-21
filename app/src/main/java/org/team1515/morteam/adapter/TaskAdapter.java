package org.team1515.morteam.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.team1515.morteam.R;

import org.team1515.morteam.MorTeam;
import org.team1515.morteam.activity.ProfileActivity;
import org.team1515.morteam.entity.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;
        TextView taskView;
        Button completeButton;

        ViewHolder(LinearLayout layout) {
            super(layout);

            this.layout = layout;

            taskView = (TextView) layout.findViewById(R.id.task_text);
            completeButton = (Button) layout.findViewById(R.id.task_button);
        }
    }

    private ProfileActivity activity;
    private List<Task> tasks;
    private boolean isPending;
    private String path;
    private TextView noneView;

    public TaskAdapter(ProfileActivity activity, boolean isPending) {
        this.activity = activity;
        tasks = new ArrayList<>();
        this.isPending = isPending;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_task, parent, false);
        ViewHolder viewHolder = new ViewHolder(layout);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Task currentTask = tasks.get(position);

        String taskString = "&#8226; " + currentTask.getTitle() + " <small>(By " +
                currentTask.getDueDate() + ")</small>";
        if(!currentTask.getDescription().isEmpty()) {
            taskString += "<br/>\t\t<small>" + currentTask.getDescription() + "</small>";
        }
        holder.taskView.setText(Html.fromHtml(taskString));

        if(isPending && (currentTask.getAssignerId().equals(MorTeam.preferences.getString("_id", ""))
                || activity.isCurrentUser
                || MorTeam.preferences.getString("position", "").equals("leader")
                || MorTeam.preferences.getString("position", "").equals("admin"))) {
            holder.completeButton.setVisibility(View.VISIBLE);
        }
        holder.completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Are you sure you want to complete this task?");
                builder.setMessage("This action is irreversible.");
                builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.completeTask(currentTask.getId());
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.create().show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }
}
