package net.team1515.morteam.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import net.team1515.morteam.R;

public class RegisterActivity extends AppCompatActivity {
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(null, 0);

        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

}
