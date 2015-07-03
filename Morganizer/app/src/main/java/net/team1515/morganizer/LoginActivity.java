package net.team1515.morganizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import net.team1515.morganizer.network.Connection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class LoginActivity extends ActionBarActivity {

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(null, 0);
        if(preferences.getBoolean("isLoggedIn", false)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_login);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loginPressed(View view) {
        EditText userBox = (EditText)findViewById(R.id.username_box);
        EditText passBox = (EditText)findViewById(R.id.password_box);
        String user = userBox.getText().toString();
        String pass = passBox.getText().toString();
        System.out.println("FOOOD" + user);

        if(user.isEmpty() || pass.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please enter a username/email and password")
                    .setTitle("Login Error");
            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.create().show();
        } else {

            try {

                Connection connection = new Connection("192.168.1.132", "/f/loginUser");
                String response = connection.execute(new BasicNameValuePair("user", user), new BasicNameValuePair("pass", pass)).get();

                //Store necessary information from login
                try {
                    JSONObject json = new JSONObject(response);
                    preferences.edit().putString("user", json.getString("user"))
                            .putString("token", json.getString("token"))
                            .putString("email", json.getString("email"))
                            .putString("teamName", json.getString("teamName"))
                            .putString("teamNumber", json.getString("teamNumber"))
                            .putString("subdivision", json.getString("subdivision"))
                            .putString("phone", json.getString("phone"))
                            .putString("first", json.getString("first"))
                            .putString("last", json.getString("last"))
                            .putBoolean("isLoggedIn", true)
                            .apply();

                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
