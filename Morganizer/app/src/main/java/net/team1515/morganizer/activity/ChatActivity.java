package net.team1515.morganizer.activity;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import net.team1515.morganizer.R;
import net.team1515.morganizer.network.Connection;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class ChatActivity extends ActionBarActivity {

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        preferences = getSharedPreferences(null, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
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


    public void chatSend(View view) {
        Connection connection = new Connection("/f/getgroupchats");
        String user = preferences.getString("user", "");
        String teamCode = preferences.getString("teamCode", "");
        try {
            String response = connection.execute(new BasicNameValuePair("user", user), new BasicNameValuePair("teamCode", teamCode)).get();
            System.out.println(response);
            //If we get some actual chats
            if(!response.equals("[]")) {
                JSONObject json = new JSONObject(response.substring(1, response.length() - 1)); //Substring to get rid of first and last brackets
                String groupName = json.getString("groupName");
                String groupID = json.getString("groupID");
            } else {
                //TODO: display no chats
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
