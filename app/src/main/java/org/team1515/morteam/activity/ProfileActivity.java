package org.team1515.morteam.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;

import org.team1515.morteam.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.team1515.morteam.MorTeam;
import org.team1515.morteam.adapter.TaskAdapter;
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

    User user;
    public boolean isCurrentUser;

    List<Task> tasks;

    RecyclerView pendingView;
    LinearLayoutManager pendingLayoutManager;
    TaskAdapter pendingAdapter;
    TextView pendingNoneView;

    RecyclerView completedView;
    LinearLayoutManager completedLayoutManager;
    TaskAdapter completedAdapter;
    TextView completedNoneView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Profile");
        }

        //Get data from intent
        Intent intent = getIntent();
        final String id = intent.getStringExtra("_id");
        isCurrentUser = intent.getBooleanExtra("isCurrentUser", false);

        if (!isCurrentUser) {
            LinearLayout editButtons = (LinearLayout) findViewById(R.id.profile_editButtons);
            editButtons.setVisibility(View.GONE);

            Button assignTaskButton = (Button) findViewById(R.id.profile_assignTask);
            String position = MorTeam.preferences.getString("position", "");
            if(position.equals("leader") || position.equals("admin")) {
                assignTaskButton.setVisibility(View.VISIBLE);
            }
        }

        CookieRequest userRequest = new CookieRequest(Request.Method.GET,
                "/users/id/" + id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject userObject = new JSONObject(response);
                            String parentEmail;
                            if (userObject.has("parentEmail")) {
                                parentEmail = userObject.getString("parentEmail");
                            } else {
                                parentEmail = "";
                            }
                            user = new User(userObject.getString("firstname"),
                                    userObject.getString("lastname"),
                                    id,
                                    userObject.getString("profpicpath") + "-300",
                                    userObject.getString("email"),
                                    parentEmail,
                                    userObject.getString("phone")
                            );

                            //Set up profile picture, name, and email
                            ImageView profilePic = (ImageView) findViewById(R.id.profile_picture);
                            Glide
                                    .with(ProfileActivity.this)
                                    .load(user.getProfPicPath())
                                    .centerCrop()
                                    .crossFade()
                                    .into(profilePic);

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

                            CookieRequest attendanceRequest = new CookieRequest(Request.Method.GET,
                                    "/users/id/" + id + "/absences",
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
                                            //setup
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            error.printStackTrace();
                                        }
                                    }
                            );
                            MorTeam.queue.add(attendanceRequest);
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
        MorTeam.queue.add(userRequest);

        setupTasks(id);
    }

    private void setupTasks(String id) {
        //Get tasks
        pendingView = (RecyclerView) findViewById(R.id.profile_pendingTasks);
        pendingLayoutManager = new LinearLayoutManager(ProfileActivity.this);
        pendingAdapter = new TaskAdapter(this, true);
        pendingView.setLayoutManager(pendingLayoutManager);
        pendingView.setAdapter(pendingAdapter);
        pendingNoneView = (TextView) findViewById(R.id.profile_pendingNone);

        completedView = (RecyclerView) findViewById(R.id.profile_completedTasks);
        completedLayoutManager = new LinearLayoutManager(ProfileActivity.this);
        completedAdapter = new TaskAdapter(this, false);
        completedView.setLayoutManager(completedLayoutManager);
        completedView.setAdapter(completedAdapter);
        completedNoneView = (TextView) findViewById(R.id.profile_completedNone);

        tasks = new ArrayList<>();

        getTasks(true, id);
        getTasks(false, id);
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
                EditText firstNameView = (EditText) ((AlertDialog) dialog).findViewById(R.id.editProfile_firstName);
                final String firstName = firstNameView.getText().toString();
                EditText lastNameView = (EditText) ((AlertDialog) dialog).findViewById(R.id.editProfile_lastName);
                final String lastName = lastNameView.getText().toString();
                EditText emailView = (EditText) ((AlertDialog) dialog).findViewById(R.id.editProfile_email);
                final String email = emailView.getText().toString();
                EditText parentEmailView = (EditText) ((AlertDialog) dialog).findViewById(R.id.editProfile_parentEmail);
                final String parentEmail = parentEmailView.getText().toString();
                EditText phoneView = (EditText) ((AlertDialog) dialog).findViewById(R.id.editProfile_phone);
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
                if (parentEmail.isEmpty()) {
                    params.put("parentEmail", user.getParentEmail());
                } else {
                    params.put("parentEmail", parentEmail);
                }
                if (phone.isEmpty()) { //BUT WHO WAS PHONE????!!//?1/1
                    params.put("phone", user.getPhone());
                } else {
                    params.put("phone", phone);
                }


                for (String stuff : params.keySet()) {
                    System.out.println(stuff + "\t" + params.get(stuff));
                }
                CookieRequest changeProfileRequest = new CookieRequest(Request.Method.PUT,
                        "/profile",
                        params,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject userObject = new JSONObject(response);

                                    Toast.makeText(ProfileActivity.this, "Profile changed successfully.", Toast.LENGTH_SHORT).show();
                                    SharedPreferences.Editor editor = MorTeam.preferences.edit();
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
                                } catch (JSONException e) {
                                    Toast.makeText(ProfileActivity.this, "Failed to change profile.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                NetworkResponse response = error.networkResponse;
                                if (response != null) {
                                    if (response.statusCode == 400) {
                                        String message = new String(response.data);
                                        System.out.println(message);
                                    }
                                }
                            }
                        }
                );
                MorTeam.queue.add(changeProfileRequest);
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
                EditText oldPasswordView = (EditText) ((AlertDialog) dialog).findViewById(R.id.changePassword_old);
                String oldPassword = oldPasswordView.getText().toString();
                EditText newPasswordView = (EditText) ((AlertDialog) dialog).findViewById(R.id.changePassword_new);
                String newPassword = newPasswordView.getText().toString();
                EditText confirmPasswordView = (EditText) ((AlertDialog) dialog).findViewById(R.id.changePassword_confirm);
                String confirmPassword = confirmPasswordView.getText().toString();

                Map<String, String> params = new HashMap<>();
                if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(ProfileActivity.this, "Please make sure that you filled all required fields.", Toast.LENGTH_SHORT).show();
                } else if (oldPassword.equals(confirmPassword)) {
                    Toast.makeText(ProfileActivity.this, "Please make sure your password are the same.", Toast.LENGTH_SHORT).show();
                } else {
                    params.put("oldPassword", oldPassword);
                    params.put("newPassword", newPassword);

                    CookieRequest changePasswordRequest = new CookieRequest(Request.Method.PUT,
                            "/password",
                            params,
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
                    MorTeam.queue.add(changePasswordRequest);
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
                params.put("name", name);
                params.put("description", description);
                params.put("dueDate", dateString);
                CookieRequest assignRequest = new CookieRequest(Request.Method.POST,
                        "/users/id/" + user.getId() + "/tasks",
                        params,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                getTasks(true, user.getId());
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        }
                );
                MorTeam.queue.add(assignRequest);
                dialog.dismiss();
            }
        });
    }

    public void getTasks(final boolean isPending, String id) {
        String path = "/users/id/" + id + "/tasks/" + (isPending ? "pending" : "completed");

        CookieRequest taskRequest = new CookieRequest(Request.Method.GET,
                path,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        tasks = new ArrayList<>();
                        try {
                            JSONArray taskArray = new JSONArray(response);
                            if(taskArray.length() == 0) {
                                if (isPending) {
                                    pendingNoneView.setVisibility(View.VISIBLE);
                                } else {
                                    completedNoneView.setVisibility(View.VISIBLE);
                                }
                            } else {
                                if (isPending) {
                                    pendingNoneView.setVisibility(View.GONE);
                                } else {
                                    completedNoneView.setVisibility(View.GONE);
                                }

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
                                                            "   "
                                                    ),
                                                    taskObject.getString("_id"),
                                                    taskObject.getString("dueDate"),
                                                    taskObject.getString("name"),
                                                    description
                                            )
                                    );
                                }
                            }

                            if (isPending) {
                                pendingAdapter.setTasks(tasks);
                                pendingAdapter.notifyDataSetChanged();
                            } else {
                                completedAdapter.setTasks(tasks);
                                completedAdapter.notifyDataSetChanged();
                            }
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
        MorTeam.queue.add(taskRequest);
    }

    public void completeTask(String taskId) {
        CookieRequest completeRequest = new CookieRequest(Request.Method.POST,
                "/tasks/id/" + taskId + "/markCompleted",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getTasks(true, user.getId());
                        getTasks(false, user.getId());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );
        MorTeam.queue.add(completeRequest);
    }

}
