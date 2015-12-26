package org.team1515.morteam.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import net.team1515.morteam.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.team1515.morteam.entities.PictureCallBack;
import org.team1515.morteam.network.CookieRequest;
import org.team1515.morteam.network.ImageCookieRequest;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class ProfileActivity extends AppCompatActivity {

    SharedPreferences preferences;
    RequestQueue queue;


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

        //Set up profile picture, name, and email
        final ImageView profilePic = (ImageView) findViewById(R.id.profile_picture);
        String profPicPath = preferences.getString("profpicpath", "") + "-300";
        ImageCookieRequest profilePicRequest = new ImageCookieRequest("http://www.morteam.com" + profPicPath,
                preferences, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                profilePic.setImageBitmap(response);
            }
        }, 0, 0, null, Bitmap.Config.RGB_565, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });
        queue.add(profilePicRequest);

        TextView nameView = (TextView) findViewById(R.id.profile_name);
        String name = preferences.getString("firstname", "") + " " + preferences.getString("lastname", "");
        nameView.setText(name);

        TextView emailView = (TextView) findViewById(R.id.profile_email);
        String email = preferences.getString("email", "");
        emailView.setText(email);

        TextView phoneView = (TextView) findViewById(R.id.profile_phone);
        phoneView.setText(formatPhoneNumber(preferences.getString("phone", "")));

        //Get attendance
        final TextView unexcusedView = (TextView) findViewById(R.id.profile_unexcused);
        final TextView presenceView = (TextView) findViewById(R.id.profile_presence);
        final TextView datesView = (TextView) findViewById(R.id.profile_dates);

        getAttendence(preferences.getString("_id", ""), new AttendenceListener() {
            @Override
            public void onResponse(double presences, JSONArray absences) {
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
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
                datesView.setText(datesText);

            }
        });
    }

    public void getAttendence(String id, final AttendenceListener listener) {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", id);

        CookieRequest attendanceRequest = new CookieRequest(Request.Method.POST,
                "/f/getUserAbsences",
                params,
                preferences,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                        try {
                            JSONObject object = new JSONObject(response);
                            int presences = object.getInt("present");
                            JSONArray absences = object.getJSONArray("absences");
                            listener.onResponse(presences, absences);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {

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
        queue.add(attendanceRequest);
    }

    private interface AttendenceListener {
        void onResponse(double presences, JSONArray absences);
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public String formatPhoneNumber(String number) {
        String phoneString = "(" + number.substring(0, 3) + ") " + number.substring(3, 6) + "-" + number.substring(6, number.length());
        return phoneString;
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
                EditText phoneView = (EditText) ((AlertDialog) dialog).findViewById(R.id.editprofile_phone);
                final String phone = phoneView.getText().toString();

                //This is horrible
                Map<String, String> params = new HashMap<>();
                if (firstName.isEmpty()) {
                    params.put("firstname", preferences.getString("firstname", ""));
                } else {
                    params.put("firstname", firstName);
                }
                if (lastName.isEmpty()) {
                    params.put("lastname", preferences.getString("lastname", ""));
                } else {
                    params.put("lastname", lastName);
                }
                if (email.isEmpty()) {
                    params.put("email", preferences.getString("email", ""));
                } else {
                    params.put("email", email);
                }
                if (phone.isEmpty()) { //BUT WHO WAS PHONE????!!//?1/1
                    params.put("phone", preferences.getString("phone", ""));
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
                                if(response.equals("success")) {
                                    Toast.makeText(ProfileActivity.this, "Profile changed successfully.", Toast.LENGTH_SHORT).show();
                                    SharedPreferences.Editor editor = preferences.edit();
                                    String newName = "";
                                    if(!firstName.isEmpty()) {
                                        editor.putString("firstname", firstName);
                                        newName += firstName;
                                    } else {
                                        newName += preferences.getString("firstname", "");
                                    }
                                    newName += " ";
                                    if(!lastName.isEmpty()) {
                                        editor.putString("lastname", lastName);
                                        newName += lastName;
                                    } else {
                                        newName += preferences.getString("lastname", "");
                                    }
                                    if(!email.isEmpty()) {
                                        editor.putString("email", email);
                                        TextView emailView = (TextView)ProfileActivity.this.findViewById(R.id.profile_email);
                                        emailView.setText(email);
                                    }
                                    if(!phone.isEmpty()) {
                                        editor.putString("phone", phone);
                                        TextView phoneView = (TextView) ProfileActivity.this.findViewById(R.id.profile_phone);
                                        phoneView.setText(formatPhoneNumber(phone));
                                    }
                                    editor.apply();
                                    TextView nameView = (TextView)ProfileActivity.this.findViewById(R.id.profile_name);
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
                                    if(response.equals("success")) {
                                        Toast.makeText(ProfileActivity.this, "Password changes successfully.", Toast.LENGTH_SHORT).show();
                                    } else if(response.equals("fail: incorrect password")) {
                                        Toast.makeText(ProfileActivity.this, "Failed to change password. You entered an incorrect old password.", Toast.LENGTH_SHORT).show();
                                    } else if(response.equals("fail: new passwords do not match")) {
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
}
