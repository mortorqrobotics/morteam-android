package net.team1515.morteam.activity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import net.team1515.morteam.R;
import net.team1515.morteam.network.ImageCookieRequest;


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
        final ImageView profilePic = (ImageView)findViewById(R.id.profile_picture);
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

        TextView nameView = (TextView)findViewById(R.id.profile_name);
        String name = preferences.getString("firstname", "") + " " + preferences.getString("lastname", "");
        nameView.setText(name);

        TextView emailView = (TextView)findViewById(R.id.profile_email);
        String email = preferences.getString("email", "");
        emailView.setText(email);
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void editProfileClicked(View view) {
        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
    }

    public void changePasswordClicked(View view) {
        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
    }
}
