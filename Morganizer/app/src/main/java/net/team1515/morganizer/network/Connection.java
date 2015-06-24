package net.team1515.morganizer.network;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 6/11/15.
 */
public class Connection {
    HttpClient httpClient = new DefaultHttpClient();
    // replace with your url
    HttpPost httpPost = new HttpPost("www.example.com");


    //Post Data
    List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
    nameValuePair.add(new BasicNameValuePair("username", "test_user"));
    nameValuePair.add(new BasicNameValuePair("password", "123456789"));



    //Encoding POST data
    try {
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
    } catch (UnsupportedEncodingException e) {
        // log exception
        e.printStackTrace();
    }

    //making POST request.
    try {
        HttpResponse response = httpClient.execute(httpPost);
        // write response to log
        Log.d("Http Post Response:", response.toString());
    } catch (ClientProtocolException e) {
        // Log exception
        e.printStackTrace();
    } catch (IOException e) {
        // Log exception
        e.printStackTrace();
    }

}