package org.team1515.morteam.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import net.team1515.morteam.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.solovyev.android.views.llm.LinearLayoutManager;
import org.team1515.morteam.MorTeam;
import org.team1515.morteam.entity.PictureCallBack;
import org.team1515.morteam.entity.Task;
import org.team1515.morteam.entity.User;
import org.team1515.morteam.network.CookieRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ProfileActivity extends AppCompatActivity {

    SharedPreferences preferences;
    RequestQueue queue;

    User user;
    boolean isCurrentUser;

    RecyclerView pendingView;
    LinearLayoutManager pendingLayoutManager;
    TaskAdapter pendingAdapter;

    RecyclerView completedView;
    LinearLayoutManager completedLayoutManager;
    TaskAdapter completedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(null, 0);
        queue = Volley.newRequestQueue(this);

        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Get data from intent
        Intent intent = getIntent();
        final String id = intent.getStringExtra("_id");
        isCurrentUser = intent.getBooleanExtra("isCurrentUser", false);

        if (!isCurrentUser) {
            Button editProfileButton = (Button) findViewById(R.id.profile_editprofile);
            editProfileButton.setVisibility(View.GONE);

            Button changePasswordButton = (Button) findViewById(R.id.profile_changepassword);
            changePasswordButton.setVisibility(View.GONE);

            Button assignTaskButton = (Button) findViewById(R.id.profile_assigntask);
            String position = preferences.getString("position", "");
            if(position.equals("leader") || position.equals("admin")) {
                assignTaskButton.setVisibility(View.VISIBLE);
            }
        }

        Map<String, String> params = new HashMap<>();
        params.put("_id", id);

        CookieRequest userRequest = new CookieRequest(Request.Method.POST,
                "/f/getUser",
                params,
                preferences,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                        try {
                            JSONObject userObject = new JSONObject(response);
                            user = new User(userObject.getString("firstname"),
                                    userObject.getString("lastname"),
                                    id,
                                    userObject.getString("profpicpath") + "-300",
                                    userObject.getString("email"),
                                    userObject.getString("phone")
                            );

                            //Set up profile picture, name, and email
                            NetworkImageView profilePic = (NetworkImageView) findViewById(R.id.profile_picture);
                            MorTeam.setNetworkImage(user.getProfPicPath(), profilePic);

                            TextView nameView = (TextView) findViewById(R.id.profile_name);
                            nameView.setText(user.getFullName());

                            TextView emailView = (TextView) findViewById(R.id.profile_email);
                            emailView.setText(user.getEmail());

                            TextView phoneView = (TextView) findViewById(R.id.profile_phone);
                            phoneView.setText(user.getPhoneFormatted());

                            //Get attendance
                            final TextView unexcusedView = (TextView) findViewById(R.id.profile_unexcused);
                            final TextView presenceView = (TextView) findViewById(R.id.profile_presence);
                            final TextView datesView = (TextView) findViewById(R.id.profile_dates);

                            Map<String, String> params = new HashMap<>();
                            params.put("user_id", user.getId());

                            CookieRequest attendanceRequest = new CookieRequest(Request.Method.POST,
                                    "/f/getUserAbsences",
                                    params,
                                    preferences,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                JSONObject object = new JSONObject(response);
                                                double presences = object.getInt("present");
                                                JSONArray absences = object.getJSONArray("absences");

                                                String unexcusedString = absences.length() + "";
                                                unexcusedView.setText(unexcusedString);

                                                String presenceText = (int) ((presences / (presences + absences.length())) * 100) + "%";
                                                presenceView.setText(presenceText);

                                                String datesText = "";
                                                for (int i = 0; i < absences.length(); i++) {
                                                    try {
                                                        JSONObject dateObject = absences.getJSONObject(i);
                                                        String formattedDate = DateFormat.format("MMMM d, yyyy", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).parse(dateObject.getString("date").replace("Z", "+0000"))).toString();
                                                        datesText += "\t" + dateObject.getString("name") + " (" + formattedDate + ")";
                                                        if (i != absences.length() - 1) {
                                                            datesText += "\n";
                                                        }
                                                    } catch (JSONException | ParseException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                datesView.setText(datesText);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            error.printStackTrace();
                                        }
                                    }
                            );
                            queue.add(attendanceRequest);


                            //Get tasks
                            pendingView = (RecyclerView) findViewById(R.id.profile_pendingtasks);
                            pendingLayoutManager = new LinearLayoutManager(ProfileActivity.this);
                            pendingAdapter = new TaskAdapter(true);
                            pendingView.setLayoutManager(pendingLayoutManager);
                            pendingView.setAdapter(pendingAdapter);

                            completedView = (RecyclerView) findViewById(R.id.profile_completedtasks);
                            completedLayoutManager = new LinearLayoutManager(ProfileActivity.this);
                            completedAdapter = new TaskAdapter(false);
                            completedView.setLayoutManager(completedLayoutManager);
                            completedView.setAdapter(completedAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );
        queue.add(userRequest);
    }


    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void editProfileClicked(View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.dialog_editprofile, null));
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText firstNameView = (EditText) ((AlertDialog) dialog).findViewById(R.id.editprofile_firstname);
                final String firstName = firstNameView.getText().toString();
                EditText lastNameView = (EditText) ((AlertDialog) dialog).findViewById(R.id.editprofile_lastname);
                final String lastName = lastNameView.getText().toString();
                EditText emailView = (EditText) ((AlertDialog) dialog).findViewById(R.id.editprofile_email);
                final String email = emailView.getText().toString();
                EditText parentEmailView = (EditText) ((AlertDialog) dialog).findViewById(R.id.editprofile_parentemail);
                final String parentEmail = parentEmailView.getText().toString();
                EditText phoneView = (EditText) ((AlertDialog) dialog).findViewById(R.id.editprofile_phone);
                final String phone = phoneView.getText().toString();

                //This is horrible
                Map<String, String> params = new HashMap<>();
                if (firstName.isEmpty()) {
                    params.put("firstname", user.getFirstName());
                } else {
                    params.put("firstname", firstName);
                }
                if (lastName.isEmpty()) {
                    params.put("lastname", user.getLastName());
                } else {
                    params.put("lastname", lastName);
                }
                if (email.isEmpty()) {
                    params.put("email", user.getEmail());
                } else {
                    params.put("email", email);
                }
                if (!parentEmail.isEmpty()) {
                    params.put("parentEmail", parentEmail);
                }
                if (phone.isEmpty()) { //BUT WHO WAS PHONE????!!//?1/1
                    params.put("phone", user.getPhone());
                } else {
                    params.put("phone", phone);
                }

                CookieRequest changeProfileRequest = new CookieRequest(Request.Method.POST,
                        "/f/editProfile",
                        params,
                        preferences,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (response.equals("success")) {
                                    Toast.makeText(ProfileActivity.this, "Profile changed successfully.", Toast.LENGTH_SHORT).show();
                                    SharedPreferences.Editor editor = preferences.edit();
                                    String newName = "";
                                    if (!firstName.isEmpty()) {
                                        editor.putString("firstname", firstName);
                                        newName += firstName;
                                    } else {
                                        newName += user.getFirstName();
                                    }
                                    newName += " ";
                                    if (!lastName.isEmpty()) {
                                        editor.putString("lastname", lastName);
                                        newName += lastName;
                                    } else {
                                        newName += user.getLastName();
                                    }
                                    if (!email.isEmpty()) {
                                        editor.putString("email", email);
                                        TextView emailView = (TextView) ProfileActivity.this.findViewById(R.id.profile_email);
                                        emailView.setText(email);
                                    }
                                    if (!phone.isEmpty()) {
                                        editor.putString("phone", phone);
                                        TextView phoneView = (TextView) ProfileActivity.this.findViewById(R.id.profile_phone);
                                        phoneView.setText(user.formatPhoneNumber(phone));
                                    }
                                    editor.apply();
                                    TextView nameView = (TextView) ProfileActivity.this.findViewById(R.id.profile_name);
                                    nameView.setText(newName);
                                } else {
                                    Toast.makeText(ProfileActivity.this, "Failed to change profile.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                System.out.println(error);
                            }
                        }
                );
                queue.add(changeProfileRequest);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.setTitle("Edit Profile");
        builder.create().show();
    }


    public void changePasswordClicked(View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.dialog_changepassword, null));
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText oldPasswordView = (EditText) ((AlertDialog) dialog).findViewById(R.id.changepassword_old);
                String oldPassword = oldPasswordView.getText().toString();
                EditText newPasswordView = (EditText) ((AlertDialog) dialog).findViewById(R.id.changepassword_new);
                String newPassword = newPasswordView.getText().toString();
                EditText confirmPasswordView = (EditText) ((AlertDialog) dialog).findViewById(R.id.changepassword_confirm);
                String confirmPassword = confirmPasswordView.getText().toString();

                Map<String, String> params = new HashMap<>();
                if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(ProfileActivity.this, "Please make sure that you filled all required fields.", Toast.LENGTH_SHORT).show();
                } else {
                    params.put("old_password", oldPassword);
                    params.put("new_password", newPassword);
                    params.put("new_password_confirm", confirmPassword);

                    CookieRequest changePasswordRequest = new CookieRequest(Request.Method.POST,
                            "/f/changePassword",
                            params,
                            preferences,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    if (response.equals("success")) {
                                        Toast.makeText(ProfileActivity.this, "Password changes successfully.", Toast.LENGTH_SHORT).show();
                                    } else if (response.equals("fail: incorrect password")) {
                                        Toast.makeText(ProfileActivity.this, "Failed to change password. You entered an incorrect old password.", Toast.LENGTH_SHORT).show();
                                    } else if (response.equals("fail: new passwords do not match")) {
                                        Toast.makeText(ProfileActivity.this, "Failed to change password. Your new passwords did not match.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ProfileActivity.this, "Failed to change password.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    System.out.println(error);
                                }
                            }
                    );
                    queue.add(changePasswordRequest);
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.setTitle("Edit Profile");
        builder.create().show();
    }

    public void assignTask(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Assign Task");
        builder.setView(getLayoutInflater().inflate(R.layout.dialog_assigntask, null));
        builder.setPositiveButton("Assign", null);
        builder.setNegativeButton("Cancel", null);
        final AlertDialog dialog = builder.create();
        dialog.show();
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nameView = (EditText) dialog.findViewById(R.id.task_name);
                String name = nameView.getText().toString();
                if(name.isEmpty()) {
                    nameView.setHint("Please enter a name");
                    nameView.setHintTextColor(getResources().getColor(R.color.red));
                    return;
                }

                EditText descriptionView = (EditText) dialog.findViewById(R.id.task_description);
                String description = descriptionView.getText().toString();

                DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.task_date);
                Date date = new Date();
                date.setHours(0);
                date.setMinutes(0);
                date.setMonth(datePicker.getMonth());
                date.setDate(datePicker.getDayOfMonth());
                date.setYear(datePicker.getYear() - 1900);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
                dateFormat.format(date);
                String dateString = dateFormat.format(date) + "Z";
                System.out.println(dateString);

                Map<String, String> params = new HashMap<>();
                params.put("task_name", name);
                params.put("task_description", description);
                params.put("user_id", user.getId());
                params.put("due_date", dateString);
                CookieRequest assignRequest = new CookieRequest(Request.Method.POST,
                        "/f/assignTask",
                        params,
                        preferences,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                pendingAdapter.getTasks();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        }
                );
                queue.add(assignRequest);
                dialog.dismiss();
            }
        });
    }

    class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
        List<Task> tasks;
        boolean isPending;
        String path;
        TextView noneView;

        public TaskAdapter(boolean isPending) {
            tasks = new ArrayList<>();
            this.isPending = isPending;

            if (isPending) {
                path = "/f/getPendingUserTasks";
                noneView = (TextView) ProfileActivity.this.findViewById(R.id.profile_pendingnone);
            } else {
                path = "/f/getCompletedUserTasks";
                noneView = (TextView) ProfileActivity.this.findViewById(R.id.profile_completednone);
            }

            getTasks();
        }

        public void getTasks() {
            Map<String, String> params = new HashMap<>();
            params.put("user_id", user.getId());

            CookieRequest taskRequest = new CookieRequest(Request.Method.POST,
                    path,
                    params,
                    preferences,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            tasks = new ArrayList<>();
                            try {
                                JSONArray taskArray = new JSONArray(response);
                                if(taskArray.length() == 0) {
                                    noneView.setVisibility(View.VISIBLE);
                                } else {
                                    noneView.setVisibility(View.GONE);
                                    for (int i = 0; i < taskArray.length(); i++) {
                                        JSONObject taskObject = taskArray.getJSONObject(i);
                                        JSONObject creatorObject = taskObject.getJSONObject("creator");

                                        String description = "";
                                        if(taskObject.has("description")) {
                                            description = taskObject.getString("description");
                                        }

                                        tasks.add(
                                                new Task(
                                                        new User(
                                                                creatorObject.getString("firstname"),
                                                                creatorObject.getString("lastname"),
                                                                creatorObject.getString("_id"),
                                                                ""
                                                        ),
                                                        taskObject.getString("_id"),
                                                        taskObject.getString("due_date"),
                                                        taskObject.getString("name"),
                                                        description
                                                )
                                        );
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            notifyDataSetChanged();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    }
            );
            queue.add(taskRequest);
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

            TextView taskView = (TextView) holder.layout.findViewById(R.id.task_text);
            String taskString = "&#8226; " + currentTask.getTitle() + " <small>(By " +
                    currentTask.getDueDate() + ")</small>";
            if(!currentTask.getDescription().isEmpty()) {
                taskString += "<br/>\t\t<small>" + currentTask.getDescription() + "</small>";
            }
            taskView.setText(Html.fromHtml(taskString));

            Button completeButton = (Button) holder.layout.findViewById(R.id.task_button);
            if(isPending && (currentTask.getAssignerId().equals(preferences.getString("_id", ""))
                    || isCurrentUser
                    || preferences.getString("position", "").equals("leader")
                    || preferences.getString("position", "").equals("admin"))) {
                completeButton.setVisibility(View.VISIBLE);
            }
            completeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setTitle("Are you sure you want to complete this task?");
                    builder.setMessage("This action is irreversible.");
                    builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Map<String, String> params = new HashMap<>();
                            params.put("target_user", user.getId());
                            params.put("task_id", currentTask.getId());

                            CookieRequest completeRequest = new CookieRequest(Request.Method.POST,
                                    "/f/markTaskAsCompleted",
                                    params,
                                    preferences,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            getTasks();
                                            completedAdapter.getTasks();
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {

                                        }
                                    }
                            );
                            queue.add(completeRequest);
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

        public class ViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout layout;

            public ViewHolder(View itemView) {
                super(itemView);
                this.layout = (LinearLayout) itemView;
            }
        }
    }
}
